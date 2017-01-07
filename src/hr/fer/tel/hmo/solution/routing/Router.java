package hr.fer.tel.hmo.solution.routing;

import hr.fer.tel.hmo.network.Link;
import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.util.Matrix;
import hr.fer.tel.hmo.vnf.ServiceChain;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Routes connections over network (greedy)
 */
public class Router {

	private Topology topology;

	private Map<Integer, List<LinkProxy>> neighbors;

	public Router(Topology topology) {
		this.topology = topology;
		this.neighbors = new HashMap<>();

		Matrix<Integer, Integer, Link> links = topology.getNetwork().getLinks();

		for (int n1 : links.keys()) {
			neighbors.put(n1,
					links.getFor(n1).entrySet()
							.parallelStream()
							.map(LinkProxy::new)
							.collect(Collectors.toCollection(LinkedList::new)));
		}

	}

	public Matrix<Integer, Integer, Route> findRouting(Placement placement) {

		// create cache
		Function<Integer, Integer> nodeIdx =
				cidx -> topology.getNetwork().getServer(
						placement.getPlacementFor(cidx)
				).getNode().getIndex();

		int numComps = topology.getComponents().length;
		int[] compCache = new int[numComps];
		for (int i = 0; i < numComps; i++) {
			compCache[i] = nodeIdx.apply(topology.getComponents()[i].getIndex());
		}

		Matrix<Integer, Integer, Route> routes = new Matrix<>();

		for (ServiceChain sc : topology.getServiceChains()) {
			int ncs = sc.getNumberOfComponents();
			if (ncs <= 1) {
				continue;
			}

			double delay = sc.getLatency();

			int prevCompIdx = sc.getComponent(0).getIndex();
			int prevNodeIdx = compCache[prevCompIdx];
			for (int i = 1; i < ncs; i++) {

				int currCompIdx = sc.getComponent(i).getIndex();
				if (null != routes.get(prevCompIdx, currCompIdx)) {
					continue;
				}

				int currNodeIdx = compCache[currCompIdx];
				Double bandwidth = topology.getDemands().get(prevCompIdx, currCompIdx);
				if (bandwidth == null) {
					bandwidth = 0.0;
				}

				List<Integer> r = path(
						prevNodeIdx, currNodeIdx,
						bandwidth, delay,
						new HashSet<>(), new ArrayList<>()
				);
				if (r == null) {
					return null;
				}

				routes.put(prevCompIdx, currCompIdx, new Route(prevCompIdx, currCompIdx, r));
				prevCompIdx = currCompIdx;
				prevNodeIdx = currNodeIdx;
			}
		}

		return routes;
	}


	/**
	 * Find a route that goes from one node to other with given demands
	 *
	 * @param from              start node
	 * @param end               end node
	 * @param delay             maximal delay
	 * @param demandedBandwidth demanded bandwidth
	 * @param forbidden         forbidden nodes
	 * @param path              current path
	 * @return list of nodes or null if route not found
	 */
	private List<Integer> path(int from, int end, double delay, double demandedBandwidth,
	                           HashSet<Integer> forbidden, List<Integer> path) {
		path.add(from);

		if (from == end) {
			return path;
		}

		forbidden.add(from);

		LinkProxy best = null;
		for (LinkProxy lp : neighbors.get(from)) {
			if (forbidden.contains(lp.to)) {
				continue;
			}

			if (delay < lp.delay) {
				continue;
			}

			if (lp.bandwidth < demandedBandwidth) {
				continue;
			}

			// if it's a link to ending node, use it
			if (end == lp.to) {
				best = lp;
				break;
			}

			best = lp.better(best);
		}

		if (best == null) {
			return null;
		}

		// use that link
		best.bandwidth -= demandedBandwidth;
		delay -= best.delay;

		return path(best.to, end, delay, demandedBandwidth, forbidden, path);
	}

	private static class LinkProxy implements Comparable<LinkProxy> {

		int to;
		double delay;
		double bandwidth;
		double power;
		boolean used;

		LinkProxy(Map.Entry<Integer, Link> entry) {
			this(entry.getKey(), entry.getValue());
		}

		LinkProxy(int to, Link link) {
			this.to = to;
			delay = link.getDelay();
			bandwidth = link.getBandwidth();
			power = link.getPowerConsumption();
			used = false;
		}

		@Override
		public int compareTo(LinkProxy other) {
			// better to use the one that is already used
			if (used != other.used) {
				return used ? -1 : 1;
			}

			int c;

			// if neither is used, prioritize power
			if (!used) {
				c = Double.compare(power, other.power);
				if (c != 0) {
					return c;
				}
			}

			// if both are used, try to take the one with smaller delay

			c = Double.compare(delay, other.delay);
			if (c != 0) {
				return c;
			}

			c = Double.compare(bandwidth, other.bandwidth);
			if (c != 0) {
				return c;
			}

			return Double.compare(power, other.power);
		}

		LinkProxy better(LinkProxy other) {
			if (other == null) {
				return this;
			}
			return compareTo(other) <= 0 ? this : other;
		}

		@Override
		public String toString() {
			return Integer.toString(to);
		}
	}

}
