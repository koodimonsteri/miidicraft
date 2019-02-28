package main;

import engine.IGameLogic;
import engine.MouseInput;
import engine.Renderer;
import engine.Window;
import org.joml.Vector3f;
import world.Terrain;

public class CubeGame implements IGameLogic {

    private final Renderer renderer;
    private Terrain terrain;

    public CubeGame(){
        renderer = new Renderer();
    }

    @Override
    public void init(Window window) throws Exception {
        renderer.init(window);
        terrain = new Terrain();
    }

    @Override
    public void render(Window window){
        renderer.render(window, terrain);
    }

    @Override
    public void update(float interval, MouseInput mouseInput){
        terrain.update(interval, mouseInput);
    }

    @Override
    public void input(Window window, MouseInput mouseInput){
        terrain.input(window, mouseInput);
    }

    @Override
    public void cleanUp(){
        renderer.cleanUp();
        terrain.cleanUp();
    }

}
