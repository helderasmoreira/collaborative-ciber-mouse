package collaborobo;

import ciberIF.beaconMeasure;
import ciberIF.ciberIF;
import collaborobo.path.Node;
import collaborobo.path.PathFinder;
import collaborobo.utils.Constants;
import collaborobo.utils.Util;
import collaborobo.visualizers.*;
import java.util.List;
import java.util.Observable;

public class CollaborativeRobot extends Observable {

  public double[][] map = new double[Constants.mapSizeY][Constants.mapSizeX];
  public double[][] probabilitiesMap = new double[Constants.mapSizeY][Constants.mapSizeX];
  public int[][] beaconProbability = new int[Constants.mapSizeY][Constants.mapSizeX];
  public double[][] aStarMatrix = new double[Constants.mapSizeY][Constants.mapSizeX];
  
  public int halfPosX, halfPosY;
  public int initialPosX, initialPosY;
  public double PosX;
  public double PosY;
  public double previousPosX;
  public double previousPosY;
  public double previousMotorPowL = 0.0;
  public double previousMotorPowR = 0.0;
  
  //Sensors
  public int frontSensorPosX;
  public int frontSensorPosY;
  public double frontSensor;
  public int leftSensorPosX;
  public int leftSensorPosY;
  public double leftSensor;
  public int rightSensorPosX;
  public int rightSensorPosY;
  public double rightSensor;
  
  private boolean firstReturn = true;
  public List<Node> nodes;
  
  private Communication comm;
  
  public String name;
  public int pos;
  public beaconMeasure beacon;
  public double compass;
  public double orientation = 0; //alternative to compass
  public ciberIF cif;
  private int ground;
  private State state;
  
  // Execution Options
  private boolean GPS_ON = false;
  private boolean USE_PROB = false;
  private boolean VISUALIZER_ON = false;
  private boolean BEACON_ON = false;
  private boolean PROBABILITIES_ON = true;
  
  enum State {
    RUN, WAIT, RETURN
  }

  // Constructor
  CollaborativeRobot() {
    cif = new ciberIF();
    beacon = new beaconMeasure();
    ground = -1;
    state = State.RUN;
    halfPosX = Constants.mapSizeX / 2;
    halfPosY = Constants.mapSizeY / 2;
    comm = new Communication(this);
  }

  public void mainLoop() {

    for (int i = 0; i < probabilitiesMap.length; i++) {
      for (int j = 0; j < probabilitiesMap[i].length; j++) {
        probabilitiesMap[i][j] = 0.5;
      }
    }

    if(!USE_PROB){
        for (int i = 0; i < aStarMatrix.length; i++) {
          for (int j = 0; j < aStarMatrix[i].length; j++) {
            aStarMatrix[i][j] = 1.0;
          }
        }
    }

    if(VISUALIZER_ON){
        MapVisualizer visualizer = new MapVisualizer(this);
        visualizer.start();
    }
    if(PROBABILITIES_ON){
        MapProbabilitiesVisualizer probVisualizer = new MapProbabilitiesVisualizer(this);
        probVisualizer.start();
    }
    if(BEACON_ON){
        BeaconVisualizer bv = new BeaconVisualizer(this);
        bv.start();
    }
 
    cif.ReadSensors();

    initialPosX = (int) (cif.GetX() * Constants.MAP_PRECISION);
    initialPosY = (int) (cif.GetY() * Constants.MAP_PRECISION);
    PosX = halfPosX;
    PosY = halfPosY;

    updateMap();

    System.out.println(PosX + " " + PosY);
    
    compass = cif.GetCompassSensor();
    
    double cpass = Util.makePositive(compass);

    if (cpass > 80 && cpass < 100) {
      orientation = Math.toRadians(90);
    } else if (cpass > 170 && cpass < 190) {
      orientation = Math.toRadians(180);
    } else if (cpass > 260 && cpass < 280) {
      orientation = Math.toRadians(270);
    } else {
      orientation = Math.toRadians(0);
    }
    
    while (true) {
      cif.ReadSensors();

      double time = cif.GetTime();
      if (time > 0) {
        // System.out.println("Time: " + time);
        decide();
      }
    }
  }


