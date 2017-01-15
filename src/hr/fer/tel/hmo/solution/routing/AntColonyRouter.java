package hr.fer.tel.hmo.solution.routing;

import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.proxies.LinkProxy;
import hr.fer.tel.hmo.solution.proxies.NodeProxy;
import hr.fer.tel.hmo.util.Matrix;
import hr.fer.tel.hmo.util.Util;

import java.util.*;

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
		for (NodeProxy np : neighbors.keys()) {
			Map<NodeProxy, LinkProxy> map = neighbors.getFor(np);
			double tau = 1. / map.size();
			map.keySet().forEach(np_ -> pheromones.put(np.node.getIndex(), np_.node.getIndex(), tau));
		}
	}

	@Override
	protected List<Integer> path(NodeProxy from, NodeProxy end, double delay, double bandwidth) {

		// Ant colony route finding

		PowerRoute best = new PowerRoute();

		Matrix<NodeProxy, NodeProxy, LinkProxy> valids =
				neighbors.filter(lp -> lp.validParams(delay, bandwidth));

		int iteration = 0;
		while (iteration++ < ITERATIONS) {

			PowerRoute currentBest = new PowerRoute();

			for (int __ = 0; __ < NUM_ANTS; __++) {
				PowerRoute ant = ant(from, end, valids);
				currentBest = currentBest.better(ant);
			}

			if (!currentBest.exists()) {
				continue;
			}

			// EVAPORATION
			pheromones.map(tau -> tau * (1 - RHO));

			// REINFORCEMENT
			double delta = 1. / currentBest.power; // TODO delta = f(power)

			int prev = currentBest.route.get(0);
			for (int i = 1, N = currentBest.route.size(); i < N; i++) {
				int curr = currentBest.route.get(i);
				pheromones.compute(prev, curr, tau -> tau + delta);
				prev = curr;
			}

			best = best.better(currentBest);
		}

		if (best.exists()) {
			// UPDATE links and nodes, use them
			int prev = best.route.get(0);
			nodes[prev].used = true;
			for (int i = 1, N = best.route.size(); i < N; i++) {
				int curr = best.route.get(i);
				// use link
				neighbors.get(nodes[prev], nodes[curr]).used = true;
				// use node
				nodes[curr].used = true;
				prev = curr;
			}
		}

		return best.route;
	}

	/**
	 * Use ant to find a feasible route
	 *
	 * @param from      from node
	 * @param to        to node
	 * @param neighbors neighbors map
	 * @return feasible route or null if none found
	 */
	private PowerRoute ant(NodeProxy from, NodeProxy to, Matrix<NodeProxy, NodeProxy, LinkProxy> neighbors) {
		List<Integer> route = new ArrayList<>();
		double power = 0.0;

		// initial structures
		HashSet<NodeProxy> newlyUsedNodes = new HashSet<>();
		HashSet<LinkProxy> newlyUsedLinks = new HashSet<>();
		newlyUsedNodes.add(from);

		do {
			route.add(from.node.getIndex());
			if (from.equals(to)) {
				break;
			}

			newlyUsedNodes.add(from);

			List<LinkProxy> links = new ArrayList<>(neighbors.getFor(from).values());
			links.removeIf(lp -> newlyUsedNodes.contains(lp.to));
			links.removeIf(newlyUsedLinks::contains);

			LinkProxy lp = chooseLink(from.node.getIndex(), links, newlyUsedLinks, newlyUsedNodes);
			if (lp == null) {
				return null;
			}

			from = lp.to;

			power += lp.powerUp(newlyUsedNodes, newlyUsedLinks);

			newlyUsedLinks.add(lp);
			newlyUsedNodes.add(from);

		} while (true);

		return new PowerRoute(power, route);
	}

	private LinkProxy chooseLink(int from, Collection<LinkProxy> links,
	                             HashSet<LinkProxy> usedLinks, HashSet<NodeProxy> usedNodes) {

		class tmp {
			private LinkProxy lp;
			private double prob;

			private tmp(LinkProxy lp) {
				this.lp = lp;
				double tau = pheromones.get(from, lp.to.node.getIndex());
				double ni = Math.max(lp.powerUp(usedNodes, usedLinks), Util.EPS);
				prob = Math.pow(tau, ALPHA) * Math.pow(ni, BETA);
			}
		}

		List<tmp> tmps = new LinkedList<>();

		double total = 0.0;
		for (LinkProxy lp : links) {
			tmp t = new tmp(lp);
			total += t.prob;
			tmps.add(t);
		}

		double _total = total;
		return Util.roulette(tmps, t -> t.prob / _total, t -> t.lp);
	}

	private static class PowerRoute {
		double power;
		List<Integer> route;

		PowerRoute() {
			this(Double.MAX_VALUE, null);
		}

		PowerRoute(double power, List<Integer> route) {
			this.power = power;
			this.route = route;
		}

		/**
		 * @return if it has a valid route
		 */
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
