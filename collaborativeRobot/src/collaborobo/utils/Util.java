package collaborobo.utils;


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

  // Arduino map function: http://www.arduino.cc/en/Reference/map
  public static double map(double x, double in_min, double in_max,
          double out_min, double out_max) {
    return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
  }

  // compass range -180;180
  public static double compassToDeg(double compass) {
    if (compass >= 0.0 && compass <= 180) {
      return compass;
    } else {
      return map(compass, -180.0, 0.0, 180.0, 360.0);
    }
  }

  public static double normalizeAngle(double angle) {

    double newAngle = angle;
    while (newAngle <= -Math.PI) {
      newAngle += 2* Math.PI;
    }
    while (newAngle > Math.PI) {
      newAngle -= 2* Math.PI;
    }
    return newAngle;
  }

  public static <T extends Comparable<T>> T constrain(T value, T min, T max) {
    if (value.compareTo(min) < 0) {
      return min;
    } else if (value.compareTo(max) > 0) {
      return max;
    } else {
      return value;
    }
  }

  public static double makePositive(double angle) {
    double newAngle = angle;
    while (newAngle <= -Math.PI) {
      newAngle += 2* Math.PI;
    }
    return newAngle;
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
