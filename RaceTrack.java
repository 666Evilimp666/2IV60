package robotrace;

import com.jogamp.opengl.util.gl2.GLUT;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

/**
 * Implementation of a race track that is made from Bezier segments.
 */
class RaceTrack {
    
    /** The width of one lane. The total width of the track is 4 * laneWidth. */
    private final static float laneWidth = 1.22f;
    private final static float trackWidth = 4 * laneWidth;

    // @TODO: 4N points? we are working with cubic bezier (function name) so why would it be 3N?
    /** Array with 3N control points, where N is the number of segments. */
    private Vector[] controlPoints = null;
    
    /* The length of the bezier segments based on control points given. */
    private double[] lengthDistrubion = null;
    
    /**
     * Constructor for the default track.
     */
    public RaceTrack() {
    }
    
    /**
     * Constructor for a spline track.
     */
    public RaceTrack(Vector[] controlPoints) {
        this.controlPoints = controlPoints;
        
        // Since we are working with cubic bezier splines all segments are based on 4 control points.
        int segments = controlPoints.length % 4;
        double[] lengths = {};
        
        // Loop through all segments and calculate their length
        for(int i = 0; i < segments; i++) {
            double len = 0;
            lengths[i] = len; 
        }
    }

    /**
     * Draws this track, based on the control points.
     */
    public void draw(GL2 gl, GLU glu, GLUT glut) {
        if (null == controlPoints) {
            /**
             * Drawing the top of the racetrack
             */
            gl.glBegin(gl.GL_QUAD_STRIP);
            for(double t = 0.0; t <= 1.0; t+=0.0001) {
                // Draw inner point of the track.
                // With an normal pointing up (Z)
                Vector v = this.getPoint(t);
                gl.glNormal3d(Vector.Z.x, Vector.Z.y, Vector.Z.z);
                gl.glVertex3d(v.x, v.y, 1);

                // Draw outer point of the track.
                Vector w = new Vector(v.x, v.y, v.z);
                double scalar = (w.length()+trackWidth)/w.length();
                w = w.scale(scalar);
                
                // With an normal pointing up (Z)
                gl.glNormal3d(Vector.Z.x, Vector.Z.y, Vector.Z.z);
                gl.glVertex3d(w.x, w.y, 1);
            }
            gl.glEnd();
            
            /**
             * Drawing the inner side of the racetrack
             */
            gl.glBegin(gl.GL_QUAD_STRIP);
            for(double t = 0.0; t <= 1.0; t+=0.0001) {
                // Top point of the racetrack
                Vector v = this.getPoint(t);
                Vector n = (new Vector(v.x*-1, v.y*-1, 0)).normalized();
                
                gl.glNormal3d(n.x, n.y, n.z);
                gl.glVertex3d(v.x, v.y, 1);
                
                gl.glNormal3d(n.x, n.y, n.z);
                gl.glVertex3d(v.x, v.y, -1);
            }
            gl.glEnd();
            
            
            /**
             * Drawing the outer side of the racetrack
            */
            gl.glBegin(gl.GL_QUAD_STRIP);
            for(double t = 0.0; t <= 1.0; t+=0.0001) {
                // Top point of the racetrack
                Vector v = this.getPoint(t);
                double scalar = (v.length()+trackWidth)/v.length();
                v = v.scale(scalar);
                
                Vector n = (new Vector(v.x, v.y, 0)).normalized();
                gl.glNormal3d(n.x, n.y, n.z);
                gl.glVertex3d(v.x, v.y, 1);
                
                gl.glNormal3d(n.x, n.y, n.z);
                gl.glVertex3d(v.x, v.y, -1);
            }
            gl.glEnd();
            
        } else {
            gl.glBegin(GL2.GL_QUAD_STRIP);
            for(double t = 0.0; t <= 1.0; t+=0.01) {
                Vector v = this.getCubicBezierPoint(t, this.controlPoints[0], this.controlPoints[1], this.controlPoints[2], this.controlPoints[3]);
                gl.glVertex3d(v.x(), v.y(), v.z());
                gl.glVertex3d(v.x(), v.y(), v.z()+0.5);
            }
            gl.glEnd();
        }
    }
    
    /**
     * Returns the center of a lane at 0 <= t < 1.
     * Use this method to find the position of a robot on the track.
     */
    public Vector getLanePoint(int lane, double t) {
        if (null == controlPoints) {
            Vector v = this.getPoint(t);
            double scalar = (v.length()+((laneWidth*lane)+laneWidth/2))/v.length();
            v = v.scale(scalar);
            
            return new Vector(v.x, v.y, 1);
        } else {
            
            
            return Vector.O; // <- code goes here
        }
    }
    
