package engine;

import utils.Timer;

public class GameEngine implements Runnable {

    public static final int TARGET_FPS = 60;
    public static final int TARGET_UPS = 30;

    private final Window window;
    private final Thread gameLoopThread;
    private final Timer timer;
    private final IGameLogic iCubeGameLogic;
    private final MouseInput mouseInput;

    public GameEngine(String title, int width, int height, boolean vSync, IGameLogic iLogic) throws Exception {
        gameLoopThread = new Thread(this, "GAME_LOOP_THREAD");
        window = new Window(title, width, height, vSync);
        this.iCubeGameLogic = iLogic;
        timer = new Timer();
        mouseInput = new MouseInput();
    }

    protected void init() throws Exception {
        window.init();
        timer.init();
        mouseInput.init(window);
        iCubeGameLogic.init(window);
    }

    public void start(){
        gameLoopThread.start();
    }

    @Override
    public void run(){
        try {
            init();
            gameLoop();
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            cleanUp();
            //System.exit(0);
        }
    }

    protected void gameLoop(){
        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;

        boolean running = true;

        while(running && !window.windowShouldClose()){
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;

            input();

            while(accumulator >= interval){
                update(interval);
                accumulator -= interval;
            }

            render();

            if(!window.isVsync()){
                sync();
            }
        }
    }

    private void sync(){
        float loopSlot = 1f / TARGET_FPS;
        double endTime = timer.getLastLoopTime() + loopSlot;
        while(timer.getTime() < endTime){
            try {
                Thread.sleep(1);
            } catch(InterruptedException ie){
                ie.printStackTrace();
            }
        }
    }

    protected void input(){
        mouseInput.input(window);
        iCubeGameLogic.input(window, mouseInput);
    }

    protected void update(float interval){
        iCubeGameLogic.update(interval, mouseInput);
    }

    protected void render(){
        iCubeGameLogic.render(window);
        window.update();
    }

    protected void cleanUp(){
        try {
            iCubeGameLogic.cleanUp();
        } catch(Exception e){
            e.getCause();
        }
    }
}
