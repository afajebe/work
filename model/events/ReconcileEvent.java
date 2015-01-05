package model.events;


import core.InteractiveLargeScaleModel;
import core.ModelPlace;
import time.LengthOfTime;
import time.TimeStamp;


/**
 * A ReconcileEvent is merely a "marker" event that a WorkerThread can watch for. When a
 * ReconcileEvent bubbles to the top of the PriorityQueue the WorkerThread can then inform
 * the parent node that it is finished processing
 */
public class ReconcileEvent extends SimulationEvent {

	/** The frequency at which ReconcileEvent occur. */
	private static LengthOfTime frequency;


	static {
		frequency = InteractiveLargeScaleModel.instance.simProps().reconcileEventFreq();
	}


	/** Set how often you want the WorkerThreads paused to reconcile off-node contacts. */
	public static LengthOfTime getFrequency() {
		return frequency;
	}


	/**
	 * Create a ReconcileEvent.
	 *
	 * @param timeOfEvent - The timeOfEvent that this event should occur
	 * @param place - This should be any ModelPlace that is owned by
	 * the GlobalModelNode that you want paused when this event is implemented.
	 */
	public ReconcileEvent(TimeStamp time, ModelPlace place) {
		super(time, place);
	}


	/** @return - A String that describes this event. */
	@Override
	public String toString() {
		return ("ReconcileEvent at time " + timeOfEvent);
	}


	/**
	 * Pause the WorkerThread that implements this event. Gives the parent GlobalModel
	 * timeOfEvent to request the queued OffNodeContactEvents.
	 */
	@Override
	public void implementEvent() {
	}
}