    /**
     * Returns the tangent of a lane at 0 <= t < 1.
     * Use this method to find the orientation of a robot on the track.
     */
    public Vector getLaneTangent(int lane, double t) {
        if (null == controlPoints) {
            return getTangent(t);
        } else {
            return Vector.O; // <- code goes here
        }
    }

    /**
     * Returns a point on the test track at 0 <= t < 1.
     */
    private Vector getPoint(double t) {
        //we create and immediately return a vector for the test track via the given formula
        return new Vector(10 * Math.cos(2 * Math.PI * t), 14 * Math.sin(2 * Math.PI * t), 0);
    }

    /**
     * Returns a tangent on the test track at 0 <= t < 1.
     */
    private Vector getTangent(double t) {
        return new Vector(-20*Math.PI * Math.sin(2 * Math.PI * t), 28*Math.PI * Math.cos(2 * Math.PI * t), 0);
    }
    
    /**
     * Returns a point on a bezier segment with control points
     * P0, P1, P2, P3 at 0 <= t < 1.
     */
    private Vector getCubicBezierPoint(double t, Vector P0, Vector P1,
                                                 Vector P2, Vector P3) {
        // Formula for quadratic bezierspline:
        // B(t) = (1-t)^3P0 + 3t(1-t)^2P1 + 3t^2(1-t)P2 + t^3P3 with t in [0,1]
        double calcX, calcY, calcZ;
        
        // Using the formula above to calculate X.
        calcX =     Math.pow(1-t,3)*P0.x()+
                    3*t*Math.pow(1-t,2)*P1.x() +
                    Math.pow(3*t,2)*(1-t)*P2.x() +
                    Math.pow(t,3)*P3.x();
        
        // Using the formula above to calculate Y.
        calcY =     Math.pow(1-t,3)*P0.y() +
                    3*t*Math.pow(1-t,2)*P1.y() +
                    Math.pow(3*t,2)*(1-t)*P2.y() +
                    Math.pow(t,3)*P3.y();
        
        // Using the formula above to calculate Y.
        calcZ =     Math.pow(1-t,3)*P0.z()+
                    3*t*Math.pow(1-t,2)*P1.z() +
                    Math.pow(3*t,2)*(1-t)*P2.z() +
                    Math.pow(t,3)*P3.z();
        
        return new Vector(calcX, calcY, calcZ);
    }
    
    /**
     * Returns a tangent on a bezier segment with control points
     * P0, P1, P2, P3 at 0 <= t < 1.
     */
    private Vector getCubicBezierTangent(double t, Vector P0, Vector P1,
                                                   Vector P2, Vector P3) {
        // Formula for quadratic bezierspline:
        // B(t) = (1-t)^3P0 + 3t(1-t)^2P1 + 3t^2(1-t)P2 + t^3P3 with t in [0,1]
        
        // Derivative is:
        // D(t) = -3*P0*(1 - t)^2 + P1*(3*(1 - t)^2 - 6*(1 - t)*t) + P2*(6*(1 - t)*t - 3*t^2) + 3*P3*t^2
        double calcX, calcY, calcZ;
        
        // Using the formula above to calculate X.
        calcX =     -3*Math.pow(1-t,2)*P0.x() +
                    (3*Math.pow(1 - t, 2) - 6*(1 - t)*t)*P1.x()+
                    (6*(1 - t)*t - Math.pow(3*t,2))*P2.x()+
                    3*Math.pow(t,2)*P3.x();
        
        // Using the formula above to calculate Y.
        calcY =     -3*Math.pow(1-t,2)*P0.y() +
                    (3*Math.pow(1 - t, 2) - 6*(1 - t)*t)*P1.y()+
                    (6*(1 - t)*t - Math.pow(3*t,2))*P2.y()+
                    3*Math.pow(t,2)*P3.y();
        
        // Using the formula above to calculate Z.
        calcZ =     -3*Math.pow(1-t,2)*P0.z() +
                    (3*Math.pow(1 - t, 2) - 6*(1 - t)*t)*P1.z()+
                    (6*(1 - t)*t - Math.pow(3*t,2))*P2.z()+
                    3*Math.pow(t,2)*P3.z();
        
        return new Vector(calcX, calcY, calcZ);
    }
}
