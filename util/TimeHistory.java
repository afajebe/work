package util;

import java.util.Vector;
import java.util.concurrent.TimeUnit;

/**
 * A Simple class that stores an array of numbers that represent times
 */
public class TimeHistory {
	
	
	/** The History of times. */
	Vector<Long> times;
	
	/** When the "currentTime" began. */
	long currentStartTime;
	
	
	/** Creates a new instance of TimeHistory */
	public TimeHistory() {
		this.times = new Vector<Long>();
	}
	
	
	/** 
	 * "Paired" with endCurrentRecord.  Records the time this method is called
	 */
	public void startNewRecord() {
		this.currentStartTime = System.nanoTime();	
	}
	
	
	/** 
	 * "Paired" with startNewRecord.  Computes this time since "endCurrentRecord"
	 * was called.  Add the time lag to this objects history.
	 */
	public void endCurrentRecord() {
		long endTime = System.nanoTime();
		long totalTime = endTime - currentStartTime;
		
		times.add(totalTime);
	}
	
	
	/** Add a complete record to this history. */
	public void addCompleteRecord(long time) {
		times.add(time);		
	}
	
	
	/** 
	 * Return a String that list a recently recorded times.
	 *
	 * @param unit - The time unit you want the time returned. 
	 * @param n - The maximum number of entries you want shown.
	 *
	 * @return - A String like "22 - 33 - 44 - 55 (seconds)"
	 */ 
	public String toString(TimeUnit unit , int n) {
		
		long divideBy = -1;
		String suffix = "";
		
		if(unit == TimeUnit.SECONDS) {
			suffix = "(sec)";
			divideBy = 100000000L;
			
		} else if(unit == TimeUnit.MILLISECONDS) {
			suffix = "(msec)";
			divideBy = 100000L;
			
		} else {
			throw new IllegalArgumentException("The time unit " + unit + " is not supported");
		}
		
		StringBuffer buffer = new StringBuffer();
		
		int tempN = Math.min(times.size() , n);
		int start = times.size() - tempN;
		
		for (int i = start ; i < start + tempN ; i++) {
			buffer.append( (times.get(i) / divideBy) + " - ");
		}
		if(buffer.length() >= 2) {
			buffer.delete(buffer.length() - 2 , buffer.length());	//remove the final " - "
		}
		buffer.append(suffix);					//add the units suffix
		
		return buffer.toString();
	}
	
	
}
