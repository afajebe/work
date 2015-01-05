package model;

import java.io.Serializable;


/**
 * This extension of ModelPlaceSeed also contains data on hospital beds.
 */
public class HospitalModelPlaceSeed extends ModelPlaceSeed implements Comparable, Serializable {
	
	/** The number of beds each hospital in this ModelPlace has. */
	public	int[]	hospitalBeds;
	
	/** The total number of beds operated in a ModelPlace. */
	public	int	totalBeds;
	
	public HospitalModelPlaceSeed(int population , int latIndex , int longIndex , int[] numBeds) {
		
		super(population,latIndex,longIndex);

		this.hospitalBeds = numBeds;	
		computeTotalBeds();
	}
	
	
	
	/** Shrink / Expand population and hospital beds by a given fraction. */
	public void rescale(double fraction) {
		this.population *= fraction;
		if(hospitalBeds != null) {
			for (int i = 0 ; i < hospitalBeds.length ; i++) {
				hospitalBeds[i] *= fraction;
			}
		}
		computeTotalBeds();
	}
	
	
	/** Add up all of the bed capacity. */
	private void computeTotalBeds() {
		totalBeds = 0;
		if(hospitalBeds != null) {
			for (int i = 0 ; i < hospitalBeds.length ; i++) {
				totalBeds += hospitalBeds[i];
			}
		}
	}
	
	
	/** Sort the ModelPlaceSkeleton by population, latitude, longitude. */
	public int compareTo(Object o) {
		HospitalModelPlaceSeed other = (HospitalModelPlaceSeed)o;
		
		if(this.population < other.population) {
			return -1;
		} else if(this.population > other.population) {
			return 1;
		} else if(this.latitudeIndex < other.latitudeIndex) {
			return -1;
		} else if(this.latitudeIndex > other.latitudeIndex) {
			return 1;
		} else if(this.longitudeIndex < other.longitudeIndex) {
			return -1;
		} else if(this.longitudeIndex > other.longitudeIndex) {
			return 1;
		} else {
			throw new IllegalStateException("Equal ModelPlaceSkeletons");
		}
	}
}