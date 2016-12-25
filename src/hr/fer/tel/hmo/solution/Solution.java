package hr.fer.tel.hmo.solution;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a <i>possible</i> solution
 */
public class Solution {

	private Placement placement;

	private List<Route> routes;

	public Solution(Placement placement, List<Route> routes) {
		this.placement = placement;
		this.routes = routes;
	}

	public Placement getPlacement() {
		return placement;
	}

	public List<Route> getRoutes() {
		return routes;
	}

	@Override
	public String toString() {
		String routing = routes.stream()
				.map(Route::toString)
				.collect(Collectors.joining(",\n", "routes={\n", "\n};"));
		return placement + "\n" + routing;
	}
}
