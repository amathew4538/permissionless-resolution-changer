package amathew4538.permissionlessresolutionchanger;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL21;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.MemoryStack;
import java.nio.IntBuffer;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EyeProjector {
    private static long clientWindowHandle;
    private static long window;
    private static int windowWidth;
    private static int screenScale = Integer.parseInt(ResolutionChanger.getScreenScale());
    private static int fbHeight;
    private static int fbWidth;
    public static int pboId;

    private static class LoadedOverlay {
        public int textureId;
        public int width;
        public int height;

        public LoadedOverlay(int textureId, int width, int height) {
            this.textureId = textureId;
            this.width = width;
            this.height = height;
        }
    }

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

        fbWidth = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
        fbHeight = MinecraftClient.getInstance().getWindow().getFramebufferHeight();

        GL15.glBufferData(GL21.GL_PIXEL_PACK_BUFFER, 50331648, GL15.GL_STREAM_READ);
        GL15.glBindBuffer(GL21.GL_PIXEL_PACK_BUFFER, 0);

        Thread projectorThread = new Thread(() -> {
            int projectorTextureId = 0;
            List<LoadedOverlay> activeOverlays = new ArrayList<>();
            LoadedOverlay chosenOverlay = null;

            try {
                GLFW.glfwMakeContextCurrent(window);
                GL.createCapabilities();

                projectorTextureId = GL11.glGenTextures();
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, projectorTextureId);

                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, fbWidth, fbHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

                ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
                Identifier overlayId = new Identifier("permissionless-resolution-changer", "textures/gui/overlay/tall_overlay.png");

                do {
                    try {
                        Resource resource = resourceManager.getResource(overlayId);
                        try (NativeImage nativeImage = NativeImage.read(resource.getInputStream())) {
                            int overlayWidth = nativeImage.getWidth();
                            int overlayHeight = nativeImage.getHeight();
                            
                            int overlayTextureId = GL11.glGenTextures();
                            GL11.glBindTexture(GL11.GL_TEXTURE_2D, overlayTextureId);
                            
                            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

                            ByteBuffer buffer = MemoryUtil.memAlloc(overlayWidth * overlayHeight * 4);
                            for (int y = 0; y < overlayHeight; y++) {
                                for (int x = 0; x < overlayWidth; x++) {
                                    int pixel = nativeImage.getPixelColor(x, y);
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

                            activeOverlays.add(new LoadedOverlay(overlayTextureId, overlayWidth, overlayHeight));
                        }
                    } catch (IOException e) {
                        System.err.println("Failed to read overlay image texture: " + overlayId);
                        e.printStackTrace();
                    }

                    String nextPath = "textures/gui/overlay/tall_overlay-" + activeOverlays.size() + ".png";
                    overlayId = new Identifier("permissionless-resolution-changer", nextPath);

                } while (resourceManager.containsResource(overlayId));

                if (!activeOverlays.isEmpty()) {
                    Random rand = new Random();
                    chosenOverlay = activeOverlays.get(rand.nextInt(activeOverlays.isEmpty() ? 1 : activeOverlays.size()));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            while (!GLFW.glfwWindowShouldClose(window)) {
                GL11.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

                int currentFbWidth = MinecraftClient.getInstance().getWindow().getFramebufferWidth();
                int currentFbHeight = MinecraftClient.getInstance().getWindow().getFramebufferHeight();

                GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, pboId);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, projectorTextureId);

                if (currentFbWidth != fbWidth || currentFbHeight != fbHeight) {
                    fbWidth = currentFbWidth;
                    fbHeight = currentFbHeight;
                    GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, fbWidth, fbHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
                }

                GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, fbWidth, fbHeight, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0L);
                GL15.glBindBuffer(GL21.GL_PIXEL_UNPACK_BUFFER, 0);

                GL11.glViewport(0, 0, 384 * screenScale, 384 * screenScale);
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                GL11.glLoadIdentity();
                GL11.glOrtho(0.0, 768.0, 0.0, 768.0, -1.0, 1.0);

                GL11.glMatrixMode(GL11.GL_MODELVIEW);
                GL11.glLoadIdentity();

                GL11.glPushMatrix();

                double scaleY = 0.2 * screenScale * (60.0f / 384.0f);

                GL11.glTranslatef(384.0f, 384.0f, 0.0f);
                GL11.glScalef(1.0f, (float) scaleY, 1.0f);
                GL11.glTranslatef(-384.0f, -384.0f, 0.0f);

                float leftXCoords = (((float) fbWidth / 2.0f) - 30.0f) / (float) fbWidth;
                float rightXCoords = (((float) fbWidth / 2.0f) + 30.0f) / (float) fbWidth;

                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, projectorTextureId);
                GL11.glBegin(GL11.GL_QUADS);
                    GL11.glTexCoord2f(leftXCoords, 0.0f); GL11.glVertex2f(0.0f, 0.0f);
                    GL11.glTexCoord2f(rightXCoords, 0.0f); GL11.glVertex2f(768.0f, 0.0f);
                    GL11.glTexCoord2f(rightXCoords, 1.0f); GL11.glVertex2f(768.0f, 768.0f);
                    GL11.glTexCoord2f(leftXCoords, 1.0f); GL11.glVertex2f(0.0f, 768.0f);
                GL11.glEnd();
                GL11.glPopMatrix();

                if (chosenOverlay != null && chosenOverlay.textureId != 0) {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, chosenOverlay.textureId);

                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

                    float viewSize = 768.0f;
                    float renderWidth = viewSize;
                    float renderHeight = viewSize;

                    float imageAspect = (float) chosenOverlay.width / (float) chosenOverlay.height;
                    if (imageAspect > 1.0f) {
                        renderHeight = viewSize / imageAspect;
                    } else {
                        renderWidth = viewSize * imageAspect;
                    }

                    float xOffset = (viewSize - renderWidth) / 2.0f;
                    float yOffset = (viewSize - renderHeight) / 2.0f;

                    GL11.glBegin(GL11.GL_QUADS);
                        GL11.glTexCoord2f(0.0f, 1.0f); GL11.glVertex2f(xOffset, yOffset);
                        GL11.glTexCoord2f(1.0f, 1.0f); GL11.glVertex2f(xOffset + renderWidth, yOffset);
                        GL11.glTexCoord2f(1.0f, 0.0f); GL11.glVertex2f(xOffset + renderWidth, yOffset + renderHeight);
                        GL11.glTexCoord2f(0.0f, 0.0f); GL11.glVertex2f(xOffset, yOffset + renderHeight);
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
            for (LoadedOverlay overlay : activeOverlays) {
                if (overlay.textureId != 0) {
                    GL11.glDeleteTextures(overlay.textureId);
                }
            }
            GLFW.glfwMakeContextCurrent(MemoryUtil.NULL);
        });

        projectorThread.setDaemon(true);
        projectorThread.start();
    }
}