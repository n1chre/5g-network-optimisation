package hr.fer.tel.hmo.solution.proxies;

import hr.fer.tel.hmo.network.Server;
import hr.fer.tel.hmo.vnf.Component;

/**
 * Proxy class for Server
 */
public class ServerProxy {

	public final int index;
	private final double pmin;
	private final double pmax;

	private final double totalProcRes;
	private final double totalMemRes;

	private double usedProcRes;
	private double usedMemRes;

	private final NodeProxy np;

	public ServerProxy(Server s, NodeProxy np) {
		index = s.getIndex();
		pmin = s.getPmin();
		pmax = s.getPmax();

		totalProcRes = s.getResources().get(0);
		totalMemRes = s.getResources().get(1);
		usedProcRes = 0;
		usedMemRes = 0;

		this.np = np;
	}

	public boolean canGo(Component c) {
		return usedProcRes + c.getResources().get(0) <= totalProcRes &&
				usedMemRes + c.getResources().get(1) <= totalMemRes;
	}

	public void emplace(Component c) {
		usedProcRes += c.getResources().get(0);
		usedMemRes += c.getResources().get(1);
		np.used = true;
	}

	public double powerUp(Component c) {
		double power = np.used ? 0.0 : np.node.getPowerConsumption();
		power += Double.compare(usedMemRes + usedProcRes, 0.0) == 0 ? pmin : 0.0;
		power += (pmax - pmin) * c.getResources().get(0) / totalProcRes;
		return power;
	}

}
