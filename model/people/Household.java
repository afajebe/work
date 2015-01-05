package model.people;


/** A Household is a group of people that share a close quarters housing unit. */
public class Household {

	/** The ages of everyone in the household. */
	private HouseholdFactory.AgeCohort[] ages;


	/**
	 * Create a new Household
	 *
	 * @param ages - An AgeCohort for each household member
	 */
	public Household(HouseholdFactory.AgeCohort[] ages) {
		if (ages.length > 128) {
			throw new RuntimeException("Households must have size 128 or less");
		}

		this.ages = ages;
	}


	/** @return - The AgeCohort of each household resident. */
	public HouseholdFactory.AgeCohort[] getAgeArray() {
		return ages;
	}


	/** @return - The number of people who live in this household. */
	public int size() {
		return ages.length;
	}
}
