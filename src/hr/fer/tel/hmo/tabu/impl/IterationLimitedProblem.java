package hr.fer.tel.hmo.tabu.impl;

import hr.fer.tel.hmo.tabu.alg.TabuProblem;

/**
 * Iteration limited tabu problem.
 * Tabu search runs for a given number of iterations
 */
abstract class IterationLimitedProblem<S> implements TabuProblem<S> {

	private int iteration;
	private final int maxIterations;

	IterationLimitedProblem(int maxIterations) {
		this.maxIterations = maxIterations;
		iteration = 0;
	}

	@Override
	public boolean stop(S best) {
		return iteration >= maxIterations;
	}

	@Override
	public void update(S curr, S best) {
		++iteration;
	}
}
