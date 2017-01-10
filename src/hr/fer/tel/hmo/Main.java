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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

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

		Placer placer = Placer.get(t, evaluator::isValid);
		Router router = Router.get(t);

		Timer timer = new Timer(true);
		new SolutionWriter(timer); // FIXME VERY BAD SOLUTION

		System.err.println("Starting tabu runs...");

		ExecutorService es = Executors.newCachedThreadPool();
		Semaphore semaphore = new Semaphore(0);
		for (int i = 0; i < TABU_RUNS; i++) {
			es.submit(new Solver(evaluator, router, placer, semaphore));
		}
		es.shutdown();
		try {
			semaphore.acquire(TABU_RUNS);
		} catch (InterruptedException ex) {
			ex.printStackTrace(System.err);
			System.exit(5);
		}

		System.out.println("Best found = " + -bestRS.getFitness());
		System.out.println(bestRS.getSolution());

	}


	private static class Solver implements Runnable {

		private Evaluator evaluator;
		private Router router;
		private Placer placer;
		private Semaphore semaphore;

		Solver(Evaluator evaluator, Router router, Placer placer, Semaphore semaphore) {
			this.evaluator = evaluator;
			this.router = router;
			this.placer = placer;
			this.semaphore = semaphore;
		}

		@Override
		public void run() {

			Placement p;
			Matrix<Integer, Integer, Route> rts;

			do {
				p = placer.next();
				rts = router.findRouting(p);
			} while (rts == null);
			if (!evaluator.isValid(p)) {
				throw new RuntimeException("Initial placement not valid");
			}

			Solution s = new Solution(p, rts);
			if (!evaluator.isValid(s)) {
				throw new RuntimeException("Not valid");
			}

			RoutingProblem rp = new RoutingProblem(evaluator, router, s);
			RoutingSolution rs = TabuSearch.search(rp);
			if (rs != null) {
				synchronized (RS_LOCK) {
					if (bestRS == null || rs.isBetterThan(bestRS)) {
						System.err.println("Found new best solution!");
						bestRS = rs;
					}
				}
			}

			semaphore.release();
		}
	}

	/**
	 * Used to write solutions at given times
	 */
	private static class SolutionWriter extends TimerTask {

		private static int[] times = new int[]{1, 5, 60};
		private int currIdx;

		private Timer timer;

		SolutionWriter(Timer timer) {
			this(timer, 0);
		}

		private SolutionWriter(Timer timer, int idx) {
			currIdx = idx;
			this.timer = timer;

			int diff = times[idx];
			if (idx > 0) {
				diff -= times[idx - 1];
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
						Util.toFile(bestRS.getSolution().toString(), filename);
					} catch (IOException ex) {
						System.err.println("Error while writing to file");
						ex.printStackTrace();
					}
				} else {
					System.err.println("No solution found for " + filename);
				}

				if (++currIdx < times.length) {
					new SolutionWriter(timer, currIdx);
				}
			}
		}
	}


}
