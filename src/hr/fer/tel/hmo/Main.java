package hr.fer.tel.hmo;

import hr.fer.tel.hmo.instance.Instance;
import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.Evaluator;
import hr.fer.tel.hmo.solution.Solution;
import hr.fer.tel.hmo.solution.placement.Placement;
import hr.fer.tel.hmo.solution.placement.Placer;
import hr.fer.tel.hmo.solution.routing.Route;
import hr.fer.tel.hmo.solution.routing.Router;
import hr.fer.tel.hmo.tabu.alg.TabuSearch;
import hr.fer.tel.hmo.tabu.impl.RoutingProblem;
import hr.fer.tel.hmo.tabu.impl.RoutingSolution;
import hr.fer.tel.hmo.util.Matrix;
import hr.fer.tel.hmo.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;
import java.util.TimerTask;

/**
 */
public class Main {

	private static final int TABU_RUNS = 1;
	private static final Object RS_LOCK = new Object();

	private static RoutingSolution bestRS = null;

	public static void main(String[] args) {

		int x = 1;

		String name = "instance" + (x == 0 ? "_small" : "-bez_43_44") + ".txt";
		args = new String[]{"/Users/fhrenic/Programming/vnf-placer/" + name};

		InputStream stream;
		if (args.length > 0) {
			try {
				Path p = Paths.get(args[0]);
				stream = Files.newInputStream(p);
			} catch (IOException ex) {
				System.err.println("File not found, reading from stdin");
				stream = System.in;
			}
		} else {
			stream = System.in;
		}

		Instance instance;
		try {
			instance = Instance.readFromStream(stream);
		} catch (IOException ex) {
			System.err.println("Error while reading from stream");
			ex.printStackTrace();
			return;
		} catch (IllegalArgumentException ex) {
			ex.printStackTrace();
			return;
		}
		System.err.println("Network configured...");


		Topology t = instance.getTopology();

		// Create evalator
		Evaluator evaluator = new Evaluator(t);
		System.err.println("Evaluator created...");

		Placer p = Placer.get(t, evaluator::isValid);

		Timer timer = new Timer(true);
		SolutionWriter sw = new SolutionWriter(timer);
		sw.setTimer();

		System.err.println("Starting tabu runs...");
		Matrix<Integer, Integer, Route> rts;
		for (int i = 0; i < TABU_RUNS; i++) {
			// TODO parallel

			Placement p_;
			do {
				p_ = p.next();
				rts = new Router(t).findRouting(p_);
			} while (rts == null);

			if (!evaluator.isValid(p_)) {
				throw new RuntimeException("kak idiote");
			}

			Solution s = new Solution(p_, rts);
			if (!evaluator.isValid(s)) {
				throw new RuntimeException("Not valid");
			}

			System.err.printf("\tTabu[%d]...%n", i);

			RoutingProblem rp = new RoutingProblem(evaluator, t, new Solution(p_, rts));
			RoutingSolution rs = TabuSearch.search(rp);
			if (rs != null) {
				synchronized (RS_LOCK) {
					if (bestRS == null || rs.isBetterThan(bestRS)) {
						bestRS = rs;
					}
				}
			}

		}

		System.out.println("Best found = " + -bestRS.getFitness());
		System.out.println(bestRS.getSolution());

	}

	private static class SolutionWriter extends TimerTask {

		private static int[] times = new int[]{1, 5, 60};
		private int currIdx;

		private Timer timer;

		SolutionWriter(Timer timer) {
			currIdx = 0;
			this.timer = timer;
		}

		void setTimer() {
			if (currIdx >= times.length) {
				return;
			}

			int diff = times[currIdx];
			if (currIdx > 0) {
				diff -= times[currIdx - 1];
			}

			timer.schedule(this, diff * 60000);
		}

		@Override
		public void run() {
			synchronized (RS_LOCK) {
				int minute = times[currIdx];
				int hour = minute / 60;
				minute %= 60;

				String filename = "res-";
				if (hour > 0) {
					filename += hour + "h-";
				}
				if (minute > 0) {
					filename += minute + "m-";
				}
				filename += "hrenic.txt";

				if (bestRS != null) {
					try {
						Util.toFile(bestRS.getSolution(), filename);
					} catch (IOException ex) {
						System.err.println("Error while writing to file");
						ex.printStackTrace();
					}
				} else {
					System.err.println("No solution found for " + filename);
				}

				currIdx++;
				setTimer();
			}
		}
	}


}
