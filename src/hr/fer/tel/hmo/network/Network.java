package hr.fer.tel.hmo.network;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Represents the whole network in this project.
 */
public class Network {

	/**
	 * All nodes
	 */
	private Node[] nodes;

	/**
	 * All servers
	 */
	private Server[] servers;

	/**
	 * Maps nodes to a list of links (incoming and outgoing)
	 */
	private Map<Node, List<Link>> links;

	/**
	 * Minimal delay between nodes
	 */
	private double[][] delay;

	public Network(int numberOfNodes, int numberOfServers) {
		nodes = new Node[numberOfNodes];
		servers = new Server[numberOfServers];
		links = new HashMap<>();
	}

	/**
	 * Adds a node to this network if it wasn't added previously
	 *
	 * @param node node to add
	 * @return whether a node was added
	 */
	public boolean addNode(Node node) {
		int index = node.getIndex();

		if (index < 0 || index >= nodes.length) {
			return false;
		}

		if (nodes[index] != null) {
			return false;
		}

		nodes[index] = node;
		links.put(node, new LinkedList<>());

		return true;
	}

	/**
	 * Add a new link between two nodes. Links are considered as bidirectional
	 *
	 * @param n1               first node index
	 * @param n2               second node index
	 * @param bandwidth        link's bandwidth
	 * @param powerConsumption link's power consumption
	 * @param delay            link's delay
	 * @return true if link was successfully added
	 */
	public boolean addLink(int n1, int n2, double bandwidth, double powerConsumption, double delay) {
		if (n1 < 0 || n1 >= nodes.length || n2 < 0 || n2 >= nodes.length) {
			return false;
		}

		if (nodes[n1] == null || nodes[n2] == null) {
			return false;
		}

		Node node1 = nodes[n1];
		Node node2 = nodes[n2];

		// bidirectional links
		Link l1 = new Link(node1, node2, bandwidth, powerConsumption, delay);
		Link l2 = new Link(node2, node1, bandwidth, powerConsumption, delay);

		links.get(node1).add(l1);
		links.get(node2).add(l2);

		return true;
	}

	/**
	 * Connect new server to this network
	 *
	 * @param serverIndex server's index
	 * @param pmin        minimal power consumption
	 * @param pmax        maximal power consumption
	 * @param nodeIdx     index of node to which it is connected
	 * @param resources   resources that it uses
	 * @return true if it was added and connected
	 */
	public boolean connectServer(int serverIndex, double pmin, double pmax, int nodeIdx, List<Double> resources) {
		if (serverIndex < 0 || serverIndex >= servers.length) {
			return false;
		}
		if (nodeIdx < 0 || nodeIdx >= nodes.length) {
			return false;
		}

		if (servers[serverIndex] != null) {
			return false;
		}

		Node node = nodes[nodeIdx];
		Server server = new Server(serverIndex, pmin, pmax, node, resources);

		servers[serverIndex] = server;

		return true;
	}
}
