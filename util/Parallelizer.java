package util;


import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * A Parallelizer is meant to simplify parallelizing a batch of independent tasks.
 *
 * A Parallelizer properly starts and shuts down an ExecutorService that executes task in parallel.
 * It also handles the CountDownLatch that is used to ensure that all jobs have completed.
 *
 * This class is provided so that parallelizing simple jobs can be done without using
 * java.util.concurrent classes or dealing with any of the annoying error catching code that is
 * required when moving from a single-threaded context to a multi-threaded context and then back to
 * a single-threaded context.
 *
 * The presumption is that a simple class that implements Runnable will be defined right before a
 * Parallelizer is constructed and used.
 */
public class Parallelizer {

	/** This executor will do the work in parallel. */
	ExecutorService service;

	/** The flag prevents the "one time use" Parallelizer from being used twice. */
	boolean isSpent = false;


	/**
	 * Start an ExecutorService that can process many jobs in parallel
	 *
	 * @param numThreads - The number of threads the service will use to process the jobs.
	 */
	public Parallelizer(int numThreads) {
		this.service = Executors.newFixedThreadPool(numThreads);
	}


	/**
	 * This method blocks until the "run()" method associated with each job is complete.
	 *
	 * @param jobs - A batch of work.
	 */
	public void doWorkInParallel(Runnable[] jobs) {

		if (this.isSpent) {
			throw new IllegalStateException("This Parallelizer is spent -- it cannot be reused");
		}

		CountDownLatch latch = new CountDownLatch(jobs.length);

		//wrap each job in a pair of Wrappers, 
		//(1) an AwaitableTask that trips the CountDownLatch
		//(2) an ErrorCatchingTask that ensure that all errors are publicized (because the ExecutorService will hide them)
		for (Runnable job : jobs) {

			if (job == null) {
				throw new IllegalArgumentException("Cannot submit null Runnables");
			}

			Runnable task = new ErrorCatchingTask(new AwaitableTask(latch, job));

			service.submit(task);
		}

		try {
			latch.await();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(0);
		}

		this.isSpent = true;

		service.shutdown();
	}


	/**
	 * This method blocks until the "run()" method associated with each job is complete.
	 *
	 * @param jobs - A batch of work.
	 */
	public void doWorkInParallel(Collection<? extends Runnable> c) {
		doWorkInParallel(c.toArray(new Runnable[0]));
	}


	/**
	 * @return - True if this Parallelizer has already been used to do work. A Parallelizer can only
	 * be used once. For cyclical jobs see java.util.concurrent.CyclicBarrier.
	 */
	public boolean isSpent() {
		return this.isSpent;
	}


	/**
	 * Shutdown the ExecutorService inside this Parallelizer (done automatically when
	 * doWorkInParallel completes).
	 */
	public void shutdown() {
		this.service.shutdown();
	}
}
