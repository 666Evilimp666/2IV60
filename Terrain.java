package robotrace;

import com.jogamp.opengl.util.gl2.GLUT;
import com.jogamp.opengl.util.texture.Texture;
import javax.media.opengl.GL2;
import javax.media.opengl.glu.GLU;
import java.awt.Color;

/**
 * Implementation of the terrain.
 */
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import static javax.media.opengl.GL.GL_FRONT;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_DIFFUSE;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_SPECULAR;
class Terrain {
    // Maximum X of the terrain
    int maxX = 20;
    
    // Maximum Y of the terrain
    int maxY = 20;
    
    // Minimum Y of the terrain
    int minY = -20;
    
    // Minimum X of the terrain
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
        //color array for the 1d texture
        Color[] colors = {Color.BLUE, Color.YELLOW, Color.GREEN};
        //bind the int returned to the 1d texture
        gl.glBindTexture(gl.GL_TEXTURE_1D, create1DTexture(gl, colors));
        //enable and set required parameters
        gl.glEnable(gl.GL_TEXTURE_1D);
        gl.glTexEnvi(gl.GL_TEXTURE_ENV, gl.GL_TEXTURE_ENV_MODE, gl.GL_REPLACE);
        //here we start to draw the terrain
        for(int x = minX; x <= maxX; x++) {
            gl.glBegin(gl.GL_TRIANGLE_STRIP);
            for(float y = minY; y <= maxY; y+=0.5) {
                //set normals pointing up
                gl.glNormal3d(Vector.Z.x, Vector.Z.y, Vector.Z.z);
                //set texture coordinats and get the verteces
                float z = heightAt(x, y);
                
                // Temp var for the texture coordinate
                float t = 0;
                
                // Set the value based on Z value.
                if(z < 0) {
                    t = 0.2f;
                }
                else if( z > 0.5) {
                    t = 0.8f;
                }
                else {
                    t = 0.5f;
                }
                
                // Use the texture and draw the vertex
                gl.glTexCoord1f(t);
                gl.glVertex3d(x, y, z);
                
                // If we are not at the max X yet calculate the triangle in that direction
                if(x != maxX) {
                    // Calculate the height at the next X point
                    z = heightAt((float)(x + 1), y);
                    
                    // And again we set the texture coordinate based on the Z
                    t = 0;
                    if(z < 0) {
                        t = 0.2f;
                    }
                    else if( z > 0.5) {
                        t = 0.8f;
                    }
                    else {
                        t = 0.5f;
                    }
                    
                    // And use that to draw the next vertex
                    gl.glTexCoord1f(t);
                    gl.glVertex3d(x + 1, y, z);
                }
            }
            gl.glEnd();
        }
        //disable 1D textures after we have drawn them
        gl.glDisable(gl.GL_TEXTURE_1D);
        
        // Set the color for the Z=0 grey transparant polygon
        // that is used to simulate water level.
        float[] grey = {0.5f, 0.5f, 0.5f, 0.3f};
        gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, grey, 0);
        gl.glMaterialfv(GL_FRONT, GL_SPECULAR, grey, 0);
        gl.glBegin(GL2.GL_TRIANGLE_STRIP);
        
        // Draw one giant quad with a normal facing up.
        gl.glNormal3d(Vector.Z.x(), Vector.Z.y(), Vector.Z.z());
        gl.glVertex3d(maxX,minY,0);
        gl.glVertex3d(minX,minY,0);
        gl.glVertex3d(maxX,maxY,0);
        gl.glVertex3d(minX,maxY,0);
        
        gl.glEnd();
    }

    /**
     * Computes the elevation of the terrain at (x, y).
     */
    public float heightAt(float x, float y) {
        return (float) (0.6 * Math.cos(0.3 * x + 0.2 * y) + 0.4 * Math.cos(x - 0.5 * y));
    }
    
    /**
    * Creates a new 1D - texture.
    * @param gl
    * @param colors
    * @return the texture ID for the generated texture.
    */
    public int create1DTexture(GL2 gl, Color[] colors){
        // Swap to 1D textures
        gl.glDisable(gl.GL_TEXTURE_2D);
        gl.glEnable(gl.GL_TEXTURE_1D);
        
        // Create an texture
        int[] texid = new int[]{-1};
        gl.glGenTextures(1, texid, 0);
        
        // Create a bytebuffer to save the colors in
        ByteBuffer bb = ByteBuffer.allocateDirect(colors.length * 4).order(ByteOrder.nativeOrder());
        
        // For every color given create a pixel/coordinate.
        for (Color color : colors) {
            int pixel = color.getRGB();
            bb.put((byte) ((pixel >> 16) & 0xFF)); // Red component
            bb.put((byte) ((pixel >> 8) & 0xFF));  // Green component
            bb.put((byte) (pixel & 0xFF));         // Blue component
            bb.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component
        }
        
        // Save the data we put into the bytebuffer
        bb.flip();
        
        // Bind the created texture to GL TEXTURE 1D
        gl.glBindTexture(gl.GL_TEXTURE_1D, texid[0]);
        gl.glTexImage1D(gl.GL_TEXTURE_1D, 0, gl.GL_RGBA8, colors.length, 0, gl.GL_RGBA, gl.GL_UNSIGNED_BYTE, bb);
        
        // Setup the filters
        gl.glTexParameteri(gl.GL_TEXTURE_1D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_LINEAR);
        gl.glTexParameteri(gl.GL_TEXTURE_1D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_LINEAR);
        
        // Rebind the texture
        gl.glBindTexture(gl.GL_TEXTURE_1D, 0);
        
        return texid[0];
    }
}
