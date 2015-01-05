/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package executables;


import core.InteractiveLargeScaleModel;
import core.SimulationProperties;


public class RunBasicModel {

	public static void main(String[] args) {
		InteractiveLargeScaleModel model = new InteractiveLargeScaleModel(
				SimulationProperties.getDefaultProperties());

		model.beginModel();
	}
}
