package hr.fer.tel.hmo.solution.routing;

import hr.fer.tel.hmo.network.Link;
import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.solution.proxies.LinkProxy;
import hr.fer.tel.hmo.solution.proxies.NodeProxy;
import hr.fer.tel.hmo.util.Matrix;
import hr.fer.tel.hmo.util.Util;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Routes connections over network (greedy)
 */
public class GreedyRouter extends Router {

	// delay is always 20
	private static final double DEFAULT_DELAY = 20.0;

	private Topology topology;

	GreedyRouter(Topology topology) {
		this.topology = topology;
	}

	public Matrix<Integer, Integer, Route> findRouting(Placement placement) {

		final Matrix<Integer, Integer, Route> routes = new Matrix<>();

		int numNodes = topology.getNetwork().getNumberOfNodes();
		NodeProxy[] nodes = new NodeProxy[numNodes];
		for (int i = 0; i < numNodes; i++) {
			nodes[i] = new NodeProxy(topology.getNetwork().getNode(i));
		}

		// create neighbors
		// node -> list of links which go out of it
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
			CACHE[i] = nodeIdx.apply(i);
		}

		class tmp {
			private int cmp1, cmp2;
			private double bandwidth;
		}

		List<tmp> tmps = new ArrayList<>();
		Matrix<Integer, Integer, Double> demands = topology.getDemands();
		for (int cmp1 : demands.keys()) {
			for (Map.Entry<Integer, Double> e : demands.getFor(cmp1).entrySet()) {
				tmp t = new tmp();
				t.cmp1 = cmp1;
				t.cmp2 = e.getKey();
				t.bandwidth = e.getValue();
				tmps.add(t);
			}
		}
		Collections.shuffle(tmps, Util.RANDOM); //

		for (tmp t : tmps) {
			int node1 = CACHE[t.cmp1];
			int node2 = CACHE[t.cmp2];

			List<Integer> r = path(
					nodes[node1], nodes[node2],
					DEFAULT_DELAY, t.bandwidth, neighbors,
					new HashSet<>(), new ArrayList<>()
			);
			if (r == null) {
				return null;
			}

			routes.put(t.cmp1, t.cmp2, new Route(t.cmp1, t.cmp2, r));
		}
		return routes;
	}


	/**
	 * Find a route that goes from one node to other with given demands
	 *
	 * @param from      start node
	 * @param end       end node
	 * @param delay     maximal delay
	 * @param bandwidth demanded bandwidth
	 * @param neighbors neighbors map
	 * @param forbidden forbidden nodes
	 * @param path      current path
	 * @return list of nodes or null if route not found
	 */
	private List<Integer> path(NodeProxy from, NodeProxy end, double delay, double bandwidth,
	                           Map<NodeProxy, List<LinkProxy>> neighbors,
	                           HashSet<Integer> forbidden, List<Integer> path) {
		path.add(from.node.getIndex());

		if (from.equals(end)) {
			return path;
		}

		forbidden.add(from.node.getIndex());

		double delay_ = delay;

		List<LinkProxy> ls = new ArrayList<>(neighbors.get(from));
		ls.removeIf(l -> forbidden.contains(l.to.node.getIndex()));
		ls.removeIf(l -> !l.validParams(delay_, bandwidth));

//		LinkProxy best = oneLevelBest(ls, end);
		LinkProxy best = twoLevelBest(ls, from, end, neighbors, delay, bandwidth);

		if (best == null) {
			return null;
		}

		// use that link
		best.used = true;
		best.bandwidth -= bandwidth;
		delay -= best.delay;

		return path(best.to, end, delay, bandwidth, neighbors, forbidden, path);
	}

	/**
	 * Find best link in a collection.
	 * Depth = 1
	 *
	 * @param links all links
	 * @param goal  goal node
	 * @return best link
	 */
	private static LinkProxy oneLevelBest(Collection<LinkProxy> links, NodeProxy goal) {
		return links.stream().min(new LinkProxy.LinkComp(goal)).orElse(null);
	}

	/**
	 * Find a link which is better
	 * Depth = 2
	 *
	 * @param x         first link
	 * @param y         second link
	 * @param from      from which node do links go
	 * @param goal      to which node do we want to go
	 * @param neighbors maps each node to links that go out of it
	 * @param delay     delay
	 * @param bandwidth bandwidth
	 * @return better link
	 */
	private static LinkProxy twoLevelBetter(LinkProxy x, LinkProxy y, NodeProxy from, NodeProxy goal,
	                                        Map<NodeProxy, List<LinkProxy>> neighbors,
	                                        double delay, double bandwidth) {
		if (x == null) {
			return y;
		}
		if (y == null) {
			return x;
		}

		boolean goalX = goal.equals(x.to);
		boolean goalY = goal.equals(y.to);

		if (goalX && goalY) {
			return x.compareTo(y) <= 0 ? x : y;
		}

		if (goalX != goalY) {
			return goalX ? x : y;
		}

		// neither goes to goal

		Function<NodeProxy, Collection<LinkProxy>> nbrs =
				nodeProxy -> neighbors.get(nodeProxy).stream()
						.filter(l -> !from.equals(l.to)) // don't go back to the same node
						.filter(l -> l.validParams(delay, bandwidth))
						.collect(Collectors.toList());

		LinkProxy bx = oneLevelBest(nbrs.apply(x.to), goal);
		LinkProxy by = oneLevelBest(nbrs.apply(y.to), goal);

		if (bx == null) {
			return x;
		}
		if (by == null) {
			return x;
		}

		return Double.compare(
				x.powerUp() + bx.powerUp(), y.powerUp() + by.powerUp()
		) <= 0 ? x : y;
	}

	/**
	 * Find best link from given collection
	 *
	 * @param links     collection of links
	 * @param from      from node
	 * @param goal      goal node
	 * @param neighbors map from node to outgoing links
	 * @param delay     delay
	 * @param bandwidth bandwidth
	 * @return best link
	 */
	private static LinkProxy twoLevelBest(Collection<LinkProxy> links, NodeProxy from, NodeProxy goal,
	                                      Map<NodeProxy, List<LinkProxy>> neighbors,
	                                      double delay, double bandwidth) {
		LinkProxy best = null;
		for (LinkProxy lp : links) {
			best = twoLevelBetter(lp, best, from, goal, neighbors, delay, bandwidth);
		}
		return best;
	}

}
