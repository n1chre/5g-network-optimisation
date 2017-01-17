package hr.fer.tel.hmo.network;

import hr.fer.tel.hmo.util.Matrix;

import java.util.function.Function;

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
		return insert(node, Node::getIndex, nodes);
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

		return !(nodes[n1] == null || nodes[n2] == null) && links.put(n1, n2, link) == null;

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
	 * @param server server
	 * @return true if it was added and connected
	 */
	public boolean addServer(Server server) {
		return insert(server, Server::getIndex, servers);
	}

	/**
	 * Insert element into an array
	 *
	 * @param t        element
	 * @param getIndex index extractor
	 * @param ts       array of t's
	 * @return true if it was inserted (if ts[index] was null)
	 */
	private <T> boolean insert(T t, Function<T, Integer> getIndex, T[] ts) {
		final int index = getIndex.apply(t);
		if (index < 0 || index >= ts.length) {
			return false;
		}

		if (ts[index] != null) {
			return false;
		}

		ts[index] = t;

		return true;
	}

}
