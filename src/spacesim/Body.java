/**
 * An object in space
 */

package spacesim;

import java.util.concurrent.CopyOnWriteArrayList;

public class Body extends SpaceObject {
    public double DENSITY;
    private SpaceSim sim;
    public double mass;
    public boolean moveable;
    public String name;
    public State nextState;
    
    // type, state, and r from SpaceObject
    
    public CopyOnWriteArrayList<Shadow> pastPos;
    
    /**
     * Create an object in space. 
     * @param sim Reference to main class
     * @param type can be "Sun", "Planet", or "Shadow"
     * @param mass
     * @param density Also determines the size, given the mass and density
     * @param x x position
     * @param y y position
     * @param z z position
     * @param dx x velocity
     * @param dy y velocity
     * @param dz  velocity
     */
    public Body(SpaceSim sim, String type, double mass, double density,
            double x, double y, double z, double dx, double dy, double dz) {
        this.DENSITY = density;
        this.sim = sim;
        this.type = type;
        this.mass = mass;
        state = new State(x, y, z, dx, dy, dz);
        nextState = state.copy();
        pastPos = new CopyOnWriteArrayList<>();
        
        if (type.equals("Sun")) {
            moveable = false;
            color = "Orange";
        } else {
            moveable = true;
            color = "Black";
        }
    }
    
    /**
     * Shadows are used as one way of tracking the planets
     */
    public class Shadow extends SpaceObject {
        public Shadow(Body b) {
            this.type = "Shadow";
            this.state = b.state.copy();
            this.r = b.r;
            color = "Blue";
        }
    }
    
    /**
     * An object holding the derivatives of position and velocity
     */
    private class Derivative {
        public double dx, dy, dz, dvx, dvy, dvz;
        public Derivative(double dx, double dy, double dz, double dvx, double dvy, double dvz) {
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
            this.dvx = dvx;
            this.dvy = dvy;
            this.dvz = dvz;
        }
        
        @Override
        public String toString() {
            return "dx: "+dx+", dy: "+dy+", dz: "+dz+", dvx: "+dvx+", dvy: "+dvy+", dvz: "+dvz;
        }
    }
    
    /**
     * Performs the Runge-Kutta time step
     * @param t current time
     * @param dt time step
     */
    public void updateBody(double t, double dt) {
        Derivative a = initEval(state, t);
        Derivative b = evaluate(state, a, t, dt*0.5);
        Derivative c = evaluate(state, b, t, dt*0.5);
        Derivative d = evaluate(state, c, t, dt);
        
        double dxdt = 1.0/6.0 * (a.dx + 2*b.dx + 2*c.dx + d.dx);
        double dydt = 1.0/6.0 * (a.dy + 2*b.dy + 2*c.dy + d.dy);
        double dzdt = 1.0/6.0 * (a.dz + 2*b.dz + 2*c.dz + d.dz);
        double dvxdt = 1.0/6.0 * (a.dvx + 2*b.dvx + 2*c.dvx + d.dvx);
        double dvydt = 1.0/6.0 * (a.dvy + 2*b.dvy + 2*c.dvy + d.dvy);
        double dvzdt = 1.0/6.0 * (a.dvz + 2*b.dvz + 2*c.dvz + d.dvz);
        
        nextState.x += dxdt * dt;
        nextState.y += dydt * dt;
        nextState.z += dzdt * dt;
        nextState.vx += dvxdt * dt;
        nextState.vy += dvydt * dt;
        nextState.vz += dvzdt * dt;
    }
    
    /**
     * Update the shadows
     */
    public void addPos() {
        if (pastPos.size() > 200) {
            pastPos.remove(0);
        }
        pastPos.add(new Shadow(this));
    }
    
    /**
     * Calculate the derivatives for the first time
     * @param s The body in question
     * @param t Current time
     */
    private Derivative initEval(State s, double t) {
        double newDX = s.vx;
        double newDY = s.vy;
        double newDZ = s.vz;
        double[] accel = acceleration(s, t);
        double newDVX = accel[0];
        double newDVY = accel[1];
        double newDVZ = accel[2];
        return new Derivative(newDX, newDY, newDZ, newDVX, newDVY, newDVZ);
    }
    
    /**
     * Calculate the derivatives
     * @param initial The initial state
     * @param d The value of previously calculated derivative
     * @param t Current time
     * @param dt time step
     */
    private Derivative evaluate(State initial, Derivative d, double t, double dt) {
        double newX = initial.x + d.dx*dt;
        double newY = initial.y + d.dy*dt;
        double newZ = initial.z + d.dz*dt;
        double newVX = initial.vx + d.dvx*dt;
        double newVY = initial.vy + d.dvy*dt;
        double newVZ = initial.vz + d.dvz*dt;
        State newState = new State(newX, newY, newZ, newVX, newVY, newVZ);
        
        double newDX = newState.vx;
        double newDY = newState.vy;
        double newDZ = newState.vz;
        double[] accel = acceleration(newState, t+dt);
        double newDVX = accel[0];
        double newDVY = accel[1];
        double newDVZ = accel[2];
        return new Derivative(newDX, newDY, newDZ, newDVX, newDVY, newDVZ);
    }
    
    /**
     * Actually figure out the acceleration from gravity
     * @param s The body in question
     * @param t The current time
     * @return (x,y,x) components of acceleration
     */
    private double[] acceleration(State s, double t) {
        double ax = 0, ay = 0, az = 0;
        for (Body b : sim.bodies) {
            if (this != b) {
                double dx = b.state.x - s.x;
                double dy = b.state.y - s.y;
                double dz = b.state.z - s.z;
                double dsq = dx*dx + dy*dy + dz*dz;
                double dr = 1.0;
                double force = 0;
                if (dsq > 0.0000001) {
                    dr = Math.sqrt(dsq);
                    force = SpaceSim.GRAV_CONST*mass*b.mass*Math.pow(dr, sim.forceLaw);
                }
                
                //System.out.println(force+", "+dr);

                ax += force * dx/dr / mass;
                ay += force * dy/dr / mass;
                az += force * dz/dr / mass;
            }
        }
        return new double[] {ax, ay, az};
    }
    
    public void setRadiusFromMass() {
        r = Math.pow(3.0 * mass / (4.0 * Math.PI * DENSITY), 1.0/3.0);
    }
}
