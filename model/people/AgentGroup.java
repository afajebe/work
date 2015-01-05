package model.people;


import cern.jet.random.engine.RandomEngine;
import model.ContactInfo;
import core.ModelPlace;
import model.disease.DiseaseState;
import model.disease.Diseases;
import model.events.AgentEvent;
import model.events.ContactType;
import model.events.SimulationEvent;
import model.people.HouseholdFactory.AgeCohort;
import time.TimeStamp;
import util.AgentEventHashTable;


/**
 * This class compactly stores a group of agents. Typically, these instantiations of this class
 * will represent the "locals" from any given area.
 */
public class AgentGroup {

	/**
	 * AgentEvent references are stored in a HashTable. This variable sets the size of this
	 * HashTable as a fraction of the population.
	 */
	private static double HASH_TABLE_FRACTION = .95;

	/** The ModelPlace where these agents live. */
	ModelPlace home;

	/** The number of agents in this group. */
	int numAgents;

	/*
	 * Variables of the people 
	 * - Each of these arrays will be of length "numAgents" 
	 * - An entire family is stored sequentially.  For instance, a family of 5 will be stored 
	 * at indices i, i+1, i+2, i+3 , i+4 (for some i)
	 */
	/** Number of agents to the left of this one that are member of its family. */
	byte[] leftFamily;

	/**
	 * This variable lists each local agents age.
	 * The value listed here is the index you would use in a call to
	 * "HouseholdFactory.AgeCohort.values()" (which returns an array)
	 */
	byte[] age;

	/**
	 * This variable lists each local agents DiseaseState. *
	 * The value listed here is the index you would use in a call to
	 * "Disease.DiseaseState.values()" (which returns an array)
	 */
	byte[] sickStatus;

	/** This table holds a reference to each "active" agents AgentEvent object. */
	AgentEventHashTable scheduleTable;

	/** The current number of Susceptible Agents in this group. */
	public int numSusceptible;

	/** The current number of NonSymptomatic Agents in this group. */
	public int numAssymptomatic;

	/** The current number of Symptomatic Agents in this group. */
	public int numSymptomatic;

	/** The current number of Contagious Agents in this group. */
	public int numContagious;

	/** The current number of NonContagious Agents in this group. */
	public int numNonContagious;

	/** The number of Recovered Agents in this group. */
	public int numRecovered;

	/** The number of agents newly infected since last datapoint. */
	public int newInfections;

	/** This variable prevents the "vaccinate" method from being called multiples times. */
	private boolean hasBeenVaccinated;


	/**
	 * Build a group of Agents
	 *
	 * @param numAgents - The number of people in this group.
	 * @param home - The ModelPlace that these agents consider "home".
	 * @param hhFactory - The object responsible for building the households inside this group.
	 */
	public AgentGroup(int numAgents, ModelPlace home, HouseholdFactory hhFactory) {

		if (numAgents <= 10) {
			throw new IllegalArgumentException("This AgentGroup is curiously small " + numAgents);
		}

		//initalize variables
		this.numAgents = numAgents;
		this.leftFamily = new byte[numAgents];
		this.age = new byte[numAgents];
		this.sickStatus = new byte[numAgents];
		this.scheduleTable = new AgentEventHashTable(numAgents, HASH_TABLE_FRACTION);
		this.home = home;
		this.hasBeenVaccinated = false;


		//variable needed in the loop below
		int agentsCreatedSoFar = 0;
		int householdSize = 0;
		Household newHousehold;
		HouseholdFactory.AgeCohort[] hhAges;

		//create the population
		do {
			//draw a new household from the house hold factory
			newHousehold = hhFactory.createHousehold();
			householdSize = newHousehold.size();
			hhAges = newHousehold.getAgeArray();

			//if this household would put the population over the edge - redraw
			while (householdSize + agentsCreatedSoFar > numAgents) {

				//propose a new household
				newHousehold = hhFactory.createHousehold();
				householdSize = newHousehold.size();
				hhAges = newHousehold.getAgeArray();
			}

			//let each person know who thier family is
			for (int i = 0; i < householdSize; i++) {
				leftFamily[agentsCreatedSoFar + i] = (byte) i;
				age[agentsCreatedSoFar + i] = (byte) hhAges[i].ordinal();
				sickStatus[agentsCreatedSoFar + i] = (byte) DiseaseState.SUSCEPTIBLE.
						ordinal();
			}

			agentsCreatedSoFar += householdSize;
		} while (agentsCreatedSoFar < numAgents);


		//at this point we have generated a population in which
		//
		//  Every Person has ::
		//	-An age chort
		//	-Links to household members
		//	-A susceptible DiseaseState

		numSusceptible = numAgents;
		numAssymptomatic = 0;
		numSymptomatic = 0;
		numContagious = 0;
		numNonContagious = 0;
		numRecovered = 0;
	}


