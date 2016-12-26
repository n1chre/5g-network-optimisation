package hr.fer.tel.hmo.solution.placement;

import hr.fer.tel.hmo.network.Server;
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
	 * placement[c] = s+1
	 * For each component index we store server's index+1.
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
	 * randomize placement
	 */
	public void random() {
		for (int c = 0; c < placement.length; c++) {
			place(c, Util.randomInt(numberOfServers));
		}
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
		placement[componentIndex] = serverIndex + 1;
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
		return placement[componentIndex] - 1;
	}

	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n", "x=[\n", "\n];");

		for (final int sIdx : placement) {
			sj.add(
					IntStream.range(0, numberOfServers)
							.mapToObj(i -> i == sIdx - 1 ? "1" : "0")
							.collect(Collectors.joining(",", "[", "]"))
			);
		}

		return sj.toString();
	}
}
