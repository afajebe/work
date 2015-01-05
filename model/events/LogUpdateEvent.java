package model.events;


import core.InteractiveLargeScaleModel;
import core.ModelPlace;
import time.LengthOfTime;
import time.TimeStamp;


/**
 * When a LogUpdateEvent is called it spurns the harvest of data from it's ModelPlace and
 * the storage of that data in a LogEntryNode. It then rescedules itself to make
 * another LogEntryNode at a later timeOfEvent.
 */
public class LogUpdateEvent extends SimulationEvent {

	/** The frequency at which Log Entries are recorded. */
	private final static LengthOfTime frequency;


	static {
		frequency = InteractiveLargeScaleModel.instance.simProps().logUpdateFreq();
	}


	/** Get how frequently Log Entries are generated. */
	public static LengthOfTime getFrequency() {
		return frequency;
	}


	/**
	 * Create a LogUpdateEvent that always adds itself after it's been processed.
	 */
	public LogUpdateEvent(TimeStamp time, ModelPlace place) {
		super(time, place);
	}


	/** Return a String that describes this event. */
	@Override
	public String toString() {
		return "LogUpdate event for place " + place.placeIDNum() + " at time :: " + timeOfEvent;
	}


	/**
	 * Prompt this event's ModelPlace to generate a log entry, Add "this" back to the
	 * appropriate queue.
	 */
	@Override
	public void implementEvent() {
		place.logCurrentState();			//tell the ModelPlace to record its current state
		this.setTime(timeOfEvent.add(frequency));
		(place.thread()).addEvent(this);	//ensure that future log entries are generated
	}
}