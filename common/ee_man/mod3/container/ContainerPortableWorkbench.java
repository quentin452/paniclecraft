package ee_man.mod3.container;

import ee_man.mod3.Core;
import ee_man.mod3.container.slot.SlotPortableCrafting;
import ee_man.mod3.container.slot.SlotCanBeSelected;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.world.World;

public class ContainerPortableWorkbench extends Container{
	
	public InventoryCrafting craftMatrix = new InventoryCrafting(this, 3, 3);
	public IInventory craftResult = new InventoryCraftResult();
	private World worldObj;
	
	public ContainerPortableWorkbench(InventoryPlayer par1InventoryPlayer, World par2World){
		this.worldObj = par2World;
		this.addSlotToContainer(new SlotPortableCrafting(par1InventoryPlayer.player, this.craftMatrix, this.craftResult, 0, 124, 35));
		int l;
		int i1;
		
		for(l = 0; l < 3; ++l){
			for(i1 = 0; i1 < 3; ++i1){
				this.addSlotToContainer(new Slot(this.craftMatrix, i1 + l * 3, 30 + i1 * 18, 17 + l * 18));
			}
		}
		
		for(l = 0; l < 3; ++l){
			for(i1 = 0; i1 < 9; ++i1){
				this.addSlotToContainer(new Slot(par1InventoryPlayer, i1 + l * 9 + 9, 8 + i1 * 18, 84 + l * 18));
			}
		}
		
		for(l = 0; l < 9; ++l){
			this.addSlotToContainer(new SlotCanBeSelected(par1InventoryPlayer, l, 8 + l * 18, 142));
		}
		
		this.onCraftMatrixChanged(this.craftMatrix);
		
	}
	
	public void onCraftMatrixChanged(IInventory par1IInventory){
		this.craftResult.setInventorySlotContents(0, CraftingManager.getInstance().findMatchingRecipe(this.craftMatrix, this.worldObj));
	}
	
	public void onCraftGuiClosed(EntityPlayer par1EntityPlayer){
		super.onContainerClosed(par1EntityPlayer);
		
		if(!this.worldObj.isRemote){
			for(int i = 0; i < 9; ++i){
				ItemStack itemstack = this.craftMatrix.getStackInSlotOnClosing(i);
				
				if(itemstack != null){
					par1EntityPlayer.dropPlayerItem(itemstack);
				}
			}
		}
	}
	
	public boolean canInteractWith(EntityPlayer par1EntityPlayer){
		return canBeWorkbench(par1EntityPlayer.getHeldItem());
	}
	
	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2){
		ItemStack itemstack = null;
		Slot slot = (Slot)this.inventorySlots.get(par2);
		
		if(slot != null && slot.getHasStack()){
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			
			if(par2 == 0){
				if(!this.mergeItemStack(itemstack1, 10, 46, true)){
					return null;
				}
				
				slot.onSlotChange(itemstack1, itemstack);
			}
			else
				if(par2 >= 10 && par2 < 37){
					if(!this.mergeItemStack(itemstack1, 37, 46, false)){
						return null;
					}
				}
				else
					if(par2 >= 37 && par2 < 46){
						if(!this.mergeItemStack(itemstack1, 10, 37, false)){
							return null;
						}
					}
					else
						if(!this.mergeItemStack(itemstack1, 10, 46, false)){
							return null;
						}
			
			if(itemstack1.stackSize == 0){
				slot.putStack((ItemStack)null);
			}
			else{
				slot.onSlotChanged();
			}
			
			if(itemstack1.stackSize == itemstack.stackSize){
				return null;
			}
			
			slot.onPickupFromSlot(par1EntityPlayer, itemstack1);
		}
		
		return itemstack;
	}
	
	public boolean func_94530_a(ItemStack par1ItemStack, Slot par2Slot){
		return par2Slot.inventory != this.craftResult && super.func_94530_a(par1ItemStack, par2Slot);
	}
	
	public static boolean canBeWorkbench(ItemStack par1){
		return par1 == null ? false : par1.itemID == Core.itemPortableWorkbench.itemID && par1.stackSize > 0;
	}
}