	/** @return - The number of people in this group of agents. */
	public int numAgents() {
		return this.numAgents;
	}


	/**
	 * Set the Status of a given individual
	 *
	 * @param index - The index of a person in this group.
	 * @param newStatus - The status this agent will be "promoted" to
	 */
	public void setDiseaseState(int index, DiseaseState newStatus) {
		
		DiseaseState oldStatus = DiseaseState.values()[sickStatus[index]];
		
		if(oldStatus == null) {
			throw new IllegalStateException("The oldStatus cannot be null :: " + oldStatus);
		}

		if (oldStatus == DiseaseState.RECOVERED || oldStatus == DiseaseState.DEAD) {
			throw new IllegalStateException(
					"A " + oldStatus.toString() + " should not receive a new DiseaseState");
		}

		//remove states from olds status		
		if (oldStatus == DiseaseState.SUSCEPTIBLE) {
			numSusceptible--;
		} else if (oldStatus == DiseaseState.NONCONTAGIOUS_ASSYMPTOMATIC) {
			numNonContagious--;
			numAssymptomatic--;
		} else if (oldStatus == DiseaseState.NONCONTAGIOUS_SYMPTOMATIC) {
			numNonContagious--;
			numSymptomatic--;
		} else if (oldStatus == DiseaseState.CONTAGIOUS_SYMPTOMATIC) {
			numContagious--;
			numSymptomatic--;
		} else if (oldStatus == DiseaseState.CONTAGIOUS_ASSYMPTOMATIC) {
			numContagious--;
			numAssymptomatic--;
		} else {
			throw new AssertionError("Impossible");
		}


		//add statues due to new status
		if (newStatus == DiseaseState.SUSCEPTIBLE) {
			throw new IllegalStateException("Cannot reenter SUSCEPTIBLE state");
		} else if (newStatus == DiseaseState.NONCONTAGIOUS_ASSYMPTOMATIC) {
			numNonContagious++;
			numAssymptomatic++;
		} else if (newStatus == DiseaseState.NONCONTAGIOUS_SYMPTOMATIC) {
			numNonContagious++;
			numSymptomatic++;
		} else if (newStatus == DiseaseState.CONTAGIOUS_SYMPTOMATIC) {
			numContagious++;
			numSymptomatic++;
		} else if (newStatus == DiseaseState.CONTAGIOUS_ASSYMPTOMATIC) {
			numContagious++;
			numAssymptomatic++;
		} else if (newStatus == DiseaseState.DEAD) {
			throw new IllegalStateException("Cannot die");
		} else if (newStatus == DiseaseState.RECOVERED) {
			numRecovered++;
			scheduleTable.remove(index);
		} else {
			throw new IllegalStateException("Unknown state");
		}

		sickStatus[index] = (byte) newStatus.ordinal();
	}


	/**
	 * @param index - The index of a person in this group.
	 * @param cType - The type of contact to implement.
	 *
	 * @return - An object that summerizes all the information required to implement a contact
	 * between this agent an another unknown agent.
	 */
	public ContactInfo extractContactInfo(int index, ContactType cType) {
		return new ContactInfo(
				HouseholdFactory.ages[age[index]],
				DiseaseState.values()[sickStatus[index]],
				cType);
	}


