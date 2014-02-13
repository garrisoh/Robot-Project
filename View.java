import java.awt.Color;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Hand;
import com.leapmotion.leap.HandList;
import com.leapmotion.leap.Listener;
import com.leapmotion.leap.Vector;
/**
 * A GUI class that displays leap coordinate data.
 *
 * @author Haley Garrison
 */
public class View extends Listener {
	// window constants
	private static final int WINDOW_WIDTH = 600;
	private static final int WINDOW_HEIGHT = 600;
	
	// GUI objects
	private JFrame window = null;
	private JLabel xlabel = null;
	private JLabel ylabel = null;
	private JLabel zlabel = null;
	private JLabel griplabel = null;
	
	// used to slow frame updates
	private int counter = 0;

	/**
	 * Creates a new frame and initializes ui elements
	 */
	public View() {
		// create new frame
		window = new JFrame("RobotControl");
		window.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setBackground(Color.WHITE);
		
		// set layout to null to use custom coordinates
		window.setLayout(null);
		
		// add some labels for coordinate data
		xlabel = makeLabel("X: 0.0", 20);
		ylabel = makeLabel("Y: 0.0", 20);
		zlabel = makeLabel("Z: 0.0", 20);
		griplabel = makeLabel("Grip: 0.0", 20);
		
		// position the labels
		xlabel.setLocation(0, (WINDOW_HEIGHT-20)/5 - xlabel.getHeight()/2);
		ylabel.setLocation(0, (WINDOW_HEIGHT-20)*2/5 - ylabel.getHeight()/2);
		zlabel.setLocation(0, (WINDOW_HEIGHT-20)*3/5 - zlabel.getHeight()/2);
		griplabel.setLocation(0, (WINDOW_HEIGHT-20)*4/5 - griplabel.getHeight()/2);
		
		// add labels and make visible
		window.add(xlabel);
		window.add(ylabel);
		window.add(zlabel);
		window.add(griplabel);
		window.setVisible(true);
	}
	
	/**
	 * Convenience method to generate labels
	 * 
	 * @param text Text to display
	 * @param fontSize Font size
	 * @return A configured JLabel
	 */
	private JLabel makeLabel(String text, int fontSize) {
		JLabel label = new JLabel(text, SwingConstants.CENTER);
		label.setFont(new Font("Helvetica", Font.PLAIN, fontSize));
		label.setSize(WINDOW_WIDTH, fontSize + 5);
		label.setForeground(Color.BLACK);
		return label;
	}
	
	/**
	 * Updates the display whenever a new frame is received
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
		
		counter++;
		// Slows the refresh rate
		if (counter % 5 != 0) {
			return;
		}

		// Update display
		Vector position = hand.palmPosition();
		double grip = hand.sphereRadius();
		xlabel.setText("X: " + Math.floor(position.getX()));
		// Convert leap coordinate system into 'Z-up' system
		ylabel.setText("Y: " + Math.floor(position.getZ()));
		zlabel.setText("Z: " + Math.floor(position.getY()));
		griplabel.setText("Grip: " + Math.floor(grip));
	}
}
