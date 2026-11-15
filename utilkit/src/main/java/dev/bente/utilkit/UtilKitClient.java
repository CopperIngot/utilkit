package dev.bente.utilkit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UtilKitClient implements ClientModInitializer {

    public static final String MOD_ID = "utilkit";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static UtilKitConfig config;
    private static MacroManager macros;
    private static KeyBinding toggleHudKey;

    @Override
    public void onInitializeClient() {
        config = UtilKitConfig.load();
        macros = new MacroManager(config);

        toggleHudKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.utilkit.toggle_hud",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F6,
                "key.categories.utilkit"));

        HudOverlay.register(config);
        UtilKitCommands.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            macros.tick(client);
            while (toggleHudKey.wasPressed()) {
                config.hud.enabled = !config.hud.enabled;
                config.save();
                Messages.send(client, "HUD " + (config.hud.enabled ? "on" : "off"));
            }
        });

        LOGGER.info("UtilKit initialised with {} binding(s)", config.macros.size());
    }

    /** Re-reads the config file from disk, keeping the live instance reference intact. */
    public static void reload() {
        UtilKitConfig fresh = UtilKitConfig.load();
        config.hud = fresh.hud;
        config.macros.clear();
        config.macros.addAll(fresh.macros);
        config.save();
    }

    public static UtilKitConfig config() {
        return config;
    }
}
