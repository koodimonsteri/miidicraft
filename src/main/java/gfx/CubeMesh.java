package gfx;

import utils.loaders.MeshLoader;

public class CubeMesh {

    private Mesh grassBlockMesh;
    private Mesh dirtBlockMesh;
    private Mesh stoneBlockMesh;

    private Texture grassTexture;
    private Texture dirtTexture;
    private Texture stoneTexture;

    private float[] test;

    public CubeMesh() {
        initTextures();
        initMeshes();
    }

    private void initTextures() {
        try {
            grassTexture = new Texture("/textures/grass_block_texture.png");
            dirtTexture = new Texture("/textures/dirt_block_texture.png");
            stoneTexture = new Texture("/textures/stone_block_texture.png");
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initMeshes() {
        try {
            grassBlockMesh = MeshLoader.loadMesh("src/main/resources/objects/grass_block.obj", 1);
            dirtBlockMesh = MeshLoader.loadMesh("src/main/resources/objects/dirt_block.obj", 1);
            stoneBlockMesh = MeshLoader.loadMesh("src/main/resources/objects/dirt_block.obj", 1);
            grassBlockMesh.setTexture(grassTexture);
            dirtBlockMesh.setTexture(dirtTexture);
            stoneBlockMesh.setTexture(stoneTexture);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public Mesh getInstancedGrassBlockMesh(int instances){
        Mesh newMesh = null;
        try {
            newMesh = MeshLoader.loadMesh("src/main/resources/objects/grass_block.obj", instances);
        } catch(Exception e){
            e.printStackTrace();
        }
        return newMesh;
    }

    // Getters for meshes
    public Mesh getGrassBlockMesh(){
        return this.grassBlockMesh;
    }

    public Mesh getDirtBlockMesh(){
        return this.dirtBlockMesh;
    }

    public Mesh getStoneBlockMesh() { return this.stoneBlockMesh; }
}
