package hr.fer.tel.hmo.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
	 * @return map that is used for that key
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
	 * @return values that are stored in a map for key 1
	 */
	public Collection<V> valuesFor(K1 k1) {
		return matrix.get(k1).values();
	}

}
