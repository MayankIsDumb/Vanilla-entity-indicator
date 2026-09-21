package com.invissee.config;

import com.invissee.InvisSeeConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.awt.Color;
import java.util.function.IntConsumer;

/**
 * Color picker inspired by Lunar Client's color_picker setting:
 * saturation/brightness square (HSBtoRGB per cell) + hue slider +
 * hex field + preset and recent swatches.
 */
public class ColorPickerScreen extends Screen {
    // Lunar Client default swatches (first 12 of its preset list)
    private static final int[] PRESETS = {
            -5636096, -43691, -22016, -171, -16733696, -11141291,
            -11141121, -16733526, -16777046, -11184641, -43521, -5635926
    };

    private final Screen parent;
    private final String label;
    private final IntConsumer onPick;

    private float hue;
    private float sat;
    private float bright;
    private EditBox hexEdit;

    private int sqX, sqY, sqW, sqH, hueX, hueW, rightX, rightW, previewY, hexY, presetY, recentY, btnY;

    private enum Drag { NONE, SV, HUE }
    private Drag dragging = Drag.NONE;

    public ColorPickerScreen(Screen parent, String label, int initialArgb, IntConsumer onPick) {
        super(Component.literal("Pick color - " + label));
        this.parent = parent;
        this.label = label;
        this.onPick = onPick;
        float[] hsb = Color.RGBtoHSB((initialArgb >> 16) & 0xFF, (initialArgb >> 8) & 0xFF, initialArgb & 0xFF, null);
        this.hue = hsb[0];
        this.sat = hsb[1];
        this.bright = hsb[2];
    }

    public int currentArgb() {
        return 0xFF000000 | Color.HSBtoRGB(hue, sat, bright);
    }

