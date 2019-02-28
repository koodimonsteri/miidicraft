package engine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private final String title;
    private int width, height;
    private long windowHandler;
    private boolean resized;
    private boolean vSync;

    public Window(String title, int w, int h, boolean vSync){
        this.title = title;
        this.width = w;
        this.height = h;
        this.vSync = vSync;
        this.resized = false;
    }

    public void init(){

        // Callback for errors
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize glfw and check if it was succesfull
        if(!glfwInit()){
            throw new IllegalStateException("Failed to initialize glfw!");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);

        //glfwWindowHint(GLFW_STENCIL_BITS, 4);
        //glfwWindowHint(GLFW_SAMPLES, 4);

        // Create the window
        windowHandler = glfwCreateWindow(width, height, title, NULL, NULL);
        if(windowHandler == NULL){
            throw new RuntimeException("Failed to create window!");
        }

        boolean maximized = false;

        if (width == 0 || height == 0) {
            // Set up a fixed width and height so window initialization does not fail
            width = 100;
            height = 100;
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            maximized = true;
        }

        // Set callback for window resizing
        glfwSetFramebufferSizeCallback(windowHandler, (window, width, height)  -> {
            this.width = width;
            this.height = height;
            this.setResized(true);
        });

        // First key callback -- closes window
        glfwSetKeyCallback(windowHandler, (window, key, scancode, action, mods) -> {
            if(key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE){
                glfwSetWindowShouldClose(window, true);
            }
        });

        if (!maximized) {
            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            // Center our window
            glfwSetWindowPos(
                    windowHandler,
                    (vidmode.width() - width) / 2,
                    (vidmode.height() - height) / 2
            );
        }

        GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(windowHandler, (vidMode.width() - width) / 2, (vidMode.height() - height) / 2);

        glfwMakeContextCurrent(windowHandler);

        // Check if vSync should be enabled and then enable it.
        if(isVsync()){
            glfwSwapInterval(1);
        }

        glfwShowWindow(windowHandler);

        GL.createCapabilities();
        glClearColor(0.0f,0.0f,0.0f,0.0f);

        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
       // glEnable(GL_DEPTH_BUFFER_BIT);

        glEnable(GL_CULL_FACE);     // Enable Cull Face, discarding all vertices facing backwards
        glCullFace(GL_BACK);        // Cull Face back facing faces

        //glfwWindowHint(GLFW_SAMPLES, 4);       // Enable antialiasing

       // System.out.println(glGetString(GL_VERSION));
        //GLUtil.setupDebugMessageCallback();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        //glEnable(GL_POLYGON_SMOOTH);

    }

    public void update(){
        glfwSwapBuffers(windowHandler);
        glfwPollEvents();
    }

    public void setClearColor(float r, float g, float b, float alpha) {
        glClearColor(r, g, b, alpha);
    }

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(windowHandler, keyCode) == GLFW_PRESS;
    }

    public boolean windowShouldClose(){
        return glfwWindowShouldClose(windowHandler);
    }


    // Getters & Setters

    public long getWindowHandler(){
        return this.windowHandler;
    }

    public String getTitle() {
        return title;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isResized() {
        return resized;
    }

    public void setResized(boolean resized){
        this.resized = resized;
    }

    public boolean isVsync(){
        return this.vSync;
    }

    public void setVsync(boolean vSync) {
        this.vSync = vSync;
    }

}
