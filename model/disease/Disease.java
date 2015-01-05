package model.disease;


import time.LengthOfTime;
import time.TimeStamp;


public interface Disease {

	/**
	 * @param randDraw - A uniformly distributed random number between 0 and 1
	 *
	 * @return - The LengthOfTime an agent is contagious.
	 */
	LengthOfTime getContagiousPeriod(double randDraw);


	/**
	 * Return the time this agent will become contagious
	 *
	 * @param timeOfExposure - The time this agent is exposed
	 * @param randDraw - A uniformly distributed random number between 0 and 1
	 *
	 * @return - Return the time this agent will become contagious (see also - latent period)
	 */
	TimeStamp getTimeContagious(TimeStamp timeOfExposure, double randDraw);


	/**
	 * Return the time this agent will become contagious
	 *
	 * @param timeOfExposure - The time this agent is exposed
	 * @param randDraw - A uniformly distributed random number between 0 and 1
	 *
	 * @return - Return the time this agent will become contagious. Returns "-1" if symptoms
	 * are never shown.
	 */
	TimeStamp getTimeSymptomatic(TimeStamp timeOfExposure, double randDraw);
	
	
	double baseInfectiousness();
}
