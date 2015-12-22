package robotrace;

import com.jogamp.opengl.math.Quaternion;
import static java.lang.Math.*;
import static javax.media.opengl.GL.*;
import static javax.media.opengl.GL2.*;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_DIFFUSE;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHTING;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_POSITION;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SHININESS;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SPECULAR;

/**
 * Handles all of the RobotRace graphics functionality,
 * which should be extended per the assignment.
 * 
 * OpenGL functionality:
 * - Basic commands are called via the gl object;
 * - Utility commands are called via the glu and
 *   glut objects;
 * 
 * GlobalState:
 * The gs object contains the GlobalState as described
 * in the assignment:
 * - The camera viewpoint angles, phi and theta, are
 *   changed interactively by holding the left mouse
 *   button and dragging;
 * - The camera view width, vWidth, is changed
 *   interactively by holding the right mouse button
 *   and dragging upwards or downwards;
 * - The center point can be moved up and down by
 *   pressing the 'q' and 'z' keys, forwards and
 *   backwards with the 'w' and 's' keys, and
 *   left and right with the 'a' and 'd' keys;
 * - Other settings are changed via the menus
 *   at the top of the screen.
 * 
 * Textures:
 * Place your "track.jpg", "brick.jpg", "head.jpg",
 * and "torso.jpg" files in the same folder as this
 * file. These will then be loaded as the texture
 * objects track, bricks, head, and torso respectively.
 * Be aware, these objects are already defined and
 * cannot be used for other purposes. The texture
 * objects can be used as follows:
 * 
 * gl.glColor3f(1f, 1f, 1f);
 * track.bind(gl);
 * gl.glBegin(GL_QUADS);
 * gl.glTexCoord2d(0, 0);
 * gl.glVertex3d(0, 0, 0);
 * gl.glTexCoord2d(1, 0);
 * gl.glVertex3d(1, 0, 0);
 * gl.glTexCoord2d(1, 1);
 * gl.glVertex3d(1, 1, 0);
 * gl.glTexCoord2d(0, 1);
 * gl.glVertex3d(0, 1, 0);
 * gl.glEnd(); 
 * 
 * Note that it is hard or impossible to texture
 * objects drawn with GLUT. Either define the
 * primitives of the object yourself (as seen
 * above) or add additional textured primitives
 * to the GLUT object.
 */
public class RobotRace extends Base {
    
    /** Array of the four robots. */
    private final Robot[] robots;
    
    /** Instance of the camera. */
    private final Camera camera;
    
    /** Instance of the race track. */
    private final RaceTrack[] raceTracks;
    
    /** Instance of the terrain. */
    private final Terrain terrain;
    
    Robot focus;
    boolean update;
    /**
     * Constructs this robot race by initializing robots,
     * camera, track, and terrain.
     */
    public RobotRace() {
        
        // Create a new array of four robots
        robots = new Robot[4];
        
        // Initialize robot 0
        robots[0] = new Robot(Material.GOLD
            /* add other parameters that characterize this robot */);
        
        // Initialize robot 1
        robots[1] = new Robot(Material.SILVER
            /* add other parameters that characterize this robot */);
        
        // Initialize robot 2
        robots[2] = new Robot(Material.WOOD
            /* add other parameters that characterize this robot */);

        // Initialize robot 3
        robots[3] = new Robot(Material.ORANGE
            /* add other parameters that characterize this robot */);
        
        // Initialize the camera
        camera = new Camera();
        
        // Initialize the race tracks
        raceTracks = new RaceTrack[5];
        
        // Test track
        raceTracks[0] = new RaceTrack();
        
        // O-track
        raceTracks[1] = new RaceTrack(new Vector[] {
            new Vector(5, 0, 0),
            new Vector(10, 5, 0.5),
            new Vector(5, 10, 1),
            new Vector(0, 5, 1.5)
        });
        
        // L-track
        raceTracks[2] = new RaceTrack(new Vector[] { 
            /* add control points */
        });
        
        // C-track
        raceTracks[3] = new RaceTrack(new Vector[] { 
            /* add control points */
        });
        
        // Custom track
        raceTracks[4] = new RaceTrack(new Vector[] { 
           /* add control points */
        });
        
        // Initialize the terrain
        terrain = new Terrain();
        
        //starting robot to focus on
         focus = robots[0]; 
         update = true;
    }
    
    /**
     * Called upon the start of the application.
     * Primarily used to configure OpenGL.
     */
    @Override
    public void initialize() {
        // Enable blending.
        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                
        // Enable depth testing.
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LESS);
		
