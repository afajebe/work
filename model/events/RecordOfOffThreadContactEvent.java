package model.events;


import model.ContactInfo;
import core.ModelPlace;
import time.TimeStamp;


/**
 * A RecordOfOffThreadContactEvent is a record of an contact event that could not occur because the
 * contact needed to occur on a different thread.
 */
public class RecordOfOffThreadContactEvent {

	/**
	 * The time the contact should have occurred but didn't because the event must occur on a
	 * different thread.
	 */
	private final TimeStamp time;

	/** The index of the ModelPlace where the event must occur. */
	private final int placeIndex;

	/** The index of the person that should be "contacted". */
	private final int indexOfPersonToContact;

	/** Information required to perform the contact. */
	private final ContactInfo info;


	/**
	 * Create a record of an contact event that could not occur because the contact needed to occur
	 * on a different thread
	 *
	 * @param time - The time the contact should have occurred but didn't because the event must
	 * occur on a
	 * different thread.
	 * @param placeIndex - The index of the ModelPlace where the event must occur.
	 * @param indexOfPersonToContact - The index of the person that should be "contacted"
	 * @param info - Information required to perform the contact.
	 */
	public RecordOfOffThreadContactEvent(TimeStamp time, int placeIndex, int indexOfPersonToContact, ContactInfo info) {
		this.time = time;
		this.placeIndex = placeIndex;
		this.indexOfPersonToContact = indexOfPersonToContact;
		this.info = info;
	}


	public int placeIndex() {
		return placeIndex;
	}


	/**
	 * @param place - The ModelPlace where the event must occur
	 *
	 * @return - The actual OffThreadContactEvent that must be executed.
	 */
	public OffThreadContactEvent makeEvent(ModelPlace place) {

		if (place.placeIDNum() != placeIndex) {
			throw new IllegalStateException("Place idNumber mismatch");
		}

		return new OffThreadContactEvent(time, place, indexOfPersonToContact, info);
	}
}
