package model.events;


import core.ModelPlace;
import model.disease.DiseaseProgress;
import model.disease.DiseaseState;
import time.TimeStamp;


public class DiseaseProgressEvent {

	private final DiseaseProgress diseaseEvent;

	private final DiseaseState resultingState;

	private final TimeStamp timeOfEvent;


	public DiseaseProgressEvent(DiseaseProgress eventType, DiseaseState resultingState, TimeStamp time) {
		this.diseaseEvent = eventType;
		this.resultingState = resultingState;
		this.timeOfEvent = time;

	}


	void implementEvent(ModelPlace placeEventOccurs, int personIndex, AgentEvent agentItinerary) {

		//set the new DiseaseState
		placeEventOccurs.locals().setDiseaseState(personIndex, resultingState);
	}


	public DiseaseProgress diseaseEvent() {
		return diseaseEvent;
	}


	public DiseaseState resultingState() {
		return resultingState;
	}


	public TimeStamp timeOfEvent() {
		return timeOfEvent;
	}
}
