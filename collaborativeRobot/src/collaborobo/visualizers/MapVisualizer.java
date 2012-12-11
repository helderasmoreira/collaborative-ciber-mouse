package collaborobo.visualizers;

import collaborobo.CollaborativeRobot;
import collaborobo.utils.Constants;
import collaborobo.utils.Util;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MapVisualizer extends Thread {

  CollaborativeRobot robot;

  public MapVisualizer(CollaborativeRobot robot) {
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
      for (int y = 0; y < robot.map.length; y++) {
        for (int x = 0; x < robot.map[y].length; x++) {
          if (robot.map[y][x] == 1.0) {
            g.setColor(Color.RED);
            g.fillRect(x, y, 1, 1);
          } else if (robot.map[y][x] == -1.0) {
            g.setColor(Color.BLUE);
            //g.fillRect(j, i, 1, 1);
          } else if (robot.map[y][x] == -1.1) {
            g.setColor(Color.GREEN);
            //g.fillRect(j, i, 1, 1);
          } else if (robot.map[y][x] == -1.2) {
            g.setColor(Color.ORANGE);
            //g.fillRect(j, i, 1, 1);
          } else if (robot.map[y][x] == -3.0) {
            g.setColor(Color.ORANGE);
            g.fillRect(x, y, 1, 1);
          }
        }
      }

      double compassRadians = Math.toRadians(robot.compass);
//      System.out.println("Compass: " + compassRadians);
//      System.out.println("Compass: " + Util.compassToDeg(robot.compass));
      double angleCenterI = compassRadians - Math.toRadians(30);
      double angleCenterF = compassRadians + Math.toRadians(30);
      
      angleCenterI = Util.normalizeAngle(angleCenterI);
      angleCenterF = Util.normalizeAngle(angleCenterF);
//      System.out.println("AngleIN: " + angleCenterI);
//      System.out.println("AngleFN: " + angleCenterF);

      double angleCenterI2 = compassRadians + Math.toRadians(30);
      double angleCenterF2 = compassRadians + Math.toRadians(90);
      angleCenterI2 = Util.normalizeAngle(angleCenterI2);
      angleCenterF2 = Util.normalizeAngle(angleCenterF2);

      double angleCenterI3 = compassRadians - Math.toRadians(90);
      double angleCenterF3 = compassRadians - Math.toRadians(30);
      angleCenterI3 = Util.normalizeAngle(angleCenterI3);
      angleCenterF3 = Util.normalizeAngle(angleCenterF3);

      calculateArea(10.3, robot.map, robot.frontSensorPosX, robot.frontSensorPosY,
              robot.leftSensorPosX, robot.leftSensorPosY,
              robot.rightSensorPosX, robot.rightSensorPosY, panel,
              angleCenterI, angleCenterF,
              angleCenterI2, angleCenterF2,
              angleCenterI3, angleCenterF3,
              g, robot.frontSensor, robot.leftSensor, robot.rightSensor);

      g.dispose();
    }

  }

  private void calculateArea(double radius, double[][] matrix,
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
        if (sensorValue > -10.0) {
          obstacleDistance = (1.0 / sensorValue) * 10.0;
          calculateVisibleArea(radius, centerPosX, centerPosY,
                  angleCenterI, angleCenterF, g, obstacleDistance, i, j,Color.RED);
        }

        // LEFT
        if (sensorValue2 > -10.0) {
          obstacleDistance = (1.0 / sensorValue2) * 10.0;
          calculateVisibleArea(radius, centerPosX2, centerPosY2,
                  angleCenterI2, angleCenterF2, g, obstacleDistance, i, j, Color.BLUE);
        }

        // RIGHT
        if (sensorValue3 > -10.0) {
          obstacleDistance = (1.0 / sensorValue3) * 10.0;
          calculateVisibleArea(radius, centerPosX3, centerPosY3,
                  angleCenterI3, angleCenterF3, g, obstacleDistance, i, j, Color.GREEN);
        }
      }
    }
  }

  private void calculateVisibleArea(double radius, int cx,
          int cy, double angleCenterI, double angleCenterF,
          Graphics g, double obstacleDistance, int y, int x, Color c) {

    double anglePoint;
    double distanceCenter;
    anglePoint = Math.atan2(cy - y, x - cx);

    distanceCenter = Math.sqrt((y - cy) * (y - cy) + (x - cx) * (x - cx));
//    System.out.println("dc: " + (distanceCenter < radius)  );
//    System.out.println("ap>=i: " + (anglePoint >= angleCenterI)  );
//    System.out.println("ap<=f: " + (anglePoint <= angleCenterF)  );
    
    boolean cond = anglePoint >= angleCenterI && anglePoint <= angleCenterF;
    if (angleCenterI > angleCenterF) {
      cond = (anglePoint >= angleCenterI || anglePoint <= angleCenterF);
    }

    if (distanceCenter < radius && cond) {
      g.setColor(c);
      g.fillRect(x, y, 1, 1);
    }
  }
}
