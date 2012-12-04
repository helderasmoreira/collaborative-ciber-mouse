
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;
import sun.text.normalizer.UBiDiProps;

public class BeaconVisualizer extends Thread {

  @Override
  public void run() {
    JPanel panel = new JPanel();
    JFrame frame = new JFrame("Beacon Visualizer");
    frame.add(panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(Constants.mapSizeX, Constants.mapSizeY);
    panel.setBackground(Color.WHITE);
    frame.setVisible(true);

    int index = 0;
    while (true) {
      Graphics g = panel.getGraphics();
      if (jClient.beacon.beaconVisible) {

        double robotDir = jClient.compass;
        double beaconRelDir = jClient.beacon.beaconDir;
        double beaconDir = robotDir + beaconRelDir;
        System.out.println("beacon dir:" + beaconDir);
        double angleI = Math.toRadians(beaconDir - 1);
        double angleF = angleI + Math.toRadians(1);

        angleI = normalizeAngle(angleI);
        angleF = normalizeAngle(angleF);

        int centerPosX = (int) jClient.PosX;
        int centerPosY = (int) jClient.PosY;

        for (int y = 0; y < jClient.beaconProbability.length; y++) {
          for (int x = 0; x < jClient.beaconProbability[y].length; x++) {
            calculateVisibleArea(centerPosX, centerPosY, angleI, angleF, g, x, y);
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
      jClient.beaconProbability[y][x] += 0.1;
      if (jClient.beaconProbability[y][x] > 0.3 &&
              jClient.beaconProbability[y][x] <= 0.4) {
        g.setColor(Color.YELLOW);
        g.fillRect(x, y, 1, 1);
      } else if(jClient.beaconProbability[y][x] > 0.8 &&
              jClient.beaconProbability[y][x] <= 1.2) {
        g.setColor(Color.ORANGE);
        g.fillRect(x, y, 1, 1);
      } else if(jClient.beaconProbability[y][x] > 1.2) {
        g.setColor(Color.RED);
        g.fillRect(x, y, 1, 1);
      }
    }
  }
}