	private void DriveMotors(double leftMotorForce, double rightMotorForce) {
		
		boolean emergency = false;

		if (frontSensor > 9.0 || leftSensor > 9.0 || rightSensor > 9.0
				|| cif.GetBumperSensor()) {
			leftMotorForce = -0.15;
			rightMotorForce = -0.15;
			emergency = true;
		}

		cif.DriveMotors(leftMotorForce, rightMotorForce);
		
		leftMotorForce = (leftMotorForce + previousMotorPowL) / 2.0;
		rightMotorForce = (rightMotorForce + previousMotorPowR) / 2.0;

		final double rot = (rightMotorForce - leftMotorForce)
				/ (Constants.ROBOT_RADIUS * 2.0);
		orientation = Util.normalizeAngle(this.orientation + rot);

		double L = (rightMotorForce + leftMotorForce) / 2.0;

		double iX = ((Math.cos(Math.toRadians(compass)) * L))
				* Constants.MAP_PRECISION;
		double iY = ((Math.sin(Math.toRadians(compass)) * L))
				* Constants.MAP_PRECISION;

		double robotMapX2 = (PosX + iX);
		double robotMapY2 = (PosY - iY);

		PosX = robotMapX2;
		PosY = robotMapY2;

		PosX = Util.constrain(PosX, 0.0, Double.valueOf(Constants.mapSizeX));
		PosY = Util.constrain(PosY, 0.0, Double.valueOf(Constants.mapSizeY));
		
		if (GPS_ON) {
			PosX = cif.GetX() * Constants.MAP_PRECISION - initialPosX
					+ halfPosX;
			PosY = initialPosY - cif.GetY() * Constants.MAP_PRECISION
					+ halfPosY;
		}
        
        if(!USE_PROB){
            if (firstReturn) {
                int y = (int) Math
                        .round((PosY > previousPosY ? PosY : previousPosY)) + 1;
                int x = (int) Math
                        .round((PosX > previousPosX ? PosX : previousPosX)) + 1;
                double matrix[][] = new double[y][x];

                List<Node> nodes = PathFinder.calculate(matrix,
                        (int) Math.round(PosX), (int) Math.round(PosY),
                        (int) Math.round(previousPosX),
                        (int) Math.round(previousPosY), USE_PROB);
                for (Node n : nodes) {
                    updateAStarMatrix(n.x, n.y);
                }
            }

            updateAStarMatrix((int) Math.round(PosX), (int) Math.round(PosY));
            previousPosX = PosX;
            previousPosY = PosY;
        }
		previousMotorPowL = leftMotorForce;
		previousMotorPowR = rightMotorForce;

		if (emergency) {
			requestInfo();
			cif.ReadSensors();
			getInfo();
			cif.DriveMotors(0.0, 0.0);
			if (!firstReturn) {
                if(USE_PROB){
                    nodes = PathFinder.calculate(probabilitiesMap,
						(int) Math.round(PosX), (int) Math.round(PosY),
						halfPosX, halfPosY, USE_PROB);
                }else{
                    nodes = PathFinder.calculate(aStarMatrix,
						(int) Math.round(PosX), (int) Math.round(PosY),
						halfPosX, halfPosY, USE_PROB);
                }
			}
			if (!firstReturn && (nodes == null || nodes.isEmpty())) {
				cif.DriveMotors(0.0, 0.0);
				System.exit(0); /* Terminate agent */
			}
		}
	}

	private void updateAStarMatrix(int x, int y) {

		int minx = x - 1, maxx = x + 1, miny = y - 1, maxy = y + 1;
		minx = minx > 0 ? minx : 0;
		maxx = (int) (maxx < Constants.mapSizeX ? maxx : Constants.mapSizeX - 1);
		miny = miny > 0 ? miny : 0;
		maxy = (int) (maxy < Constants.mapSizeY ? maxy : Constants.mapSizeY - 1);
		
		if(x > 0 && x < Constants.mapSizeX && y > 0 && y < Constants.mapSizeY) {
			aStarMatrix[y][x] = 0.0;
			aStarMatrix[y][maxx] = 0.0;
			aStarMatrix[y][minx] = 0.0;
			aStarMatrix[maxy][x] = 0.0;
			aStarMatrix[miny][x] = 0.0;
			aStarMatrix[maxy][maxx] = 0.0;
			aStarMatrix[miny][minx] = 0.0;
			aStarMatrix[maxy][minx] = 0.0;
			aStarMatrix[miny][maxx] = 0.0;
		}

	}

