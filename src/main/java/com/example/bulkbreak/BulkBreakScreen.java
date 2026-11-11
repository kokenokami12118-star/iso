package com.example.bulkbreak;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * 一括破壊の設定画面。右Shiftでも、Mod Menuの設定ボタンからも開く。
 * 設定はconfig/bulk-break.jsonに保存され、ワールドに入っているときはサーバーにも即反映される。
 * (ワールドの外で変えた設定は、次にワールドへ入ったときに反映される)
 */
public class BulkBreakScreen extends Screen {
    private final Screen parent;

    private Button toggleButton;
    private Button maxButton;
    private EditBox maxBox;

    public BulkBreakScreen(Screen parent) {
        super(Component.literal("一括破壊"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreenAndShow(parent);
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int top = this.height / 2 - 50;

        if (Minecraft.getInstance().player == null) {
            Button note = addRenderableWidget(Button.builder(
                    Component.literal("ワールドに入ると設定が反映されます"), b -> { })
                    .bounds(cx - 100, top - 28, 200, 20).build());
            note.active = false;
        }

        toggleButton = addRenderableWidget(Button.builder(Component.empty(), b -> {
            BulkBreakConfig.enabled = !BulkBreakConfig.enabled;
            BulkBreakConfig.save();
            BulkBreakClient.send(BulkBreakConfig.enabled ? "bulkbreak on" : "bulkbreak off");
            refresh();
        }).bounds(cx - 100, top, 200, 24).build());

        maxBox = new EditBox(this.font, cx - 100, top + 36, 70, 20, Component.literal("上限"));
        maxBox.setMaxLength(3);
        maxBox.setValue(String.valueOf(BulkBreakConfig.max));
        addRenderableWidget(maxBox);

        maxButton = addRenderableWidget(Button.builder(Component.empty(), b -> {
            BulkBreakConfig.max = readMax();
            BulkBreakConfig.save();
            maxBox.setValue(String.valueOf(BulkBreakConfig.max));
            BulkBreakClient.send("bulkbreak max " + BulkBreakConfig.max);
            refresh();
        }).bounds(cx - 26, top + 36, 126, 20).build());

        addRenderableWidget(Button.builder(Component.literal("壊すブロックを選ぶ"), b ->
                Minecraft.getInstance().setScreenAndShow(new BlockSelectScreen(parent)))
                .bounds(cx - 100, top + 68, 200, 20).build());

        addRenderableWidget(Button.builder(Component.literal("閉じる"), b -> onClose())
                .bounds(cx - 100, top + 94, 200, 20).build());

        refresh();
    }

    private int readMax() {
        try {
            int v = Integer.parseInt(maxBox.getValue());
            return Math.max(1, Math.min(BulkBreakMod.LIMIT_MAX, v));
        } catch (NumberFormatException e) {
            return BulkBreakMod.DEFAULT_MAX;
        }
    }

    private void refresh() {
        toggleButton.setMessage(Component.literal("一括破壊: " + (BulkBreakConfig.enabled ? "ON" : "OFF")
                + "  (クリックで切替)"));
        maxButton.setMessage(Component.literal("上限を設定 (今: " + BulkBreakConfig.max + "個)"));
    }
}
