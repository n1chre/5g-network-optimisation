package hr.fer.tel.hmo.tabu.impl;

import hr.fer.tel.hmo.tabu.alg.TabuProblem;

import java.util.Collection;

/**
 * Created by fhrenic on 07/01/2017.
 */
public class RoutingProblem implements TabuProblem<RoutingSolution> {

	@Override
	public RoutingSolution initial() {
		return null;
	}

	@Override
	public boolean isBetter(RoutingSolution s1, RoutingSolution s2) {
		return false;
	}

	@Override
	public Collection<RoutingSolution> neighborhood(RoutingSolution curr) {
		return null;
	}

	@Override
	public boolean stop(RoutingSolution best) {
		return false;
	}

	@Override
	public void update(RoutingSolution curr, RoutingSolution best) {
	}

}
