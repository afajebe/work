package core;


import cern.jet.random.engine.MersenneTwister;
import cern.jet.random.engine.RandomEngine;
import model.ModelPlaceRecord;
import model.ModelPlaceSeed;
import model.events.LogUpdateEvent;
import model.people.AgentGroup;
import model.people.USHouseholdMaker;
import time.LengthOfTime;
import time.ModelTimeUnit;
import time.TimeStamps;


public class ModelPlace {
	
	/** The WorkerThread that owns this place. */
	public final WorkerThread parentThread;

	/** A unique model-wide identifying number. */
	private final int placeIDNum;

	/** The ModelPlace's RNG. To ensure reproducibility, each ModelPlace must have its own RNG. */
	private final RandomEngine rand;

	/** The population of local agents. */
	private AgentGroup localAgents;

	/** A Set of time-Series data that describes this ModelPlace alone. */
	private ModelPlaceRecord record;

	/** An object that contains "data" about this ModelPlace. */
	private ModelPlaceSeed placeSeed;


	/**
	 * A place is meant to efficiently store people that live in a given area
	 *
	 * @param parent - This place's parent thread.
	 * @param placeIDNum - A unique identifying number of this local area
	 * @param rngSeed - A seed for this place's RNG
	 * @param placeSeed - An object that contains more detailed info about this place
	 */
	public ModelPlace(WorkerThread parent, int placeIndex, int rngSeed, ModelPlaceSeed placeSeed) {
		
		this.parentThread = parent;
		this.placeIDNum = placeIndex;
		this.rand = new MersenneTwister(rngSeed);
		this.placeSeed = placeSeed;

		this.localAgents = new AgentGroup(
				placeSeed.population,
				this,
				new USHouseholdMaker(rand));

		this.record = new ModelPlaceRecord(placeIndex, parentThread.threadIDNum());

		//information about this node is recorded at the end of the day
		parentThread.addEvent(new LogUpdateEvent(
				TimeStamps.initalTime().add(new LengthOfTime(0.975, ModelTimeUnit.DAYS)), 
				this));
	}


	/** @return The WorkerThread that owns this ModelPlace. */
	public WorkerThread thread() {
		return parentThread;
	}


	/** @return This ModelPlace's index. */
	public int placeIDNum() {
		return placeIDNum;
	}


	/** @return This ModelPlace's RNG. */
	public RandomEngine getRand() {
		return rand;
	}


	/** @return - The set of agents who live locally (i.e. in this Place.) . */
	public AgentGroup locals() {
		return this.localAgents;
	}


	public int getX() {
		return this.placeSeed.latitudeIndex;
	}


	public int getY() {
		return this.placeSeed.longitudeIndex;
	}


	/** Log the current state. */
	public void logCurrentState() {

		record.addDataPoint(
				localAgents.numSusceptible,
				localAgents.numContagious,
				localAgents.numNonContagious,
				localAgents.numSymptomatic,
				localAgents.numAssymptomatic,
				localAgents.numRecovered);

//		if(localAgents.numRecovered >= 100) {
//			//AgentEvent.closeSchools();
//			localAgents.vaccinate(.30);
//			//AgentBehaviorModel.implementSocialDistancing();
//		}
	}


	/** @return This ModelPlace's ModelPlaceRecord. */
	public ModelPlaceRecord getRecord() {
		return record;
	}
}
