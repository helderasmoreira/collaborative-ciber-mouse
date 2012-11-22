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

import ciberIF.beaconMeasure;
import ciberIF.ciberIF;

/**
 * example of a basic agent implemented using the java interface library.
 */
public class jClient {

	enum State {
		RUN, WAIT, RETURN
	}
	
	public static final double mapPrecision = 10.0;
	public static final int mapSizeY = 280;
	public static final int mapSizeX = 560;
	public static int pos;
	static public double[][] map = new double[mapSizeY][mapSizeX];
	static public double[][] beaconProbability = new double[mapSizeY][mapSizeX];
	int initialPosX, initialPosY;
	int halfPosX, halfPosY;
	
	public static String[] dataToProcess = new String[5];
	
	ciberIF cif;

	public static void main(String[] args) {

		String host, robName;
		int arg;

		// default values
		host = "localhost";
		robName = "Bla";
		pos = 2;

		// parse command-line arguments
		try {
			arg = 0;
			while (arg < args.length) {
				if (args[arg].equals("-pos")) {
					if (args.length > arg + 1) {
						pos = Integer.valueOf(args[arg + 1]).intValue();
						arg += 2;
					}
				} else if (args[arg].equals("-robname")) {
					if (args.length > arg + 1) {
						robName = args[arg + 1];
						arg += 2;
					}
				} else if (args[arg].equals("-host")) {
					if (args.length > arg + 1) {
						host = args[arg + 1];
						arg += 2;
					}
				} else
					throw new Exception();
			}
		} catch (Exception e) {
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
	public void mainLoop() {

		MapVisualizer visualizer = new MapVisualizer();
		visualizer.start();

		cif.ReadSensors();

		initialPosX = (int) (cif.GetX() * mapPrecision);
		initialPosY = (int) (cif.GetY() * mapPrecision);

		updateMap();
		
		for(int i=0;i<beaconProbability.length;i++)
			for(int j=0;j<beaconProbability[i].length;j++)
				beaconProbability[i][j] = i*j;

		while (true) {
			cif.ReadSensors();
			decide();
		}
	}

	private void updateMap() {
		
		for(int i = 1; i <= 5; i++) {
			dataToProcess[i-1] = cif.GetMessageFrom(i);
			System.out.println("message from " + i + ": " + dataToProcess[i-1]);
		}
		
		map[(int) (initialPosY - cif.GetY() * mapPrecision + halfPosY)][(int) (cif
				.GetX() * mapPrecision - initialPosX + halfPosX)] = 1.0;
	}


	private String getWallsNearBeacon(int numberOfPoints) {
		// TODO Auto-generated method stub
		return null;
	}
	
	/* returns a string with the format:
	 * "x-y-value;value;value;value;..."
	 * values from top-left to bottom-right from left to right
	 * in a normal case it would be 8(+1) values 
	 * but we need to consider limit cases so they can be less than 8
	 */
	private String getProbableBeacon(int radius) {
		
		double max = 0.0;
		int iMax = 0;
		int jMax = 0;
		for(int i = 0; i < beaconProbability.length; i++)
			for(int j = 0; j< beaconProbability[i].length; j++)
				if (beaconProbability[i][j] > max) {
					iMax = i;
					jMax = j;
					max = beaconProbability[i][j];
				}
		
		String ret = iMax + "-" + jMax + "-" + beaconProbability[iMax][jMax];
		
		for(int i = Math.max(iMax - radius, 0); i <= Math.min(iMax + radius, mapSizeY-1); i++)
			for(int j = Math.max(jMax - radius, 0); j <= Math.min(jMax + radius, mapSizeX-1); j++)
				ret += ";" + beaconProbability[i][j];
			
		return ret;
	}

	public void getInfo() {
		if (cif.IsObstacleReady(0))
			irSensor0 = cif.GetObstacleSensor(0);
		if (cif.IsObstacleReady(1))
			irSensor1 = cif.GetObstacleSensor(1);
		if (cif.IsObstacleReady(2))
			irSensor2 = cif.GetObstacleSensor(2);
		if (cif.IsGroundReady())
			ground = cif.GetGroundSensor();
		if (cif.IsBeaconReady(beaconToFollow))
			beacon = cif.GetBeaconSensor(beaconToFollow);
		
		String probableBeacon = getProbableBeacon(1);
		String probableWallsNearBeacon = getWallsNearBeacon(3);
		
		cif.Say(cif.GetX() + ";" + cif.GetY() +";" + irSensor0 + ";" + irSensor1 + ";" + irSensor2);
	}

	public void requestInfo() {
		
		cif.RequestIRSensor(0);
		cif.RequestIRSensor(1);
		cif.RequestIRSensor(2);
		cif.RequestBeaconSensor(beaconToFollow);
		cif.RequestGroundSensor();
	}

	public void original_wander(boolean followBeacon) {
		if (irSensor0 > 2.0 || irSensor1 > 2.0 || irSensor2 > 2.0)
			cif.DriveMotors(0.1, -0.1);
		else if (irSensor1 > 1.0)
			cif.DriveMotors(0.1, 0.0);
		else if (irSensor2 > 1.0)
			cif.DriveMotors(0.0, 0.1);
		else if (followBeacon && beacon.beaconVisible
				&& beacon.beaconDir > 20.0)
			cif.DriveMotors(0.0, 0.1);
		else if (followBeacon && beacon.beaconVisible
				&& beacon.beaconDir < -20.0)
			cif.DriveMotors(0.1, 0.0);
		else
			cif.DriveMotors(0.1, 0.1);
		
		updateMap();
	}

	public void wander(boolean followBeacon) {
		// verifica se há algum obstáculo a evitar
		if (irSensor0 > 2.0 || irSensor1 > 2.0 || irSensor2 > 2.0)
			processWall();
		// se não houver obstáculos e o beacon não estiver enquandrado, roda
		else if (followBeacon && beacon.beaconVisible
				&& beacon.beaconDir > 10.0)
			cif.DriveMotors(0.0, 0.1);
		else if (followBeacon && beacon.beaconVisible
				&& beacon.beaconDir < -10.0)
			cif.DriveMotors(0.1, 0.0);
		// caso contrário, anda em frente
		else
			cif.DriveMotors(0.1, 0.1);

		updateMap();

	}

	private void processWall() {

		// enquanto o beacon não estiver enquadrado ou não estiver visível,
		// percorre a parede
		while (beacon.beaconDir > 15.0 || beacon.beaconDir < -15.0
				|| !beacon.beaconVisible) {
			// se bater, tenta rodar para sair da situação
			if (cif.GetBumperSensor()) {
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
		if (beacon.beaconVisible) {
			if (irSensor0 > 2.0 && irSensor1 >= irSensor2)
				cif.DriveMotors(0.1, -0.1);
			else if (irSensor0 > 2.0 && irSensor1 < irSensor2)
				cif.DriveMotors(-0.1, 0.1);
			else if (irSensor1 > 2.0)
				cif.DriveMotors(0.1, 0.0);
			else if (irSensor2 > 2.0)
				cif.DriveMotors(0.0, 0.1);
		}
	}

	/**
	 * basic reactive decision algorithm, decides action based on current sensor
	 * values
	 */
	public void decide() {
		getInfo();

		switch (state) {
		case RUN: /* Go */
			if (cif.GetVisitingLed())
				state = State.WAIT;
			if (ground == 0) { /* Visit Target */
				cif.SetVisitingLed(true);
			}

			else {
				wander(true);
			}
			break;
		case WAIT: /* Wait for others to visit target */
			if (cif.GetReturningLed())
				state = State.RETURN;

			cif.DriveMotors(0.0, 0.0);
			break;
		case RETURN: /* Return to home area */

			if (cif.GetFinished())
				System.exit(0); /* Terminate agent */
			if (ground == 1) { /* Finish */
				cif.Finish();
				System.out.println(robName + " found home at " + cif.GetTime()
						+ "\n");
			} else {
				original_wander(false);
			}
			break;

		}

		requestInfo();
	}

	static void print_usage() {
		System.out
				.println("Usage: java jClient [-robname <robname>] [-pos <pos>] [-host <hostname>[:<port>]]");
	}

	private String robName;
	private double irSensor0, irSensor1, irSensor2;
	private beaconMeasure beacon;
	private int ground;
	private State state;

	private int beaconToFollow;
};

