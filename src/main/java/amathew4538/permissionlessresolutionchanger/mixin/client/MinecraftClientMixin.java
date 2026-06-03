package amathew4538.permissionlessresolutionchanger.mixin.client;

import amathew4538.permissionlessresolutionchanger.PermissionlessResolutionChanger;
import amathew4538.permissionlessresolutionchanger.ResolutionChanger;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void OnClientTick(CallbackInfo ci) {
        if (PermissionlessResolutionChanger.baseSizeKeybind.wasPressed()) {
            ResolutionChanger.setResolutionToBase();
        } else if (PermissionlessResolutionChanger.tallSizeKeybind.wasPressed()) {
            ResolutionChanger.setResolutionToTall();
        } else if (PermissionlessResolutionChanger.thinSizeKeybind.wasPressed()) {
            ResolutionChanger.setResolutionToThin();
        } else if (PermissionlessResolutionChanger.wideSizeKeybind.wasPressed()) {
            ResolutionChanger.setResolutionToWide();
        }
    }
}
