package util;

import model.events.AgentEvent;


/**
 * The class provides a quick and dirty HashTable that can effectively "shrink" sparse (mostly null) 
 * AgentEvent arrays.
 * 
 * For instance, if you have an array of length 50 Million, with 5 million reference and 45 Million 
 * nulls values, it can be better (in a memory footprint sense) to store only the 5 million references
 * in a HashTable instead.  This is the exact problem that this class is designed to address.
 * 
 * This HashTable is not meant to grow in size. If the table gets "too full" an error will be
 * thrown, the table will never be rehashed.
 */
public class AgentEventHashTable {

	/** The "smaller" destination array. */
	private AgentEvent[] table;

	/** The size of the table above. */
	private int tableSize;

	/** How many references are stored in the table. */
	private int count;

	/** What fraction of this table can be full before errors are thrown. */
	private double loadCapacity = .75;

	/** The exact number of objects this table can hold before errors are thrown. */
	private int threshold;


	/**
	 * 
	 * @param originalArraySize - How large is the array you are attempting to shrink (or rather, 
	 * @param expectedMaxFillRate - The maximum percentage of the population that will be tracked
	 * at any given time.
	 */
	public AgentEventHashTable(int originalArraySize, double expectedMaxFillRate) {
		
		if(expectedMaxFillRate > 1.0 || expectedMaxFillRate <= 0.0) {
			throw new IllegalArgumentException("expectedMaxFillRate must be greater than " +
				"zero and less than or equal to one :: " + expectedMaxFillRate
				);
		}
		
		this.tableSize = (int)(originalArraySize * expectedMaxFillRate / loadCapacity);
		table = new AgentEvent[tableSize];
		
		this.count = 0;
		this.threshold = (int)(tableSize * loadCapacity);
	}
	
	
	public void put(AgentEvent event) {
		
		if(event == null) {
			throw new IllegalArgumentException("Cannot put a null reference in a DirtyHashTable");
		}
		
		int hash = event.personIndex % tableSize;
		//while this space is filled - look below (wrap around if need be)
		while(table[hash] != null) {
			hash = (hash + 1) % tableSize;
		}
		
		count++;
		table[hash] = event;
		
		if(count > threshold) {
			System.out.println("Table has passed its ideal size :: " + threshold);
//			throw new IllegalStateException("Table has passed its maximum size :: " + threshold);
		}
	}
	
	
	/** Retrieve but do not remove an AgentEvent object. */
	public AgentEvent get(int agentIndex) {
		
		int hash = agentIndex % tableSize;
		boolean found = false;
		do {
			//found a reference
			if(table[hash] != null) {				
				if(table[hash].personIndex == agentIndex) {
					found = true;
				} else {
					hash = (hash + 1) % tableSize;;
				}				
			} else {
				//found a null;
				hash = (hash + 1) % tableSize;
			}			
		} while(!found);		
		
		return table[hash];		
	}
	
	
	/**
	 * Remove an event from this HashTable.
	 * 
	 * @param agentIndex - The index of the event owner
	 */
	public void remove(int agentIndex) {
						
		int hash = agentIndex % tableSize;
		boolean found = false;
		do {
			//found a reference
			if(table[hash] != null) {				
				if(table[hash].personIndex == agentIndex) {
					found = true;
				} else {
					hash = (hash + 1) % tableSize;;
				}				
			} else {
				//found a null;
				hash = (hash + 1) % tableSize;
			}			
		} while(!found);		
		
		count--;
		table[hash] = null;		
	}
	
	
	
	

}