	/**
	 * @param index - The index of a person in this group.
	 *
	 * @return The DiseaseState of this person.
	 */
	public DiseaseState getDiseaseState(int index) {
		return DiseaseState.values()[sickStatus[index]];
	}


	/**
	 * @param index - The index of a person in this group.
	 *
	 * @return The AgeCohort of this person.
	 */
	public AgeCohort getAgeCohort(int index) {
		return HouseholdFactory.ages[age[index]];
	}


	/**
	 * @param index - The index of a person in this group.
	 *
	 * @return True if this person has any family members
	 */
	public boolean hasAFamily(int index) {
		return (getFamilySize(index) > 1);
	}


	/**
	 * @param index - The index of a person in this group.
	 *
	 * @return The size of a person's family - always >= 1
	 */
	public int getFamilySize(int index) {

		int size = leftFamily[index] + 1;

		index++;

		while (index < numAgents && leftFamily[index] > 0) {
			index++;
			size++;
		}
		return size;
	}


	/**
	 * @param cohort - The cohort of the person you wish to "draw" (random draws will be
	 * repeated until the selected individual is in this cohort)
	 *
	 * @return The index of a random person in a particular age cohort
	 */
	public int getRandomPerson(HouseholdFactory.AgeCohort cohort) {

		RandomEngine rand = home.getRand();
		int randIndex;

		do {
			randIndex = (int) (rand.nextDouble() * numAgents);

			if (age[randIndex] == cohort.ordinal()) {
				return randIndex;
			}
		} while (true);
	}


	/**
	 * Infect an agent.
	 *
	 * @param index - The index of a person in this group (the infected agent)
	 */
	public void expose(int index) {

		if (sickStatus[index] != (byte) DiseaseState.SUSCEPTIBLE.ordinal()) {
			throw new RuntimeException("ERROR :: Must expose SUSCEPTIBLE person");
		}

		setDiseaseState(index, DiseaseState.NONCONTAGIOUS_ASSYMPTOMATIC);

		newInfections++;

		SimulationEvent agentEvent = new AgentEvent(
				home.parentThread.getCurrentTime(),
				home,
				index,
				hasAFamily(index));

		scheduleTable.put((AgentEvent) agentEvent);

		home.parentThread.addEvent(agentEvent);
	}


	/**
	 * Find a random unifected adult and infect him (used for seeding the sick population).
	 */
	public void exposeRandomAdult() {

		do {
			int randIndex = this.getRandomPerson(HouseholdFactory.AgeCohort.ADULT_COHORT);

			//test to make sure you got an infectable person
			if (sickStatus[randIndex] == (byte) DiseaseState.SUSCEPTIBLE.ordinal()) {
				this.expose(randIndex);
				return;
			}
		} while (true);
	}


	/**
	 * Implement a random family contact.
	 *
	 * @param indexOfContactor - The index of the person who is contacting a random family member.
	 */
	public void makeRandomFamilyContact(int indexOfContactor) {

		RandomEngine rand = home.getRand();

		int familySize = this.getFamilySize(indexOfContactor);

		if (familySize > 1) {

			int contactIndex =
					indexOfContactor - leftFamily[indexOfContactor] + //the far left family member
					((int) (rand.nextDouble() * familySize));				//ANY random family member

			while (contactIndex == indexOfContactor) {
				//redraw
				contactIndex =
						indexOfContactor - leftFamily[indexOfContactor]
						+ ((int) (rand.nextDouble() * familySize));
			}

			//make the contact
			ContactInfo ci = extractContactInfo(indexOfContactor, ContactType.FAMILY_CONTACT);
			contact(contactIndex, ci);

		} else {
			throw new RuntimeException("Cannot call makeRandomFamilyContact on agent without family");
		}
	}


