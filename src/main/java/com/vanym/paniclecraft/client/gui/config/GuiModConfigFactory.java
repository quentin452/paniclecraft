package com.vanym.paniclecraft.client.gui.config;

import java.util.Set;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

@SideOnly(Side.CLIENT)
public class GuiModConfigFactory implements IModGuiFactory {
    
    @Override
    public void initialize(Minecraft minecraftInstance) {}
    
    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return GuiModConfig.class;
    }
    
    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }
    
    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }
    
}
