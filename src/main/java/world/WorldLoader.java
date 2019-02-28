package world;

import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import utils.NoiseGenerator;
import world.Chunk;
import world.ChunkTower;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * This class is for generating, loading and saving world.
 * It is working on its own thread.
 * All loading actions happen always in the same order; Y, Z, X
 */
public class WorldLoader implements Runnable {

    private final int loadDistance;
    private final int chunkSize = 4096;

    private int width, height;
    private ChunkTower[][] toBeLoaded = null;            // ChunkTower array
    private List<ChunkTower> toBeSaved = null;           // List of chunks to be saved. --
    private Thread loadThread;                           // Loading Thread. --
    private float[][] heightMap;
    private boolean running = false, loading = false;
    private BlockingQueue<ChunkTower[][]> blockingQueue;

    public WorldLoader(int w, int h, BlockingQueue<ChunkTower[][]> queue){
        //loadThread = new Thread(this,"LoadThread");
        this.width = w;
        this.height = h;
        this.blockingQueue = queue;
        this.loadDistance = width * height;
        heightMap = NoiseGenerator.generateSimplexNoise(width, height);

        initWorldSaving();
        //ChunkTower cT = generateChunkTower(0, 0);
        //writeChunkTowerToFile(cT);
        //readChunkTowerFromFile(0, 0);
        //this.stop();
    }

    public ChunkTower generateChunkTower(float x, float z){
        ChunkTower chunkTower = new ChunkTower(x, z);
        List<Chunk> chunks = new ArrayList<>();
        for(int i = 0; i < 16; i++){
            chunks.add(generateChunk(x, i * 16, z));
        }
        chunkTower.addChunks(chunks);
        updateChunkTowerVisibilities(chunkTower);
        writeChunkTowerToFile(chunkTower);
        return chunkTower;
    }

    /*
     * This function generates chunk ie. fills array of chars based on height map and initializes the Chunk.
     * Array is first filled with small letters. Block visibilities are updated later
     * LowerCase letter means that block is invisible. (Block not facing air)
     * UpperCase letter means that block is visible. (Block facing air)
     */
    private Chunk generateChunk(float x, float y, float z){

        int i, j, k, i2, j2, k2;
        float curH;
        Chunk chunk = new Chunk((int)x, (int)y, (int)z);

        char[][][] chunkBlocks;
        chunkBlocks = new char[16][16][16];

        for(j = 0; j < 16; j++){
            for(k = 0; k < 16; k++){
                for(i = 0; i < 16; i++){
                    i2 = (int)x + i + heightMap.length / 2;
                    j2 = (int)y + j;
                    k2 = (int)z + k + heightMap.length / 2;
                    curH = heightMap[i2][k2];
                    if(j2 < (int)(curH * 0.3)){
                        chunkBlocks[i][j][k] = 's';      // Stone block   j < 0.3 * h
                    } else if(j2 < (int)(curH)){
                        chunkBlocks[i][j][k] = 'd';      // Dirt block    j < h
                    } else if(j2 == (int)curH) {
                        chunkBlocks[i][j][k] = 'g';      // Grass block   j == h
                    } else if(j2 > (int)curH){
                        chunkBlocks[i][j][k] = 'a';      // Air           j > h
                    }
                }
            }
        }
        chunk.setBlocks(chunkBlocks);
        //updateChunkVisibilities(chunk);
        return chunk;
    }

