package hr.fer.tel.hmo.alg.tabu;

import java.util.Comparator;
import java.util.Optional;

/**
 * Basic tabu search implementation.
 */
public class TabuSearch<S> {

	/**
	 * Problem we want to solve using tabu search
	 */
	private final TabuProblem<S> problem;

	/**
	 * Create a new tabu search for given problem
	 *
	 * @param problem problem
	 */
	public TabuSearch(TabuProblem<S> problem) {
		this.problem = problem;
	}

	/**
	 * Perform a search and return best found solution
	 *
	 * @return best found solution
	 */
	public S search() {
		S curr = problem.initial();
		S best = curr;

		Comparator<S> comparator = (s1, s2) -> problem.isBetter(s1, s2) ? -1 : 0;

		do {
			Optional<S> mini = problem.neighborhood(curr).parallelStream().min(comparator);
			if (mini.isPresent()) {
				curr = mini.get();
				if (problem.isBetter(curr, best)) {
					best = curr;
				}
			}
			problem.update(curr, best);
		} while (!problem.stop(best));

		return best;
	}

}