  private void updateMap() {
    int robotMapX = (int) Math.round(PosX);
    int robotMapY = (int) Math.round(PosY);

    map[robotMapY][robotMapX] = 1.0;

    int[] frontSensorPos = Util.frontSensorMapPosition(robotMapX, robotMapY, compass);
    int[] leftSensorPos = Util.leftSensorMapPosition(robotMapX, robotMapY, compass);
    int[] rightSensorPos = Util.rightSensorMapPosition(robotMapX, robotMapY, compass);

    
    frontSensorPos[0] = Util.constrain(frontSensorPos[0], 0, Constants.mapSizeX-1);
    frontSensorPos[1] = Util.constrain(frontSensorPos[1], 0, Constants.mapSizeY-1);
    leftSensorPos[0] = Util.constrain(leftSensorPos[0], 0, Constants.mapSizeX-1);
    leftSensorPos[1] = Util.constrain(leftSensorPos[1], 0, Constants.mapSizeY-1);
    rightSensorPos[0] = Util.constrain(rightSensorPos[0], 0, Constants.mapSizeX-1);
    rightSensorPos[1] = Util.constrain(rightSensorPos[1], 0, Constants.mapSizeY-1);
    
    map[frontSensorPos[1]][frontSensorPos[0]] = -1.0;
    map[leftSensorPos[1]][leftSensorPos[0]] = -1.1;
    map[rightSensorPos[1]][rightSensorPos[0]] = -1.2;

    frontSensorPosX = frontSensorPos[0];
    frontSensorPosY = frontSensorPos[1];

    leftSensorPosX = leftSensorPos[0];
    leftSensorPosY = leftSensorPos[1];

    rightSensorPosX = rightSensorPos[0];
    rightSensorPosY = rightSensorPos[1];

    SensorProbBean sb = new SensorProbBean();
    sb.compass = compass;
    sb.frontSensor = frontSensor;
    sb.frontSensorPosX = frontSensorPosX;
    sb.frontSensorPosY = frontSensorPosY;
    sb.leftSensor = leftSensor;
    sb.leftSensorPosX = leftSensorPosX;
    sb.leftSensorPosY = leftSensorPosY;
    sb.rightSensor = rightSensor;
    sb.rightSensorPosX = rightSensorPosX;
    sb.rightSensorPosY = rightSensorPosY;
    sb.mapX = PosX;
    sb.mapY = PosY;

    comm.say();
    tellObservers(sb);

  }

  public void tellObservers(Object arg) {
    setChanged();
    notifyObservers(arg);
  }

  public void getInfo() {
	  
	comm.receive();
	  
    if (cif.IsObstacleReady(0)) {
      frontSensor = cif.GetObstacleSensor(0);
    }
    if (cif.IsObstacleReady(1)) {
      leftSensor = cif.GetObstacleSensor(1);
    }
    if (cif.IsObstacleReady(2)) {
      rightSensor = cif.GetObstacleSensor(2);
    }
    if (cif.IsCompassReady()) {
      compass = cif.GetCompassSensor();
    }
    if (cif.IsGroundReady()) {
      ground = cif.GetGroundSensor();
    }
    if (cif.IsBeaconReady(Constants.BEACON)) {
    	beacon = cif.GetBeaconSensor(Constants.BEACON);
    	if (!beacon.beaconVisible) {
    		beacon = createBeaconFromMatrix();
    	}
    	
    } else {
      beacon.beaconVisible = false;
    }
  }

  private beaconMeasure createBeaconFromMatrix() {
	  
	int maxY = 0, maxX = 0;
	int max = 0;
	for(int y=0;y<beaconProbability.length;y++) {
          for(int x=0;x<beaconProbability[y].length;x++) {
            if (beaconProbability[y][x] > max) {
                maxY = y;
                maxX = x;
                max = beaconProbability[y][x];
            }
        }
      }
	
	beaconMeasure beacon = new beaconMeasure();
        
	beacon.beaconDir = Math.toDegrees(Math.atan2(PosY - maxY, maxX - PosX));
	beacon.beaconVisible = true;
	if(max == 0){
        beacon.beaconVisible = false;
    }
	else {
        System.out.println("beacon from matrix - maxX: " + maxX + "  maxY: " + maxY);
     }
	return beacon;
}

public void requestInfo() {

    // estes são sempre pedidos
    cif.RequestIRSensor(0);
    cif.RequestIRSensor(1);
    cif.RequestIRSensor(2);
    cif.RequestCompassSensor();
    // em cada ciclo pedimos um diferente
    switch ((int) cif.GetTime() % 3) {
      case 1:
        cif.RequestBeaconSensor(Constants.BEACON);
        break;
      case 2:
        cif.RequestGroundSensor();
        break;
      default:
        cif.RequestCompassSensor();
    }
  }

