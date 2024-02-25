package com.kqp.inventorytabs.tabs.render;

import java.awt.Rectangle;

import org.apache.logging.log4j.core.Logger;

import com.kqp.inventorytabs.init.InventoryTabs;
import com.kqp.inventorytabs.mixin.accessor.HandledScreenAccessor;
import com.kqp.inventorytabs.mixin.accessor.ScreenAccessor;
import com.kqp.inventorytabs.tabs.TabManager;
import com.kqp.inventorytabs.tabs.tab.Tab;
import com.mojang.blaze3d.systems.RenderSystem;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import static com.kqp.inventorytabs.init.InventoryTabs.*;

/**
 * Handles the rendering of tabs.
 */
@Environment(EnvType.CLIENT)
public class TabRenderer {

    private static final Identifier BUTTONS_TEXTURE = InventoryTabs.id("textures/gui/sprites/buttons.png");

    public static final int TAB_WIDTH = 15;
    public static final int TAB_HEIGHT = 15;
    public static final int BUTTON_WIDTH = 15;
    public static final int BUTTON_HEIGHT = 15;

    public final TabManager tabManager;

    private TabRenderInfo[] tabRenderInfos;

    private long pageTextRefreshTime;

    public TabRenderer(TabManager tabManager) {
        this.tabManager = tabManager;
    }

    public void renderBackground(DrawContext context) {
        tabRenderInfos = getTabRenderInfos();

        for (int i = 0; i < tabRenderInfos.length; i++) {
            TabRenderInfo tabRenderInfo = tabRenderInfos[i];

            if (tabRenderInfo != null) {
                if (tabRenderInfo.tabReference != tabManager.currentTab) {
                    renderTab(context, tabRenderInfo);
                }
            }
        }
    }

    public void renderForeground(DrawContext context, double mouseX, double mouseY) {

        for (int i = 0; i < tabRenderInfos.length; i++) {
            TabRenderInfo tabRenderInfo = tabRenderInfos[i];

            if (tabRenderInfo != null) {
                if (tabRenderInfo.tabReference == tabManager.currentTab) {
                    renderTab(context, tabRenderInfo);
                }
            }
        }

        drawButtons(context, mouseX, mouseY);

        drawPageText(context);
    }

    private void drawButtons(DrawContext context, double mouseX, double mouseY) {
        HandledScreen<?> currentScreen = tabManager.getCurrentScreen();

        int width = ((HandledScreenAccessor) currentScreen).getBackgroundWidth();
        int height = ((HandledScreenAccessor) currentScreen).getBackgroundHeight();
        int oX = (currentScreen.width - width) / 2;
        int oY = (currentScreen.height - height) / 2;

        // Drawing back button
        int x = oX - BUTTON_WIDTH - 4;
        x += ((TabRenderingHints) currentScreen).getTopRowXOffset();
        int y = oY - 16;
        y += ((TabRenderingHints) currentScreen).getTopRowYOffset();
        boolean hovered = new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT - 2).contains(mouseX, mouseY);
        int u = 0;
        u += tabManager.canGoBackAPage() && hovered ? BUTTON_WIDTH * 2 : 0;
        int v = tabManager.canGoBackAPage() ? 0 : 13;
        context.drawTexture(BUTTONS_TEXTURE, x, y, u, v, BUTTON_WIDTH, BUTTON_HEIGHT - 2);

