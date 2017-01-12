package hr.fer.tel.hmo.solution.routing;

import hr.fer.tel.hmo.network.Link;
import hr.fer.tel.hmo.network.Node;
import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.util.Matrix;
import hr.fer.tel.hmo.vnf.ServiceChain;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Routes connections over network (greedy)
 */
public class GreedyRouter extends Router {

	private Topology topology;

	GreedyRouter(Topology topology) {
		this.topology = topology;
	}

	public Matrix<Integer, Integer, Route> findRouting(Placement placement) {

		int numNodes = topology.getNetwork().getNumberOfNodes();
		NodeProxy[] nodes = new NodeProxy[numNodes];
		for (int i = 0; i < numNodes; i++) {
			nodes[i] = new NodeProxy(topology.getNetwork().getNode(i));
		}

		// create neighbors
		Map<NodeProxy, List<LinkProxy>> neighbors = new HashMap<>();
		Matrix<Integer, Integer, Link> links = topology.getNetwork().getLinks();
		for (int n1 : links.keys()) {
			neighbors.put(nodes[n1],
					links.getFor(n1).entrySet()
							.parallelStream()
							.map(e -> new LinkProxy(nodes[e.getKey()], e.getValue()))
							.collect(Collectors.toCollection(LinkedList::new)));
		}

		// create cache
		// cache[component index] = node index (component -> server -> node)
		Function<Integer, Integer> nodeIdx =
				cidx -> topology.getNetwork().getServer(
						placement.getPlacementFor(cidx)
				).getNode().getIndex();

		int numComps = topology.getComponents().length;
		int[] CACHE = new int[numComps];
		for (int i = 0; i < numComps; i++) {
			CACHE[i] = nodeIdx.apply(topology.getComponents()[i].getIndex());
		}

		Matrix<Integer, Integer, Route> routes = new Matrix<>();

		for (ServiceChain sc : topology.getServiceChains()) {

			int ncs = sc.getNumberOfComponents();
			if (ncs <= 1) {
				continue;
			}

			double delay = sc.getLatency();

			int prevCompIdx = sc.getComponent(0).getIndex();
			for (int i = 1; i < ncs; i++) {

				int currCompIdx = sc.getComponent(i).getIndex();
				if (null != routes.get(prevCompIdx, currCompIdx)) {
					// if we already found a route between given components
					prevCompIdx = currCompIdx;
					continue;
				}

				int prevNodeIdx = CACHE[prevCompIdx];
				int currNodeIdx = CACHE[currCompIdx];
				Double bandwidth = topology.getDemands().get(prevCompIdx, currCompIdx);
				if (bandwidth == null) {
					bandwidth = 0.0;
				}

				List<Integer> r = path(
						nodes[prevNodeIdx], nodes[currNodeIdx],
						delay, bandwidth, neighbors,
						new HashSet<>(), new ArrayList<>()
				);
				if (r == null) {
					return null;
				}

				routes.put(prevCompIdx, currCompIdx, new Route(prevCompIdx, currCompIdx, r));
				prevCompIdx = currCompIdx;
			}
		}

		return routes;
	}


	/**
	 * Find a route that goes from one node to other with given demands
	 *
	 * @param from              start node
	 * @param end               end node
	 * @param delay             maximal delay
	 * @param demandedBandwidth demanded bandwidth
	 * @param neighbors         neighbors map
	 * @param forbidden         forbidden nodes
	 * @param path              current path
	 * @return list of nodes or null if route not found
	 */
	private List<Integer> path(NodeProxy from, NodeProxy end, double delay, double demandedBandwidth,
	                           Map<NodeProxy, List<LinkProxy>> neighbors,
	                           HashSet<Integer> forbidden, List<Integer> path) {
		path.add(from.node.getIndex());

		if (from.equals(end)) {
			return path;
		}

		forbidden.add(from.node.getIndex());

		LinkProxy best = null;
		for (LinkProxy lp : neighbors.get(from)) {
			if (forbidden.contains(lp.to.node.getIndex())) {
				continue;
			}

			if (delay < lp.delay) {
				continue;
			}

			if (lp.bandwidth < demandedBandwidth) {
				continue;
			}

			// if it's a link to ending node, use it
			if (end == lp.to) {
				best = lp;
				break;
			}

			best = lp.better(best);
		}

		if (best == null) {
			return null;
		}

		// use that link
		best.used = true;
		best.bandwidth -= demandedBandwidth;
		delay -= best.delay;

		return path(best.to, end, delay, demandedBandwidth, neighbors, forbidden, path);
	}

	private static class LinkProxy implements Comparable<LinkProxy> {

		NodeProxy to;
		double delay;
		double bandwidth;
		double power;
		boolean used;

		LinkProxy(NodeProxy to, Link link) {
			this.to = to;
			delay = link.getDelay();
			bandwidth = link.getBandwidth();
			power = link.getPowerConsumption();
			used = false;
		}

		private double powerUp() {
			double powerUp = 0.0;
			if (!used) {
				powerUp += power;
			}
			if (!to.used) {
				powerUp += to.node.getPowerConsumption();
			}
			return powerUp;
		}

		@Override
		public int compareTo(LinkProxy other) {
			int c = Double.compare(powerUp(), other.powerUp());
			if (c != 0) {
				return c;
			}
			c = Double.compare(delay, other.delay);
			if (c != 0) {
				return c;
			}
			return -Double.compare(bandwidth, other.bandwidth);
		}

		LinkProxy better(LinkProxy other) {
			if (other == null) {
				return this;
			}
			return compareTo(other) <= 0 ? this : other;
		}

		@Override
		public String toString() {
			return to + " " + bandwidth;
		}
	}

	private static class NodeProxy {
		Node node;
		boolean used;

		NodeProxy(Node node) {
			this.node = node;
			used = false;
		}


		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof NodeProxy)) {
				return false;
			}

			NodeProxy nodeProxy = (NodeProxy) o;

			return node.getIndex() == nodeProxy.node.getIndex();
		}

		@Override
		public int hashCode() {
			return node.getIndex();
		}
	}

}
