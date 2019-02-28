package world;

import gfx.BlockType;
import gfx.GameItem;
import gfx.InstancedMesh;
import gfx.Mesh;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
 * Chunk is 16x16x16 box.
 */

public class Chunk {

    private Vector3f position;              // Chunk position in world
    private char[][][] blocks;              // 3D char array which holds cube types
    private HashMap<Mesh, List<GameItem>> chunkNonInstancedMeshMap;
    private HashMap<Mesh, List<GameItem>> chunkInstancedMeshMap;      // Map containing 1 instanced mesh for every BlockType in chunk

    public Chunk(int x, int y, int z){
        this.position = new Vector3f(x, y, z);
        initChunk();

    }

    public Chunk(Vector3f pos){
        this.position = pos;
        initChunk();
    }

    private void initChunk(){
        chunkNonInstancedMeshMap = new HashMap<>();
        chunkInstancedMeshMap = new HashMap<>();
    }

    // Run length encode chunk blocks for saving
    public String getEncodedChunk(){
        int count = 0;
        char prev = blocks[0][0][0];
        String result = "";
        for(int j = 0; j < 16; j++){
            for(int k = 0; k < 16; k++){
                for(int i = 0; i < 16; i++){
                    char cur = blocks[i][j][k];
                    if(blocks[i][j][k] == prev){
                        if(Character.isUpperCase(prev) && Character.isUpperCase(cur)) {
                            count++;
                        } else if(Character.isLowerCase(prev) && Character.isLowerCase(cur)){
                            count++;
                        } else {
                            result += Integer.toString(count);
                            result += Character.toString(prev);
                            prev = cur;
                            count = 1;
                        }
                    } else {
                        result += Integer.toString(count);
                        result += Character.toString(prev);
                        prev = cur;
                        count = 1;
                    }
                }
            }
        }
        result += Integer.toString(count);
        result += Character.toString(prev);
        //System.out.println(result + "\n" + "chunkPos: " + position.x + " : " + position.y + " : " + position.z);
        return result;
    }

    public void addGameItem(GameItem gItem){
        boolean isInstanced;
        isInstanced = gItem.getMesh() instanceof InstancedMesh;     // Check if gameItem is instanced or not
        List<GameItem> tempList;
        if(isInstanced) {
            tempList = chunkInstancedMeshMap.get(gItem.getMesh());
        } else {
            tempList = chunkNonInstancedMeshMap.get(gItem.getMesh());
        }
        if(tempList == null){
            tempList = new ArrayList<>();
        }
        tempList.add(gItem);
        //System.out.println("tempList len: " + tempList.size());
        if(isInstanced) {
            chunkInstancedMeshMap.put(gItem.getMesh(), tempList);
        } else {
            chunkNonInstancedMeshMap.put(gItem.getMesh(), tempList);
        }
        //System.out.println("chunkMeshMap keys: " + chunkMeshMap.keySet().size() + " values: " + chunkMeshMap.values().size());
    }

    public HashMap<Character, Integer> getChunkBlocksHashMap(){
        HashMap<Character, Integer> resMap = new HashMap<>();
        Character cur;
        for(int i = 0; i < 16; i++){
            for(int j = 0; j < 16; j++){
                for(int k = 0; k < 16; k++){
                    cur = blocks[i][j][k];
                    if(Character.isUpperCase(cur) && resMap.containsKey(cur)){
                        resMap.put(cur, resMap.get(cur) + 1);
                    } else {
                        resMap.put(cur, 1);
                    }
                }
            }
        }
        return resMap;
    }

    public void setBlocks(char[][][] blocks){
        this.blocks = blocks;
    }

    public char[][][] getBlocks(){
        return this.blocks;
    }

    public Vector3f getPosition(){
        return this.position;
    }

    public HashMap<Mesh, List<GameItem>> getNonInstancedChunkMeshes() {
        return this.chunkNonInstancedMeshMap;
    }

    public HashMap<Mesh, List<GameItem>> getInstancedChunkMeshes() {
        return this.chunkInstancedMeshMap;
    }
}
