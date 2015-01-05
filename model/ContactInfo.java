package model;


import model.disease.DiseaseState;
import model.events.ContactType;
import model.people.HouseholdFactory;
import model.people.HouseholdFactory.AgeCohort;


/**
 * A wrapper for all of the information required to properly implementing a contact. This wrapper
 * may need to be edited to support different diseases.
 *
 * For instance, if you need to know which stage of disease the "contactor" was in as well as how
 * intimate the contact is this object might store a "DiseaseStatus" and "IntimacyLevel" variables
 */
public class ContactInfo {

	/** The AgeCohort of the contactor. */
	public final AgeCohort age;

	/** The DiseaseState of the contactor. */
	public final DiseaseState diseaseState;

	/** The type of contact to implement. */
	public final ContactType contactType;


	/**
	 * Creates a new instance of ContactInfo
	 *
	 * @param age - The AgeCohort of the contactor
	 * @param diseaseState - The DiseaseState of the contactor
	 * @param contactType - The type of contact being implemented
	 */
	public ContactInfo(HouseholdFactory.AgeCohort age, DiseaseState diseaseState, ContactType contactType) {
		this.age = age;
		this.diseaseState = diseaseState;
		this.contactType = contactType;
	}
}
