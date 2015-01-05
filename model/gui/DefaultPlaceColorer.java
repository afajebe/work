package model.gui;


import core.ModelPlace;
import model.people.AgentGroup;
import util.ImageUtility;


public class DefaultPlaceColorer implements PlaceColorer {

	/** Generate a color given the number of people sick and the number of recovered people. */
	@Override
	public int getColor(ModelPlace place) {
		
		AgentGroup localAgents = place.locals();

		double redMult = 100.0;
		double blueMult = 2.0;

		int infections = localAgents.numContagious + localAgents.numNonContagious;
		int recoveries = localAgents.numRecovered;

		//calculate the "redness" of a pixel (tied to infectives)
		double fraction = ((double) infections) / ((double) localAgents.numAgents()) * redMult;
		fraction = Math.min(1.0, fraction);
		int red = (int) (255.0 * fraction);

		//calculate
		int green = 0;

		//calculate the "blueness" of a pixel (tied to recovered)
		fraction = ((double) recoveries) / ((double) localAgents.numAgents()) * blueMult;
		fraction = Math.min(1.0, fraction);
		int blue = (int) (255.0 * fraction);

		return ImageUtility.getRGB(red, green, blue);
	}
}
