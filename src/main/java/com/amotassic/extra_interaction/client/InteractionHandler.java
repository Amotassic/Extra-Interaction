package com.amotassic.extra_interaction.client;

import com.amotassic.extra_interaction.ExtraInteraction;
import com.amotassic.extra_interaction.interaction.InteractionRec;
import com.amotassic.extra_interaction.interaction.Interactions;
import com.amotassic.extra_interaction.network.SendInteraction;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.joml.Matrix3x2f;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InteractionHandler {
    public static final Minecraft minecraft = Minecraft.getInstance();
    public static final KeyMapping interactKey = new KeyMapping("key.extra_interact.interact", GLFW.GLFW_KEY_F, KeyMapping.Category.GAMEPLAY);
    static final Identifier option = ExtraInteraction.id("textures/option.png");
    static final Identifier lock = ExtraInteraction.id("textures/lock.png");
    static int selectIndex = 0;
    static List<InteractionRec> interactions = new ArrayList<>();

    public static boolean hasOptions() {return !interactions.isEmpty();}

    public static void onHudRender(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        interactions.clear();
        if (minecraft.level == null || minecraft.player == null || minecraft.screen != null) return;
        HitResult result = minecraft.hitResult;
        if (result == null || result.getType() == HitResult.Type.MISS) return;

        Object target = null;
        if (result instanceof BlockHitResult blockHitResult) target = blockHitResult.getBlockPos();
        else if (result instanceof EntityHitResult entityHitResult) target = entityHitResult.getEntity();
        if (target != null) Interactions.getInteractions(minecraft.player, target, interactions, hasControlDown());

        if (interactions.isEmpty()) return;
        int size = interactions.size();
        if (selectIndex >= size) selectIndex = 0;
        int x = (int) (screenWidth() * Config.getXPos());
        int y = (int) (screenHeight() * Config.getYPos());
        final int width = Config.optionWidth.get();
        final int height = Config.optionHeight.get(); final int halfHeight = height / 2;
        final int showCount = Config.showCount.get();
        final int startY = y - halfHeight;
        final int endY = startY + height * (showCount + 1);
        guiGraphics.enableScissor(0, startY, screenWidth(), endY);
        var font = minecraft.font;
        if (selectIndex >= showCount) y -= (selectIndex - showCount + 1) * height; // 将选项位置整体向上移动

        for (int i = 0; i < interactions.size(); i++) {
            if (y > startY - height && y < endY) {
                int textY = y + (height - font.lineHeight) / 2;
                boolean bl = i == selectIndex;
                if (bl) {
                    var keyText = Component.keybind("key.extra_interact.interact");
                    drawKeyBoardKey(guiGraphics, x - font.width(keyText) - 9, textY - 1, keyText);
                }
                renderImage(guiGraphics, option, x, y, 1, 1, width, height);
                var rec = interactions.get(i);
                var icon = rec.canUse() ? rec.interaction().icon : lock;
                if (icon != null) renderImage(guiGraphics, icon, x, textY - 2, 1, 1, 14, 14);
                guiGraphics.text(font, Component.translatable(rec.name()), x + (icon == null ? 3 : 15), textY, bl ? -256 : -1, false);
            }
            y += height;
        }
        if (size > showCount) {
            final int barStartY = startY + halfHeight + 2;
            guiGraphics.fill(x - 2, barStartY, x - 3, endY - halfHeight - 2, -16777216);
            final int barHeight = showCount * height - 4;
            final int scrollHeight = (int) ((float) showCount / size * barHeight);
            final int scrollY = (int) ((float) selectIndex / (size - 1) * (barHeight - scrollHeight));
            guiGraphics.fill(x - 2, barStartY + scrollY, x - 3, barStartY + scrollY + scrollHeight, -8355712);
        }
        guiGraphics.disableScissor();
    }

    public static void onKey(int key, int scancode, int action, int modifiers) {
        var player = minecraft.player;
        if (interactions.isEmpty() || minecraft.screen != null || player == null) return;
        if (key == interactKey.getKey().getValue()) {
            if (action == 1) {
                var rec = interactions.get(selectIndex);
                if (rec.canUse()) {
                    Interactions.applyAction(player, rec.target(), rec.name());
                    Objects.requireNonNull(minecraft.getConnection()).send(new SendInteraction(rec.name(), rec.target()));
                } else player.sendOverlayMessage(Component.translatable(rec.interaction().getTip()).withStyle(ChatFormatting.RED));
            }
        }
    }

    public static boolean onMouseScroll(double scrollDeltaX, double scrollDeltaY, boolean leftDown, boolean middleDown, boolean rightDown, double mouseX, double mouseY) {
        if (!interactions.isEmpty()) {
            int size = interactions.size();
            if (size > 1) {
                if (scrollDeltaY > 0) selectIndex = (selectIndex - 1 + size) % size;
                else if (scrollDeltaY < 0) selectIndex = (selectIndex + 1) % size;
                return true;
            }
        }
        return false;
    }

    public static int screenWidth() {return minecraft.getWindow().getGuiScaledWidth();}

    public static int screenHeight() {return minecraft.getWindow().getGuiScaledHeight();}

    public static void renderImage(GuiGraphicsExtractor guiGraphics, Identifier identifier, int x, int y, float uw, float uh, int width, int height) {
        AbstractTexture texture = minecraft.getTextureManager().getTexture(identifier);
        guiGraphics.guiRenderState.addGuiElement(new BlitRenderState(RenderPipelines.GUI_TEXTURED, TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler()), new Matrix3x2f(guiGraphics.pose()), x, y, x + width, y + height, 0, uw, 0, uh, -1, guiGraphics.peekScissorStack()));
    }

    public static void drawKeyBoardKey(GuiGraphicsExtractor guiGraphics, int x, int y, Component key) {
        var font = minecraft.font;
        // 按键尺寸
        int width = font.width(key) + 4;
        int height = 12;
        int topColor = 0xFF707070;  // 上亮面
        int faceColor = 0xFF505050;  // 主体灰色
        int bottomColor = 0xFF202020;  // 下阴影
        // 背景
        guiGraphics.fillGradient(x, y, x + width, y + height, topColor, bottomColor); // 垂直渐变背景
        guiGraphics.fill(x, y, x + width, y + height, faceColor); // 覆盖主色
        // 画边框
        guiGraphics.fill(x, y, x + width, y + 1, topColor); // 顶部线
        guiGraphics.fill(x, y, x + 1, y + height, topColor); // 左边线
        guiGraphics.fill(x, y + height - 1, x + width, y + height, bottomColor); // 底部线
        guiGraphics.fill(x + width - 1, y, x + width, y + height, bottomColor); // 右边线
        guiGraphics.centeredText(font, key, x + width / 2, y + (height - 8) / 2, 0xFFFFFFFF);
    }

    public static Window getWindow() {return minecraft.getWindow();}

    public static boolean hasControlDown() {
        if (Util.getPlatform() == Util.OS.OSX) {
            return InputConstants.isKeyDown(getWindow(), 343) || InputConstants.isKeyDown(getWindow(), 347);
        } else {
            return InputConstants.isKeyDown(getWindow(), 341) || InputConstants.isKeyDown(getWindow(), 345);
        }
    }
}
