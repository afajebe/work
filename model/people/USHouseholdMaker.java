package model.people;


import cern.jet.random.engine.RandomEngine;
import model.people.HouseholdFactory.AgeCohort;

/**
 *  This class is an implementation of the HouseholdFactory interface.  This class
 *	produces houses that, as a distribution, exhibit propeties of the most recent US Census.
 */
public class USHouseholdMaker implements HouseholdFactory {
	
	private RandomEngine rand;
	
	/**  Create an object that will make households for you. */
	public USHouseholdMaker(RandomEngine rand) {
		this.rand = rand;
	}
	
	
	/**  Get a new random household. */
	public Household createHousehold() {
		AgeCohort[] ages = new AgeCohort[drawHouseholdSize()];
		
		//head of household
		ages[0] = HouseholdFactory.AgeCohort.ADULT_COHORT;
		
		if(ages.length > 1) {
			for(int i = 1 ; i < ages.length ; i++) {
				ages[i] = getRandomDependentCohort();
			}
		}
		
		return new Household(ages);
	}
	
	
	/**  Get a random household size. */
	private int drawHouseholdSize() {
		double tempDouble = rand.nextDouble();
		
		if(tempDouble < .26) return 1;  //26%
		if(tempDouble < .59) return 2;  //33%
		if(tempDouble < .75) return 3;  //16%
		if(tempDouble < .90) return 4;  //15%
		if(tempDouble < .97) return 5;  //7%
		if(tempDouble < .99) return 6;  //2%
		return 7;			//1%
	}
	
	
	/**  Get the age of a dependant (non Head of Household). */
	private AgeCohort getRandomDependentCohort() {
		
		double tempDouble = rand.nextDouble();
		
		//25.6% of the population is under 18
		//74.4% of the population is 18 or over
		//281.4M = total population
		
		//expected value of people in house = 2.42
		//leaves 1.42 expected value of people that can be kids
		//43.6% of 1.42 = .62 = 25.6% of 2.42
		
		if(tempDouble < .436) {
			return HouseholdFactory.AgeCohort.CHILD_COHORT;
		} else {
			return HouseholdFactory.AgeCohort.ADULT_COHORT;
		}
	}
}