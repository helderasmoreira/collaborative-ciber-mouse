package collaborobo;


import collaborobo.utils.Constants;
import collaborobo.utils.Util;
import java.util.Arrays;
import java.util.Comparator;

public class Communication {

  public static String[] dataToProcess = new String[5];
  public static int order = 0;
  private CollaborativeRobot robot;

  public Communication(CollaborativeRobot robot) {
    this.robot = robot;
    for (int i = 0; i < dataToProcess.length; i++) {
      dataToProcess[i] = "";
    }
  }

  /*
   * order represents which probable point we want (ie most probable, second most probable)
   * returns a string with the format: "y-offsetY-x-offsetX-value|value;value;value|value..."
   * values from top-left to bottom-right from left to right 
   * | represents a row change
   * ; represents a column change 
   * in a normal case it would be 8(+1) values 
   * but we need to consider limit cases so they can be less than 8
   */
  public String getProbableBeacon(int order) {

    int maxValues[][] = new int[order][3];

    for (int y = 0; y < robot.beaconProbability.length; y++) {
      for (int x = 0; x < robot.beaconProbability[y].length; x++) {
        int minimum = getMinimum(maxValues);
        if (robot.beaconProbability[y][x] > (maxValues[minimum][0])) {
          maxValues[minimum][1] = y;
          maxValues[minimum][0] = (robot.beaconProbability[y][x]);
          maxValues[minimum][2] = x;
        }
      }
    }

    Arrays.sort(maxValues, new Comparator<int[]>() {
      @Override
      public int compare(int[] o1, int[] o2) {
        return ((Integer) o2[0]).compareTo(o1[0]);
      }
    });

    int yMax = maxValues[order - 1][1];
    int xMax = maxValues[order - 1][2];

    if (robot.beaconProbability[yMax][xMax] == 0) {
      return "";
    }

    String ret = yMax + "-" + robot.initialPosY + "-" + xMax + "-" + robot.initialPosX + "-"
            + +robot.beaconProbability[yMax][xMax] + "|";

    for (int i = Math.max(yMax - 1, 0); i <= Math.min(yMax + 1, Constants.mapSizeY - 1); i++) {
      for (int j = Math.max(xMax - 1, 0); j <= Math.min(xMax + 1, Constants.mapSizeX - 1); j++) {
        if (i == yMax && j == xMax) {
          continue;
        }
        ret += robot.beaconProbability[i][j] + ";";
      }
      ret = ret.substring(0, ret.length() - 1);
      ret += "|";
    }

    return ret;
  }

  /*
   * returns the index of the minimum value of maxValues[x][0]
   */
  private int getMinimum(int[][] maxValues) {
    int min = Integer.MAX_VALUE;
    int iMin = 0;
    for (int i = 0; i < maxValues.length; i++) {
      if (maxValues[i][0] < min) {
        min = maxValues[0][0];
        iMin = i;
      }
    }
    return iMin;
  }

  /*
   * decodes the message received and applies it to the current map
   * message should be like: "y-offsetY-x-offsetX-value|value;value;value|value..."
   */
  public void decodeAndApplyProbableBeaconMessage(String message) {

    if (message.equals("")) {
      return;
    }

    String[] lines = message.split("\\|");

    String beaconMostProbablePoint[] = lines[0].split("-");

    int mostProbableY = Integer.parseInt(beaconMostProbablePoint[0]);
    int mostProbableX = Integer.parseInt(beaconMostProbablePoint[2]);
    int value = Integer.parseInt(beaconMostProbablePoint[4]);

    int offsetY = Integer.parseInt(beaconMostProbablePoint[1]);
    int offsetX = Integer.parseInt(beaconMostProbablePoint[3]);

    mostProbableY = mostProbableY - (offsetY - robot.initialPosY);
    mostProbableX = mostProbableX + (offsetX - robot.initialPosX);

    robot.beaconProbability[mostProbableY][mostProbableX] = Math.max(value, robot.beaconProbability[mostProbableY][mostProbableX]);

    if (lines.length == 3) { // corner case
      // ignore? not worth the work, besides the beacon won't be at the
      // corner of the matrix...
    } else {

      String[] firstLine = lines[1].split(";");
      String[] secondLine = lines[2].split(";");
      String[] thirdLine = lines[3].split(";");

      if (mostProbableY > Constants.mapSizeY || mostProbableX > Constants.mapSizeX) {
        return;
      }

      if (firstLine.length < 3 || thirdLine.length < 3 || secondLine.length < 2) //point near the wall
      {
        return;
      }

      robot.beaconProbability[mostProbableY - 1][mostProbableX - 1] = Math.max(Integer.parseInt(firstLine[0]), robot.beaconProbability[mostProbableY - 1][mostProbableX - 1]);
      robot.beaconProbability[mostProbableY - 1][mostProbableX] = Math.max(Integer.parseInt(firstLine[1]), robot.beaconProbability[mostProbableY - 1][mostProbableX]);
      robot.beaconProbability[mostProbableY - 1][mostProbableX + 1] = Math.max(Integer.parseInt(firstLine[2]), robot.beaconProbability[mostProbableY - 1][mostProbableX + 1]);

      robot.beaconProbability[mostProbableY][mostProbableX - 1] = Math.max(Integer.parseInt(secondLine[0]), robot.beaconProbability[mostProbableY][mostProbableX - 1]);
      robot.beaconProbability[mostProbableY][mostProbableX + 1] = Math.max(Integer.parseInt(secondLine[1]), robot.beaconProbability[mostProbableY][mostProbableX + 1]);

      robot.beaconProbability[mostProbableY + 1][mostProbableX - 1] = Math.max(Integer.parseInt(thirdLine[0]), robot.beaconProbability[mostProbableY + 1][mostProbableX - 1]);
      robot.beaconProbability[mostProbableY + 1][mostProbableX] = Math.max(Integer.parseInt(thirdLine[1]), robot.beaconProbability[mostProbableY + 1][mostProbableX]);
      robot.beaconProbability[mostProbableY + 1][mostProbableX + 1] = Math.max(Integer.parseInt(thirdLine[2]), robot.beaconProbability[mostProbableY + 1][mostProbableX + 1]);
    }
  }

