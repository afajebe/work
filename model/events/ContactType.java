package model.events;


public enum ContactType {

	FAMILY_CONTACT("family contact", 8.0),
	RANDOM_CONTACT("random contact", 1.0),
	REPEATABLE_CONTACT("repeatable contact", 3.0);

	private final String textValue;

	private final double contactIntimacy;


	private ContactType(String textValue, double intimacy) {
		this.textValue = textValue;
		this.contactIntimacy = intimacy;
	}


	@Override
	public String toString() {
		return textValue;
	}


	public double getIntimacy() {
		return contactIntimacy;
	}
}
