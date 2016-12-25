package hr.fer.tel.hmo.util;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a wrapper around matrix class.
 * It can be used to store values under key, which is a pair of values
 */
public class Matrix<K1, K2, V> {

	private final Map<KeyPair<K1, K2>, V> matrix = new HashMap<>();

	/**
	 * Put value into matrix under matrix[k1][k2]
	 *
	 * @param k1 key 1
	 * @param k2 key 2
	 * @param v  value
	 * @return previous value or null if none
	 */
	public V put(K1 k1, K2 k2, V v) {
		KeyPair<K1, K2> kp = new KeyPair<>(k1, k2);
		return matrix.put(kp, v);
	}

	/**
	 * Get value stored at matrix[k1][k2]
	 *
	 * @param k1 key 1
	 * @param k2 key 2
	 * @return value or null if none
	 */
	public V get(K1 k1, K2 k2) {
		return matrix.get(new KeyPair<>(k1, k2));
	}

	/**
	 * Represents a pair of keys for matrix
	 *
	 * @param <K1> first key type
	 * @param <K2> second key type
	 */
	private static class KeyPair<K1, K2> {
		K1 k1;
		K2 k2;

		KeyPair(K1 k1, K2 k2) {
			this.k1 = k1;
			this.k2 = k2;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) {
				return true;
			}
			if (!(o instanceof Matrix.KeyPair)) {
				return false;
			}

			KeyPair<?, ?> keyPair = (KeyPair<?, ?>) o;

			return k1.equals(keyPair.k1) && k2.equals(keyPair.k2);
		}

		@Override
		public int hashCode() {
			int result = k1.hashCode();
			result = 31 * result + k2.hashCode();
			return result;
		}
	}

}
