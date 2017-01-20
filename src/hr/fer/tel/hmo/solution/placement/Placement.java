package hr.fer.tel.hmo.solution.placement;

import hr.fer.tel.hmo.util.Util;
import hr.fer.tel.hmo.vnf.Component;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents a placement of components on servers
 */
public class Placement {

	/**
	 * placement[c] = s
	 * For each component index we store server's index.
	 */
	private int[] placement;

	/**
	 * Number of possible servers
	 */
	private final int numberOfServers;

	/**
	 * Create a new empty placement.
	 *
	 * @param numberOfComponents number of components
	 * @param numberOfServers    number of servers
	 */
	public Placement(int numberOfComponents, int numberOfServers) {
		placement = new int[numberOfComponents];
		this.numberOfServers = numberOfServers;
	}

	/**
	 * @return an exact copy of this placement
	 */
	private Placement copy() {
		Placement cp = new Placement(placement.length, numberOfServers);
		cp.placement = Arrays.copyOf(this.placement, this.placement.length);
		return cp;
	}

	/**
	 * Randomize placement
	 */
	void randomize() {
		for (int c = 0; c < placement.length; c++) {
			place(c, Util.randomInt(numberOfServers));
		}
	}

	/**
	 * Create a placement that has n components on shuffled.
	 *
	 * @param n number of components to shuffle
	 * @return new placement
	 */
	public Placement neighbor(int n) {
		n = Math.min(n, placement.length);
		int[] indexes = Util.rndIndexes(placement.length, n);
		Placement p = copy();
		for (int i = n - 1; i > 0; --i) {
			Util.swap(p.placement, indexes[i], indexes[Util.randomInt(i)]);
		}
		return p;
	}

	/**
	 * Places a component with given index on a server with given index
	 *
	 * @param componentIndex component's index
	 * @param serverIndex    server's index
	 */
	void place(int componentIndex, int serverIndex) {
		placement[componentIndex] = serverIndex;
	}

	/**
	 * Get index of a server on which the component is placed
	 *
	 * @param component component
	 * @return server's index
	 */
	public int getPlacementFor(Component component) {
		return getPlacementFor(component.getIndex());
	}

	/**
	 * Get index of a server on which the component with given index is placed
	 *
	 * @param componentIndex component's index
	 * @return server's index
	 */
	public int getPlacementFor(int componentIndex) {
		return placement[componentIndex];
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Placement)) {
			return false;
		}

		Placement placement1 = (Placement) o;

		return numberOfServers == placement1.numberOfServers && Arrays.equals(placement, placement1.placement);
	}

	@Override
	public int hashCode() {
		int result = Arrays.hashCode(placement);
		result = 31 * result + numberOfServers;
		return result;
	}

	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n", "x=[\n", "\n];");

		for (final int sIdx : placement) {
			sj.add(
					IntStream.range(0, numberOfServers)
							.mapToObj(i -> i == sIdx ? "1" : "0")
							.collect(Collectors.joining(",", "[", "]"))
			);
		}

		return sj.toString();
	}
}
