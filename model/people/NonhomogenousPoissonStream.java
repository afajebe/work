package model.people;


import cern.jet.random.engine.RandomEngine;
import com.google.common.primitives.Doubles;
import java.util.Arrays;
import time.LengthOfTime;
import time.ModelTimeUnit;
import time.TimeStamp;



/** A NonhomogenousPoissonStream generates events at a rate that varies as the day progresses. */
public class NonhomogenousPoissonStream implements BehaviorStream {

	/** This array provides one days worth of probabilities. */
	private final double[] probEventPerUnitTime;


	/**
	 * Create a NonhomogenousPoissonStream that will generate events at a rate that varies as the
	 * day progresses.
	 *
	 * @param oneProbPerTimeStep - This array must have a length equal to the number of time steps
	 * per simulated day. It must also contain valid probability values. Not All probability entries
	 * can be 0.
	 */
	public NonhomogenousPoissonStream(double[] oneProbPerTimeStep) {

		if (oneProbPerTimeStep.length != LengthOfTime.TIME_STEPS_PER_DAY) {
			throw new IllegalArgumentException(
					"The input array of of probabilites must contain one probability value "
					+ "for each time step in a day");
		}

		if (Doubles.max(oneProbPerTimeStep) > 1.0) {
			throw new IllegalArgumentException(
					"The input array of probabilities contains a value greater than 1.0");
		}

		if (Doubles.min(oneProbPerTimeStep) < 0.0) {
			throw new IllegalArgumentException(
					"The input array of probabilities contains a value less than 0.0");
		}

		if (Doubles.max(oneProbPerTimeStep) < 0.0) {
			throw new IllegalArgumentException(
					"The input array of probabilities must contain "
					+ "at least one value greater than 0.0");
		}

		this.probEventPerUnitTime = Arrays.copyOf(
				oneProbPerTimeStep,
				oneProbPerTimeStep.length);
	}


	/** @return - The time at which the next event of this type will occur. */
	@Override
	public TimeStamp timeOfNextEvent(TimeStamp currentTime, RandomEngine rand) {

		int index = currentTime.asIndexIntoOneDay();

		double numTimeSteps = 1;
		while (rand.nextDouble() < probEventPerUnitTime[index]) {

			numTimeSteps++;
			index++;

			index %= LengthOfTime.TIME_STEPS_PER_DAY;
		}

		LengthOfTime delay = new LengthOfTime(
				numTimeSteps / (double) LengthOfTime.TIME_STEPS_PER_DAY,
				ModelTimeUnit.DAYS);

		return currentTime.add(delay);
	}


	@Override
	public double expectedNumEventsPerDay() {

		double expectedNumEvents = 0;
		for (double prob : probEventPerUnitTime) {
			expectedNumEvents += prob;
		}

		return expectedNumEvents;
	}
}
