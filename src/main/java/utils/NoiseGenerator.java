package utils;

import org.joml.SimplexNoise;

public class NoiseGenerator {

    public NoiseGenerator(){

    }

    public static float[][] generateSimplexNoise(int width, int height){
        float[][] simplexnoise = new float[width][height];
        float frequency = 5.0f / (float) width;

        for(int x = 0; x < width; x++){
            for(int y = 0; y < height; y++){
                simplexnoise[x][y] = (float) Math.floor(SimplexNoise.noise(x * frequency,y * frequency) * 5) + 50;
                //simplexnoise[x][y] = (simplexnoise[x][y] + 1) / 2;   //generate values between 0 and 1
            }
        }

        return simplexnoise;
    }
}