    private void updateChunkTowerVisibilities(ChunkTower chunkTower){
        int i, j, k, w;
        Chunk[] chunks = chunkTower.getChunks();

        for(w = 0; w < 16; w++){                                    // Loop through chunks
            char[][][] tempBlocks = chunks[w].getBlocks();
            for(j = 0; j < 16; j++) {                               // Chunk Y axis
                for (k = 0; k < 16; k++) {                          // Chunk Z axis
                    for (i = 0; i < 16; i++) {                      // Chunk X axis
                        if(i - 1 > 0) {
                            if (tempBlocks[i - 1][j][k] == 'a' && tempBlocks[i][j][k] != 'a') {
                                tempBlocks[i][j][k] = Character.toUpperCase(tempBlocks[i][j][k]);
                            }
                        }
                        if(i + 1 < 16) {
                            if (tempBlocks[i + 1][j][k] == 'a' && tempBlocks[i][j][k] != 'a') {
                                tempBlocks[i][j][k] = Character.toUpperCase(tempBlocks[i][j][k]);
                            }
                        }
                        if(j - 1 > 0) {
                            if (tempBlocks[i][j - 1][k] == 'a' && tempBlocks[i][j][k] != 'a') {
                                tempBlocks[i][j][k] = Character.toUpperCase(tempBlocks[i][j][k]);
                            }
                        } else {                                    // Check below chunk
                            if(w - 1 > 0){
                                if(chunkTower.getChunks()[w - 1].getBlocks()[i][15][k] == 'a' && tempBlocks[i][j][k] != 'a') {
                                    tempBlocks[i][j][k] = Character.toUpperCase(tempBlocks[i][j][k]);
                                }
                            }
                        }
                        if(j + 1 < 16) {
                            if (tempBlocks[i][j + 1][k] == 'a' && tempBlocks[i][j][k] != 'a') {
                                tempBlocks[i][j][k] = Character.toUpperCase(tempBlocks[i][j][k]);
                            }
                        } else {                                    // Check chunk on top
                            if(w + 1 < 16){
                                if(chunkTower.getChunks()[w + 1].getBlocks()[i][0][k] == 'a' && tempBlocks[i][j][k] != 'a') {
                                    tempBlocks[i][j][k] = Character.toUpperCase(tempBlocks[i][j][k]);
                                }
                            }
                        }
                        if(k - 1 > 0) {
                            if (tempBlocks[i][j][k - 1] == 'a' && tempBlocks[i][j][k] != 'a') {
                                tempBlocks[i][j][k] = Character.toUpperCase(tempBlocks[i][j][k]);
                            }
                        }
                        if(k + 1 < 16){
                            if (tempBlocks[i][j][k + 1] == 'a' && tempBlocks[i][j][k] != 'a') {
                                tempBlocks[i][j][k] = Character.toUpperCase(tempBlocks[i][j][k]);
                            }
                        }
                    }
                }
            }
            chunks[w].setBlocks(tempBlocks);
        }
    }

    private void initWorldSaving(){
        new File("C:/users/otto/ideaProjects/miidicraft/world").mkdir();
    }

