package world;

import engine.MouseInput;
import engine.Window;
import gfx.*;
import org.joml.FrustumIntersection;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import utils.Utils;

import java.util.*;
import java.util.concurrent.*;

//TODO: finish world loading on another thread.
//TODO: implement instanced rendering and apply it to chunks.
//TODO: implement perlin noise for better terrain generation.
//TODO: 3D axis aligned collision detection and solving, (AABB)
//TODO: add HUD to the game
//TODO: Fog
//TODO: SkyBox
//TODO: 3D object picking
//TODO: Optimizations, ie. frustum culling, more efficient transformations
//TODO: ??light effects??

public class Terrain {

    private final int nearbyChunksSize = 9;                         // MUST to be odd number
    private final int chunkDim         = 16;                        // Chunk x, y, z size
    private boolean shouldLoadChunks   = true;
    private boolean terrainChanged     = false;
    private final int terrainSize      = 1024;
    private Player player;
    private CubeMesh cubeMesh;                                      // Generates and holds meshes
    private static ChunkTower[][] nearbyChunks;                     // Size of this array is always odd and the player is in the middle ChunkTower.

    private WorldLoader worldLoader;
    private BlockingQueue<ChunkTower[][]> blockingQueue;
    private ExecutorService executor;

    public Terrain() {
        player = new Player();
        cubeMesh = new CubeMesh();
        nearbyChunks = new ChunkTower[nearbyChunksSize][nearbyChunksSize];

        // Create instance of WorldLoader class with size of terrainSize x terrainSize.
        // It handles world generation, loading and saving on another Thread
        blockingQueue = new LinkedBlockingQueue<>();                 // Create new LinkedBlockingQueue for communicating chunks between main thread and world loading thread.
        executor = Executors.newSingleThreadExecutor();              // Create new SingleThreadExecutor
        worldLoader =  new WorldLoader(terrainSize, terrainSize, blockingQueue);
    }

    private void genInstancedChunkMeshes(Vector2f pos){

    }



    private Vector2f getNearestMissingChunkPos(ChunkTower[][] chunks){
        int centerIdx = nearbyChunksSize / 2;
        if(chunks[centerIdx][centerIdx] == null){                     // Check if center ChunkTower is null. In that case return Vector2f(0,0)
            //chunks[chunks.length / 2][chunks.length / 2] = generateChunkTower(0, 0);
            return new Vector2f(0, 0);
        } else {
            Vector2f centerPos = chunks[centerIdx][centerIdx].getPosition();
            float newX, newZ;
            Vector2f closest = new Vector2f(), current = new Vector2f();
            int x, z, nullCounter = 0, x1, z1, minDist = 1000000;     // Initialize minDistance to large value.
            for(x = 0; x < chunks.length; x++){
                for(z = 0; z < chunks.length; z++){
                    if(chunks[x][z] == null){
                        x1 = Math.abs(x - (chunks.length / 2));       // Get distance by axes
                        z1 = Math.abs(z - (chunks.length / 2));
                        int totalDist = x1 + z1;
                        current.x = x;
                        current.y = z;
                        if(totalDist < minDist){
                            closest.x = current.x;
                            closest.y = current.y;
                            minDist = totalDist;
                        }
                        nullCounter++;
                    }
                }
            }
            if(nullCounter == 0){
                return null;
            } else {
                float xOff, zOff;
                xOff = centerPos.x + ((closest.x - (nearbyChunks.length / 2)) * 16);
                zOff = centerPos.y + ((closest.y - (nearbyChunks.length / 2)) * 16);
                closest.x = xOff;
                closest.y = zOff;
                return closest;
            }


        }
    }

    private boolean playerIsInChunkTower(ChunkTower chunkTower){

        if(chunkTower != null &&
           player.getPosition().x > chunkTower.getPosition().x      &&
           player.getPosition().x < chunkTower.getPosition().x + 16 &&
           player.getPosition().z > chunkTower.getPosition().y      &&
           player.getPosition().z < chunkTower.getPosition().y + 16) {
            return true;
        } else return false;
    }

    // Check if should load more ChunkTowers to nearbyChunks
    private boolean shouldLoadChunks(){
        int i, j;
        for(i = 0; i < nearbyChunks.length; i++){
            for(j = 0; j < nearbyChunks.length; j++){
                if(nearbyChunks[i][j] == null){
                    return true;
                }
            }
        }
        return false;
    }

