package hr.fer.tel.hmo.solution.routing;

import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.proxies.LinkProxy;
import hr.fer.tel.hmo.solution.proxies.NodeProxy;
import hr.fer.tel.hmo.util.Matrix;

import java.util.List;
import java.util.Map;

/**
 * Find route using ant colony optimization.
 */
public class AntColonyRouter extends SequentialRouter {

	private static final int NUM_ANTS = 4;
	private static final int ITERATIONS = 100;

	private static final double ALPHA = 1.0;
	private static final double BETA = 1.0;
	private static final double RHO = 0.1;

	private Matrix<Integer, Integer, Double> pheromones;

	AntColonyRouter(Topology topology) {
		super(topology);
	}

	@Override
	protected void initialize() {
		super.initialize();

		pheromones = new Matrix<>();
		for (Map.Entry<NodeProxy, List<LinkProxy>> e : neighbors.entrySet()) {
			NodeProxy np = e.getKey();
			List<LinkProxy> lps = e.getValue();

			double tau = 1. / lps.size();
			lps.forEach(lp -> pheromones.put(np.node.getIndex(), lp.to.node.getIndex(), tau));
		}

	}

	@Override
	protected List<Integer> path(NodeProxy from, NodeProxy end, double delay, double bandwidth) {

		PowerRoute best = new PowerRoute();

		int iteration = 0;
		while (iteration++ < ITERATIONS) {

			PowerRoute currentBest = new PowerRoute();

			for (int ant = 0; ant < NUM_ANTS; ant++) {
				// TODO run ant
				// TODO update solution if needed
			}

			if (!currentBest.exists()) {
				return null;
			}

			// TODO delta = f(power)
			double delta = 1. / currentBest.power;

			// EVAPORATION
			pheromones.map(tau -> tau * (1 - RHO));

			// REINFORCEMENT
			int prev = currentBest.route.get(0);
			for (int i = 1, N = currentBest.route.size(); i < N; i++) {
				int curr = currentBest.route.get(i);
				pheromones.compute(prev, curr, tau -> tau + delta);
				prev = curr;
			}

			best = best.better(currentBest);
		}

		return best.route;
	}

	private static class PowerRoute {
		double power;
		List<Integer> route;

		PowerRoute() {
			power = Double.MAX_VALUE;
			route = null;
		}

		boolean exists() {
			return route != null;
		}

		/**
		 * @param other other route
		 * @return this or other, which ever is better
		 */
		PowerRoute better(PowerRoute other) {
			return other == null || power <= other.power ? this : other;
		}
	}

}
