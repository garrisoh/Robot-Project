/*
class to control a 4-axis robot arm with an Arduino through Java.

Issues:
	1.) The shoulder motor doesn't always move to the correct angle. It appears to be off by a different amount around 90 degrees than it is around 0 degrees, so it's hard to correct for it.
	2.) The grip linkages do not keep the jaws parallel. That means the separation at the tip is different from the separation at the back, and also that it will make less contact with objects. It also complicates the kinematics.
	3.) The motors sometimes don't move to exactly the right position. This is mostly apparent with the base, which is often off by one degree. Thus, the coordinates need to be fudged at times.
	4.) When the gripper opens/closes, it changes the length from wrist to tip, so the angles for a certain coordinate will change if the grip is opened or closed. The coordinates used to grab a block may not work for putting it back.
	5.) The robot cannot sense the actual angle of a motor, only what angle the motor SHOULD be at. So if it gets stuck, it won't know.

Suggestions:
	1.) Try to avoid letting the grip or anything it holds touch the board the arm is mounted on. Since there's a tendency for movements to not be fluid, it's possible for it dip down in the middle of a movement and hit the board.
*/


import com.leapmotion.leap.*;

class Arm extends Listener
{
	// leap motion max and min coordinates
	private static final double MAX_X = 200;
	private static final double MAX_Y = 200;
	private static final double MAX_Z = 400;
	private static final double MAX_R = 100;
	private static final double MIN_X = -200;
	private static final double MIN_Y = -200;
	private static final double MIN_Z = 0;
	private static final double MIN_R = 52;

	// robot arm max and min coordinates
	private static final double MAX2_X = 20;
	private static final double MAX2_Y = 30;
	private static final double MAX2_Z = 10;
	private static final double MAX2_R = 6;
	private static final double MIN2_X = -20;
	private static final double MIN2_Y = 10;
	private static final double MIN2_Z = -7.5;
	private static final double MIN2_R = 0;
    
	public static void main(String[] args)
	{
		Arm arm = new Arm(new SerialComm(args));
		//arm.stackBlocks();
		double[] angles = arm.findAnglesConstantPitch(new double[]{17,21,0}, 10, -55);
		System.out.printf("%f, %f, %f, %f", angles[0], angles[1], angles[2], angles[3]);
		System.out.println(arm.safetyCheckAxisAngles(angles));
		
		System.out.println(arm.safetyCheckAxisAngles(new double[]{131323,1192, 440,-1111}));
	}
	
    /**
	 * Calculates angles and send to the arduino
	 *
	 * @param c Leap controller
	 */
	public void onFrame(Controller c) {
		// Check that there are hands in the field
		HandList hands = c.frame().hands();
		if (hands.count() <= 0) {
			return;
		}
		
		// Check that the hand is open
		Hand hand = hands.rightmost();
		if (hand.fingers().count() < 2) {
			return;
		}
        
		// Get Leap coordinates
		Vector position = hand.palmPosition();
		double grip = hand.sphereRadius();
        double leapX = position.getX();
        double leapY = position.getY();
        double leapZ = position.getZ();
        
        double robotX = Utility.map(leapX, MIN_X, MAX_X, MIN2_X, MAX2_X);
        double robotY = Utility.map(leapY, MIN_Y, MAX_Y, MIN2_Y, MAX2_Y);
        double robotZ = Utility.map(leapZ, MIN_Z, MAX_Z, MIN2_Z, MAX2_Z);
        double robotGrip = Utility.map(grip, MIN_R, MAX_R, MIN2_R, MAX2_R);
        
        set(robotX, robotY, robotZ, robotGrip);
	}
	
	
	double[] axisAngles;

	double[] baseAxisRange;
	double[] shoulderAxisRange = {45,135};    // not the actual limits of axis, but going lower risks slamming into the ground
	double[] elbowAxisRange = {-140,0};
	double[] wristAxisRange = {-90, 90};
	double[] gripAxisRange = {30, 90};
	double[][] axisRanges = {this.baseAxisRange, this.shoulderAxisRange, this.elbowAxisRange, this.wristAxisRange, this.gripAxisRange};

	
	SerialComm comm;

	// adjustments between angle of motor and angle of axis. First number is due to the robot's design. Second is error correction (motors are not installed with perfect orientation).
	int baseAxisToMotorAdjustment = + 90 + 4;
	int shoulderAxisToMotorAdjustment = 0 + 14;
	int elbowAxisToMotorAdjustment = + 180 - 2;
	int wristAxisToMotorAdjustment = + 90;
	int gripAxisToMotorAdjustment = + 45 - 2;
	int[] axisToMotorAdjustments = {this.baseAxisToMotorAdjustment, this.shoulderAxisToMotorAdjustment, this.elbowAxisToMotorAdjustment, this.wristAxisToMotorAdjustment, this.gripAxisToMotorAdjustment};

