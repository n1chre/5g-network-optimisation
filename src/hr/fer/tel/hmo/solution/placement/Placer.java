package hr.fer.tel.hmo.solution.placement;

import hr.fer.tel.hmo.network.Topology;

import java.util.function.Function;

/**
 * Abstract class that creates placements
 */
public abstract class Placer {

	private static Placer PLACER = null;

	public static Placer get(Topology t, Function<Placement, Boolean> isValid) {
		if (PLACER == null) {
//			PLACER = new RandomPlacer(t, isValid);
			PLACER = new GreedyPlacer(t, isValid);
		}
		return PLACER;
	}

	/**
	 * Static network topology
	 */
	Topology topology;

	/**
	 * Function to test if a placement is valid
	 */
	Function<Placement, Boolean> isValid;

	Placer(Topology topology, Function<Placement, Boolean> isValid) {
		this.topology = topology;
		this.isValid = isValid;
	}

	/**
	 * @return next initial placement to use
	 */
	public abstract Placement next();

}
