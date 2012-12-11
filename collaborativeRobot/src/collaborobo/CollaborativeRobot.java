package collaborobo;

import collaborobo.path.PathFinder;
import collaborobo.path.Node;
import ciberIF.beaconMeasure;
import ciberIF.ciberIF;
import collaborobo.utils.Constants;
import collaborobo.utils.Util;
import collaborobo.visualizers.*;
import java.util.List;
import java.util.Observable;

public class CollaborativeRobot extends Observable {

  enum State {

    RUN, WAIT, RETURN
  }
  public double[][] map = new double[Constants.mapSizeY][Constants.mapSizeX];
  public double[][] probabilitiesMap = new double[Constants.mapSizeY][Constants.mapSizeX];
  public int[][] beaconProbability = new int[Constants.mapSizeY][Constants.mapSizeX];
  public double[][] aStarMatrix = new double[Constants.mapSizeY][Constants.mapSizeX];
  public int initialPosX, initialPosY;
  private boolean firstReturn = true;
  public double PosX;
  public double PosY;
  public double PosX_aStar;
  public double PosY_aStar;
  public double previousPosX;
  public double previousPosY;
  public int halfPosX, halfPosY;
  public int frontSensorPosX;
  public int frontSensorPosY;
  public double frontSensor;
  public int leftSensorPosX;
  public int leftSensorPosY;
  public double leftSensor;
  public int rightSensorPosX;
  public int rightSensorPosY;
  public double rightSensor;
  public double turnAround = -1.0;
  public double previousMotorPowL = 0.0;
  public double previousMotorPowR = 0.0;
  public double orientation = 0;
  public List<Node> nodes;
  
  private Communication comm;
  
  public String name;
  public int pos;
  public beaconMeasure beacon;
  public double compass;
  public ciberIF cif;
  private int ground;
  private State state;

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

    for (int i = 0; i < aStarMatrix.length; i++) {
      for (int j = 0; j < aStarMatrix[i].length; j++) {
        aStarMatrix[i][j] = 1.0;
      }
    }
/*
    MapVisualizer visualizer = new MapVisualizer(this);
    visualizer.start();
*/
    MapProbabilitiesVisualizer probVisualizer = new MapProbabilitiesVisualizer(this);
    probVisualizer.start();

/*    BeaconVisualizer bv = new BeaconVisualizer(this);
    bv.start();*/
 
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

    double cpass = Util.makePositive(this.compass);

    if (cpass > 80 && cpass < 100) {
      orientation = Math.toRadians(90);
    } else if (cpass > 170 && cpass < 190) {
      orientation = Math.toRadians(180);
    } else if (cpass > 260 && cpass < 280) {
      orientation = Math.toRadians(270);
    } else {
      orientation = Math.toRadians(0);
    }

    orientation = Util.normalizeAngle(orientation);

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

    if (frontSensor > 9.0 || leftSensor > 9.0 || rightSensor > 9.0 || cif.GetBumperSensor()) {
      leftMotorForce = -0.15;
      rightMotorForce = -0.15;
      emergency = true;
    }

    cif.DriveMotors(leftMotorForce, rightMotorForce);

    leftMotorForce = (leftMotorForce + previousMotorPowL) / 2.0;
    rightMotorForce = (rightMotorForce + previousMotorPowR) / 2.0;

    final double rot = (rightMotorForce - leftMotorForce) / (Constants.ROBOT_RADIUS * 2);
    orientation = Util.normalizeAngle(this.orientation + rot);

    double L = (rightMotorForce + leftMotorForce) / 2.0;

    double iX = ((Math.cos(Math.toRadians(compass)) * L)) * Constants.MAP_PRECISION;
    double iY = ((Math.sin(Math.toRadians(compass)) * L)) * Constants.MAP_PRECISION;

    double robotMapX2 = (PosX + iX);
    double robotMapY2 = (PosY - iY);

    PosX = robotMapX2;
    PosY = robotMapY2;

    PosY_aStar = initialPosY - cif.GetY()
            * Constants.MAP_PRECISION + halfPosY;
    PosX_aStar = cif.GetX() * Constants.MAP_PRECISION
            - initialPosX + halfPosX;

    if (firstReturn) {
      int y = (int) Math.round((PosY_aStar > previousPosY ? PosY_aStar : previousPosY)) + 1;
      int x = (int) Math.round((PosX_aStar > previousPosX ? PosX_aStar : previousPosX)) + 1;
      double matrix[][] = new double[y][x];

      List<Node> nodes = PathFinder.calculate(matrix, (int) Math.round(PosX_aStar), (int) Math.round(PosY_aStar), (int) Math.round(previousPosX), (int) Math.round(previousPosY));
      for (Node n : nodes) {
        updateAStarMatrix(n.x, n.y);
      }
    }

    previousMotorPowL = leftMotorForce;
    previousMotorPowR = rightMotorForce;

    previousPosX = PosX_aStar;
    previousPosY = PosY_aStar;

    updateAStarMatrix((int) Math.round(PosX_aStar), (int) Math.round(PosY_aStar));

