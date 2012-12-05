public class Node implements Comparable {

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
         * @return <code>less than 0</code> This object is smaller * * *          * than <code>0</code>; <code>0</code> Object are the * *
         * same. <code>bigger than 0</code> This object is bigger than o.
         */
        @Override
        public int compareTo(Object o) {
            Node p = (Node) o;
            return (int) (this.f - p.f);
        }

        @Override
        public boolean equals(Object other) {
            Node p = (Node) other;
            return p.x == this.x && p.y == this.y;
        }
    }
    
    
