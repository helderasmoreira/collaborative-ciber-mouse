/*
    This file is part of ciberRatoToolsSrc.

    Copyright (C) 2001-2011 Universidade de Aveiro

    ciberRatoToolsSrc is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    ciberRatoToolsSrc is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Foobar; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;

import ciberIF.beaconMeasure;
import ciberIF.ciberIF;


/**
 * example of a basic agent
 * implemented using the java interface library.
 */
public class jClient {

	ciberIF cif;

	enum State {RUN, WAIT, RETURN}
	
	static int mapSizeY = 280;
	static int mapSizeX = 560;
 	
	static public double[][] map = new double[mapSizeY][mapSizeX];
	int initialPosX, initialPosY;
	int halfPosX, halfPosY;

	public static void main(String[] args) {

		String host, robName;
		int pos; 
		int arg;

		//default values
		host = "localhost";
		robName = "Bla";
		pos = 1;


		// parse command-line arguments
		try {
			arg = 0;
			while (arg<args.length) {
				if(args[arg].equals("-pos")) {
					if(args.length > arg+1) {
						pos = Integer.valueOf(args[arg+1]).intValue();
						arg += 2;
					}
				}
				else if(args[arg].equals("-robname")) {
					if(args.length > arg+1) {
						robName = args[arg+1];
						arg += 2;
					}
				}
				else if(args[arg].equals("-host")) {
					if(args.length > arg+1) {
						host = args[arg+1];
						arg += 2;
					}
				}
				else throw new Exception();
			}
		}
		catch (Exception e) {
			print_usage();
			return;
		}

		// create client
		jClient client = new jClient();

		client.robName = robName;

		// register robot in simulator
		client.cif.InitRobot(robName, pos, host);

		// main loop
		client.mainLoop();

	}

	// Constructor
	jClient() {
		cif = new ciberIF();
		beacon = new beaconMeasure();
		beaconToFollow = 0;
		ground = -1;
		state = State.RUN;
		halfPosX = mapSizeX / 2;
		halfPosY = mapSizeY / 2;
	}

	/** 
	 * reads a new message, decides what to do and sends action to simulator
	 */
	public void mainLoop () {
		
		PrimeThread p = new PrimeThread();
	    p.start();
	    
	    cif.ReadSensors();
	    
	    initialPosX = (int) (cif.GetX()*10.0);
		initialPosY = (int) (cif.GetY()*10.0);
		
		updateMap();
	    
		while(true) {
			cif.ReadSensors();
			decide();
		}
	}

	private void updateMap() {
		map[(int) (initialPosY-cif.GetY()*10.0+halfPosY)][(int) (cif.GetX()*10.0-initialPosX+halfPosX)] = 1.0;
	}
	
	public void getInfo() {
		if(cif.IsObstacleReady(0))
			irSensor0 = cif.GetObstacleSensor(0);
		if(cif.IsObstacleReady(1))
			irSensor1 = cif.GetObstacleSensor(1);
		if(cif.IsObstacleReady(2))
			irSensor2 = cif.GetObstacleSensor(2);
		if(cif.IsGroundReady())
			ground = cif.GetGroundSensor();
		if(cif.IsBeaconReady(beaconToFollow))
			beacon = cif.GetBeaconSensor(beaconToFollow);
	}
	
	public void requestInfo() {
		cif.Say(robName);
		cif.RequestIRSensor(0);
		cif.RequestIRSensor(1);
		cif.RequestIRSensor(2);
		cif.RequestBeaconSensor(beaconToFollow);
		cif.RequestGroundSensor();
		//System.out.println(cif.GetX() + " " + cif.GetX() );
	}
	
	public void original_wander(boolean followBeacon) {
	    if(irSensor0>2.0 || irSensor1>2.0 ||  irSensor2>2.0) 
		    cif.DriveMotors(0.1,-0.1);
	    else if(irSensor1>1.0) cif.DriveMotors(0.1,0.0);
	    else if(irSensor2>1.0) cif.DriveMotors(0.0,0.1);
	    else if(followBeacon && beacon.beaconVisible && beacon.beaconDir > 20.0) 
	    cif.DriveMotors(0.0,0.1);
	    else if(followBeacon && beacon.beaconVisible && beacon.beaconDir < -20.0) 
	    cif.DriveMotors(0.1,0.0);
	    else cif.DriveMotors(0.1,0.1);
	}

	public void wander(boolean followBeacon) {
		// verifica se há algum obstáculo a evitar
		if(irSensor0 > 2.0 || irSensor1 > 2.0 ||  irSensor2 > 2.0)
			processWall();
		// se não houver obstáculos e o beacon não estiver enquandrado, roda
		else if(followBeacon && beacon.beaconVisible && beacon.beaconDir > 10.0) 
			cif.DriveMotors(0.0,0.1);
		else if(followBeacon && beacon.beaconVisible && beacon.beaconDir < -10.0) 
			cif.DriveMotors(0.1,0.0);
		// caso contrário, anda em frente
		else cif.DriveMotors(0.1,0.1);
		
		updateMap();
		
	}

