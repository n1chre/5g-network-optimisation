package hr.fer.tel.hmo.solution.routing;

import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.util.Matrix;

/**
 * Routes connections over network (greedy)
 */
public abstract class Router {

	private static Router ROUTER = null;

	public static Router get(Topology t) {
		if (ROUTER == null) {
//			ROUTER = new GreedyRouter(t);
			ROUTER = new AntColonyRouter(t);
		}
		return ROUTER;
	}

	public abstract Matrix<Integer, Integer, Route> findRouting(Placement placement);
}
