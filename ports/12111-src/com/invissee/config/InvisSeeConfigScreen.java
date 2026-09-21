package com.invissee.config;

import com.invissee.InvisSeeConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class InvisSeeConfigScreen extends Screen {
    private final Screen parent;
    private EditBox rangeEdit;
    private EditBox lineWidthEdit;

    public InvisSeeConfigScreen(Screen parent) {
        super(Component.literal("Vanilla entity indicator - AFKZ Studio"));
        this.parent = parent;
    }

    public static Screen create(Screen parent) {
        return new InvisSeeConfigScreen(parent);
    }

    private int otherColorY;
    private int playerColorY;

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int w = 200;
        int h = 20;
        int gap = 22;
        // Start lower to fit within 240 height (title at 10, subtitle at 22)
        int y = 35;

        // Enabled
        this.addRenderableWidget(Button.builder(Component.literal("Enabled: " + (InvisSeeConfig.enabled ? "ON" : "OFF")), btn -> {
            InvisSeeConfig.enabled = !InvisSeeConfig.enabled;
            btn.setMessage(Component.literal("Enabled: " + (InvisSeeConfig.enabled ? "ON" : "OFF")));
        }).bounds(centerX - w/2, y, w, h).build());
        y += gap;

        // Show Invisible
        this.addRenderableWidget(Button.builder(Component.literal("Show Invisible: " + (InvisSeeConfig.showInvisible ? "ON" : "OFF")), btn -> {
            InvisSeeConfig.showInvisible = !InvisSeeConfig.showInvisible;
            btn.setMessage(Component.literal("Show Invisible: " + (InvisSeeConfig.showInvisible ? "ON" : "OFF")));
        }).bounds(centerX - w/2, y, w, h).build());
        y += gap;

        // Only Players
        this.addRenderableWidget(Button.builder(Component.literal("Only Players: " + (InvisSeeConfig.onlyPlayers ? "ON" : "OFF")), btn -> {
            InvisSeeConfig.onlyPlayers = !InvisSeeConfig.onlyPlayers;
            btn.setMessage(Component.literal("Only Players: " + (InvisSeeConfig.onlyPlayers ? "ON" : "OFF")));
        }).bounds(centerX - w/2, y, w, h).build());
        y += gap;

        // Entity Color - opens Lunar-style color picker
        otherColorY = y;
        this.addRenderableWidget(Button.builder(Component.literal("Other Entities: #" + String.format("%06X", InvisSeeConfig.entityColor & 0xFFFFFF)), btn -> {
            this.minecraft.setScreen(new ColorPickerScreen(this, "Other Entities", InvisSeeConfig.entityColor, picked -> {
                InvisSeeConfig.entityColor = picked;
                InvisSeeConfig.color = picked;
            }));
        }).bounds(centerX - w/2, y, w, h).build());
        y += gap;

        // Player Color - opens Lunar-style color picker
        playerColorY = y;
        this.addRenderableWidget(Button.builder(Component.literal("Player Color: #" + String.format("%06X", InvisSeeConfig.playerColor & 0xFFFFFF)), btn -> {
            this.minecraft.setScreen(new ColorPickerScreen(this, "Player Color", InvisSeeConfig.playerColor, picked -> {
                InvisSeeConfig.playerColor = picked;
            }));
        }).bounds(centerX - w/2, y, w, h).build());
        y += gap;

        // Range with label
        rangeEdit = new EditBox(this.font, centerX - w/2, y, w, h, Component.literal("Range"));
        rangeEdit.setValue(String.valueOf(InvisSeeConfig.range));
        rangeEdit.setHint(Component.literal("Range 16-256"));
        this.addRenderableWidget(rangeEdit);
        y += gap;
        lineWidthEdit = new EditBox(this.font, centerX - w/2, y, w, h, Component.literal("Line Width"));
        lineWidthEdit.setValue(String.valueOf(InvisSeeConfig.lineWidth));
        lineWidthEdit.setHint(Component.literal("Line Width 0.5-5.0"));
        this.addRenderableWidget(lineWidthEdit);
        y += gap;

        // Done and Reset side by side to save vertical space
        int halfW = 98;
        this.addRenderableWidget(Button.builder(Component.literal("Done - Save"), btn -> {
            try {
                InvisSeeConfig.range = Math.max(16, Math.min(256, Integer.parseInt(rangeEdit.getValue().trim())));
            } catch (Exception ignored) {}
            try {
                InvisSeeConfig.lineWidth = Math.max(0.5f, Math.min(5.0f, Float.parseFloat(lineWidthEdit.getValue().trim())));
            } catch (Exception ignored) {}
            InvisSeeConfig.save();
            this.minecraft.setScreen(parent);
        }).bounds(centerX - halfW - 2, y, halfW, h).build());

        this.addRenderableWidget(Button.builder(Component.literal("Reset Defaults"), btn -> {
            InvisSeeConfig.entityColor = 0xFF00FF00;
            InvisSeeConfig.color = 0xFF00FF00;
            InvisSeeConfig.playerColor = 0xFFFF0000;
            InvisSeeConfig.range = 128;
            InvisSeeConfig.lineWidth = 1.5f;
            InvisSeeConfig.showInvisible = true;
            InvisSeeConfig.onlyPlayers = false;
            InvisSeeConfig.enabled = true;
            InvisSeeConfig.save();
            this.minecraft.setScreen(new InvisSeeConfigScreen(parent));
        }).bounds(centerX + 2, y, halfW, h).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width/2, 6, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("Other=mobs, Player=players - O to open, G to toggle"), this.width/2, 18, 0xAAAAAA);
        // Preview colors - drawn inside button's right edge, aligned with button Y
        int cx = this.width / 2;
        // Other entities preview (slightly inset from right edge)
        graphics.fill(cx + 78, otherColorY + 4, cx + 96, otherColorY + 16, InvisSeeConfig.entityColor);
        graphics.renderOutline(cx + 78, otherColorY + 4, 18, 12, 0xFF000000);
        // Player preview
        graphics.fill(cx + 78, playerColorY + 4, cx + 96, playerColorY + 16, InvisSeeConfig.playerColor);
        graphics.renderOutline(cx + 78, playerColorY + 4, 18, 12, 0xFF000000);
        // Labels for edit boxes
        graphics.drawString(this.font, "Range:", this.width/2 - 100, rangeEdit.getY() - 10, 0xAAAAAA);
        graphics.drawString(this.font, "Line Width:", this.width/2 - 100, lineWidthEdit.getY() - 10, 0xAAAAAA);
    }

    @Override
    public void onClose() {
        InvisSeeConfig.save();
        super.onClose();
    }
}
