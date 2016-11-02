/**
 * In theory there could be other space objects, but for now this is not really
 * needed. Serves as the base class for Body
 */

package spacesim;

public class SpaceObject {
    public String type;
    public State state;
    public double r;
    public String color;
    
    public class State {
        public double x, y, z, vx, vy, vz;
        
        /**
         * Generates a state in space from the initial position and velocity
         */
        public State(double x, double y, double z, double vx, double vy, double vz) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.vx = vx;
            this.vy = vy;
            this.vz = vz;
        }
        
        public State copy() {
            return new State(x, y, z, vx, vy, vz);
        }
        
        @Override
        public String toString() {
            return "x: "+x+", y: "+y+", z: "+z+", vx: "+vx+", vy: "+vy+", vz: "+vz;
        }
    }
}
