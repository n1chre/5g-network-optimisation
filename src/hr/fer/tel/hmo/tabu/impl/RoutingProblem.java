package hr.fer.tel.hmo.tabu.impl;

import hr.fer.tel.hmo.solution.Evaluator;
import hr.fer.tel.hmo.solution.Solution;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.solution.routing.Route;
import hr.fer.tel.hmo.solution.routing.Router;
import hr.fer.tel.hmo.util.Matrix;
import hr.fer.tel.hmo.util.Util;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Simple routing problem, doesn't have any tabu elements
 */
public class RoutingProblem extends RoutingIterationLimitedProblem {

	private Set<RoutingSolution> dontUseFirst;
	private Set<RoutingSolution> dontUseSecond;
	private Set<RoutingSolution> dontUseThird;

	public RoutingProblem(Evaluator evaluator, Router router, Solution initial) {
		super(evaluator, router, initial);
		dontUseFirst = new HashSet<>();
		dontUseSecond = new HashSet<>();
		dontUseThird = new HashSet<>();
	}

	@Override
	public Collection<RoutingSolution> neighborhood(RoutingSolution curr) {

		Set<RoutingSolution> neighbors = new HashSet<>();
		Collection<Placement> nbrs = new HashSet<>();

		Placement p = curr.getSolution().getPlacement().copy();
		int C = p.getNumberOfComponents();

		List<Integer> idxs = IntStream.range(0, C).boxed().collect(Collectors.toList());
		Collections.shuffle(idxs, Util.RANDOM);

		for (int i = 0; i < C / 5; i++) {
			p.neighbors(idxs.get(i)).stream()
					.filter(evaluator::isValid)
					.forEach(nbrs::add);
		}

		for (Placement p_ : nbrs) {
			Matrix<Integer, Integer, Route> rts = router.findRouting(p_);
			if (rts != null) {
				RoutingSolution rs = toRS(new Solution(p_, rts));
				// filter neighbors based on tabu list
				if (dontUseFirst.contains(rs) || dontUseSecond.contains(rs) || dontUseThird.contains(rs)) {
					continue;
				}
				neighbors.add(rs);
			}
		}

		dontUseThird = dontUseSecond;
		dontUseSecond = dontUseFirst;
		dontUseFirst = neighbors;

		return neighbors;
	}

}
