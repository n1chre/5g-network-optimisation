package hr.fer.tel.hmo.tabu.alg;

import java.util.Collection;

/**
 * Basic tabu search implementation.
 */
public class Tabu {

	/**
	 * Perform a search and return best found solution
	 *
	 * @param problem search will be performed on this problem
	 * @return best found solution
	 */
	public static <S> S search(TabuProblem<S> problem) {
		S curr = problem.initial();
		S best = curr;

		do {
			Collection<S> ns = problem.neighborhood(curr);
			curr = null;
			for (S s : ns) {
				if (curr == null || problem.isBetter(s, curr)) {
					curr = s;
				}
			}
			if (curr != null && problem.isBetter(curr, best)) {
				best = curr;
			}
			problem.update(curr, best);
		} while (!problem.stop(best));

		return best;
	}

}
