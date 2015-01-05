package model.events;


import core.ModelPlace;
import model.people.HouseholdFactory.AgeCohort;
import model.people.BehaviorStreams;
import time.TimeStamp;


/**
 * A few static methods that help implement Random Contact Events.  This class provides no 
 * constructors to prevent wasted memory.
 */
public final class RandomContactEvent {

	/**
	 * Calculate and then return the time of the next Random Contact Event.
	 *
	 * @param place - The ModelPlace that will provide a random number generator
	 * @param minTime - The earliest possible next occurance of a Random Contact Event
	 *
	 * @return - The time of the next Random Contact Event
	 */
	public static TimeStamp setNextTime(ModelPlace place, TimeStamp minTime) {
		//calculate time of the next random contact
		return BehaviorStreams.randomContactStream.timeOfNextEvent(minTime, place.getRand());
	}


	/**
	 * Implement a Random contact, reschedule the next Random Contact Event.
	 *
	 * @param place - The home ModelPlace for the "hero" in the Repeat Contact
	 * @param personIndex - The "hero" in a Random contact
	 * @param currentTime - The time this event should occur
	 *
	 * @return - The time of the next Random Contact Event
	 */
	public static TimeStamp implementEvent(ModelPlace place, int personIndex, TimeStamp currentTime) {

		//schools are closed
		if (AgentEvent.SCHOOLS_CLOSED) {
			//and you are traking a child
			if (place.locals().getAgeCohort(personIndex) == AgeCohort.CHILD_COHORT) {

				//return the time of the next event w/o making a contact
				return setNextTime(place, currentTime);
			}
		}

		//apply current social distancing rate
		if (place.getRand().nextDouble() < AgentEvent.SOCIAL_DISTANCE) {

			//execute a Random Contact
			place.locals().makeRandomContact(personIndex);
		}

		//compute time of next Random Contact Event
		return setNextTime(place, currentTime);
	}
}
