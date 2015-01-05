package time;


import com.google.common.primitives.Ints;


public class NeverOccurringTimeStamp extends TimeStamp {

	NeverOccurringTimeStamp() {
		super(Integer.MAX_VALUE);
	}


	@Override
	public TimeStamp add(LengthOfTime timePeriod) {
		throw new UnsupportedOperationException("Not Defined -- This time does not exist");
	}


	@Override
	public TimeStamp subtract(LengthOfTime timePeriod) {		
		throw new UnsupportedOperationException("Not Defined -- This time does not exist");
	}


	@Override
	public int compareTo(TimeStamp t) {
		return Ints.compare(this.time, t.time);
	}


	@Override
	public boolean occursAfter(TimeStamp ts) {
		return this.time > ts.time;
	}


	@Override
	public boolean occursBefore(TimeStamp ts) {
		return this.time < ts.time;
	}


	@Override
	public boolean occursSimultaneously(TimeStamp ts) {
		return this.time == ts.time;
	}


	@Override
	public boolean occurs() {
		return false;
	}


	@Override
	public int asIndexIntoOneDay() {
		throw new UnsupportedOperationException("Not Defined -- This time does not exist");
	}


	@Override
	public String toString() {
		return Integer.toString(time);
	}
	
}
