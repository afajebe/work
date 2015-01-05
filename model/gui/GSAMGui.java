package model.gui;


import java.awt.image.BufferedImage;


/** A GSAM GUI must support these methods. */
public interface GSAMGui {

	/** Update the GUI to reflect that this number of people are currently sick. */
	public void updateNumSick(int numCurrentlySick);


	/**
	 * Update the GUI to reflect that this number of people have been sick over the duration of the
	 * run.
	 */
	public void updateTotalSick(int totalSick);


	/** Update the GUI to show this map of incidence. */
	public void updateMap(BufferedImage image);
}
