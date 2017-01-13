package hr.fer.tel.hmo.solution.proxies;

import hr.fer.tel.hmo.network.Node;

/**
 * Created by fhrenic on 13/01/2017.
 */
public class NodeProxy {

	public Node node;
	public boolean used;

	public NodeProxy(Node node) {
		this.node = node;
		used = false;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof NodeProxy)) {
			return false;
		}

		NodeProxy nodeProxy = (NodeProxy) o;

		return node.getIndex() == nodeProxy.node.getIndex();
	}

	@Override
	public int hashCode() {
		return node.getIndex();
	}
}