	/**
	 * Implement a random contact -- Find a random person in the same age cohort and contact
	 * that person. The random contact will not necessarily be in the same place.
	 *
	 * @param indexOfContactor - The index of the person who is contacting a random person.
	 */
	public void makeRandomContact(int indexOfContactor) {

		int zipcodeOfContactie = home.parentThread.manager().population.getRandomPlaceIndex(
				home.placeIDNum(),
				home.getRand().nextDouble());

		ContactInfo ci = extractContactInfo(indexOfContactor, ContactType.RANDOM_CONTACT);

		//if contact happens between 2 people in this zipcode
		if (zipcodeOfContactie == home.placeIDNum()) {
			//make the contact
			contact(-indexOfContactor, ci);

		} else {
			//export the contact for execution at Reconcilations			
			home.parentThread.exportContact(zipcodeOfContactie, Integer.MIN_VALUE, ci);
		}
	}


	public void makeRepeatableContact(int indexOfContactor) {
//		//@todo -- fix this -- only here for compilation
	}


	/**
	 * Implement a contact between a person in this group and a ContactInfo wrapper.
	 *
	 * @param indexOfContactie - If this value is non-negative we assume it is a valid array index
	 * and we will contact the person with this index. <p>
	 *
	 * If indexOfContactie is negative then we will find a random person in the given age cohort.
	 * The random contact CANNOT have the index abs(indexOfContactie). We do not allow that index
	 * because we want to prevent any "self contacts".
	 */
	public void contact(int indexOfContactie, ContactInfo info) {

		//only process a contact if the contact can produce an infection
		if (info.diseaseState.isContagious()) {
//		if(info.sickStatus == Disease.DiseaseState.INFECTED) {

			//since indexOfContactie might not be an actually index (because it could be negative)
			int actualContactieIndex = indexOfContactie;

			//if we need to find a random person
			if (indexOfContactie < 0) {

				int tempIndex;

				/*
				 * Note -- If any ModelPlace's have a small population this loop might never 
				 * exit because there won't be a viable agent.
				 */
				do {
					tempIndex = getRandomPerson(info.age);

					//if we are guaranteed not to have a self-contact
					if (tempIndex != Math.abs(indexOfContactie)) {			//do I need a Math.abs here??

						actualContactieIndex = tempIndex;
					}
				} while (actualContactieIndex < 0);
			}
			//actualContactieIndex now contains a valid index in this zipcode
			if (sickStatus[actualContactieIndex] == DiseaseState.SUSCEPTIBLE.ordinal()) {

				//conside the contact a successful transmission
				if (home.getRand().nextDouble() < info.contactType.getIntimacy() * Diseases.disease().baseInfectiousness()) {
					expose(actualContactieIndex);
				}
			} else if (DiseaseState.values()[sickStatus[actualContactieIndex]].isInfected()) {
				//if the "contactie" was already infect we must push back his infection time

				//determine if you need to push back time
				TimeStamp currentTime = home.parentThread.getCurrentTime();

				AgentEvent mapExtract = scheduleTable.get(actualContactieIndex);
				TimeStamp exposureTime = mapExtract.getTimeOfExposure();

				//push back the exposure time because this agent was exposed earlier by an "offnode" agent
				if (currentTime.occursBefore(exposureTime)) {

					//System.out.println(actualContactieIndex + "\t:: exposureTime is becoming :: " + currentTime + " from " + exposureTime);

					//remove old AgentEvent from queue which is 
					//tagged with the incorrect time
					home.parentThread.removeEvent(mapExtract);

					//push back infection time
					mapExtract.recomputeEventTimes(currentTime);

					home.parentThread.addEvent(mapExtract);
				}
			}
		}
	}


	/**
	 * A VERY ugly implementation of mass vaccination.
	 *
	 * @param fraction - The fraction of SUSCEPTIBLE agents who are vaccinated
	 */
	public void vaccinate(double fraction) {

		if (hasBeenVaccinated) {
			return;
		} else {
			hasBeenVaccinated = true;
		}

		RandomEngine rand = home.getRand();

		for (int i = 0; i < this.numAgents; i++) {

			//if this agent is SUSCEPTIBLE
			if (sickStatus[i] == (byte) DiseaseState.SUSCEPTIBLE.ordinal()) {
				//AND he makes the "random cut"
				if (rand.nextDouble() < fraction) {
					sickStatus[i] = (byte) DiseaseState.RECOVERED.ordinal();
				}
			}
		}
	}
}