	    // Normalize normals.
        gl.glEnable(GL_NORMALIZE);
        
        // Enable textures. 
        gl.glEnable(GL_TEXTURE_2D);
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        gl.glBindTexture(GL_TEXTURE_2D, 0);
        
	// Try to load four textures, add more if you like.
        track = loadTexture("track.jpg");
        brick = loadTexture("brick.jpg");
        head = loadTexture("head.jpg");
        torso = loadTexture("torso.jpg");
    }
    
    /**
     * Configures the viewing transform.
     */
    @Override
    public void setView() {
        int time = (int) gs.tAnim;
        if(time%5 == 0 && update) {
           focus = robotSwitch(focus);
           update = false;
        }
        if(time%5 != 0) {
            update = true;
        }
        
        camera.update(gs, focus);
        
        // Select part of window.
        gl.glViewport(0, 0, gs.w, gs.h);
        
        // Set projection matrix.
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        
        // Using ArcTan to calculate the FOV in radiance by taking the length of opposite (gs.vWidth/2) and adjacent (gs.vDist)
        // Multiply by 2 because the triangle was cut in half in order for ArcTan to work.
        double viewingCorner = (atan2(gs.vWidth/2, (double)gs.vDist))*(double)2;
                
        // Converting viewingCorner from radiance to degrees.
        float fov = (float)Math.toDegrees(viewingCorner);
        
        // Set the perspective.
        glu.gluPerspective(fov, (float)gs.w / (float)gs.h, 0.1*gs.vDist, 10.0*gs.vDist);
        
        // Set camera.
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();
        
        // Update the view according to the camera mode and robot of interest.
        // For camera modes 1 to 4, determine which robot to focus on.
        glu.gluLookAt(camera.eye.x(),    camera.eye.y(),    camera.eye.z(),
                      camera.center.x(), camera.center.y(), camera.center.z(),
                      camera.up.x(),     camera.up.y(),     camera.up.z());
    }
    
    /**
     * Draws the entire scene.
     */
    @Override
    public void drawScene() {
        // Background color.
        gl.glClearColor(0.529f, 0.8f, 0.92f, 0f);
        
        // Clear background.
        gl.glClear(GL_COLOR_BUFFER_BIT);
        
        // Clear depth buffer.
        gl.glClear(GL_DEPTH_BUFFER_BIT);
        
        // Set color to black.
        gl.glColor3f(0f, 0f, 0f);
        
        gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        
        /**
         * Light sources
         */
        // Use smooth shading
        gl.glShadeModel(GL_SMOOTH);  
        // Enable lighting
        gl.glEnable(GL_LIGHTING);    
        // Enable light source #0 
        gl.glEnable(GL_LIGHT0);
        
        // Give light source #0 a white color.
        float[] whiteColor = {1.0f, 1.0f, 1.0f, 1.0f, 0f};
        gl.glLightfv(GL_LIGHT0, GL_DIFFUSE, whiteColor, 0); 
        
        // 10 degrees expressed as radians.
        float radRot = 10*(float)Math.PI/180;

        // Vector from eye center to eye
        Vector eyeRepos = camera.eye.subtract(camera.center);

        // Use the cross-product of camera position and upvector to calculate the horizontal rotation axis.
        Vector horizontalAxis = eyeRepos.cross(camera.up).normalized();

        // Use the cross-product of camera position and the horizontal rotation axis to calculate the vertical rotation axis.
        Vector verticalAxis = horizontalAxis.cross(eyeRepos).normalized();

        // Create a quaternion and rotate up
        Quaternion q = new Quaternion();
        q.rotateByAngleNormalAxis(radRot, (float)horizontalAxis.x(), (float)horizontalAxis.y(), (float)horizontalAxis.z());

        // Create a quaternion and rotate left
        Quaternion q2 = new Quaternion();
        q.rotateByAngleNormalAxis(-radRot, (float)verticalAxis.x(), (float)verticalAxis.y(), (float)verticalAxis.z());

        // Combine both rotations into quaternion "q"
        q.mult(q2);

        // Create output float for the quaternion
        float[] vecOut = {0f,0f,0f, 0f};

        // Starting point of the light
        float[] vecIn = {(float)camera.eye.x(), (float)camera.eye.y(), (float)camera.eye.z()};

        // Perform the rotation on the vector
        vecOut = q.rotateVector(vecOut, 0, vecIn, 0);

        // position the light
        gl.glLightfv(GL_LIGHT0, GL_POSITION, vecOut, 0);
        
        // Draw the axis frame.
        if (gs.showAxes)
            drawAxisFrame();
        
        // loop through all robots to draw them.
        for(int i = 0; i < robots.length; i++)
        {
            // The stepsize of the robot
            double incr = 0.0005*Math.random();
            
            // Avoiding looping the robot around the track. (doesn't look real otherwise on the L and C track)
            if(robots[i].progress + incr <= 1)
                robots[i].progress += incr;
            else
                robots[i].progress = 1;
            
            // setup the location and direction of the robot
            robots[i].position = raceTracks[gs.trackNr].getLanePoint(i, robots[i].progress);
            robots[i].direction = raceTracks[gs.trackNr].getLaneTangent(i, robots[i].progress);
            
            // draw the robot
            robots[i].draw(gl, glu, glut, gs.showStick, gs.tAnim);
        }
        
        // Draw the race track.
        raceTracks[gs.trackNr].draw(gl, glu, glut);
        
        // Draw the terrain.
        terrain.draw(gl, glu, glut);
    }
    
    /**
     * Draws the x-axis (red), y-axis (green), z-axis (blue),
     * and origin (yellow).
     */
    public void drawAxisFrame() {
        // Red arrow for X-Axis
        float[] red = {1f, 0f, 0f, 1f};
        gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, red, 0);
        gl.glMaterialfv(GL_FRONT, GL_SPECULAR, red, 0);
        // Cube part
        gl.glPushMatrix();
            gl.glTranslatef(0.5f, 0f, 0f);
            gl.glScalef(0.85f, 0.05f, 0.05f);
            glut.glutSolidCube(1f);
        gl.glPopMatrix();
        // Cone Part
        gl.glPushMatrix();
            gl.glRotatef(90f, 0f, 1f, 0f);
            gl.glTranslatef(0f, 0f, 0.9f);
            glut.glutSolidCone(0.08f, 0.1f, 100, 100);
        gl.glPopMatrix();
        
        
        // Green arrow for Y-axis
        float[] green = {0f, 1f, 0f, 1f};
        gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, green, 0);
        gl.glMaterialfv(GL_FRONT, GL_SPECULAR, green, 0);
        // Cube part
        gl.glPushMatrix();
            gl.glTranslatef(0f, 0.5f, 0f);
            gl.glScalef(0.08f, 0.85f, 0.05f);
            glut.glutSolidCube(1f);
        gl.glPopMatrix();
        // Cone Part
        gl.glPushMatrix();
            gl.glRotatef(90f, -1f, 0f, 0f);
            gl.glTranslatef(0f, 0f, 0.9f);
            glut.glutSolidCone(0.08f, 0.1f, 100, 100);
        gl.glPopMatrix();
        
        // Blue arrow for Z-axis
        float[] blue = {0f, 0f, 1f, 1f};
        gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, blue, 0);
        gl.glMaterialfv(GL_FRONT, GL_SPECULAR, blue, 0);
        // Cube part
        gl.glPushMatrix();
            gl.glTranslatef(0f, 0f, 0.5f);
            gl.glScalef(0.05f, 0.05f, 0.85f);
            glut.glutSolidCube(1f);
        gl.glPopMatrix();
        // Cone Part
        gl.glPushMatrix();
            gl.glTranslatef(0f, 0f, 0.9f);
            glut.glutSolidCone(0.05f, 0.1f, 100, 100);
        gl.glPopMatrix();
        
        // Yellow Sphere at origin
        float[] yellow = {1f, 1f, 0f, 1f};
        gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, yellow, 0);
        gl.glMaterialfv(GL_FRONT, GL_SPECULAR, yellow, 0);
        glut.glutSolidSphere(0.1f, 100, 100);
    }
 
    /**
     * Picks a new robot to focus on, depending on the robots positions and last focus
     * @param current the current robot being focused on
     * @return  the new robot to be focused on
     */
    private Robot robotSwitch(Robot current) {
        Robot result;
        if(robots[0] != current && robots[0].progress >= robots[1].progress && robots[0].progress >= robots[2].progress && 
                robots[0].progress >= robots[3].progress) {
            result = robots[0];
        }
        else if(robots[1] != current && robots[1].progress >= robots[2].progress &&  robots[1].progress >= robots[3].progress) {
            result = robots[1];
        }
        else if(robots[2] != current && robots[2].progress >= robots[3].progress) {
            result = robots[2];
        }
        else {
            result = robots[3];
        }
        return result;
    }
    /**
     * Main program execution body, delegates to an instance of
     * the RobotRace implementation.
     */
    public static void main(String args[]) {
        RobotRace robotRace = new RobotRace();
        robotRace.run();
    } 
}
