package engine;

import org.joml.FrustumIntersection;
import utils.loaders.ResLoader;
import utils.Transformation;
import gfx.GameItem;
import gfx.Mesh;
import org.joml.Matrix4f;
import world.Terrain;

import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE2;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class Renderer {

    private static final float fov = (float)Math.toRadians(60.0f);    // Field of view
    private static final float zNear = 0.1f;
    private static final float zFar = 1000.0f;

    private FrustumIntersection frustumIntersection;
    private final Transformation transformation;
    private ShaderProgram shaderProgram;

    public Renderer() {
        transformation = new Transformation();
        frustumIntersection = new FrustumIntersection();
    }

    public void init(Window window) throws Exception {
        shaderProgram = new ShaderProgram();
        shaderProgram.createVertexShader(ResLoader.loadResources("/shaders/vertex.vert"));
        shaderProgram.createFragmentShader(ResLoader.loadResources("/shaders/fragment.frag"));
        shaderProgram.link();

        shaderProgram.createUniform("modelViewNonInstancedMatrix");
        shaderProgram.createUniform("projectionMatrix");

        shaderProgram.createUniform("isInstanced");

        shaderProgram.createUniform("texture_sampler");
        }

    public void render(Window window, Terrain terrain){
        clear();

        if(window.isResized()){
            glViewport(0, 0, window.getWidth(), window.getHeight());
            window.setResized(false);
        }

        // Update Projection and View matrices once per render cycle
        transformation.updateProjectionMatrix(fov, window.getWidth(), window.getHeight(), zNear, zFar);
        transformation.updateViewMatrix(terrain.getPlayer().getCamera());

        renderTerrain(window, terrain);

    }

    private void renderTerrain(Window window, Terrain terrain){
        shaderProgram.bind();

      //  Matrix4f projectionMatrix = transformation.getProjectionMatrix(fov, window.getWidth(), window.getHeight(), zNear, zFar);
        Matrix4f projectionMatrix = transformation.getProjectionMatrix();
        shaderProgram.setUniform("projectionMatrix", projectionMatrix);

        Matrix4f viewMatrix = transformation.getViewMatrix();

        shaderProgram.setUniform("texture_sampler", 0);

        frustumIntersection.set(projectionMatrix.mul(viewMatrix));        // Set FrustumIntersection for later filtering

        renderNonInstancedMeshes(terrain, viewMatrix);
        renderInstancedMeshes(terrain, viewMatrix);

        shaderProgram.unBind();
    }

    private void renderNonInstancedMeshes(Terrain terrain, Matrix4f viewMatrix){
        shaderProgram.setUniform("isInstanced", 0);
        // Render each mesh with the associated game Items
        Map<Mesh, List<GameItem>> mapMeshes = terrain.getNonInstancedTerrainMeshes(frustumIntersection);
        for (Mesh mesh : mapMeshes.keySet()) {
            mesh.renderList(mapMeshes.get(mesh), (GameItem gameItem) -> {
                        Matrix4f modelViewMatrix = transformation.getModelViewMatrix(gameItem, viewMatrix);
                        shaderProgram.setUniform("modelViewNonInstancedMatrix", modelViewMatrix);
                    }
            );
        }
    }

    private void renderInstancedMeshes(Terrain terrain, Matrix4f viewMatrix){
        shaderProgram.setUniform("isInstanced", 1);


    }

    public void cleanUp(){
        if(shaderProgram != null){
            shaderProgram.cleanUp();
        }
    }

    public void clear(){
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // | GL_STENCIL_BUFFER_BIT);
    }
}
