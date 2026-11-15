package dev.bente.utilkit;

import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;

/** Small wrapper so key polling is in one place. */
final class KeyState {

    private KeyState() {
    }

    static boolean isPressed(Window window, int keyCode) {
        return InputUtil.isKeyPressed(window, keyCode);
    }
}
