package hr.fer.tel.hmo.solution;

import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.solution.routing.Route;
import hr.fer.tel.hmo.util.Matrix;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Represents a <i>possible</i> solution
 */
public class Solution {

	private Placement placement;

	private Matrix<Integer, Integer, Route> routes;

	public Solution(Placement placement, Matrix<Integer, Integer, Route> routes) {
		this.placement = placement;
		this.routes = routes;
	}

	public Placement getPlacement() {
		return placement;
	}

	public Matrix<Integer, Integer, Route> getRoutes() {
		return routes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Solution)) {
			return false;
		}

		Solution solution = (Solution) o;

		if (placement != null ? !placement.equals(solution.placement) : solution.placement != null) {
			return false;
		}
		return routes != null ? routes.equals(solution.routes) : solution.routes == null;
	}

	@Override
	public int hashCode() {
		int result = placement != null ? placement.hashCode() : 0;
		result = 31 * result + (routes != null ? routes.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		StringJoiner routing = new StringJoiner(",\n", "routes={\n", "\n};");
		for (Integer from : routes.keys()) {
			routing.add(
					routes.valuesFor(from).stream()
							.map(Route::toString)
							.collect(Collectors.joining(",\n"))
			);
		}
		return placement + "\n" + routing;
	}
}
