package model.disease;


import java.util.Objects;
import time.LengthOfTime;
import time.ModelTimeUnit;
import time.TimeStamp;
import time.TimeStamps;


public class SwineFlu implements Disease {

	private final static double SHOW_SYMPTOMS_PROB = .67;

	private final static double BASE_INFECTIOUSNESS = 0.0255;


	/**
	 * @param randDraw - A uniformly distributed random number between 0 and 1
	 *
	 * @return - The number of hours an agent is contagious.
	 */
	@Override
	public LengthOfTime getContagiousPeriod(double randDraw) {
		//30% -- 1 Day
		//40% -- 2 Days
		//20% -- 3 Days
		//10% -- 4 Days
		//average - 2.1 Days
		final double hours;
		if (randDraw < .3) {
			hours = 24;
		} else if (randDraw < (.3 + .4)) {
			hours = 48;
		} else if (randDraw < (.3 + .4 + .2)) {
			hours = 72;
		} else {
			hours = 96;
		}

		return new LengthOfTime(hours, ModelTimeUnit.HOURS);
	}


	/**
	 * Return the time this agent will become contagious
	 *
	 * @param timeOfExposure - The time this agent is exposed
	 * @param randDraw - A uniformly distributed random number between 0 and 1
	 *
	 * @return - Return the time this agent will become contagious (see also - latent period)
	 */
	@Override
	public TimeStamp getTimeContagious(TimeStamp timeOfExposure, double randDraw) {
		
		Objects.requireNonNull(timeOfExposure);

		double hours;
		//30% -- 1 Day
		//50% -- 2 Days
		//20% -- 3 Days
		//average - 1.9 Days
		if (randDraw < .3) {
			hours = 24;
		} else if (randDraw < (.3 + .5)) {
			hours = 48;
		} else {
			hours = 72;
		}

		return timeOfExposure.add(new LengthOfTime(hours, ModelTimeUnit.HOURS));
	}


	/**
	 * Return the time this agent will become contagious
	 *
	 * @param timeOfExposure - The time this agent is exposed
	 * @param randDraw - A uniformly distributed random number between 0 and 1
	 *
	 * @return - Return the time this agent will become contagious. Returns "-1" if symptoms
	 * are never shown.
	 */
	@Override
	public TimeStamp getTimeSymptomatic(TimeStamp timeOfExposure, double randDraw) {

		Objects.requireNonNull(timeOfExposure);

		if (randDraw < SHOW_SYMPTOMS_PROB) {
			double tempRand = (SHOW_SYMPTOMS_PROB - randDraw) / SHOW_SYMPTOMS_PROB;

			double hours;
			//30% -- 1 Day
			//50% -- 2 Days
			//20% -- 3 Days
			//average - 1.9 days
			if (tempRand < .3) {
				hours = 24;
			} else if (tempRand < (.3 + .5)) {
				hours = 48;
			} else {
				hours = 72;
			}

			return timeOfExposure.add(new LengthOfTime(hours, ModelTimeUnit.HOURS));

		} else {
			return TimeStamps.neverOccuringTime();
		}
	}


	@Override
	public double baseInfectiousness() {
		return BASE_INFECTIOUSNESS;
	}
}
