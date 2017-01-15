package hr.fer.tel.hmo.solution.routing;

import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.proxies.LinkProxy;
import hr.fer.tel.hmo.solution.proxies.NodeProxy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

		LinkProxy best = neighbors.getFor(from).values().stream()
				.filter(lp -> !forbidden.contains(lp.to.node.getIndex()))
				.filter(lp -> lp.validParams(delay, bandwidth))
				.min(new LinkProxy.LinkComp(end))
				.orElse(null);

		if (best == null) {
			return null;
		}

		// use that link
		best.used = true;
		best.bandwidth -= bandwidth;

		return path(best.to, end, delay - best.delay, bandwidth, forbidden, path);
	}

}
