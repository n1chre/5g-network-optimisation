package hr.fer.tel.hmo.solution;

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
	 * Indexes of intermediate nodes on this route (not including those
	 * nodes which are connected to servers having these components)
	 */
	private int[] intermediate;

	public Route(int from, int to, int[] intermediate) {
		this.from = from;
		this.to = to;
		this.intermediate = intermediate;
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public int[] getIntermediate() {
		return intermediate;
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
}
