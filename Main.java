import com.leapmotion.leap.Controller;

public class Main {

	public static void main(String[] args) {
		SerialComm sc = new SerialComm(args);
		Arm arm = new Arm(sc);
		View view = new View();
		Controller controller = new Controller();
		controller.addListener(arm);
		controller.addListener(view);
		
		try{
		      System.in.read();
		    }
		    catch (Exception e){
		        System.out.print(e);
		      }
	      // forces GUI window to close	
	      System.exit(0);
	}
}