        // Drawing forward button
        x = oX + width + 4;
        x += ((TabRenderingHints) currentScreen).getTopRowXOffset();
        y = oY - 16;
        y += ((TabRenderingHints) currentScreen).getTopRowYOffset();
        hovered = new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT - 2).contains(mouseX, mouseY);
        u = 15;
        u += tabManager.canGoForwardAPage() && hovered ? BUTTON_WIDTH * 2 : 0;
        v = tabManager.canGoForwardAPage() ? 0 : 13;
        context.drawTexture(BUTTONS_TEXTURE, x, y, u, v, BUTTON_WIDTH, BUTTON_HEIGHT - 2);

    }

    private void drawPageText(DrawContext context) {
        if (tabManager.getMaxPages() > 1 && pageTextRefreshTime > 0) {
            // RenderSystem.pushMatrix();
            // TODO: Figure out rendering

            int color = 0xFFFFFFFF;

            if (pageTextRefreshTime <= 20) {
                RenderSystem.enableBlend();
                // RenderSystem.disableAlphaTest();
                RenderSystem.defaultBlendFunc();
                RenderSystem.colorMask(true, true, true, true);
                float transparency = pageTextRefreshTime / 20F;

                color &= 0x00FFFFFF;
                color = ((int) (0xFF * transparency) << 24) | color;
            }

            HandledScreen<?> currentScreen = tabManager.getCurrentScreen();
            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

            int height = ((HandledScreenAccessor) currentScreen).getBackgroundHeight();
            int oX = currentScreen.width;
            int oY = (currentScreen.height - height) / 2;

            String text = (tabManager.currentPage + 1) + " / " + (tabManager.getMaxPages() + 1);
            int x = (oX - textRenderer.getWidth(text)) / 2;
            int y = oY - 34;

            context.drawText(MinecraftClient.getInstance().textRenderer, text, x, y, color, false);

            // RenderSystem.popMatrix();
        }
    }

    private void renderTab(DrawContext context, TabRenderInfo tabRenderInfo) {
        HandledScreen<?> currentScreen = tabManager.getCurrentScreen();
        tabRenderInfo.tabReference.renderTabIcon(context, tabRenderInfo, currentScreen);
    }

    public void renderHoverTooltips(DrawContext context, double mouseX, double mouseY) {
        HandledScreen<?> currentScreen = tabManager.getCurrentScreen();
        TextRenderer textRenderer = ((ScreenAccessor) currentScreen).getTextRenderer();

        for (int i = 0; i < tabRenderInfos.length; i++) {
            TabRenderInfo tabRenderInfo = tabRenderInfos[i];

            if (tabRenderInfo != null) {
                Rectangle itemRec = new Rectangle(tabRenderInfo.itemX, tabRenderInfo.itemY, 16, 16);

                if (itemRec.contains(mouseX, mouseY)) {
                    context.drawTooltip(textRenderer, tabRenderInfo.tabReference.getHoverText(), (int) mouseX,
                            (int) mouseY);
                }
            }
        }
    }

    public TabRenderInfo[] getTabRenderInfos() {
        HandledScreen<?> currentScreen = tabManager.getCurrentScreen();

        int maxRowLength = tabManager.getMaxRowLength();
        int numVisibleTabs;
        if (isBigInvLoaded) {
            numVisibleTabs = (maxRowLength * 2) + 5;
        } else if (isPlayerExLoaded) {
            numVisibleTabs = (maxRowLength * 2) - 3;
        } else if (isLevelzLoaded) {
            numVisibleTabs = (maxRowLength * 2) - 2;
        } else {
            numVisibleTabs = maxRowLength * 2;
        }
        int startingIndex = tabManager.currentPage * numVisibleTabs;

        TabRenderInfo[] tabRenderInfo = new TabRenderInfo[numVisibleTabs];

        int x = (currentScreen.width - ((HandledScreenAccessor) currentScreen).getBackgroundWidth()) / 2;
        int y = (currentScreen.height - ((HandledScreenAccessor) currentScreen).getBackgroundHeight()) / 2;

        for (int i = 0; i < numVisibleTabs; i++) {
            if (startingIndex + i < tabManager.tabs.size()) {
                // Setup basic info
                Tab tab = tabManager.tabs.get(startingIndex + i);
                boolean topRow = i < maxRowLength;
                if (isPlayerExLoaded) {
                    topRow = i < maxRowLength - 3;
                } else if (isLevelzLoaded) {
                    topRow = i < maxRowLength - 2;
                }
                boolean selected = tab == tabManager.currentTab;

                // Create tab info object
                TabRenderInfo tabInfo = new TabRenderInfo();
                tabInfo.tabReference = tab;
                tabInfo.index = startingIndex + i;

                // Calc x value
                tabInfo.x = x + i * (TAB_WIDTH + 1);
                if (!topRow) {
                    tabInfo.x -= maxRowLength * (TAB_WIDTH + 1);
                }

                // Calc y value
                if (topRow) {
                    tabInfo.y = y - 13;
                } else {
                    if (isBigInvLoaded) {
                        tabInfo.y = y + ((HandledScreenAccessor) currentScreen).getBackgroundHeight() + 13;
                    } else {
                        tabInfo.y = y + ((HandledScreenAccessor) currentScreen).getBackgroundHeight() - 4;
                    }
                }

                // Calc texture dimensions
                tabInfo.texW = TAB_WIDTH;
                tabInfo.texH = 15;

                // Calc texture U
                if (i == 0 || i == maxRowLength) {
                    tabInfo.texU = 0;
                } else {
                    tabInfo.texU = TAB_WIDTH;
                }

                // Calc texture V
                if (topRow) {
                    if (selected) {
                        tabInfo.texV = 32;
                    } else {
                        tabInfo.texV = 0;
                    }
                } else {
                    if (selected) {
                        tabInfo.texV = 96;
                    } else {
                        tabInfo.texV = 64;
                    }
                }

                tabInfo.itemX = tabInfo.x;
                tabInfo.itemY = tabInfo.y;

                // Apply rendering hints
                if (currentScreen instanceof TabRenderingHints) {
                    if (topRow) {
                        if (isPlayerExLoaded) {
                            tabInfo.x += ((TabRenderingHints) currentScreen).getTopRowXOffset() + 87;
                            tabInfo.itemX += ((TabRenderingHints) currentScreen).getTopRowXOffset() + 87;
                        } else if (isLevelzLoaded) {
                            tabInfo.x += ((TabRenderingHints) currentScreen).getTopRowXOffset() + 54;
                            tabInfo.itemX += ((TabRenderingHints) currentScreen).getTopRowXOffset() + 54;
                        } else {
                            tabInfo.x += ((TabRenderingHints) currentScreen).getTopRowXOffset();
                            tabInfo.itemX += ((TabRenderingHints) currentScreen).getTopRowXOffset();
                        }
                        tabInfo.y += ((TabRenderingHints) currentScreen).getTopRowYOffset();
                        tabInfo.itemY += ((TabRenderingHints) currentScreen).getTopRowYOffset();
                    } else {
                        if (isBigInvLoaded) {
                            tabInfo.x += ((TabRenderingHints) currentScreen).getBottomRowXOffset() - 145;
                            tabInfo.itemX += ((TabRenderingHints) currentScreen).getBottomRowXOffset() - 145;
                        } else if (isPlayerExLoaded) {
                            tabInfo.x += ((TabRenderingHints) currentScreen).getBottomRowXOffset() + 86;
                            tabInfo.itemX += ((TabRenderingHints) currentScreen).getBottomRowXOffset() + 86;
                        } else if (isLevelzLoaded) {
                            tabInfo.x += ((TabRenderingHints) currentScreen).getBottomRowXOffset() + 60;
                            tabInfo.itemX += ((TabRenderingHints) currentScreen).getBottomRowXOffset() + 60;
                        } else {
                            tabInfo.x += ((TabRenderingHints) currentScreen).getBottomRowXOffset();
                            tabInfo.itemX += ((TabRenderingHints) currentScreen).getBottomRowXOffset();
                        }
                        tabInfo.y += ((TabRenderingHints) currentScreen).getBottomRowYOffset();
                        tabInfo.itemY += ((TabRenderingHints) currentScreen).getBottomRowYOffset();
                    }
                }

                tabRenderInfo[i] = tabInfo;
            }
        }

        return tabRenderInfo;
    }

    public void update() {
        pageTextRefreshTime = Math.max(pageTextRefreshTime - 1, 0);
    }

    public void resetPageTextRefreshTime() {
        pageTextRefreshTime = 60;
    }
}
