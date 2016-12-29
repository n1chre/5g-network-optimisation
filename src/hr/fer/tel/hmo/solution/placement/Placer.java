package hr.fer.tel.hmo.solution.placement;

import hr.fer.tel.hmo.network.Topology;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Abstract class that creates placements
 */
public abstract class Placer {

	private static Placer PLACER = null;

	public static Placer get(Topology t, Function<Placement, Boolean> isValid) {
		if (PLACER == null) {
			PLACER = new RandomPlacer(t, isValid);
		}
		return PLACER;
	}

	/**
	 * Static network topology
	 */
	protected Topology topology;

	/**
	 * Function to test if a placement is valid
	 */
	protected Function<Placement, Boolean> isValid;

	protected Placer(Topology topology, Function<Placement, Boolean> isValid) {
		this.topology = topology;
		this.isValid = isValid;
	}

	/**
	 * Create initial placements, at most maxPlacements of them
	 *
	 * @param maxPlacements upper bound
	 * @return initial placements
	 */
	public abstract List<Placement> getInitialPlacements(int maxPlacements);
}
