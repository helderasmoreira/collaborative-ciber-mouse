package collaborobo.visualizers;

import collaborobo.CollaborativeRobot;
import collaborobo.utils.Constants;
import collaborobo.utils.Util;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BeaconVisualizer extends Thread {

  CollaborativeRobot robot;

  public BeaconVisualizer(CollaborativeRobot robot) {
    this.robot = robot;
  }

  @Override
  public void run() {
    JFrame frame = new JFrame("Beacon Visualizer - " + robot.pos);
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
      if (robot.beacon.beaconVisible && robot.cif.GetTime() > 4.0) {

        double robotDir = robot.compass;
        double beaconRelDir = robot.beacon.beaconDir;
        double beaconDir = robotDir + beaconRelDir;

        double angleI = Math.toRadians(beaconDir - 1);
        double angleF = angleI + Math.toRadians(1);

        angleI = Util.normalizeAngle(angleI);
        angleF = Util.normalizeAngle(angleF);

        int centerPosX = (int) robot.PosX;
        int centerPosY = (int) robot.PosY;

        for (int y = 0; y < robot.beaconProbability.length; y++) {
          for (int x = 0; x < robot.beaconProbability[y].length; x++) {
            calculateVisibleArea(centerPosX, centerPosY, angleI, angleF, g, x, y);
            int prob = robot.beaconProbability[y][x];
            if (prob > 3 && prob <= 4) {
              g.setColor(Color.YELLOW);
              g.fillRect(x, y, 1, 1);
            } else if (prob > 8 && prob <= 12) {
              g.setColor(Color.ORANGE);
              g.fillRect(x, y, 1, 1);
            } else if (prob > 12 && prob <= 100) {
              g.setColor(Color.RED);
              g.fillRect(x, y, 1, 1);
            } else if (prob > 100) {
              g.setColor(Color.BLACK);
              g.fillRect(x, y, 1, 1);
            }
          }
        }
      }
      g.dispose();
    }
  }

  private void calculateVisibleArea(int cx, int cy,
          double ai, double af, Graphics g, int x, int y) {

    double anglePoint;
    anglePoint = Math.atan2(cy - y, x - cx);

    if (anglePoint >= ai && anglePoint <= af) {
      robot.beaconProbability[y][x] += 1;
    }
  }
}