    private Vector2i getChunkShiftDir(){
        Vector2i dir = new Vector2i(0, 0);
        ChunkTower center = nearbyChunks[nearbyChunks.length/2][nearbyChunks.length/2];
        if(center != null) {
            if (player.getPosition().x < center.getPosition().x) {
                dir.x = 1;
            }
            if (player.getPosition().x > center.getPosition().x + 16) {
                dir.x = -1;
            }
            if (player.getPosition().z < center.getPosition().y) {
                dir.y = 1;
            }
            if (player.getPosition().z > center.getPosition().y + 16) {
                dir.y = -1;
            }
        }
       // System.out.println("shiftdir: " + dir.x + " : " + dir.y);
        return dir;
    }

    private void printNearbyChunks(){
        for(int r = 0; r < nearbyChunksSize; r++){
            for(int t  = 0; t <nearbyChunksSize; t++) {
                if (nearbyChunks[r][t] != null) {
                    System.out.print("[" + (int)nearbyChunks[r][t].getPosition().x + " : " + (int)nearbyChunks[r][t].getPosition().y + "]");
                }
            }
            System.out.println("\n");
        }
    }

    /*
     * Shift ChunkTowers in nearbyChunks according to parameter x and z.
     * -x shift to left, +x shift to right
     * -z shift down, +z shift up
     * Player should ALWAYS be in the middle one after shifts.
     */
    private void shiftChunks(Vector2i dir){
        List<ChunkTower> toBeRemoved = new ArrayList<>();
        ChunkTower[][] tempChunks = nearbyChunks.clone();
        System.out.println("--------Shifting chunks (" + dir.x + " : " + dir.y + ")");
        Utils.printNearbyChunks(nearbyChunks); ///////////////
        int i, j, shiftCount = 0;
        int x = dir.x, z = dir.y;
        if(x > 0){
            for(j = 0; j < nearbyChunksSize; j++){
                //toBeRemoved.add(nearbyChunks[i][0]);
                for(i = nearbyChunksSize - 1; i > 0; i--){
                    nearbyChunks[i][j] = tempChunks[i - 1][j];
                }
                nearbyChunks[0][j] = null;
            }
            shiftCount++;
        } else if(x < 0){
            for(j = 0; j < nearbyChunksSize; j++){
                toBeRemoved.add(nearbyChunks[0][j]);
                for(i = 0; i < nearbyChunksSize - 1; i++){
                    nearbyChunks[i][j] = tempChunks[i + 1][j];
                }
                nearbyChunks[nearbyChunksSize - 1][j] = null;
            }
            shiftCount++;
        }
        if(z > 0) {
            for(i = 0; i < nearbyChunksSize; i++){
                //toBeRemoved.add(nearbyChunks[i][0]);
                for(j = nearbyChunksSize - 1; j > 0; j--){
                    nearbyChunks[i][j] = tempChunks[i][j - 1];
                }
                nearbyChunks[i][0] = null;
            }
            shiftCount++;
        } else if(z < 0){
            for(i = 0; i < nearbyChunksSize; i++){
                toBeRemoved.add(nearbyChunks[i][nearbyChunksSize - 1]);
                for(j = 0; j < nearbyChunksSize - 1; j++){
                    nearbyChunks[i][j] = tempChunks[i][j + 1];
                }
                nearbyChunks[i][nearbyChunksSize - 1] = null;
            }
            shiftCount++;
        }
        if(shiftCount > 0){
            this.shouldLoadChunks = true;
        }
        System.out.println("SHIFTED CHUNKS!!!!!!!!");
        Utils.printNearbyChunks(nearbyChunks);


    }

