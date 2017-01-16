package hr.fer.tel.hmo.network;

import hr.fer.tel.hmo.util.Matrix;

import java.util.List;

/**
 * Represents the whole network in this project.
 */
public class Network {

	/**
	 * All nodes
	 */
	private final Node[] nodes;

	/**
	 * All servers
	 */
	private final Server[] servers;

	/**
	 * links[from][to] = link
	 */
	private final Matrix<Integer, Integer, Link> links;

	/**
	 * Create a new network with desired number of nodes and servers.
	 * Network is configured using addNode, addLink and addServer methods
	 *
	 * @param numberOfNodes   number of nodes in network
	 * @param numberOfServers number of servers in network
	 */
	public Network(int numberOfNodes, int numberOfServers) {
		nodes = new Node[numberOfNodes];
		servers = new Server[numberOfServers];
		links = new Matrix<>();
	}

	/**
	 * @return number of nodes
	 */
	public int getNumberOfNodes() {
		return nodes.length;
	}

	/**
	 * @return number of servers
	 */
	public int getNumberOfServers() {
		return servers.length;
	}

	/**
	 * @param index node's index
	 * @return node with given index
	 */
	public Node getNode(int index) {
		return nodes[index];
	}

	/**
	 * @param index server's index
	 * @return server at given index
	 */
	public Server getServer(int index) {
		return servers[index];
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

		return true;
	}

	/**
	 * Add a new link between two nodes. Links are considered as unidirectional
	 *
	 * @param n1   first node index
	 * @param n2   second node index
	 * @param link link between two nodes
	 * @return true if link was successfully added
	 */
	public boolean addLink(int n1, int n2, Link link) {
		if (n1 < 0 || n1 >= nodes.length || n2 < 0 || n2 >= nodes.length) {
			return false;
		}

		if (nodes[n1] == null || nodes[n2] == null) {
			return false;
		}

		//		ret &= links.put(n2, n1, link) == null;

		return links.put(n1, n2, link) == null;
	}

	/**
	 * Get link between two nodes
	 *
	 * @param from index of first node
	 * @param to   index of second node
	 * @return link between two nodes or null if such doesn't exist
	 */
	public Link getLink(int from, int to) {
		return links.get(from, to);
	}

	/**
	 * Returns all links. Indexes are node indexes
	 *
	 * @return matrix of links
	 */
	public Matrix<Integer, Integer, Link> getLinks() {
		return links;
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
	public boolean addServer(int serverIndex, double pmin, double pmax, int nodeIdx, List<Double> resources) {
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
