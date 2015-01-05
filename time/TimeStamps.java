package time;


public class TimeStamps {

	private static final TimeStamp initalTime = new TimeStamp(0);

	private static final TimeStamp neverOccuringTime = new NeverOccurringTimeStamp();


	public static TimeStamp initalTime() {
		return initalTime;
	}


	public static TimeStamp neverOccuringTime() {
		return neverOccuringTime;
	}


	/** @return - The TimeStamp that occurs first from the given TimeStamp inputs. */
	public static TimeStamp firstOf(TimeStamp... times) {

		TimeStamp minTime = null;

		for (TimeStamp curTimeStamp : times) {

			//the first time stamp is always best when you have nothing else to compare it too
			if (minTime == null) {
				minTime = curTimeStamp;
			} else {
				if (curTimeStamp.occursBefore(minTime)) {
					minTime = curTimeStamp;
				}
			}
		}

		return minTime;
	}
}