    if (emergency) {
      if (!firstReturn) {
        nodes = PathFinder.calculate(aStarMatrix, (int) Math.round(PosX_aStar), (int) Math.round(PosY_aStar), halfPosX, halfPosY);
      }
      if (!firstReturn && (nodes == null || nodes.isEmpty())) {
        cif.DriveMotors(0.0, 0.0);
        System.exit(0); /* Terminate agent */
      }
    }

  }

  private void updateAStarMatrix(int x, int y) {

    aStarMatrix[y][x] = 0.0;
    aStarMatrix[y][x + 1] = 0.0;
    aStarMatrix[y][x - 1] = 0.0;
    aStarMatrix[y + 1][x] = 0.0;
    aStarMatrix[y - 1][x] = 0.0;
    aStarMatrix[y + 1][x + 1] = 0.0;
    aStarMatrix[y - 1][x - 1] = 0.0;
    aStarMatrix[y + 1][x - 1] = 0.0;
    aStarMatrix[y - 1][x + 1] = 0.0;

    for (int i = 0; i < probabilitiesMap.length; i++) {
      for (int j = 0; j < probabilitiesMap[i].length; j++) {
        if (probabilitiesMap[i][j] > 0.75) {
          aStarMatrix[i][j] = 0.80;
        }
      }
    }
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

    int[] frontSensorPos = Util.frontSensorMapPosition(robotMapX, robotMapY, compass);
    int[] leftSensorPos = Util.leftSensorMapPosition(robotMapX, robotMapY, compass);
    int[] rightSensorPos = Util.rightSensorMapPosition(robotMapX, robotMapY, compass);
    // System.out.println("Left Sensor X: " + leftSensorPos[0]);
    // System.out.println("Left Sensor Y: " + leftSensorPos[1]);
    // System.out.println("Right Sensor X: " + rightSensorPos[0]);
    // System.out.println("Right Sensor Y: " + rightSensorPos[1]);
    
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
    	if (beacon.beaconVisible)
    		beacon = cif.GetBeaconSensor(Constants.BEACON);
    	else {
    		beacon = createBeaconFromMatrix();
    	}
    } else {
      beacon.beaconVisible = false;
    }
  }

  private beaconMeasure createBeaconFromMatrix() {
	  
	int maxY = 0, maxX = 0;
	int max = 0;
	for(int y=0;y<beaconProbability.length;y++)
		for(int x=0;x<beaconProbability[y].length;x++)
			if (beaconProbability[y][x] > max) {
				maxY = y;
				maxX = x;
				max = beaconProbability[y][x];
			}
	
	beaconMeasure beacon = new beaconMeasure();
        if(max == 0){
            beacon.beaconVisible = false;
        }
	beacon.beaconDir = Math.toDegrees(Math.atan2(PosY - maxY, maxX - PosX));
	beacon.beaconVisible = true;
	return beacon;
}

public void requestInfo() {

    // estes são sempre pedidos
    cif.RequestIRSensor(0);
    cif.RequestIRSensor(1);
    cif.RequestIRSensor(2);

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

    double anglePoint = Util.makePositive(Math.atan2(PosY_aStar - n.y, n.x - PosX_aStar));
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

    int x = (int) Math.round(PosX_aStar);
    int y = (int) Math.round(PosY_aStar);

    if (nodes != null && (Math.abs(x - n.x) > 2 || Math.abs(y - n.y) > 2)) {
      nodes.add(new Node(n.x, n.y));
    }

    //prunePath();
    //System.out.println(n.x + " " + n.y + " " + x + " " + y);
  }

  private void alignRobot(double anglePoint, double angleNow) {

    double dif = Util.angleDifference(Util.normalizeAngle(angleNow),
            Util.normalizeAngle(anglePoint));

    requestInfo();
    cif.ReadSensors();
    getInfo();

    double lPowIn = 0.20 * (2 * Constants.ROBOT_RADIUS * -dif);
    double rPowIn = 0.20 * (2 * Constants.ROBOT_RADIUS * dif);

    DriveMotors(Util.constrain(lPowIn, -0.15, 0.15), Util.constrain(rPowIn, -0.15, 0.15));

    updateMap();
  }

  private void prunePath() {

    if (nodes == null || nodes.isEmpty()) {
      return;
    }

    Node n = nodes.get(0);

    double distance = Math.sqrt(Math.abs(n.x - PosX)
            * Math.abs(n.x - PosX) + Math.abs(n.y - PosY)
            * Math.abs(n.y - PosY));

    for (int i = 1; i < nodes.size(); i++) {
      double distance2 = Math.sqrt(Math.abs(nodes.get(i).x - PosX)
              * Math.abs(nodes.get(i).x - PosX) + Math.abs(nodes.get(i).y - PosY)
              * Math.abs(nodes.get(i).y - PosY));
      if (distance <= distance2) {
        nodes.remove(i - 1);
        distance = distance2;
      } else {
        break;
      }
    }
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
          nodes = PathFinder.calculate(aStarMatrix, (int) Math.round(PosX), (int) Math.round(PosY), halfPosX, halfPosY);

          if (nodes != null && nodes.size() > 0) {
            for (int i = 0; i < nodes.size(); i++) {
              map[nodes.get(i).y][nodes.get(i).x] = -3.0;
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
