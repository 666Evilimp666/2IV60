package robotrace;

import com.jogamp.opengl.util.gl2.GLUT;
import static javax.media.opengl.GL.GL_FRONT;
import javax.media.opengl.GL2;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_DIFFUSE;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SHININESS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SPECULAR;
import javax.media.opengl.glu.GLU;

/**
 * Implementation of a race track that is made from Bezier segments.
 */
class RaceTrack {
    
    /** The width of one lane. The total width of the track is 4 * laneWidth. */
    private final static float laneWidth = 1.22f;
    private final static float trackWidth = 4 * laneWidth;
    
    /** based on how many points a curve / line / spline should be drawn. (0 < stepSize <= 1) **/
    private final float stepSize = 0.0001f;
    
    /** Array with location of trees on this track */
    private Vector[] treeLocations = new Vector[]{};

    /** Array with 3N control points, where N is the number of segments. */
    private Vector[] controlPoints = null;
    
    /* Total length of the track */
    private double trackLength = 0;
    
    /* The length of the bezier segments based on control points given. */
    private double[] lengthDistribution = null;
    
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
        
        if(controlPoints != null) {
            // Calculate the distance of each spline.
            // This is done in order to see what spline we are on since they don't always have the same size.
            // (i.e. if we have a track of 4 splines, and we are on 0.3 (30%) that does not imply we are on the second spline.
            double[] lengths = new double[controlPoints.length/4];
            // Since we are working with cubic bezier splines all segments are based on 4 control points.
            // Loop through all segments and calculate their length
            for(int i = 0; i < controlPoints.length/4; i++) {
                // Length so far
                double len = 0;

                // Loop through the track in the same steps we use to render it
                // And see the distance between each render point to calculate the distance
                // of the bezierspline.
                for(double t = stepSize; t <= 1.0; t+=stepSize) {
                    // Find start point of this step
                    Vector v = this.getCubicBezierPoint(t-stepSize, this.controlPoints[(i*4)], this.controlPoints[(i*4)+1], this.controlPoints[(i*4)+2], this.controlPoints[(i*4)+3]);

                    // Find end point of this step
                    Vector v2 = this.getCubicBezierPoint(t, this.controlPoints[(i*4)], this.controlPoints[(i*4)+1], this.controlPoints[(i*4)+2], this.controlPoints[(i*4)+3]);

                    // Subtract the start point from the end point to get the size of this segment.
                    // and add that to the current length.
                    len += (v2.subtract(v)).length();
                }

                lengths[i] = len;
                trackLength += len;
            }
            lengthDistribution = lengths;
        }
    }
    
    /**
     * Constructor for a spline track.
     */
    public RaceTrack(Vector[] controlPoints, Vector[] treeLocations) {
        this(controlPoints);
        
        this.treeLocations = treeLocations;
    }

    /**
     * Draws this track, based on the control points.
     */
    public void draw(GL2 gl, GLU glu, GLUT glut) {
        //enable 2D textures and set the required parameters
        gl.glEnable(gl.GL_TEXTURE_2D);
        gl.glTexEnvi(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_REPLACE);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_LINEAR);
        gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_LINEAR);

        if (null == controlPoints) {
            /**
             * Drawing the top of the racetrack
             */
            Base.track.bind(gl); //bind the track texture so we can use it
            gl.glBegin(gl.GL_QUAD_STRIP);
            for(double t = 0.0; t <= 1.0; t+=stepSize) {
                // Draw inner point of the track.
                // With an normal pointing up (Z)
                Vector v = this.getPoint(t);
                gl.glNormal3d(Vector.Z.x, Vector.Z.y, Vector.Z.z);
                gl.glTexCoord2i(0 , 0);
                gl.glVertex3d(v.x, v.y, 1);

                // Draw outer point of the track.
                Vector w = new Vector(v.x, v.y, v.z);
                double scalar = (w.length()+trackWidth)/w.length();
                w = w.scale(scalar);
                
                // With an normal pointing up (Z)
                gl.glNormal3d(Vector.Z.x, Vector.Z.y, Vector.Z.z);
                gl.glTexCoord2i(1 , 2);
                gl.glVertex3d(w.x, w.y, 1);
            }
            gl.glEnd();
            Base.brick.bind(gl); //bind the brick texture so we can use it
            /**
             * Drawing the inner side of the racetrack
             */
            gl.glBegin(gl.GL_QUAD_STRIP);
            for(double t = 0.0; t <= 1.0; t+=stepSize) {
                // Top point of the racetrack
                Vector v = this.getPoint(t);
                Vector n = (new Vector(v.x*-1, v.y*-1, 0)).normalized();
                
                gl.glNormal3d(n.x, n.y, n.z);
                gl.glTexCoord2f(0f , 0f);
                gl.glVertex3d(v.x, v.y, 1);
                
                gl.glNormal3d(n.x, n.y, n.z);
                gl.glTexCoord2f(0.5f , 0.5f);
                gl.glVertex3d(v.x, v.y, -1);
            }
            gl.glEnd();
            
            
            /**
             * Drawing the outer side of the racetrack
            */
            gl.glBegin(gl.GL_QUAD_STRIP);
            for(double t = 0.0; t <= 1.0; t+=stepSize) {
                // Top point of the racetrack
                Vector v = this.getPoint(t);
                double scalar = (v.length()+trackWidth)/v.length();
                v = v.scale(scalar);
                
                Vector n = (new Vector(v.x, v.y, 0)).normalized();
                gl.glNormal3d(n.x, n.y, n.z);
                gl.glTexCoord2f(0f , 0f);
                gl.glVertex3d(v.x, v.y, 1);
                
                gl.glNormal3d(n.x, n.y, n.z);
                gl.glTexCoord2f(0.5f , 0.5f);
                gl.glVertex3d(v.x, v.y, -1);
            }
            gl.glEnd();
            
        } else {
            // The start/end "side" of the track only has to be drawn if the track is not looped.
            // i.e. the start and end point are different.
            if(
                    this.controlPoints[0].x() != this.controlPoints[this.controlPoints.length-1].x() ||
                    this.controlPoints[0].y() != this.controlPoints[this.controlPoints.length-1].y() ||
                    this.controlPoints[0].z() != this.controlPoints[this.controlPoints.length-1].z()
            ) {
                /**
                 * Drawing Start of the track
                 */
                gl.glBegin(gl.GL_QUAD_STRIP);
                    Vector startIn = this.controlPoints[0];
                    // Find the normal by taking a single step forward.
                    // Invert it and normalize it.
                    Vector startStep = (this.getCubicBezierPoint(stepSize, this.controlPoints[0], this.controlPoints[1], this.controlPoints[2], this.controlPoints[3])).subtract(startIn).normalized();
                    Vector normalStart = startStep.scale(-1);

                    // Draw the inner 2 points (Up and Down)
                    gl.glVertex3d(startIn.x, startIn.y, -1);
                    gl.glNormal3d(normalStart.x, normalStart.y, normalStart.z);
                    gl.glVertex3d(startIn.x, startIn.y, startIn.z);

                    // Draw outer point of the track.
                    Vector startOut = startIn.add((startStep).cross(Vector.Z).normalized().scale(trackWidth));

                    gl.glVertex3d(startOut.x, startOut.y, -1);
                    gl.glNormal3d(normalStart.x, normalStart.y, normalStart.z);
                    gl.glVertex3d(startOut.x, startOut.y, startOut.z);
                    gl.glNormal3d(normalStart.x, normalStart.y, normalStart.z);
                gl.glEnd();

                /**
                 * Drawing End of the track
                 */
                gl.glBegin(gl.GL_QUAD_STRIP);
                    Vector endIn = this.controlPoints[controlPoints.length-1];
                    // Find the normal by taking a single step backward.
                    // Invert it and normalize it.
                    Vector endStep = (this.getCubicBezierPoint(1-stepSize, this.controlPoints[this.controlPoints.length-4], this.controlPoints[this.controlPoints.length-3], this.controlPoints[this.controlPoints.length-2], this.controlPoints[this.controlPoints.length-1])).subtract(endIn);
                    Vector normalEnd = endStep.scale(-1).normalized();

                    // Draw the inner 2 points (Up and Down)
                    gl.glVertex3d(endIn.x, endIn.y, -1);
                    gl.glNormal3d(normalEnd.x, normalEnd.y, normalEnd.z);
                    gl.glVertex3d(endIn.x, endIn.y, endIn.z);

                    // Draw outer point of the track.
                    Vector endOut = endIn.add((endStep).cross(Vector.Z).normalized().scale(trackWidth*-1));
                    
                    gl.glVertex3d(endOut.x, endOut.y, -1);
                    gl.glNormal3d(normalEnd.x, normalEnd.y, normalEnd.z);
                    gl.glVertex3d(endOut.x, endOut.y, endOut.z);
                    gl.glNormal3d(normalEnd.x, normalEnd.y, normalEnd.z);
                gl.glEnd();
            }
            Base.track.bind(gl); //bind the track texture so we can use it
            /**
             * Drawing the top of the racetrack
             */
            gl.glBegin(gl.GL_QUAD_STRIP);
            for(int i = 0; i < controlPoints.length/4; i++) {
                for(double t = 0.0; t <= 1.0; t+=stepSize) {
                    // Draw inner point of the track.
                    // With an normal pointing up
                    Vector v = this.getCubicBezierPoint(t, this.controlPoints[(i*4)], this.controlPoints[(i*4)+1], this.controlPoints[(i*4)+2], this.controlPoints[(i*4)+3]);
                    gl.glNormal3d(Vector.Z.x, Vector.Z.y, Vector.Z.z);
                    gl.glTexCoord2i(0 , 0);
                    gl.glVertex3d(v.x, v.y, v.z);

                    // Draw outer point of the track.
                    Vector w = this.getCubicBezierPoint(t+stepSize, this.controlPoints[(i*4)], this.controlPoints[(i*4)+1], this.controlPoints[(i*4)+2], this.controlPoints[(i*4)+3]);
                    w = v.add((w.subtract(v)).cross(Vector.Z).normalized().scale(trackWidth));

                    // With an normal pointing up (Z)
                    gl.glNormal3d(Vector.Z.x, Vector.Z.y, Vector.Z.z);
                    gl.glTexCoord2i(1 , 2);
                    gl.glVertex3d(w.x, w.y, w.z);
                }
            }
            gl.glEnd();
            
            /**
             * Drawing the inner side of the racetrack
             */
            Base.brick.bind(gl); //bind the brick texture so we can use it
            gl.glBegin(GL2.GL_QUAD_STRIP);
            for(int i = 0; i < controlPoints.length/4; i++) {
                for(double t = 0.0; t <= 1.0; t+=stepSize) {
                    Vector v = this.getCubicBezierPoint(t, this.controlPoints[(i*4)], this.controlPoints[(i*4)+1], this.controlPoints[(i*4)+2], this.controlPoints[(i*4)+3]);
                    Vector n = (new Vector(v.x*-1, v.y*-1, 0)).normalized();
                    gl.glNormal3d(n.x, n.y, n.z);
                    gl.glTexCoord2f(0.0f , 0.0f);
                    gl.glVertex3d(v.x(), v.y(), v.z());
                    gl.glNormal3d(n.x, n.y, n.z);
                    gl.glTexCoord2f(0.5f , 0.5f);
                    gl.glVertex3d(v.x(), v.y(), -1);
                }
            }
            gl.glEnd();
            
            
            /**
             * Drawing the outer side of the racetrack
            */
            gl.glBegin(gl.GL_QUAD_STRIP);
            for(int i = 0; i < controlPoints.length/4; i++) {
                for(double t = 0.0; t <= 1.0; t+=stepSize) {
                    // Top point of the racetrack
                    Vector v = this.getCubicBezierPoint(t, this.controlPoints[(i*4)], this.controlPoints[(i*4)+1], this.controlPoints[(i*4)+2], this.controlPoints[(i*4)+3]);
                    Vector v2 = this.getCubicBezierPoint(t+stepSize, this.controlPoints[(i*4)], this.controlPoints[(i*4)+1], this.controlPoints[(i*4)+2], this.controlPoints[(i*4)+3]);
                    v = v.add((v2.subtract(v)).cross(Vector.Z).normalized().scale(trackWidth));

                    Vector n = (new Vector(v.x, v.y, 0)).normalized();
                    gl.glNormal3d(n.x, n.y, n.z);
                    gl.glTexCoord2f(0f , 0f);
                    gl.glVertex3d(v.x, v.y, v.z);

                    gl.glNormal3d(n.x, n.y, n.z);
                    gl.glTexCoord2f(0.5f , 0.5f);
                    gl.glVertex3d(v.x, v.y, -1);
                }
            }
            gl.glEnd();
        }
        gl.glDisable(gl.GL_TEXTURE_2D); //disable the 2d textures as we are going to draw non-textured trees
        /** Drawing trees at all tree-points **/
        int sizeFactor = 0;
        for(Vector location : treeLocations) {
            sizeFactor++;
            gl.glPushMatrix();
                // Translate to the correct location
                gl.glTranslated(location.x(), location.y(), location.z());
            
                // Set the materials for the trunk of the tree
                gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, Material.WOOD.diffuse, 0);
                gl.glMaterialfv(GL_FRONT, GL_SPECULAR, Material.WOOD.specular, 0);
                gl.glMaterialf(GL_FRONT, GL_SHININESS, Material.WOOD.shininess);

                // Draw the Tree trunk
                // With random width/height
                double width = 0.1+Math.abs(Math.cos(sizeFactor));
                double height = 0.3+2*Math.abs(Math.cos(sizeFactor));
                glut.glutSolidCylinder(width, height, 15, 15);

                // setting materials for the leafs
                float[] green = {0f, 0.39f, 0f, 1f};
                gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, green, 0);
                gl.glMaterialfv(GL_FRONT, GL_SPECULAR, green, 0);
                
                gl.glPushMatrix();
                    gl.glTranslated(0, 0, height*0.85);
                    
                    // Randomize the leaf shape between cone and balls
                    if(Math.tan(sizeFactor*sizeFactor) > 0) {
                        // Draw a big base cone
                        glut.glutSolidCone(width*2.1, height, 15, 15);
                        
                        // With a smaller cone ontop
                        gl.glPushMatrix();
                            gl.glTranslated(0, 0, height*0.4);
                            glut.glutSolidCone(width*1.9, height, 15, 15);
                        gl.glPopMatrix();
                        
                        // And an even smalled ontop of that
                        gl.glPushMatrix();
                            gl.glTranslated(0, 0, height*0.8);
                            glut.glutSolidCone(width*1.6, height, 15, 15);
                        gl.glPopMatrix();
                    } else {
                        // draw 4 spheres at the base with one ontop to represent leaves.
                        gl.glPushMatrix();
                            gl.glTranslated(width/2, width/2, 0);
                            glut.glutSolidSphere(width*0.9, 15, 15);
                        gl.glPopMatrix();
                        
                        gl.glPushMatrix();
                            gl.glTranslated(width/2, width/-2, 0);
                            glut.glutSolidSphere(width*0.9, 15, 15);
                        gl.glPopMatrix();
                        
                        gl.glPushMatrix();
                            gl.glTranslated(width/-2, width/-2, 0);
                            glut.glutSolidSphere(width*0.9, 15, 15);
                        gl.glPopMatrix();
                        
                        gl.glPushMatrix();
                            gl.glTranslated(width/-2, width/2, 0);
                            glut.glutSolidSphere(width*0.9, 15, 15);
                        gl.glPopMatrix();
                        
                        gl.glPushMatrix();
                            gl.glTranslated(0, width/2, width/2);
                            glut.glutSolidSphere(width*0.9, 15, 15);
                        gl.glPopMatrix();
                        
                        gl.glPushMatrix();
                            gl.glTranslated(0, width/-2, width/2);
                            glut.glutSolidSphere(width*0.9, 15, 15);
                        gl.glPopMatrix();
                        
                        gl.glPushMatrix();
                            gl.glTranslated(width/-2, 0, width/2);
                            glut.glutSolidSphere(width*0.9, 15, 15);
                        gl.glPopMatrix();
                        
                        gl.glPushMatrix();
                            gl.glTranslated(width/2, 0, width/2);
                            glut.glutSolidSphere(width*0.9, 15, 15);
                        gl.glPopMatrix();
                    }
                gl.glPopMatrix();
            
            gl.glPopMatrix();
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
            double[] segmentPoint = this.getSegmentPoint(t);
            int segment = (int)segmentPoint[0];
            
            Vector v = this.getCubicBezierPoint(segmentPoint[1], this.controlPoints[(segment*4)], this.controlPoints[(segment*4)+1], this.controlPoints[(segment*4)+2], this.controlPoints[(segment*4)+3]);
            Vector v2 = this.getCubicBezierPoint(segmentPoint[1]+0.0001, this.controlPoints[(segment*4)], this.controlPoints[(segment*4)+1], this.controlPoints[(segment*4)+2], this.controlPoints[(segment*4)+3]);

            Vector u = (v2.subtract(v)).cross(Vector.Z).normalized().scale((laneWidth*lane)+laneWidth/2);           
            return v.add(u);
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
            double[] segmentPoint = this.getSegmentPoint(t);
            int segment = (int)segmentPoint[0];
            return this.getCubicBezierTangent(segmentPoint[1], this.controlPoints[(segment*4)], this.controlPoints[(segment*4)+1], this.controlPoints[(segment*4)+2], this.controlPoints[(segment*4)+3]);
        } 
    }
    
    /**
     * Finds what controlpoint segment and position the current t is in.
     * @param t The overall progress over the track 0 <= t <=1
     * @return Returns an double array. 0=segment, 1=position on that segment with 0<=position<=1
     */
    private double[] getSegmentPoint(double t) {
            // Create a var to keep track of what segment it is.
            double segment = -1;
        
            // If there are only 4 control points we don't need to find out what segment we are in.
            if(controlPoints.length == 4)
                segment = 0;
            else {
                // Find the total distance traveled
                double lengthTraveled = t*trackLength;
                
                // Loop through all segments and find what segment the point is in.
                // And how far into the segment it is.
                for(int i = 0; i < lengthDistribution.length; i++) {
                    if(lengthTraveled <= lengthDistribution[i]) {
                        segment = i;
                        t = lengthTraveled/lengthDistribution[i];
                        break;
                    } else {
                        lengthTraveled -= lengthDistribution[i];
                    }
                }
                if(segment >= lengthDistribution.length || segment == -1) {
                    segment = lengthDistribution.length-1;
                }
            }
            
        return new double[]{segment,t};
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
        // B(t) = (1-t)^3P0 + 3t(1-t)^2P1 + 3(1-t)t^2P2 + (t^3)P3 with t in [0,1]
        double calcX, calcY, calcZ;
        
        // Using the formula above to calculate X.
        calcX =     Math.pow(1-t,3)*P0.x()+
                    3*t*Math.pow(1-t,2)*P1.x() +
                    3*(1-t)*Math.pow(t,2)*P2.x() +
                    Math.pow(t,3)*P3.x();
        
        // Using the formula above to calculate Y.
        calcY =     Math.pow(1-t,3)*P0.y() +
                    3*t*Math.pow(1-t,2)*P1.y() +
                    3*(1-t)*Math.pow(t,2)*P2.y() +
                    Math.pow(t,3)*P3.y();
        
        // Using the formula above to calculate Z.
        calcZ =     Math.pow(1-t,3)*P0.z() +
                    3*t*Math.pow(1-t,2)*P1.z() +
                    3*(1-t)*Math.pow(t,2)*P2.z() +
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
        // D(t) = P0 ((6-3 t) t-3)+P1 (t (9 t-12)+3)+t (t (3 P3-9 P2)+6 P2)
        double calcX, calcY, calcZ;
        
        // Using the formula above to calculate X.
        calcX =     P0.x() * ((6-3*t) * t-3) +
                    P1.x() * (t*(9*t-12)+3) +
                    t * (t * (3*P3.x()-9*P2.x())+6*P2.x());
        
        // Using the formula above to calculate Y.
        calcY =     P0.y() * ((6-3*t) * t-3) +
                    P1.y() * (t*(9*t-12)+3) +
                    t * (t * (3*P3.y()-9*P2.y())+6*P2.y());
        
        // Using the formula above to calculate Z.
        calcZ =     P0.z() * ((6-3*t) * t-3) +
                    P1.z() * (t*(9*t-12)+3) +
                    t * (t * (3*P3.z()-9*P2.z())+6*P2.z());
        
        return new Vector(calcX, calcY, calcZ);
    }
}
