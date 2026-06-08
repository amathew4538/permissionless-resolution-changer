package amathew4538.permissionlessresolutionchanger;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.Minecraft;

public class EyeProjector {
    public static void StartProjector() {
        long clientWindowHandle = Minecraft.getInstance().getWindow().getHandle();

        Thread projectorThread = new Thread(() -> {
            GLFWErrorCallback.createPrint(System.err).set();
		
            if(!GLFW.glfwInit()) {
                    throw new IllegalStateException("Unable to initialize GLFW");
            }

            GLFW.glfwDefaultWindowHints();
            GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
            GLFW.glfwWindowHint(GLFW.GLFW_FOCUS_ON_SHOW, GLFW.GLFW_FALSE);
            GLFW.glfwWindowHint(GLFW.GLFW_FLOATING, GLFW.GLFW_TRUE);

            window = GLFW.glfwCreateWindow(384, 384, "Eye Projector", NULL, clientWindowHandle);
            if(window == NULL) {
                throw new IllegalStateException("Unable to create GLFW Window");
            }

            try {
                GLFW.glfwSetWindowPos(
                    window,
                    (ResolutionChanger.getScreenWidth() / 2 - 192) / 2,
                    ResolutionChanger.getScreenHeight() / 2
                );

                GLFW.glfwMakeContextCurrent(window);
                GLFW.glfwShowWindow(window);
            }

            while (!GLFW.glfwWindowShouldClose(window)) {
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                GLFW.glfwSwapBuffers(window);
            }
        });

        projectorThread.setDaemon(true);
        projectorThread.start();
    }
}