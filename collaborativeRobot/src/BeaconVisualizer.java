
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BeaconVisualizer extends Thread {

  @Override
  public void run() {
    JPanel panel = new JPanel();
    JFrame frame = new JFrame("Beacon Visualizer - " + CollaborativeRobot.pos);
    frame.add(panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(Constants.mapSizeX, Constants.mapSizeY);
    panel.setBackground(Color.WHITE);
    frame.setVisible(true);

    while (true) {
      Graphics g = panel.getGraphics();
      if (CollaborativeRobot.beacon.beaconVisible) {

        double robotDir = CollaborativeRobot.compass;
        double beaconRelDir = CollaborativeRobot.beacon.beaconDir;
        double beaconDir = robotDir + beaconRelDir;

        double angleI = Math.toRadians(beaconDir - 1);
        double angleF = angleI + Math.toRadians(1);

        angleI = normalizeAngle(angleI);
        angleF = normalizeAngle(angleF);

        int centerPosX = (int) CollaborativeRobot.PosX;
        int centerPosY = (int) CollaborativeRobot.PosY;

        for (int y = 0; y < CollaborativeRobot.beaconProbability.length; y++) {
          for (int x = 0; x < CollaborativeRobot.beaconProbability[y].length; x++) {
            calculateVisibleArea(centerPosX, centerPosY, angleI, angleF, g, x, y);
            if (CollaborativeRobot.beaconProbability[y][x] > 3
                    && CollaborativeRobot.beaconProbability[y][x] <= 4) {
              g.setColor(Color.YELLOW);
              g.fillRect(x, y, 1, 1);
            } else if (CollaborativeRobot.beaconProbability[y][x] > 8
                    && CollaborativeRobot.beaconProbability[y][x] <= 12) {
              g.setColor(Color.ORANGE);
              g.fillRect(x, y, 1, 1);
            } else if (CollaborativeRobot.beaconProbability[y][x] > 12
                    && CollaborativeRobot.beaconProbability[y][x] <= 100) {
              g.setColor(Color.RED);
              g.fillRect(x, y, 1, 1);
            } else if (CollaborativeRobot.beaconProbability[y][x] > 100) {
              g.setColor(Color.BLACK);
              g.fillRect(x, y, 1, 1);
            }
          }
        }
      }
      g.dispose();
    }
  }

  double normalizeAngle(double angle) {
    double newAngle = angle;
    while (newAngle <= -Math.PI) {
      newAngle += 2 * Math.PI;
    }
    while (newAngle > Math.PI) {
      newAngle -= 2 * Math.PI;
    }
    return newAngle;
  }

  private static void calculateVisibleArea(int cx, int cy,
          double ai, double af, Graphics g, int x, int y) {

    double anglePoint;
    anglePoint = Math.atan2(cy - y, x - cx);

    if (anglePoint >= ai && anglePoint <= af) {
      CollaborativeRobot.beaconProbability[y][x] += 1;
    }
  }
}
