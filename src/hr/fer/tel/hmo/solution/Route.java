package hr.fer.tel.hmo.solution;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Represents a route between two components
 */
public class Route {

	/**
	 * Index of a component from which we route traffic
	 */
	private int from;

	/**
	 * Index of a component to which we route traffic
	 */
	private int to;

	/**
	 * Indexes of all nodes on this route
	 */
	private int[] nodes;

	public Route(int from, int to, int[] nodes) {
		this.from = from;
		this.to = to;
		this.nodes = nodes;
		if (nodes.length > 0 && nodes[0] == nodes[nodes.length - 1]) {
			// they are on the same node
			nodes = new int[]{nodes[0]};
		}
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public int[] getNodes() {
		return nodes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Route)) {
			return false;
		}

		Route route = (Route) o;

		if (from != route.from) {
			return false;
		}
		return to == route.to;
	}

	@Override
	public int hashCode() {
		int result = from;
		result = 31 * result + to;
		return result;
	}

	@Override
	public String toString() {
		return String.format("<%d,%d,%s>", from + 1, to + 1,
				Arrays.stream(nodes)
						.mapToObj(i -> Integer.toString(i + 1))
						.collect(Collectors.joining(",", "[", "]"))
		);
	}
}
