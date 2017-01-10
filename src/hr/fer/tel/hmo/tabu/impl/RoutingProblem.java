package hr.fer.tel.hmo.tabu.impl;

import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.Evaluator;
import hr.fer.tel.hmo.solution.Solution;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.solution.routing.Route;
import hr.fer.tel.hmo.solution.routing.Router;
import hr.fer.tel.hmo.tabu.alg.TabuProblem;
import hr.fer.tel.hmo.util.Matrix;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Simple routing problem, doesn't have any tabu elements
 */
public class RoutingProblem implements TabuProblem<RoutingSolution> {

	/**
	 * Maximum number of iterations to run
	 */
	private static final int MAX_ITERATIONS = 123;

	/**
	 * Neighborhood size
	 */
	private static final int NEIGHBORHOOD_SIZE = 10;

	private Evaluator evaluator;
	private Topology topology;
	private RoutingSolution initial;
	private int iteration;
	private int maxIterations;
	private int neighborhoodSize;

	public RoutingProblem(Evaluator evaluator, Topology topology, Solution initial) {
		this(evaluator, topology, initial, MAX_ITERATIONS, NEIGHBORHOOD_SIZE);
	}

	public RoutingProblem(Evaluator evaluator, Topology topology, Solution initial, int maxIterations, int neighborhoodSize) {
		this.evaluator = evaluator;
		this.topology = topology;
		this.initial = toRS(initial);
		this.iteration = 0;
		this.maxIterations = maxIterations;
		this.neighborhoodSize = neighborhoodSize;
	}

	@Override
	public RoutingSolution initial() {
		return initial;
	}

	@Override
	public boolean isBetter(RoutingSolution s1, RoutingSolution s2) {
		return s1.isBetterThan(s2);
	}

	@Override
	public Collection<RoutingSolution> neighborhood(RoutingSolution curr) {
		Collection<RoutingSolution> neighbors = new LinkedList<>();

		Placement p = curr.getSolution().getPlacement().copy();

		Collection<Placement> nbrs = new HashSet<>();
		for (int i = 0; i < p.getNumberOfComponents(); i++) {
			nbrs.addAll(p.neighbors(i)
					.parallelStream().filter(evaluator::isValid)
					.collect(Collectors.toList()));
		}

		for (Placement p_ : nbrs) {
			Matrix<Integer, Integer, Route> rts = new Router(topology).findRouting(p_);
			if (rts != null) {
				Solution s = new Solution(p_, rts);
				neighbors.add(toRS(s));
			}
		}

		return neighbors;
	}

	@Override
	public boolean stop(RoutingSolution best) {
		return iteration >= maxIterations;
	}

	@Override
	public void update(RoutingSolution curr, RoutingSolution best) {
		++iteration;
	}

	/**
	 * @param solution solution
	 * @return wrapped solution
	 */
	private RoutingSolution toRS(Solution solution) {
		return new RoutingSolution(solution, evaluator.fitness(solution));
	}

}
