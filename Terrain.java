package robotrace;

import com.jogamp.opengl.util.gl2.GLUT;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;

/**
 * Implementation of the terrain.
 */
class Terrain {
    int maxX = 20;
    int maxY = 20;
    int minY = -20;
    int minX = -20;
    /**
     * Can be used to set up a display list.
     */
    public Terrain() {
        
    }

    /**
     * Draws the terrain.
     */
    public void draw(GL2 gl, GLU glu, GLUT glut) {
        //try mesh
        gl.glBegin(gl.GL_QUAD_STRIP);
        for(int x = minX; x <= maxX; x++) {
            for(int y = minY; y <= maxY; y++) {
                //set normals pointing up
                
                gl.glNormal3d(Vector.Z.x, Vector.Z.y, Vector.Z.z);
                //getting the height to make the vertex
                gl.glVertex3d(x, y, heightAt(x, y));
            }
        }
        gl.glEnd();
    }

    /**
     * Computes the elevation of the terrain at (x, y).
     */
    public float heightAt(float x, float y) {
        return (float) (0.6 * Math.cos(0.3 * x + 0.2 * y) + 0.4 * Math.cos(x - 0.5 * y));
    }
}
