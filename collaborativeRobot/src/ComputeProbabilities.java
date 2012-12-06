import java.util.Observable;
import java.util.Observer;

public class ComputeProbabilities implements Observer {
	
	protected static final float A_PROB = 0.45f;
	protected static final float B_PROB = 0.475f;
	protected static final float C_PROB = 0.535f;
	protected static final float D_PROB = -1.0f;
	
	@Override
	public void update(Observable arg0, Object arg1) {	
		
		final double robotSize = 2.0*Constants.ROBOT_RADIUS*Constants.MAP_PRECISION;
		
		int minx = (int) (jClient.PosX - robotSize), 
				maxx = (int) (jClient.PosX + robotSize), 
				miny = (int) (jClient.PosY - robotSize), 
				maxy = (int) (jClient.PosY + robotSize);
		
		minx = minx > 0 ? minx : 0;
		maxx = (int) (maxx < Constants.arenaSizeX*2*Constants.MAP_PRECISION ? maxx : Constants.arenaSizeX*2*Constants.MAP_PRECISION - 1);
		miny = miny > 0 ? miny : 0;
		maxy = (int) (maxy < Constants.arenaSizeY*2*Constants.MAP_PRECISION ? maxy : Constants.arenaSizeY*2*Constants.MAP_PRECISION - 1);
		
		double prob = A_PROB;
		
		for (int i = miny; i < maxy; i++) {
			for (int j = minx; j < maxx; j++) {
				double distanceCenter = Math.sqrt(Math.abs(i - jClient.PosY)
						* Math.abs(i - jClient.PosY) + Math.abs(j - jClient.PosX)
						* Math.abs(j - jClient.PosX));
				
				/*if(distanceCenter < robotSize)
					jClient.probabilitiesMap[i][j] = (prob * jClient.probabilitiesMap[i][j])
						/ ((prob * jClient.probabilitiesMap[i][j]) + ((1 - prob) * (1 - jClient.probabilitiesMap[i][j])));*/
			}
		}
		
		/*double prob = A_PROB;
		int y = (int) jClient.PosY;
		int x = (int) jClient.PosX;
		
		jClient.probabilitiesMap[y][x] = (prob * jClient.probabilitiesMap[y][x])
				/ ((prob * jClient.probabilitiesMap[y][x]) + ((1 - prob) * (1 - jClient.probabilitiesMap[y][x])));*/
		
		
		double compassRadians = Math.toRadians(jClient.compass);
		double angleCenterI = compassRadians + Math.toRadians(-30);
		double angleCenterF = compassRadians + Math.toRadians(30);
		
		angleCenterI = normalizeAngle(angleCenterI);
		angleCenterF = normalizeAngle(angleCenterF);
		
		double angleCenterI2 = angleCenterI+Math.toRadians(60);
		double angleCenterF2 = angleCenterF+Math.toRadians(60);
		angleCenterI2 = normalizeAngle(angleCenterI2);
		angleCenterF2 = normalizeAngle(angleCenterF2);
		
		double angleCenterI3 = angleCenterI-Math.toRadians(60);
		double angleCenterF3 = angleCenterF-Math.toRadians(60);
		angleCenterI3 = normalizeAngle(angleCenterI3);
		angleCenterF3 = normalizeAngle(angleCenterF3);
		
		final double griddelta = 2*(1/Constants.SENSOR_MINIMUM_VALUE)*Constants.MAP_PRECISION;
		
		minx = (int) (jClient.PosX - griddelta); 
		maxx = (int) (jClient.PosX + griddelta); 
		miny = (int) (jClient.PosY - griddelta); 
		maxy = (int) (jClient.PosY + griddelta);
		
		minx = minx > 0 ? minx : 0;
		maxx = (int) (maxx < Constants.arenaSizeX*2*Constants.MAP_PRECISION ? maxx : Constants.arenaSizeX*2*Constants.MAP_PRECISION - 1);
		miny = miny > 0 ? miny : 0;
		maxy = (int) (maxy < Constants.arenaSizeY*2*Constants.MAP_PRECISION ? maxy : Constants.arenaSizeY*2*Constants.MAP_PRECISION - 1);
		
		for (int i = miny; i < maxy; i++) {
			for (int j = minx; j < maxx; j++) {
				
				// FRONT
				if(jClient.frontSensor >= Constants.SENSOR_MINIMUM_VALUE) {
					calculateVisibleArea(jClient.frontSensorPosX, jClient.frontSensorPosY,
							angleCenterI, angleCenterF, jClient.leftSensor, i, j);
				}
				
				
				// LEFT
				if(jClient.leftSensor >= Constants.SENSOR_MINIMUM_VALUE) {
					calculateVisibleArea(jClient.leftSensorPosX, jClient.leftSensorPosY,
							angleCenterI2, angleCenterF2, jClient.leftSensor, i, j);
				}
				
				// RIGHT
				if(jClient.rightSensor >= Constants.SENSOR_MINIMUM_VALUE) {
					calculateVisibleArea(jClient.rightSensorPosX, jClient.rightSensorPosY,
							angleCenterI3, angleCenterF3, jClient.rightSensor, i, j);
				}
				
			}
		}
	}
	
	double normalizeAngle(double angle) {
	    double newAngle = angle;
	    while (newAngle <= -Math.PI) {
	      newAngle += 2 * Math.PI;
	    }
	    while (newAngle > Math.PI) {
	      newAngle -= 2 * Math.PI;
	    }
	    return newAngle;
	  }

	
	private static void calculateVisibleArea(int centerPosX2,
			int centerPosY2, double angleCenterI, double angleCenterF,
			double sensorValue, int i, int j) {
		
		double anglePoint;
		double distanceCenter;
		
		anglePoint = Math.atan2(centerPosY2 - i, j - centerPosX2);
		
		if (anglePoint >= angleCenterI
				&& anglePoint <= angleCenterF) {

			distanceCenter = Math.sqrt(Math.abs(i - centerPosY2)
					* Math.abs(i - centerPosY2) + Math.abs(j - centerPosX2)
					* Math.abs(j - centerPosX2));
			
			double prob = computeProb(sensorValue, distanceCenter);
			if(prob == -1.0) {
				return;
			}
			
			// Teorema de Bayes
			jClient.probabilitiesMap[i][j] = (prob * jClient.probabilitiesMap[i][j])
					/ ((prob * jClient.probabilitiesMap[i][j]) + ((1 - prob) * (1 - jClient.probabilitiesMap[i][j])));
		}
	}
	
	 public static double computeProb(final double reading, final double dist) {

			if (dist < (1 / (reading + Constants.OBST_NOISE))*Constants.MAP_PRECISION)
				return A_PROB;
			else if (dist < (1 / reading)*Constants.MAP_PRECISION - Constants.MIN_WALL_WIDTH*Constants.MAP_PRECISION)
				return B_PROB;
			else if (dist < (1 / reading)*Constants.MAP_PRECISION + Constants.MIN_WALL_WIDTH*Constants.MAP_PRECISION)
				return C_PROB;
			else
				return D_PROB;
		}
  

}
