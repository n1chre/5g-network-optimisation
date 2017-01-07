package hr.fer.tel.hmo.tabu.alg;

import java.util.Comparator;
import java.util.Optional;

/**
 * Basic tabu search implementation.
 */
public class TabuSearch {

	/**
	 * Perform a search and return best found solution
	 *
	 * @param problem search will be performed on this problem
	 * @return best found solution
	 */
	public static <S> S search(TabuProblem<S> problem) {
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
