/**
* Static class containing mathematical functions
* */
public class Utility {
	public static void main(String args[]){
		// Demonstration
		System.out.println(asind(1,Math.sqrt(2))); //asin
		System.out.println(asind(1,Math.sqrt(2))); //acos
		System.out.println(atand(1,1)); //atan
		System.out.println(sind(30)); //asin
		System.out.println(cosd(60)); //acos
		System.out.println(tand(45)); //atan
		System.out.println();
		System.out.println(map(100.2,50,300,-7.5,30)); // map to z axis
		System.out.println(map(0.4,-300,300,-10,10)); // map to y axis
		System.out.println(map(40.0,-300,300,-20,20)); // map to x axis
		// error handling
		System.out.println(map(0,50,300,-7.5,30)); // map to z axis
}

// Trig functions
	
	public static double sind(double angle){
		return Math.sin(Math.toRadians(angle));
	}
	
	public static double cosd(double angle){
		return Math.cos(Math.toRadians(angle));
	}
	
	public static double tand(double angle){
		return Math.tan(Math.toRadians(angle));
	}
	
	public static double atand(double opposite, double adjacent){
		return Math.toDegrees(Math.atan(opposite/adjacent));
	}
	
	public static double acosd(double adjacent, double hypotenuse){
		return Math.toDegrees(Math.acos(adjacent/hypotenuse));
	}
	
	public static double asind(double opposite, double hypotenuse){
		return Math.toDegrees(Math.asin(opposite/hypotenuse));
	}
	
	/** mapping leap coordinates onto robot coordinates
	 * @param value point
	 * @param low1 minimum of original
	 * @param high1 maximum of original
	 * @param low2 minimum of output
	 * @param high1 maximum of output
	 * @return
	 */
	
	public static double map(double value,double low1, double high1, double low2, double high2){
		double percent = (value - low1)/(high1 - low1);
	    if(percent > 1) {
	        percent = 1;
	    }
	    if(percent < 0) {
	        percent = 0;
	    }
	    return (high2 - low2) * percent + low2;
	}
}