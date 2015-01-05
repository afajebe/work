package util;

import core.InteractiveLargeScaleModel;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Properties;


/**
 * A PeriodicLog is designed to help efficently create log files that display 
 * multiple periodic variables.  For instance, if you created an articial economy
 * model that produced quarterly - unemployment, GDP, S&P500, oil prices, etc..
 * numbers than this object would help write a single file that details all of these
 * variables.
 */
public class PeriodicLog implements Serializable {
	
	private	String[]	variables;
	private	StringHistory[]	logs;
	private Properties	props;

	
	/** Creates a new instance of PeriodicLog */
	public PeriodicLog(String[] variableNames) {
		this.variables = Arrays.copyOf(variableNames , variableNames.length);
		this.logs = new StringHistory[variableNames.length];
		for (int i = 0 ; i < logs.length ; i++) {
			logs[i] = new StringHistory();
		}
	}
	
	
	/** 
	 * Add a new piece of data to the appriopriate variable
	 *
	 * @param data - a peice of data
	 * @param variableName - The "column" this data should go in
	 */
	public void addData(String variableName , String data) {
		for (int i = 0 ; i < variables.length ; i++) {
			if(variables[i].equals(variableName)) {
				logs[i].addDataPoint(data);
				return;
			}
		}
		throw new IllegalArgumentException("input :: " + variableName + " does not match a variable being tracked");
	}
	
	
	/** 
	 * Incorporate another log into this log.  The properties object of this log is not changes. 
	 * 
	 * @param addThisLog - Another PeriodicLog to add
	 * 
	 * @throws IllegalArgumentException if a variable in the new log already exists in this log. 
	 */
	public void incorporateLog(PeriodicLog addThisLog) {
		
		//check for duplicate variable values
		for (int i = 0; i < variables.length; i++) {
			for (int j = 0; j < addThisLog.variables.length; j++) {
				if(variables[i].equals(addThisLog.variables[j])) {
					throw new IllegalArgumentException("" +
						"When merging logs all varaibles must be unique :: " + 
						variables[i] + " exists in both logs"
						);
				}				
			}
		}
		
		//add space to variables - while copying		
		//the array we will replace "variables" with		
		String[] tempVariables = Arrays.copyOf(variables, variables.length + addThisLog.variables.length);				
		//the array we will replace "logs" with
		StringHistory[] tempLogs = Arrays.copyOf(logs , tempVariables.length);
		
		//copy additional values
		int shift = variables.length;
		for (int i = 0; i < addThisLog.variables.length; i++) {
			tempVariables[i + shift] = addThisLog.variables[i];
			tempLogs[i + shift] = addThisLog.logs[i];
		}		
		
		//replace old copies with new copies
		this.variables = tempVariables;
		this.logs = tempLogs;		
	}
	
	
	/** Return a copy of the variables this object tracks. */
	public String[] listVariables() {
		return Arrays.copyOf(variables, variables.length);
	}
	
	
	/** 
	 * Extract the values stored under a specific heading. 
	 * 
	 * @throws IllegalArgumentException - If variable doesn't match a variable currently being tracked.
	 */
	public String[] extract(String variable) {
		
		int index = -1;
		for (int i = 0; i < variables.length; i++) {
			if(variables[i].equals(variable)) {
				index = i;
				break;
			}
		}
		if(index == -1) {
			throw new IllegalArgumentException("Variable :: " + variable + " is not found");
		} else {
			return logs[index].getData();
		}	
	}
	
	
	/** Provides access to this log's properties object. */
	public String getProperty(String key) {
		return props.getProperty(key);
	}
	
	
	/** 
	 * Saves this Object twice.  Once as a ".txt" file and a second time as a serialized object.
	 * Write the compiled data to a file. 
	 * 
	 * @param fileName - The prefix of the desired ".txt" and ".slg" files
	 */
	public void toFile(String fileName) {
		try {
			Utility.serialize(this, InteractiveLargeScaleModel.OUTPUT_DIRECTORY + fileName + ".slg");
			Utility.writeToNewFile(InteractiveLargeScaleModel.OUTPUT_DIRECTORY + fileName + ".txt", this.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}


	/** 
	 * Load a properties file to attach to this log. 
	 * 
	 * @param props - A properties file to absorb
	 */
	public void setProperties(Properties props) {
		this.props = props;
	}
	
		
	/** Push all logs to a single String. */
	public String toString() {
		
		StringBuffer buffer = new StringBuffer();
		for(int i = 0 ; i < logs.length ; i++) {
			buffer.append(variables[i] + "\t" + logs[i].toString() + "\n");
		}
		return buffer.toString();
	}	
}
