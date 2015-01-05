package model.events;


import core.ModelPlace;
import model.disease.DiseaseTimeLine;
import time.TimeStamp;
import time.TimeStamps;


/**
 * An AgentEvent keeps track of all the events that might effect a single agent. Currently, this
 * list includes: (1) FamilyContactEvents, (2) RandomContactEvents, and (3) DiseaseProgressEvents
 *
 * An AgentEvent is stored in a WorkerThread's PriorityQueue. The priority of this event is
 * determined by the minimum timeOfEvent that each of the above events (family,random,disease) is
 * due to
 * occur. When this object is popped and executed one of the 3 event types will be executed. This
 * object will then create the next event of that type (if appropriate) and insert itself back into
 * the queue (if appropriate).
 *
 * Creating only ONE AgentEvent object per infected agent allows the global model to:
 * (1) Have a shorter, faster, more efficient PriorityQueue that runs the model. (The faster this PQ
 * is the faster we can push events to and pop events from the PQ)
 * (2) Have a MUCH smaller memory footprint because we don't create several extra objects to
 * store a few primitive variables (fewer "entry" objects (mini class in Collections), fewer
 * "SimulationEvent" objects).
 */
public class AgentEvent extends SimulationEvent {

	public static boolean SCHOOLS_CLOSED = false;

	public static double SOCIAL_DISTANCE = 1.0;

	public final int personIndex;

	protected DiseaseTimeLine diseaseTimeLine;

	protected TimeStamp diseaseEventTime;

	protected TimeStamp familyEventTime;

	protected TimeStamp randomEventTime;

	protected TimeStamp repeatableEventTime;


	/**
	 * Create an object the manages and agents behavior
	 *
	 * @param timeOfInfection - The timeOfEvent this agent is exposed to disease
	 * @param place - The ModelPlace that owns this agent
	 * @param personIndex - The index of the agent that this object manages
	 * @param hasFamily - Should this object create family contacts?
	 */
	public AgentEvent(TimeStamp timeOfExposure, ModelPlace place, int personIndex, boolean hasFamily) {
		super(null, place);
		this.personIndex = personIndex;

		computeEventTimes(timeOfExposure, hasFamily);
	}


	private void computeEventTimes(TimeStamp timeOfExposure, boolean hasFamily) {

		this.diseaseTimeLine = new DiseaseTimeLine(timeOfExposure, place.getRand());

		this.diseaseEventTime = diseaseTimeLine.curTime();
		
		this.setTime(diseaseEventTime);

		//create the FamilyContactEvent if required
		if (hasFamily) {
			familyEventTime = FamilyContactEvent.setNextTime(place, diseaseTimeLine.infectiousTime());
		} else {
			familyEventTime = TimeStamps.neverOccuringTime();
		}

		//schedule the RandomContactEvent
		this.randomEventTime = RandomContactEvent.setNextTime(
				place,
				diseaseTimeLine.infectiousTime());

		//schedule the Repeatable Contacts
		this.repeatableEventTime = RepeatableContactEvent.setNextTime(
				place,
				diseaseTimeLine.infectiousTime());
	}


	/**
	 * Recompute event times because the infection timeOfEvent was slightly earlier than initially
	 * thought.
	 */
	public void recomputeEventTimes(TimeStamp updatedTimeOfExposure) {
		computeEventTimes(
				updatedTimeOfExposure,
				familyEventTime.occurs());
	}


	/** Return the time this agent was 1st exposed to disease. */
	public TimeStamp getTimeOfExposure() {
		return diseaseTimeLine.timeOfExposure();
	}


	public DiseaseTimeLine diseaseHistory() {
		return this.diseaseTimeLine;
	}


	/**
	 * Basically a switch statement that implements the correct event and then adds itself
	 * back to the PriorityQueue if appropiate. If 2 events have the same timeOfEvent then only
	 * 1 event is actually executed. In the event of a timeOfEvent tie the events are executed in
	 * this order :: DiseaseProgressEvent, RandomContactEvent, FamilyContactEvent.
	 */
	@Override
	public void implementEvent() {

		implementProperEvent();

		//add this object back to the queue if this agent hasn't recovered
		if (diseaseEventTime.occurs()) {

			timeOfEvent = TimeStamps.firstOf(
					diseaseEventTime,
					randomEventTime,
					repeatableEventTime,
					familyEventTime);

			(place.thread()).addEvent(this);
		} else {
//			System.out.println("Agent :: " + this.place.placeIDNum() + "-" + this.personIndex + " is done");
		}
	}


	/**
	 * Return the SimulationEvent that this AgentEvent will implement when implementEvent
	 * is called on this object.
	 */
	private void implementProperEvent() {

		if (timeOfEvent == diseaseEventTime) {
			
			DiseaseProgressEvent event = diseaseTimeLine.popDiseaseProgressEvent();
			event.implementEvent(place, personIndex, this);
			
			diseaseEventTime = diseaseTimeLine.curTime();

		} else if (timeOfEvent == randomEventTime) {

			randomEventTime = RandomContactEvent.implementEvent(
					place,
					personIndex,
					randomEventTime);

		} else if (timeOfEvent == familyEventTime) {

			familyEventTime = FamilyContactEvent.implementEvent(
					place,
					personIndex,
					familyEventTime);

		} else if (timeOfEvent == repeatableEventTime) {

			repeatableEventTime = RepeatableContactEvent.implementEvent(
					place,
					personIndex,
					repeatableEventTime);
		} else {
			throw new IllegalStateException("time was an invalid value, object malformed");
		}
	}


	/** Toggle off Random Contacts for children. */
	public static void closeSchools() {
		SCHOOLS_CLOSED = true;
	}


	/** Toggle On Random Contacts for children. */
	public static void openSchools() {
		SCHOOLS_CLOSED = false;
	}


	public static void setSocialDistance(double fraction) {
		SOCIAL_DISTANCE = 1.0 - fraction;
	}
}