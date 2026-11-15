package dev.bente.utilkit;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/** Local-only chat feedback, never sent to the server. */
public final class Messages {

    private Messages() {
    }

    public static void send(MinecraftClient client, String message) {
        if (client.player == null) {
            return;
        }
        client.player.sendMessage(Text.literal("[UtilKit] ").formatted(Formatting.AQUA)
                .append(Text.literal(message).formatted(Formatting.GRAY)), false);
    }
}
