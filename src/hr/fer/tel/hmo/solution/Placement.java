package hr.fer.tel.hmo.solution;

import hr.fer.tel.hmo.network.Server;
import hr.fer.tel.hmo.vnf.Component;

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
	 * Create a new empty placement.
	 *
	 * @param numberOfComponents number of components
	 */
	public Placement(int numberOfComponents) {
		placement = new int[numberOfComponents];
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

}
