package collaborobo.visualizers;


import collaborobo.CollaborativeRobot;
import collaborobo.utils.Constants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class MapProbabilitiesVisualizer extends Thread {

  CollaborativeRobot robot;
  
  public MapProbabilitiesVisualizer(CollaborativeRobot robot) {
    this.robot = robot;
  }

  @Override
  public void run() {
    JFrame frame = new JFrame("Map Probabilities Visualizer");
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

      for (int y = 0; y < robot.probabilitiesMap.length; y++) {
        for (int x = 0; x < robot.probabilitiesMap[y].length; x++) {
          if (robot.probabilitiesMap[y][x] > 0.50) {
            g.setColor(Color.RED);
            g.fillRect(x, y, 1, 1);
          } else if (robot.probabilitiesMap[y][x] < 0.5) {
            g.setColor(Color.BLUE);
            g.fillRect(x, y, 1, 1);
          }
        }
      }

      g.dispose();
    }
  }
}
