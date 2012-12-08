import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class MapProbabilitiesVisualizer extends Thread {

	MapProbabilitiesVisualizer() {
		
	}

	public void run() {

		JPanel panel = new JPanel();
		JFrame frame = new JFrame("Map Probabilities Visualizer");
		frame.add(panel);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(Constants.mapSizeX, Constants.mapSizeY);
		panel.setBackground(Color.WHITE);
		frame.setVisible(true);

		while (true) {
			Graphics g = panel.getGraphics();
			for (int i = 0; i < CollaborativeRobot.probabilitiesMap.length; i++) {
				for (int j = 0; j < CollaborativeRobot.probabilitiesMap[i].length; j++)
				{
					if (CollaborativeRobot.probabilitiesMap[i][j] > 0.50) {
						g.setColor(Color.RED);
						g.fillRect(j, i, 1, 1);
					}
					else if (CollaborativeRobot.probabilitiesMap[i][j] < 0.5){
						g.setColor(Color.BLUE);
						g.fillRect(j, i, 1, 1);
					}
				}
			}
					
			g.dispose();
		}
		
	}
	
	
}
