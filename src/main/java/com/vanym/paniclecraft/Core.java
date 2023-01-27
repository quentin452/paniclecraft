package com.vanym.paniclecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.vanym.paniclecraft.client.ModConfig;
import com.vanym.paniclecraft.command.CommandMod3;
import com.vanym.paniclecraft.core.CreativeTabMod3;
import com.vanym.paniclecraft.core.GuiHandler;
import com.vanym.paniclecraft.core.IProxy;
import com.vanym.paniclecraft.core.component.ModComponent;
import com.vanym.paniclecraft.core.component.ModComponentAdvSign;
import com.vanym.paniclecraft.core.component.ModComponentBroom;
import com.vanym.paniclecraft.core.component.ModComponentCannon;
import com.vanym.paniclecraft.core.component.ModComponentDeskGame;
import com.vanym.paniclecraft.core.component.ModComponentPainting;
import com.vanym.paniclecraft.core.component.ModComponentPortableWorkbench;
import com.vanym.paniclecraft.item.ItemMod3;
import com.vanym.paniclecraft.recipe.RecipeDummy;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.oredict.RecipeSorter;

@Mod(
    modid = DEF.MOD_ID,
    name = DEF.MOD_NAME,
    version = DEF.VERSION,
    guiFactory = "com.vanym.paniclecraft.client.gui.config.GuiModConfigFactory")
public class Core {
    
    @Instance(DEF.MOD_ID)
    public static Core instance;
    
    @SidedProxy(
        clientSide = "com.vanym.paniclecraft.client.ClientProxy",
        serverSide = "com.vanym.paniclecraft.server.ServerProxy",
        modId = DEF.MOD_ID)
    public static IProxy proxy;
    
    public final ModComponentBroom broom = new ModComponentBroom();
    public final ModComponentAdvSign advSign = new ModComponentAdvSign();
    public final ModComponentPainting painting = new ModComponentPainting();
    public final ModComponentDeskGame deskgame = new ModComponentDeskGame();
    public final ModComponentCannon cannon = new ModComponentCannon();
    public final ModComponentPortableWorkbench portableworkbench =
            new ModComponentPortableWorkbench();
    
    public CreativeTabMod3 tab;
    
    public CommandMod3 command;
    
    public ModConfig config;
    
    public final SimpleNetworkWrapper network =
            NetworkRegistry.INSTANCE.newSimpleChannel(DEF.MOD_ID);
    
    protected final List<ModComponent> components = new ArrayList<>(
            Arrays.asList(this.broom, this.advSign, this.painting,
                          this.deskgame, this.cannon,
                          this.portableworkbench));
    
    public List<ModComponent> getComponents() {
        return Collections.unmodifiableList(this.components);
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModMetadata modMeta = event.getModMetadata();
        modMeta.modId = DEF.MOD_ID;
        modMeta.name = DEF.MOD_NAME;
        modMeta.authorList = Arrays.asList(new String[]{"ee_man"});
        modMeta.url = "http://www.minecraftforum.net/topic/1715756-";
        modMeta.description = "Create, Play or Draw and Clean up After";
        modMeta.version = DEF.VERSION;
        modMeta.autogenerated = false;
        
        this.config = new ModConfig(event.getSuggestedConfigurationFile());
        
        this.command = new CommandMod3();
        
        FMLCommonHandler.instance().bus().register(this);
        
        if (this.config.getBoolean("creativeTab", "general", true, "")) {
            this.tab = new CreativeTabMod3(DEF.MOD_ID);
        }
        
        if (Loader.isModLoaded("ComputerCraft")) {
            this.components.add(com.vanym.paniclecraft.plugins.computercraft.ComputerCraftPlugin.instance());
        }
        
        this.preInitCommon();
        
        for (ModComponent component : Core.instance.getComponents()) {
            component.preInit(this.config);
        }
        proxy.preInit(this.config);
    }
    
    protected void preInitCommon() {
        RecipeSorter.register(DEF.MOD_ID + ":dummyshaped", RecipeDummy.Shaped.class,
                              RecipeSorter.Category.SHAPED,
                              "after:forge:shapedore after:forge:shapelessore");
        RecipeSorter.register(DEF.MOD_ID + ":dummyshapeless", RecipeDummy.Shapeless.class,
                              RecipeSorter.Category.SHAPELESS,
                              "after:forge:shapedore after:forge:shapelessore");
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
        for (ModComponent component : Core.instance.getComponents()) {
            component.init(this.config);
        }
        proxy.init(this.config);
    }
    
    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(this.config);
        if (this.config.hasChanged()) {
            this.config.save();
        }
    }
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(this.command);
    }
    
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (!event.modID.equals(DEF.MOD_ID)) {
            return;
        }
        for (ModComponent component : Core.instance.getComponents()) {
            component.configChanged(this.config);
        }
        proxy.configChanged(this.config);
        if (this.config.hasChanged()) {
            this.config.save();
        }
    }
    
    public void registerItem(ItemMod3 item) {
        GameRegistry.registerItem(item, item.getName());
        if (this.tab != null) {
            item.setCreativeTab(this.tab);
            if (this.tab.iconitem == null) {
                this.tab.iconitem = item;
            }
        }
    }
    
    public FMLEmbeddedChannel getChannel(Side source) {
        return NetworkRegistry.INSTANCE.getChannel(DEF.MOD_ID, source);
    }
}
