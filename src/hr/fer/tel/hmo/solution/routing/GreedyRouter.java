package hr.fer.tel.hmo.solution.routing;

import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.proxies.LinkProxy;
import hr.fer.tel.hmo.solution.proxies.NodeProxy;

import java.util.LinkedHashSet;
import java.util.List;
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
		LinkedHashSet<NodeProxy> path = new LinkedHashSet<>();

		path.add(from);
		while (!from.equals(end)) {

			from.used = true;

			double _delay = delay;
			LinkProxy best = neighbors.getFor(from).values().stream()
					.filter(lp -> !path.contains(lp.to))
					.filter(lp -> lp.validParams(_delay, bandwidth))
					.min(new LinkProxy.LinkComp(end))
					.orElse(null);

			if (best == null) {
				return null;
			}

			best.used = true;
			best.bandwidth -= bandwidth;
			delay -= best.delay;

			from = best.to;
			path.add(from);
		}

		return path.stream().map(np -> np.node.getIndex()).collect(Collectors.toList());
	}

}
