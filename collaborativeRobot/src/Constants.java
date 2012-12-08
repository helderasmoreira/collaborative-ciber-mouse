
public class Constants {

  public static final double OBST_NOISE = 0.1;
  public static final double MIN_WALL_WIDTH = 0.3;
  public static final double MAP_PRECISION = 10.0;
  public static final double SENSOR_MINIMUM_VALUE = 1.0;
  public static final double ROBOT_RADIUS = 0.5;
  public static final double MAX_ANGLE_DEGREES_DEVIATION = 5.0;
  public static final int BEACON = 0;
  
  public static final int arenaSizeY = 14;
  public static final int mapSizeY = arenaSizeY * 2 * (int) MAP_PRECISION;
  public static final int arenaSizeX = 28;
  public static final int mapSizeX = arenaSizeX * 2 * (int) MAP_PRECISION;
  public static final int robotRadius = (int) Constants.MAP_PRECISION / 2;
}
