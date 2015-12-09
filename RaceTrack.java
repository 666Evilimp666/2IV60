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

    /** Array with 3N control points, where N is the number of segments. */
    private Vector[] controlPoints = null;
    
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
            // draw the spline track
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
        return Vector.O; // <- code goes here
    }
    
    /**
     * Returns a tangent on a bezier segment with control points
     * P0, P1, P2, P3 at 0 <= t < 1.
     */
    private Vector getCubicBezierTangent(double t, Vector P0, Vector P1,
                                                   Vector P2, Vector P3) {
        return Vector.O; // <- code goes here
    }
}
