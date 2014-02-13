/**
* Static class containing mathematical functions
* */
public class Utility {
	public static void main(String args[]){
		// Demonstration
		System.out.println(asind(1 / Math.sqrt(2))); //asin
		System.out.println(asind(1 / Math.sqrt(2))); //acos
		System.out.println(atand(1 /1)); //atan
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
	
	// Prints an error message
	public static void error(String message, Object... args) {
		System.out.printf(message, args);
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
	
	public static double atand(double x){
		return Math.toDegrees(Math.atan(x));
	}
	
	public static double acosd(double x){
		return Math.toDegrees(Math.acos(x));
	}
	
	public static double asind(double x){
		return Math.toDegrees(Math.asin(x));
	}
	
	/// Returns the maximum value in the array.
	public static double max(double[] x)
	{
		double maxValue = 0;
		for ( int i = 0; i < x.length; ++i)
			if (x[i] > maxValue) maxValue = x[i];
		return maxValue;
	}
	static double[] absArray(double[] x)
	{
		double[] output = new double[x.length];
		for ( int i = 0; i < x.length; ++i)
			output[i] = Math.abs(x[i]);
		return output;
	}
	static double[] arraySubtraction(double[] x, double[] y)
	{
		double[] output = new double[x.length];
		// Two arrays, subtract each set of components.
			for ( int i = 0; i < x.length; ++i)
				output[i] = x[i] - y[i];
		return output;
	}
	static double[] arraySubtraction(double[] x, double y)
	{
		double[] output = new double[x.length];
		
		// Array - non-array, subtract y from every part of x
	
			for ( int i = 0; i < x.length; ++i)
				output[i] = x[i] - y;
		return output;
	}
	
	static double[] arrayAddition(double[] x, double[] y)
	{
		double[] output = {};
// Two arrays, add each set of components.
		{
			for ( int i = 0; i < x.length; ++i)
				output[i] = x[i] + y[i];
		}
		
		return output;
	}
	static double[] arrayAddition(double[] x, double y)
	{
		double[] output = {};
		
		// Array + non-array, add y to every part of x
		{
			for ( int i = 0; i < x.length; ++i)
				output[i] = x[i] + y;
		}
		
		return output;
	}
	static double[] arrayAddition(double x, double[] y)
	{
		double[] output = {};
		
		// non-array + array, add x to every part of y
		{
			for ( int i = 0; i < y.length; ++i)
				output[i] = x + y[i];
		}
		return output;
	}
	

	static double[] arrayMultiplication(double[] x, double[] y)
	{
		double[] output = {};
		// Two arrays, multiply each set of components.
		{
			for ( int i = 0; i < x.length; ++i)
				output[i] = x[i] * y[i];
		}
		
		return output;
	}
	static double[] arrayMultiplication(double[] x, double y)
	{
		double[] output = {};
		
		// Array * non-array, multiply y with every part of x
		{
			for ( int i = 0; i < x.length; ++i)
				output[i] = x[i] * y;
		}
		
		return output;
	}
	static double[] arrayMultiplication(double x, double[] y)
	{
		double[] output = {};
		
		// non-array * array, multiply x with every part of y
		{
			for ( int i = 0; i < y.length; ++i)
				output[i] = x * y[i];
		}
		
		return output;
	}


	static double[] getDirectionVector(double[] pointA, double[] pointB)
	{
		double[] unitVector =  arraySubtraction(pointB, pointA);
		for (int i = 0; i < unitVector.length; ++i)
		{
			unitVector[i] = unitVector[i] / Math.sqrt( Math.pow((pointB[0]-pointA[0]),2) + Math.pow((pointB[1]-pointA[1]),2) + Math.pow((pointB[2]-pointA[2]),2) );
		}
		return unitVector;
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
		// at the moment all that happens is that if the point is out of range it is set to the max or min.
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