    private static String hex(int argb) {
        return String.format("%06X", argb & 0xFFFFFF);
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        sqW = 190;
        sqH = 120;
        hueW = 16;
        rightW = 104;
        sqX = cx - 168;
        sqY = 38;
        hueX = sqX + sqW + 8;
        rightX = hueX + hueW + 12;

        previewY = sqY;
        hexY = previewY + 32;
        presetY = hexY + 34;
        recentY = presetY + 56;
        btnY = sqY + sqH + 44;

        hexEdit = new EditBox(this.font, rightX, hexY, rightW, 20, Component.literal("Hex"));
        hexEdit.setValue(hex(currentArgb()));
        hexEdit.setHint(Component.literal("#RRGGBB"));
        hexEdit.setResponder(text -> {
            String clean = text.replaceAll("[^0-9A-Fa-f]", "");
            if (clean.length() == 6 || clean.length() == 8) {
                try {
                    int v = (int) Long.parseLong(clean.length() == 8 ? clean.substring(2) : clean, 16);
                    float[] hsb = Color.RGBtoHSB((v >> 16) & 0xFF, (v >> 8) & 0xFF, v & 0xFF, null);
                    hue = hsb[0];
                    sat = hsb[1];
                    bright = hsb[2];
                } catch (NumberFormatException ignored) {}
            }
        });
        this.addRenderableWidget(hexEdit);

        int halfW = 98;
        this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> apply(true))
                .bounds(cx - halfW - 2, btnY, halfW, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> apply(false))
                .bounds(cx + 2, btnY, halfW, 20).build());
    }

    private void apply(boolean save) {
        if (save) {
            int argb = currentArgb();
            InvisSeeConfig.pushRecent(argb);
            InvisSeeConfig.save();
            onPick.accept(argb);
        }
        this.minecraft.setScreen(parent);
    }

    private boolean inRect(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void pickSv(double mx, double my) {
        sat = (float) Math.max(0.0, Math.min(1.0, (mx - sqX) / sqW));
        bright = (float) Math.max(0.0, Math.min(1.0, 1.0 - (my - sqY) / sqH));
        if (!hexEdit.isFocused()) hexEdit.setValue(hex(currentArgb()));
    }

    private void pickHue(double my) {
        hue = (float) Math.max(0.0, Math.min(1.0, (my - sqY) / sqH));
        if (!hexEdit.isFocused()) hexEdit.setValue(hex(currentArgb()));
    }

    private void setFromArgb(int argb) {
        float[] hsb = Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, null);
        hue = hsb[0];
        sat = hsb[1];
        bright = hsb[2];
        hexEdit.setValue(hex(argb));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean bl) {
        // Left click is button 0 on GLFW versions and button 1 on SDL
        // versions (26.3+) - accept both, like vanilla's click handling.
        if (event.button() == 0 || event.button() == 1) {
            double mx = event.x();
            double my = event.y();
            if (inRect(mx, my, sqX, sqY, sqW, sqH)) {
                dragging = Drag.SV;
                pickSv(mx, my);
                return true;
            }
            if (inRect(mx, my, hueX, sqY, hueW, sqH)) {
                dragging = Drag.HUE;
                pickHue(my);
                return true;
            }
            int hit = swatchAt(mx, my);
            if (hit != 0) {
                dragging = Drag.NONE;
                setFromArgb(hit);
                return true;
            }
        }
        dragging = Drag.NONE;
        return super.mouseClicked(event, bl);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (dragging == Drag.SV) {
            pickSv(event.x(), event.y());
            return true;
        }
        if (dragging == Drag.HUE) {
            pickHue(event.y());
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = Drag.NONE;
        return super.mouseReleased(event);
    }

    @Override
    public void tick() {
        super.tick();
        // Drag follow-up: the press armed `dragging` in mouseClicked
        // (click delivery is proven - buttons work through it). Poll the
        // cursor directly instead of trusting drag events or button-state
        // APIs, which vary by version (26.3 SDL backend).
        if (dragging == Drag.NONE) return;
        if (this.minecraft == null) return;
        try {
            double mx = this.minecraft.mouseHandler.getScaledXPos(this.minecraft.getWindow());
            double my = this.minecraft.mouseHandler.getScaledYPos(this.minecraft.getWindow());
            if (inRect(mx, my, sqX, sqY, sqW, sqH)) {
                dragging = Drag.SV;
                pickSv(mx, my);
            } else if (inRect(mx, my, hueX, sqY, hueW, sqH)) {
                dragging = Drag.HUE;
                pickHue(my);
            }
        } catch (Throwable ignored) {}
    }

    /** Returns ARGB of the preset/recent swatch under the mouse, or 0. */
    private int swatchAt(double mx, double my) {
        // presets: 6 cols x 2 rows of 15px
        for (int i = 0; i < PRESETS.length; i++) {
            int x = rightX + (i % 6) * 17;
            int y = presetY + 12 + (i / 6) * 17;
            if (inRect(mx, my, x, y, 15, 15)) return PRESETS[i] | 0xFF000000;
        }
        // recents: up to 6 in one row
        for (int i = 0; i < InvisSeeConfig.recentColors.size() && i < 6; i++) {
            int x = rightX + i * 17;
            if (inRect(mx, my, x, recentY + 12, 15, 15)) return InvisSeeConfig.recentColors.get(i);
        }
        return 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
        graphics.drawCenteredString(this.font, Component.literal("drag the square / hue bar, type hex, or click a swatch"), this.width / 2, 22, 0xAAAAAA);

        // SV square: 32 cols x 20 rows, like Lunar's 114x60 HSB image
        int cols = 32;
        int rows = 20;
        for (int c = 0; c < cols; c++) {
            float s = (c + 0.5f) / cols;
            for (int r = 0; r < rows; r++) {
                float b = 1.0f - (r + 0.5f) / rows;
                int rgb = 0xFF000000 | Color.HSBtoRGB(hue, s, b);
                graphics.fill(sqX + sqW * c / cols, sqY + sqH * r / rows,
                        sqX + sqW * (c + 1) / cols, sqY + sqH * (r + 1) / rows, rgb);
            }
        }
        graphics.renderOutline(sqX, sqY, sqW, sqH, 0xFF000000);
        // crosshair at current sat/bright
        int cxm = sqX + (int) (sqW * sat);
        int cym = sqY + (int) (sqH * (1.0f - bright));
        graphics.renderOutline(cxm - 3, cym - 3, 7, 7, 0xFF000000);
        graphics.renderOutline(cxm - 2, cym - 2, 5, 5, 0xFFFFFFFF);

        // hue bar: 12 rainbow segments
        for (int r = 0; r < 12; r++) {
            int rgb = 0xFF000000 | Color.HSBtoRGB(r / 12.0f, 1.0f, 1.0f);
            graphics.fill(hueX, sqY + sqH * r / 12, hueX + hueW, sqY + sqH * (r + 1) / 12, rgb);
        }
        graphics.renderOutline(hueX, sqY, hueW, sqH, 0xFF000000);
        int hym = sqY + (int) (sqH * hue);
        graphics.fill(hueX - 2, hym - 1, hueX + hueW + 2, hym + 1, 0xFFFFFFFF);

        // preview + labels
        int argb = currentArgb();
        graphics.fill(rightX, previewY, rightX + rightW, previewY + 28, argb);
        graphics.renderOutline(rightX, previewY, rightW, 28, 0xFF000000);
        graphics.drawString(this.font, "Hex:", rightX, hexY - 10, 0xAAAAAA);
        graphics.drawString(this.font, "Presets:", rightX, presetY, 0xAAAAAA);
        for (int i = 0; i < PRESETS.length; i++) {
            int x = rightX + (i % 6) * 17;
            int y = presetY + 12 + (i / 6) * 17;
            graphics.fill(x, y, x + 15, y + 15, PRESETS[i] | 0xFF000000);
            graphics.renderOutline(x, y, 15, 15, 0xFF000000);
        }
        graphics.drawString(this.font, "Recent:", rightX, recentY, 0xAAAAAA);
        for (int i = 0; i < InvisSeeConfig.recentColors.size() && i < 6; i++) {
            int x = rightX + i * 17;
            graphics.fill(x, recentY + 12, x + 15, recentY + 27, InvisSeeConfig.recentColors.get(i));
            graphics.renderOutline(x, recentY + 12, 15, 15, 0xFF000000);
        }
    }
}
