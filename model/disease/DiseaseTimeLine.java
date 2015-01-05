package model.disease;


import cern.jet.random.engine.RandomEngine;
import java.util.LinkedList;
import model.events.DiseaseProgressEvent;
import time.LengthOfTime;
import time.TimeStamp;
import time.TimeStamps;


/** A DiseaseTimeLine keeps track of when an Agent's DiseaseState should change. */
public class DiseaseTimeLine {

	private final TimeStamp timeOfExposure;

	private final TimeStamp symptomaticTime;

	private final TimeStamp infectiousTime;

	private final TimeStamp recoveryTime;

	private final LinkedList<DiseaseProgressEvent> events;


	public DiseaseTimeLine(TimeStamp timeOfExposure, RandomEngine rng) {

		this.timeOfExposure = timeOfExposure;

		this.infectiousTime = Diseases.disease().getTimeContagious(
				timeOfExposure, rng.nextDouble());

		this.symptomaticTime = Diseases.disease().getTimeSymptomatic(
				timeOfExposure, rng.nextDouble());

		LengthOfTime contagiousPeriod = Diseases.disease().getContagiousPeriod(
				rng.nextDouble());

		this.recoveryTime = infectiousTime.add(contagiousPeriod);

		this.events = buildEvents();
	}


	private LinkedList<DiseaseProgressEvent> buildEvents() {

		LinkedList<DiseaseProgressEvent> eventList = new LinkedList<>();

		if (symptomaticTime.doesNotOccur()) {

			//become contagious, never get symptomatic
			eventList.add(new DiseaseProgressEvent(
					DiseaseProgress.BECOME_CONTAGIOUS,
					DiseaseState.CONTAGIOUS_ASSYMPTOMATIC,
					infectiousTime));

		} else {

			if (symptomaticTime.occursBefore(infectiousTime)) {

				//symptomatic first, contagious second
				eventList.add(new DiseaseProgressEvent(
						DiseaseProgress.BECOME_SYMPTOMATIC,
						DiseaseState.NONCONTAGIOUS_SYMPTOMATIC,
						symptomaticTime));

				eventList.add(new DiseaseProgressEvent(
						DiseaseProgress.BECOME_CONTAGIOUS,
						DiseaseState.CONTAGIOUS_SYMPTOMATIC,
						infectiousTime));

			} else {

				//contagious first, symptomatic second
				eventList.add(new DiseaseProgressEvent(
						DiseaseProgress.BECOME_CONTAGIOUS,
						DiseaseState.CONTAGIOUS_ASSYMPTOMATIC,
						infectiousTime));

				eventList.add(new DiseaseProgressEvent(
						DiseaseProgress.BECOME_SYMPTOMATIC,
						DiseaseState.CONTAGIOUS_SYMPTOMATIC,
						symptomaticTime));
			}
		}

		eventList.add(new DiseaseProgressEvent(
				DiseaseProgress.RECOVER,
				DiseaseState.RECOVERED,
				recoveryTime));

		return eventList;
	}


	public TimeStamp timeOfExposure() {
		return this.timeOfExposure;
	}


	public TimeStamp infectiousTime() {
		return this.infectiousTime;
	}


	public DiseaseProgressEvent popDiseaseProgressEvent() {
		return events.removeFirst();
	}


	public TimeStamp curTime() {
		if (events.isEmpty()) {
			return TimeStamps.neverOccuringTime();
		} else {
			return events.peek().timeOfEvent();
		}
	}
}
