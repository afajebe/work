package model.people;


import cern.jet.random.engine.RandomEngine;
import time.TimeStamp;


public interface BehaviorStream {

	/**
	 * @return - The next time an event of this type occurs. For example, if FamilyContactStream
	 * implements BehaviorStream it will tell you when the next family contact should occur, if
	 * RandomContactStream implements BehaviorStream it will well you when the next random contact
	 * should occur.
	 */
	public TimeStamp timeOfNextEvent(TimeStamp currentTime, RandomEngine rand);


	/** @return - The expected number of these events per day. */
	public double expectedNumEventsPerDay();
}
