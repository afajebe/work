package model.disease;


/**
 * This enum represent the flu.
 *
 * Implementing other diseases should be straight forward.
 */
public enum DiseaseState {

	SUSCEPTIBLE("Susceptible", false, false, false),
	NONCONTAGIOUS_ASSYMPTOMATIC("NONCONTAGIOUS_ASSYMPTOMATIC", true, false, false),
	NONCONTAGIOUS_SYMPTOMATIC("NONCONTAGIOUS_SYMPTOMATIC", true, true, false),
	CONTAGIOUS_SYMPTOMATIC("CONTAGIOUS_SYMPTOMATIC", true, true, true),
	CONTAGIOUS_ASSYMPTOMATIC("CONTAGIOUS_ASSYMPTOMATIC", true, false, true),
	RECOVERED("RECOVERED", false, false, false),
	DEAD("DEAD", false, false, false);

	//variables
	private final String textValue;

	private final boolean isInfected;

	private final boolean hasSymptoms;

	private final boolean isContagious;

	DiseaseState(String textValue, boolean isInfected, boolean hasSymptoms, boolean isContagious) {
		this.textValue = textValue;
		this.isInfected = isInfected;
		this.hasSymptoms = hasSymptoms;
		this.isContagious = isContagious;
	}


	@Override
	public String toString() {
		return textValue;
	}


	public boolean isInfected() {
		return isInfected;
	}


	public boolean hasSymptoms() {
		return hasSymptoms;
	}


	public boolean isContagious() {
		return isContagious;
	}
}