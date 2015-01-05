package model.events;


import model.ContactInfo;
import core.ModelPlace;
import model.people.HouseholdFactory;
import time.TimeStamp;


/**
 * An OffNodeContactEvent is a record of an contact between 2 agents. This contact
 * can not be instantly executed because one of the agents involved in the contact
 * resides on a different GlobalModeNode (aka machine or JVM).<p>
 *
 * Before a ContactEvent can be implemented it must be given a reference to its ModelPlace.
 * It can't have access to its ModelPlace at creation because that ModelPlace is off-node.
 */
public class OffThreadContactEvent extends SimulationEvent {

	/** The index of the person that should be "contacted". */
	public final int personToContact;

	/** An object that describes the "contactor". */
	public final ContactInfo info;


	/**
	 * Create an OffNodeContactEvent.
	 *
	 * @param time - The time that this event should occur
	 * @param place - The index of the ModelPlace where this event occurs.
	 * @param personToContact - The index of the person to contact (if negative contact a random
	 * person)
	 * @param info - A collection of information that is required to implement a contact
	 */
	OffThreadContactEvent(TimeStamp time, ModelPlace place, int personToContact, ContactInfo info) {
		super(time, place);

		//probably Integer.MIN_VALUE
		this.personToContact = personToContact;
		if (personToContact > 0) {
			throw new IllegalArgumentException("Cannot create an OffNodeContactEvent with a positive index");
			//the above exception will be invalid once we implement freqent contacts that live in different zipcodes
		}

		this.info = info;
	}


	/** Return the index of the contactor in this ContactEvent. */
	public int getIndex() {
		return personToContact;
	}


	/** Return the ContactType of this contact. */
	public ContactType getType() {
		return info.contactType;
	}


	/** Return the age of the Contactor. */
	public HouseholdFactory.AgeCohort getAgeOfContactor() {
		return info.age;
	}


	/** Return a String that describes this event. */
	@Override
	public String toString() {
		return "ONCE :: " + (info.contactType.toString() + " at time :: " + timeOfEvent
				+ " for agent " + personToContact + " in zip :: " + place.placeIDNum());
	}


	/** Implement a contact that originated on a different GlobalModelNode. */
	@Override
	public void implementEvent() {

		//currently the only option because FAMILY_CONTACTS won't become ContactEvents.
		//FAMILY_CONTACTS will stay inside AgentEvents.
		if (info.contactType == ContactType.RANDOM_CONTACT) {
			place.locals().contact(personToContact, info);
		} else {
			throw new RuntimeException("currently OffNodeContactEvents should be RANDOM_CONTACTS");
		}
	}	
}