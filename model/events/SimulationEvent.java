package model.events;


import core.ModelPlace;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.Serializable;
import time.TimeStamp;


/**
 * A SimulationEvent is designed to be stored in a PriorityQueue.
 *
 * SimulationEvents are associated with a timeOfEvent, thus they can be executed in chronological order.
 * Ties are broken by a unique ID that is obtained from the event's executing thread.
 */
public abstract class SimulationEvent implements Comparable<SimulationEvent>, Serializable {

	/** When this event should be executed. */
	protected TimeStamp timeOfEvent;

	/** This place that should implement this event. */
	protected final ModelPlace place;

	/**
	 * This variable is retrieved from the thread that owns this objects "place". The
	 * purpose of this variable is to provide a guaranteed exact ordering of all Events
	 * (by breaking ties between events with the same timeOfEvent)
	 */
	protected int myId;


	/**
	 * Create a SimulationEvent.
	 *
	 * @param time - The time that this event should occur
	 * @param place - The ModelPlace where this event occurs
	 */
	public SimulationEvent(TimeStamp time, ModelPlace place) {
		this.timeOfEvent = time;
		this.place = place;
		if (place != null) {
			this.myId = place.thread().generateUniqueEventID();
		}
	}


	/** Implement this event. */
	public abstract void implementEvent();


	/** @return - The time this event should be implemented. */
	public TimeStamp getTime() {
		return timeOfEvent;
	}


	/** Set the time this event should be implemented. */
	public void setTime(TimeStamp newTime) {
		this.timeOfEvent = newTime;
	}


	/** @return - The place where this event occurs. */
	public ModelPlace getPlace() {
		return place;
	}


	/** This Comparison is based on the times each of the 2 events should occur. */
	@Override
	public int compareTo(SimulationEvent otherEvent) {

		SimulationEvent other = (SimulationEvent) otherEvent;

		//this object is equal to itself
		if (this == other) {
			return 0;
		}
		
		if(this.timeOfEvent.occursAfter(otherEvent.timeOfEvent)) {
			return 1;
		} else if (this.timeOfEvent.occursBefore(otherEvent.timeOfEvent)) {
			return -1;
		} else {

			//if timeOfEvent is the same sort via zipcode
			if (this.place.placeIDNum() > other.place.placeIDNum()) {
				return 1;
			} else if (this.place.placeIDNum() < other.place.placeIDNum()) {
				return -1;
			} else {
				//if timeOfEvent and zipcode are the same sort via id

				if (this.myId > other.myId) {
					return 1;
				} else if (this.myId < other.myId) {
					return -1;
				} else {
					IllegalStateException e = null;
					try {
						PrintStream stream = new PrintStream("error1.txt");
						stream.println(this.getClass());
						stream.println(other.getClass());
						e = new IllegalStateException("Equal but not");
						e.printStackTrace(stream);
					} catch (FileNotFoundException ex) {
						ex.printStackTrace();
					}
					System.exit(0);
				}
			}
		}
		throw new IllegalStateException("Should never get here");

	}
}