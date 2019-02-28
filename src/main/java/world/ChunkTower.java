package world;

import org.joml.Vector2f;

import java.util.Arrays;
import java.util.List;

/*
 * ChunkTower represents tower of chunks, 16 on top of each other. Result is (16 x 16 x 256) tower (width, depth, height)
 */
public class ChunkTower {

    private Chunk[] chunks;        // First chunk in this array starts at y = 0
    private Vector2f position;     // Tower position ( x , z ) axes in world coordinates.
    private boolean firstInstance = true;

    public ChunkTower(float x, float y){
        this.position = new Vector2f(x, y);
        chunks = new Chunk[16];
        Arrays.fill(chunks, null);
    }

    public ChunkTower(Vector2f pos){
        this.position = pos;
        chunks = new Chunk[16];
        Arrays.fill(chunks, null);
    }

    public void addChunks(List<Chunk> c){
        for(int i = 0; i < c.size(); i++){
            chunks[i] = c.get(i);
        }
    }

    public void addChunk(Chunk chunk){
        chunks[(int)(chunk.getPosition().y / 16)] = chunk;
    }

    public Chunk[] getChunks(){
        return this.chunks;
    }

    public Vector2f getPosition(){
        return this.position;
    }

    public boolean getFirstInstance(){
        return this.firstInstance;
    }

    public void setFirstInstance(boolean b){
        this.firstInstance = b;
    }
}
