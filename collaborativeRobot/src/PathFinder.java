
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class PathFinder {

    final double ISWALL = 0.75;
    PriorityQueue<PathFinder.Node> open = new PriorityQueue<>();
    ArrayList<PathFinder.Node> closed = new ArrayList();
    //int expandedCounter = 0;
    //double lastCost = 0.0;
    private double[][] map;
    Node goal;

    public static class Node implements Comparable {

        public int x;
        public int y;
        public Node parent;
        public double g;
        public double f;

        Node(int x, int y) {
            this.x = x;
            this.y = y;
            g = f = 0;
            parent = null;
        }

        @Override
        public String toString() {
            return "(" + x + ", " + y + ") ";
        }

        /**
         * Compare to another object using the total cost f.
         *
         * @param o The object to compare to.
         * @see Comparable#compareTo()
         * @return <code>less than 0</code> This object is smaller *
         * than <code>0</code>; <code>0</code> Object are the
         * same. <code>bigger than 0</code> This object is bigger than o.
         */
        @Override
        public int compareTo(Object o) {
            Node p = (Node) o;
            return (int) (this.f - p.f);
        }
    }

    public PathFinder(double[][] map) {
        this.map = map;
    }

    protected boolean isGoal(Node node) {
        return (node.x == goal.x) && (node.y == goal.y);
    }

    protected Double g(Node from, Node to) {

        if (from.x == to.x && from.y == to.y) {
            return 0.0;
        }

        if (map[to.y][to.x] <= ISWALL) {
            return 1.0; //maybe change this to probability
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

    protected List<Node> generateSuccessors(Node node) {
        List<Node> ret = new LinkedList<>();
        int x = node.x;
        int y = node.y;
        if (y < map.length - 1 && map[y + 1][x] == 0.0) {
            ret.add(new Node(x, y + 1));
        }

        if (x < map[0].length - 1 && map[y][x + 1] == 0.0) {
            ret.add(new Node(x + 1, y));
        }

        if (y > 0 && map[y - 1][x] == 0.0) {
            ret.add(new Node(x, y - 1));
        }

        if (x > 0 && map[y][x - 1] == 0.0) {
            ret.add(new Node(x - 1, y));
        }

        return ret;
    }

    public List<Node> calculate(int oX, int oY, int goalX, int goalY) {
        this.goal = new Node(goalX, goalY);

        return this.compute(new Node(oX, oY));
    }

    private List<Node> compute(Node start) {
        open.add(start);
        System.out.println("Goal: " +goal);
        
        while (open.peek() != null && !isGoal(open.peek())) {
            Node current = open.poll();
            closed.add(current);
            
//            System.out.println("Current:" +current);
//            System.out.println("Parent:" + current.parent);
//            System.out.println("Open:" +open);
//            System.out.println("Closed:" +closed);

            List<Node> sucessors = generateSuccessors(current);
//            System.out.println("Successors:" +sucessors);
//            System.out.println();
            
            for (Node n : sucessors) {
                double cost = current.g + g(current, n);

                if (open.contains(n) && cost < n.g) {
                    open.remove(n);
                } else if(closed.contains(n) && cost < n.g) {
                    closed.remove(n);
                } else{
                    n.g = cost;//set g(neighbor) to cost
                    n.f = n.g + h(n, goal);//set priority queue rank to g(neighbor) + h(neighbor)
                    n.parent = current;//set neighbor's parent to current
                    open.add(n);
                }
            }
        }
        
        List<Node> path = new LinkedList<>();
        goal = open.poll();
        Node n = goal;        
        while(n.parent != null){
            path.add(n);
            n = n.parent;
        }
        
        return path;
    }

    //just to test
    public static void main(String[] args) {
        double[][] map = new double[][]{
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},
            {1.0, 1.0, 0.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0},
            {0.0, 1.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 1.0},
            {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 0.0},
            {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0, 1.0},
            {0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0}};

        PathFinder pf = new PathFinder(map);

        System.out.println("Find a path from the top left corner to the right bottom one.");

        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[0].length; j++) {
                System.out.print(map[i][j] + " ");
            }
            System.out.println();
        }

        long begin = System.currentTimeMillis();
        System.out.println(".");
        List<Node> nodes = pf.calculate(map[0].length - 1, map.length - 1, 0, 0);
        System.out.println(".");
        long end = System.currentTimeMillis();

        System.out.println("Time = " + (end - begin) + " ms");

        if (nodes == null) {
            System.out.println("No path");
        } else {
            System.out.print("Path = ");
            for (Node n : nodes) {
                System.out.print(n);
            }
            System.out.println();
        }

    }
}