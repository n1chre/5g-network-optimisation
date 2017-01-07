package hr.fer.tel.hmo.tabu.alg;

import java.util.Collection;

/**
 * Interface for tabu search
 */
public interface TabuProblem<S> {

	/**
	 * @return initial solution
	 */
	S initial();

	/**
	 * @param s1 first solution
	 * @param s2 second solution
	 *
	 * @return is s1 better than s2
	 */
	boolean isBetter(S s1, S s2);

	/**
	 * @param curr current solution
	 * @return neighbors of current solution
	 */
	Collection<S> neighborhood(S curr);

	/**
	 * @param best best solution
	 * @return true if search should stop
	 */
	boolean stop(S best);

	/**
	 * Update this object, called after every iteration
	 *
	 * @param curr current solution
	 * @param best best solution
	 */
	void update(S curr, S best);

}
