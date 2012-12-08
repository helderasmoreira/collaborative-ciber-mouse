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

import java.util.List;
import java.util.Observable;

import ciberIF.beaconMeasure;
import ciberIF.ciberIF;

/**
 * example of a basic agent implemented using the java interface library.
 */
public class jClient extends Observable {

	enum State {
		RUN, WAIT, RETURN
	}

	public static final int robotRadius = (int) Constants.MAP_PRECISION / 2;
	static public double[][] map = new double[Constants.mapSizeY][Constants.mapSizeX];
	static public double[][] probabilitiesMap = new double[Constants.mapSizeY][Constants.mapSizeX];
	static public int[][] beaconProbability = new int[Constants.mapSizeY][Constants.mapSizeX];
	static public double[][] aStarMatrix = new double[Constants.mapSizeY][Constants.mapSizeX];
	public static int initialPosX, initialPosY;
	private boolean firstReturn = true;
	public static double PosX;
	public static double PosY;
	public static double PosX_aStar;
	public static double PosY_aStar;
	public static double previousPosX;
	public static double previousPosY;
	public static int halfPosX, halfPosY;
	static int frontSensorPosX;
	static int frontSensorPosY;
	static double frontSensor;
	static int leftSensorPosX;
	static int leftSensorPosY;
	static double leftSensor;
	static int rightSensorPosX;
	static int rightSensorPosY;
	static double rightSensor;
	static ciberIF cif;
	static int pos;

	public static int sensorRequest = 0;
	public static double turnAround = -1.0;
	
	public double previousMotorPowL = 0.0;
	public double previousMotorPowR = 0.0;
	
	public double orientation = 0;
	
	List<Node> nodes;

