package time;


import com.google.common.primitives.Ints;
import java.io.Serializable;


/**
 * The purpose of the TimeStamp class is to (1) provide readable "time operations" like add,
 * subtract, occursAfter, occursBefore, etc (2) enable easy time based sorting.
 */
public class TimeStamp implements Comparable<TimeStamp>, Serializable {

	/** A precise time. */
	final int time;


	TimeStamp(int time) {

		if (time < 0) {
			throw new IllegalArgumentException("Cannot create a negative time :: " + time);
		}

		this.time = time;
	}


	public TimeStamp add(LengthOfTime timePeriod) {

		int newTime = this.time + timePeriod.toNumTicks();

		return new TimeStamp(newTime);
	}


	public TimeStamp subtract(LengthOfTime timePeriod) {

		int newTime = this.time - timePeriod.toNumTicks();

		return new TimeStamp(newTime);
	}


	@Override
	public int compareTo(TimeStamp t) {
		return Ints.compare(this.time, t.time);
	}


	public boolean occursAfter(TimeStamp ts) {
		return this.time > ts.time;
	}


	public boolean occursBefore(TimeStamp ts) {
		return this.time < ts.time;
	}


	public boolean occursSimultaneously(TimeStamp ts) {
		return this.time == ts.time;
	}


	public boolean occurs() {
		return true;
	}


	public boolean doesNotOccur() {
		return !occurs();
	}


	public int asIndexIntoOneDay() {
		return time % LengthOfTime.TIME_STEPS_PER_DAY;
	}


	@Override
	public String toString() {
		return Integer.toString(time);
	}
}
