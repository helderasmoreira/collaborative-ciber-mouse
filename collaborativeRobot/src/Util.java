
public class Util {

	public static double angleDifference(double alpha, double beta) {

		double deltatheta = 0;

		if ((alpha < 0 && beta < 0) || (alpha > 0 && beta > 0)) {
			deltatheta = beta - alpha;
		} else {
			if (Math.abs(alpha) + Math.abs(beta) <= Math.PI)
				deltatheta = (Math.abs(alpha) + Math.abs(beta));
			else
				deltatheta = -Math.abs(2 * Math.PI - Math.abs(alpha - beta));
			if (beta < alpha)
				deltatheta *= -1;
		}
		return deltatheta;
	}

}
