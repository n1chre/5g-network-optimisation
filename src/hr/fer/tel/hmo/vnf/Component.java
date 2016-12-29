package hr.fer.tel.hmo.vnf;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents one component that needs to be placed on a server.
 */
public class Component {

	private int index;

	private List<Double> resources;

	private List<ServiceChain> serviceChains;

	/**
	 * Create a new component that needs resources
	 *
	 * @param index     component's index
	 * @param resources resources needed
	 */
	public Component(int index, List<Double> resources) {
		this.index = index;
		this.resources = resources;
		this.serviceChains = new LinkedList<>();
	}

	/**
	 * Adds service chain to this component which it's a part of.
	 *
	 * @param serviceChain service chain to add
	 */
	public void addServiceChain(ServiceChain serviceChain) {
		serviceChains.add(serviceChain);
	}

	public int getIndex() {
		return index;
	}

	public List<Double> getResources() {
		return resources;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Component)) {
			return false;
		}

		Component component = (Component) o;

		return index == component.index;
	}

	@Override
	public int hashCode() {
		return index;
	}
}
