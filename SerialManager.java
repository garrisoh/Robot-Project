import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 

import java.util.Enumeration;

/**
 * A modified version of the sample class taken from http://playground.arduino.cc/Interfacing/Java.
 * This class is used to interface with the arduino via serial communications.  It uses the RXTX java library (gnu).
 * SerialManager is intended to be subclassed to provide custom functionality including response to input events
 * and output communication.  SerialManager provides the basic functionality needed to 
 * establish two-way communication, but must be extended to provide any further functionality.
 * 
 * @author(Haley Garrison)
 * @version(9/14/13)
 */
public class SerialManager implements SerialPortEventListener {
	// The default timeout (in milliseconds) when connecting to a port
	public static final int DEFAULT_TIME_OUT = 100000;
	// The default baud rate
	public static final int DEFAULT_DATA_RATE = 9600;

	private SerialPort serialPort;

	// Reader for input stream
	private BufferedReader input;
	// Output stream to the serial port
	private OutputStream output;
	// Time to block while waiting for port to open
	private int timeOut = DEFAULT_TIME_OUT;
	// Baud rate for serial port
	private int dataRate = DEFAULT_DATA_RATE;

	/**
	 * Constructor - runs initialization with default values for timeOut and dataRate.
	 * 
	 * @param portNames A String array of possible USB port names.  These are computer specific.
	 */
	public SerialManager(String[] portNames) {
		initialize(portNames);
	}
 
	/**
	 * Constructor - runs initialization with custom values for timeOut and dataRate.
	 * 
	 * @param portNames A String array of possible USB port names.  These are computer specific.
	 * @param timeOut Time to wait while opening connection
	 * @param dataRate Rate of communication.  Must match the device to be used.
	 */
	public SerialManager(String[] portNames, int timeOut, int dataRate) {
		this.timeOut = timeOut;
		this.dataRate = dataRate;
		initialize(portNames);
	}

	/**
	 * Getter for input stream reader.
	 * 
	 * @return input 
	 */
	public BufferedReader getInput() {
		return input;
	}
 
	/**
	 * Getter for output stream.
	 * 
	 * @return output - an OutputStream
	 */
	public OutputStream getOutput() {
		return output;
	}
 
	/**
	 * Opens the serial port and sets up I/O streams.
	 * 
	 * @param portNames A String array of possible USB port names.  These are computer specific.
	 */
	public void initialize(String[] portNames) {
		// makes sure that if the serial port was open, it is closed before establishing a new connection
		close();   

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		//First, find the port id's from the given port names
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : portNames) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			// get a serial port, using this class name for the application name
			serialPort = (SerialPort) portId.open(this.getClass().getName(), timeOut);

			// set port parameters
			serialPort.setSerialPortParams(dataRate,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			// an InputStreamReader is decorated with a BufferedReader for better efficiency
			// the stream readers read characters from raw bytes out of the input stream
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add serial event listener
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);

			// sleep to allow port setup to complete on background threads.
			Thread.sleep(2000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This should be called when you stop using the port.
	 * This will prevent port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
			serialPort = null;
		}
	}

	/**
	 * Handle an event on the serial port.  Override to provide custom functionality.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				// or other handling
				String inputLine = input.readLine();
				System.out.println(inputLine);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
 
	/**
	 * Send serial output as an array of bytes.  Override to provide custom functionality.
	 * 
	 * Note: Bytes in Arduino are unsigned values from 0 to 255.  Bytes in Java are signed two's complement values from
	 * -128 to 127.  This corresponds to the char type in Arduino.  Therefore, the data should be received as an array of
	 * chars in Arduino (see http://arduino.cc/en/Serial/ReadBytes or http://arduino.cc/en/Serial/ReadBytesUntil).
	 */
	public synchronized void serialOutput(byte[] toSend) {
		try {
			output.write(toSend);
			output.flush();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}
}