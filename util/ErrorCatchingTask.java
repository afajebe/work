package util;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;


/**
 * An ErrorCatchingTask is a Decorator that adds a try/catch block to a Runnable. The purpose
 * of this decorator is to prevent Executors from suppressing exceptions.
 */
public class ErrorCatchingTask implements Runnable {

	Runnable runMe;


	/** An ErrorCatchingTask is a Decorator that adds a try/catch block to a Runnable. */
	public ErrorCatchingTask(Runnable runMe) {

		if (runMe == null) {
			throw new IllegalArgumentException("Cannot be null");
		}

		this.runMe = runMe;
	}


	@Override
	public void run() {
		try {
			runMe.run();
		} catch (Exception | Error ex) {
			writeErrorMessageAndClose(ex);
		}
	}


	private void writeErrorMessageAndClose(Throwable ex) {
		try {
			ex.printStackTrace();
			String errorFile = "errorFile.txt";

			System.out.
					println("\n\n**** ERROR STACK TRACE WRITTEN TO :: " + errorFile + " ****\n\n");
			PrintWriter pw = new PrintWriter(new File(errorFile));
			ex.printStackTrace(pw);
			pw.flush();
		} catch (FileNotFoundException ex1) {
			System.out.println(ex1.getMessage());
		}
		System.exit(0);
	}
}
