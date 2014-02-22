ASME Motion Control Robot Arm Project
=============
###Hardware
To run this project, you will need the following hardware:
- Arduino (Uno or Leonardo will do)
- A standard USB cable to connect the Arduino (USB A/B for Uno or USB A to micro B for Leonardo)
- Leap Motion controller
- Robot Arm.  This can be found in the digital circuits lab on 4th floor AEC.
- DC power supply.  The arm should receive 4-6 volts.
- A computer

###Software
In addition to the hardware listed above, you will need to have the following software installed:
- The latest version of Java.  See https://www.java.com/en/download/
- Leap Motion Java SDK.  In order to download this, you must first register as a developer at https://developer.leapmotion.com
- Arduino IDE.  Available at http://arduino.cc/en/Main/Software#.UwjwnHlIi5c
- RXTX.  This should come with the download of the Arduino IDE, but if not, you can find instructions on setting up the library here: http://playground.arduino.cc/Interfacing/Java#.UwjxjXlIi5c
- The code found in this repository.  Download the zip file using the link on the right side.

###Setup
The first thing you will need to do to set up this project is to configure the hardware.  Using a breadboard, you can connect each of the robot's servos to power (red wire) and ground (black or brown wire).  Power should receive the 4-6 volt supply from the DC source, and ground should be the common connection on the supply.  The PWM connections of each of the servos (yellow or white wire) should be connected to a separate row on the breadboard.

To set up the Arduino, first connect it to the computer with the appropriate USB cable.  In the Arduino IDE, open the RobotArduino.ino sketch included in this repo and hit the verify button.  Then, from the menu bar, select the correct Arduino board and the USB port you are using.  Once the board is configured, click the upload button.  With the DC supply turned off, attach the servos' PWM wires to the following pins on the Arduino:
- Base: 11
- Shoulder: 10
- Elbow: 9
- Wrist: 6
- Grip: 5

Also be sure to connect the common ground for the servos to one of the Arduino's GND pins (there may be more than one, any of them is fine).  Do not connect the power supply to the Arduino, the Arduino will get its power from the computer.

Next, plug in the Leap Motion.  If this is the first time using the Leap, you will need to go to http://www.leapmotion.com/ and click on 'setup' to install the Leap daemon.  Make sure that the LeapJava.jar file in the downloaded SDK files is either in the same directory as the other java files downloaded from this repo or in your Java extensions folder (instructions here: https://developer.leapmotion.com/documentation/java/devguide/Sample_Tutorial.html).  The same goes for the RXTX.jar file.

###Running the Program
Make sure that the Leap daemon is running and that the Leap is on and tracking.  Turn the voltage on the supply to about 5 Volts.  In terminal/command prompt, navigate to the project directory:

cd /path/to/directory/Robot-Project/

Once you are in this directory, enter the following commands, replacing \<portname\> with the USB port name you specified in the Arduino IDE (ex. "/dev/tty.usbmodemfa131"):

javac ./*.java

java Main \<portname\>

You're all set!  Move your hand around and watch the robot arm follow you.  When you are finished running the program, close out of the window and turn the power supply back off.

###Sample Video
Click the link below to see the project in action:

http://www.youtube.com/watch?v=PKMMIXXXwFU