    // This function loops through nearbyChunks and checks if there is new chunks from worldLoader.
    // If there is, create gameItem for corresponding big letters.
    private void processCurrentChunks(){
        int n, m, i, j, k, w;
        for(n = 0; n < nearbyChunksSize; n++){           // Loop nearbyChunks array
            for(m = 0; m < nearbyChunksSize; m++){       //
                if(nearbyChunks[n][m] != null) {
                    if (nearbyChunks[n][m].getFirstInstance()) {        // Check for first instance --> need to create GameItems
                        Vector2f v2f = nearbyChunks[n][m].getPosition();
                        for (w = 0; w < 16; w++) {                      // Chunks in ChunkTower
                            for (j = 0; j < 16; j++) {                  // Y axis
                                for (k = 0; k < 16; k++) {              // Z axis
                                    for (i = 0; i < 16; i++) {          // X axis
                                        char curC = nearbyChunks[n][m].getChunks()[w].getBlocks()[i][j][k];
                                        if (Character.isUpperCase(curC)) {
                                            GameItem gItem;
                                            if (curC == 'S') {                                                      // Stone block
                                                gItem = new CubeItem(cubeMesh.getStoneBlockMesh());
                                                ((CubeItem) gItem).setBlockType(BlockType.GRASS);
                                                gItem.setPosition(v2f.x + i, (w * 16) + j, v2f.y + k);
                                                gItem.setScale(0.5f);
                                                nearbyChunks[n][m].getChunks()[w].addGameItem(gItem);
                                            } else if (curC == 'D') {                                               // Dirt block
                                                gItem = new CubeItem(cubeMesh.getDirtBlockMesh());
                                                ((CubeItem) gItem).setBlockType(BlockType.DIRT);
                                                gItem.setPosition(v2f.x + i, (w * 16) + j, v2f.y + k);
                                                gItem.setScale(0.5f);
                                                nearbyChunks[n][m].getChunks()[w].addGameItem(gItem);
                                            } else if(curC == 'G') {                                                // Grass block
                                                gItem = new CubeItem(cubeMesh.getGrassBlockMesh());
                                                ((CubeItem) gItem).setBlockType(BlockType.STONE);
                                                gItem.setPosition(v2f.x + i, (w * 16) + j, v2f.y + k);
                                                gItem.setScale(0.5f);
                                                nearbyChunks[n][m].getChunks()[w].addGameItem(gItem);
                                            }
                                            //System.out.println("GameItem pos: " + gItem.getPosition().x + " : " + gItem.getPosition().y + " : " + gItem.getPosition().z);
                                        }
                                    }
                                }
                            }
                        }
                        nearbyChunks[n][m].setFirstInstance(false);
                    }
                }
            }
        }
        terrainChanged = false;
    }

