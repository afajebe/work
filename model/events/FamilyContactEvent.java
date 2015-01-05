package model.events;


import core.ModelPlace;
import model.people.BehaviorStreams;
import time.TimeStamp;


/**
 * A few static methods that help implement Family Contact Events.  This class provides no 
 * constructors to prevent wasted memory.
 */
public final class FamilyContactEvent {

	/** 
	 * Calculate and then return the time of the next Family Contact Event. 
	 * 
	 * @param place - The ModelPlace that will provide a random number generator
	 * @param minTime - The earliest possible next occurance of a Family Contact Event
	 * 
	 * @return - The time of the next Family Contact Event
	 */
	public static TimeStamp setNextTime(ModelPlace place, TimeStamp minTime) {
		//calculate time of the next family Contact
		return BehaviorStreams.familyContactStream.timeOfNextEvent(minTime, place.getRand());
	}


	/** 
	 * Implement a Family contact, reschedule the next FamilyContactEvent. 
	 * 
	 * @param place - The ModelPlace where a Family Contact will occur
	 * @param personIndex - The "hero" in a family contact
	 * @param currentTime - The time this event should occur
	 * 
	 * @return - The time of the next event 
	 */
	public static TimeStamp implementEvent(ModelPlace place, int personIndex, TimeStamp currentTime) {

		//execute a Family Contact
		place.locals().makeRandomFamilyContact(personIndex);
		
		//compute time of next Family Contact Event
		return setNextTime(place , currentTime);
	}
}
