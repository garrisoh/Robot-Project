import java.util.Scanner;

public class AngleSelect {

	public static void main(String[] args) {
		SerialComm sc = new SerialComm(args);
		Arm arm = new Arm(sc);
		
		Scanner scan = new Scanner(System.in);
		while(true) {
			if (scan.hasNext()) {
				String input = scan.nextLine();
				if (input.equals("x"))
					break;
				Integer angle = new Integer(input);
				double[] angles = {0, 90, angle, 0, 90};
				arm.setAxisAnglesOptimized(angles);
			}
		}
		scan.close();
	}
}
