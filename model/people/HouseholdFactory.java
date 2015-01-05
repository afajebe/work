package model.people;


/**
 *  HouseholdFactory is an interface that supplies all the methods required
 *	to create Households that exhibt properties of a specific demographic dist.
 */
public interface HouseholdFactory {
	
	public static final AgeCohort[] ages = AgeCohort.values();

	/**
	 *  Every person in a household is a member of one of these AgeChorts.
	 */
	public enum AgeCohort {

		CHILD_COHORT, ADULT_COHORT
	}

	/**
	 *  Return a new Household that exhibit a given demographic distribution.
	 */
	public Household createHousehold();
}