package dev.bente.utilkit;

import net.fabricmc.loader.api.FabricLoader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Plain-JSON config stored at {@code .minecraft/config/utilkit.json}.
 */
public final class UtilKitConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("utilkit.json");
    }

    public Hud hud = new Hud();
    public List<Macro> macros = new ArrayList<>();

    public static final class Hud {
        public boolean enabled = true;
        public int x = 6;
        public int y = 6;
        public boolean background = true;
        public boolean showCoords = true;
        public boolean showFps = true;
        public boolean showPing = true;
        public boolean showBiome = true;
        public boolean showDurability = true;
        public String customText = "";
    }

    public static final class Macro {
        /** GLFW key code, -1 when unset. */
        public int keyCode = -1;
        /** Action string, e.g. {@code chat:/spawn} or {@code toggle:hud}. */
        public String action = "";

        public Macro() {
        }

        public Macro(int keyCode, String action) {
            this.keyCode = keyCode;
            this.action = action;
        }
    }

    public static UtilKitConfig load() {
        Path file = path();
        if (!Files.exists(file)) {
            UtilKitConfig fresh = new UtilKitConfig();
            fresh.save();
            return fresh;
        }
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            UtilKitConfig loaded = GSON.fromJson(reader, UtilKitConfig.class);
            if (loaded == null) {
                return new UtilKitConfig();
            }
            if (loaded.hud == null) {
                loaded.hud = new Hud();
            }
            if (loaded.macros == null) {
                loaded.macros = new ArrayList<>();
            }
            return loaded;
        } catch (IOException | RuntimeException e) {
            UtilKitClient.LOGGER.warn("Could not read config, using defaults", e);
            return new UtilKitConfig();
        }
    }

    public void save() {
        try {
            Path file = path();
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            UtilKitClient.LOGGER.warn("Could not save config", e);
        }
    }
}
