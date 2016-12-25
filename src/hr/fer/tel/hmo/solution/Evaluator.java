package hr.fer.tel.hmo.solution;

import hr.fer.tel.hmo.network.Link;
import hr.fer.tel.hmo.network.Network;
import hr.fer.tel.hmo.vnf.Component;
import hr.fer.tel.hmo.vnf.ServiceChain;

import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
		return false;
	}

	public boolean isValid(Solution solution) {
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

		double sol = 0.0;

		BitSet usedNodes = new BitSet(network.getNumberOfNodes());
		BitSet usedServers = new BitSet(network.getNumberOfServers());
		Set<Link> usedLinks = new HashSet<>();

		for (Component c : components) {
			int serverIndex = placement.getPlacementFor(c);
			usedServers.set(serverIndex);

			sol += network.getServer(serverIndex).getAdditionalPower(c);
		}

		for (Route r : solution.getRoutes().values()) {
			int[] nodes = r.getNodes();
			if (nodes[0] == nodes[nodes.length - 1]) {
				// no intermediate nodes
				// components are on servers that are connected to the same node
				continue;
			}

			// mark nodes as used
			// add used link powers
			usedNodes.set(nodes[0]);
			for (int i = 1; i < nodes.length; i++) {
				usedLinks.add(network.getLink(nodes[i - 1], nodes[i]));
				usedNodes.set(nodes[i]);
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

	private boolean isLatencyValidForServiceChain(Solution solution, ServiceChain sc) {
		//return solution.getRoutes()
		return false;
	}


}
