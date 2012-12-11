package collaborobo;

import collaborobo.utils.Constants;
import collaborobo.utils.Util;
import java.util.Observable;
import java.util.Observer;

public class ComputeProbabilities implements Observer {

  protected static final float A_PROB = 0.45f;
  protected static final float B_PROB = 0.475f;
  protected static final float C_PROB = 0.535f;
  protected static final float D_PROB = -1.0f;
  CollaborativeRobot robot;

  public ComputeProbabilities(CollaborativeRobot robot) {
    this.robot = robot;
  }

  @Override
  public void update(Observable obj, Object arg) {

    if (arg instanceof SensorProbBean) {
      SensorProbBean sb = (SensorProbBean) arg;

      final double robotSize = Constants.ROBOT_RADIUS * Constants.MAP_PRECISION;

      int minx = (int) (sb.mapX - robotSize),
              maxx = (int) (sb.mapX + robotSize),
              miny = (int) (sb.mapY - robotSize),
              maxy = (int) (sb.mapY + robotSize);

      minx = minx > 0 ? minx : 0;
      maxx = (int) (maxx < Constants.mapSizeX ? maxx : Constants.mapSizeX - 1);
      miny = miny > 0 ? miny : 0;
      maxy = (int) (maxy < Constants.mapSizeY ? maxy : Constants.mapSizeY - 1);

      double prob = A_PROB;

      for (int y = miny; y < maxy; y++) {
        for (int x = minx; x < maxx; x++) {
          double distanceCenter = Math.sqrt(Math.abs(y - sb.mapY)
                  * Math.abs(y - sb.mapY) + Math.abs(x - sb.mapX)
                  * Math.abs(x - sb.mapX));

          if (distanceCenter < robotSize) {
            robot.probabilitiesMap[y][x] = 0.0;
          }
        }
      }

      /*double prob = A_PROB;
       int y = (int) jClient.PosY;
       int x = (int) jClient.PosX;
		
       jClient.probabilitiesMap[y][x] = (prob * jClient.probabilitiesMap[y][x])
       / ((prob * jClient.probabilitiesMap[y][x]) + ((1 - prob) * (1 - jClient.probabilitiesMap[y][x])));*/


      double compassRadians = Math.toRadians(robot.compass);
      double frontAngleBegin = compassRadians - Math.toRadians(30);
      double frontAngleEnd = compassRadians + Math.toRadians(30);

      frontAngleBegin = Util.normalizeAngle(frontAngleBegin);
      frontAngleEnd = Util.normalizeAngle(frontAngleEnd);

      double leftAngleBegin = compassRadians + Math.toRadians(30);
      double leftAngleEnd = compassRadians + Math.toRadians(90);
      leftAngleBegin = Util.normalizeAngle(leftAngleBegin);
      leftAngleEnd = Util.normalizeAngle(leftAngleEnd);

      double rightAngleBegin = compassRadians - Math.toRadians(90);
      double rightAngleEnd = compassRadians - Math.toRadians(30);
      rightAngleBegin = Util.normalizeAngle(rightAngleBegin);
      rightAngleEnd = Util.normalizeAngle(rightAngleEnd);

      final double griddelta = 2 * (1 / Constants.SENSOR_MINIMUM_VALUE) * Constants.MAP_PRECISION;

      minx = (int) (sb.mapX - griddelta);
      maxx = (int) (sb.mapX + griddelta);
      miny = (int) (sb.mapY - griddelta);
      maxy = (int) (sb.mapY + griddelta);

      minx = minx > 0 ? minx : 0;
      maxx = (int) (maxx < Constants.mapSizeX ? maxx : Constants.mapSizeX - 1);
      miny = miny > 0 ? miny : 0;
      maxy = (int) (maxy < Constants.mapSizeY ? maxy : Constants.mapSizeY - 1);

      for (int y = miny; y < maxy; y++) {
        for (int x = minx; x < maxx; x++) {

          // FRONT
          if (sb.frontSensor >= Constants.SENSOR_MINIMUM_VALUE) {
            calculateVisibleArea(sb.frontSensorPosX, sb.frontSensorPosY,
                    frontAngleBegin, frontAngleEnd, sb.leftSensor, y, x);
          }

          // LEFT
          if (sb.leftSensor >= Constants.SENSOR_MINIMUM_VALUE) {
            calculateVisibleArea(sb.leftSensorPosX, sb.leftSensorPosY,
                    leftAngleBegin, leftAngleEnd, sb.leftSensor, y, x);
          }

          // RIGHT
          if (sb.rightSensor >= Constants.SENSOR_MINIMUM_VALUE) {
            calculateVisibleArea(sb.rightSensorPosX, sb.rightSensorPosY,
                    rightAngleBegin, rightAngleEnd, sb.rightSensor, y, x);
          }
        }
      }
    }
  }

  private void calculateVisibleArea(int cx, int cy, double angleBegin,
          double angleEnd, double sensorValue, int y, int x) {

    double theta = Math.atan2(cy - y, x - cx);

    boolean cond = (theta >= angleBegin && theta <= angleEnd);
    if (angleBegin > angleEnd) {
      cond = (theta >= angleBegin || theta <= angleEnd);
    }

    if (cond) {
      double distanceCenter = Math.sqrt((y - cy) * (y - cy) + (x - cx) * (x - cx));

      double prob = computeProb(sensorValue, distanceCenter);
      if (prob == -1.0) {
        return;
      }

      // Teorema de Bayes
      robot.probabilitiesMap[y][x] =
              (prob * robot.probabilitiesMap[y][x])
              / ((prob * robot.probabilitiesMap[y][x])
              + ((1 - prob) * (1 - robot.probabilitiesMap[y][x])));
    }
  }

  public double computeProb(final double reading, final double dist) {

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
