package com.vanym.paniclecraft.client.gui.container;

import java.awt.Color;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.vanym.paniclecraft.Core;
import com.vanym.paniclecraft.DEF;
import com.vanym.paniclecraft.container.ContainerPalette;
import com.vanym.paniclecraft.network.message.MessagePaletteSetColor;
import com.vanym.paniclecraft.utils.MainUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

@SideOnly(Side.CLIENT)
public class GuiPalette extends GuiContainer implements ICrafting {
    
    protected static final ResourceLocation GUI_TEXTURE =
            new ResourceLocation(DEF.MOD_ID, "textures/guis/paletteGui.png");
    
    protected final GuiOneColorField[] textColor = new GuiOneColorField[3];
    protected GuiHexColorField textHex;
    
    protected ContainerPalette container;
    
    public GuiPalette(ContainerPalette container) {
        super(container);
        this.container = container;
    }
    
    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        for (int i = 0; i < this.textColor.length; ++i) {
            this.textColor[i] = new GuiOneColorField(
                    this.fontRendererObj,
                    this.guiLeft + 40,
                    this.guiTop + 42 - i * 12,
                    26,
                    12);
            int base = 0x555555;
            base |= 0xFF << (i * 8);
            int disabled = 0xAA << (i * 8);
            this.textColor[i].setTextColor(base);
            this.textColor[i].setDisabledTextColour(disabled);
            this.textColor[i].setMaxStringLength(3);
            this.textColor[i].setEnableBackgroundDrawing(true);
        }
        this.textHex = new GuiHexColorField(
                this.fontRendererObj,
                this.guiLeft + 8,
                this.guiTop + 58,
                50,
                12);
        this.textHex.setEnableBackgroundDrawing(true);
        this.textHex.setMaxStringLength(7);
        this.container.removeCraftingFromCrafters(this);
        this.container.addCraftingToCrafters(this);
    }
    
    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        this.container.removeCraftingFromCrafters(this);
    }
    
    @Override
    public void updateScreen() {
        super.updateScreen();
        this.textHex.updateCursorCounter();
        Arrays.asList(this.textColor).forEach(t->t.updateCursorCounter());
    }
    
    @Override
    protected void keyTyped(char character, int key) {
        if (this.textHexKeyTyped(character, key)) {
            return;
        }
        for (int i = 0; i < this.textColor.length; ++i) {
            if (this.textColorKeyTyped(i, character, key)) {
                return;
            }
        }
        super.keyTyped(character, key);
    }
    
    protected boolean textHexKeyTyped(char character, int key) {
        String previousText = this.textHex.getText();
        if (!this.textHex.textboxKeyTyped(character, key)) {
            return false;
        }
        String text = this.textHex.getText();
        if (previousText.equals(text)) {
            return true;
        }
        Color previousColor;
        Color color;
        try {
            previousColor = Color.decode(previousText);
        } catch (NumberFormatException e) {
            previousColor = null;
        }
        try {
            color = Color.decode(text);
        } catch (NumberFormatException e) {
            color = new Color(0);
        }
        if (previousColor != null && color.equals(previousColor)) {
            return true;
        }
        this.setColor(color);
        return true;
    }
    
    protected boolean textColorKeyTyped(int i, char character, int key) {
        GuiOneColorField textOne = this.textColor[i];
        String previousText = textOne.getText();
        if (!textOne.textboxKeyTyped(character, key)) {
            return false;
        }
        String text = textOne.getText();
        if (previousText.equals(text)) {
            return true;
        }
        int previousColor;
        int color;
        try {
            previousColor = Integer.decode(previousText);
        } catch (NumberFormatException e) {
            previousColor = 0;
        }
        try {
            color = Integer.decode(text);
        } catch (NumberFormatException e) {
            color = 0;
        }
        if (color == previousColor) {
            return true;
        }
        int rgb = MainUtils.getAlphaless(this.getColor());
        rgb &= ~(0xff << (i * 8));
        rgb |= color << (i * 8);
        this.setColor(new Color(rgb));
        return true;
    }
    
    @Override
    protected void mouseClicked(int x, int y, int eventButton) {
        super.mouseClicked(x, y, eventButton);
        this.textHex.mouseClicked(x, y, eventButton);
        Arrays.asList(this.textColor).forEach(t->t.mouseClicked(x, y, eventButton));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float renderPartialTicks) {
        super.drawScreen(mouseX, mouseY, renderPartialTicks);
    }
    
    protected void drawInventoriesNames() {
        String palette;
        if (this.container.inventoryPalette.hasCustomInventoryName()) {
            palette = this.container.inventoryPalette.getInventoryName();
        } else {
            String translate = this.container.inventoryPalette.getInventoryName() + ".name";
            palette = StatCollector.translateToLocal(translate).trim();
        }
        this.fontRendererObj.drawString(palette, 8, 6, 4210752);
        String player;
        if (this.container.inventoryPlayer.hasCustomInventoryName()) {
            player = this.container.inventoryPlayer.getInventoryName();
        } else {
            player = I18n.format(this.container.inventoryPlayer.getInventoryName(), new Object[0]);
        }
        this.fontRendererObj.drawString(player, 8, this.ySize - 96 + 2, 4210752);
    }
    
    protected void drawRGBLabels() {
        final String letters = "BGR";
        for (int i = 0; i < this.textColor.length; ++i) {
            int yoffset = this.textColor[i].getEnableBackgroundDrawing() ? 2 : 0;
            this.fontRendererObj.drawString(letters.charAt(i) + ": ",
                                            -this.guiLeft + this.textColor[i].xPosition - 11,
                                            -this.guiTop + this.textColor[i].yPosition + yoffset,
                                            4210752);
        }
    }
    
    @Override
    public void drawGuiContainerForegroundLayer(int par1, int par2) {
        this.drawInventoriesNames();
        this.drawRGBLabels();
    }
    
    @Override
    public void drawGuiContainerBackgroundLayer(float f, int i, int j) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(GUI_TEXTURE);
        int k = (this.width - this.xSize) / 2;
        int l = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(k, l, 0, 0, this.xSize, 3 * 18 + 17 + 96);
        int rgb = MainUtils.getAlphaless(this.getColor());
        float f7 = (float)(rgb >> 16 & 255) / 255.0F;
        float f6 = (float)(rgb >> 8 & 255) / 255.0F;
        float f5 = (float)(rgb & 255) / 255.0F;
        GL11.glColor4f(1.0F * f7, 1.0F * f6, 1.0F * f5, 1.0F);
        this.drawTexturedModalRect(k + 8, l + 38, 0, 167, 16, 16);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_BLEND);
        this.textHex.drawTextBox();
        Arrays.asList(this.textColor).forEach(t->t.drawTextBox());
    }
    
    protected void setColor(Color color) {
        Core.instance.network.sendToServer(new MessagePaletteSetColor(color));
    }
    
    protected Color getColor() {
        return this.container.getColor();
    }
    
    protected void updateText(ItemStack stack) {
        boolean empty = (stack == null);
        Color color = this.container.getColor();
        if (color == null) {
            color = new Color(0);
        }
        int rgb = MainUtils.getAlphaless(this.container.getColor());
        this.textHex.setEnabled(!empty);
        if (empty || !this.textHex.isFocused()) {
            this.textHex.setRGB(rgb);
            this.textHex.setFocused(false);
        }
        for (int i = 0; i < this.textColor.length; ++i) {
            this.textColor[i].setEnabled(!empty);
            if (empty || !this.textColor[i].isFocused()) {
                this.textColor[i].setText(Integer.toString((rgb >> i * 8) & 0xFF));
                this.textColor[i].setFocused(false);
            }
        }
    }
    
    @Override
    @SuppressWarnings("rawtypes")
    public void sendContainerAndContentsToPlayer(Container container, List inv) {
        this.sendSlotContents(container, 0, container.getSlot(0).getStack());
    }
    
    @Override
    public void sendSlotContents(Container container, int slot, ItemStack stack) {
        if (slot != 0) {
            return;
        }
        this.updateText(stack);
    }
    
    @Override
    public void sendProgressBarUpdate(Container container, int id, int level) {}
    
    protected static class GuiOneColorField extends GuiTextField {
        
        protected static final String NUM_CHARS = "0123456789";
        
        public GuiOneColorField(FontRenderer font, int x, int y, int width, int height) {
            super(font, x, y, width, height);
        }
        
        @Override
        public void setFocused(boolean focus) {
            super.setFocused(focus);
            if (focus) {
                return;
            }
            int num;
            try {
                num = Integer.decode(this.getText());
            } catch (NumberFormatException e) {
                num = 0;
            }
            this.setText(Integer.toString(num));
        }
        
        @Override
        public void writeText(String text) {
            StringBuilder sb = new StringBuilder();
            char[] chars = text.toCharArray();
            for (char c : chars) {
                if (NUM_CHARS.indexOf(c) == -1) {
                    continue;
                }
                sb.append(c);
            }
            super.writeText(sb.toString());
        }
        
        @Override
        public boolean textboxKeyTyped(char character, int key) {
            if (!super.textboxKeyTyped(character, key)) {
                return false;
            }
            this.checkNum();
            return true;
        }
        
        protected boolean checkNum() {
            String text = this.getText();
            if (text.isEmpty()) {
                return false;
            }
            int pos = this.getCursorPosition();
            int sel = this.getSelectionEnd();
            int num;
            try {
                num = Integer.decode(text);
            } catch (NumberFormatException e) {
                return false;
            }
            if (num > 0xff) {
                this.setText(Integer.toString(0xff));
                this.setCursorPosition(pos);
                this.setSelectionPos(sel);
                return true;
            }
            if (num < 0) {
                this.setText(Integer.toString(0));
                return true;
            }
            return false;
        }
    }
    
    protected static class GuiHexColorField extends GuiTextField {
        
        protected static final String NUM_CHARS = "0123456789ABCDEFabcdef";
        
        protected static final List<String> COLORS_ENABLED = Arrays.asList("\u00a79", "\u00a79",
                                                                           "\u00a7a", "\u00a7a",
                                                                           "\u00a7c", "\u00a7c");
        
        protected static final List<String> COLORS_DISABLED = Arrays.asList("\u00a71", "\u00a71",
                                                                            "\u00a72", "\u00a72",
                                                                            "\u00a74", "\u00a74");
        
        protected boolean isEnabled = true;
        
        public GuiHexColorField(FontRenderer font, int x, int y, int width, int height) {
            super(font, x, y, width, height);
        }
        
        @Override
        public void setFocused(boolean focus) {
            super.setFocused(focus);
            if (focus) {
                return;
            }
            int rgb;
            try {
                rgb = Integer.decode(this.getText());
            } catch (NumberFormatException e) {
                rgb = 0;
            }
            this.setRGB(rgb);
        }
        
        public void setRGB(int rgb) {
            this.setText(String.format("#%06X", rgb));
        }
        
        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            this.isEnabled = enabled;
        }
        
        @Override
        public void writeText(String text) {
            this.clearSign();
            StringBuilder sb = new StringBuilder();
            char[] chars = text.toCharArray();
            int sel = this.getSelectionEnd();
            for (int i = 0; i < chars.length; ++i) {
                char c = chars[i];
                if (sel == 0 && i == 0 && c == '#') {
                    sb.append(c);
                    continue;
                }
                if (NUM_CHARS.indexOf(c) == -1) {
                    continue;
                }
                c = Character.toUpperCase(c);
                sb.append(c);
            }
            super.writeText(sb.toString());
        }
        
        protected boolean clearSign() {
            String text = this.getText();
            int pos = this.getCursorPosition();
            int sel = this.getSelectionEnd();
            boolean skiped = false;
            StringBuilder sb = new StringBuilder();
            char[] chars = text.toCharArray();
            for (int i = 0; i < chars.length; ++i) {
                char c = chars[i];
                if (NUM_CHARS.indexOf(c) >= 0) {
                    sb.append(c);
                } else {
                    skiped = true;
                    if (pos > i) {
                        --pos;
                    }
                    if (sel > i) {
                        --sel;
                    }
                }
            }
            this.setText(sb.toString());
            this.setCursorPosition(pos);
            this.setSelectionPos(sel);
            return skiped;
        }
        
        @Override
        public boolean textboxKeyTyped(char character, int key) {
            if (!super.textboxKeyTyped(character, key)) {
                return false;
            }
            this.checkPrefix();
            return true;
        }
        
        protected boolean checkPrefix() {
            String text = this.getText();
            if (text.isEmpty() || text.startsWith("#")) {
                return false;
            }
            int pos = this.getCursorPosition();
            int sel = this.getSelectionEnd();
            this.setText("#" + text);
            this.setCursorPosition(pos + 1);
            this.setSelectionPos(sel + 1);
            return true;
        }
        
        @Override
        public void drawTextBox() {
            if (!this.getVisible()) {
                return;
            }
            int pos = this.getCursorPosition();
            int sel = this.getSelectionEnd();
            String text = this.getText();
            String textNum = this.getText();
            Iterator<String> it;
            if (this.isEnabled) {
                it = COLORS_ENABLED.iterator();
            } else {
                it = COLORS_DISABLED.iterator();
            }
            StringBuilder sb = new StringBuilder(textNum);
            int length = sb.length();
            for (int i = length - 1; i >= 1; --i) {
                String colorCode = it.hasNext() ? it.next() : "\u00a7f";
                sb.insert(i, colorCode);
            }
            this.setMaxStringLength(7 + Math.max(0, length - 1) * 2);
            this.setText(sb.toString());
            this.setCursorPosition(convertPos(pos));
            this.setSelectionPos(convertPos(sel));
            super.drawTextBox();
            this.setText(text);
            this.setMaxStringLength(7);
            this.setCursorPosition(pos);
            this.setSelectionPos(sel);
        }
        
        protected static int convertPos(int pos) {
            return pos + Math.max(0, pos - 1) * 2;
        }
    }
}
