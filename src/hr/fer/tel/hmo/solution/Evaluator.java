package hr.fer.tel.hmo.solution;

import hr.fer.tel.hmo.network.Link;
import hr.fer.tel.hmo.network.Network;
import hr.fer.tel.hmo.vnf.Component;

import java.util.BitSet;
import java.util.HashSet;
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

	public Evaluator(Network network, Component[] components) {
		this.network = network;
		this.components = components;
	}

	/**
	 * Evaluates given placement and routing.
	 * This is done by calculating total power consupmtion:
	 * 1. power used by servers
	 * 2. power used by links
	 * 3. power used by nodes
	 *
	 * @param placement placement of components
	 * @param routing   all routes through a network
	 * @return total power consumption
	 */
	public double evaluate(Placement placement, Routing routing) {
		double sol = 0.0;

		BitSet usedNodes = new BitSet(network.getNumberOfNodes());
		BitSet usedServers = new BitSet(network.getNumberOfServers());
		Set<Link> usedLinks = new HashSet<>();

		for (Component c : components) {
			int serverIndex = placement.getPlacementFor(c);
			usedServers.set(serverIndex);

			sol += network.getServer(serverIndex).getAdditionalPower(c);
		}

		for (Route r : routing) {
			int[] intermediate = r.getIntermediate();
			if (intermediate.length == 0) {
				// no intermediate nodes
				// components are on servers that are connected to the same node
				continue;
			}

			int nIdxFrom = extractNodeIndex(placement, r, true);
			int nIdxTo = extractNodeIndex(placement, r, false);

			// mark nodes as used
			// add used link powers
			usedNodes.set(nIdxFrom);
			int lastIdx = nIdxFrom;
			for (int idx : intermediate) {
				usedNodes.set(idx);
				sol += network.getLink(lastIdx, idx).getPowerConsumption();
				lastIdx = idx;
			}
			usedNodes.set(nIdxTo);
			sol += network.getLink(lastIdx, nIdxTo).getPowerConsumption();
		}

		// minimal power used by server (only count those which are on)
		sol += usedNodes.stream().mapToDouble(i -> network.getServer(i).getPmin()).sum();

		// power used by nodes (only count those which are used)
		sol += usedNodes.stream().mapToDouble(i -> network.getNode(i).getPowerConsumption()).sum();

		return sol;
	}

	/**
	 * This is a helper function used for extracting node index.
	 * Return index of node that has a server connected which has a component on it.
	 * Component index is f(r);
	 *
	 * @param p    placement of components
	 * @param r    route we want to analyze
	 * @param from true if we want to get component that is marked as <code>from</code> in given route
	 * @return index of a node
	 */
	private int extractNodeIndex(Placement p, Route r, boolean from) {
		int idx = from ? r.getFrom() : r.getTo();
		return network.getServer(p.getPlacementFor(components[idx])).getNode().getIndex();
	}

}
