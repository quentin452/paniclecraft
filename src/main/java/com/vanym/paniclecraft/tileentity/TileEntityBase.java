package com.vanym.paniclecraft.tileentity;

import com.vanym.paniclecraft.Core;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class TileEntityBase extends TileEntity {
    
    public void markForUpdate() {
        this.markDirty();
        if (this.worldObj != null) {
            this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
        }
    }
    
    public void safeMarkForUpdate() {
        Core.instance.syncTileEntityUpdater.safeMarkForUpdate(this);
    }
    
    @Override
    public boolean shouldRefresh(
            Block oldBlock,
            Block newBlock,
            int oldMeta,
            int newMeta,
            World world,
            int x,
            int y,
            int z) {
        return oldBlock != newBlock;
    }
    
    @Override
    public boolean canUpdate() {
        return false;
    }
    
    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound dataTag = new NBTTagCompound();
        this.writeToNBT(dataTag);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 1, dataTag);
    }
    
    @Override
    public void onDataPacket(NetworkManager manager, S35PacketUpdateTileEntity packet) {
        NBTTagCompound nbtData = packet.func_148857_g();
        this.readFromNBT(nbtData);
    }
}
