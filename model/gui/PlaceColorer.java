package model.gui;


import core.ModelPlace;


public interface PlaceColorer {

	/** Generate a color given the number of people sick and the number of recovered people. */
	public int getColor(ModelPlace place);
}