    private void writeChunkTowerToFile(ChunkTower chunkTower){
        String fileName = "world/chunk(" + (int)chunkTower.getPosition().x + ", " + (int)chunkTower.getPosition().y + ")";
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, false), "UTF-8"));

            //System.out.println(encodeChunkTower(chunkTower) + "\n");
            writer.write(encodeChunkTower(chunkTower));           // Write run length encoded chunkTower to file.

            writer.close();                                       // Close the writer
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    private String decodeRLEString(String s){

        StringBuffer sb = new StringBuffer();
        Pattern pattern = Pattern.compile("[0-9]+|[a-zA-Z]");
        Matcher matcher = pattern.matcher(s);
        while(matcher.find()){
            int count = Integer.parseInt(matcher.group());
           // System.out.println("count: " + count);
            matcher.find();
            while(count-- != 0){
                sb.append(matcher.group());
            }
        }
        //System.out.println(sb);


        return sb.toString();
    }

    private ChunkTower readChunkTowerFromFile(int x, int z){
        ChunkTower chunkTower = null;
        File dir = new File("world");                // Reference to world directory.
        String filename = "chunk(" + x + ", " + z + ")";       // Build filename from coordinates.
        File[] files = dir.listFiles((File dir2, String name) -> (name.equals(filename)));   // Check if that filename already exists in world.
        if(files == null || files.length == 0){                // If it doesn't return null
            return null;
        } else {                                               // Else read and build chunk
            try {
                chunkTower = new ChunkTower(x, z);
                File f = files[0];
                String s, fullS = "";
                BufferedReader br = new BufferedReader(new FileReader(f));

                while ((s = br.readLine()) != null) {                   // Read the whole file
                    fullS += s;
                }

                String decodedS = decodeRLEString(fullS);               // Decode full chunkTower string

                int i, j, k, w, sCount = 0;
                char[][][] newBlocks;
                for (w = 0; w < 16; w++) {                              // 16 chunks in ChunkTower
                    newBlocks = new char[16][16][16];                   // Init new char array for block types
                    for (j = 0; j < 16; j++) {                          // Y axis
                        for (k = 0; k < 16; k++) {                      // Z axis
                            for (i = 0; i < 16; i++) {                  // X axis
                                newBlocks[i][j][k] = decodedS.charAt(sCount);
                                sCount++;
                            }
                        }
                    }
                    Chunk newChunk = new Chunk(x, w * 16, z);        // Initialize new Chunk
                    newChunk.setBlocks(newBlocks);                      // Set blocks to the Chunk
                    chunkTower.addChunk(newChunk);                      // Add Chunk to ChunkTower

                }

                br.close();                                             // Close the reader

                //System.out.println(f.getName());
            } catch(EOFException eof){
                System.out.println("END OF FILE EXCEPTION");
                System.err.println();
            } catch(IOException ioE) {
                System.out.println("---------Jotain kusi ja pahasti----------");
                System.err.println();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
        return chunkTower;
    }

    /*
     * Run length encodes one ChunkTower
     */
    private String encodeChunkTower(ChunkTower cT){
        Chunk[] chunks = cT.getChunks();
        String result = "";
        for(Chunk chunk : chunks){
            result += chunk.getEncodedChunk();
        }
        return result;
    }

    // Returns target ChunkTower x and y indices in toBeLoaded array
    private Vector2i getChunkTowerIdx(ChunkTower target, ChunkTower center){
        Vector2i v2i = new Vector2i();
        int newX, newY;
        newX = (int)((toBeLoaded.length / 2) - ((center.getPosition().x - target.getPosition().x) / 16)); //- (target.getPosition().x / 16)));
        newY = (int)((toBeLoaded.length / 2) - ((center.getPosition().y - target.getPosition().y) / 16)); // - (target.getPosition().y / 16)));
        v2i.x = newX;
        v2i.y = newY;
        return v2i;
    }

    /*
     * Main loading method, this is called every time mainThread needs chunks ie. calls executor -> setToBeLoaded
     * Loads or generates ChunkTowers as long as toBeLoaded array contains null values.
     */
    private void mainChunkLoader(Vector3f playerPos){

        while(loading){
            Vector2f nearestMissingChunk = getNearestMissingChunkPos(toBeLoaded, playerPos);
            if(nearestMissingChunk == null){
                loading = false;
                break;
            }
            int centerIdx = toBeLoaded.length / 2;
            System.out.println("missing x: " + nearestMissingChunk.x + "\tmissing z: " + nearestMissingChunk.y);
            // Check if we have file for this position
            ChunkTower chunkTower = readChunkTowerFromFile((int)nearestMissingChunk.x, (int)nearestMissingChunk.y);
            if(chunkTower != null){
                if(toBeLoaded[centerIdx][centerIdx] == null){
                    toBeLoaded[centerIdx][centerIdx] = chunkTower;
                } else {
                    Vector2i chunkIdx = getChunkTowerIdx(chunkTower, toBeLoaded[centerIdx][centerIdx]);
                    System.out.println("chunkIdx: " + chunkIdx.x + " : " + chunkIdx.y);
                    toBeLoaded[chunkIdx.x][chunkIdx.y] = chunkTower;
                }
                //Vector2d newIdx = getChunkTowerIdx(chunkTower, toBeLoaded[toBeLoaded.length / 2][toBeLoaded.length / 2]);
                //System.out.println(newIdx.x + " : " + newIdx.y);
            } else {
                if(nearestMissingChunk.x == 0 && nearestMissingChunk.y == 0){
                    chunkTower = generateChunkTower(nearestMissingChunk.x, nearestMissingChunk.y);
                    toBeLoaded[centerIdx][centerIdx] = chunkTower;
                    System.out.println("------------------------" + nearestMissingChunk.x + "  :  " + nearestMissingChunk.y);
                } else {
                    chunkTower = generateChunkTower(nearestMissingChunk.x, nearestMissingChunk.y);
                    Vector2i chunkIdx = getChunkTowerIdx(chunkTower, toBeLoaded[centerIdx][centerIdx]);
                    toBeLoaded[chunkIdx.x][chunkIdx.y] = chunkTower;
                    System.out.println("CHUNK IDX: " + chunkIdx.x + " : " + chunkIdx.y + "\tCHUNK XY: " + chunkTower.getPosition());
                }
            }
            try {
                //System.out.println("Loader Queue size: " + blockingQueue.remainingCapacity());
                for(int r = 0; r < toBeLoaded.length; r++){
                    for(int t  = 0; t < toBeLoaded.length; t++) {
                        if (toBeLoaded[r][t] != null) {
                            System.out.print("[" + (int)toBeLoaded[r][t].getPosition().x + " : " + (int)toBeLoaded[r][t].getPosition().y + "]");
                        } else {
                            System.out.print("[null]");
                        }
                    }
                    System.out.println("\n");
                }

                blockingQueue.clear();                               // Clear blocking queue in case there was still old chunks
                blockingQueue.put(toBeLoaded);                       // Add new loaded chunks to queue
            }catch(InterruptedException e){
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }

        }
    }

    // Finds the nearest missing ChunkTower
    private Vector2f getNearestMissingChunkPos(ChunkTower[][] chunks, Vector3f playerPos){
        int centerIdx = chunks.length / 2;
        if(chunks[centerIdx][centerIdx] == null){                     // Check if center ChunkTower is null. In that case return player position Vector2f(x1,y1)
            return new Vector2f(playerPos.x - (playerPos.x % 16), playerPos.z - (playerPos.z % 16));
        } else {
            /*
             * Loops through chunks (ChunkTower) array.
             * Check if current spot is null. Keep count of how many nulls there are.
             * Calculate lowest distance in (x + y) axes.
             * After loop if nullCounter == 0, return null.
             * Else return closest position
             */
            Vector2f centerPos = toBeLoaded[centerIdx][centerIdx].getPosition();
            float newX, newZ;
            Vector2f closest = new Vector2f(), current = new Vector2f();
            int totalDist;
            int x, z, nullCounter = 0, x1, z1, minDist = 1000000;     // Initialize minDistance to large value.
            for(x = 0; x < chunks.length; x++){
                for(z = 0; z < chunks.length; z++){
                    if(chunks[x][z] == null){
                        //System.out.println("CHUNK XY IDX: " + x + " : " + z);
                        x1 = Math.abs(x - (chunks.length / 2));       // Get distance by axes
                        z1 = Math.abs(z - (chunks.length / 2));
                        totalDist = x1 + z1;
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
                closest.x = centerPos.x + ((closest.x - (toBeLoaded.length / 2)) * 16);
                closest.y = centerPos.y + ((closest.y - (toBeLoaded.length / 2)) * 16);
                return closest;
            }
        }
    }

    // Writes all available chunks to file. Called when shutting down
    private void writeAllChunksToFile(){
        int i, j;
        for(i = 0; i < toBeLoaded.length; i++){
            for(j = 0; j < toBeLoaded.length; j++){
                if(toBeLoaded[i][j] != null){
                    writeChunkTowerToFile(toBeLoaded[i][j]);
                }
            }
        }
    }

    public synchronized void setToBeLoaded(ChunkTower[][] chunks, Vector3f playerPos){
        this.toBeLoaded = chunks;
        this.loading = true;
        this.mainChunkLoader(playerPos);           // Loading loop
    }

    public synchronized void setToBeSaved(List<ChunkTower> chunks){
        this.toBeSaved = chunks;
    }

    public synchronized void stop(){
        try {
            writeAllChunksToFile();
            if(!Thread.currentThread().isInterrupted()) {
                //loading = false;
                //Thread.currentThread().interrupt();
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void run() {
        try {
            System.out.println("WorldLoader created!");
            //running = true;
            //loadLoop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
