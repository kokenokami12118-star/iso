package com.example.bulkbreak;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * クライアント側に保存する設定(config/bulk-break.json)。
 * ワールド(サーバー)に入るたびに、この設定をサーバーへ送って反映する。
 */
public final class BulkBreakConfig {
    public static boolean enabled = false;
    public static int max = BulkBreakMod.DEFAULT_MAX;
    /** 対象ブロックのID(例: minecraft:diamond_ore)。空なら全ブロックが対象。 */
    public static final Set<String> selected = new LinkedHashSet<>();

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static class Data {
        boolean enabled = false;
        int max = BulkBreakMod.DEFAULT_MAX;
        List<String> selected = new ArrayList<>();
    }

    private BulkBreakConfig() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("bulk-break.json");
    }

    public static void load() {
        Path p = path();
        if (!Files.exists(p)) return;
        try {
            Data d = GSON.fromJson(Files.readString(p, StandardCharsets.UTF_8), Data.class);
            if (d == null) return;
            enabled = d.enabled;
            max = Math.max(1, Math.min(BulkBreakMod.LIMIT_MAX, d.max));
            selected.clear();
            if (d.selected != null) selected.addAll(d.selected);
        } catch (Exception e) {
            // 読めないときは初期値のまま
        }
    }

    public static void save() {
        Data d = new Data();
        d.enabled = enabled;
        d.max = max;
        d.selected = new ArrayList<>(selected);
        try {
            Files.writeString(path(), GSON.toJson(d), StandardCharsets.UTF_8);
        } catch (IOException e) {
            // 保存できなくてもゲームは続ける
        }
    }
}
