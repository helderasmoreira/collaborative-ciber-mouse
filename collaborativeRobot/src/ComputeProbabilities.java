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
		
		double prob = 0.0;
		
		for (int i = miny; i < maxy; i++) {
			for (int j = minx; j < maxx; j++) {
				double distanceCenter = Math.sqrt(Math.abs(i - jClient.PosY)
						* Math.abs(i - jClient.PosY) + Math.abs(j - jClient.PosX)
						* Math.abs(j - jClient.PosX));
				
				if(distanceCenter <= 2.0*Constants.ROBOT_RADIUS*Constants.MAP_PRECISION)
					jClient.probabilitiesMap[i][j] = (prob * jClient.probabilitiesMap[i][j])
						/ ((prob * jClient.probabilitiesMap[i][j]) + ((1 - prob) * (1 - jClient.probabilitiesMap[i][j])));
			}
		}
		
		
		double compassRadians = Math.toRadians(jClient.compass);
		double angleCenterI = Util.angleDifference(compassRadians, Math.toRadians(60));
		double angleCenterF = angleCenterI + Math.toRadians(60);
		
		double angleCenterI2 = angleCenterI-Math.toRadians(60);
		double angleCenterF2 = angleCenterF-Math.toRadians(60);
		
		double angleCenterI3 = angleCenterI+Math.toRadians(60);
		double angleCenterF3 = angleCenterF+Math.toRadians(60);
		
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
	
	private static void calculateVisibleArea(int centerPosX2,
			int centerPosY2, double angleCenterI, double angleCenterF,
			double sensorValue, int i, int j) {
		
		double anglePoint;
		double distanceCenter;
		
		anglePoint = Math.atan2(j - centerPosX2, centerPosY2 - i);
		
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