	double segment1Length = 15.25;	// in cm
	double segment2Length = 12;	// in cm



	// Function to pass in info from the Leap.
	void set(double x, double y, double z, double gripSeparation)
	{
		this.gripControl(gripSeparation);	// Set the grip separation.
		double gripLength = this.getCurrentGripInfo().gripLength;
		double pitchAngle = this.getAxisAngle("shoulder") + this.getAxisAngle("elbow") + this.getAxisAngle("wrist");
		double[] newCoordinates = {x, y, z};
		double[] newAngles = this.findAnglesConstantPitch(newCoordinates, gripLength, pitchAngle);
		if (this.safetyCheckAxisAngles(newAngles))
			this.setAxisAnglesOptimized(newAngles);	// Move to specified position
		else
			System.out.println("invalid position specified");
	}


	//Constructor. Can pass in host name and network port if different from the usual.
	//Arm(host, networkPort)
	Arm(SerialComm serialComm)
	{
		this.axisAngles = new double[]{0, 77, -82, -50, 75};	// Stores the current axis angles. Initial values determine where the robot starts.

		this.baseAxisRange = new double[]{-90, 90};
		this.shoulderAxisRange = new double[]{45,135};    // not the actual limits of axis, but going lower risks slamming into the ground
		this.elbowAxisRange = new double[]{-140,0};
		this.wristAxisRange = new double[]{-90, 90};
		this.gripAxisRange = new double[]{35, 90};
		this.axisRanges = new double[][]{this.baseAxisRange, this.shoulderAxisRange, this.elbowAxisRange, this.wristAxisRange, this.gripAxisRange};

		this.comm = serialComm;

		// adjustments between angle of motor and angle of axis. First number is due to the robot's design. Second is error correction (motors are not installed with perfect orientation).
		this.baseAxisToMotorAdjustment = + 90 + 4;
		this.shoulderAxisToMotorAdjustment = 0 + 14;
		this.elbowAxisToMotorAdjustment = + 180 - 2;
		this.wristAxisToMotorAdjustment = + 90;
		this.gripAxisToMotorAdjustment = + 45 - 2;
		this.axisToMotorAdjustments = new int[]{this.baseAxisToMotorAdjustment, this.shoulderAxisToMotorAdjustment, this.elbowAxisToMotorAdjustment, this.wristAxisToMotorAdjustment, this.gripAxisToMotorAdjustment};

		this.segment1Length = 15.25;	// in cm
		this.segment2Length = 12;	// in cm
	}

	void onReady()
	{
		// Note: if the range of the motor is set here, BreakoutJS will reassign the range to whatever the values are, ie 0-180 (actual range of physical motor) becomes -90-90, and saying motor.angle = 0 will set it to what would have previously been 90. Should make use of that, after getting things working.
		/*
		this.baseMotor = new Servo(this.ardy, this.ardy.getDigitalPin(11) );
		this.shoulderMotor = new Servo(this.ardy, this.ardy.getDigitalPin(10) );
		this.elbowMotor = new Servo(this.ardy, this.ardy.getDigitalPin(9) );
		this.wristMotor = new Servo(this.ardy, this.ardy.getDigitalPin(6) );
		this.gripMotor = new Servo(this.ardy, this.ardy.getDigitalPin(5) );

		this.motors = {this.baseMotor, this.shoulderMotor, this.elbowMotor, this.wristMotor, this.gripMotor};

*/
		// Go to initial position
		if (!this.safetyCheckAxisAngles(this.axisAngles))
		{
			System.out.println("invalid initial angles.");
			return;
		}
		for ( int i = 0; i < 5; ++i)
		{
	//		this.motors[i].angle = this.axisAngles[i] + this.axisToMotorAdjustments[i];
		}

	}





