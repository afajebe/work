package time;


/** A LengthOfTime is an immutable amount of time. */
public class LengthOfTime {

	/** The number of time steps per simulated day. */
	public static final int TIME_STEPS_PER_DAY = 100;

	final int numYears;

	final int numDays;

	final int numHours;

	final int numMins;


	/**
	 * Define a specific LengthOfTime
	 *
	 * @param numYears - The number of years in this length of time
	 * @param numDays - The number of days in this length of time
	 * @param numHours - The number of hours in this length of time
	 * @param numMin - The number of minutes in this length of time
	 */
	public LengthOfTime(int numYears, int numDays, int numHours, int numMin) {

		if (numYears < 0) {
			throw new IllegalArgumentException("numYears cannot be negative :: " + numYears);
		}
		if (numDays < 0) {
			throw new IllegalArgumentException("numDays cannot be negative :: " + numDays);
		}
		if (numHours < 0) {
			throw new IllegalArgumentException("numHours cannot be negative :: " + numHours);
		}
		if (numMin < 0) {
			throw new IllegalArgumentException("numMin cannot be negative :: " + numMin);
		}

		this.numYears = numYears;
		this.numDays = numDays;
		this.numHours = numHours;
		this.numMins = numMin;
	}


	public LengthOfTime(TimeStamp earlyTime, TimeStamp laterTime) {

		assert (earlyTime.occursBefore(laterTime));

		double totalNumberOfTicks = laterTime.time - earlyTime.time;

		this.numYears = (int) (totalNumberOfTicks / ticksPerYear());
		totalNumberOfTicks -= numYears * ticksPerYear();

		this.numDays = (int) (totalNumberOfTicks / ticksPerDay());
		totalNumberOfTicks -= numDays * ticksPerDay();

		this.numHours = (int) (totalNumberOfTicks / ticksPerHour());
		totalNumberOfTicks -= numHours * ticksPerHour();

		this.numMins = (int) (totalNumberOfTicks / ticksPerMin());
	}


	public LengthOfTime(double amount, ModelTimeUnit unit) {

		if (unit == ModelTimeUnit.YEARS) {

			this.numYears = (int) amount;
			this.numDays = (int) ((amount % 1) * 365);
			this.numHours = (int) (((amount * 365) % 1) * 24);
			this.numMins = (int) (((amount * 365 * 24) % 1) * 60);

		} else if (unit == ModelTimeUnit.DAYS) {

			this.numYears = 0;
			this.numDays = (int) amount;
			this.numHours = (int) ((amount % 1) * 24);
			this.numMins = (int) (((amount * 24) % 1) * 60);

		} else if (unit == ModelTimeUnit.HOURS) {

			this.numYears = 0;
			this.numDays = 0;
			this.numHours = (int) amount;
			this.numMins = (int) ((amount % 1) * 60);

		} else if (unit == ModelTimeUnit.MINUTES) {

			this.numYears = 0;
			this.numDays = 0;
			this.numHours = 0;
			this.numMins = (int) amount;

		} else {
			throw new IllegalStateException("Unexpected ModelTimeUnit :: " + unit);
		}
	}

	private static final LengthOfTime oneTimeStep = new LengthOfTime(1.0 / (double) TIME_STEPS_PER_DAY, ModelTimeUnit.DAYS);


	public static LengthOfTime oneModelingTimeStep() {
		return oneTimeStep;
	}


	int toNumTicks() {

		int totalDays = 365 * numYears + numDays;
		int ticksForDays = totalDays * TIME_STEPS_PER_DAY;

		int totalMins = 60 * numHours + numMins;
		int MINS_PER_DAY = 24 * 60;
		int ticksForMins = totalMins * TIME_STEPS_PER_DAY / MINS_PER_DAY;

		int totalTicks = ticksForDays + ticksForMins;

		return totalTicks;
	}


	@Override
	public String toString() {
		return (numYears + " years, " + numDays + " days, " + numHours + " hours, " + numMins + " mins");
	}


	private double ticksPerYear() {
		return 365 * TIME_STEPS_PER_DAY;
	}


	private double ticksPerDay() {
		return TIME_STEPS_PER_DAY;
	}


	private double ticksPerHour() {
		return TIME_STEPS_PER_DAY / 24.0;
	}


	private double ticksPerMin() {
		return TIME_STEPS_PER_DAY / (24.0 * 60.0);

	}


	public int numYears() {
		return this.numYears;
	}
}
