package hr.fer.tel.hmo.solution;

import hr.fer.tel.hmo.network.Link;
import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.solution.routing.Route;
import hr.fer.tel.hmo.util.Matrix;
import hr.fer.tel.hmo.vnf.Component;
import hr.fer.tel.hmo.vnf.ServiceChain;

import java.util.*;

/**
 * This class knows how to evaluate given placement and routing
 */
public class Evaluator {

	/**
	 * Network topology
	 */
	private Topology topology;

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
	 * Evaluates given placement and routing.
	 * This is done by calculating total power consupmtion:
	 * 1. power used by servers
	 * 2. power used by links
	 * 3. power used by nodes
	 *
	 * @param solution possible solution
	 * @return total power consumption
	 */
	public double evaluate(Solution solution) {
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
				if (left < 0) {
					return false;
				}
				serverAvailable.set(r, left);
			}
		}
		return true;
	}

	/**
	 * Test if solution is valid
	 *
	 * @param s possible solution
	 * @return true if solution is valid
	 */
	public boolean isValid(Solution s) {
		return isLatencyValid(s) && isBandwidthValid(s);
	}

	/**
	 * Check if all service chains have allowed latency
	 *
	 * @param s possible solution
	 * @return true if all service chains have valid latency
	 */
	private boolean isLatencyValid(Solution s) {
		return topology.getServiceChains().parallelStream().allMatch(sc -> this.isLatencyValidForServiceChain(s, sc));
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

		double lat = 0.0;

		int n = sc.getNumberOfComponents();
		for (int i = 1; i < n; i++) {
			Route r = routes.get(sc.getComponent(i - 1).getIndex(), sc.getComponent(i).getIndex());
			if (r == null) {
				System.out.println(sc.getComponent(i - 1).getIndex());
				System.out.println(sc.getComponent(i).getIndex());
				throw new RuntimeException();
			}

			int[] nodes = r.getNodes();
			for (int j = 1; j < nodes.length; j++) {
				lat += topology.getNetwork().getLink(nodes[j - 1], nodes[j]).getDelay();
			}

			if (lat > sc.getLatency()) {
				return false;
			}
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
		Map<Link, Double> bandwidths = new HashMap<>();

		for (ServiceChain sc : topology.getServiceChains()) {

			int n = sc.getNumberOfComponents();
			if (n <= 1) {
				continue;
			}
			int previous = sc.getComponent(0).getIndex();

			for (int i = 1; i < n; i++) {

				int current = sc.getComponent(i).getIndex();
				Route r = routes.get(previous, current);
				int[] nodes = r.getNodes();

				double demand = topology.getDemands().get(previous, current);

				for (int j = 1; j < nodes.length; j++) {
					Link link = topology.getNetwork().getLink(nodes[j - 1], nodes[j]);

					bandwidths.putIfAbsent(link, link.getBandwidth());
					Double bw = bandwidths.get(link);
					bw -= demand;
					if (bw < 0) {
						return false;
					}
					bandwidths.put(link, bw);
				}

				previous = current;
			}
		}

		return true;
	}


}
