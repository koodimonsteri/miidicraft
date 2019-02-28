package utils;

import world.ChunkTower;

import java.util.List;

public class Utils {

    public static int[] listIntToArray(List<Integer> list) {
        int[] result = list.stream().mapToInt((Integer v) -> v).toArray();
        return result;
    }

    public static float[] listToArray(List<Float> list) {
        int size = list != null ? list.size() : 0;
        float[] floatArr = new float[size];
        for (int i = 0; i < size; i++) {
            floatArr[i] = list.get(i);
        }
        return floatArr;
    }

    public static void printNearbyChunks(ChunkTower[][] nearbyChunks){
        for(int r = 0; r < nearbyChunks.length; r++){
            for(int t  = 0; t <nearbyChunks.length; t++) {
                if (nearbyChunks[r][t] != null) {
                    System.out.print("[" + (int)nearbyChunks[r][t].getPosition().x + " : " + (int)nearbyChunks[r][t].getPosition().y + "]");
                } else {
                    System.out.print("[null]");
                }
            }
            System.out.println("\n");
        }
    }
}
