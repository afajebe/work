package model.disease;


public class Diseases {

	private static Disease modeledDisease = new SwineFlu();
	
	public static Disease disease() {
		return modeledDisease;
	}
}
