package dev.bente.utilkit;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Locale;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/** Client-side {@code /utilkit} command tree. */
public final class UtilKitCommands {

    private UtilKitCommands() {
    }

    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                literal("utilkit")
                        .executes(ctx -> help(ctx.getSource()))
                        .then(literal("hud").executes(ctx -> {
                            UtilKitConfig config = UtilKitClient.config();
                            config.hud.enabled = !config.hud.enabled;
                            config.save();
                            feedback(ctx.getSource(), "HUD " + (config.hud.enabled ? "on" : "off"));
                            return 1;
                        }))
                        .then(literal("list").executes(ctx -> list(ctx.getSource())))
                        .then(literal("reload").executes(ctx -> {
                            feedback(ctx.getSource(), "Reloading config");
                            MinecraftClient.getInstance().send(() -> UtilKitClient.reload());
                            return 1;
                        }))
                        .then(literal("bind")
                                .then(argument("key", StringArgumentType.word())
                                        .then(argument("action", StringArgumentType.greedyString())
                                                .executes(ctx -> bind(
                                                        ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "key"),
                                                        StringArgumentType.getString(ctx, "action"))))))
                        .then(literal("unbind")
                                .then(argument("key", StringArgumentType.word())
                                        .executes(ctx -> unbind(
                                                ctx.getSource(),
                                                StringArgumentType.getString(ctx, "key")))))));
    }

    private static int help(FabricClientCommandSource source) {
        feedback(source, "UtilKit commands:");
        feedback(source, "  /utilkit hud - toggle the HUD");
        feedback(source, "  /utilkit bind <key> <action> - e.g. bind j chat:/spawn");
        feedback(source, "  /utilkit unbind <key>");
        feedback(source, "  /utilkit list - show bindings");
        feedback(source, "  /utilkit reload - reload config from disk");
        feedback(source, "Actions: chat:<text>, toggle:hud, copy:coords, drop:stack, drop:one");
        return 1;
    }

    private static int bind(FabricClientCommandSource source, String keyName, String action) {
        Integer code = KeyNames.parse(keyName);
        if (code == null) {
            error(source, "Unknown key: " + keyName);
            return 0;
        }
        String normalized = action.trim().toLowerCase(Locale.ROOT).startsWith("chat:")
                ? "chat:" + action.trim().substring(5)
                : action.trim();
        if (!MacroManager.isSupported(normalized)) {
            error(source, "Unsupported action: " + action);
            return 0;
        }

        UtilKitConfig config = UtilKitClient.config();
        config.macros.removeIf(macro -> macro.keyCode == code);
        config.macros.add(new UtilKitConfig.Macro(code, normalized));
        config.save();

        feedback(source, "Bound " + KeyNames.format(code) + " to " + normalized);
        return 1;
    }

    private static int unbind(FabricClientCommandSource source, String keyName) {
        if (keyName.equalsIgnoreCase("all")) {
            UtilKitClient.config().macros.clear();
            UtilKitClient.config().save();
            feedback(source, "Cleared all bindings");
            return 1;
        }
        Integer code = KeyNames.parse(keyName);
        if (code == null) {
            error(source, "Unknown key: " + keyName);
            return 0;
        }
        boolean removed = UtilKitClient.config().macros.removeIf(macro -> macro.keyCode == code);
        if (removed) {
            UtilKitClient.config().save();
            feedback(source, "Unbound " + KeyNames.format(code));
        } else {
            feedback(source, "Nothing bound to " + KeyNames.format(code));
        }
        return 1;
    }

    private static int list(FabricClientCommandSource source) {
        var macros = UtilKitClient.config().macros;
        if (macros.isEmpty()) {
            feedback(source, "No bindings yet. Try: /utilkit bind j chat:/spawn");
            return 1;
        }
        feedback(source, macros.size() + " binding(s):");
        for (UtilKitConfig.Macro macro : macros) {
            feedback(source, "  " + KeyNames.format(macro.keyCode) + " -> " + macro.action);
        }
        return 1;
    }

    private static void feedback(FabricClientCommandSource source, String message) {
        source.sendFeedback(Text.literal("[UtilKit] ").formatted(Formatting.AQUA)
                .append(Text.literal(message).formatted(Formatting.GRAY)));
    }

    private static void error(FabricClientCommandSource source, String message) {
        source.sendError(Text.literal("[UtilKit] " + message));
    }
}
