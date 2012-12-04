
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class BeaconVisualizer extends Thread {

  @Override
  public void run() {
    JPanel panel = new JPanel();
		JFrame frame = new JFrame("Beacon Visualizer");
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(jClient.mapSizeX, jClient.mapSizeY);
		panel.setBackground(Color.WHITE);
    frame.setVisible(true);

    while (true) {
      Graphics g = panel.getGraphics();
      for (int i = 0; i < jClient.beaconProbability.length; i++) {
        for (int j = 0; j < jClient.beaconProbability[i].length; j++) {
          if(jClient.beacon.beaconVisible){
            double beaconDir = jClient.beacon.beaconDir;
            double angleI = beaconDir - 2;
            double angleF = beaconDir + 2;
            int centerPosX = (int) jClient.PosX;
            int centerPosY = (int) jClient.PosY;
            calculateVisibleArea(centerPosX, centerPosY, angleI, angleF, g, i, j);
          }
        }
      }    
      g.dispose();
    }
  }
  
  private static void calculateVisibleArea(int centerPosX2,int centerPosY2,
          double angleCenterI, double angleCenterF, Graphics g, int i, int j) {
		
		double anglePoint;
		anglePoint = Math.atan2(j - centerPosX2, centerPosY2 - i);

		if (anglePoint > angleCenterI && anglePoint < angleCenterF) {
			g.setColor(Color.RED);
			g.fillRect(j, i, 1, 1);
		}
	}
    
}