	private void processWall() {
		
		// enquanto o beacon não estiver enquadrado ou não estiver visível, percorre a parede
		while(beacon.beaconDir > 15.0 || beacon.beaconDir < -15.0 || !beacon.beaconVisible)
		{
			// se bater, tenta rodar para sair da situação
			if(cif.GetBumperSensor()) {
				cif.DriveMotors(-0.15, 0.15);
			}
			
			requestInfo();
			cif.ReadSensors();
			getInfo();
			
			// se estiver numa esquina, roda no sentido do relógio
			if (irSensor2 < 2.0 && irSensor0 < 2.0 && irSensor2 < 3.0) {
				cif.DriveMotors(0.1, -0.1);
			// se estiver num canto, roda no sentido contrário ao do relógio
			} else if (irSensor0 <= 2.0 && irSensor1 <= 2.0 && irSensor2 <= 6.0) {
				cif.DriveMotors(0.1, 0.1);
			// caso não haja obstáculo ou esteja perto da parede, anda
			} else if (irSensor0 >= 2.0 || irSensor2 > 6.0) {
				cif.DriveMotors(-0.1, 0.1);
			}
			
			updateMap();

		}
		
		// se o beacon estiver visível, tenta desviar-se dos obstáculos
		if(beacon.beaconVisible) {
			if(irSensor0 > 2.0 && irSensor1 >= irSensor2) cif.DriveMotors(0.1,-0.1);
			else if(irSensor0 > 2.0 && irSensor1 < irSensor2) cif.DriveMotors(-0.1,0.1);
			else if(irSensor1>2.0) cif.DriveMotors(0.1,0.0);
			else if(irSensor2>2.0) cif.DriveMotors(0.0,0.1);
		}
		
		//System.out.println(irSensor1 + " " + irSensor0 + " " + irSensor2);
	}

	/**
	 * basic reactive decision algorithm, decides action based on current sensor values
	 */
	public void decide() {
		getInfo();

		//System.out.println("Measures: ir0=" + irSensor0 + " ir1=" + irSensor1 + " ir2=" + irSensor2 + "\n");
		//System.out.println("Measures: x=" + x + " y=" + y + " dir=" + dir);

		//System.out.println(robName + " state " + state);
		
		
		
		switch(state) {
		case RUN:    /* Go */
			if( cif.GetVisitingLed() ) state = State.WAIT;
			if( ground == 0 ) {         /* Visit Target */
				cif.SetVisitingLed(true);
				//System.out.println(robName + " visited target at " + cif.GetTime() + "\n");
			}

			else {
				wander(true);
			}
			break;
		case WAIT: /* Wait for others to visit target */
			if(cif.GetReturningLed()) state = State.RETURN;

			cif.DriveMotors(0.0,0.0);
			break;
		case RETURN: /* Return to home area */

			if( cif.GetFinished() ) System.exit(0); /* Terminate agent */
			if( ground == 1) { /* Finish */
				cif.Finish();
				System.out.println(robName + " found home at " + cif.GetTime() + "\n");
			}
			else {
				original_wander(false);
			}
			break;

		}

		requestInfo();
	}

	static void print_usage() {
		System.out.println("Usage: java jClient [-robname <robname>] [-pos <pos>] [-host <hostname>[:<port>]]");
	}

	private String robName;
	private double irSensor0, irSensor1, irSensor2;
	private beaconMeasure beacon;
	private int    ground;
	private State state;

	private int beaconToFollow;
};

class PrimeThread extends Thread {
    PrimeThread() {
    }

    public void run() {
    	//Create a JPanel
    	 JPanel panel=new JPanel();

    	 //Create a JFrame that we will use to add the JPanel
    	 JFrame frame=new JFrame("Create a JPanel");

    	 //ADD JPanel INTO JFrame
    	 frame.add(panel);

    	 //Set default close operation for JFrame
    	 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    	 //Set JFrame size to :
    	 frame.setSize(jClient.mapSizeX,jClient.mapSizeY);
    	 
    	 panel.setBackground(Color.WHITE);

    	 //Make JFrame visible. So we can see it
    	 frame.setVisible(true);
    	 
    	 frame.setResizable(true);
    	 
    	 
    	  
    	while(true) {
    		Graphics g = panel.getGraphics();
	        for(int i = 0; i < jClient.map.length; i++)
	        {
	        	for(int j = 0; j< jClient.map[i].length; j++)
	        		if(jClient.map[i][j] == 1.0) {
	        			
	        			
	        			g.setColor(Color.RED);
	        			g.fillRect(j, i, 1, 1);
	        			
	        		}
	        }
	        
	        g.dispose();
	        
    	}
    }
}



