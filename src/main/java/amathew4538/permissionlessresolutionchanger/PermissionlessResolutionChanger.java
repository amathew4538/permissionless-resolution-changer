package amathew4538.permissionlessresolutionchanger;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.TranslatableText;

public class PermissionlessResolutionChanger implements ClientModInitializer {
	public static final String MOD_ID = "permissionless-resolution-changer";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

	public static KeyBinding baseSizeKeybind;
	public static KeyBinding tallSizeKeybind;
	public static KeyBinding thinSizeKeybind;
	public static KeyBinding wideSizeKeybind;

	@Override
	public void onInitializeClient() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		baseSizeKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			this.Translate("key.prc.base","Base Resolution").getString(),
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_H,
			this.Translate("key.categories.prc","Permissionless Resolution Changer").getString()
		));
		tallSizeKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			this.Translate("key.prc.tall","Tall Resolution").getString(),
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_J,
			this.Translate("key.categories.prc","Permissionless Resolution Changer").getString()
		));
		thinSizeKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			this.Translate("key.prc.thin","Thin Resolution").getString(),
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_K,
			this.Translate("key.categories.prc","Permissionless Resolution Changer").getString()
		));
		wideSizeKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			this.Translate("key.prc.wide","Wide Resolution").getString(),
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_L,
			this.Translate("key.categories.prc","Permissionless Resolution Changer").getString()
		));

		LOGGER.info("Permissionless Resolution Changer Initialized!");
	}

	public Text Translate(String key, String replacement) {
		Text t = new TranslatableText(key);

		if(t.getString().equals(key)){
			return new LiteralText(replacement);
		}

		return t;
	}
}