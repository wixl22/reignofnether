package com.solegendary.reignofnether.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import com.solegendary.reignofnether.ReignOfNether;
import com.solegendary.reignofnether.healthbars.HealthBarClientEvents;
import com.solegendary.reignofnether.orthoview.OrthoviewClientEvents;
import com.solegendary.reignofnether.util.MiscUtil;
import com.solegendary.reignofnether.util.MyRenderer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Supplier;

/**
 * Class for creating buttons that consist of an icon inside of a frame which is selectable
 * All functionality that occurs on click/hover/etc. is enforced by HudClientEvents
 */

public class Button {

    public String name;
    public int x; // top left
    public int y;
    int iconSize;
    public static int iconFrameSize = 22;
    public static int iconFrameSelectedSize = 24;

    ResourceLocation iconResource;

    public KeyMapping hotkey = null; // for action/ability buttons
    public LivingEntity entity = null; // for selected unit buttons

    /** https://stackoverflow.com/questions/29945627/java-8-lambda-void-argument
     * Supplier       ()    -> x
     * Consumer       x     -> ()
     * Runnable       ()    -> ()
     * Predicate      x     -> boolean
     */
    public Supplier<Boolean> isSelected; // controls selected frame rendering
    public Supplier<Boolean> isActive; // special highlighting for an on-state (eg. auto-cast/auto-producing)
    public Supplier<Boolean> isEnabled; // is the button allowed to be used right now? (eg. off cooldown)
    public Runnable onUse;

    // used for cooldown indication, productionItem progress, etc.
    // @ 0.0, appears clear and normal
    // @ 0.5, bottom half is greyed out
    // @ 1.0, whole button is greyed out
    public float greyPercent = 0.0f;

    Minecraft MC = Minecraft.getInstance();

    // constructor for ability/action/production buttons
    public Button(String name, int iconSize,
                  String iconResourcePath, KeyMapping hotkey,
                  Supplier<Boolean> isSelected, Supplier<Boolean> isActive, Supplier<Boolean> isEnabled, Runnable onClick) {
        this.name = name;
        this.iconResource = new ResourceLocation(ReignOfNether.MOD_ID, iconResourcePath);
        this.iconSize = iconSize;
        this.hotkey = hotkey;
        this.isSelected = isSelected;
        this.isActive = isActive;
        this.isEnabled = isEnabled;
        this.onUse = onClick;
    }

    // constructor for unit selection buttons
    public Button(String name, int iconSize,
                  String iconResourcePath, LivingEntity entity,
                  Supplier<Boolean> isSelected, Supplier<Boolean> isActive, Supplier<Boolean> isEnabled, Runnable onClick) {
        this.name = name;
        this.iconResource = new ResourceLocation(ReignOfNether.MOD_ID, iconResourcePath);
        this.iconSize = iconSize;
        this.entity = entity;
        this.isSelected = isSelected;
        this.isActive = isActive;
        this.isEnabled = isEnabled;
        this.onUse = onClick;
    }

    public void renderHealthBar(PoseStack poseStack) {
        HealthBarClientEvents.renderForEntity(poseStack, entity,
                x + ((float) iconFrameSize / 2), y - 5,
                iconFrameSize - 1,
                HealthBarClientEvents.RenderMode.GUI_ICON);
    }

    public void render(PoseStack poseStack, int x, int y, int mouseX, int mouseY) {
        this.x = x;
        this.y = y;
        MyRenderer.renderIconFrameWithBg(poseStack, x, y, iconFrameSize, 0x64000000);

        // item/unit icon
        MyRenderer.renderIcon(
                poseStack,
                iconResource,
                x+4, y+4,
                iconSize
        );
        // hotkey letter
        if (this.hotkey != null) {
            GuiComponent.drawCenteredString(poseStack, MC.font,
                    hotkey.getKey().getDisplayName().getString().toUpperCase(),
                    x + iconSize + 4,
                    y + iconSize - 1,
                    0xFFFFFF);
        }

        // user is holding click or hotkey down over the button and render frame if so
        if (isEnabled.get() && (isSelected.get() || (hotkey != null && hotkey.isDown()) || (isMouseOver(mouseX, mouseY) && MiscUtil.isLeftClickDown(MC)))) {
            ResourceLocation iconFrameSelectedResource = new ResourceLocation(ReignOfNether.MOD_ID, "textures/hud/icon_frame_selected.png");
            MyRenderer.renderIcon(
                    poseStack,
                    iconFrameSelectedResource,
                    x-1,y-1,
                    iconFrameSelectedSize
            );
        }
        // light up on hover
        if (isEnabled.get() && isMouseOver(mouseX, mouseY)) {
            GuiComponent.fill(poseStack, // x1,y1, x2,y2,
                    x, y,
                    x + iconFrameSize,
                    y + iconFrameSize,
                    0x32FFFFFF); //ARGB(hex); note that alpha ranges between ~0-16, not 0-255
        }

        if (greyPercent > 0) {
            int greyHeightPx = Math.round(greyPercent * iconFrameSize);
            GuiComponent.fill(poseStack, // x1,y1, x2,y2,
                    x, y + greyHeightPx,
                    x + iconFrameSize,
                    y + iconFrameSize,
                    0x80000000); //ARGB(hex); note that alpha ranges between ~0-16, not 0-255
        }
    }
    public boolean isMouseOver(int mouseX, int mouseY) {
        return (mouseX >= x &&
                mouseY >= y &&
                mouseX < x + iconFrameSize &&
                mouseY < y + iconFrameSize
        );
    }

    // must be done from mouse press event
    public void checkLeftClicked(int mouseX, int mouseY) {
        if (!OrthoviewClientEvents.isEnabled() || !isEnabled.get())
            return;

        if (isMouseOver(mouseX, mouseY)) {
            if (this.entity != null)
                System.out.println("Clicked on button - entity id: " + entity.getId());
            else if (this.hotkey != null)
                System.out.println("Clicked on button - hotkey: " + hotkey.getKey().getDisplayName());

            if (MC.player != null)
                MC.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.2f, 1.0f);
            this.onUse.run();
        }
    }

    // must be done from mouse press event
    public void checkRightClicked(int mouseX, int mouseY) {
        if (!OrthoviewClientEvents.isEnabled() || !isEnabled.get())
            return;

        if (isMouseOver(mouseX, mouseY)) {
            return; // TODO: activate autocasts
        }
    }

    // must be done from key press event
    public void checkPressed(int key) {
        if (!OrthoviewClientEvents.isEnabled() || !isEnabled.get())
            return;

        if (hotkey != null && hotkey.getKey().getValue() == key) {
            if (MC.player != null)
                MC.player.playSound(SoundEvents.UI_BUTTON_CLICK, 0.2f, 1.0f);
            this.onUse.run();
        }
    }
}
