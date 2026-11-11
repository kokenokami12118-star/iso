package com.example.bulkbreak;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.ArrayDeque;

/** 右Shiftで設定画面を開く。ワールドに入ったら保存済みの設定をサーバーへ送る。 */
public class BulkBreakClient implements ClientModInitializer {
    private static KeyMapping openKey;

    private static final ArrayDeque<String> PENDING = new ArrayDeque<>();
    private static int cooldown = 0;

    /** ワールド内ならコマンドを送る(ワールド外では何もしない)。 */
    public static void send(String cmd) {
        var player = Minecraft.getInstance().player;
        if (player != null) {
            player.connection.sendCommand(cmd);
        }
    }

    /** 保存済みの設定を、少しずつ(スパム扱いされないように間隔をあけて)サーバーへ送る準備。 */
    private static void queueSync() {
        PENDING.clear();
        cooldown = 20;
        if (!BulkBreakConfig.enabled
                && BulkBreakConfig.selected.isEmpty()
                && BulkBreakConfig.max == BulkBreakMod.DEFAULT_MAX) {
            return; // 初期設定のままなら何も送らない
        }
        PENDING.add("bulkbreak sync " + BulkBreakConfig.enabled + " " + BulkBreakConfig.max);
        StringBuilder sb = new StringBuilder();
        for (String id : BulkBreakConfig.selected) {
            if (sb.length() > 0 && sb.length() + id.length() + 1 > 200) {
                PENDING.add("bulkbreak addlist " + sb);
                sb.setLength(0);
            }
            if (sb.length() > 0) sb.append(',');
            sb.append(id);
        }
        if (sb.length() > 0) {
            PENDING.add("bulkbreak addlist " + sb);
        }
    }

    @Override
    public void onInitializeClient() {
        BulkBreakConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("bulk-break", "main"));

        openKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.bulk-break.open",
                InputConstants.Type.KEYBOARD,
                InputConstants.KEY_RSHIFT,
                category));

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> queueSync());

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openKey.consumeClick()) {
                if (mc.player != null) {
                    mc.setScreenAndShow(new BulkBreakScreen(null));
                }
            }
            if (!PENDING.isEmpty() && mc.player != null && --cooldown <= 0) {
                send(PENDING.poll());
                cooldown = 5;
            }
        });
    }
}
