package world;

import engine.Camera;
import engine.MouseInput;
import engine.Window;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Player {

    private static final float playerPositionStep = 0.4f;
    private static final float mouseSensitivity = 0.5f;

    private Vector3f position;
    private Vector3f location;
    private Vector3f velocity;
    private float velocityscale;
    private Camera camera;

    public Player(){
        position = new Vector3f(5, 60, 5);
        location = new Vector3f(0, 0, 0);
        velocity = new Vector3f(0, 0, 0);
        velocityscale = 0.5f;
        camera = new Camera();
        camera.setPosition(5, 60, 5);
    }

    public void movePlayer(){
        velocity.mul(velocityscale);
        camera.movePosition(velocity.x * playerPositionStep, velocity.y * playerPositionStep, velocity.z * playerPositionStep);
        position = new Vector3f(camera.getPosition()).sub(0, -1, 0);
    }

    public void update(float interval, MouseInput mouseInput){
        movePlayer();
        // Update camera based on mouse
        if (mouseInput.isRightButtonPressed()) {
            Vector2f rotVec = mouseInput.getDisplVec();
            camera.moveRotation(rotVec.x * mouseSensitivity, rotVec.y * mouseSensitivity, 0);
        }
    }

    public void input(Window window, MouseInput mouseInput){
        if(window.isKeyPressed(GLFW_KEY_W)){
            velocity.z = -1;
        }
        if(window.isKeyPressed(GLFW_KEY_S)){
            velocity.z = 1;
        }
        if(window.isKeyPressed(GLFW_KEY_A)){
            velocity.x = -1;
        }
        if(window.isKeyPressed(GLFW_KEY_D)){
            velocity.x = 1;
        }
        if(window.isKeyPressed(GLFW_KEY_Z)){
            velocity.y = -1;
        }
        if(window.isKeyPressed(GLFW_KEY_X)){
            velocity.y = 1;
        }
    }

    public Vector3f getPosition(){
        return this.position;
    }

    public Camera getCamera(){
        return this.camera;
    }
}
