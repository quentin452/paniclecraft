package com.vanym.paniclecraft.tileentity;

import java.awt.Color;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

import com.vanym.paniclecraft.DEF;
import com.vanym.paniclecraft.core.component.advsign.AdvSignForm;
import com.vanym.paniclecraft.core.component.advsign.AdvSignText;
import com.vanym.paniclecraft.utils.NumberUtils;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityAdvSign extends TileEntityBase {
    
    public static final String IN_MOD_ID = "advanced_sign";
    public static final ResourceLocation ID = new ResourceLocation(DEF.MOD_ID, IN_MOD_ID);
    
    protected final AdvSignText frontText = new AdvSignText();
    protected final AdvSignText backText = new AdvSignText();
    
    protected Color standColor = Color.WHITE;
    
    protected double direction = 0.0D;
    protected AdvSignForm form = AdvSignForm.WALL;
    
    protected UUID editor = null;
    
    public static final String TAG_FRONTTEXT = "FrontText";
    public static final String TAG_BACKTEXT = "BackText";
    public static final String TAG_STANDCOLOR = "StandColor";
    
    protected static final String TAG_DIRECTION = "Direction";
    protected static final String TAG_FORM = "Form";
    
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTag) {
        return this.writeToNBT(nbtTag, false);
    }
    
    public NBTTagCompound writeToNBT(NBTTagCompound nbtTag, boolean toStack) {
        nbtTag.setTag(TAG_FRONTTEXT, this.frontText.serializeNBT());
        nbtTag.setTag(TAG_BACKTEXT, this.backText.serializeNBT());
        nbtTag.setInteger(TAG_STANDCOLOR, this.standColor.getRGB());
        if (toStack) {
            return nbtTag;
        }
        super.writeToNBT(nbtTag);
        nbtTag.setDouble(TAG_DIRECTION, this.direction);
        nbtTag.setInteger(TAG_FORM, this.form.getIndex());
        return nbtTag;
    }
    
    @Override
    public void readFromNBT(NBTTagCompound nbtTag) {
        this.readFromNBT(nbtTag, false);
    }
    
    public void readFromNBT(NBTTagCompound nbtTag, boolean fromStack) {
        if (nbtTag.hasKey(TAG_FRONTTEXT, 10)) {
            this.frontText.deserializeNBT(nbtTag.getCompoundTag(TAG_FRONTTEXT));
        }
        if (nbtTag.hasKey(TAG_BACKTEXT, 10)) {
            this.backText.deserializeNBT(nbtTag.getCompoundTag(TAG_BACKTEXT));
        }
        this.standColor = new Color(nbtTag.getInteger(TAG_STANDCOLOR), true);
        if (fromStack) {
            return;
        }
        super.readFromNBT(nbtTag);
        this.setDirection(nbtTag.getDouble(TAG_DIRECTION));
        this.setForm(AdvSignForm.byIndex(nbtTag.getInteger(TAG_FORM)));
    }
    
    public AdvSignText getFront() {
        return this.frontText;
    }
    
    public AdvSignText getBack() {
        return this.backText;
    }
    
    public AdvSignText getSide(boolean front) {
        return front ? this.getFront() : this.getBack();
    }
    
    public void setStandColor(Color color) {
        Objects.requireNonNull(color);
        this.standColor = color;
    }
    
    public Color getStandColor() {
        return this.standColor;
    }
    
    public void setForm(AdvSignForm form) {
        this.form = Objects.requireNonNull(form);
    }
    
    public AdvSignForm getForm() {
        return this.form;
    }
    
    public void setDirection(double direction) {
        direction = NumberUtils.finite(direction);
        direction = MathHelper.wrapDegrees(direction);
        if (direction < 0) {
            direction += 360.0D;
        }
        this.direction = direction;
    }
    
    public double getDirection() {
        return this.direction;
    }
    
    public void setEditor(UUID editor) {
        this.editor = editor;
    }
    
    public void resetEditor() {
        this.editor = null;
    }
    
    public boolean hasEditor() {
        return this.editor == null;
    }
    
    public boolean isEditor(UUID player) {
        return this.editor != null && this.editor.equals(player);
    }
    
    @Override
    public boolean onlyOpsCanSetNbt() {
        return true;
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(this.pos).grow(0.25D);
    }
    
    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 16384.0D;
    }
    
    public static boolean isValidTag(NBTTagCompound signTag) {
        return new Color(signTag.getInteger(TAG_STANDCOLOR), true).getAlpha() == 0xff
            && Stream.of(TAG_FRONTTEXT, TAG_BACKTEXT)
                     .map(signTag::getCompoundTag)
                     .map(AdvSignText::new)
                     .allMatch(AdvSignText::isValid);
    }
}
