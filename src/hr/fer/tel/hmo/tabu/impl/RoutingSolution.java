package hr.fer.tel.hmo.tabu.impl;

import hr.fer.tel.hmo.solution.Solution;

/**
 * This class should only contain valid solutions.
 * It has cached fitness so it's not calculated twice (or more)
 */
public class RoutingSolution {

	private final double fitness;

	private final Solution solution;

	RoutingSolution(Solution solution, double fitness) {
		this.fitness = fitness;
		this.solution = solution;
	}

	public boolean isBetterThan(RoutingSolution other) {
		return fitness > other.fitness;
	}

	public double getFitness() {
		return fitness;
	}

	public Solution getSolution() {
		return solution;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof RoutingSolution)) {
			return false;
		}

		RoutingSolution that = (RoutingSolution) o;

		if (Double.compare(that.fitness, fitness) != 0) {
			return false;
		}
		return solution.equals(that.solution);
	}

	@Override
	public int hashCode() {
		int result;
		long temp;
		temp = Double.doubleToLongBits(fitness);
		result = (int) (temp ^ (temp >>> 32));
		result = 31 * result + solution.hashCode();
		return result;
	}

	@Override
	public String toString() {
		return String.format("%.2f", -fitness);
	}
}
