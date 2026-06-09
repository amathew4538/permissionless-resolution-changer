package amathew4538.permissionlessresolutionchanger;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryStack;
import java.nio.IntBuffer;
import java.nio.ByteBuffer;

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
                Integer.parseInt(ResolutionChanger.getScreenWidth()) / 2 - 768,
                Integer.parseInt(ResolutionChanger.getScreenHeight()) / 2 - 192
            );

            GLFW.glfwShowWindow(window);
            System.out.println("Window created");
        } catch (Exception e) {
            e.printStackTrace();
        }

        int pboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, pboId);

        int bufferSize = 384 * 384 * 4;
        GL15.glBufferData(GL21.GL_PIXEL_PACK_BUFFER, bufferSize, GL15.GL_STREAM_READ);
        GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);
        
        Thread projectorThread = new Thread(() -> {
            int projectorTextureId = 0;
            try {
                GLFW.glfwMakeContextCurrent(window);
                GL.createCapabilities();

                projectorTextureId = GL11.glGenTextures();
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, projectorTextureId);

                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 384, 384, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

            } catch (Exception e) {
                e.printStackTrace();
            }

            while (!GLFW.glfwWindowShouldClose(window)) {
                GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

                int fbWidth = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
                int fbHeight = MinecraftClient.getInstance().getWindow().getFramebufferHeight();

                if (fbWidth <= 0) fbWidth = 384;

                GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboId);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, projectorTextureId);
                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 384, 384, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0L);
                GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);

                GL11.glViewport(0, 0, 384, 384);
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glLoadIdentity();
                GL11.glOrtho(-1.0, 1.0, -1.0, 1.0, -1.0, 1.0);

                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glLoadIdentity();

                double scaleY = ((double) fbHeight / (double) fbWidth);
                GL11.glScalef(1.0f, (float) scaleY, 1.0f);

                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glBegin(GL11.GL_QUADS);
                    GL11.glTexCoord2f(0.0f, 0.0f);
                    GL11.glVertex2f(-1.0f, -1.0f);

                    GL11.glTexCoord2f(1.0f, 0.0f);
                    GL11.glVertex2f(1.0f, -1.0f);

                    GL11.glTexCoord2f(1.0f, 1.0f);
                    GL11.glVertex2f(1.0f, 1.0f);

                    GL11.glTexCoord2f(0.0f, 1.0f);
                    GL11.glVertex2f(-1.0f, 1.0f);

                GL11.glEnd();
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

                GLFW.glfwSwapBuffers(window);
                try {
                    Thread.sleep(16);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }

            if (projectorTextureId != 0) {
                GL11.glDeleteTextures(projectorTextureId);
            }
            GLFW.glfwMakeContextCurrent(MemoryUtil.NULL);
        });

        projectorThread.setDaemon(true);
        projectorThread.start();
    }
}