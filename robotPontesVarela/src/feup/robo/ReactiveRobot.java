package feup.robo;

import ciberIF.beaconMeasure;
import ciberIF.ciberIF;

public class ReactiveRobot {

    public static final String DEFAULT_NAME = "reactive";
    public static final String DEFAULT_HOST = "localhost";
    public static final int DEFAULT_POS = 1;
    
    public static final double OBSTACLE_THRESHOLD = 1.8;
    public static final double FRONT_THRESHOLD = 2.3;
    public static final int RIGHT = 1;
    public static final int LEFT = -1;
    public static final int GROUND_FOUND = 0;
    public static final int GROUND_NOT_FOUND = -1;
    public static final int BEACON_FLAG = 0;
    public static final double MAX_VALUE = 0.3;
    public static final double FORWARD_VALUE = 0.08;
    
    //Ciber Rato interface
    private ciberIF crinterf;
    private String name;
    private double frontSensor, leftSensor, rightSensor;
    private beaconMeasure beacon;
    private int ground = -1;
    private int turnDirection = RIGHT;

    public ReactiveRobot() {
        crinterf = new ciberIF();
        beacon = new beaconMeasure();
    }

    public static void main(String[] args) {
        String host, robName;
	int pos; 
	int arg;

	//default values
	host = DEFAULT_HOST;
	robName = DEFAULT_NAME;
	pos = DEFAULT_POS;

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
		else {
                    throw new Exception();
                }
	    }
	}
	catch (Exception e) {
            print_usage();
            return;
	}

        // create client
        ReactiveRobot robot = new ReactiveRobot();
        robot.name = robName;
        // register robot in simulator
        robot.crinterf.InitRobot(robName, pos, host);
        // main loop
        robot.mainLoop();
    }

    /**
     * reads a new message, decides what to do and sends action to simulator
     */
    public void mainLoop() {
        while (true) {
            readSensors();
            double time = crinterf.GetTime();
            if (time > 0) {
                System.out.println("Time: " + time);
                if (shouldStop()) {
                    stop();
                    finish();
                } else if (hasObstacle()) {
                    avoidObstacle();
                } else if (shouldMoveToBeacon()) {
                    moveToBeacon();
                } else {
                    wander();
                }
            }
        }
    }
    
    private void readSensors() {
        crinterf.ReadSensors();
        if (crinterf.IsObstacleReady(0)) {
            frontSensor = crinterf.GetObstacleSensor(0);
            //System.out.println("Sensor 0:" + frontSensor);
        }
        if (crinterf.IsObstacleReady(1)) {
            leftSensor = crinterf.GetObstacleSensor(1);
            //System.out.println("Sensor 1:" + irSensor1);
        }
        if (crinterf.IsObstacleReady(2)) {
            rightSensor = crinterf.GetObstacleSensor(2);
            //System.out.println("Sensor 2:" + irSensor2);
        }
        if (crinterf.IsBeaconReady(BEACON_FLAG)) {
            beacon = crinterf.GetBeaconSensor(BEACON_FLAG);
        }
        if (crinterf.IsGroundReady()) {
            ground = crinterf.GetGroundSensor();
        }
    }

    private boolean hasObstacle() {
        if (frontSensor > FRONT_THRESHOLD || 
                leftSensor > OBSTACLE_THRESHOLD ||
                rightSensor > OBSTACLE_THRESHOLD) {
            return true;
        }
        return false;
    }

    private void avoidObstacle() {
        if(frontSensor > 10 || rightSensor > 10 || leftSensor > 10){
            double value = 0.1;
            goBack(value);
            System.out.println("Go Back Value: " + value);
        }else if(frontSensor > FRONT_THRESHOLD) {
            double value = map(frontSensor, -1, 10, 0.05, MAX_VALUE);
            System.out.println("Avoid Front Value: " + value);
            if(Math.random() > 0.15){
                turnRight(value);
            } else {
                turnLeft(value);
            }
        }else if(rightSensor > OBSTACLE_THRESHOLD) {
            double value = map(rightSensor, -1, 10, 0.05, MAX_VALUE);
            System.out.println("Avoid Right Value: " + value);
            turnLeft(value);
        }else  if(leftSensor > OBSTACLE_THRESHOLD) {
            double value = map(frontSensor, -1, 10, 0.05, MAX_VALUE);
            System.out.println("Avoid Left Value: " + value);
            turnRight(value);
        }
    }
    
    public boolean isBeaconTime(double time) {
        for(double tMod=50; tMod<60;tMod++){
            if(time % tMod == 0)
                return true;
        }
        return false;
    }

    private boolean shouldMoveToBeacon() {
        double time = crinterf.GetTime();
        if(isBeaconTime(time))
            return beacon.beaconVisible && ground != GROUND_FOUND;
        else
            return false;
    }
    
    private boolean shouldStop() {
        return ground == GROUND_FOUND;      
    }

    private void moveToBeacon() {
        if(beacon.beaconDir > 10) {
            double value = map(beacon.beaconDir, 10, 180, 0.01, 1.2);
            System.out.println("Beacon Move Left: " + value);
            turnLeft(value);
        } else if(beacon.beaconDir < -10){
            double value = map(beacon.beaconDir, -10, -180, 0.01, 1.2);
            System.out.println("Beacon Move Right: " + value);
            turnRight(value);
        } else {
            System.out.println("Beacon Forward");
            goForward(FORWARD_VALUE);
        }
    }

    private void wander() {
        double time = crinterf.GetTime();
        
        if(isWanderTime(time)){
            if(turnDirection == RIGHT){
                turnRight(0.1);
                System.out.println("Wander: Turning Right");
            }else if(turnDirection == LEFT){
                turnLeft(0.1);
                System.out.println("Wander: Turning Left");
            }
            if(time % 102 == 0) {
                turnDirection *= -1;
                System.out.println("Wander: Inverting direction");
            }
        } else {
            goForward(FORWARD_VALUE);
        }
    }
    
    public boolean isWanderTime(double time) {
        for(int tMod=100; tMod<103;tMod++){
            if(time % tMod == 0){
                return true;
            }
        }
        return false;
    }
    
    public void turnRight(double value) {
        crinterf.DriveMotors(value, -0.4*value);
    }
    
    public void turnLeft(double value) {
        crinterf.DriveMotors(-0.4*value, value);
    }
    
    public void goForward(double value) {
        crinterf.DriveMotors(value, value);
    }
    
    public void goBack(double value) {
        crinterf.DriveMotors(-value, -value);
    }
    
    public void stop() {
        crinterf.DriveMotors(0.0, 0.0);
    }
    public void finish() {
        crinterf.SetVisitingLed(true);
        crinterf.Finish();
        if (crinterf.GetFinished()) {
            System.exit(0);
        }
    }

    static void print_usage() {
        System.out.println("Usage: java jClient [-robname <robname>] [-pos <pos>] [-host <hostname>[:<port>]]");
    }
    
    //Arduino map function: http://www.arduino.cc/en/Reference/map
    public double map(double x, double in_min, double in_max, double out_min, double out_max) {
        return (x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min;
    }
}
