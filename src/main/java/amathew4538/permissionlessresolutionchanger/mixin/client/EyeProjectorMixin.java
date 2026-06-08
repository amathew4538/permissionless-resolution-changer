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
    @Inject(method = "tick", at = @At("HEAD"))
    private void OnClientTick(CallbackInfo ci) {
        GLFW.glfwPollEvents();

        if (ResolutionChanger.isTallChanging) {
            if (MinecraftClient.getInstance().getWindow().getFramebufferHeight() == Integer.parseInt(ResolutionChanger.getDPI())) {
                ResolutionChanger.waitingForTallStabilization = false;
                EyeProjector.StartProjector();
            }
        }
    }
}
