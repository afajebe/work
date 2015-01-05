package core;


import model.events.AgentEvent;
import model.events.OffThreadContactEvent;
import model.events.ReconcileEvent;
import model.events.SimulationEvent;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import model.ContactInfo;
import model.events.RecordOfOffThreadContactEvent;
import time.TimeStamp;
import time.TimeStamps;


/**
 * A WorkerThread is responsible for processing the events associated with agents who live in the
 * ModelPlaces that WorkerThread is allocated.
 */
public class WorkerThread implements Runnable, Iterable<ModelPlace> {

	/** The Model's Thread Manager. */
	private final ThreadManager manager;

	/** The index of this thread. */
	private final int threadIndex;

	/** The places this Thread manages. */
	private TreeMap<Integer, ModelPlace> places;

	/** This threads currentTime (as defined by the minimum event of the queue) */
	private TimeStamp currentTime = TimeStamps.initalTime();

	/** The queue of events that need processing. */
	private TreeMap<SimulationEvent, SimulationEvent> eventQueue;

	/**
	 * offThreadEventQueues.get(i) = The seeds of OffThreadContactEvents that need to be sent to
	 * thread i.
	 */
	private ArrayList<ArrayDeque<RecordOfOffThreadContactEvent>> queuesOfOffThreadEventSeeds;

	/** This field is used to assign each SimulationEvent a unique idNum used to break ties. */
	private int nextEventID;


	/** Create a WorkerThread (Should make one per CPU). */
	WorkerThread(ThreadManager threadManager, int threadIndex) {

		this.manager = threadManager;
		this.threadIndex = threadIndex;

		this.places = new TreeMap<>();
		this.eventQueue = new TreeMap<>();

		this.queuesOfOffThreadEventSeeds = new ArrayList<>(threadManager.numThreads());
		for (int i = 0; i < threadManager.numThreads(); i++) {
			queuesOfOffThreadEventSeeds.add(i, new ArrayDeque<RecordOfOffThreadContactEvent>());
		}
	}


	/** Prompt this thread to create all the ModelPlaces this WorkerThread is responsible for. */
	public void buildThisThreadsPlaces() {

		//add all places
		for (int i = 0; i < manager.population.numPlaces; i++) {
			if (threadIndex == manager.population.threadOwners[i]) {
				buildAndAddPlace(i);
			}
		}
	}


	/**
	 * Add a ModelPlace to this Thread's processing workload -- doing so requires the creation of
	 * the population of agents that live in this place.
	 *
	 * @param placeIndex - The index of the place being built
	 */
	private void buildAndAddPlace(int placeIndex) {

//		System.out.println(
//				"Creating place :: " + placeIndex
//				+ " :: on thread :: " + threadIndex);

		int rngSeed = placeIndex + this.manager.getModel().getRunNumber();

		ModelPlace place = new ModelPlace(
				this,
				placeIndex,
				rngSeed,
				manager.population.extraction.getSeed(placeIndex)); //<-- @todo - fix this train

		places.put(placeIndex, place);

		//seed the reconcile Events when this WorkerThread gets its first ModelPlace
		if (places.size() == 1) {
			addEvent(new ReconcileEvent(
					TimeStamps.initalTime().add(ReconcileEvent.getFrequency()),
					place));

		}
	}


	/** @return - The ThreadManager that manages all the threads. */
	public ThreadManager manager() {
		return this.manager;
	}


	/** @return This thread's unique id number. */
	public int threadIDNum() {
		return threadIndex;
	}


	/** @return - The number of ModelPlaces in this Thread. */
	public int numPlaces() {
		return this.places.size();
	}


	/** @return a uniformly increasing identification number for SimulationEvents to use. */
	public int generateUniqueEventID() {
		return ++nextEventID;
	}


	/** Get the current time. */
	public TimeStamp getCurrentTime() {
		return currentTime;
	}


	/** Add a SimulationEvent to the eventQueue. */
	public void addEvent(SimulationEvent e) {
		eventQueue.put(e, e);
	}


	/** Create an OffThreadContactEvent and add it to the event queue. */
	public void buildAndAddOffThreadContactEvent(RecordOfOffThreadContactEvent record) {

		//ensure that the OffThreadContactEvent should be added to this WorkerThread
		if (places.containsKey(record.placeIndex())) {

			//build OffThreadContactEvent and add it to the queue
			ModelPlace placeEventOccurs = places.get(record.placeIndex());
			OffThreadContactEvent event = record.makeEvent(placeEventOccurs);

			eventQueue.put(event, event);
		} else {
			throw new IllegalArgumentException(
					"Attempting to add an OffThreadContactEventSeed that references a Place not "
					+ "managed by this thread");
		}
	}


	/** Remove an AgentEvent from the executionQueue. */
	public void removeEvent(AgentEvent e) {
		eventQueue.remove(e);
	}


	/** @return - A ModelPlace that resides on this Thread. */
	public ModelPlace getPlace(int placeIndex) {

		if (places.containsKey(placeIndex)) {
			return places.get(placeIndex);
		} else {
			throw new IllegalArgumentException(
					"This thread does not contain ModelPlace: " + placeIndex);
		}
	}


