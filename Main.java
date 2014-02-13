
public class Main {

	public static void main(String[] args) {
		SerialCom sc = SerialCom(args[0]);
		Arm arm = new Arm(sc);
		View view = new View();
		Controller.addListener(arm);
		Controller.addListener(view);
		
		try{
		      System.in.read();
		    }
		    catch (Exception e){
		        System.out.print(e);
		      }

	}

}
