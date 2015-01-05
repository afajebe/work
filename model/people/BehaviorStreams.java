package model.people;


/**
 * This class provides a single point of access to the BehaviorStreams that are used in the model.
 * Each BehaviorStream used is stored as a singleton object here to increase reuse.
 */
public class BehaviorStreams {

	private static final double EXPECTED_RANDOM_CONTACTS_PER_DAY = 10;

	private static final double EXPECTED_REPEAT_CONTACTS_PER_DAY = 5;

	private static final double EXPECTED_FAMILY_CONTACTS_PER_DAY = 5;

	public static final BehaviorStream randomContactStream;

	public static final BehaviorStream repeatContactStream;

	public static final BehaviorStream familyContactStream;


	static {
		randomContactStream = new PoissonStream(EXPECTED_RANDOM_CONTACTS_PER_DAY);
		repeatContactStream = new PoissonStream(EXPECTED_REPEAT_CONTACTS_PER_DAY);
		familyContactStream = new PoissonStream(EXPECTED_FAMILY_CONTACTS_PER_DAY);
	}


	public static BehaviorStream randomContactStream() {
		return randomContactStream;
	}


	public static BehaviorStream repeatContactStream() {
		return repeatContactStream;
	}


	public static BehaviorStream familyContactStream() {
		return familyContactStream;
	}
}
