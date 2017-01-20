package hr.fer.tel.hmo.solution;

import hr.fer.tel.hmo.network.Link;
import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.solution.routing.Route;
import hr.fer.tel.hmo.util.Matrix;
import hr.fer.tel.hmo.util.Util;
import hr.fer.tel.hmo.vnf.Component;
import hr.fer.tel.hmo.vnf.ServiceChain;

import java.util.*;
import java.util.function.Function;

/**
 * This class knows how to evaluate given placement and routing
 */
public class Evaluator {

	/**
	 * Network topology
	 */
	private final Topology topology;

	public Evaluator(Topology topology) {
		this.topology = topology;
	}

	/**
	 * @param solution solution
	 * @return fitness
	 */
	public double fitness(Solution solution) {
		return -evaluate(solution);
	}

	/**
	 * Assert that solution is correct
	 *
	 * @param solution solution you want to check
	 */
	public void assertSolution(Solution solution) {
		if (!isValid(solution.getPlacement())) {
			throw new RuntimeException("Placement invalid");
		}
		if (!isLatencyValid(solution)) {
			throw new RuntimeException("Latency invalid");
		}
		if (!isBandwidthValid(solution)) {
			throw new RuntimeException("Bandwidth invalid");
		}

		final Placement p = solution.getPlacement();

		Function<Integer, Integer> compToNode =
				c -> topology.getNetwork().getServer(p.getPlacementFor(c)).getNode().getIndex();

		for (Route r : solution.getRoutes().values()) {
			int[] nodes = r.getNodes();

			if (nodes[0] != compToNode.apply(r.getFrom())) {
				throw new RuntimeException("Doesn't start from given node");
			}

			if (nodes[nodes.length - 1] != compToNode.apply(r.getTo())) {
				throw new RuntimeException("Doesn't end in given node");
			}

			for (int i = 1; i < nodes.length; i++) {
				if (topology.getNetwork().getLink(nodes[i - 1], nodes[i]) == null) {
					throw new RuntimeException("Nodes arent connected");
				}
			}
		}
	}

	/**
	 * Evaluates given placement and routing.
	 * This is done by calculating total power consupmtion:
	 * 1. power used by servers
	 * 2. power used by links
	 * 3. power used by nodes
	 *
	 * @param solution possible solution
	 * @return total power consumption
	 */
	private double evaluate(Solution solution) {
		Placement placement = solution.getPlacement();
		Matrix<Integer, Integer, Route> routes = solution.getRoutes();

		double sol = 0.0;

		BitSet usedNodes = new BitSet(topology.getNetwork().getNumberOfNodes());
		BitSet usedServers = new BitSet(topology.getNetwork().getNumberOfServers());
		Set<Link> usedLinks = new HashSet<>();

		for (Component c : topology.getComponents()) {
			int serverIndex = placement.getPlacementFor(c);
			usedServers.set(serverIndex);

			sol += topology.getNetwork().getServer(serverIndex).getAdditionalPower(c);
		}

		for (Integer from : routes.keys()) {
			for (Route r : routes.valuesFor(from)) {
				int[] nodes = r.getNodes();
				if (nodes.length == 1) {
					continue; // both topology.getComponents() on same node
				}

				// mark nodes as used
				// add used link powers
				usedNodes.set(nodes[0]);
				for (int i = 1; i < nodes.length; i++) {
					usedLinks.add(topology.getNetwork().getLink(nodes[i - 1], nodes[i]));
					usedNodes.set(nodes[i]);
				}
			}
		}

		// power used by links
		sol += usedLinks.parallelStream().mapToDouble(Link::getPowerConsumption).sum();

		// minimal power used by server (only count those which are on)
		sol += usedServers.stream().parallel().mapToDouble(i -> topology.getNetwork().getServer(i).getPmin()).sum();

		// power used by nodes (only count those which are used)
		sol += usedNodes.stream().parallel().mapToDouble(i -> topology.getNetwork().getNode(i).getPowerConsumption()).sum();

		return sol;
	}

