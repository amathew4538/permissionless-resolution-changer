package amathew4538.permissionlessresolutionchanger.mixin.client;

import amathew4538.permissionlessresolutionchanger.KeyBindingHelper;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameOptions.class)
public class GameOptionsMixin {
        @Mutable @Final
        @Shadow public KeyBinding[] keysAll;

        @Inject(at = @At("HEAD"), method = "load()V")
        public void loadHook(CallbackInfo info) {
            keysAll = KeyBindingHelper.process(keysAll);
        }

}