package hr.fer.tel.hmo.tabu.impl;

import hr.fer.tel.hmo.solution.Solution;

/**
 * This class should only contain valid solutions.
 * It has cached fitness so it's not calculated twice (or more)
 */
public class RoutingSolution {

	private double fitness;

	private Solution solution;

	RoutingSolution(Solution solution, double fitness) {
		this.fitness = fitness;
		this.solution = solution;
	}

	public boolean isBetterThan(RoutingSolution other) {
		return fitness < other.fitness;
	}

	public double getFitness() {
		return fitness;
	}

	public Solution getSolution() {
		return solution;
	}

}
