package gfx;

import org.joml.Vector3f;

public class GameItem {

    private final Vector3f position;
    private final Vector3f rotation;
    private float scale;

    private Mesh mesh;

    public GameItem(Mesh mesh){
        this.mesh = mesh;
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = 1.0f;
    }

    public GameItem(){
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = 1.0f;
    }

    public void cleanUp() {
        mesh.cleanUp();
    }

    // Getters & Setters

    public void setPosition(float x, float y, float z){
        this.position.x = x;
        this.position.y = y;
        this.position.z = z;
    }

    public void setPosition(Vector3f pos){
        this.position.x = pos.x;
        this.position.y = pos.y;
        this.position.z = pos.z;
    }

    public Vector3f getPosition(){
        return this.position;
    }

    public void setRotation(float x, float y, float z){
        this.rotation.x = x;
        this.rotation.y = y;
        this.rotation.z = z;
    }

    public Vector3f getRotation(){
        return this.rotation;
    }

    public void setScale(float s){
        this.scale = s;
    }

    public float getScale(){
        return this.scale;
    }

    public Mesh getMesh(){
        return this.mesh;
    }

    public void setMesh(Mesh mesh){
        this.mesh = mesh;
    }

}
