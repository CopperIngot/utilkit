package dev.bente.utilkit;

import net.minecraft.client.util.InputUtil;

import java.util.Locale;

/** Translates between key names typed by the user and GLFW key codes. */
public final class KeyNames {

    private KeyNames() {
    }

    /**
     * Accepts {@code f}, {@code f6}, {@code space}, or a full translation key such as
     * {@code key.keyboard.f}. Returns {@code null} for anything that is not a keyboard key.
     */
    public static Integer parse(String raw) {
        if (raw == null) {
            return null;
        }
        String name = raw.toLowerCase(Locale.ROOT).trim();
        if (name.isEmpty()) {
            return null;
        }
        if (!name.startsWith("key.")) {
            name = "key.keyboard." + name;
        }
        try {
            InputUtil.Key key = InputUtil.fromTranslationKey(name);
            if (key.getCategory() != InputUtil.Type.KEYSYM) {
                return null;
            }
            return key.getCode();
        } catch (RuntimeException e) {
            return null;
        }
    }

    public static String format(int code) {
        if (code < 0) {
            return "unbound";
        }
        try {
            return InputUtil.Type.KEYSYM.createFromCode(code).getLocalizedText().getString();
        } catch (RuntimeException e) {
            return "key " + code;
        }
    }
}
