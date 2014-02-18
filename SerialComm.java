/**
 * A class for communicating serially with the Arduino
 *
 * @author Haley Garrison
 */
public class SerialComm extends SerialManager {
	
	/**
	 * Constructor - runs initialization with default values for timeOut and dataRate.
	 * 
	 * @param portNames A String array of possible USB port names.  These are computer specific.
	 */
	public SerialComm(String[] portNames) {
		super(portNames);
	}
	
	/**
	 * Constructor - runs initialization with custom values for timeOut and dataRate.
	 * 
	 * @param portNames A String array of possible USB port names.  These are computer specific.
	 * @param timeOut Time to wait while opening connection
	 * @param dataRate Rate of communication.  Must match the device to be used.
	 */
	public SerialComm(String[] portNames, int timeout, int dataRate) {
		super(portNames, timeout, dataRate);
	}
	
	/**
	 * Sends angles serially to the Arduino.
	 */
	public void send(double base, double shoulder, double elbow, double wrist, double grip) {
		// convert doubles to ints and store in an array
		byte[] buffer = new byte[5];
		buffer[0] = mapAngleToByte(base);
		buffer[1] = mapAngleToByte(shoulder);
		buffer[2] = mapAngleToByte(elbow);
		buffer[3] = mapAngleToByte(wrist);
		buffer[4] = mapAngleToByte(grip);
		
		// send the bytes
		serialOutput(buffer);
	}
	
	/**
	 * Converts an angle from 0 to 180 degrees into a single byte.
	 * 
	 * @param angle The angle to convert.  Value will be rounded to an int before conversion.
	 * @return A single byte representing the angle
	 */
	private byte mapAngleToByte(double angle) {
		angle = Utility.map(angle, 0, 180, -128, 127);
		return (byte)Math.round((float)angle);
	}
}
