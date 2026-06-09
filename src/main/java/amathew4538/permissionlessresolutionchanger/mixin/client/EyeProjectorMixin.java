package amathew4538.permissionlessresolutionchanger.mixin.client;

import amathew4538.permissionlessresolutionchanger.ResolutionChanger;
import amathew4538.permissionlessresolutionchanger.EyeProjector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.nbt.CompoundTag;
import org.apache.commons.lang3.ArrayUtils;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import java.io.File;
import java.io.PrintWriter;
import java.util.Iterator;
import org.lwjgl.glfw.GLFW;

@Mixin(MinecraftClient.class)
public class EyeProjectorMixin {
    private static boolean isWaiting = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void OnClientTick(CallbackInfo ci) {
        GLFW.glfwPollEvents();

        if (ResolutionChanger.isTallChanging && !isWaiting) {
            isWaiting = true;

            Thread monitorThread = new Thread(() -> {
                try {
                    int targetDPI = Integer.parseInt(ResolutionChanger.getDPI());
                    while (ResolutionChanger.isTallChanging) {
                        MinecraftClient client = MinecraftClient.getInstance();

                        if (client.getWindow().getFramebufferHeight() == targetDPI) {
                            ResolutionChanger.isTallChanging = false;

                            client.execute(() -> {
                                EyeProjector.StartProjector();
                            });

                            break;
                        }

                        Thread.sleep(16);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    isWaiting = false;
                }
            });

            monitorThread.setDaemon(true);
            monitorThread.start();
        }
    }
}