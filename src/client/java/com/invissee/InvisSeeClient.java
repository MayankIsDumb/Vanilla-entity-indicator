package com.invissee;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.resources.Identifier;

public class InvisSeeClient implements ClientModInitializer {
    public static final String MOD_ID = "invissee";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static KeyMapping toggleKey;
    private static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("[InvisSee] Initializing - Visualize Entity Supporting Block (ported from vanilla Dec 9 2025 debug)");
        InvisSeeConfig.load();

        // Register renderer
        VisualizeEntitySupportingBlockRenderer.register();

        // Keybind: default G to toggle, O to open config
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.invissee.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KeyMapping.Category.MISC
        ));
        configKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.invissee.config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                InvisSeeConfig.enabled = !InvisSeeConfig.enabled;
                InvisSeeConfig.save();
                if (client.player != null) {
                    client.player.displayClientMessage(
                            Component.literal("[InvisSee] visualize_entity_supporting_block: " + (InvisSeeConfig.enabled ? "§aON §7(F3+F6 port)" : "§cOFF")),
                            true
                    );
                }
                LOGGER.info("[InvisSee] Toggled {}", InvisSeeConfig.enabled ? "ON" : "OFF");
            }
            while (configKey.consumeClick()) {
                client.setScreen(com.invissee.config.InvisSeeConfigScreen.create(client.screen));
            }
        });

        // Also listen for F3+F6 combo to mimic vanilla debug exactly - 1.21.11 uses Window object
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) return;
            var window = client.getWindow();
            boolean f3Down = InputConstants.isKeyDown(window, GLFW.GLFW_KEY_F3);
            if (f3Down && InputConstants.isKeyDown(window, GLFW.GLFW_KEY_F6)) {
                if (F3F6Debouncer.canTrigger()) {
                    InvisSeeConfig.enabled = !InvisSeeConfig.enabled;
                    InvisSeeConfig.save();
                    if (client.player != null) {
                        client.player.displayClientMessage(
                                Component.literal("[InvisSee] F3+F6 -> visualize_entity_supporting_block: " + (InvisSeeConfig.enabled ? "§aON" : "§cOFF")),
                                true
                        );
                    }
                }
            } else {
                F3F6Debouncer.reset();
            }
        });
    }

    static class F3F6Debouncer {
        private static long lastTrigger = 0;
        private static final long COOLDOWN = 300; // ms
        static boolean canTrigger() {
            long now = System.currentTimeMillis();
            if (now - lastTrigger > COOLDOWN) {
                lastTrigger = now;
                return true;
            }
            return false;
        }
        static void reset() {
            // allow next press after release, but keep cooldown
        }
    }
}
