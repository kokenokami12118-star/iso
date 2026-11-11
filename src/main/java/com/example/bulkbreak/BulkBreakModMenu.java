package com.example.bulkbreak;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Mod Menu の「設定」ボタンから開く画面。 */
public class BulkBreakModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new BulkBreakScreen(parent);
    }
}
