package model.people;


import cern.jet.random.engine.RandomEngine;
import com.google.common.math.DoubleMath;
import java.math.RoundingMode;
import time.LengthOfTime;
import time.ModelTimeUnit;
import time.TimeStamp;


/**
 * A PoissonStream generates the requested number of events per day in expectation. The
 * probability an event occurs per unit time does not vary. Thus, the events this series
 * of probabilities generates are part of a typical Poisson process in which the time between
 * events is roughly exponentially distributed when the time step is small and geometrically
 * distributed when the time step is not small. (Note the geometric distribution approaches the
 * exponential distribution as the time step goes to zero)
 */
public class PoissonStream implements BehaviorStream {

	private final double probEventPerUnitTime;


	public PoissonStream(double numExpectedEventsPerDay) {
		
		if (numExpectedEventsPerDay <= 0) {
			throw new IllegalStateException("Cannot build a stream that fires exactly zero events");
		}

		this.probEventPerUnitTime = numExpectedEventsPerDay / (1.0 * LengthOfTime.TIME_STEPS_PER_DAY);

		if (probEventPerUnitTime > 1.0) {
			throw new IllegalStateException(
					"Cannot support " + numExpectedEventsPerDay
					+ " events per day because the probability per unit time must exceed 1");
		}

	}


	/** @return - The time at which the next event of this type will occur. */
	@Override
	public TimeStamp timeOfNextEvent(TimeStamp currentTime, RandomEngine rand) {

		int numTimeSteps = computeNumTimeStepsRequired(rand.nextDouble());

		LengthOfTime delay = new LengthOfTime(
				1.0 * numTimeSteps / (double) LengthOfTime.TIME_STEPS_PER_DAY,
				ModelTimeUnit.DAYS);

		return currentTime.add(delay);
	}


	@Override
	public double expectedNumEventsPerDay() {
		return probEventPerUnitTime * LengthOfTime.TIME_STEPS_PER_DAY;
	}


	/**
	 * A PoissonStream implements a Geometric Random Variable where p = probEventPerUnitTime. We
	 * want to know how many random draws are required until an event occurs. Therefore, we use the
	 * cumulative distribution of a geometric random variable "CDF = 1 - (1-p)^k". We set the CDF in
	 * the equation equal to the random draw, then solve for k.
	 *
	 * @return - The number of time step (i.e. random draws) required for an event to occur.
	 */
	private int computeNumTimeStepsRequired(double randDraw) {

		double k = Math.log(randDraw) / Math.log(1.0 - probEventPerUnitTime);

		return DoubleMath.roundToInt(k, RoundingMode.UP);
	}
}
