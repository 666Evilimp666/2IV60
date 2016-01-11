package robotrace;

import com.jogamp.opengl.util.gl2.GLUT;
import static javax.media.opengl.GL.GL_FRONT;
import static javax.media.opengl.GL.GL_LINES;
import javax.media.opengl.GL2;
import static javax.media.opengl.fixedfunc.GLLightingFunc.*;
import javax.media.opengl.glu.GLU;

/**
* Represents a Robot, to be implemented according to the Assignments.
*/
class Robot {
    // Progress along the track
    public float progress = 0f;
    
    /** The position of the robot. */
    public Vector position = new Vector(1, 1, 0);
    
    /** The direction in which the robot is running. */
    public Vector direction = new Vector(1, 0, 0);

    /** The material from which this robot is built. */
    private final Material material;
    
    // GL, GLU, GLUT is stored to avoid juggling it between all drawing functions.
    // since no asynchronous functions are running in this class it gives no issues.
    private GL2 gl;
    private GLU glu;
    private GLUT glut;
    
    // The animation speed of this unit. (base 1.0) with a max differeence of 10%.
    private double animationSpeed = (Math.random()*0.2)+0.9;
    
    /**
     * Variable to see the difference between right/left arms and legs.
     */
    private enum LimbOrientation {
        RIGHT,
        LEFT
    }
    
    /**
     * Parameters to more easily change the dimensions of robot parts later.
     */
    // HeadHeight (Default Z axis)
    private double headHeight = 0.3;
    // HeadWidth (Default X axis)
    private double headWidth = 0.2;
    // HeadDepth (Default Y axis)
    private double headDepth = 0.2;
    
    // LegHeight (Default Z axis)
    private double legHeight = 1;
    // LegWidth (Default X axis)
    private double legWidth = 0.1;
    // LegDepth (Default Y axis)
    private double legDepth = 0.2;
    
    // ArmHeight (Default Z axis)
    private double armHeight = 0.8;
    // ArmWidth (Default X axis)
    private double armWidth = 0.1;
    // ArmDepth (default Y axis)
    private double armDepth = 0.2;
    
    // TorsoHeight (Default Z axis)
    private double torsoHeight = 1.3;
    // TorsoWidth (Default x axis)
    private double torsoWidth = 0.3;
    // TorsoDepth (Default Y axis)
    private double torsoDepth = 0.5;
    
    // How heigh the legs are compared to torso.
    private double torsoLegOverlap = 0.2;
    
    // How much height between top of torso and arm attachement.
    private double torsoArmDifference = 0.1;
    
    /**
     * Parameters for the offset of body parts.
     */
    // Head location
    private Vector headOffset = new Vector(0, 0, legHeight+torsoHeight-torsoLegOverlap);
    // Arm location
    private Vector armOffset = new Vector(torsoWidth/2, 0, legHeight+torsoHeight-torsoArmDifference-torsoLegOverlap);
    // Leg location
    private Vector legOffset = new Vector(torsoWidth/2, 0, legHeight-torsoLegOverlap);
    
    /**
     * Constructs the robot with initial parameters.
     */
    public Robot(Material material
        /* add other parameters that characterize this robot */) {
        this.material = material;

        // code goes here ...
    }

    /**
     * Draws this robot (as a {@code stickfigure} if specified).
     * @param gl    The gl object
     * @param glu   The glu object
     * @param glut  The glut object
     * @param stickFigure   Whether a stick figure should be drawn instead of an real robot.
     * @param tAnim Time since the start of the animation
     */
    public void draw(GL2 gl, GLU glu, GLUT glut, boolean stickFigure, float tAnim) {
        this.gl = gl;
        this.glu = glu;
        this.glut = glut;
        
        // Use the materials given to the robot to draw parts.
        gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, this.material.diffuse, 0);
        gl.glMaterialfv(GL_FRONT, GL_SPECULAR, this.material.specular, 0);
        gl.glMaterialf(GL_FRONT, GL_SHININESS, this.material.shininess);
        
        
        gl.glPushMatrix();
        
