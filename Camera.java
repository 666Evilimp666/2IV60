package robotrace;

import static java.lang.Math.*;

/**
 * Implementation of a camera with a position and orientation. 
 */
class Camera {

    /** The position of the camera. */
    public Vector eye = new Vector(3f, 6f, 5f);

    /** The point to which the camera is looking. */
    public Vector center = Vector.O;

    /** The up vector. */
    public Vector up = Vector.Z;

    /**
     * Updates the camera viewpoint and direction based on the
     * selected camera mode.
     */
    public void update(GlobalState gs, Robot focus) {
        // Change center to given.
        center = gs.cnt;
        
        switch (gs.camMode) {
            // Helicopter mode
            case 1:
                setHelicopterMode(gs, focus);
                break;
                
            // Motor cycle mode    
            case 2:
                setMotorCycleMode(gs, focus);
                break;
                
            // First person mode    
            case 3:
                setFirstPersonMode(gs, focus);
                break;
                
            // Auto mode    
            case 4:
                setAutoMode(gs, focus);
                break;
                
            // Default mode    
            default:
                setDefaultMode(gs);
        }
        
        // When the camera is looking straight down fix the "up" vector.
        if(
                (eye.x-center.x < 0.01 && center.x-eye.x < 0.01) &&
                (eye.y-center.y < 0.01 && center.y-eye.y < 0.01))
            up = Vector.Y;
        else
            up = Vector.Z;
    }

    /**
     * Computes eye, center, and up, based on the camera's default mode.
     */
    private void setDefaultMode(GlobalState gs) {
        // Setting up variables for Eye location
        float calcX, calcY, calcZ;
        
        // Corner for Z axis is given in XY plane to Z
        // Slider have it from Z axis
        double phi = Math.PI/2-gs.phi;
        
        // x = r * cos(theta) * sin(phi)
        calcX = gs.vDist * (float)cos(gs.theta) * (float)sin(phi);
        
        // y = r * sin(theta) * sin(phi)
        calcY = gs.vDist * (float)sin(gs.theta) * (float)sin(phi);
        
        // z = r * cos(phi)
        calcZ = gs.vDist * (float)cos(phi);
        
        // Setting the Eye.
        eye = new Vector(calcX, calcY, calcZ).add(center);
    }

    /**
     * Computes eye, center, and up, based on the helicopter mode.
     * The camera should focus on the robot.
     */
    private void setHelicopterMode(GlobalState gs, Robot focus) {
        double calcX, calcY, calcZ, r;
        
        calcX = focus.position.x;
        calcY = focus.position.y;
        center = focus.position;
        up = Vector.Z;
        calcZ = 20;
        r = Math.hypot(calcX, calcY);
        calcX = r * cos(gs.theta);
        calcY = r * sin(gs.theta);
        eye = new Vector(calcX, calcY, calcZ).add(center);
    }

    /**
     * Computes eye, center, and up, based on the motorcycle mode.
     * The camera should focus on the robot.
     */
    private void setMotorCycleMode(GlobalState gs, Robot focus) {
        // code goes here ...
    }

    /**
     * Computes eye, center, and up, based on the first person mode.
     * The camera should view from the perspective of the robot.
     */
    private void setFirstPersonMode(GlobalState gs, Robot focus) {
        // code goes here ...
    }
    
    /**
     * Computes eye, center, and up, based on the auto mode.
     * The above modes are alternated.
     */
    private void setAutoMode(GlobalState gs, Robot focus) {
        // code goes here ...
    }

    private void robotSwitch(GlobalState gs, float t) {
        t = t/10;
        if( t > 3) {
            
        }
    }
}
