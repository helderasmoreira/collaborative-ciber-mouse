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
      int wallColor = 255;
      int colorStep = wallColor/6;      

      for (int y = 0; y < robot.probabilitiesMap.length; y++) {
        for (int x = 0; x < robot.probabilitiesMap[y].length; x++) {
            double cellProb = robot.probabilitiesMap[y][x];
            if(cellProb == -3.0){
            	g.setColor(Color.GREEN);
                g.fillRect(x, y, 1, 1);
            }
            if (cellProb >= 1) {
                g.setColor(new Color(wallColor, 0, 0));
                g.fillRect(x, y, 1, 1);
            }else if(cellProb >= 0.9){
                g.setColor(new Color(wallColor, colorStep, colorStep));
                g.fillRect(x, y, 1, 1);
            }else if(cellProb >= 0.8){
                g.setColor(new Color(wallColor, 2*colorStep, 2*colorStep));
                g.fillRect(x, y, 1, 1);
            }else if(cellProb >= 0.7){
                g.setColor(new Color(wallColor, 3*colorStep, 3*colorStep));
                g.fillRect(x, y, 1, 1);
            }else if(cellProb >= 0.6){
                g.setColor(new Color(wallColor, 4*colorStep, 4*colorStep));
                g.fillRect(x, y, 1, 1);
            }else if(cellProb > 0.5){
                g.setColor(new Color(wallColor, 5*colorStep, 5*colorStep));
                g.fillRect(x, y, 1, 1);
            }else if(cellProb == 0.5){
                g.setColor(Color.WHITE);
                g.fillRect(x, y, 1, 1);
            }else if(cellProb >= 0.4){
                g.setColor(new Color(255-colorStep, 255-colorStep, 255-colorStep));
                g.fillRect(x, y, 1, 1);
            }else if(cellProb >= 0.3){
                g.setColor(new Color(255-2*colorStep, 255-2*colorStep, 255-2*colorStep));
                g.fillRect(x, y, 1, 1);
            }else if(cellProb >= 0.2){
                g.setColor(new Color(255-3*colorStep, 255-3*colorStep, 255-3*colorStep));
                g.fillRect(x, y, 1, 1);
            }else if(cellProb >= 0.1){
                g.setColor(new Color(255-4*colorStep, 255-4*colorStep, 255-4*colorStep));
                g.fillRect(x, y, 1, 1);
            }else if(cellProb > 0.0){
                g.setColor(new Color(255-5*colorStep, 255-5*colorStep, 255-5*colorStep));
                g.fillRect(x, y, 1, 1);
            }else if(cellProb == 0.0){
                g.setColor(Color.BLACK);
                g.fillRect(x, y, 1, 1);
            }
        }
      }

      g.dispose();
    }
  }
}
