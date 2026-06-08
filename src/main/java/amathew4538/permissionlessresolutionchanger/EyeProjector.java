package amathew4538.permissionlessresolutionchanger;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.glfw.GLFWErrorCallback;

public class EyeProjector {
    private static long clientWindowHandle;
    private static long window;

    public static void StartProjector() {
        clientWindowHandle = MinecraftClient.getInstance().getWindow().getHandle();

        GLFWErrorCallback.createPrint(System.err).set();

        if(!GLFW.glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_FLOATING, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(384, 384, "Eye Projector", MemoryUtil.NULL, clientWindowHandle);
        if(window == MemoryUtil.NULL) {
            throw new IllegalStateException("Unable to create GLFW Window");
        }

        try {
            GLFW.glfwSetWindowPos(
                window,
                (Integer.parseInt(ResolutionChanger.getScreenWidth()) / 2 - 192) / 2,
                Integer.parseInt(ResolutionChanger.getScreenHeight()) / 2
            );

            GLFW.glfwMakeContextCurrent(MemoryUtil.NULL);
            GLFW.glfwShowWindow(window);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Thread projectorThread = new Thread(() -> {
            try {
                GLFW.glfwMakeContextCurrent(window);
                GL.createCapabilities();
                
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (!GLFW.glfwWindowShouldClose(window)) {
                GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                
                // Texture logic will go here
                
                GLFW.glfwSwapBuffers(window);
                
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        projectorThread.setDaemon(true);
        projectorThread.start();
    }
}