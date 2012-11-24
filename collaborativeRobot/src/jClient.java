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
  
  public static final int arenaSizeX = 28;
  public static final int arenaSizeY = 14;
  public static final double mapPrecision = 10.0;
  public static final int mapSizeX = arenaSizeX * 2 * (int) mapPrecision;
  public static final int mapSizeY = arenaSizeY * 2 * (int) mapPrecision;
  public static final int robotRadius = (int) mapPrecision / 2;
  static public double[][] map = new double[mapSizeY][mapSizeX];
	static public double[][] beaconProbability = new double[mapSizeY][mapSizeX];
  int initialPosX, initialPosY;
  double PosX, PosY;
  int halfPosX, halfPosY;
  static ciberIF cif;
  static int pos;

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

    // create client
    jClient client = new jClient();

    client.robName = robName;

    // register robot in simulator
    cif.InitRobot(robName, pos, host);

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
    PosX = halfPosX;
    PosY = halfPosY;

    updateMap();

    if (pos == 2) {
		beaconProbability[10][10] = 0.70;
		beaconProbability[9][9] = 0.65;
		beaconProbability[9][10] = 0.64;
		beaconProbability[9][11] = 0.63;
		beaconProbability[10][9] = 0.62;
		beaconProbability[10][11] = 0.61;
		beaconProbability[11][9] = 0.60;
		beaconProbability[11][10] = 0.59;
		beaconProbability[11][11] = 0.58;
		
		beaconProbability[20][20] = 0.90;
		beaconProbability[19][19] = 0.85;
		beaconProbability[19][20] = 0.84;
		beaconProbability[19][21] = 0.83;
		beaconProbability[20][19] = 0.82;
		beaconProbability[20][21] = 0.81;
		beaconProbability[21][19] = 0.80;
		beaconProbability[21][20] = 0.79;
		beaconProbability[21][21] = 0.78;
	}

    while (true) {
      cif.ReadSensors();

      double time = cif.GetTime();
      if (time > 0) {
        System.out.println("Time: " + time);
        decide();
      }
    }
  }

  private void DriveMotors(double leftMotorForce, double rightMotorForce) {
    cif.DriveMotors(leftMotorForce, rightMotorForce);
    
    double compassRadians = Math.toRadians(compassToDeg(compass));
    double L = (rightMotorForce + leftMotorForce) / 2.0;
    double iX = ((Math.cos(compassRadians) * L))*mapPrecision;
    double iY = ((Math.sin(compassRadians) * L))*mapPrecision;
    double robotMapX2 = (PosX + iX);
    double robotMapY2 = (PosY - iY);
    
//    System.out.println("rM="+ rightMotorForce );
//    System.out.println("lM="+ leftMotorForce );
//    System.out.println("L="+ L );
//    System.out.println("compassRadians="+ compassRadians);
//    System.out.println("iX="+ iX);
//    System.out.println("iY="+ iY);
    
    PosX = robotMapX2;
    PosY = robotMapY2;
  }

  private void updateMap() {
    //int robotMapY = (int) (initialPosY - cif.GetY() * mapPrecision + halfPosY);
    //int robotMapX = (int) (cif.GetX() * mapPrecision - initialPosX + halfPosX);
    int robotMapX = (int) PosX;
    int robotMapY = (int) PosY;
  
    map[robotMapY][robotMapX] = 1.0;
    
    //System.out.println("(X1,Y1)=(" + robotMapX + "," + robotMapY + ")");
    //System.out.println("(X2,Y2)=(" + PosX + "," + PosY + ")\n");
    


    int[] frontSensorPos = frontSensorMapPosition(robotMapX, robotMapY, compass);
    int[] leftSensorPos = leftSensorMapPosition(robotMapX, robotMapY, compass);
    int[] rightSensorPos = rightSensorMapPosition(robotMapX, robotMapY, compass);
    //System.out.println("Left Sensor X: " + leftSensorPos[0]);
    //System.out.println("Left Sensor Y: " + leftSensorPos[1]);
    //System.out.println("Right Sensor X: " + rightSensorPos[0]);
    //System.out.println("Right Sensor Y: " + rightSensorPos[1]);
    map[frontSensorPos[1]][frontSensorPos[0]] = -1.0;
    map[leftSensorPos[1]][leftSensorPos[0]] = -1.1;
    map[rightSensorPos[1]][rightSensorPos[0]] = -1.2;
    
    Communication.say();
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
    cif.Say(robName);
    cif.RequestIRSensor(0);
    cif.RequestIRSensor(1);
    cif.RequestIRSensor(2);
    cif.RequestBeaconSensor(beaconToFollow);
    cif.RequestGroundSensor();
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
      if (irSensor2 < 2.0 && irSensor0 < 2.0 && irSensor2 < 3.0) {
        DriveMotors(0.1, -0.1);
        // se estiver num canto, roda no sentido contrário ao do relógio
      } else if (irSensor0 <= 2.0 && irSensor1 <= 2.0 && irSensor2 <= 6.0) {
        DriveMotors(0.1, 0.1);
        // caso não haja obstáculo ou esteja perto da parede, anda
      } else if (irSensor0 >= 2.0 || irSensor2 > 6.0) {
        DriveMotors(-0.1, 0.1);
      }

      updateMap();

    }

    // se o beacon estiver visível, tenta desviar-se dos obstáculos
    if (beacon.beaconVisible) {
      if (irSensor0 > 2.0 && irSensor1 >= irSensor2) {
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

        if (cif.GetFinished()) {
          System.exit(0); /* Terminate agent */
        }
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

  //Arduino map function: http://www.arduino.cc/en/Reference/map
  public double map(double x, double in_min, double in_max, double out_min, double out_max) {
    return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
  }

  //compass range -180;180
  public double compassToDeg(double compass) {
    if (compass >= 0.0 && compass <= 180) {
      return compass;
    } else {
      return map(compass, -180.0, 0.0, 180.0, 360.0);
    }
  }

  /* 
   * sensorDir is in Degrees
   * returns [x,y]
   */
  private int[] sensorMapPosition(int robotMapX, int robotMapY, double sensorDir) {
    double sensorPosX = robotMapX + robotRadius * Math.cos(Math.toRadians(sensorDir));
    double sensorPosY = robotMapY - robotRadius * Math.sin(Math.toRadians(sensorDir));

    return new int[]{Math.round((float) sensorPosX), Math.round((float) sensorPosY)};
  }

  public int[] frontSensorMapPosition(int robotMapX, int robotMapY, double compass) {
    double frontSensorDirection = compassToDeg(compass);
    return sensorMapPosition(robotMapX, robotMapY, frontSensorDirection);
  }

  public int[] leftSensorMapPosition(int robotMapX, int robotMapY, double compass) {
    double leftSensorDirection = compassToDeg(compass) + 60;
    return sensorMapPosition(robotMapX, robotMapY, leftSensorDirection);
  }

  public int[] rightSensorMapPosition(int robotMapX, int robotMapY, double compass) {
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
  private double compass;
  private beaconMeasure beacon;
  private int ground;
  private State state;
  private int beaconToFollow;

};
