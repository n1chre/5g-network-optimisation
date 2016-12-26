package hr.fer.tel.hmo.solution.placement;

import hr.fer.tel.hmo.network.Topology;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * Tries to create a random placement
 */
public class RandomPlacer extends Placer {

	RandomPlacer(Topology topology, Function<Placement, Boolean> isValid) {
		super(topology, isValid);
	}

	@Override
	public Collection<Placement> getInitialPlacements(int maxPlacements) {
		List<Placement> placements = new LinkedList<>();
		Placement p = new Placement(topology.getComponents().length, topology.getNetwork().getNumberOfServers());

		while (maxPlacements > 0) {
			p.random();
			if (isValid.apply(p)) {
				placements.add(p.copy());
				maxPlacements--;
			}
		}

		return placements;
	}
}
