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
		frame.setSize(Constants.mapSizeX, Constants.mapSizeY);
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
						//g.fillRect(j, i, 1, 1);
          } else if (jClient.map[i][j] == -1.1) {
            g.setColor(Color.GREEN);
						//g.fillRect(j, i, 1, 1);
          } else if (jClient.map[i][j] == -1.2) {
            g.setColor(Color.ORANGE);
						//g.fillRect(j, i, 1, 1);
          }
          else if(jClient.map[i][j] == -3.0) {
        	  g.setColor(Color.ORANGE);
        	  g.fillRect(j, i, 1, 1);
          }
			}
			
			double compassRadians = Math.toRadians(jClient.compass);
			double angleCenterI = compassRadians + Math.toRadians(-30);
			double angleCenterF = compassRadians + Math.toRadians(30);
			
			angleCenterI = normalizeAngle(angleCenterI);
			angleCenterF = normalizeAngle(angleCenterF);
			
			double angleCenterI2 = angleCenterI+Math.toRadians(60);
			double angleCenterF2 = angleCenterF+Math.toRadians(60);
			angleCenterI2 = normalizeAngle(angleCenterI2);
			angleCenterF2 = normalizeAngle(angleCenterF2);
			
			double angleCenterI3 = angleCenterI-Math.toRadians(60);
			double angleCenterF3 = angleCenterF-Math.toRadians(60);
			angleCenterI3 = normalizeAngle(angleCenterI3);
			angleCenterF3 = normalizeAngle(angleCenterF3);
			
			calculateArea(10.3, jClient.map, jClient.frontSensorPosX, jClient.frontSensorPosY, 
					jClient.leftSensorPosX, jClient.leftSensorPosY,
					jClient.rightSensorPosX, jClient.rightSensorPosY, panel,
					angleCenterI, angleCenterF, 
					angleCenterI2, angleCenterF2,
					angleCenterI3, angleCenterF3,
					g, jClient.frontSensor, jClient.leftSensor, jClient.rightSensor);
      
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
				if(sensorValue > 0.0) {
					obstacleDistance = (1.0/sensorValue)*10.0;
					calculateVisibleArea(radius, centerPosX, centerPosY,
							angleCenterI, angleCenterF, g, obstacleDistance, i, j);
				}
				
				// LEFT
				if(sensorValue2 > 0.0) {
					obstacleDistance = (1.0/sensorValue2)*10.0;
					calculateVisibleArea(radius, centerPosX2, centerPosY2,
							angleCenterI2, angleCenterF2, g, obstacleDistance, i, j);
				}
				
				// RIGHT
				if(sensorValue3 > 0.0) {
					obstacleDistance = (1.0/sensorValue3)*10.0;
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
				&& anglePoint < angleCenterF  ) {
			g.setColor(Color.GREEN);
			g.fillRect(j, i, 1, 1);
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
	
}
