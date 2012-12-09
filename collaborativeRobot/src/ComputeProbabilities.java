
import java.util.Observable;
import java.util.Observer;

public class ComputeProbabilities implements Observer {

  protected static final float A_PROB = 0.45f;
  protected static final float B_PROB = 0.475f;
  protected static final float C_PROB = 0.535f;
  protected static final float D_PROB = -1.0f;

  @Override
  public void update(Observable obj, Object arg) {

    if (arg instanceof SensorProbBean) {
      SensorProbBean sb = (SensorProbBean) arg;

      final double robotSize = 2.0 * Constants.ROBOT_RADIUS * Constants.MAP_PRECISION;

      int minx = (int) (sb.mapX - robotSize),
              maxx = (int) (sb.mapX + robotSize),
              miny = (int) (sb.mapY - robotSize),
              maxy = (int) (sb.mapY + robotSize);

      minx = minx > 0 ? minx : 0;
      maxx = (int) (maxx < Constants.arenaSizeX * 2 * Constants.MAP_PRECISION ? maxx : Constants.arenaSizeX * 2 * Constants.MAP_PRECISION - 1);
      miny = miny > 0 ? miny : 0;
      maxy = (int) (maxy < Constants.arenaSizeY * 2 * Constants.MAP_PRECISION ? maxy : Constants.arenaSizeY * 2 * Constants.MAP_PRECISION - 1);

      double prob = A_PROB;

      for (int i = miny; i < maxy; i++) {
        for (int j = minx; j < maxx; j++) {
          double distanceCenter = Math.sqrt(Math.abs(i - sb.mapY)
                  * Math.abs(i - sb.mapY) + Math.abs(j - sb.mapX)
                  * Math.abs(j - sb.mapX));

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


      double compassRadians = Math.toRadians(sb.compass);
      double angleCenterI = compassRadians + Math.toRadians(-30);
      double angleCenterF = compassRadians + Math.toRadians(30);

      angleCenterI = Util.normalizeAngle(angleCenterI);
      angleCenterF = Util.normalizeAngle(angleCenterF);

      double angleCenterI2 = angleCenterI + Math.toRadians(60);
      double angleCenterF2 = angleCenterF + Math.toRadians(60);
      angleCenterI2 = Util.normalizeAngle(angleCenterI2);
      angleCenterF2 = Util.normalizeAngle(angleCenterF2);

      double angleCenterI3 = angleCenterI - Math.toRadians(60);
      double angleCenterF3 = angleCenterF - Math.toRadians(60);
      angleCenterI3 = Util.normalizeAngle(angleCenterI3);
      angleCenterF3 = Util.normalizeAngle(angleCenterF3);

      final double griddelta = 2 * (1 / Constants.SENSOR_MINIMUM_VALUE) * Constants.MAP_PRECISION;

      minx = (int) (sb.mapX - griddelta);
      maxx = (int) (sb.mapX + griddelta);
      miny = (int) (sb.mapY - griddelta);
      maxy = (int) (sb.mapY + griddelta);

      minx = minx > 0 ? minx : 0;
      maxx = (int) (maxx < Constants.arenaSizeX * 2 * Constants.MAP_PRECISION ? maxx : Constants.arenaSizeX * 2 * Constants.MAP_PRECISION - 1);
      miny = miny > 0 ? miny : 0;
      maxy = (int) (maxy < Constants.arenaSizeY * 2 * Constants.MAP_PRECISION ? maxy : Constants.arenaSizeY * 2 * Constants.MAP_PRECISION - 1);

      for (int i = miny; i < maxy; i++) {
        for (int j = minx; j < maxx; j++) {

          // FRONT
          if (sb.frontSensor >= Constants.SENSOR_MINIMUM_VALUE) {
            calculateVisibleArea(sb.frontSensorPosX, sb.frontSensorPosY,
                    angleCenterI, angleCenterF, sb.leftSensor, i, j);
          }


          // LEFT
          if (sb.leftSensor >= Constants.SENSOR_MINIMUM_VALUE) {
            calculateVisibleArea(sb.leftSensorPosX, sb.leftSensorPosY,
                    angleCenterI2, angleCenterF2, sb.leftSensor, i, j);
          }

          // RIGHT
          if (CollaborativeRobot.rightSensor >= Constants.SENSOR_MINIMUM_VALUE) {
            calculateVisibleArea(sb.rightSensorPosX, sb.rightSensorPosY,
                    angleCenterI3, angleCenterF3, sb.rightSensor, i, j);
          }

        }
      }
    }
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
      if (prob == -1.0) {
        return;
      }

      // Teorema de Bayes
      CollaborativeRobot.probabilitiesMap[i][j] = (prob * CollaborativeRobot.probabilitiesMap[i][j])
              / ((prob * CollaborativeRobot.probabilitiesMap[i][j]) + ((1 - prob) * (1 - CollaborativeRobot.probabilitiesMap[i][j])));
    }
  }

  public static double computeProb(final double reading, final double dist) {

    if (dist < (1 / (reading + Constants.OBST_NOISE)) * Constants.MAP_PRECISION) {
      return A_PROB;
    } else if (dist < (1 / reading) * Constants.MAP_PRECISION - Constants.MIN_WALL_WIDTH * Constants.MAP_PRECISION) {
      return B_PROB;
    } else if (dist < (1 / reading) * Constants.MAP_PRECISION + Constants.MIN_WALL_WIDTH * Constants.MAP_PRECISION) {
      return C_PROB;
    } else {
      return D_PROB;
    }
  }
}
