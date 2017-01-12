package hr.fer.tel.hmo.tabu.impl;

import hr.fer.tel.hmo.solution.Evaluator;
import hr.fer.tel.hmo.solution.Solution;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.solution.routing.Route;
import hr.fer.tel.hmo.solution.routing.Router;
import hr.fer.tel.hmo.util.Matrix;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

		Placement p = curr.getSolution().getPlacement().copy();

		Collection<Placement> nbrs = new HashSet<>();

		int C = p.getNumberOfComponents();

		List<Integer> idxs = IntStream.range(0, C).boxed().collect(Collectors.toList());
		Collections.shuffle(idxs);

		for (int i = 0; i < C / 5; i++) {
			nbrs.addAll(p.neighbors(idxs.get(i))
					.parallelStream().filter(evaluator::isValid)
					.collect(Collectors.toList()));
		}

		for (Placement p_ : nbrs) {
			Matrix<Integer, Integer, Route> rts = router.findRouting(p_);
			if (rts != null) {
				Solution s = new Solution(p_, rts);
				neighbors.add(toRS(s));
			}
		}

		// filter neighbors based on tabu list
		neighbors = neighbors.parallelStream()
				.filter(rs -> !dontUseFirst.contains(rs))
				.filter(rs -> !dontUseSecond.contains(rs))
				.collect(Collectors.toCollection(HashSet::new));

		// add one new random solution
		Placement rnd = curr.getSolution().getPlacement().copy();
		rnd.randomize();
		if (evaluator.isValid(rnd)) {
			Matrix<Integer, Integer, Route> rts = router.findRouting(rnd);
			if (rts != null) {
				neighbors.add(toRS(new Solution(rnd, rts)));
			}
		}

		dontUseSecond = dontUseFirst;
		dontUseFirst = neighbors;

		return neighbors;
	}


}
