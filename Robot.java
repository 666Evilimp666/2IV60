package robotrace;

import com.jogamp.opengl.util.gl2.GLUT;
import static javax.media.opengl.GL.GL_FRONT;
import static javax.media.opengl.GL.GL_LINES;
import javax.media.opengl.GL2;
import javax.media.opengl.fixedfunc.GLLightingFunc;
import static javax.media.opengl.fixedfunc.GLLightingFunc.*;
import javax.media.opengl.glu.GLU;

/**
* Represents a Robot, to be implemented according to the Assignments.
*/
class Robot {
    
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
    private double headDepth = 0.1;
    
    // LegHeight (Default Z axis)
    private double legHeight = 1;
    // LegWidth (Default X axis)
    private double legWidth = 0.2;
    // LegDepth (Default Y axis)
    private double legDepth = 0.2;
    
    // ArmHeight (Default Z axis)
    private double armHeight = 0.8;
    // ArmWidth (Default X axis)
    private double armWidth = 0.2;
    // ArmDepth (default Y axis)
    private double armDepth = 0.2;
    
    // TorsoHeight (Default Z axis)
    private double torsoHeight = 1.3;
    // TorsoWidth (Default x axis)
    private double torsoWidth = 0.6;
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
    }
    
    /**
     * Function for drawing the head.
     * @param stickFigure Whether a stick figure should be drawn instead of an real robot.
     * @param tAnim Time since the animation started.
     */
    private void drawHead(boolean stickFigure, float tAnim) {
        gl.glPushMatrix();
        gl.glTranslated(this.position.x(), this.position.y(), this.position.z());
        // Done in 2 translations incase we want headanimations lateron
        gl.glTranslated(this.headOffset.x(), this.headOffset.y(), this.headOffset.z());
        if(stickFigure) {
            gl.glPushMatrix();
                // Translate head to correct position.
                gl.glTranslated(0, 0, headHeight);
                // Scale head according to given dimensions.
                gl.glScaled(headWidth, headDepth, headHeight);
                glut.glutWireSphere(1, 10, 10);
            gl.glPopMatrix();
        } else {
            // actual drawing
            gl.glPushMatrix();
                // Translate head to correct position.
                gl.glTranslated(0, 0, headHeight);
                // Scale head according to given dimensions.
                gl.glScaled(headWidth, headDepth, headHeight);
                glut.glutSolidSphere(1, 8, 8);
            gl.glPopMatrix();
        }
        gl.glPopMatrix();
    }
    
    /**
     * Function for drawing the torso.
     * @param stickFigure   Whether a stick figure should be drawn instead of an real robot.
     */
    private void drawTorso(boolean stickFigure) {
        gl.glPushMatrix();
        gl.glTranslated(this.position.x(), this.position.y(), this.position.z());
        
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
            gl.glPushMatrix();
                // Translate torso to correct position.
                gl.glTranslated(0, 0, headOffset.z()-(torsoHeight/2));
                // Scale torso according to given dimensions.
                gl.glScaled(torsoWidth, torsoDepth, torsoHeight);
                glut.glutSolidCube(1);
            gl.glPopMatrix();
        }
        gl.glPopMatrix();
    }
    
    
    /**
     * Function for drawing an arm.
     * @param orientation   Indicator for which arm we are drawing.
     * @param stickFigure   Whether a stick figure should be drawn instead of an real robot.
     * @param tAnim         Time since the animation started.
     */
    private void drawArm(Robot.LimbOrientation orientation, boolean stickFigure, float tAnim) {
        gl.glPushMatrix();
        
        gl.glTranslated(this.position.x(), this.position.y(), this.position.z());
        
        // Mirror the arm so we only need code for drawing one arm.
        if(orientation == Robot.LimbOrientation.LEFT)
            gl.glScaled(-1, 1, 1);
        
        gl.glTranslated(this.armOffset.x(), this.armOffset.y(), this.armOffset.z());
        
        
        if(stickFigure) {
            // Drawing an stick representation of the arm.
            
            // Sphere at the attachmentpoint.
            glut.glutSolidSphere(0.1, 5, 5);
            
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
        
        gl.glTranslated(this.position.x(), this.position.y(), this.position.z());
        
        // Mirror the arm so we only need code for drawing one arm.
        if(orientation == Robot.LimbOrientation.LEFT)
            gl.glScaled(-1, 1, 1);
        
        gl.glTranslated(this.legOffset.x(), this.legOffset.y(), this.legOffset.z());
                
        if(stickFigure) {
            gl.glPushMatrix();
                gl.glTranslated(0,0,torsoLegOverlap/2);
            
                // Sphere at the attachmentpoint.
                glut.glutSolidSphere(0.1, 5, 5);

                // Line representing the arm.
                gl.glBegin(GL_LINES);
                gl.glVertex3d(0f, 0f, 0f);
                gl.glVertex3d(0f, 0f, this.legHeight*-1);
                gl.glEnd();
            gl.glPopMatrix();
        } else {
            // Drawing the actual robot
            gl.glPushMatrix();
                // Translate leg to correct position.
                gl.glTranslated(legWidth/2, 0, (legHeight/-2)+torsoLegOverlap);
                // Scale leg according to given dimensions.
                gl.glScaled(legWidth, legDepth, legHeight);
                glut.glutSolidCube(1);
            gl.glPopMatrix();
        }
        gl.glPopMatrix();
    }
}
