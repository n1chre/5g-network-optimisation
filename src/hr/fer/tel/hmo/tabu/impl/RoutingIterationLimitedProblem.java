package hr.fer.tel.hmo.tabu.impl;

import hr.fer.tel.hmo.solution.Evaluator;
import hr.fer.tel.hmo.solution.Solution;
import hr.fer.tel.hmo.solution.routing.Router;

/**
 * Tabu problem that is iteration limited and knows how to compare solutions.
 */
public abstract class RoutingIterationLimitedProblem extends IterationLimitedProblem<RoutingSolution> {

	/**
	 * Maximum number of iterations to run
	 */
	private static final int MAX_ITERATIONS = 1000;

	final Evaluator evaluator;
	final Router router;
	private final RoutingSolution initial;

	RoutingIterationLimitedProblem(Evaluator evaluator, Router router, Solution initial) {
		super(MAX_ITERATIONS);
		this.evaluator = evaluator;
		this.router = router;
		this.initial = toRS(initial);
	}

	@Override
	public RoutingSolution initial() {
		return initial;
	}

	@Override
	public boolean isBetter(RoutingSolution s1, RoutingSolution s2) {
		return s1 != null && s1.isBetterThan(s2);
	}

	/**
	 * @param solution solution
	 * @return wrapped solution
	 */
	RoutingSolution toRS(Solution solution) {
		return new RoutingSolution(solution, evaluator.fitness(solution));
	}
}
