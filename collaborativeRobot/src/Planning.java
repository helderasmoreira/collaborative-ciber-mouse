
import java.util.LinkedList;
import java.util.List;

public class Planning {

    final double ISWALL = 0.75;

    public static class Node {

        public int x;
        public int y;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ") ";
        }
    }
    private double[][] map;
    Node origin;
    Node goal;

    public Planning(double[][] map, int originX, int originY, int goalX, int goalY) {
        this.map = map;
        this.origin = new Node(originX, originY);
        this.goal = new Node(goalX, goalY);
    }

    protected boolean isGoal(Node node) {
        return (node.x == goal.x) && (node.y == goal.y);
    }

    protected Double g(Node from, Node to) {

        if (from.x == to.x && from.y == to.y) {
            return 0.0;
        }

        if (map[to.y][to.x] <= ISWALL) {
            return 1.0; //for now we investigate all the map
            //test if returning probability is better
        }

        return Double.MAX_VALUE; //not possible to cross that point
    }

    protected Double h(Node from, Node to) {
        /*Using Linear Distance Heuristic as in:
         http://theory.stanford.edu/~amitp/GameProgramming/Heuristics.html */
        double cost = 1; //cost from one place to the adjacent
        double line = (from.x - to.x) * (from.x - to.x) + (from.y - to.y) * (from.y - to.y);

        return new Double(cost * Math.sqrt(line));
    }

    ///TO-DO: improve this considering 4 directions only now
    protected List<Node> generateSuccessors(Node node) {
        List<Node> ret = new LinkedList<Node>();
        int x = node.x;
        int y = node.y;

        if (y < map.length - 1 && map[y + 1][x] == 1) {
            ret.add(new Node(x, y + 1));
        }

        if (x < map[0].length - 1 && map[y][x + 1] == 1) {
            ret.add(new Node(x + 1, y));
        }

        return ret;
    }
}