	// returns the angles needed to reach given coordinates and pitch.
	double[] findAnglesConstantPitch(double[] coordinates, double gripLength, double pitchAngle)
	{
		double x = coordinates[0];
		double y = coordinates[1];
		double z = coordinates[2];
		double newHorizontalLength = Math.sqrt(Math.pow(x,2) + Math.pow(y,2));
		double totalDistance = Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));

		// we'll keep the gripper oriented the same way with respect to the xy plane
		double verticalGripLength = gripLength * Utility.sind( pitchAngle);
		double horizontalGripLength = gripLength * Utility.cosd( pitchAngle);

		// by keeping the orientation of the gripper the same, we can solve for the position of the wrist axis instead.
		double shoulderToWristLength = Math.sqrt( Math.pow((newHorizontalLength - horizontalGripLength),2) + Math.pow((z - verticalGripLength),2));
		double shoulderToWristAngle = Utility.atand( (z - verticalGripLength) / (newHorizontalLength - horizontalGripLength) );
		if ( shoulderToWristAngle < 0 ) shoulderToWristAngle = shoulderToWristAngle + 180;

		/*
	from Law of Cosines
	c^2 = a^2 + b^2 - 2 * a * b * cosd(C)
	2 * a * b * cosd(C) = a^2 + b^2 - c^2
	cosd(C) = ( a^2 + b^2 - c^2 ) / ( 2 * a * b )
	C = acosd( ( a^2 + b^2 - c^2 ) / ( 2 * a * b ) )
		 */
		double C = Utility.acosd( ( Math.pow(this.segment1Length,2) + Math.pow(this.segment2Length,2) - Math.pow(shoulderToWristLength,2) ) / ( 2 * this.segment1Length * this.segment2Length ) );
		// B = acosd( ( a^2 + c^2 - b^2 ) / ( 2 * a * c ) )
		double B = Utility.acosd( ( Math.pow(this.segment1Length,2) + Math.pow(shoulderToWristLength,2) - Math.pow(this.segment2Length,2) ) / ( 2 * this.segment1Length * shoulderToWristLength ) );

		double newElbowAngle =  C - 180;
		double newShoulderAngle = shoulderToWristAngle + B;

		double newWristAngle = pitchAngle - newElbowAngle - newShoulderAngle;

		double newBaseAngle = Utility.atand(x/y);

		
		double[] angles = {newBaseAngle, newShoulderAngle, newElbowAngle, newWristAngle};
		return angles;
	}

	// returns the coordinates corresponding to the given angles and gripLength
	double[] findCoordinates(double[] angles, double gripLength)
	{
		double baseAngle = angles[0];
		double shoulderAngle = angles[1];
		double elbowAngle = angles[2];
		double wristAngle = angles[3];
		double z = this.segment1Length * Utility.sind(shoulderAngle) + this.segment2Length * Utility.sind(elbowAngle + shoulderAngle) + gripLength * Utility.sind(wristAngle + elbowAngle + shoulderAngle);
		double horizontalLength = this.segment1Length * Utility.cosd(shoulderAngle) + this.segment2Length * Utility.cosd(elbowAngle + shoulderAngle) + gripLength * Utility.cosd(wristAngle + elbowAngle + shoulderAngle);
		double y = horizontalLength * Utility.cosd(baseAngle);
		double x = horizontalLength * Utility.sind(baseAngle);
		double[] coordinates = {x,y,z};
		return coordinates;
	}

	// checks to ensure the given angles are within the arm's limits
	boolean safetyCheckAxisAngles(double[] angles)
	{
		for ( int i = 0; i < angles.length; ++i)
		{
			if ( angles[i] < this.axisRanges[i][0] || angles[i] > this.axisRanges[i][1] )
			{
				return false;
			}
		}
		return true;
	}

	class GripInfo
	{
		public double gripSeparation;
		public double gripLength;
		public double gripPadAngle;
		public GripInfo(double separation, double length, double padAngle)
		{
			this.gripSeparation = separation;
			this.gripLength = length;
			this.gripPadAngle = padAngle;
		}
	}

	// calculates the grip separation and distance from wrist axis to grip tip, given the angle of the top linkage.
	// returns a structure containing separation, length, and the angle of the gripper pads.
	GripInfo getGripInfo(double topLinkageAngle)
	{
		// measurements are in cm. Sorry for the confusing names/descriptions.
		double topLinkageLength = 5.2;	// length of linkage attached to gear
		double bottomLinkageLength = 4.65;	//length of each bottom linkage
		double linkageSeparationGripper = 2.7;	// distance between where the two linkages attach on the gripper side
		double gripperBendDistance = 4.7;	// distance from where the top linkage attaches to where the piece bends
		double bendAngle = 30;	// angle that the grip bends between the pad and where it attaches to the linkages.
		double yBendToTip = 6.9;	// distance from the bend to the tip of the gripper (measured in line with the two screws holding the grip together.
		double gripInteriorStep = .8;	// distance from the inner part of the grip (with pad) to the line between the two screws.
		double topLinkageSeparation = 2.75;	// distance between the axes of the two gears (or the ends of the two top linkages)
		double bottomLinkageSeparation = 1;	// distance between the two bottom linkages
		double topToBottomLinkDistanceY = 2.4;	// distance between the top and bottom linkages on the main body (motor side), measured parallel to arm
		double topToBottomLinkDistanceX = (topLinkageSeparation - bottomLinkageSeparation) / 2;	// distance between the top and bottom linkages on the main body (motor side), measured perpendicular to arm
		double topToBottomLinkDistanceTotal = Math.sqrt(Math.pow(topToBottomLinkDistanceX,2) + Math.pow(topToBottomLinkDistanceY,2) );	// distance between axes of top  and bottom linkages on the arm end
		double topToBottomLinkAngle = Utility.atand(topToBottomLinkDistanceX/topToBottomLinkDistanceY);	// angle between anchor points of the top and bottom linkages
		double wristToGripGearLength = 4.7;	// length from the wrist axis to the center of the gears

		/*
	figure out the angle of the gripPlane (plane along the inside of the gripper)
	c^2 = a^2 + b^2 - 2 * a * b * cosd(C)		// Law of Cosines
	C = acosd( ( a^2 + b^2 - c^2 ) / ( 2 * a * b ) ) // derived from Law of Cosines


	A ---------- B
	 \	         \		illustration attempt. ABD = topLinkageAngle + topToBottomLinkAngle, AB = topLinkageLength,
	  \           \		AC = linkageSeparationGripper, CD = bottomLinkageLength,
	    -----______\		BD = topToBottomLinkDistanceTotal
	  C            D

		 */

		// AD^2 = AB^2 + BD^2 - 2 * AB * BD * cosd(ABD)
		double AD = Math.sqrt(Math.pow(topLinkageLength,2) + Math.pow(topToBottomLinkDistanceTotal,2) - 2*topLinkageLength*topToBottomLinkDistanceTotal*Utility.cosd(topLinkageAngle+topToBottomLinkAngle) );

		// BD^2 = AB^2 + AD^2 - 2 * AB * AD * cosd(BAD)
		// BAD = acosd( ( AB^2 + AD^2 - BD^2 ) / ( 2 * AB * AD ) )
		double BAD = Utility.acosd( ( Math.pow(topLinkageLength,2) + Math.pow(AD,2) - Math.pow(topToBottomLinkDistanceTotal,2) ) / ( 2 * topLinkageLength * AD ) );

		// DAC = acosd( ( AC^2 + AD^2 - CD^2 ) / ( 2 * AC * AD ) )
		double DAC = Utility.acosd( ( Math.pow(linkageSeparationGripper,2) + Math.pow(AD,2) - Math.pow(bottomLinkageLength,2) ) / ( 2 * linkageSeparationGripper * AD ) );

		double BAC = BAD + DAC;

		double gripPadAngle = ( topLinkageAngle - ( 180 - BAC ) + bendAngle );

		// x and y coordinates of a gripper tip, with (0,0) as the center of the gear the top linkage is attached to
		double y = (yBendToTip+gripperBendDistance*Utility.cosd(bendAngle))*Utility.cosd(gripPadAngle) + (gripInteriorStep+gripperBendDistance*Utility.sind(bendAngle))*Utility.sind(gripPadAngle) + topLinkageLength*Utility.cosd(topLinkageAngle);
		double x = (yBendToTip+gripperBendDistance*Utility.cosd(bendAngle))*Utility.sind(gripPadAngle) + (gripInteriorStep+gripperBendDistance*Utility.sind(bendAngle))*Utility.sind(gripPadAngle-90) + topLinkageLength*Utility.sind(topLinkageAngle);

		// these are for debugging purposes.
		double xAtTopLink = topLinkageLength*Utility.sind(topLinkageAngle);
		double sepAtTopLink = 2*xAtTopLink + topLinkageSeparation;
		double xAtBend = xAtTopLink + (gripperBendDistance*Utility.cosd(bendAngle))*Utility.sind(gripPadAngle) + (gripperBendDistance*Utility.sind(bendAngle) )*Utility.sind(gripPadAngle-90);
		double sepAtBend = 2*xAtBend + topLinkageSeparation;
		double xAtTip = (yBendToTip + gripperBendDistance*Utility.cosd(bendAngle))*Utility.sind(gripPadAngle) + (gripInteriorStep+gripperBendDistance*Utility.sind(bendAngle))*Utility.sind(gripPadAngle-90) + topLinkageLength*Utility.sind(topLinkageAngle);

		double currentSeparation = 2*x + topLinkageSeparation;
		double gripLength = y + wristToGripGearLength;

		GripInfo info = new GripInfo(currentSeparation, gripLength, gripPadAngle);
		return info;
	}

	// translates verbal name for axis into index for the arrays storing info (axisAngles, axisToMotorAdjustments, etc.)
	int getIndexOfAxis(String axisName)
	{
		if (axisName == "base" )
			return 0;
		else if (axisName == "shoulder")
			return 1;
		else if (axisName == "elbow")
			return 2;
		else if (axisName == "wrist")
			return 3;
		else if (axisName == "grip")
			return 4;
		else Utility.error("unrecognized axis");
		return 0;	// TODO: should probably throw an exception instead.
	}



	double[] getCurrentCoordinates()
	{
		double gripLength = this.getCurrentGripInfo().gripLength;
		double[] coordinates = this.findCoordinates(this.axisAngles, gripLength);
		return coordinates;
	}

	double getAxisAngle(String axisName)
	{
		int axis = getIndexOfAxis(axisName);
		//if (axis == undefined)
		//	return;
		double angle = this.axisAngles[axis];
		return angle;
	}


	// enhanced function, only sets the angles that actually change, and doesn't use servoRead. Much faster.
	void setAxisAnglesOptimized(double[] newAngles)
	{
		if (!this.safetyCheckAxisAngles(newAngles))
		{
			System.out.println("invalid angles passed to setAxisAnglesOptimized. These should have been checked earlier.");
			for(int i=0; i<newAngles.length; i++) {
				System.out.println(newAngles[i]);
			}
			return;
		}
		double[] servoAngles = new double[5];
		for(int i = 0; i < newAngles.length; ++i)
		{
			servoAngles[i] = newAngles[i] + axisToMotorAdjustments[i];
		}
		for(int i = newAngles.length; i < 5; ++i)
			servoAngles[i] = axisAngles[i] + axisToMotorAdjustments[i];
		comm.send(servoAngles[0], servoAngles[1], servoAngles[2], servoAngles[3], servoAngles[4]);
		/*
		for ( int i = 0; i < newAngles.length; ++i)
		{
			if ( Utility.int16( newAngles[i] ) != this.axisAngles[i] )
			{
				// only write to the servo if we're actually changing the angle
				//this.ardy.servoWrite(this.ports[i], int16(newAngles[i] + this.axisToMotorAdjustments[i]));
		//		this.motors[i].angle = newAngles[i] + this.axisToMotorAdjustments[i];
				this.axisAngles[i] = Utility.int16( newAngles[i] );	// rather than read from the servo (which takes a lot of time), just set the value to whatever we told the servo to go to.
				// note that this could cause rounding issues if the axisToMotorAdjustment is a non-integer.
			}
		}*/
	}


	GripInfo getCurrentGripInfo()
	{
		return this.getGripInfo(this.getAxisAngle("grip"));
	}

	void gripControl(double targetSeparation)
	{
		gripControl(targetSeparation, false);
	}
	// moves grip to specified separation
	void gripControl(double targetSeparation, boolean relative)
	{
		double currentSeparation = this.getCurrentGripInfo().gripSeparation;
		if (relative)
			targetSeparation = currentSeparation + targetSeparation;
		int direction;
		if ( targetSeparation - currentSeparation > 0 )
			direction = 1;
		else
			direction = -1;
		double angle = this.getAxisAngle("grip");
		// we could use inverse kinematics for this, but it's a lot easier to just open/close the grip until we reach our target.
		while ( (targetSeparation-currentSeparation)*direction > 0 )
		{
			angle = angle + direction;	// advance closer to target by one degree
			if ( angle < this.gripAxisRange[0] || angle > this.gripAxisRange[1] )
			{
				Utility.error("grip angle out of range: %f", angle);
				return;
			}
	//		this.gripMotor.angle = Utility.int16(angle+this.gripAxisToMotorAdjustment);
			this.axisAngles[this.getIndexOfAxis("grip")] = angle;	// update the grip angle
			currentSeparation = this.getCurrentGripInfo().gripSeparation;
		}
	}

	
	
	/// UNUSED
	
	
	
	/* allows a single axis to be easily set from the command line.
	axisName-- string with name of axis to be moved.
	angle-- the angle to move the axis to.
	relative-- bool. If set to true, it means the axis should move BY the given angle amount, not TO the angle. Default is false.
	safetyRangeOverride-- bool. Set this to true if you need to move an axis outside its range. Use with caution. Default is false.
	 */
	void move(String axisName, double angle, boolean relative, boolean safetyRangeOverride)
	{
		/*if (relative == undefined)
			relative = false;
		if (safetyRangeOverride == undefined)
			safetyRangeOverride = false;
*/
		int i = this.getIndexOfAxis(axisName);
		//if (i == undefined)
		//	return;

		if (relative)
			// if set to relative, it means the arm should be moved by the specified amount.
			angle = angle + this.axisAngles[i];

		// check to ensure we're within bounds. The checks for unreal and infinity shouldn't be needed, but are there just in case.
		if ( ( !safetyRangeOverride && ( angle < this.axisRanges[i][0] || angle > this.axisRanges[i][1] ) ))
		{
			Utility.error("invalid position for %s: %f", axisName, angle);
			return;
		}

	//	this.motors[i].angle = angle + this.axisToMotorAdjustments[i];
		this.axisAngles[i] = angle;
	}



	//stacks four blocks at (27,0)
	void stackBlocks()
	{
		double openSep = 4;	// how far apart the grip should be when "open" (not holding a block)
		double closeSep = 2;	// how far apart the grip should be when "closed" (holding a block)
		// for more pressure, specify a smaller separation

		this.gripControl(openSep);	// make sure the grip is open first

		// first block
		this.moveTo(new double[]{17,21,0}, -55);	// position grip above block
		this.moveStraightTo(new double[]{17,21,-6}, -55.0);	// drop down
		this.gripControl(closeSep);	// grab block
		this.moveStraightTo(new double[]{17,21,0},-55);	// go up
		this.moveTo(new double[]{0,28,0}, -55);	// move to above the stack
		this.moveStraightTo(new double[]{0,28,-5.5}, -55.0);	// drop down
		this.gripControl(openSep);	// release block
		this.moveStraightTo(new double[]{0,28,0},-55.0);	// go up

		// second block
		this.moveTo(new double[]{10,25,0},-55.0);	// position grip above block
		this.moveStraightTo(new double[]{10,25,-6}, -55.0);	// drop down
		this.gripControl(closeSep);	// grab block
		this.moveStraightTo(new double[]{10,25,0},-55.0);	// go up
		this.moveTo(new double[]{0,27,0},-55);	// move to above the stack
		this.moveStraightTo(new double[]{0,27,-4},-55.0);	// drop down
		this.gripControl(openSep);	// release block
		this.moveStraightTo(new double[]{0,27,2},-55.0);	// go up

		// third block
		this.moveTo(new double[]{-9.25,26,2},-55.0);	// position grip above block
		this.moveStraightTo(new double[]{-9.25,26,-6}, -55.0);	// drop down
		this.gripControl(closeSep);	// grab block
		this.moveStraightTo(new double[]{-9.25,26,2},-55.0);	// go up
		this.moveTo(new double[]{1,28.5,2},-55);	// move to above the stack
		this.moveStraightTo(new double[]{1,28.5,-0.5},-55.0);	// drop down
		this.gripControl(openSep);	// release block
		this.moveStraightTo(new double[]{1,27,3},-55.0);	// go up

		// fourth block
		this.moveTo(new double[]{-12,12.5,3},-60.0);	// position grip above block
		this.moveStraightTo(new double[]{-12,12.5,-7},-60.0);	// drop down
		this.gripControl(closeSep);	// grab block
		this.moveStraightTo(new double[]{-12,12.5,-7},-55.0);
		this.moveStraightTo(new double[]{-12,12.5,5},-50.0);	// go up
		this.moveTo(new double[]{1,29,5},-50.0);	// move to above the stack
		this.moveStraightTo(new double[]{1,29,2.5},-50.0);	// drop down
		this.gripControl(openSep);	// release block
		this.moveStraightTo(new double[]{1,29,6},-45.0);	// go up

	}

	// unstacks the blocks and returns them to their original positions.
	void unstackBlocks()
	{
		double openSep = 4;	// how far apart the grip should be when "open" (not holding a block)
		double closeSep = 2;	// how far apart the grip should be when "closed" (holding a block)
		// for more pressure, specify a smaller separation

		this.gripControl(openSep);

		// fourth block (top)
		this.moveTo(new double[]{0,28,6},-50);	// move to above the stack
		this.moveTo(new double[]{0,28.5,2.75},-50);	// drop down
		this.gripControl(closeSep);	// grab block
		this.moveTo(new double[]{0,29,6},-45);	// go up
		this.moveTo(new double[]{-12,12.5,5},-50);	// position grip above the mark
		this.moveTo(new double[]{-12,12.5,-7},-60);	// drop down
		this.gripControl(openSep);	// release block
		this.moveTo(new double[]{-12,12.5,3},-60);	// go up

		// third block
		this.moveTo(new double[]{0,27,3},-55);	// move to above the stack
		this.moveTo(new double[]{0,28,-0.5},-55);	// drop down
		this.gripControl(closeSep);	// grab block
		this.moveTo(new double[]{0,28,2.5},-55);	// go up
		this.moveTo(new double[]{-9.8,25.5,2},-55);	// position grip above the mark
		this.moveTo(new double[]{-9.8,25.5,-6.5}, -55);	// drop down
		this.gripControl(openSep);	// release block
		this.moveTo(new double[]{-9.8,25.5,2},-55);	// go up

		// second block
		this.moveTo(new double[]{0,28,2},-55);	// move to above the stack
		this.moveTo(new double[]{0,28,-3.5},-55);	// drop down
		this.gripControl(closeSep);	// grab block
		this.moveTo(new double[]{0,28,0},-55);	// go up
		this.moveTo(new double[]{10,25,0},-55);	// position grip above the mark
		this.moveTo(new double[]{10,25,-7}, -55);	// drop down
		this.gripControl(openSep);	// release block
		this.moveTo(new double[]{10,25,0},-55);	// go up

		// first block (bottom)
		this.moveTo(new double[]{0,28,0},-55);	// move to above the stack
		this.moveTo(new double[]{0,28,-5.5}, -55);	// drop down
		this.gripControl(closeSep);	// grab block
		this.moveTo(new double[]{0,28,0},-55);	// go up
		this.moveTo(new double[]{17,21,0},-55);	// position grip above the mark
		this.moveTo(new double[]{17,21,-6}, -55);	// drop down
		this.gripControl(openSep);	// release block
		this.moveTo(new double[]{17,21,0}, -55);	// go up

	}


	void moveTo(double[] coordinates)
	{
		double pitchAngle = this.getAxisAngle("shoulder") + this.getAxisAngle("elbow") + this.getAxisAngle("wrist");
		boolean moveEvenly = false;
		moveTo(coordinates, pitchAngle, moveEvenly);
	}
	void moveTo(double[] coordinates, double pitchAngle)
	{
		boolean moveEvenly = false;
		moveTo(coordinates, pitchAngle, moveEvenly);
	}
	// moves the tip of the gripper to the given coordinates. Can specify a new pitch, or leave blank to use current one.
	void moveTo(double[] coordinates, double pitchAngle, boolean moveEvenly)	
	{
		double gripLength = this.getCurrentGripInfo().gripLength;
		
		double[] newAngles = this.findAnglesConstantPitch(coordinates, gripLength, pitchAngle);
		if (!this.safetyCheckAxisAngles(newAngles))
		{
			Utility.error("invalid angles:\n%f     %f    %f     %f\nTrying to reach point (%f, %f, %f)", newAngles[0], newAngles[1], newAngles[2], newAngles[3], coordinates[0], coordinates[1], coordinates[2]);
			return;
		}
		if (moveEvenly)
			this.moveAxesAtSpeedEvenly(newAngles);
		else
			this.moveAxesAtSpeed(newAngles);
		//this.setAxisAnglesOptimized(newAngles);
	}
	void moveToInches(double[] coordinates, double pitchAngle)
	{
		this.moveTo(Utility.arrayMultiplication(coordinates, 2.54), pitchAngle);
	}


	void moveAxesAtSpeedEvenly(double[] targetAngles)
	{
		double speed = 50;	// degrees per second
		double maxDegreeStep = 1;
		moveAxesAtSpeedEvenly(targetAngles, speed, maxDegreeStep);
	}
	void moveAxesAtSpeedEvenly(double[] targetAngles, double speed)
	{
		double maxDegreeStep = 1;
		moveAxesAtSpeedEvenly(targetAngles, speed, maxDegreeStep);
	}
	/**
	Moves the arm at the specified speed (in degrees/sec), spreading each axis's movement over the whole period.
	Speed of motors (how fast they move to a new position) is 300 degrees/sec for elbow, 428.5 for shoulder, 333 for wrist and grip
	maxDegreeStep is the maximum number of degrees to move an axis in one step.
	 */
	void moveAxesAtSpeedEvenly(double[] targetAngles, double speed, double maxDegreeStep)
	{
		
		double period = 1 / speed;	// seconds per degree
	
		//targetAngles = double(int16(targetAngles));
		boolean thereYet = false;
		double[] currentAngles = this.axisAngles;
		double[] degreeStep = new double[currentAngles.length];	// the number of degrees to move each axis
		if ( targetAngles.length < currentAngles.length )	// if not all angles are specified, remaining angles should be current angles
		{
			for ( int i = targetAngles.length; i < currentAngles.length; ++i)
				targetAngles[i] = currentAngles[i];
		}
		for ( int i = 0; i < targetAngles.length; ++i)
		{
			degreeStep[i] = Math.abs(targetAngles[i] - currentAngles[i] ) / Utility.max(Utility.absArray(Utility.arraySubtraction(targetAngles, currentAngles) ) ) * maxDegreeStep;
		}

		while (!thereYet)
		{
			double[] nextAngles = currentAngles;		//preallocating, and ensuring that if we don't assign an axis, it will stay at the current value.
			thereYet = true;
			for ( int i = 0; i < targetAngles.length; ++i)	// 
			{
				int direction;
				if ( targetAngles[i] - currentAngles[i] > 0 )
					direction = 1;
				else if ( targetAngles[i] - currentAngles[i] < 0 )
					direction = -1;
				else
					// we're done with this axis. Move to the next one
					continue;
				thereYet = false;	// if we haven't skipped this with continue, it means we haven't reached this target angle yet, so we're not done yet.
				if ( Math.abs(targetAngles[i] - currentAngles[i]) > degreeStep[i] )
					nextAngles[i] = currentAngles[i] + degreeStep[i]*direction;
				else
					// the degreeStep is larger than the distance we have left.
					nextAngles[i] = targetAngles[i];
			}
			this.setAxisAnglesOptimized(nextAngles);
			//sleep(period*degreeStep*1000);
			try
			{
				Thread.sleep((long) (period*Utility.max(degreeStep)*1000));
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			currentAngles = nextAngles;	// updating the angles.
		}
	}

	
	void moveAxesAtSpeed(double[] targetAngles)
	{
		double speed = 50;	// degrees per second
		double degreeStep = 1;
		moveAxesAtSpeed(targetAngles, speed, degreeStep);
	}
	void moveAxesAtSpeed(double[] targetAngles, double speed)
	{
		double degreeStep = 1;
		moveAxesAtSpeed(targetAngles, speed, degreeStep);
	}
	// moves the arm at the specified speed (in degrees/sec)
	void moveAxesAtSpeed(double[] targetAngles, double speed, double degreeStep)
	{
		// speed of motors (how fast they move to a new position) is 300 degrees/sec for elbow, 428.5 for shoulder, 333 for wrist and grip

		double period = 1 / speed;	// seconds per degree
		
		//targetAngles = Utility.int16(targetAngles);
		boolean thereYet = false;

		while (!thereYet)
		{
			double[] currentAngles = this.axisAngles;
			double[] nextAngles = new double[currentAngles.length];
			for (int i = 0; i < this.axisAngles.length; ++i)
				nextAngles[i] = this.axisAngles[i];
			thereYet = true;
			for ( int i = 0; i < targetAngles.length; ++i)
			{
				int direction;
				if ( targetAngles[i] - currentAngles[i] > 0 )
					direction = 1;
				else if ( targetAngles[i] - currentAngles[i] < 0 )
					direction = -1;
				else
					// we're done with this axis. Move to the next one
					continue;
				thereYet = false;	// if we haven't skipped this with continue, it means we haven't reached all target angles yet.
				if ( Math.abs(targetAngles[i] - currentAngles[i]) > degreeStep )
					nextAngles[i] = currentAngles[i] + degreeStep*direction;
				else
					// the degreeStep is larger than the distance we have left.
					nextAngles[i] = targetAngles[i];
			}
			this.setAxisAnglesOptimized(nextAngles);
			try
			{
				Thread.sleep((long) (period*degreeStep*1000));
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	/// wait

		}
	}

	

	void moveStraightTo(double[] targetCoordinates)
	{	
		// if no pitch is specified, use the current one
		double pitchAngle = this.getAxisAngle("shoulder") + this.getAxisAngle("elbow") + this.getAxisAngle("wrist");
		double speed = 35;
		moveStraightTo(targetCoordinates, pitchAngle, speed);
	}
	void moveStraightTo(double[] targetCoordinates, double pitchAngle)
	{
		double speed = 35;
		moveStraightTo(targetCoordinates, pitchAngle, speed);
	}
	// moves the tip of the gripper to the given coordinates in a straight line.
	void moveStraightTo(double[] targetCoordinates, double pitchAngle, double speed)
	{	
		double gripLength = this.getCurrentGripInfo().gripLength;
		
		double stepDistance = 1;
		double period = 1 / speed;
		double[] currentCoordinates = this.getCurrentCoordinates();
		double[] direction = Utility.getDirectionVector(currentCoordinates, targetCoordinates);
		boolean reached = false;
		while (!reached)
		{
			double distance = Math.sqrt( Math.pow((targetCoordinates[0]-currentCoordinates[0]),2) + Math.pow((targetCoordinates[1]-currentCoordinates[1]),2) + Math.pow((targetCoordinates[2]-currentCoordinates[2]),2) );
			double[] nextCoordinates = new double[3];
			if ( distance < stepDistance )
			{
				nextCoordinates = targetCoordinates;
				reached = true;
			}
			else
				nextCoordinates = Utility.arrayAddition(currentCoordinates, Utility.arrayMultiplication(stepDistance, direction ));
			double[] newAngles = this.findAnglesConstantPitch(nextCoordinates, gripLength, pitchAngle);
			if (!this.safetyCheckAxisAngles(newAngles))
			{
				Utility.error("invalid angles:\n%f     %f    %f     %f\nTrying to reach point (%f, %f, %f)", newAngles[0], newAngles[1], newAngles[2], newAngles[3], nextCoordinates[0], nextCoordinates[1], nextCoordinates[2]);
				return;
			}
			this.setAxisAnglesOptimized(newAngles);
			currentCoordinates = nextCoordinates;
			try
			{
				Thread.sleep((long) (period*stepDistance*1000));
			} catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}


}