  /*
   * creates and sends the correct message to the simulator
   */
  public void say() {
    String probableBeacon = getProbableBeacon(1);
    String sensors = getSensors();

    if (robot.cif.GetTime() % 2.0 == 0 
            && robot.cif.GetTime() > 4.0) {
      robot.cif.Say(probableBeacon);
      //System.out.println("sending : " + probableBeacon);
    } else {
      robot.cif.Say(sensors);
      //System.out.println("sending : " + sensors);
    }
  }

  /* gets the sensors of the robot in a string in the following format:
   * sensors|x|offsetX|y|offsetY|frontsensor|leftsensor|rightsensor|compass
   */
  private String getSensors() {
    return "sensors|" + ((int) robot.PosX) + "|" + robot.initialPosX 
            + "|" + ((int) robot.PosY) + "|" + robot.initialPosY 
            + "|" + robot.frontSensor + "|" + robot.leftSensor 
            + "|" + robot.rightSensor + "|" + robot.compass;
  }

  /*
   * receives all the messages from the simulator
   * ignores messages from itself, null and repeated messages
   */
  public void receive() {
    for (int i = 1; i <= 5; i++) {
      if (i == robot.pos || robot.cif.GetMessageFrom(i) == null) {
        continue;
      }
      if (dataToProcess[i - 1].equals(robot.cif.GetMessageFrom(i))) {
        //System.out.println("repeated message from " + i);
        continue;
      } else {
        dataToProcess[i - 1] = robot.cif.GetMessageFrom(i);
        if (dataToProcess[i - 1].contains("sensors")) {
          decodeAndApplySensorsMessage(dataToProcess[i - 1]);
        } else {
          //System.out.println("message from " + i + ": " + dataToProcess[i - 1]);
          decodeAndApplyProbableBeaconMessage(dataToProcess[i - 1]);
        }
      }
    }
  }

  /*
   * decodes the message received from the other mouse about their sensors
   * format: sensors|x|offsetX|y|offsetY|frontsensor|leftsensor|rightsensor|compass
   */
  private void decodeAndApplySensorsMessage(String message) {
    String[] contents = message.split("\\|");
    SensorProbBean sb = new SensorProbBean();
    sb.compass = Double.parseDouble(contents[8]);
    sb.frontSensor = Double.parseDouble(contents[5]);
    sb.leftSensor = Double.parseDouble(contents[6]);
    sb.rightSensor = Double.parseDouble(contents[7]);

    int otherRoboX = Integer.parseInt(contents[1]);
    int otherRoboXoffset = Integer.parseInt(contents[2]);
    int otherRoboY = Integer.parseInt(contents[3]);
    int otherRoboYoffset = Integer.parseInt(contents[4]);

    sb.mapX = otherRoboX + (otherRoboXoffset - robot.initialPosX);
    sb.mapY = otherRoboY - (otherRoboYoffset - robot.initialPosX);

    int[] fsp = Util.frontSensorMapPosition((int) sb.mapX, (int) sb.mapY, sb.compass);
    sb.frontSensorPosX = fsp[0];
    sb.frontSensorPosY = fsp[1];

    int[] lsp = Util.leftSensorMapPosition((int) sb.mapX, (int) sb.mapY, sb.compass);
    sb.leftSensorPosX = lsp[0];
    sb.leftSensorPosY = lsp[1];

    int[] rsp = Util.rightSensorMapPosition((int) sb.mapX, (int) sb.mapY, sb.compass);
    sb.rightSensorPosX = rsp[0];
    sb.rightSensorPosY = rsp[1];
    
    robot.tellObservers(sb);
  }
}