  private void goHome() {
    if (nodes.size() == 1) {
      DriveMotors(0.0, 0.0);
      return;
    }

    nodes.remove(nodes.size() - 1);
    Node n = nodes.get(nodes.size() - 1);

    double anglePoint = Util.makePositive(Math.atan2(PosY - n.y, n.x - PosX));
    /*double angleNow = Util.makePositive(orientation);*/
    double angleNow = Util.makePositive(Math.toRadians(compass));

    if (Math.abs(angleNow - anglePoint) > Math.toRadians(Constants.MAX_ANGLE_DEGREES_DEVIATION)) {

      while (Math.abs(angleNow - anglePoint) > Math.toRadians(Constants.MAX_ANGLE_DEGREES_DEVIATION)) {
        alignRobot(anglePoint, angleNow);
        /*angleNow = Util.makePositive(orientation);*/
        angleNow = Util.makePositive(Math.toRadians(compass));
      }

    } else {
      DriveMotors(0.1, 0.1);
    }

    updateMap();

    int x = (int) Math.round(PosX);
    int y = (int) Math.round(PosY);

    if (nodes != null && (Math.abs(x - n.x) > 2 || Math.abs(y - n.y) > 2)) {
      nodes.add(new Node(n.x, n.y));
    }

    //prunePath();
  }

  private void alignRobot(double anglePoint, double angleNow) {
	    requestInfo();
	    cif.ReadSensors();
	    getInfo();
	    
	    double dif = Util.angleDifference(Util.normalizeAngle(angleNow),
	            Util.normalizeAngle(anglePoint));
	    
	    double lPowIn = 0.20 * (2 * Constants.ROBOT_RADIUS * -dif);
	    double rPowIn = 0.20 * (2 * Constants.ROBOT_RADIUS * dif);
	    
	    lPowIn = ((int)(lPowIn*1000))/1000.0;
	    rPowIn = ((int)(rPowIn*1000))/1000.0;
	    
	    if(lPowIn == 0.0 && rPowIn == 0.0) {
	    	lPowIn = -0.001;
	    	rPowIn = 0.001;
	    }
	    
	    DriveMotors(Util.constrain(lPowIn, -0.15, 0.15), Util.constrain(rPowIn, -0.15, 0.15));
	    
	    updateMap();
  }

  public void wander(boolean followBeacon) {
    // verifica se há algum obstáculo a evitar
    if (frontSensor > 2.0 || leftSensor > 2.0 || rightSensor > 2.0) {
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
      
      if(ground == 0) {
    	  cif.SetVisitingLed(true);
    	  DriveMotors(0.0,0.0);
    	  break;
      }

      // se estiver numa esquina, roda no sentido do relógio
      if (rightSensor < 1.5 && frontSensor < 1.5 && rightSensor < 2.0) {
        DriveMotors(0.1, -0.1);
        // se estiver num canto, roda no sentido contrário ao do relógio
      } else if (frontSensor <= 1.5 && leftSensor <= 1.5 && rightSensor <= 3.0) {
        DriveMotors(0.1, 0.1);
        // caso não haja obstáculo ou esteja perto da parede, anda
      } else if (frontSensor >= 1.5 || rightSensor > 3.0) {
        DriveMotors(-0.1, 0.1);
      }

      updateMap();

    }

    // se o beacon estiver visível, tenta desviar-se dos obstáculos
    if (beacon.beaconVisible) {
      if (frontSensor > 1.5 && leftSensor >= rightSensor) {
        DriveMotors(0.1, -0.1);
      } else if (frontSensor > 2.0 && leftSensor < rightSensor) {
        DriveMotors(-0.1, 0.1);
      } else if (leftSensor > 2.0) {
        DriveMotors(0.1, 0.0);
      } else if (rightSensor > 2.0) {
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
        if (firstReturn) {
            if(USE_PROB){
                nodes = PathFinder.calculate(probabilitiesMap, (int) Math.round(PosX), (int) Math.round(PosY), halfPosX, halfPosY, USE_PROB);
            }else{
                nodes = PathFinder.calculate(aStarMatrix, (int) Math.round(PosX), (int) Math.round(PosY), halfPosX, halfPosY, USE_PROB);   
            }

          if (nodes != null && nodes.size() > 0) {
            for (int i = 0; i < nodes.size(); i++) {
             probabilitiesMap[nodes.get(i).y][nodes.get(i).x] = -3.0;
            }
            System.out.println("GOING HOME!");
            firstReturn = false;
          } else {
            System.exit(0); /* Terminate agent */
          }
        }
        if (cif.GetFinished()) {
          System.exit(0); /* Terminate agent */
        }
        if (ground == 1) { /* Finish */
          cif.Finish();
          System.out.println(name + " found home at " + cif.GetTime()
                  + "\n");
        } else {
          //original_wander(false);
          if (nodes != null && nodes.size() > 0) {
            goHome();
          }
        }
        break;
    }

    requestInfo();
  }
};
