package dev.bente.utilkit;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Executes user bindings. A binding fires once per physical key press (edge triggered) and only
 * while no screen is open, so typing in chat never triggers a macro.
 */
public final class MacroManager {

    private final UtilKitConfig config;
    private final List<Integer> heldKeys = new ArrayList<>();

    public MacroManager(UtilKitConfig config) {
        this.config = config;
    }

    public void tick(MinecraftClient client) {
        if (client.player == null || client.player.networkHandler == null) {
            heldKeys.clear();
            return;
        }

        boolean screenOpen = client.currentScreen != null;
        long handle = client.getWindow().getHandle();

        for (UtilKitConfig.Macro macro : config.macros) {
            if (macro.keyCode < 0) {
                continue;
            }
            boolean down = !screenOpen && KeyState.isPressed(handle, macro.keyCode);
            boolean wasDown = heldKeys.contains(macro.keyCode);
            if (down && !wasDown) {
                execute(client, macro.action);
            }
            if (down && !wasDown) {
                heldKeys.add(macro.keyCode);
            } else if (!down && wasDown) {
                heldKeys.remove(Integer.valueOf(macro.keyCode));
            }
        }
    }

    public static boolean isSupported(String action) {
        if (action == null || action.isEmpty()) {
            return false;
        }
        if (action.startsWith("chat:") && action.length() > 5) {
            return true;
        }
        return switch (action) {
            case "toggle:hud", "copy:coords", "drop:stack", "drop:one" -> true;
            default -> false;
        };
    }

    private static void execute(MinecraftClient client, String action) {
        if (client.player == null || action == null) {
            return;
        }
        try {
            if (action.startsWith("chat:")) {
                client.player.networkHandler.sendChatMessage(action.substring(5));
                return;
            }
            switch (action) {
                case "toggle:hud" -> {
                    UtilKitClient.config().hud.enabled = !UtilKitClient.config().hud.enabled;
                    UtilKitClient.config().save();
                    Messages.send(client, "HUD " + onOff(UtilKitClient.config().hud.enabled));
                }
                case "copy:coords" -> {
                    String coords = String.format("%.1f %.1f %.1f",
                            client.player.getX(), client.player.getY(), client.player.getZ());
                    client.keyboard.setClipboard(coords);
                    Messages.send(client, "Copied " + coords);
                }
                case "drop:stack" -> client.player.dropSelectedItem(true);
                case "drop:one" -> client.player.dropSelectedItem(false);
                default -> Messages.send(client, "Unknown action: " + action);
            }
        } catch (RuntimeException e) {
            UtilKitClient.LOGGER.warn("Macro '{}' failed", action, e);
        }
    }

    private static String onOff(boolean value) {
        return value ? "on" : "off";
    }
}
