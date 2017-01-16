package hr.fer.tel.hmo.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

/**
 * Class that provides utility methods for all other classes
 */
public class Util {

	public static final double EPS = 1e-6;
	public static final Random RANDOM = ThreadLocalRandom.current();

	private Util() {
		// can't be created
	}

	/**
	 * Perform roulette selection on given collection
	 * it should hold that sum(probability(t) for t in ts) = 1
	 *
	 * @param ts          collection
	 * @param probability knows what is probability of choosing each element
	 * @param extractor   knows how to extract value from each element
	 * @return chosen unit
	 */
	public static <T, R> R roulette(
			Collection<T> ts,
			Function<T, Double> probability,
			Function<T, R> extractor
	) {
		if (ts.isEmpty()) {
			return null;
		}

		double p = Util.randomDouble();
		double P = 0.0;

		for (T t : ts) {

			double p_ = probability.apply(t);
			if (p_ < 0.0 || p_ > 1.0 || Double.isNaN(P) || Double.isInfinite(P)) {
				throw new RuntimeException("Probability for " + t + " not in [0,1] range");
			}
			P += p_;

			if (p <= P) {
				return extractor.apply(t);
			}
		}

		throw new RuntimeException("Can't happen");
	}

	/**
	 * Create c random indexes from [0,n> interval
	 *
	 * @param n parameter n
	 * @param c parameter c
	 * @return random indexes
	 */
	public static int[] rndIndexes(int n, int c) {
		int[] idxs = new int[n];
		for (int i = 0; i < n; i++) {
			idxs[i] = i;
		}
		for (int i = 0; i < c; i++) {
			swap(idxs, i, i + randomInt(n - i));
		}
		return Arrays.copyOf(idxs, c);
	}

	/**
	 * Check if matrix dimensions are n rows by m columns.
	 * If n=-1, only columns are checked
	 *
	 * @param matrix matrix to check
	 * @param n      expected number of rows
	 * @param m      expected number of columns
	 * @return true if matrix is n x m
	 */
	public static boolean checkMatrix(List<List<Double>> matrix, int n, int m) {
		if (matrix == null) {
			return false;
		}

		if (n >= 0 && matrix.size() != n) {
			return false;
		}

		for (List<Double> row : matrix) {
			if (row.size() != m) {
				return false;
			}
		}

		return true;
	}

	/**
	 * Check if size of given array is n
	 *
	 * @param array array to check
	 * @param n     expected size
	 * @return true if it is of expected size
	 */
	public static boolean checkArray(List<Double> array, int n) {
		return array != null && array.size() == n;
	}

	/**
	 * Return randomize integer that is bounded by given bound
	 *
	 * @param bound integer bound
	 * @return randomize integer
	 */
	public static int randomInt(int bound) {
		return RANDOM.nextInt(bound);
	}

	/**
	 * @return randomize double with uniform distribution on [0,1]
	 */
	public static double randomDouble() {
		return RANDOM.nextDouble();
	}

	public static <T> void shuffle(T[] arr) {
		int n = arr.length;
		if (n <= 1) {
			return;
		}
		for (int i = n - 1; i > 0; --i) {
			swap(arr, i, randomInt(i));
		}
	}

	/**
	 * Swap values in array
	 *
	 * @param arr array
	 * @param i   index i
	 * @param j   index j
	 */
	private static <T> void swap(T[] arr, int i, int j) {
		T t = arr[i];
		arr[i] = arr[j];
		arr[j] = t;
	}

	/**
	 * Shuffle values in an array
	 * Uses Knuth's shuffling shuffle
	 *
	 * @param arr array to shuffle
	 */
	public static void shuffle(int[] arr) {
		int n = arr.length;
		if (n <= 1) {
			return;
		}
		for (int i = n - 1; i > 0; --i) {
			swap(arr, i, randomInt(i));
		}
	}

	/**
	 * swap ints at given positions in array
	 *
	 * @param arr array
	 * @param i   position
	 * @param j   position
	 */
	public static void swap(int[] arr, int i, int j) {
		arr[i] ^= arr[j];
		arr[j] ^= arr[i];
		arr[i] ^= arr[j];
	}

	/**
	 * Write content to file
	 *
	 * @param content  content
	 * @param filename filename
	 */
	public static void toFile(String content, String filename) throws IOException {
		Files.write(Paths.get(filename), Collections.singletonList(content));
	}

}
