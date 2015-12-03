package robotrace;

/**
* Materials that can be used for the robots.
*/
public enum Material {

    /** 
     * Gold material properties.
     */
    GOLD (
        new float[] {1f, 0.8431f, 0f, 1.0f},
        new float[] {1f, 0.8431f, 0f, 1.0f},
        50f),

    /**
     * Silver material properties.
     */
    SILVER (
        new float[] {0.75f, 0.75f, 0.75f, 1.0f},
        new float[] {0.70f, 0.70f, 0.70f, 0.5f},
        30f),

    /** 
     * Wood material properties.
     */
    WOOD (
        new float[] {0.2f, 0.1f, 0f, 1.0f},
        new float[] {0.2f, 0.1f, 0f, 1.0f},
        2f),

    /**
     * Orange material properties.
     */
    ORANGE (
        new float[] {1f, 0.35f, 0f, 1.0f},
        new float[] {0.35f, 0.35f, 0.35f, 1.0f},
        10f);

    /** The diffuse RGBA reflectance of the material. */
    float[] diffuse;

    /** The specular RGBA reflectance of the material. */
    float[] specular;
    
    /** The specular exponent of the material. */
    float shininess;

    /**
     * Constructs a new material with diffuse and specular properties.
     */
    private Material(float[] diffuse, float[] specular, float shininess) {
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
    }
}
