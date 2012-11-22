import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GetMapArea {

	public static void main(String[] args) {

		int sizeX = 200;
		int sizeY = 200;

		double radius = 70.0;
		double[][] matrix = new double[200][200];

		int centerPosX = sizeX/2;
		int centerPosY = sizeY/2;
		
		JPanel panel = new JPanel();
		JFrame frame = new JFrame("MATRIX");
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(sizeX, sizeY);
		panel.setBackground(Color.CYAN);
		frame.setVisible(true);
		frame.setResizable(true);
		Graphics g = panel.getGraphics();
		
		double angleCenterI = Math.toRadians(-30);
		double angleCenterF = Math.toRadians(30);

		calculateArea(radius, matrix, centerPosX, centerPosY, panel,
				angleCenterI, angleCenterF, g);
		
		g.dispose();
	}

	private static void calculateArea(double radius, double[][] matrix,
			int centerPosX, int centerPosY, JPanel panel, double angleCenterI,
			double angleCenterF, Graphics g) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				
				double anglePoint = Math.atan2(centerPosX - j, centerPosY - i);
				double distanceCenter = Math.sqrt(Math.abs(i - centerPosY)
						* Math.abs(i - centerPosY) + Math.abs(j - centerPosX)
						* Math.abs(j - centerPosX));

				if (distanceCenter < radius && anglePoint > angleCenterI
						&& anglePoint < angleCenterF) {
					g.setColor(Color.RED);
					g.fillRect(j, i, 1, 1);
				}
				
			}
		}
	}
}
