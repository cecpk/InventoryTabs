package com.kqp.inventorytabs.tabs.tab;

import com.kqp.inventorytabs.mixin.accessor.ScreenAccessor;
import com.kqp.inventorytabs.tabs.render.TabRenderInfo;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

/**
 * Base interface for tabs.
 */
@Environment(EnvType.CLIENT)
public abstract class Tab {
    private final ItemStack renderItemStack;

    protected Tab(ItemStack renderItemStack) {
        this.renderItemStack = renderItemStack;
    }

    /**
     * Fires whenever the tab is clicked.
     */
    public abstract void open();

    /**
     * Returns true if the tab should stop being displayed. Should be synced up with
     * the provider that provides this tab.
     *
     * @return
     */
    public abstract boolean shouldBeRemoved();

    /**
     * Returns the text that's displayed when hovering over the tab.
     *
     * @return
     */
    public abstract Text getHoverText();

    /**
     * Called when the screen associated with the tab is closed.
     */
    public void onClose() {
    }

    /**
     * Returns the tab's priority when being displayed. The player's inventory is at
     * 100.
     *
     * @return
     */
    public int getPriority() {
        return 0;
    }

    /**
     * Renders the tab's icon
     *
     * @param context       DrawContext
     * @param tabRenderInfo TabRenderInfo
     * @param currentScreen HandledScreen
     */
    @Environment(EnvType.CLIENT)
    public void renderTabIcon(DrawContext context, TabRenderInfo tabRenderInfo, HandledScreen<?> currentScreen) {
        TextRenderer textRenderer = ((ScreenAccessor) currentScreen).getTextRenderer();
        context.getMatrices().push();
        context.getMatrices().translate(0, 0, 100.0F);
        // RenderSystem.enableRescaleNormal();
        context.drawItem(renderItemStack, tabRenderInfo.itemX, tabRenderInfo.itemY);
        context.drawItemInSlot(textRenderer, renderItemStack, tabRenderInfo.itemX, tabRenderInfo.itemY);
        context.getMatrices().pop();
    }
}
