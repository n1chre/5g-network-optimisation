package hr.fer.tel.hmo.util;

import java.util.List;

/**
 * Class that provides utility methods for all other classes
 */
public class Util {

	private Util() {
		// can't be created
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


}
