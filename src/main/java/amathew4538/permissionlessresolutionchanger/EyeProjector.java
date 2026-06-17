package amathew4538.permissionlessresolutionchanger;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryStack;
import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import java.io.IOException;

public class EyeProjector {
    private static long clientWindowHandle;
    private static long window;
    private static int windowWidth;
    public static int pboId;

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

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            GLFW.glfwGetWindowSize(window, pWidth, pHeight);

            windowWidth = pWidth.get(0);
        }

        pboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, pboId);

        GL15.glBufferData(GL21.GL_PIXEL_PACK_BUFFER, 2359296, GL15.GL_STREAM_READ);
        GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);

        Thread projectorThread = new Thread(() -> {
            int projectorTextureId = 0;
            int overlayTextureId = 0;
            int overlayWidth = 1;
            int overlayHeight = 1;

            try {
                GLFW.glfwMakeContextCurrent(window);
                GL.createCapabilities();

                projectorTextureId = GL11.glGenTextures();
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, projectorTextureId);

                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 768, 768, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

                try {
                    Identifier overlayIdentifier = new Identifier("permissionless-resolution-changer", "textures/gui/overlay/tall_overlay.png");
                    Resource resource = MinecraftClient.getInstance().getResourceManager().getResource(overlayIdentifier);
                    
                    try (NativeImage nativeImage = NativeImage.read(resource.getInputStream())) {
                        overlayWidth = nativeImage.getWidth();
                        overlayHeight = nativeImage.getHeight();
                        
                        overlayTextureId = GL11.glGenTextures();
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, overlayTextureId);
                        
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                        ByteBuffer buffer = MemoryUtil.memAlloc(overlayWidth * overlayHeight * 4);
                        for (int y = 0; y < overlayHeight; y++) {
                            for (int x = 0; x < overlayWidth; x++) {
                                int pixel = nativeImage.getColor(x, y);
                                buffer.put((byte) (pixel & 0xFF));
                                buffer.put((byte) ((pixel >> 8) & 0xFF));
                                buffer.put((byte) ((pixel >> 16) & 0xFF));
                                buffer.put((byte) ((pixel >> 24) & 0xFF));
                            }
                        }
                        buffer.flip();

                        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, overlayWidth, overlayHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
                        
                        MemoryUtil.memFree(buffer);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to load tall_overlay.png asset");
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            while (!GLFW.glfwWindowShouldClose(window)) {
                GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

                int fbWidth = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
                int fbHeight = MinecraftClient.getInstance().getWindow().getFramebufferHeight();

                if (fbWidth <= 0) fbWidth = 768;

                GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboId);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, projectorTextureId);
                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 768, 768, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0L);
                GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);

                GL11.glViewport(0, 0, 768, 768);
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glLoadIdentity();
                GL11.glOrtho(0.0, 768.0, 0.0, 768.0, -1.0, 1.0);

                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glLoadIdentity();

                GL11.glPushMatrix();

                // double scaleX = ((double) fbHeight / (double) fbWidth) * (windowWidth / 384);

                GL11.glTranslatef(384.0f, 384.0f, 0.0f);
                GL11.glScalef(1.0f, 6.0f, 1.0f);
                GL11.glTranslatef(-384.0f, -384.0f, 0.0f);

                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, projectorTextureId);
                GL11.glBegin(GL11.GL_QUADS);
                    GL11.glTexCoord2f(0.4609375f, 0.0f); GL11.glVertex2f(0.0f, 0.0f);
                    GL11.glTexCoord2f(0.5390625f, 0.0f); GL11.glVertex2f(768.0f, 0.0f);
                    GL11.glTexCoord2f(0.5390625f, 1.0f); GL11.glVertex2f(768.0f, 768.0f);
                    GL11.glTexCoord2f(0.4609375f, 1.0f); GL11.glVertex2f(0.0f, 768.0f);
                GL11.glEnd();
                GL11.glPopMatrix();

                if (overlayTextureId != 0) {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, overlayTextureId);

                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                    float viewSize = 768.0f;
                    float renderWidth = viewSize;
                    float renderHeight = viewSize;

                    float imageAspect = (float) overlayWidth / (float) overlayHeight;
                    if (imageAspect > 1.0f) {
                        renderHeight = viewSize / imageAspect;
                    } else {
                        renderWidth = viewSize * imageAspect;
                    }

                    float xOffset = (viewSize - renderWidth) / 2.0f;
                    float yOffset = (viewSize - renderHeight) / 2.0f;

                    GL11.glBegin(GL11.GL_QUADS);
                        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex2f(xOffset, yOffset);
                        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex2f(xOffset + renderWidth, yOffset);
                        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex2f(xOffset + renderWidth, yOffset + renderHeight);
                        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex2f(xOffset, yOffset + renderHeight);
                    GL11.glEnd();

                    GL11.glDisable(GL11.GL_BLEND);
                }

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
            if (overlayTextureId != 0) {
                GL11.glDeleteTextures(overlayTextureId);
            }
            GLFW.glfwMakeContextCurrent(MemoryUtil.NULL);
        });

        projectorThread.setDaemon(true);
        projectorThread.start();
    }
}