package hr.fer.tel.hmo.tabu.impl;

import hr.fer.tel.hmo.solution.Evaluator;
import hr.fer.tel.hmo.solution.Solution;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.solution.routing.Route;
import hr.fer.tel.hmo.solution.routing.Router;
import hr.fer.tel.hmo.util.Matrix;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Simple routing problem, doesn't have any tabu elements
 */
public class RoutingProblem extends RoutingIterationLimitedProblem {

	private Set<RoutingSolution> dontUseFirst;
	private Set<RoutingSolution> dontUseSecond;

	public RoutingProblem(Evaluator evaluator, Router router, Solution initial) {
		super(evaluator, router, initial);
		dontUseFirst = new HashSet<>();
		dontUseSecond = new HashSet<>();
	}

	@Override
	public Collection<RoutingSolution> neighborhood(RoutingSolution curr) {

		Set<RoutingSolution> neighbors = new HashSet<>();

		final int N = 16;
		for (int i = 0; i < N; i++) {

			Placement p = curr.getSolution().getPlacement().neighbor(6);
			if (!evaluator.isValid(p)) {
				continue;
			}

			Matrix<Integer, Integer, Route> rts = router.findRouting(p);
			if (rts == null) {
				continue;
			}

			RoutingSolution rs = toRS(new Solution(p, rts));
			// filter neighbors based on tabu list
			if (dontUseFirst.contains(rs) || dontUseSecond.contains(rs)) {
				continue;
			}
			neighbors.add(rs);
		}


		dontUseSecond = dontUseFirst;
		dontUseFirst = neighbors;

		return neighbors;
	}

}
