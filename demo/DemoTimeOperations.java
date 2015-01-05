package demo;


import time.LengthOfTime;
import time.ModelTimeUnit;
import time.TimeStamp;
import time.TimeStamps;


public class DemoTimeOperations {

	public static void main(String[] args) {

		demonstrateTimeMath();

		examineOccursAndDoesNotOccur();

		examineOneTimeStep();
	}


	private static void demonstrateTimeMath() {
		TimeStamp startTime = TimeStamps.initalTime();
		LengthOfTime year = new LengthOfTime(1.5284, ModelTimeUnit.YEARS);
		TimeStamp endTime = startTime.add(year);


		System.out.println("startTime :: " + startTime.toString());
		System.out.println("year :: " + year.toString());
		System.out.println("endtime :: " + endTime.toString());


		LengthOfTime calculatedYear = new LengthOfTime(startTime, endTime);
		System.out.println("calculatedYear :: " + calculatedYear.toString());
	}


	private static void examineOccursAndDoesNotOccur() {
		TimeStamp neverOccurs = TimeStamps.neverOccuringTime();
		System.out.println("neverOccurs.occurs() :: " + neverOccurs.occurs());
		System.out.println("neverOccurs.doesNotOccur() :: " + neverOccurs.doesNotOccur());


		TimeStamp initalTime = TimeStamps.initalTime();
		System.out.println("initalTime.occurs() :: " + initalTime.occurs());
		System.out.println("initalTime.doesNotOccur() :: " + initalTime.doesNotOccur());
	}


	public static void examineOneTimeStep() {
		LengthOfTime tp = LengthOfTime.oneModelingTimeStep();
		System.out.println(tp.toString());
	}
}
