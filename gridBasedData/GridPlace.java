package gridBasedData;

import java.io.Serializable;


/**
 * A Simple class that enable easy sorting of LandScan population data.
 * 
 * NOTE:  If complicated changes need to be made to the computation of a distance between two
 * GridPlaces this is the places to add a method like "double computeDist(GridPlace anotherPlace)"
 */
public class GridPlace implements Comparable, Serializable {

	public final int x;

	public final int y;

	public final int pop;


	public GridPlace(int x, int y, int pop) {
		this.x = x;
		this.y = y;
		this.pop = pop;
	}


	public int compareTo(Object o) {
		GridPlace other = (GridPlace) o;

		return this.pop - other.pop;
	}


	public String toString() {
		return pop + "\t" + x + "\t" + y;
	}
	
	
	/**
	 * Compute the total population of this set.
	 * 
	 * @param places - An array of grid values.
	 * @return - The total population in all of the places.
	 */
	public static long getTotalPopulation(GridPlace[] places) {
		long pop = 0;
		for (int i = 0; i < places.length; i++) {
			pop += places[i].pop;
		}
		return pop;
	}
}