	/**
	 * A contact needs to be made between 2 agents that aren't in the same ModelPlace. Send a record
	 * of this contact to the Node. If the contact needs to be exported to a 2nd Node then the
	 * current GMN will create an OffNodeContactEvent.
	 *
	 * @param zipcode - The zipcode of the contactie
	 * @param index - The index of the contactie (if known) (will be negative if not known)
	 * @param ageOfContactor - The AgeChort of the contactor (sick person)
	 * @param ssOfContactor - The DiseaseState of the contactor (sick person)
	 * @param contactType - The ContactType being made, lets us know how intimate a contact is made.
	 */
	public void exportContact(int zipcode, int index, ContactInfo contactorSummary) {

		//if contact is local (on this thread)
		if (places.containsKey(zipcode)) {
			//implement the contact at the 2nd ModelPlace that this node manages
			ModelPlace place = this.places.get(zipcode);

			place.locals().contact(index, contactorSummary);
		} else {
			//contact isn't local - so send if up to the Node
			int destinationThread = this.manager.population.threadOwners[zipcode];
			this.queuesOfOffThreadEventSeeds.get(destinationThread).
					add(
					new RecordOfOffThreadContactEvent(currentTime, zipcode, index, contactorSummary));
		}
	}


	/**
	 * Load the OffThreadContactEvents that were queued up on other threads during the last round
	 * of processing.
	 */
	void retrieveOffThreadContactEventsFromOtherThreads() {

		int totalOTCECount = 0;

		for (int i = 0; i < manager.numThreads(); i++) {

			WorkerThread otherThread = manager.getThread(i);

			ArrayDeque<RecordOfOffThreadContactEvent> loadTheseEvents = otherThread.queuesOfOffThreadEventSeeds.
					get(threadIndex);

			totalOTCECount += loadTheseEvents.size();
			while (loadTheseEvents.size() > 0) {
				buildAndAddOffThreadContactEvent(loadTheseEvents.removeFirst());
			}
		}

		System.out.println("Thread :: " + threadIndex + " imported " + totalOTCECount + " OTCEs");

	}


	/**
	 * We expect this WorkerTheads's PriorityQueuse to have following events in it :: <br>
	 * - 1 Event PER "owned" ModelPlace AT ALL TIMES <br>
	 * - 1 ReconcileEvent AT ALL TIMES <br>
	 * - 1 AgentEvent per sick agent <p>
	 *
	 * If the PQ's size is too small we infer that it is because there are no sick agents.
	 */
	public boolean hasInfections() {
		return (eventQueue.size() > this.numPlaces() + 1);
	}


	/** Add a ReconcileEvent to this WorkerThread. This event will fire at the given time). */
	public void addReconcileEvent(TimeStamp time) {
		//associate the ReconcileEvent with an arbitrary ModelPlace
		ModelPlace arbitraryPlace = this.eventQueue.lastEntry().getValue().getPlace();
		this.eventQueue.lastEntry().getValue();
		addEvent(new ReconcileEvent(time, arbitraryPlace));

//		System.out.println("Adding ReconcileEvent at time :: " + time);
	}


	/** Process events from the processQueue until you reach a ReconcileEvent. */
	@Override
	public void run() {

//		executionHistory.startNewRecord();

		/*
		 * We can insert an innner while loop that processes X events
		 * and then lets this Thread yield, however, this hinders performance
		 * (at low X) and seems to have no performance increase above the
		 * no loop version when X is high
		 */
		while (true) {

			if (eventQueue.isEmpty()) {
				throw new IllegalStateException("The PriorityQueue should never be empty");
			}

			SimulationEvent e = eventQueue.pollFirstEntry().getValue();
//			System.out.println(e.toString());

			//set the proper time
			currentTime = e.getTime();

			e.implementEvent();

			if (e instanceof ReconcileEvent) {
//				executionHistory.endCurrentRecord();
				return;
			}
		}
	}


	/** @return - The number of events waiting to be sent to another thread. */
	public int getNumQueueOTCE() {
		int sum = 0;
		for (ArrayDeque<RecordOfOffThreadContactEvent> deque : queuesOfOffThreadEventSeeds) {
			sum += deque.size();
		}
		return sum;
	}


	/** Return the number of sick agents this thread manages. */
	public int getNumSick() {
		int numSick = 0;
		for (ModelPlace place : this.places.values()) {
			numSick += place.locals().numAssymptomatic + place.locals().numContagious;
		}
		return numSick;
	}


	public int getTotalInfections() {
		int totalNumSick = 0;
		for (ModelPlace place : this.places.values()) {
			totalNumSick += place.locals().numRecovered;
		}
		return totalNumSick;
	}


	/**
	 * This method resets the newInfection count on every ModelPlace this thread manages.
	 *
	 * @return The number of new infections on this thread since the last data point.
	 */
	public int getNumNewInfections() {

		int newInfections = 0;

		for (ModelPlace place : this.places.values()) {
			newInfections += place.locals().newInfections;
			place.locals().newInfections = 0;
		}
		return newInfections;
	}


	@Override
	public Iterator<ModelPlace> iterator() {
		return this.places.values().iterator();
	}
//	/**  Retrieve Records. */
//	public ModelPlaceRecord[] getRecords() {
//		ModelPlaceRecord[] records = new ModelPlaceRecord[places.size()];
//
//		Collection c = places.values();
//		Iterator it = c.iterator();
//		int counter = 0;
//		ModelPlace elem;
//		while (it.hasNext()) {
//			elem = (ModelPlace) it.next();
//			records[counter] = elem.getRecord();
//			counter++;
//		}
//
//		return records;
//	}
//	/**  Prompt this WorkerThread to write its own log files. */
//	public void writeToLog(String writeMe) {
//		dout.write(writeMe);
//	}
}