	/**
	 * Test if placement of topology.getComponents() is valid (look at used resources)
	 *
	 * @param placement placement of topology.getComponents() onto servers
	 * @return true if placement is valid
	 */
	public boolean isValid(Placement placement) {
		List<List<Double>> res = new ArrayList<>();
		int S = topology.getNetwork().getNumberOfServers();
		for (int s = 0; s < S; s++) {
			res.add(new ArrayList<>(topology.getNetwork().getServer(s).getResources()));
		}

		for (Component c : topology.getComponents()) {
			int s = placement.getPlacementFor(c);
			List<Double> serverAvailable = res.get(s);
			int R = serverAvailable.size();
			for (int r = 0; r < R; r++) {
				double left = serverAvailable.get(r) - c.getResources().get(r);
				if (left < 0 && left < -Util.EPS) {
					return false;
				}
				serverAvailable.set(r, left);
			}
		}
		return true;
	}

	/**
	 * Check if all service chains have allowed latency
	 *
	 * @param s possible solution
	 * @return true if all service chains have valid latency
	 */
	private boolean isLatencyValid(Solution s) {
		return topology.getServiceChains().parallelStream()
				.allMatch(sc -> isLatencyValidForServiceChain(s, sc));
	}

	/**
	 * Check latency for service chain
	 *
	 * @param solution possible solution
	 * @param sc       service chain
	 * @return whether latency is below maximal allowed
	 */
	private boolean isLatencyValidForServiceChain(Solution solution, ServiceChain sc) {
		Matrix<Integer, Integer, Route> routes = solution.getRoutes();

		double delay = 0.0;
		double latency = sc.getLatency();

		int ncs = sc.getNumberOfComponents();

		int prevCompIdx = sc.getComponent(0).getIndex();
		for (int i = 1; i < ncs; i++) {
			int currCompIdx = sc.getComponent(i).getIndex();

			Route r = routes.get(prevCompIdx, currCompIdx);
			if (r == null) {
				throw new RuntimeException();
			}

			int[] nodes = r.getNodes();
			for (int j = 1; j < nodes.length; j++) {
				delay += topology.getNetwork().getLink(nodes[j - 1], nodes[j]).getDelay();
			}

			if (delay > latency) {
				return false;
			}

			prevCompIdx = currCompIdx;
		}

		return true;
	}

	/**
	 * Check is demanded bandwidth ok
	 *
	 * @param solution possible solution
	 * @return true if bandwidth is ok
	 */
	private boolean isBandwidthValid(Solution solution) {

		Matrix<Integer, Integer, Route> routes = solution.getRoutes();
		Matrix<Integer, Integer, Double> bandwidths = new Matrix<>();
		Matrix<Integer, Integer, Boolean> cmps = new Matrix<>();

		for (ServiceChain sc : topology.getServiceChains()) {

			int n = sc.getNumberOfComponents();
			if (n <= 1) {
				continue;
			}
			int previous = sc.getComponent(0).getIndex();

			for (int i = 1; i < n; i++) {

				int current = sc.getComponent(i).getIndex();

				if (cmps.get(previous, current) != null) {
					previous = current;
					continue;
				}
				cmps.put(previous, current, true);

				Route r = routes.get(previous, current);
				int[] nodes = r.getNodes();

				double demand = topology.getDemands().get(previous, current);

				for (int j = 1; j < nodes.length; j++) {
					int n1idx = nodes[j - 1];
					int n2idx = nodes[j];

					Double bw = bandwidths.get(n1idx, n2idx);
					if (bw == null) {
						bw = topology.getNetwork().getLink(n1idx, n2idx).getBandwidth();
					}
					bw -= demand;
					if (bw < 0) {
						return false;
					}
					bandwidths.put(n1idx, n2idx, bw);
				}

				previous = current;
			}
		}

		return true;
	}


}
