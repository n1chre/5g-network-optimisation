package hr.fer.tel.hmo.util;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * This class is a wrapper around matrix class.
 * It can be used to store values under key, which is a pair of values
 */
public class Matrix<K1, K2, V> {

	private final Map<K1, Map<K2, V>> matrix = new HashMap<>();

	/**
	 * Put value into matrix under matrix[k1][k2]
	 *
	 * @param k1 key 1
	 * @param k2 key 2
	 * @param v  value
	 * @return previous value or null if none
	 */
	public V put(K1 k1, K2 k2, V v) {
		matrix.putIfAbsent(k1, new HashMap<>());
		return matrix.get(k1).put(k2, v);
	}

	/**
	 * Get value stored at matrix[k1][k2]
	 *
	 * @param k1 key 1
	 * @param k2 key 2
	 * @return value or null if none
	 */
	public V get(K1 k1, K2 k2) {
		Map<K2, V> map = matrix.get(k1);
		return map == null ? null : map.get(k2);
	}

	/**
	 * @param k1 key 1
	 * @return mapTo that is used for that key
	 */
	public Map<K2, V> getFor(K1 k1) {
		return matrix.get(k1);
	}

	/**
	 * @return set of main keys
	 */
	public Set<K1> keys() {
		return matrix.keySet();
	}

	/**
	 * @param k1 key 1
	 * @return values that are stored in a mapTo for key 1
	 */
	public Collection<V> valuesFor(K1 k1) {
		return matrix.get(k1).values();
	}

	public Collection<V> values() {
		Collection<V> C = new LinkedList<>();

		for (K1 k1 : keys()) {
			C.addAll(valuesFor(k1));
		}

		return C;
	}


	/**
	 * Map function over values in matrix.
	 *
	 * @param function mapping function
	 * @param <V2>     type of resulting value
	 * @return new created matrix
	 */
	public <V2> Matrix<K1, K2, V2> mapTo(Function<V, V2> function) {
		Matrix<K1, K2, V2> ret = new Matrix<>();

		for (Map.Entry<K1, Map<K2, V>> e1 : matrix.entrySet()) {
			K1 k1 = e1.getKey();
			Map<K2, V> m = e1.getValue();
			for (Map.Entry<K2, V> e2 : m.entrySet()) {
				ret.put(k1, e2.getKey(), function.apply(e2.getValue()));
			}
		}

		return ret;
	}

	/**
	 * Map a function to each value in a map
	 *
	 * @param function function
	 */
	public void map(Function<V, V> function) {
		for (Map<K2, V> m : matrix.values()) {
			for (K2 k2 : m.keySet()) {
				m.compute(k2, (__, v) -> function.apply(v));
			}
		}
	}

	/**
	 * Create a matrix with only filtered values
	 *
	 * @param filter used to filter values
	 * @return new matrix
	 */
	public Matrix<K1, K2, V> filter(Predicate<V> filter) {
		final Matrix<K1, K2, V> _matrix = new Matrix<>();

		for (Map.Entry<K1, Map<K2, V>> e1 : matrix.entrySet()) {
			for (Map.Entry<K2, V> e2 : e1.getValue().entrySet()) {
				V v = e2.getValue();
				if (filter.test(v)) {
					_matrix.put(e1.getKey(), e2.getKey(), v);
				}
			}
		}

		return _matrix;
	}

	/**
	 * Compute for single value in matrix
	 *
	 * @param k1       first key
	 * @param k2       second key
	 * @param function function
	 */
	public void compute(K1 k1, K2 k2, Function<V, V> function) {
		Map<K2, V> map = matrix.get(k1);
		if (map == null) {
			return;
		}
		map.compute(k2, (__, v) -> function.apply(v));
	}

	@Override
	public String toString() {
		StringJoiner mainJoiner = new StringJoiner("\n\t", "{\n\t", "\n}");
		for (K1 k1 : keys()) {
			for (Map.Entry<K2, V> e : getFor(k1).entrySet()) {
				mainJoiner.add(k1 + " " + e.getKey() + " " + e.getValue());
			}
		}
		return mainJoiner.toString();
	}
}
