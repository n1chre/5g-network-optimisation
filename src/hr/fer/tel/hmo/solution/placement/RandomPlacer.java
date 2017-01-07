package hr.fer.tel.hmo.solution.placement;

import hr.fer.tel.hmo.network.Topology;

import java.util.function.Function;

/**
 * Tries to create a randomize placement
 */
public class RandomPlacer extends Placer {

	RandomPlacer(Topology topology, Function<Placement, Boolean> isValid) {
		super(topology, isValid);
	}

	@Override
	public Placement next() {
		Placement p = new Placement(
				topology.getComponents().length,
				topology.getNetwork().getNumberOfServers()
		);

		do {
			p.randomize();
		} while (isValid.apply(p));

		return p;
	}
}
