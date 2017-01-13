package hr.fer.tel.hmo.solution.proxies;


import hr.fer.tel.hmo.network.Link;

import java.util.Comparator;

/**
 * Proxy class for link
 */
public class LinkProxy implements Comparable<LinkProxy>{

	public NodeProxy to;
	public double delay;
	public double bandwidth;
	private double power;
	public boolean used;

	public LinkProxy(NodeProxy to, Link link) {
		this.to = to;
		delay = link.getDelay();
		bandwidth = link.getBandwidth();
		power = link.getPowerConsumption();
		used = false;
	}

	/**
	 * Check if we delay and bandwidth constraints are satisfied with this link
	 *
	 * @param delay     delay
	 * @param bandwidth bandwidth
	 * @return true if they are satisfied
	 */
	public boolean validParams(double delay, double bandwidth) {
		return this.delay <= delay && bandwidth <= this.bandwidth;
	}

	/**
	 * How much will power rise if we choose this link
	 *
	 * @return power up
	 */
	public double powerUp() {
		double power = 0.0;
		if (!used) {
			power += this.power;
		}
		if (!to.used) {
			power += to.node.getPowerConsumption();
		}
		return power;
	}

	@Override
	public int compareTo(LinkProxy o) {
		return Double.compare(powerUp(), o.powerUp());
	}

	public static class LinkComp implements Comparator<LinkProxy> {

		private NodeProxy goalNode;

		public LinkComp(NodeProxy goalNode) {
			this.goalNode = goalNode;
		}

		@Override
		public int compare(LinkProxy x, LinkProxy y) {
			// returns -1 if x is better, 0 if they are same, 1 if y is better

			if (y == null) {
				return -1;
			}
			if (x == null) {
				return 1;
			}

			if (x.to.equals(y.to)) {
				return x.compareTo(y);
			}

			if (goalNode.equals(x.to)) {
				return -1;
			}

			if (goalNode.equals(y.to)) {
				return 1;
			}

			return x.compareTo(y);
		}
	}

}