        // Translate the robot to the correct coordinates.
        gl.glTranslated(this.position.x(), this.position.y(), this.position.z());
        
        // cos alpha = (A.B)/(|A|*|B|)
        // A = Vector.Y, B=this.direction.normalized()
        // since A and B in this case are both unit length we dont need to calculate the length and divide since it will be 1.
        Vector dir = this.direction.normalized();
        double dotProduct = Vector.Y.dot(dir);
        double acos = Math.acos(dotProduct);
        
        // acos returns an angle in radians and we need degrees for glRotate.
        double angle = Math.toDegrees(acos);
        
        // If the crossproduct is negative then we have a rotation the other way around.
        // and thus need to flip the angle to the negative side.
        Vector crossProduct = Vector.Y.cross(dir);
        if(crossProduct.z() < 0)
            angle = angle*-1;
        
        // Apply the rotation
        gl.glRotated(angle, 0f, 0f, 1f);
        
        
        // Draw torso
        drawTorso(stickFigure);
        
        // Draw head
        drawHead(stickFigure, tAnim);
        
        // Draw left arm
        drawArm(Robot.LimbOrientation.LEFT, stickFigure, tAnim);
        
        // Draw right arm
        drawArm(Robot.LimbOrientation.RIGHT, stickFigure,  tAnim);
        
        // Draw left leg
        drawLeg(Robot.LimbOrientation.LEFT, stickFigure,  tAnim);
        
        // Draw right leg
        drawLeg(Robot.LimbOrientation.RIGHT, stickFigure,  tAnim);
                
