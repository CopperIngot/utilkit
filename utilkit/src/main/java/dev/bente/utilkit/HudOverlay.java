package dev.bente.utilkit;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.List;

/** Draws the configurable info panel in the top-left corner. */
public final class HudOverlay {

    private HudOverlay() {
    }

    @SuppressWarnings("deprecation") // HudRenderCallback is still the simplest stable hook for 1.21.8
    public static void register(UtilKitConfig config) {
        HudRenderCallback.EVENT.register((context, tickCounter) -> render(context, config));
    }

    private static void render(DrawContext context, UtilKitConfig config) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || client.options.hudHidden) {
            return;
        }

        UtilKitConfig.Hud hud = config.hud;
        if (!hud.enabled) {
            return;
        }

        List<Text> lines = new ArrayList<>();
        if (hud.showCoords) {
            lines.add(Text.literal(coordsLine(client)));
        }
        if (hud.showFps) {
            lines.add(Text.literal("FPS " + client.getCurrentFps()));
        }
        if (hud.showPing) {
            lines.add(Text.literal("Ping " + ping(client) + "ms"));
        }
        if (hud.showBiome) {
            lines.add(Text.literal("Biome " + biome(client)));
        }
        if (hud.showDurability) {
            String durability = durabilityLine(client);
            if (durability != null) {
                lines.add(Text.literal(durability));
            }
        }
        if (hud.customText != null && !hud.customText.isBlank()) {
            lines.add(Text.literal(hud.customText));
        }
        if (lines.isEmpty()) {
            return;
        }

        TextRenderer renderer = client.textRenderer;
        int width = 0;
        for (Text line : lines) {
            width = Math.max(width, renderer.getWidth(line));
        }
        int lineHeight = renderer.fontHeight + 1;
        int height = lines.size() * lineHeight;
        int x = hud.x;
        int y = hud.y;

        if (hud.background) {
            context.fill(x - 3, y - 3, x + width + 3, y + height + 2, 0x66000000);
        }
        int lineY = y;
        for (Text line : lines) {
            context.drawTextWithShadow(renderer, line, x, lineY, 0xFFFFFFFF);
            lineY += lineHeight;
        }
    }

    private static String coordsLine(MinecraftClient client) {
        return String.format("XYZ %d %d %d  %s",
                client.player.getBlockX(),
                client.player.getBlockY(),
                client.player.getBlockZ(),
                client.player.getHorizontalFacing().asString());
    }

    private static int ping(MinecraftClient client) {
        if (client.getNetworkHandler() == null || client.player == null) {
            return 0;
        }
        PlayerListEntry entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        return entry == null ? 0 : entry.getLatency();
    }

    private static String biome(MinecraftClient client) {
        if (client.world == null || client.player == null) {
            return "?";
        }
        RegistryEntry<Biome> entry = client.world.getBiome(client.player.getBlockPos());
        return entry.getKey()
                .map(key -> key.getValue().getPath().replace('_', ' '))
                .orElse("?");
    }

    private static String durabilityLine(MinecraftClient client) {
        ItemStack held = client.player.getMainHandStack();
        if (held == null || held.isEmpty() || !held.isDamageable()) {
            return null;
        }
        int max = held.getMaxDamage();
        int remaining = max - held.getDamage();
        return "Durability " + remaining + "/" + max;
    }
}
