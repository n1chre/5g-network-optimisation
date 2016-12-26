package hr.fer.tel.hmo.solution;

import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.util.Matrix;

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
	public String toString() {
		StringJoiner routing = new StringJoiner(",\n");
		for (Integer from : routes.keys()) {
			routing.add(
					routes.valuesFor(from).stream()
							.map(Route::toString)
							.collect(Collectors.joining(",\n", "routes={\n", "\n};"))
			);
		}
		return placement + "\n" + routing;
	}
}
