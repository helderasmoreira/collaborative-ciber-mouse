
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MapVisualizer extends Thread {
  
  CollaborativeRobot robot;

  MapVisualizer(CollaborativeRobot robot) {
    this.robot = robot;
  }

  @Override
  public void run() {

    JFrame frame = new JFrame("Map Visualizer");
    JPanel panel = new JPanel();
    panel.setBackground(Color.WHITE);
    frame.add(panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(Constants.mapSizeX, Constants.mapSizeY);
    frame.setPreferredSize(new Dimension(Constants.mapSizeX, Constants.mapSizeY));
    frame.setVisible(true);
    frame.pack();

    while (true) {
      Graphics g = panel.getGraphics();
      for (int i = 0; i < CollaborativeRobot.map.length; i++) {
        for (int j = 0; j < CollaborativeRobot.map[i].length; j++) {
          if (CollaborativeRobot.map[i][j] == 1.0) {
            g.setColor(Color.RED);
            g.fillRect(j, i, 1, 1);
          } else if (CollaborativeRobot.map[i][j] == -1.0) {
            g.setColor(Color.BLUE);
            //g.fillRect(j, i, 1, 1);
          } else if (CollaborativeRobot.map[i][j] == -1.1) {
            g.setColor(Color.GREEN);
            //g.fillRect(j, i, 1, 1);
          } else if (CollaborativeRobot.map[i][j] == -1.2) {
            g.setColor(Color.ORANGE);
            //g.fillRect(j, i, 1, 1);
          } else if (CollaborativeRobot.map[i][j] == -3.0) {
            g.setColor(Color.ORANGE);
            g.fillRect(j, i, 1, 1);
          }
        }
      }

      double compassRadians = Math.toRadians(robot.compass);
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

      calculateArea(10.3, CollaborativeRobot.map, CollaborativeRobot.frontSensorPosX, CollaborativeRobot.frontSensorPosY,
              CollaborativeRobot.leftSensorPosX, CollaborativeRobot.leftSensorPosY,
              CollaborativeRobot.rightSensorPosX, CollaborativeRobot.rightSensorPosY, panel,
              angleCenterI, angleCenterF,
              angleCenterI2, angleCenterF2,
              angleCenterI3, angleCenterF3,
              g, CollaborativeRobot.frontSensor, CollaborativeRobot.leftSensor, CollaborativeRobot.rightSensor);

      g.dispose();
    }

  }

  private static void calculateArea(double radius, double[][] matrix,
          int centerPosX, int centerPosY, int centerPosX2, int centerPosY2, int centerPosX3, int centerPosY3, JPanel panel,
          double angleCenterI, double angleCenterF,
          double angleCenterI2, double angleCenterF2,
          double angleCenterI3, double angleCenterF3,
          Graphics g, double sensorValue, double sensorValue2, double sensorValue3) {

    g.setColor(Color.BLUE);
    g.fillRect(centerPosX, centerPosY, 2, 2);

    g.setColor(Color.BLUE);
    g.fillRect(centerPosX2, centerPosY2, 2, 2);

    g.setColor(Color.BLUE);
    g.fillRect(centerPosX3, centerPosY3, 2, 2);

    for (int i = 0; i < matrix.length; i++) {
      for (int j = 0; j < matrix[i].length; j++) {

        double obstacleDistance;
        // FRONT
        if (sensorValue > 0.0) {
          obstacleDistance = (1.0 / sensorValue) * 10.0;
          calculateVisibleArea(radius, centerPosX, centerPosY,
                  angleCenterI, angleCenterF, g, obstacleDistance, i, j);
        }

        // LEFT
        if (sensorValue2 > 0.0) {
          obstacleDistance = (1.0 / sensorValue2) * 10.0;
          calculateVisibleArea(radius, centerPosX2, centerPosY2,
                  angleCenterI2, angleCenterF2, g, obstacleDistance, i, j);
        }

        // RIGHT
        if (sensorValue3 > 0.0) {
          obstacleDistance = (1.0 / sensorValue3) * 10.0;
          calculateVisibleArea(radius, centerPosX3, centerPosY3,
                  angleCenterI3, angleCenterF3, g, obstacleDistance, i, j);
        }
      }
    }
  }

  private static void calculateVisibleArea(double radius, int centerPosX2,
          int centerPosY2, double angleCenterI, double angleCenterF,
          Graphics g, double obstacleDistance, int i, int j) {

    double anglePoint;
    double distanceCenter;
    anglePoint = Math.atan2(centerPosY2 - i, j - centerPosX2);

    distanceCenter = Math.sqrt(Math.abs(i - centerPosY2)
            * Math.abs(i - centerPosY2) + Math.abs(j - centerPosX2)
            * Math.abs(j - centerPosX2));

    if (distanceCenter < radius && anglePoint > angleCenterI
            && anglePoint < angleCenterF) {
      g.setColor(Color.GREEN);
      g.fillRect(j, i, 1, 1);
    }
  }
}
