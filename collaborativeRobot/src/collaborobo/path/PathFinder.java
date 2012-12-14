package collaborobo.path;


import collaborobo.utils.Constants;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class PathFinder {        
    PriorityQueue<Node> open = new PriorityQueue<Node>();
    ArrayList<Node> closed = new ArrayList<Node>();
    public int explored = 0;
    private double maxExpected;
    Node start;
    Node goal;
    private double[][] map;
    
    final double ISWALL = 0.5;

    public PathFinder(double[][] map, boolean useProb) {
        this.map = map;
        this.maxExpected = map.length * map[0].length;
        if(useProb){
         growWalls(map);   
        }
    }

    private void growWalls(double[][] map) {
        List<Node> visited = new LinkedList<Node>();

        double step = (int) (Constants.ROBOT_RADIUS * Constants.MAP_PRECISION);
        double height = map.length - 1;
        double width = map[0].length - 1;

        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[0].length; x++) {
                Node current = new Node(x, y);

                if (!visited.contains(current) && map[y][x] > ISWALL) {
                    visited.add(current);

                    for (int i = 1; i <= step; i++) {
                        //baixo
                        if (y + i < height && map[y + i][x] < map[y][x]) {
                                visited.add(new Node(x, y + i));
                                map[y + i][x] = map[y][x];
                        }
                        //direita    
                        if (x + i < width && map[y][x + i] < map[y][x]) {
                            visited.add(new Node(x + i, y));
                            map[y][x + i] = map[y][x];
                        }
                        //cima    
                        if (y - i > 0 && map[y - i][x] < map[y][x]) {
                            visited.add(new Node(x, y - i));
                            map[y - i][x] = map[y][x];
                        }
                        //esquerda
                        if (x - i > 0 && map[y][x - i] < map[y][x]) {
                            visited.add(new Node(x - i, y));
                            map[y][x - i] = map[y][x];
                        }
                        //cima direita
                        if (y - i > 0 && x + i < width && map[y - i][x + i] < map[y][x]) {
                            visited.add(new Node(x + i, y - i));
                            map[y - i][x + i] = map[y][x];
                        }
                        //cima esquerda
                        if (y - i > 0 && x - i > 0 && map[y - i][x - i] < map[y][x]) {
                            visited.add(new Node(x - i, y - i));
                            map[y - i][x - i] = map[y][x];
                        }
                        //baixo direita
                        if (y + i < height && x + i < width && map[y + i][x + i] < map[y][x]) {
                            visited.add(new Node(x + i, y + i));
                            map[y + i][x + i] = map[y][x];
                        }
                        //baixo esquerda
                        if (y + i < height && x - i > 0 && map[y + i][x - i] < map[y][x]) {
                            visited.add(new Node(x - i, y + i));
                            map[y + i][x - i] = map[y][x];
                        }
                    }
                }
            }
        }
    }

    protected boolean isGoal(Node node) {
        return (node.x == goal.x) && (node.y == goal.y);
    }

    protected Double movementcost(Node from, Node to) {

        if (from.x == to.x && from.y == to.y) {
            return 0.0;
        }

        if (map[to.y][to.x] < ISWALL) {
            return 1.0 + map[to.y][to.x]; //maybe change this to probability
        }

        return Double.MAX_VALUE; //not possible to cross that point
    }

    protected Double h(Node from, Node to) {
        /*Using Linear Distance Heuristic as in:
         http://theory.stanford.edu/~amitp/GameProgramming/Heuristics.html */
         double cost = 1; //cost from one place to the adjacent
         double line = (from.x - to.x) * (from.x - to.x) + (from.y - to.y) * (from.y - to.y);
         double h = cost * Math.sqrt(line);
         

        /*Using Manhattan distance as in:
         http://theory.stanford.edu/~amitp/GameProgramming/Heuristics.html
        double cost = 1; //cost from one place to the adjacent
        double line = Math.abs(from.x - to.x) + Math.abs(from.y - to.y);
        double h = cost * line;
        */
         
         /*Using Chebyshev distance as in: h(n) = D * max(abs(n.x-goal.x), abs(n.y-goal.y))
         http://theory.stanford.edu/~amitp/GameProgramming/Heuristics.html 
        double cost = 1; //cost from one place to the adjacent
        double line = Math.max(Math.abs(from.x - to.x), Math.abs(from.y - to.y));
        double h = cost * line; */



        return h;
    }

    protected List<Node> generateSuccessors(Node node) { //considerando apenas 4 dir.
        List<Node> ret = new LinkedList<Node>();
        int x = node.x;
        int y = node.y;
        
        //baixo
        if (y < map.length - 1 && map[y + 1][x] < ISWALL) {
            ret.add(new Node(x, y + 1));
        }

        //direita
        if (x < map[0].length - 1 && map[y][x + 1] < ISWALL) {
            ret.add(new Node(x + 1, y));
        }

        //cima
        if (y > 0 && map[y - 1][x] < ISWALL) {
            ret.add(new Node(x, y - 1));
        }

        //esquerda
        if (x > 0 && map[y][x - 1] < ISWALL) {
            ret.add(new Node(x - 1, y));
        }
        
        //cima direita
        if (y > 0 &&  x < map[0].length - 1 && map[y - 1][x + 1] < ISWALL) {
            ret.add(new Node(x + 1, y - 1));
        }
        
        //cima esquerda
        if (y > 0 && x > 0 && map[y - 1][x - 1] < ISWALL) {
            ret.add(new Node(x - 1, y - 1));
        }
        
        //baixo direita
        if (y < map.length - 1 && x < map[0].length - 1 && map[y + 1][x + 1] < ISWALL) {
            ret.add(new Node(x + 1, y + 1));
        }
        
        //baixo esquerda
        if (y < map.length - 1 && x > 0 && map[y + 1][x - 1] < ISWALL) {
            ret.add(new Node(x - 1, y + 1));
        }
        

        if(ret.isEmpty()){
            System.out.println("No sucessors of "+node);
        }
        return ret;
    }

    private List<Node> rebuildPath(Node goalNode) {
        List<Node> path = new LinkedList<Node>();
        Node n = goalNode;
        while (n.parent != null) {
            path.add(n);
            n = n.parent;
        }

        return path;
    }

    public List<Node> calculate(int oX, int oY, int goalX, int goalY) {
        this.goal = new Node(goalX, goalY);
        this.start = new Node(oX, oY);
        
        return this.compute(this.start);
    }

    private List<Node> compute(Node start) {
        open.add(start);
        //System.out.println("Goal: " + goal);
        List<Node> path = null;

        while (open.peek() != null) {
            if (isGoal(open.peek())) {
                return rebuildPath(open.poll());
            } else {
                this.explored++;
                Node current = open.poll();
                closed.add(current);

//            System.out.println("Current:" +current);
//            System.out.println("Parent:" + current.parent);
//            System.out.println("Open:" +open);
//            System.out.println("Closed:" +closed);

                List<Node> sucessors = generateSuccessors(current);
//                System.out.println("Sucessors:" + sucessors);
//                System.out.println();
                
                for (Node n : sucessors) {
                    double cost = current.g + movementcost(current, n);

                    if (open.contains(n) && cost < n.g) {
                        open.remove(n);
                    }
                    if (closed.contains(n) && cost < n.g) {
                        closed.remove(n);
                    }
                    if (!(open.contains(n) || closed.contains(n))) {
                        n.g = cost;//set g(neighbor) to cost
                        n.f = n.g + h(n, goal);//set priority queue rank to g(neighbor) + h(neighbor)
                        n.parent = current;//set neighbor's parent to current
                        open.add(n);
                    }
                }
            }
        }
        return path;
    }
    
    public static List<Node> calculate(double[][] map, int oX, int oY, int goalX, int goalY, boolean useProb) {

        PathFinder pf = new PathFinder(map, useProb);

        List<Node> nodes = pf.calculate(oX, oY, goalX, goalY);
        if (nodes == null) {
            System.out.println("No path");
        }

        return nodes;
    }
}
