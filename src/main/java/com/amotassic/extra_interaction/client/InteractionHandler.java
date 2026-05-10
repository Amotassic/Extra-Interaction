package com.amotassic.extra_interaction.client;

import com.amotassic.extra_interaction.interaction.Interactions;
import com.amotassic.extra_interaction.network.SendInteraction;
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
import net.minecraft.util.Tuple;
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
    static int selectIndex = 0;
    static List<Tuple<Object, String>> interactions = new ArrayList<>();

    public static boolean hasOptions() {return !interactions.isEmpty();}

    public static void onHudRender(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        interactions.clear();
        if (minecraft.level == null || minecraft.player == null) return;
        HitResult result = minecraft.hitResult;
        if (result == null || result.getType() == HitResult.Type.MISS) return;

        Object target = null;
        if (result instanceof BlockHitResult blockHitResult) target = blockHitResult.getBlockPos();
        else if (result instanceof EntityHitResult entityHitResult) target = entityHitResult.getEntity();
        if (target != null) for (String interaction : Interactions.getInteractions(minecraft.player, target)) {
            interactions.add(new Tuple<>(target, interaction));
        }

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
                    guiGraphics.text(font, keyText, x - font.width(keyText) - 5, textY, -1, false);
                }
                renderImage(guiGraphics, Config.getOptionTexture(), x, y, 1, 1, width, height);
                guiGraphics.text(font, Component.translatable(interactions.get(i).getB()), x + 3, textY, bl ? -256 : -1, false);
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
        if (interactions.isEmpty() || minecraft.screen != null) return;
        if (key == interactKey.getKey().getValue()) {
            if (action == 1) {
                var tuple = interactions.get(selectIndex);
                Interactions.applyAction(minecraft.player, tuple.getA(), tuple.getB());
                Objects.requireNonNull(minecraft.getConnection()).send(new SendInteraction(tuple.getB(), tuple.getA()));
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
}
