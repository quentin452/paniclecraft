package com.vanym.paniclecraft.client.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.lwjgl.input.Keyboard;

import com.vanym.paniclecraft.Core;
import com.vanym.paniclecraft.DEF;
import com.vanym.paniclecraft.client.gui.element.AbstractButton;
import com.vanym.paniclecraft.client.gui.element.Button;
import com.vanym.paniclecraft.client.gui.element.GuiCircularSlider;
import com.vanym.paniclecraft.client.gui.element.GuiHexColorField;
import com.vanym.paniclecraft.client.gui.element.GuiStyleEditor;
import com.vanym.paniclecraft.client.utils.AdvTextInput;
import com.vanym.paniclecraft.core.component.advsign.AdvSignForm;
import com.vanym.paniclecraft.core.component.advsign.AdvSignText;
import com.vanym.paniclecraft.core.component.advsign.FormattingUtils;
import com.vanym.paniclecraft.network.message.MessageAdvSignChange;
import com.vanym.paniclecraft.tileentity.TileEntityAdvSign;
import com.vanym.paniclecraft.utils.ColorUtils;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiEditAdvSign extends GuiScreen {
    
    protected final TileEntityAdvSign sign;
    
    protected final SideEditState frontState;
    protected final SideEditState backState;
    
    protected boolean front;
    
    protected int updateCounter;
    
    protected Button buttonDone;
    protected Button buttonCopy;
    protected Button buttonPaste;
    protected Button buttonAddLine;
    protected Button buttonRemoveLine;
    protected Button buttonToggleStick;
    protected Button buttonFlip;
    
    protected GuiCircularSlider sliderDir;
    
    protected GuiHexColorField standColorHex;
    protected GuiHexColorField textColorHex;
    
    public GuiEditAdvSign(TileEntityAdvSign sign) {
        this(sign, true);
    }
    
    public GuiEditAdvSign(TileEntityAdvSign sign, boolean front) {
        this.sign = sign;
        this.front = front;
        this.frontState = new SideEditState(sign.getFront());
        this.backState = new SideEditState(sign.getBack());
    }
    
    @Override
    public void initGui() {
        int xCenter = this.width / 2;
        this.buttonDone = new Button(
                xCenter - 100,
                this.height / 4 + 120,
                200,
                20,
                I18n.format("gui.done"),
                b->this.mc.displayGuiScreen(null));
        this.buttonAddLine = new Button(xCenter + 59, this.height / 4 + 68, 20, 20, "+", b-> {
            AdvSignText text = this.getState().getText();
            text.getLines().add(new TextComponentString(""));
            text.fixSize();
            this.updateElements();
        });
        this.buttonRemoveLine = new Button(
                this.buttonAddLine.x + 21,
                this.buttonAddLine.y,
                20,
                20,
                "-",
                b-> {
                    AdvSignText text = this.getState().getText();
                    text.removeLast();
                    text.fixSize();
                    this.getState()
                        .switchToLine(Math.min(this.getState().getLine(),
                                               text.getLines().size() - 1));
                    this.updateElements();
                });
        this.buttonCopy = new Button(xCenter - 100, this.height / 4 + 99, 40, 20, "Copy", b-> {
            GuiUtils.setClipboardString(this.getState()
                                            .getText()
                                            .getLines()
                                            .stream()
                                            .map(ITextComponent::getFormattedText)
                                            .map(FormattingUtils::trimReset)
                                            .collect(Collectors.joining(System.lineSeparator())));
        });
        this.buttonPaste = new Button(xCenter - 59, this.height / 4 + 99, 40, 20, "Paste", b-> {
            this.getState().pasteFull(GuiUtils.getClipboardString());
            this.updateElements();
        });
        this.buttonToggleStick =
                new Button(xCenter - 100, this.height / 4 + 57, 55, 20, "Stick: ", b-> {
                    this.sign.setForm(AdvSignForm.byIndex(this.sign.getForm().getIndex() + 1));
                    this.updateElements();
                });
        this.buttonFlip = new Button(xCenter - 100, this.height / 4 + 78, 60, 20, "Side: ", b-> {
            this.front = !this.front;
            this.updateElements();
        });
        this.sliderDir = new GuiCircularSlider(15, xCenter - 100, this.height / 4 + 15, 40, 40);
        this.sliderDir.setGetter(()->this.sign.getDirection() / 360.0D);
        this.sliderDir.setSetter(v-> {
            v *= 16.0D;
            if (GuiScreen.isShiftKeyDown()) {
                v = (double)Math.round(v);
            }
            v *= 22.5D;
            v = (double)Math.round(v);
            this.sign.setDirection(v);
        });
        this.sliderDir.setOffset(-0.25D);
        this.standColorHex =
                new GuiHexColorField(
                        1,
                        this.fontRenderer,
                        xCenter + 48,
                        this.height / 4 + 106);
        this.standColorHex.setSetter(rgb->this.sign.setStandColor(new Color(rgb)));
        this.textColorHex =
                new GuiHexColorField(2, this.fontRenderer, xCenter + 48, this.height / 4 + 90);
        this.textColorHex.setSetter(rgb->this.getState().getText().setTextColor(new Color(rgb)));
        List<GuiStyleEditor> stylingMenu =
                GuiStyleEditor.createMenu(xCenter + 61, this.height / 4 + 25,
                                          ()->this.getState().getInput().getStyle(),
                                          (style)-> {
                                              SideEditState state = this.getState();
                                              state.getInput().applyStyle(style);
                                              state.updateLine();
                                          });
        this.updateElements();
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        this.buttonList.add(this.buttonDone);
        this.buttonList.add(this.buttonRemoveLine);
        this.buttonList.add(this.buttonAddLine);
        this.buttonList.add(this.buttonCopy);
        this.buttonList.add(this.buttonPaste);
        this.buttonList.add(this.buttonToggleStick);
        this.buttonList.add(this.buttonFlip);
        this.buttonList.add(this.sliderDir);
        this.buttonList.addAll(stylingMenu);
    }
    
    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        this.getState().updateLine();
        Core.instance.network.sendToServer(new MessageAdvSignChange(this.sign));
    }
    
    @Override
    public void updateScreen() {
        ++this.updateCounter;
        this.standColorHex.updateCursorCounter();
        this.textColorHex.updateCursorCounter();
    }
    
    @Override
    protected void actionPerformed(GuiButton button) {
        AbstractButton.hook(button);
    }
    
    @Override
    protected void keyTyped(char character, int key) {
        if (this.standColorHex.textboxKeyTyped(character, key)
            || this.textColorHex.textboxKeyTyped(character, key)) {
            return;
        }
        if (key == Keyboard.KEY_ESCAPE) {
            this.actionPerformed(this.buttonDone);
            return;
        }
        if (key == Keyboard.KEY_UP || key == Keyboard.KEY_PRIOR /* page up */) {
            this.getState().switchLine(-1);
        } else if (Stream.of(Keyboard.KEY_DOWN, Keyboard.KEY_NEXT, Keyboard.KEY_TAB,
                             Keyboard.KEY_RETURN, Keyboard.KEY_NUMPADENTER)
                         .anyMatch(code->code == key)) {
            this.getState().switchLine(+1);
        } else {
            AdvTextInput input = this.getState().getInput();
            if (!input.keyTyped(character, key)
                && ChatAllowedCharacters.isAllowedCharacter(character)) {
                input.insertText(Character.toString(character));
            }
            this.getState().updateLine();
        }
    }
    
    @Override
    protected void mouseClicked(int x, int y, int eventButton) throws IOException {
        super.mouseClicked(x, y, eventButton);
        this.standColorHex.mouseClicked(x, y, eventButton);
        this.textColorHex.mouseClicked(x, y, eventButton);
    }
    
    @Override
    protected void mouseClickMove(int x, int y, int button, long timeSinceMouseClick) {
        super.mouseClickMove(x, y, button, timeSinceMouseClick);
        Stream<GuiCircularSlider> sliders = this.buttonList.stream()
                                                           .filter(GuiCircularSlider.class::isInstance)
                                                           .map(GuiCircularSlider.class::cast);
        sliders.forEach(s->s.mouseDragged(this.mc, x, y));
    }
    
    @Override
    public void drawScreen(int mouseX, int mouseY, float renderPartialTicks) {
        if (!this.sliderDir.isPressed()) {
            this.drawDefaultBackground();
        }
        this.drawCenteredString(this.fontRenderer, I18n.format("sign.edit"), this.width / 2, 40,
                                0xffffff);
        if (this.sliderDir.isPressed()) {
            this.sliderDir.drawButton(this.mc, mouseX, mouseY, renderPartialTicks);
            String tooltipKey =
                    GuiScreen.isShiftKeyDown() ? "gui.%s.advanced_sign.slider_unshift_tooltip"
                                               : "gui.%s.advanced_sign.slider_shift_tooltip";
            this.drawCenteredString(this.fontRenderer,
                                    I18n.format(String.format(tooltipKey, DEF.MOD_ID)),
                                    this.width / 2, this.height - 75, 0xffffff);
            return;
        }
        this.drawSign();
        String linesText = String.format("Lines:%2d", this.getState().getText().getLines().size());
        int linesTextWidth = this.fontRenderer.getStringWidth(linesText);
        this.drawString(this.fontRenderer, linesText,
                        this.buttonAddLine.x - 2 - linesTextWidth,
                        this.buttonAddLine.y + 10, 0xffffff);
        String standText = "Stand:";
        int standTextWidth = this.fontRenderer.getStringWidth(standText);
        this.drawString(this.fontRenderer, standText,
                        this.standColorHex.x - 2 - standTextWidth,
                        this.standColorHex.y + 3, 0xffffff);
        this.standColorHex.drawTextBox();
        String textText = "Text:";
        int textTextWidth = this.fontRenderer.getStringWidth(textText);
        this.drawString(this.fontRenderer, textText,
                        this.textColorHex.x - 2 - textTextWidth,
                        this.textColorHex.y + 3, 0xffffff);
        this.textColorHex.drawTextBox();
        super.drawScreen(mouseX, mouseY, renderPartialTicks);
    }
    
    protected void drawSign() {
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.width / 2, 0.0F, 50.0F);
        float scale = 93.75F;
        GlStateManager.scale(-scale, -scale, -scale);
        GlStateManager.rotate(this.front ? 180.0F : 0.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(0.0F, -1.0625F, 0.0F);
        Core.instance.advSign.tileAdvSignRenderer.render(this.sign, -0.5D, -0.75D,
                                                         -0.5D, 0.0F, -1, true, false,
                                                         this);
        GlStateManager.popMatrix();
    }
    
    public boolean isBlink() {
        return this.updateCounter / 6 % 2 == 0;
    }
    
    public AdvTextInput getInput(boolean front, int line) {
        return this.getState(front).getInput(line);
    }
    
    protected SideEditState getState() {
        return this.getState(this.front);
    }
    
    protected SideEditState getState(boolean front) {
        return front ? this.frontState : this.backState;
    }
    
    protected void updateElements() {
        this.buttonAddLine.enabled = !this.getState().getText().isMax();
        this.buttonRemoveLine.enabled = !this.getState().getText().isMin();
        this.standColorHex.setRGB(ColorUtils.getAlphaless(this.sign.getStandColor()));
        this.textColorHex.setRGB(ColorUtils.getAlphaless(this.getState().getText().getTextColor()));
        this.buttonToggleStick.displayString =
                "Stick: " + (this.sign.getForm() == AdvSignForm.STICK_DOWN ? "ON" : "OFF");
        this.buttonFlip.displayString = "Side: " + (this.front ? "Front" : "Back");
    }
    
    protected class SideEditState {
        
        public final AdvTextInput input = new AdvTextInput();
        protected final AdvSignText text;
        
        protected int editLine = 0;
        
        public SideEditState(AdvSignText text) {
            this.text = text;
            this.switchLine(0);
        }
        
        public void switchLine(int offset) {
            this.switchToLine(this.editLine + offset);
        }
        
        public void switchToLine(int line) {
            int size = this.text.getLines().size();
            if (size > 0) {
                this.editLine = (size + line % size) % size;
                this.input.read(this.text.getLines().get(this.editLine));
            }
        }
        
        public void pasteFull(String text) {
            List<ITextComponent> lines = this.text.getLines();
            lines.clear();
            Arrays.stream(text.split("\\R", AdvSignText.MAX_LINES))
                  .limit(AdvSignText.MAX_LINES)
                  .map(FormattingUtils::parseLine)
                  .forEachOrdered(lines::add);
            for (int i = 0; i < lines.size(); i++) {
                this.switchToLine(i);
                this.trim();
                this.updateLine();
            }
        }
        
        protected void trim() {
            while (!this.lineFits(this.input.getComponent().getFormattedText())
                && (this.input.removeBack() || this.input.removeLast())) {
            }
        }
        
        protected boolean lineFits(CharSequence line) {
            int max = this.getMaxLineFontWidth();
            return GuiEditAdvSign.this.fontRenderer.getStringWidth(line.toString()) <= max;
        }
        
        public void updateLine() {
            this.trim();
            try {
                this.text.getLines().set(this.editLine, this.input.getComponent());
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        
        public int getLine() {
            return this.editLine;
        }
        
        public AdvTextInput getInput() {
            return this.input;
        }
        
        public AdvSignText getText() {
            return this.text;
        }
        
        public AdvTextInput getInput(int line) {
            return this.editLine == line ? this.input : null;
        }
        
        protected int getMaxLineFontWidth() {
            int size = this.text.getLines().size();
            return 23 * size;
        }
    }
}
