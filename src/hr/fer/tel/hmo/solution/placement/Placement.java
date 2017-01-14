package hr.fer.tel.hmo.solution.placement;

import hr.fer.tel.hmo.network.Server;
import hr.fer.tel.hmo.util.Util;
import hr.fer.tel.hmo.vnf.Component;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
	private int numberOfServers;

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
	public Placement copy() {
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
	 * Permute this placement
	 */
	public void permute() {
		Util.shuffle(placement);
	}

	public List<Placement> neighbors(int x) {
		List<Placement> neighbors = new LinkedList<>();
		for (int i = 0; i < placement.length; i++) {
			if (x == i) {
				continue;
			}
			Placement p = copy();
			if (Util.randomDouble() < 0.2) {
				p.permute();
			} else {
				Util.swap(p.placement, i, x);
			}
			neighbors.add(p);
		}

		return neighbors;
	}

	/**
	 * Place a component on a server
	 *
	 * @param component component to place
	 * @param server    server on which it goes
	 */
	public void place(Component component, Server server) {
		place(component.getIndex(), server.getIndex());
	}

	/**
	 * Places a component with given index on a server with given index
	 *
	 * @param componentIndex component's index
	 * @param serverIndex    server's index
	 */
	public void place(int componentIndex, int serverIndex) {
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

	public int getNumberOfComponents() {
		return placement.length;
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

		if (numberOfServers != placement1.numberOfServers) {
			return false;
		}
		return Arrays.equals(placement, placement1.placement);
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
