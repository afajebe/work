package model;


/**
 * A wrapper for all variable needed to create a ModelPlace -- and the population within.
 *
 * This class is meant to be extended to support more sophisticated ModelPlace creation schemes.
 * For instance, an extention of this class could provide a HouseholdFactory that specifies the
 * size distirbution of local households.
 */
public class ModelPlaceSeed {

	/** The population of a ModelPlace. */
	public int population;

	/** The latitude of a ModelPlace. */
	public final int latitudeIndex;

	/** The longitude of a ModelPlace. */
	public final int longitudeIndex;


	public ModelPlaceSeed(int population, int latIndex, int longIndex) {

		if (latIndex < 0) {
			throw new IllegalArgumentException(
					"latIndex must be a positive integer" + latIndex);
		}
		if (longIndex < 0) {
			throw new IllegalArgumentException(
					"longIndex must be a positive integer " + longIndex);
		}

		this.population = population;
		this.latitudeIndex = latIndex;
		this.longitudeIndex = longIndex;
	}
}
