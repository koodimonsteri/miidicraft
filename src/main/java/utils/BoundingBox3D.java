package utils;

import org.joml.Vector3f;

/*
 * This class represents axis aligned bounding box (AABB)
 *
 */
public class BoundingBox3D {

    private Vector3f minAABB, maxAABB;

    public BoundingBox3D(float x1, float y1, float z1, float x2, float y2, float z2){
        this.minAABB = new Vector3f(x1, y1, z1);
        this.maxAABB = new Vector3f(x2, y2, z2);
    }

    public BoundingBox3D(Vector3f min, Vector3f max){
        this.minAABB = min;
        this.maxAABB = max;
    }

    // Checks if given point in 3D space is inside this.
    public boolean isInBoundaries(Vector3f v3f){
        if((v3f.x > this.minAABB.x) && v3f.x < (this.maxAABB.x) &&
           (v3f.y > this.minAABB.y) && v3f.y < (this.maxAABB.y) &&
           (v3f.z > this.minAABB.z) && v3f.z < (this.maxAABB.z)){
            return true;
        } else return false;
    }
}
