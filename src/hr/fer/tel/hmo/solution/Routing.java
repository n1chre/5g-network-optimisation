package hr.fer.tel.hmo.solution;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Represents a routing for given problem
 */
public class Routing implements Iterable<Route> {

	/**
	 * List of all routes
	 */
	private Set<Route> routes;

	/**
	 * Create an empty routing
	 */
	public Routing() {
		routes = new HashSet<>();
	}

	public void addRoute(Route route) {
		routes.add(route);
	}

	@Override
	public Iterator<Route> iterator() {
		return routes.iterator();
	}

}
