
public class Util {

  public static double angleDifference(double alpha, double beta) {

    double deltatheta = 0;

    if ((alpha < 0 && beta < 0) || (alpha > 0 && beta > 0)) {
      deltatheta = beta - alpha;
    } else {
      if (Math.abs(alpha) + Math.abs(beta) <= Math.PI) {
        deltatheta = (Math.abs(alpha) + Math.abs(beta));
      } else {
        deltatheta = -Math.abs(2 * Math.PI - Math.abs(alpha - beta));
      }
      if (beta < alpha) {
        deltatheta *= -1;
      }
    }
    return deltatheta;
  }

  /*
   * sensorDir is in Degrees returns [x,y]
   */
  private static int[] sensorMapPosition(int robotMapX, int robotMapY, double sensorDir) {
    double sensorPosX = robotMapX + Constants.robotRadius
            * Math.cos(Math.toRadians(sensorDir));
    double sensorPosY = robotMapY - Constants.robotRadius
            * Math.sin(Math.toRadians(sensorDir));

    return new int[]{Math.round((float) sensorPosX),
              Math.round((float) sensorPosY)};
  }

  public static int[] frontSensorMapPosition(int robotMapX, int robotMapY, double compass) {
    double frontSensorDirection = compass;
    return sensorMapPosition(robotMapX, robotMapY, frontSensorDirection);
  }

  public static int[] leftSensorMapPosition(int robotMapX, int robotMapY, double compass) {
    double leftSensorDirection = compass + 60;
    return sensorMapPosition(robotMapX, robotMapY, leftSensorDirection);
  }

  public static int[] rightSensorMapPosition(int robotMapX, int robotMapY, double compass) {
    double rightSensorDirection = compass - 60;
    return sensorMapPosition(robotMapX, robotMapY, rightSensorDirection);
  }
}
