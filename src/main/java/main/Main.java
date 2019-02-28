package main;

import engine.GameEngine;
import engine.IGameLogic;


public class Main {

    public static void main(String[] args) {
        try {
            boolean vSync = true;
            IGameLogic myCubeGame = new CubeGame();
            GameEngine gameEngine = new GameEngine("Miidicraft1.0", 600, 600, vSync, myCubeGame);
            gameEngine.start();
        } catch(Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

}
