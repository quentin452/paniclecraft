package com.vanym.paniclecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.vanym.paniclecraft.block.IMod3Block;
import com.vanym.paniclecraft.command.CommandMod3;
import com.vanym.paniclecraft.command.CommandVersion;
import com.vanym.paniclecraft.core.CreativeTabMod3;
import com.vanym.paniclecraft.core.GUIs;
import com.vanym.paniclecraft.core.IProxy;
import com.vanym.paniclecraft.core.ModConfig;
import com.vanym.paniclecraft.core.Shooter;
import com.vanym.paniclecraft.core.SyncTileEntityUpdater;
import com.vanym.paniclecraft.core.Version;
import com.vanym.paniclecraft.core.component.IModComponent;
import com.vanym.paniclecraft.core.component.IModComponent.IServerSideConfig;
import com.vanym.paniclecraft.core.component.ModComponentAdvSign;
import com.vanym.paniclecraft.core.component.ModComponentBroom;
import com.vanym.paniclecraft.core.component.ModComponentCannon;
import com.vanym.paniclecraft.core.component.ModComponentDeskGame;
import com.vanym.paniclecraft.core.component.ModComponentPainting;
import com.vanym.paniclecraft.core.component.ModComponentPortableWorkbench;
import com.vanym.paniclecraft.item.IMod3Item;
import com.vanym.paniclecraft.network.message.MessageComponentConfig;
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
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEmbeddedChannel;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.World;
import net.minecraftforge.oredict.RecipeSorter;

@Mod(
    modid = DEF.MOD_ID,
    name = DEF.MOD_NAME,
    version = DEF.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    guiFactory = "com.vanym.paniclecraft.client.gui.config.GuiModConfigFactory")
public class Core implements IGuiHandler {
    
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
    
    public final SyncTileEntityUpdater syncTileEntityUpdater = new SyncTileEntityUpdater();
    
    public final Shooter shooter = new Shooter();
    
    protected final List<IModComponent> components = new ArrayList<>(
            Arrays.asList(this.broom, this.advSign, this.painting,
                          this.deskgame, this.cannon,
                          this.portableworkbench));
    
    public List<IModComponent> getComponents() {
        return Collections.unmodifiableList(this.components);
    }
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModMetadata modMeta = event.getModMetadata();
        modMeta.modId = DEF.MOD_ID;
        modMeta.name = DEF.MOD_NAME;
        modMeta.authorList = Arrays.asList(new String[]{"ee_man"});
        modMeta.url = "https://github.com/vanym/paniclecraft";
        modMeta.description = "Create, Play or Draw and Clean up After";
        modMeta.version = DEF.VERSION;
        modMeta.autogenerated = false;
        
        this.config = new ModConfig(event.getSuggestedConfigurationFile());
        
        this.command = new CommandMod3();
        this.command.addSubCommand(new CommandVersion());
        
        FMLCommonHandler.instance().bus().register(this);
        
        if (this.config.getBoolean("creativeTab", "general", true, "")) {
            this.tab = new CreativeTabMod3(DEF.MOD_ID);
        }
        
        if (this.config.getBoolean("versionCheck", "general", true, "")) {
            Version.startVersionCheck();
        }
        
        if (Loader.isModLoaded("ComputerCraft")) {
            this.components.add(com.vanym.paniclecraft.plugins.computercraft.ComputerCraftPlugin.instance());
        }
        
        this.preInitCommon();
        
        for (IModComponent component : Core.instance.getComponents()) {
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
        Core.instance.network.registerMessage(MessageComponentConfig.Handler.class,
                                              MessageComponentConfig.class, 5, Side.CLIENT);
        FMLCommonHandler.instance().bus().register(this.syncTileEntityUpdater);
    }
    
    @EventHandler
    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, instance);
        for (IModComponent component : Core.instance.getComponents()) {
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
    
    @EventHandler
    public void serverStarted(FMLServerStartedEvent event) {
        this.syncTileEntityUpdater.serverStarted(event);
    }
    
    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (!event.modID.equals(DEF.MOD_ID)) {
            return;
        }
        for (IModComponent component : Core.instance.getComponents()) {
            component.configChanged(this.config);
        }
        proxy.configChanged(this.config);
        this.sendConfigToAllPlayers();
        if (this.config.hasChanged()) {
            this.config.save();
        }
    }
    
    public void registerBlock(IMod3Block mod3Block) {
        Block block = mod3Block.getBlock();
        GameRegistry.registerBlock(block, mod3Block.getItemClass(),
                                   mod3Block.getRegistryName(),
                                   mod3Block.getItemArgs());
        if (this.tab != null) {
            block.setCreativeTab(Core.instance.tab);
        }
    }
    
    public void registerItem(IMod3Item mod3Item) {
        Item item = mod3Item.getItem();
        GameRegistry.registerItem(item, mod3Item.getRegistryName());
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
    
    @SubscribeEvent
    public void onConnectionFromClient(FMLNetworkEvent.ServerConnectionFromClientEvent event) {
        this.sendConfigToPlayer(event.manager);
    }
    
    @SuppressWarnings("unchecked")
    protected void sendConfigToAllPlayers() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null && server.isServerRunning()) {
            ServerConfigurationManager manager = server.getConfigurationManager();
            List<EntityPlayerMP> players = manager.playerEntityList;
            players.stream()
                   .map(p->p.playerNetServerHandler.netManager)
                   .forEach(this::sendConfigToPlayer);
        }
    }
    
    protected void sendConfigToPlayer(NetworkManager manager) {
        Core.instance.getComponents().forEach(manager.isLocalChannel() ? component-> {
            IServerSideConfig config = component.getServerSideConfig();
            if (config != null) {
                component.setServerSideConfig(config);
            }
        } : component-> {
            MessageComponentConfig message = new MessageComponentConfig(component);
            if (message.isEmpty()) {
                return;
            }
            FMLEmbeddedChannel channel = Core.instance.getChannel(Side.SERVER);
            Packet packet = channel.generatePacketFrom(message);
            manager.scheduleOutboundPacket(packet);
        });
    }
    
    @Override
    public Object getServerGuiElement(
            int ID,
            EntityPlayer player,
            World world,
            int x,
            int y,
            int z) {
        if (ID >= 0 && ID < GUIs.values().length) {
            return GUIs.values()[ID].getServerGuiElement(ID, player, world, x, y, z);
        } else {
            return null;
        }
    }
    
    @Override
    public Object getClientGuiElement(
            int ID,
            EntityPlayer player,
            World world,
            int x,
            int y,
            int z) {
        if (ID >= 0 && ID < GUIs.values().length) {
            return GUIs.values()[ID].getClientGuiElement(ID, player, world, x, y, z);
        } else {
            return null;
        }
    }
}