	public static void main(String[] args) {

		String host, robName;
		int arg;

		// default values
		host = "localhost";
		robName = "Bla";
		pos = 1;

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
				} else {
					throw new Exception();
				}
			}
		} catch (Exception e) {
			print_usage();
			return;
		}

		for(int i = 0; i < aStarMatrix.length; i++)
			for(int j = 0; j < aStarMatrix[i].length; j++)
				aStarMatrix[i][j] = 1.0;
		
		// create client
		jClient client = new jClient();

		client.robName = robName;

		final double[] sensorPositions = { 0.0, -45.0, 45.0, 0.0 };

		// register robot in simulator
		// cif.InitRobot2(robName, pos, sensorPositions, host);
		cif.InitRobot(robName, pos, host);

		ComputeProbabilities observing = new ComputeProbabilities();
		client.addObserver(observing);

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
		halfPosX = Constants.mapSizeX / 2;
		halfPosY = Constants.mapSizeY / 2;
	}

	/**
	 * reads a new message, decides what to do and sends action to simulator
	 */
	public void mainLoop() {

		for (int i = 0; i < probabilitiesMap.length; i++)
			for (int j = 0; j < probabilitiesMap[i].length; j++)
				probabilitiesMap[i][j] = 0.5;

	MapVisualizer visualizer = new MapVisualizer();
	visualizer.start();

	/*MapProbabilitiesVisualizer probabilitiesVisualizer = new MapProbabilitiesVisualizer();
	probabilitiesVisualizer.start();

    BeaconVisualizer bv = new BeaconVisualizer();
    bv.start();*/

		Communication.init();

		cif.ReadSensors();

		initialPosX = (int) (cif.GetX() * Constants.MAP_PRECISION);
		initialPosY = (int) (cif.GetY() * Constants.MAP_PRECISION);
		PosX = halfPosX;
		PosY = halfPosY;
		
		PosX_aStar = PosX;
		PosY_aStar = PosY;
		
		previousPosX = PosX_aStar;
		previousPosY = PosY_aStar;
		
		aStarMatrix[(int) Math.round(PosY)][(int) Math.round(PosX)] = 0.0;
		
		updateMap();
		
		System.out.println(PosX + " " + PosY);
		
		double compass = compassToDeg(makePositive(this.compass));
		
		if(compass > 80 && compass < 100)
			orientation = Math.toRadians(90);
		else if(compass > 170 && compass < 190)
			orientation = Math.toRadians(180);
		else if(compass > 260 && compass < 280)
			orientation = Math.toRadians(270);
		else orientation = Math.toRadians(0);
		
		orientation = normalizeAngle(orientation);
		
		while (true) {
			cif.ReadSensors();
			
			double time = cif.GetTime();
			if (time > 0) {
				// System.out.println("Time: " + time);
				decide();
			}
		}
	}
	
	public static double normalizeAngle(double angle) {

		if (angle < -Math.PI)
			angle = 2 * Math.PI + angle;
		else if (angle > Math.PI)
			angle = -1 * (2 * Math.PI - angle);

		return angle;
	}

	private void DriveMotors(double leftMotorForce, double rightMotorForce) {
		boolean emergency = false;
		
		if(irSensor0 > 9.0 || irSensor1 > 9.0 || irSensor2 > 9.0 || cif.GetBumperSensor()) {
			leftMotorForce = -0.15;
			rightMotorForce = -0.15;
			emergency = true;
		}
		
		cif.DriveMotors(leftMotorForce, rightMotorForce);
			
		leftMotorForce = (leftMotorForce + previousMotorPowL) / 2.0;
		rightMotorForce = (rightMotorForce + previousMotorPowR) / 2.0;
		
		final double rot = (rightMotorForce - leftMotorForce) / (Constants.ROBOT_RADIUS * 2);
		orientation = normalizeAngle(this.orientation + rot);
		
		double L = (rightMotorForce + leftMotorForce) / 2.0;
		
		double iX = ((Math.cos(Math.toRadians(compassToDeg(compass))) * L)) * Constants.MAP_PRECISION;
		double iY = ((Math.sin(Math.toRadians(compassToDeg(compass))) * L)) * Constants.MAP_PRECISION;
		
		double robotMapX2 = (PosX + iX);
		double robotMapY2 = (PosY - iY);
		
		PosX = robotMapX2;
		PosY = robotMapY2;
		
		PosY_aStar = (int) (initialPosY - cif.GetY() *
				 Constants.MAP_PRECISION + halfPosY);
		PosX_aStar = (int) (cif.GetX() * Constants.MAP_PRECISION -
				 initialPosX + halfPosX);
		
		if(firstReturn) {
			int y = (int) (PosY_aStar > previousPosY ? PosY_aStar : previousPosY) + 1;
			int x = (int) (PosX_aStar > previousPosX ? PosX_aStar : previousPosX) + 1;
			double matrix[][] = new double[y][x];
			
			List<Node> nodes = PathFinder.calculate(matrix, (int) PosX_aStar, (int) PosY_aStar, (int) previousPosX, (int) previousPosY);
			for(Node n: nodes) {
				updateAStarMatrix(n.x, n.y);
			}
		}
		
		previousMotorPowL = leftMotorForce;
		previousMotorPowR = rightMotorForce;
		
		previousPosX = PosX_aStar;
		previousPosY = PosY_aStar;
		
		updateAStarMatrix((int)PosX_aStar, (int)PosY_aStar);
		
		if(emergency) {
			if(!firstReturn)
				nodes = PathFinder.calculate(aStarMatrix, (int) Math.round(PosX_aStar), (int) Math.round(PosY_aStar), halfPosX, halfPosY);
			if(nodes == null || nodes.size() == 0) {
				cif.DriveMotors(0.0, 0.0);
				System.out.println("ACABOU");
				System.exit(0); /* Terminate agent */
			}
		}
		
	}

	private void updateAStarMatrix(int x, int y) {
		
		aStarMatrix[y][x] = 0.0;
		aStarMatrix[y][x+1] = 0.0;
		aStarMatrix[y][x-1] = 0.0;
		aStarMatrix[y+1][x] = 0.0;
		aStarMatrix[y-1][x] = 0.0;
		aStarMatrix[y+1][x+1] = 0.0;
		aStarMatrix[y-1][x-1] = 0.0;
		aStarMatrix[y+1][x-1] = 0.0;
		aStarMatrix[y-1][x+1] = 0.0;
		
		for(int i = 0; i < probabilitiesMap.length; i++)
			for(int j = 0; j < probabilitiesMap[i].length; j++)
				if(probabilitiesMap[i][j] > 0.75)
					aStarMatrix[i][j] = 0.70;
	}

	private void updateMap() {
		// int robotMapY = (int) (initialPosY - cif.GetY() *
		// Constants.MAP_PRECISION + halfPosY);
		// int robotMapX = (int) (cif.GetX() * Constants.MAP_PRECISION -
		// initialPosX + halfPosX);
		int robotMapX = (int) Math.round(PosX);
		int robotMapY = (int) Math.round(PosY);

		map[robotMapY][robotMapX] = 1.0;

		// System.out.println("(X1,Y1)=(" + robotMapX + "," + robotMapY + ")");
		// System.out.println("(X2,Y2)=(" + PosX + "," + PosY + ")\n");

		int[] frontSensorPos = frontSensorMapPosition(robotMapX, robotMapY,
				compass);
		int[] leftSensorPos = leftSensorMapPosition(robotMapX, robotMapY,
				compass);
		int[] rightSensorPos = rightSensorMapPosition(robotMapX, robotMapY,
				compass);
		// System.out.println("Left Sensor X: " + leftSensorPos[0]);
		// System.out.println("Left Sensor Y: " + leftSensorPos[1]);
		// System.out.println("Right Sensor X: " + rightSensorPos[0]);
		// System.out.println("Right Sensor Y: " + rightSensorPos[1]);
		map[frontSensorPos[1]][frontSensorPos[0]] = -1.0;
		map[leftSensorPos[1]][leftSensorPos[0]] = -1.1;
		map[rightSensorPos[1]][rightSensorPos[0]] = -1.2;

		frontSensorPosX = frontSensorPos[0];
		frontSensorPosY = frontSensorPos[1];
		frontSensor = irSensor0;

		leftSensorPosX = leftSensorPos[0];
		leftSensorPosY = leftSensorPos[1];
		leftSensor = irSensor1;

		rightSensorPosX = rightSensorPos[0];
		rightSensorPosY = rightSensorPos[1];
		rightSensor = irSensor2;

		Communication.say();
		setChanged();
		notifyObservers();

	}

	public void getInfo() {
		if (cif.IsObstacleReady(0)) {
			irSensor0 = cif.GetObstacleSensor(0);
		}
		if (cif.IsObstacleReady(1)) {
			irSensor1 = cif.GetObstacleSensor(1);
		}
		if (cif.IsObstacleReady(2)) {
			irSensor2 = cif.GetObstacleSensor(2);
		}
		if (cif.IsCompassReady()) {
			compass = cif.GetCompassSensor();
		}
		if (cif.IsGroundReady()) {
			ground = cif.GetGroundSensor();
		}
		if (cif.IsBeaconReady(beaconToFollow)) {
			beacon = cif.GetBeaconSensor(beaconToFollow);
		}

		Communication.receive();
	}

	public void requestInfo() {

		// estes são sempre pedidos
		cif.RequestIRSensor(0);
		cif.RequestIRSensor(1);
		cif.RequestIRSensor(2);

		// em cada ciclo pedimos um diferente
		switch (sensorRequest++ % 3) {
		case 1:
			cif.RequestBeaconSensor(beaconToFollow);
			break;
		case 2:
			cif.RequestGroundSensor();
			break;
		default:
			cif.RequestCompassSensor();
		}
	}
	
	private void goHome() {
		if(nodes.size() == 1) {
			DriveMotors(0.0, 0.0);
			return;
		}
		
		nodes.remove(nodes.size() - 1);
		Node n = nodes.get(nodes.size() - 1);
	
		double anglePoint = makePositive(Math.atan2(PosY_aStar - n.y, n.x - PosX_aStar));
		/*double angleNow = makePositive(orientation);*/
		double angleNow = makePositive(Math.toRadians(compassToDeg(jClient.compass)));
		
		if(Math.abs(angleNow - anglePoint) > Math.toRadians(Constants.MAX_ANGLE_DEGREES_DEVIATION)) {
		
			while(Math.abs(angleNow - anglePoint) > Math.toRadians(Constants.MAX_ANGLE_DEGREES_DEVIATION)) {
				alignRobot(anglePoint, angleNow);
				/*angleNow = makePositive(orientation);*/
				angleNow = makePositive(Math.toRadians(compassToDeg(jClient.compass)));
			}
		
		}
		else {
			DriveMotors(0.1, 0.1);
		}
		
		updateMap();
		
		int x = (int) Math.round(PosX_aStar);
		int y = (int) Math.round(PosY_aStar);
		
		if(nodes != null && (Math.abs(x - n.x) > 10 || Math.abs(y - n.y) > 10)) {
			nodes.add(new Node(n.x, n.y));
		}
		
		//prunePath();
		//System.out.println(n.x + " " + n.y + " " + x + " " + y);
	}

	private void alignRobot(double anglePoint, double angleNow) {
		
		double dif = angleDifference(normalizeAngle(angleNow), normalizeAngle(anglePoint));
		
		requestInfo();
		cif.ReadSensors();
		getInfo();
		
		double lPowIn = 0.20 * (2 * Constants.ROBOT_RADIUS * -dif );
		double rPowIn = 0.20 * (2 * Constants.ROBOT_RADIUS * dif );
		
		DriveMotors(constrain(lPowIn, -0.15, 0.15), constrain(rPowIn, -0.15, 0.15));

		updateMap();
	}
	
	private void prunePath() {

		if (nodes == null || nodes.isEmpty())
			return;
		
		Node n = nodes.get(0);
		
		double distance = Math.sqrt(Math.abs(n.x - PosX)
				* Math.abs(n.x - PosX) + Math.abs(n.y - PosY)
				* Math.abs(n.y - PosY));

		for (int i = 1; i < nodes.size(); i++) {
			double distance2 = Math.sqrt(Math.abs(nodes.get(i).x - PosX)
					* Math.abs(nodes.get(i).x - PosX) + Math.abs(nodes.get(i).y - PosY)
					* Math.abs(nodes.get(i).y - PosY));
			if (distance <= distance2) {
				nodes.remove(i-1);
				distance = distance2;
			} else
				break;
		}
	}
	
	private static double constrain(double value, double min, double max) {
		if (value < min)
			return min;
		else if (value > max)
			return max;
		else
			return value;
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
	
	public static double makePositive(double angle) {
		return angle >= 0 ? angle : angle + 2 * Math.PI;
	}

	public void original_wander(boolean followBeacon) {
		if (irSensor0 > 2.0 || irSensor1 > 2.0 || irSensor2 > 2.0) {
			DriveMotors(0.1, -0.1);
		} else if (irSensor1 > 1.0) {
			DriveMotors(0.1, 0.0);
		} else if (irSensor2 > 1.0) {
			DriveMotors(0.0, 0.1);
		} else if (followBeacon && beacon.beaconVisible
				&& beacon.beaconDir > 20.0) {
			DriveMotors(0.0, 0.1);
		} else if (followBeacon && beacon.beaconVisible
				&& beacon.beaconDir < -20.0) {
			DriveMotors(0.1, 0.0);
		} else {
			DriveMotors(0.1, 0.1);
		}

		updateMap();
	}

	public void wander(boolean followBeacon) {
		// verifica se há algum obstáculo a evitar
		if (irSensor0 > 2.0 || irSensor1 > 2.0 || irSensor2 > 2.0) {
			processWall();
		} // se não houver obstáculos e o beacon não estiver enquandrado, roda
		else if (followBeacon && beacon.beaconVisible
				&& beacon.beaconDir > 10.0) {
			DriveMotors(0.0, 0.1);
		} else if (followBeacon && beacon.beaconVisible
				&& beacon.beaconDir < -10.0) {
			DriveMotors(0.1, 0.0);
		} // caso contrário, anda em frente
		else {
			DriveMotors(0.1, 0.1);
		}

		updateMap();

	}

	private void processWall() {

		// enquanto o beacon não estiver enquadrado ou não estiver visível,
		// percorre a parede
		while (beacon.beaconDir > 15.0 || beacon.beaconDir < -15.0
				|| !beacon.beaconVisible) {
			// se bater, tenta rodar para sair da situação
			if (cif.GetBumperSensor()) {
				DriveMotors(-0.15, 0.15);
			}

			requestInfo();
			cif.ReadSensors();
			getInfo();

			// se estiver numa esquina, roda no sentido do relógio
			if (irSensor2 < 1.5 && irSensor0 < 1.5 && irSensor2 < 2.0) {
				DriveMotors(0.1, -0.1);
				// se estiver num canto, roda no sentido contrário ao do relógio
			} else if (irSensor0 <= 1.5 && irSensor1 <= 1.5 && irSensor2 <= 3.0) {
				DriveMotors(0.1, 0.1);
				// caso não haja obstáculo ou esteja perto da parede, anda
			} else if (irSensor0 >= 1.5 || irSensor2 > 3.0) {
				DriveMotors(-0.1, 0.1);
			}

			updateMap();

		}

		// se o beacon estiver visível, tenta desviar-se dos obstáculos
		if (beacon.beaconVisible) {
			if (irSensor0 > 1.5 && irSensor1 >= irSensor2) {
				DriveMotors(0.1, -0.1);
			} else if (irSensor0 > 2.0 && irSensor1 < irSensor2) {
				DriveMotors(-0.1, 0.1);
			} else if (irSensor1 > 2.0) {
				DriveMotors(0.1, 0.0);
			} else if (irSensor2 > 2.0) {
				DriveMotors(0.0, 0.1);
			}
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
			if (cif.GetVisitingLed()) {
				state = State.WAIT;
			}
			if (ground == 0) { /* Visit Target */
				cif.SetVisitingLed(true);
			} else {
				wander(true);
			}
			break;
		case WAIT: /* Wait for others to visit target */
			if (cif.GetReturningLed()) {
				state = State.RETURN;
			}

			DriveMotors(0.0, 0.0);
			break;
		case RETURN: /* Return to home area */
			if(firstReturn) {
				nodes = PathFinder.calculate(aStarMatrix, (int) Math.round(PosX), (int) Math.round(PosY), halfPosX, halfPosY);
				
				if(nodes != null && nodes.size() > 0) {
					for(int i = 0; i < nodes.size(); i++) {
						map[nodes.get(i).y][nodes.get(i
								).x] = -3.0;
					}
					System.out.println("GOING HOME!");
					firstReturn = false;
				}
				else System.exit(0); /* Terminate agent */
			}
			if (cif.GetFinished()) {
				System.exit(0); /* Terminate agent */
			}
			if (ground == 1) { /* Finish */
				cif.Finish();
				System.out.println(robName + " found home at " + cif.GetTime()
						+ "\n");
			} else {
				//original_wander(false);
				if(nodes != null && nodes.size() > 0)
					goHome();
			}
			break;

		}

		requestInfo();
	}

	// Arduino map function: http://www.arduino.cc/en/Reference/map
	public static double map(double x, double in_min, double in_max,
			double out_min, double out_max) {
		return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
	}

	// compass range -180;180
	public static double compassToDeg(double compass) {
		if (compass >= 0.0 && compass <= 180) {
			return compass;
		} else {
			return map(compass, -180.0, 0.0, 180.0, 360.0);
		}
	}

	/*
	 * sensorDir is in Degrees returns [x,y]
	 */
	private int[] sensorMapPosition(int robotMapX, int robotMapY,
			double sensorDir) {
		double sensorPosX = robotMapX + robotRadius
				* Math.cos(Math.toRadians(sensorDir));
		double sensorPosY = robotMapY - robotRadius
				* Math.sin(Math.toRadians(sensorDir));

		return new int[] { Math.round((float) sensorPosX),
				Math.round((float) sensorPosY) };
	}

	public int[] frontSensorMapPosition(int robotMapX, int robotMapY,
			double compass) {
		double frontSensorDirection = compassToDeg(compass);
		return sensorMapPosition(robotMapX, robotMapY, frontSensorDirection);
	}

	public int[] leftSensorMapPosition(int robotMapX, int robotMapY,
			double compass) {
		double leftSensorDirection = compassToDeg(compass) + 60;
		return sensorMapPosition(robotMapX, robotMapY, leftSensorDirection);
	}

	public int[] rightSensorMapPosition(int robotMapX, int robotMapY,
			double compass) {
		double rightSensorDirection = compassToDeg(compass) - 60;
		return sensorMapPosition(robotMapX, robotMapY, rightSensorDirection);
	}

	public double euclideanDistance(int x1, int y1, int x2, int y2) {
		return Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
	}

	static void print_usage() {
		System.out
				.println("Usage: java jClient [-robname <robname>] [-pos <pos>] [-host <hostname>[:<port>]]");
	}

	private String robName;
	private double irSensor0, irSensor1, irSensor2;
	static double compass;
	static public beaconMeasure beacon;
	private int ground;
	private State state;
	private int beaconToFollow;

};
