
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MapProbabilitiesVisualizer extends Thread {

  MapProbabilitiesVisualizer() {
  }

  @Override
  public void run() {
    JFrame frame = new JFrame("Map Probabilities Visualizer");
    JPanel panel = new JPanel();
    frame.add(panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(Constants.mapSizeX, Constants.mapSizeY);
    frame.setPreferredSize(new Dimension(Constants.mapSizeX, Constants.mapSizeY));
    panel.setBackground(Color.WHITE);
    frame.setVisible(true);
    frame.pack();

    while (true) {
      Graphics g = panel.getGraphics();

      for (int y = 0; y < CollaborativeRobot.probabilitiesMap.length; y++) {
        for (int x = 0; x < CollaborativeRobot.probabilitiesMap[y].length; x++) {
          if (CollaborativeRobot.probabilitiesMap[y][x] > 0.50) {
            g.setColor(Color.RED);
            g.fillRect(x, y, 1, 1);
          } else if (CollaborativeRobot.probabilitiesMap[y][x] < 0.5) {
            g.setColor(Color.BLUE);
            g.fillRect(x, y, 1, 1);
          }
        }
      }

      g.dispose();
    }
  }
}