        gl.glPopMatrix();
    }
    
    /**
     * Function for drawing the head.
     * @param stickFigure Whether a stick figure should be drawn instead of an real robot.
     * @param tAnim Time since the animation started.
     */
    private void drawHead(boolean stickFigure, float tAnim) {
        gl.glPushMatrix();
        // Done in 2 translations incase we want headanimations lateron
        gl.glTranslated(this.headOffset.x(), this.headOffset.y(), this.headOffset.z());
        if(stickFigure) {
            gl.glPushMatrix();
                // Translate head to correct position.
                gl.glTranslated(0, 0, headHeight);
                // Scale head according to given dimensions.
                gl.glScaled(headWidth, headDepth, headHeight);
                glut.glutWireCube(2f);
            gl.glPopMatrix();
        } else {
            //enable 2D textures and set the parameters
            gl.glEnable(gl.GL_TEXTURE_2D);
            gl.glTexEnvi(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_REPLACE);
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP);
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP);
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_LINEAR);
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_LINEAR);
            int m = getMaterial(); //an integer representing the material of the robot
            Base.head.bind(gl);
            // actual drawing
            gl.glPushMatrix();
                // Translate head to correct position.
                gl.glTranslated(0, 0, headHeight);
                // Scale head according to given dimensions.
                gl.glScaled(headWidth, headDepth, headHeight);
                
                // Since everything is scaled and translated we can assume the head is a cube around the origin.
                
                // top
                gl.glBegin(gl.GL_QUADS);
                    gl.glNormal3d(Vector.Z.x(), Vector.Z.y(), Vector.Z.z());
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 1);
                    gl.glVertex3d(1, 1, 1);
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 1);
                    gl.glVertex3d(-1, 1, 1);
                    gl.glTexCoord2f(0.25f * m, 0);
                    gl.glVertex3d(-1, -1, 1);
                    gl.glTexCoord2f(0.25f * m, 1);
                    gl.glVertex3d(1, -1, 1);
                gl.glEnd();
                
                // Right
                gl.glBegin(gl.GL_QUADS);
                    gl.glNormal3d(Vector.X.x(), Vector.X.y(), Vector.X.z());
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 1);
                    gl.glVertex3d(1, 1, 1);
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 0);
                    gl.glVertex3d(1, 1, -1);
                    gl.glTexCoord2f(0.25f * m, 0);
                    gl.glVertex3d(1, -1, -1);
                    gl.glTexCoord2f(0.25f * m, 1);
                    gl.glVertex3d(1, -1, 1);
                gl.glEnd();
                
                // Left
                gl.glBegin(gl.GL_QUADS);
                    gl.glNormal3d(Vector.X.x()*-1, Vector.X.y()*-1, Vector.X.z()*-1);
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 1);
                    gl.glVertex3d(-1, 1, 1);
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 0);
                    gl.glVertex3d(-1, 1, -1);
                    gl.glTexCoord2f(0.25f * m, 0);
                    gl.glVertex3d(-1, -1, -1);
                    gl.glTexCoord2f(0.25f * m, 1);
                    gl.glVertex3d(-1, -1, 1);
                gl.glEnd();
                
                // front
                gl.glBegin(gl.GL_QUADS);
                    gl.glNormal3d(Vector.Y.x(), Vector.Y.y(), Vector.Y.z());
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 1);
                    gl.glVertex3d(1, 1, 1);
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 0);
                    gl.glVertex3d(1, 1, -1);
                    gl.glTexCoord2f(0.25f * m, 0);
                    gl.glVertex3d(-1, 1, -1);
                    gl.glTexCoord2f(0.25f * m, 1);
                    gl.glVertex3d(-1, 1, 1);
                gl.glEnd();
                
                // Back
                gl.glBegin(gl.GL_QUADS);
                    gl.glNormal3d(Vector.Y.x()*-1, Vector.Y.y()*-1, Vector.Y.z()*-1);
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 1);
                    gl.glVertex3d(1, -1, 1);
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 0);
                    gl.glVertex3d(1, -1, -1);
                    gl.glTexCoord2f(0.25f * m, 0);
                    gl.glVertex3d(-1, -1, -1);
                    gl.glTexCoord2f(0.25f * m, 1);
                    gl.glVertex3d(-1, -1, 1);
                gl.glEnd();
            gl.glPopMatrix();
        }
        gl.glPopMatrix();
        gl.glDisable(gl.GL_TEXTURE_2D);
    }
    
    /**
     * Function for drawing the torso.
     * @param stickFigure   Whether a stick figure should be drawn instead of an real robot.
     */
    private void drawTorso(boolean stickFigure) {
        gl.glPushMatrix();
        if(stickFigure){
            // Some straight line representing torso height.
            gl.glColor3d(0, 0, 0);
            gl.glBegin(GL_LINES);
            gl.glVertex3d(0f, 0f, legHeight+torsoHeight-torsoLegOverlap);
            gl.glVertex3d(0f, 0f, legHeight-torsoLegOverlap);
            gl.glEnd();
            
            // Horizontal line representing where the arms will be attached too.
            gl.glBegin(GL_LINES);
            gl.glVertex3d(this.armOffset.x(), 0f, this.armOffset.z());
            gl.glVertex3d(this.armOffset.x()*-1, 0f, this.armOffset.z());
            gl.glEnd();
            
            // Horizontal line representing where the legs will be attached too.
            gl.glBegin(GL_LINES);
            gl.glVertex3d(this.legOffset.x(), 0f, this.legOffset.z()+(torsoLegOverlap/2));
            gl.glVertex3d(this.legOffset.x()*-1, 0f, this.legOffset.z()+(torsoLegOverlap/2));
            gl.glEnd();
        } else {
            gl.glEnable(gl.GL_TEXTURE_2D);
            gl.glTexEnvi(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_REPLACE);
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP);
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP);
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_LINEAR);
            gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_LINEAR);
            int m = getMaterial(); //an integer representing the material of the robot
            Base.torso.bind(gl);
            gl.glPushMatrix();
                // Translate torso to correct position.
                gl.glTranslated(0, 0, headOffset.z()-(torsoHeight/2));
                // Scale torso according to given dimensions.
                gl.glScaled(torsoWidth, torsoDepth, torsoHeight);
                
                // top
                gl.glBegin(gl.GL_QUADS);
                    gl.glNormal3d(Vector.Z.x(), Vector.Z.y(), Vector.Z.z());
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 1);
                    gl.glVertex3d(0.5, 0.5, 0.5);
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 0);
                    gl.glVertex3d(-0.5, 0.5, 0.5);
                    gl.glTexCoord2f(0.25f * m, 0);
                    gl.glVertex3d(-0.5, -0.5, 0.5);
                    gl.glTexCoord2f(0.25f * m, 1);
                    gl.glVertex3d(0.5, -0.5, 0.5);
                gl.glEnd();
                
                // Right
                gl.glBegin(gl.GL_QUADS);
                    gl.glNormal3d(Vector.X.x(), Vector.X.y(), Vector.X.z());
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 1);
                    gl.glVertex3d(0.5, 0.5, 0.5);
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 0);
                    gl.glVertex3d(0.5, 0.5, -0.5);
                    gl.glTexCoord2f(0.25f * m, 0);
                    gl.glVertex3d(0.5, -0.5, -0.5);
                    gl.glTexCoord2f(0.25f * m, 1);
                    gl.glVertex3d(0.5, -0.5, 0.5);
                gl.glEnd();
                
                // Left
                gl.glBegin(gl.GL_QUADS);
                    gl.glNormal3d(Vector.X.x()*-1, Vector.X.y()*-1, Vector.X.z()*-1);
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 1);
                    gl.glVertex3d(-0.5, 0.5, 0.5);
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 0);
                    gl.glVertex3d(-0.5, 0.5, -0.5);
                    gl.glTexCoord2f(0.25f * m, 0);
                    gl.glVertex3d(-0.5, -0.5, -0.5);
                    gl.glTexCoord2f(0.25f * m, 1);
                    gl.glVertex3d(-0.5, -0.5, 0.5);
                gl.glEnd();
                
                // front
                gl.glBegin(gl.GL_QUADS);
                    gl.glNormal3d(Vector.Y.x(), Vector.Y.y(), Vector.Y.z());
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 1);
                    gl.glVertex3d(0.5, 0.5, 0.5);
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 0);
                    gl.glVertex3d(0.5, 0.5, -0.5);
                    gl.glTexCoord2f(0.25f * m, 0);
                    gl.glVertex3d(-0.5, 0.5, -0.5);
                    gl.glTexCoord2f(0.25f * m, 1);
                    gl.glVertex3d(-0.5, 0.5, 0.5);
                gl.glEnd();
                
                // Back
                gl.glBegin(gl.GL_QUADS);
                    gl.glNormal3d(Vector.Y.x()*-1, Vector.Y.y()*-1, Vector.Y.z()*-1);
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 1);
                    gl.glVertex3d(0.5, -0.5, 0.5);
                    gl.glTexCoord2f((0.26f * m) - 0.25f, 0);
                    gl.glVertex3d(0.5, -0.5, -0.5);
                    gl.glTexCoord2f(0.25f * m, 0);
                    gl.glVertex3d(-0.5, -0.5, -0.5);
                    gl.glTexCoord2f(0.25f * m, 1);
                    gl.glVertex3d(-0.5, -0.5, 0.5);
                gl.glEnd();
            gl.glPopMatrix();
        }
        gl.glPopMatrix();
        gl.glDisable(gl.GL_TEXTURE_2D);
    }
    
    
    /**
     * Function for drawing an arm.
     * @param orientation   Indicator for which arm we are drawing.
     * @param stickFigure   Whether a stick figure should be drawn instead of an real robot.
     * @param tAnim         Time since the animation started.
     */
    private void drawArm(Robot.LimbOrientation orientation, boolean stickFigure, float tAnim) {
        gl.glPushMatrix();
        
        // Mirror the arm so we only need code for drawing one arm.
        if(orientation == Robot.LimbOrientation.LEFT)
            gl.glScaled(-1, 1, 1);
        
        // Translate the arm to the correct position
        gl.glTranslated(this.armOffset.x(), this.armOffset.y(), this.armOffset.z());
        
        // Rotate the arm over the course of the animation over 45 degrees, shifted 10 degrees.
        if(orientation == Robot.LimbOrientation.LEFT)
            gl.glRotated((Math.abs(Math.cos(tAnim*2*this.animationSpeed))*45)-10, 1, 0, 0);
        else
            gl.glRotated((Math.abs(Math.cos(tAnim*2*this.animationSpeed+(Math.PI/2)))*45)-10, 1, 0, 0);
        
        if(stickFigure) {
            // Drawing an stick representation of the arm.
            
            // Sphere at the attachmentpoint.
            glut.glutSolidSphere(0.1, 15, 15);
            
            // Line representing the arm.
            gl.glBegin(GL_LINES);
            gl.glVertex3d(0f, 0f, 0f);
            gl.glVertex3d(0f, 0f, this.armHeight*-1);
            gl.glEnd();
        } else {
            // Drawing the actual robot
            gl.glPushMatrix();
                // Translate arm to correct position.
                gl.glTranslated(armWidth/2, 0, armHeight/-2);
                // Scale arm according to given dimensions.
                gl.glScaled(armWidth, armDepth, armHeight);
                glut.glutSolidCube(1);
            gl.glPopMatrix();
        }
        gl.glPopMatrix();
    }
    
    
    /**
     * Function for drawing an leg.
     * @param orientation   Indicator for which leg we are drawing.
     * @param stickFigure   Whether a stick figure should be drawn instead of an real robot.
     * @param tAnim         Time since the animation started.
     */
    private void drawLeg(Robot.LimbOrientation orientation, boolean stickFigure, float tAnim) {
        gl.glPushMatrix();
        
        // Mirror the arm so we only need code for drawing one arm.
        if(orientation == Robot.LimbOrientation.LEFT)
            gl.glScaled(-1, 1, 1);
        
        gl.glTranslated(this.legOffset.x(), this.legOffset.y(), this.legOffset.z());
                
        // Rotate the arm over the course of the animation over 50 degrees, shifted 25 degrees.
        if(orientation == Robot.LimbOrientation.LEFT)
            gl.glRotated((Math.abs(Math.cos(tAnim*2*this.animationSpeed+(Math.PI/2)))*50)-25, 1, 0, 0);
        else
            gl.glRotated((Math.abs(Math.cos(tAnim*2*this.animationSpeed))*50)-25, 1, 0, 0);
        
        if(stickFigure) {
            gl.glPushMatrix();
                gl.glTranslated(0,0,torsoLegOverlap/2);
            
                // Sphere at the attachmentpoint.
                glut.glutSolidSphere(0.1, 15, 15);

                // Line representing the arm.
                gl.glBegin(GL_LINES);
                gl.glVertex3d(0f, 0f, 0f);
                gl.glVertex3d(0f, 0f, this.legHeight*-1);
                gl.glEnd();
            gl.glPopMatrix();
        } else {
            // Drawing a rotated cylinder to make leg-attachment to body seem less weird
            gl.glPushMatrix();
                // rotate the cylinder
                gl.glRotated(90, 0, 1, 0);
                glut.glutSolidCylinder(torsoLegOverlap*0.9, legWidth*1.1, 15, 15);
                glut.glutSolidCylinder(torsoLegOverlap*0.2, legWidth*1.5, 5, 5);
            gl.glPopMatrix();
            
            // Drawing the leg
            gl.glPushMatrix();
                // Translate leg to correct position.
                gl.glTranslated(legWidth/2, 0, (legHeight/-2)+torsoLegOverlap);
                // Scale leg according to given dimensions.
                gl.glScaled(legWidth, legDepth, legHeight-0.15);
                glut.glutSolidCube(1);
            gl.glPopMatrix();
        }
        gl.glPopMatrix();
    }
    
    int getMaterial() {
        switch (this.material) {
            case GOLD:
                return 1;
            case SILVER:
                return 2;
            case WOOD:
                return 3;
            default:
                return 4;
        }
    }
}
