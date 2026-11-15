package dev.bente.utilkit;

import net.minecraft.client.util.InputUtil;

/** Small wrapper so key polling is in one place. */
final class KeyState {

    private KeyState() {
    }

    static boolean isPressed(long windowHandle, int keyCode) {
        return InputUtil.isKeyPressed(windowHandle, keyCode);
    }
}
