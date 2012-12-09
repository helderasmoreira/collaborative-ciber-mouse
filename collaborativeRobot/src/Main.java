
public class Main {

  public static void main(String[] args) {

    String host, robName;
    int arg;

    // default values
    host = "localhost";
    robName = "Collabot";
    int pos = 1;

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
    CollaborativeRobot robot = new CollaborativeRobot();
    robot.name = robName;
    robot.pos = pos;
    robot.cif.InitRobot(robName, pos, host);

    ComputeProbabilities observing = new ComputeProbabilities();
    robot.addObserver(observing);

    robot.mainLoop();
  }

  private static void print_usage() {
    System.out.println(
            "Usage: java jClient [-robname <robname>] [-pos <pos>] [-host <hostname>[:<port>]]");
  }
}
