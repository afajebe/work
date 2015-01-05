package model.people;


/**
 * A simple class that allows us to locate an Agent by providing the zipcode and index
 * of that agent.
 */
public class AgentLocation implements Comparable<AgentLocation> {
	
	/** The home zipcode of an agent. */
	public int zip;
	
	/** An agents index in his home zipcode. */
	public int index;
	
	/** Build a new AgentLocation. */
	public AgentLocation(int zip , int index) {
		this.zip = zip;
		this.index = index;
	}
	
	
	/** Sort AgentLocations by zip first and index second. */
	@Override
	public int compareTo(AgentLocation other) {
		if (zip < other.zip) {
			return -1;
		} else if (zip > other.zip) {
			return 1;
		} else {
			//zips equal
			if (index < other.index) {
				return -1;
			} else if (index > other.index) {
				return 1;
			} else {
				return 0;
			}
		}
	}
}
