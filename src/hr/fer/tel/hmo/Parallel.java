package hr.fer.tel.hmo;

import java.util.Collection;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is used to execute tasks in parallel.
 * If some task returns an acceptable result, any other computation is discarded.
 * If none of them return an acceptable result, default value is returned.
 */
public abstract class Parallel<T> implements Observer {

	/*
	 * TODO
	 * Implement Parallel so it doesn't extend Observable. Instead it should
	 * return a Future<T> on run call.
	 */

	/**
	 * Parallel AND computation of boolean computations.
	 * If any call produces a false, false is returned and all other calls are stopped.
	 */
	public static Parallel<Boolean> PARALLEL_AND() {
		return new Parallel<Boolean>() {
			@Override
			protected boolean isAcceptable(Boolean b) {
				return !b;
			}

			@Override
			protected Boolean defaultValue() {
				return true;
			}
		};
	}

	/**
	 * Parallel OR computation of boolean computations.
	 * If any call produces a true, true is returned and all other calls are stopped.
	 */
	public static Parallel<Boolean> PARALLEL_OR() {
		return new Parallel<Boolean>() {
			@Override
			protected boolean isAcceptable(Boolean b) {
				return b;
			}

			@Override
			protected Boolean defaultValue() {
				return false;
			}
		};
	}

	/**
	 * Task counter. When it goes down to zero, all tasks have finished
	 * and we return the default value
	 */
	private AtomicInteger ai;

	private final ExecutorService executor;

	private CompletableFuture<T> future;

	/**
	 * Create a new parallel object.
	 */
	public Parallel() {
		this.executor = Executors.newCachedThreadPool();
		this.ai = new AtomicInteger(0);
	}

	/**
	 * @param t value to test
	 * @return true if it is an acceptable value, it will be returned
	 */
	protected abstract boolean isAcceptable(T t);

	/**
	 * @return default value that is returned if no task produces an acceptable value
	 */
	protected abstract T defaultValue();

	/**
	 * Override this method if you want to examine all produced values.
	 *
	 * @param t value
	 */
	protected void onEach(T t) {
	}

	/**
	 * Run all tasks in parallel
	 *
	 * @param callables these will be called in parallel
	 */
	public Future<T> run(Collection<Callable<T>> callables) {
		if (ai.get() != 0) {
			throw new RuntimeException("Already running");
		}

		ai.set(callables.size());
		callables.parallelStream().map(Task::new).forEach(executor::submit);
		executor.shutdown();

		future = new CompletableFuture<>();
		return future;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void update(Observable o, Object arg) {
		T t = (T) arg;
		onEach(t);
		boolean acceptable = isAcceptable(t);
		int curr = ai.decrementAndGet();
		if (!acceptable) {
			if (curr <= 0) {
				onFinish(defaultValue());
			}
			return;
		}
		onFinish(t);
	}

	/**
	 * Kills all other computations and notifies observers that it is done.
	 *
	 * @param t value to return
	 */
	private void onFinish(T t) {
		ai.set(0);
		executor.shutdownNow();
		future.complete(t);
	}

	/**
	 * Implementation of a task
	 */
	private class Task extends Observable implements Runnable {

		/**
		 * Callable that is called
		 */
		private Callable<T> callable;

		/**
		 * Create a new task with given callable, add Parallel as an observer
		 *
		 * @param callable callable
		 */
		Task(Callable<T> callable) {
			this.callable = callable;
			addObserver(Parallel.this);
		}

		@Override
		public void run() {
			try {
				T result = callable.call();
				setChanged();
				notifyObservers(result);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
	}

}
