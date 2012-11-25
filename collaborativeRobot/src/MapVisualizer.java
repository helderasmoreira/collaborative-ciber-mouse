import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MapVisualizer extends Thread {

	MapVisualizer() {
		
	}

	public void run() {

		JPanel panel = new JPanel();
		JFrame frame = new JFrame("Map Visualizer");
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(jClient.mapSizeX, jClient.mapSizeY);
		panel.setBackground(Color.WHITE);
		frame.setVisible(true);

		while (true) {
			Graphics g = panel.getGraphics();
			for (int i = 0; i < jClient.map.length; i++) {
				for (int j = 0; j < jClient.map[i].length; j++)
					if (jClient.map[i][j] == 1.0) {
						g.setColor(Color.RED);
						g.fillRect(j, i, 1, 1);
					} else if (jClient.map[i][j] == -1.0) {
            g.setColor(Color.BLUE);
						g.fillRect(j, i, 1, 1);
          } else if (jClient.map[i][j] == -1.1) {
            g.setColor(Color.GREEN);
						g.fillRect(j, i, 1, 1);
          } else if (jClient.map[i][j] == -1.2) {
            g.setColor(Color.ORANGE);
						g.fillRect(j, i, 1, 1);
          }
			}
			
			double compassRadians = Math.toRadians(jClient.compass);
			double angleCenterI = angleDifference(compassRadians, Math.toRadians(60));
			double angleCenterF = angleCenterI + Math.toRadians(60);
			
			calculateArea(10.0, jClient.map, jClient.frontSensorPosX, jClient.frontSensorPosY, panel,
					angleCenterI, angleCenterF, g);
			
			calculateArea(10.0, jClient.map, jClient.leftSensorPosX, jClient.leftSensorPosY, panel,
					angleCenterI-Math.toRadians(60), angleCenterF-Math.toRadians(60), g);
			
			calculateArea(10.0, jClient.map, jClient.rightSensorPosX, jClient.rightSensorPosY, panel,
					angleCenterI+Math.toRadians(60), angleCenterF+Math.toRadians(60), g);
			g.dispose();
		}
		
	}
	
	private static void calculateArea(double radius, double[][] matrix,
			int centerPosX, int centerPosY, JPanel panel, double angleCenterI,
			double angleCenterF, Graphics g) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				
				double anglePoint = Math.atan2(j - centerPosX, centerPosY - i);
				double distanceCenter = Math.sqrt(Math.abs(i - centerPosY)
						* Math.abs(i - centerPosY) + Math.abs(j - centerPosX)
						* Math.abs(j - centerPosX));

				if (distanceCenter < radius && anglePoint > angleCenterI
						&& anglePoint < angleCenterF) {
					g.setColor(Color.MAGENTA);
					g.fillRect(j, i, 1, 1);
				}
				
			}
		}
	}
	
	public static double angleDifference(double alpha, double beta) {

		double deltatheta = 0;

		if ((alpha < 0 && beta < 0) || (alpha > 0 && beta > 0)) {
			deltatheta = beta - alpha;
		} else {
			if (Math.abs(alpha) + Math.abs(beta) <= Math.PI)
				deltatheta = (Math.abs(alpha) + Math.abs(beta));
			else
				deltatheta = -Math.abs(2 * Math.PI - Math.abs(alpha - beta));
			if (beta < alpha)
				deltatheta *= -1;
		}
		return deltatheta;
	}
}
