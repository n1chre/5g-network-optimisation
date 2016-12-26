package hr.fer.tel.hmo.solution;

import hr.fer.tel.hmo.network.Link;
import hr.fer.tel.hmo.network.Network;
import hr.fer.tel.hmo.util.Matrix;
import hr.fer.tel.hmo.vnf.Component;
import hr.fer.tel.hmo.vnf.ServiceChain;

import java.util.*;

/**
 * This class knows how to evaluate given placement and routing
 */
public class Evaluator {

	/**
	 * Network configuration used to evaluate placement and routing
	 */
	private Network network;

	/**
	 * Components that are placed onto servers
	 */
	private Component[] components;

	/**
	 * List of service chains
	 */
	private List<ServiceChain> serviceChains;

	public Evaluator(Network network, Component[] components, List<ServiceChain> serviceChains) {
		this.network = network;
		this.components = components;
		this.serviceChains = serviceChains;
	}

	public boolean isValid(Placement placement) {
		// TODO implement this
		return false;
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

		BitSet usedNodes = new BitSet(network.getNumberOfNodes());
		BitSet usedServers = new BitSet(network.getNumberOfServers());
		Set<Link> usedLinks = new HashSet<>();

		for (Component c : components) {
			int serverIndex = placement.getPlacementFor(c);
			usedServers.set(serverIndex);

			sol += network.getServer(serverIndex).getAdditionalPower(c);
		}

		for (Integer from : routes.keys()) {
			for (Route r : routes.valuesFor(from)) {
				int[] nodes = r.getNodes();
				if (nodes.length == 1) {
					continue; // both components on same node
				}

				// mark nodes as used
				// add used link powers
				usedNodes.set(nodes[0]);
				for (int i = 1; i < nodes.length; i++) {
					usedLinks.add(network.getLink(nodes[i - 1], nodes[i]));
					usedNodes.set(nodes[i]);
				}
			}
		}

		// power used by links
		sol += usedLinks.parallelStream().mapToDouble(Link::getPowerConsumption).sum();

		// minimal power used by server (only count those which are on)
		sol += usedNodes.stream().parallel().mapToDouble(i -> network.getServer(i).getPmin()).sum();

		// power used by nodes (only count those which are used)
		sol += usedNodes.stream().parallel().mapToDouble(i -> network.getNode(i).getPowerConsumption()).sum();

		return sol;
	}

	public boolean isValid(Solution s) {
		// TODO make parallel
		return isLatencyValid(s) && isBandwidthValid(s);
	}

	/**
	 * Check if all service chains have allowed latency
	 *
	 * @param s possible solution
	 * @return true if all service chains have valid latency
	 */
	private boolean isLatencyValid(Solution s) {
		return serviceChains.parallelStream().allMatch(sc -> this.isLatencyValidForServiceChain(s, sc));
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

			int[] nodes = r.getNodes();
			for (int j = 1; j < nodes.length; j++) {
				lat += network.getLink(nodes[i - 1], nodes[i]).getDelay();
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

		for (ServiceChain sc : serviceChains) {

			int n = sc.getNumberOfComponents();
			if (n == 0) {
				continue;
			}
			Component previous = sc.getComponent(0);

			for (int i = 1; i < n; i++) {

				Component current = sc.getComponent(i);
				Route r = routes.get(previous.getIndex(), current.getIndex());
				int[] nodes = r.getNodes();

				for (int j = 1; j < nodes.length; j++) {
					Link link = network.getLink(nodes[i - 1], nodes[i]);

					bandwidths.putIfAbsent(link, link.getBandwidth());
					Double bw = bandwidths.get(link);
					bw -= previous.getDemandedBandwidthFor(current);
					if (bw < 0) {
						return false;
					}
					bandwidths.put(link, bw);
				}
			}
		}

		return true;
	}


}
