package hr.fer.tel.hmo.solution.routing;

import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.proxies.LinkProxy;
import hr.fer.tel.hmo.solution.proxies.NodeProxy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Routes connections over network (greedy)
 */
public class GreedyRouter extends SequentialRouter {

	GreedyRouter(Topology topology) {
		super(topology);
	}

	@Override
	protected List<Integer> path(NodeProxy from, NodeProxy end, double delay, double bandwidth) {
		return path(from, end, delay, bandwidth, new HashSet<>(), new ArrayList<>());
	}

	/**
	 * Find a route that goes from one node to other with given demands
	 *
	 * @param from      start node
	 * @param end       end node
	 * @param delay     maximal delay
	 * @param bandwidth demanded bandwidth
	 * @param forbidden forbidden nodes
	 * @param path      current path
	 * @return list of nodes or null if route not found
	 */
	private List<Integer> path(NodeProxy from, NodeProxy end, double delay, double bandwidth,
	                           HashSet<Integer> forbidden, List<Integer> path) {
		path.add(from.node.getIndex());

		if (from.equals(end)) {
			return path;
		}

		from.used = true;
		forbidden.add(from.node.getIndex());

		List<LinkProxy> ls = new ArrayList<>(neighbors.get(from));
		ls.removeIf(l -> forbidden.contains(l.to.node.getIndex()));
		ls.removeIf(l -> !l.validParams(delay, bandwidth));

		LinkProxy best = oneLevelBest(ls, end);
//		LinkProxy best = twoLevelBest(ls, from, end, neighbors, delay, bandwidth);

		if (best == null) {
			return null;
		}

		// use that link
		best.used = true;
		best.bandwidth -= bandwidth;

		return path(best.to, end, delay - best.delay, bandwidth, forbidden, path);
	}

	/**
	 * Find best link in a collection.
	 * Depth = 1
	 *
	 * @param links all links
	 * @param goal  goal node
	 * @return best link
	 */
	private static LinkProxy oneLevelBest(List<LinkProxy> links, NodeProxy goal) {
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

		Function<NodeProxy, List<LinkProxy>> nbrs =
				nodeProxy -> neighbors.get(nodeProxy).stream()
						.filter(l -> !from.equals(l.to)) // don't go back to the same node
						.filter(l -> l.validParams(delay, bandwidth))
						.collect(Collectors.toList());

		LinkProxy bx = oneLevelBest(nbrs.apply(x.to), goal);
		LinkProxy by = oneLevelBest(nbrs.apply(y.to), goal);

		if (bx == null) {
			return y;
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
	private static LinkProxy twoLevelBest(List<LinkProxy> links, NodeProxy from, NodeProxy goal,
	                                      Map<NodeProxy, List<LinkProxy>> neighbors,
	                                      double delay, double bandwidth) {
		LinkProxy best = null;
		for (LinkProxy lp : links) {
			best = twoLevelBetter(lp, best, from, goal, neighbors, delay, bandwidth);
		}
		return best;
	}

}
