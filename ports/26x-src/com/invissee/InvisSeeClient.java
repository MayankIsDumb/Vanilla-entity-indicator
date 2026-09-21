package com.invissee;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InvisSeeClient implements ClientModInitializer {
    public static final String MOD_ID = "vanilla-entity-indicator";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyMapping toggleKey;
    private static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[vanilla-entity-indicator] Initializing - Visualize Entity Supporting Block (gizmo renderer)");
        InvisSeeConfig.load();

        VisualizeEntitySupportingBlockRenderer.register();

        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.invissee.toggle",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_G,
                KeyMapping.Category.MISC
        ));
        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.invissee.config",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_O,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                InvisSeeConfig.enabled = !InvisSeeConfig.enabled;
                InvisSeeConfig.save();
                if (client.player != null) {
                    client.player.sendOverlayMessage(
                            Component.literal("[Indicator] enabled: " + (InvisSeeConfig.enabled ? "ON" : "OFF"))
                    );
                }
                LOGGER.info("[vanilla-entity-indicator] Toggled {}", InvisSeeConfig.enabled ? "ON" : "OFF");
            }
            while (configKey.consumeClick()) {
                client.setScreenAndShow(com.invissee.config.InvisSeeConfigScreen.create(null));
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) return;
            boolean f3Down = InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_F3);
            if (f3Down && InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_F6)) {
                if (F3F6Debouncer.canTrigger()) {
                    InvisSeeConfig.enabled = !InvisSeeConfig.enabled;
                    InvisSeeConfig.save();
                    if (client.player != null) {
                        client.player.sendOverlayMessage(
                                Component.literal("[Indicator] F3+F6: " + (InvisSeeConfig.enabled ? "ON" : "OFF"))
                        );
                    }
                }
            }
        });
    }

    static class F3F6Debouncer {
        private static long lastTrigger = 0;
        private static final long COOLDOWN = 300;
        static boolean canTrigger() {
            long now = System.currentTimeMillis();
            if (now - lastTrigger > COOLDOWN) {
                lastTrigger = now;
                return true;
            }
            return false;
        }
    }
}
