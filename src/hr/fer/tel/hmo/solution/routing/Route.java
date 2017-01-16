package hr.fer.tel.hmo.solution.routing;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a route between two components
 */
public class Route {

	/**
	 * Index of a component from which we route traffic
	 */
	private final int from;

	/**
	 * Index of a component to which we route traffic
	 */
	private final int to;

	/**
	 * Indexes of all nodes on this route
	 */
	private int[] nodes;

	Route(int from, int to, List<Integer> nodes) {
		this.from = from;
		this.to = to;
		int n = nodes.size();
		if (n == 0) {
			throw new RuntimeException("Can't be 0");
		}

		if (nodes.get(0).equals(nodes.get(n - 1))) {
			// they are on the same node
			this.nodes = new int[]{nodes.get(0)};
		} else {
			this.nodes = new int[nodes.size()];
			int idx = 0;
			for (Integer i : nodes) {
				this.nodes[idx++] = i;
			}
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
		if (to != route.to) {
			return false;
		}
		return Arrays.equals(nodes, route.nodes);
	}

	@Override
	public int hashCode() {
		int result = from;
		result = 31 * result + to;
		result = 31 * result + Arrays.hashCode(nodes);
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