    private void fetchChunksFromLoader(){
        try {
            ChunkTower[][] temp = blockingQueue.poll();
            //System.out.println("Terrain blocking queue size: " + blockingQueue.remainingCapacity());
            if (temp != null) {
                nearbyChunks = temp;
                terrainChanged = true;
                System.out.println("GOT SOME CHUNKS FROM LOADER!!");
                Utils.printNearbyChunks(nearbyChunks);
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    // For testing
    private int getMissingChunkCount(){
        int sum = 0;
        for(int i = 0; i < nearbyChunksSize; i++){
            for(int j = 0; j < nearbyChunksSize; j++){
                if(nearbyChunks[i][j] != null){
                    if(chunkTowerHasGameItems(nearbyChunks[i][j])) {
                        System.out.println("chunktower has gameitems: " + chunkTowerHasGameItems(nearbyChunks[i][j]) + "\nchunk pos: " + nearbyChunks[i][j].getPosition().x + " : " + nearbyChunks[i][j].getPosition().y);
                        sum++;
                    }
                }
            }
        }
        return sum;
    }

    // For testing
    private boolean chunkTowerHasGameItems(ChunkTower cT){
        for(int i = 0; i < 16; i++){
            if(cT.getChunks()[i].getNonInstancedChunkMeshes().isEmpty()){
                return false;
            }
        }
        return true;
    }

    private Vector2i getChunkTowerIdx(ChunkTower target, ChunkTower center){
        Vector2i v2i = new Vector2i();
        int newX, newY;
        newX = (int)((nearbyChunksSize / 2) - ((center.getPosition().x / chunkDim) - (target.getPosition().x / chunkDim)));
        newY = (int)((nearbyChunksSize / 2) - ((center.getPosition().y / chunkDim) - (target.getPosition().y / chunkDim)));
        v2i.x = newX;
        v2i.y = newY;
        return v2i;
    }

    public void update(float interval, MouseInput mouseInput){

        player.update(interval, mouseInput);              // First update player

        Vector2i shiftDir = getChunkShiftDir();
        if(!((shiftDir.x == 0) && (shiftDir.y == 0))) {
            //Utils.printNearbyChunks(nearbyChunks);
            shiftChunks(shiftDir);
        }

        shouldLoadChunks = this.shouldLoadChunks();
        System.out.println("PlayerPos: " + player.getPosition().x + " : " + player.getPosition().y + " : " + player.getPosition().z);

        if(shouldLoadChunks){                                  // If we should load chunks
            executor.execute(() -> worldLoader.setToBeLoaded(nearbyChunks, player.getPosition()));     // Execute method on worldLoader
            shouldLoadChunks = false;                          // Set shouldLoad to False
        }
        fetchChunksFromLoader();

        if(terrainChanged){
            processCurrentChunks();         // If terrain has changed, process nearbyChunks and create GameItems
        }
    }



    public void input(Window window, MouseInput mouseInput){
        player.input(window, mouseInput);
    }

    public void cleanUp() {
        try {
            executor.execute(() -> worldLoader.stop());         // Stop world loader and shutdown executor
            executor.shutdown();
            //executor.awaitTermination(5, TimeUnit.SECONDS);

        }catch(Exception e){
            System.err.println("---!!!Jotain kusi ja pahasti world loader executorin sulkeutuessa!!!---");
            e.printStackTrace();
        }

    }

    public Map<Mesh, List<GameItem>> getInstancedGameMeshes(FrustumIntersection frustumIntersection){
        Map<Mesh, List<GameItem>> res = new HashMap<>();
        int n, m, w;
        for(n = 0; n < nearbyChunksSize; n++){
            for(m = 0; m < nearbyChunksSize; m++){
                if(nearbyChunks[n][m] != null) {
                    for(w = 0; w < 16; w++){
                        Chunk tempChunk = nearbyChunks[n][m].getChunks()[w];

                        // Test frustumIntersection from chunk minAab and maxAab
                        if(frustumIntersection.testAab(tempChunk.getPosition(), new Vector3f(tempChunk.getPosition().x + chunkDim, tempChunk.getPosition().y + chunkDim, tempChunk.getPosition().z + chunkDim)))
                        {
                            for (Mesh mesh : tempChunk.getInstancedChunkMeshes().keySet()) {
                                List<GameItem> tempList = res.get(mesh);
                                if (tempList == null) {
                                    tempList = new ArrayList<>();
                                    tempList.addAll(tempChunk.getInstancedChunkMeshes().get(mesh));
                                } else {
                                    tempList.addAll(tempChunk.getInstancedChunkMeshes().get(mesh));
                                }
                                //System.out.println(tempList.size());
                                res.put(mesh, tempList);
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    public Map<Mesh, List<GameItem>> getNonInstancedTerrainMeshes(FrustumIntersection frustumIntersection){
        Map<Mesh, List<GameItem>> res = new HashMap<>();
        int n, m, w;
        for(n = 0; n < nearbyChunksSize; n++){
            for(m = 0; m < nearbyChunksSize; m++){
                if(nearbyChunks[n][m] != null) {
                    for(w = 0; w < 16; w++){
                        Chunk tempChunk = nearbyChunks[n][m].getChunks()[w];

                        // Test frustumIntersection from chunk minAab and maxAab
                        if(frustumIntersection.testAab(tempChunk.getPosition(), new Vector3f(tempChunk.getPosition().x + chunkDim, tempChunk.getPosition().y + chunkDim, tempChunk.getPosition().z + chunkDim)))
                        {
                            for (Mesh mesh : tempChunk.getNonInstancedChunkMeshes().keySet()) {
                                List<GameItem> tempList = res.get(mesh);
                                if (tempList == null) {
                                    tempList = new ArrayList<>();
                                    tempList.addAll(tempChunk.getNonInstancedChunkMeshes().get(mesh));
                                } else {
                                    tempList.addAll(tempChunk.getNonInstancedChunkMeshes().get(mesh));
                                }
                                //System.out.println(tempList.size());
                                res.put(mesh, tempList);
                            }
                        }
                    }
                }
            }
        }
        return res;
    }

    public Player getPlayer(){
        return this.player;
    }
}
