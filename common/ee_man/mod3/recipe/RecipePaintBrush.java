package ee_man.mod3.recipe;

import ee_man.mod3.Core;
import ee_man.mod3.items.ItemPaintBrush;

import net.minecraft.block.BlockColored;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RecipePaintBrush implements IRecipe{
	
	@Override
	public boolean matches(InventoryCrafting par1InventoryCrafting, World var2World){
		int var3 = 0;
		boolean var4 = false;
		
		for(int var5 = 0; var5 < par1InventoryCrafting.getSizeInventory(); ++var5){
			ItemStack var6 = par1InventoryCrafting.getStackInSlot(var5);
			
			if(var6 != null){
				if(var6.itemID == Core.itemPaintBrush.itemID){
					var3++;
				}
				else{
					if(var6.itemID != Item.dyePowder.itemID){
						return false;
					}
					
					var4 = true;
				}
			}
		}
		
		return var3 == 1 && var4;
	}
	
	@Override
	public ItemStack getCraftingResult(InventoryCrafting par1InventoryCrafting){
		ItemStack var2 = null;
		int[] var3 = new int[3];
		int var4 = 0;
		int var5 = 0;
		ItemPaintBrush var6 = null;
		int var7;
		int var9;
		float var10;
		float var11;
		int var17;
		
		for(var7 = 0; var7 < par1InventoryCrafting.getSizeInventory(); ++var7){
			ItemStack var8 = par1InventoryCrafting.getStackInSlot(var7);
			
			if(var8 != null){
				if(var8.getItem() instanceof ItemPaintBrush){
					var6 = (ItemPaintBrush)var8.getItem();
					
					var2 = var8.copy();
					
					if(var6.hasColor(var8)){
						var9 = var6.getColor(var2);
						var10 = (float)(var9 >> 16 & 255) / 255.0F;
						var11 = (float)(var9 >> 8 & 255) / 255.0F;
						float var12 = (float)(var9 & 255) / 255.0F;
						var4 = (int)((float)var4 + Math.max(var10, Math.max(var11, var12)) * 255.0F);
						var3[0] = (int)((float)var3[0] + var10 * 255.0F);
						var3[1] = (int)((float)var3[1] + var11 * 255.0F);
						var3[2] = (int)((float)var3[2] + var12 * 255.0F);
						++var5;
					}
				}
				else{
					if(var8.itemID != Item.dyePowder.itemID){
						return null;
					}
					
					float[] var14 = EntitySheep.fleeceColorTable[BlockColored.getBlockFromDye(var8.getItemDamage())];
					int var16 = (int)(var14[0] * 255.0F);
					int var15 = (int)(var14[1] * 255.0F);
					var17 = (int)(var14[2] * 255.0F);
					var4 += Math.max(var16, Math.max(var15, var17));
					var3[0] += var16;
					var3[1] += var15;
					var3[2] += var17;
					++var5;
				}
			}
		}
		
		if(var6 == null){
			return null;
		}
		else{
			var7 = var3[0] / var5;
			int var13 = var3[1] / var5;
			var9 = var3[2] / var5;
			var10 = (float)var4 / (float)var5;
			var11 = (float)Math.max(var7, Math.max(var13, var9));
			var7 = (int)((float)var7 * var10 / var11);
			var13 = (int)((float)var13 * var10 / var11);
			var9 = (int)((float)var9 * var10 / var11);
			var17 = (var7 << 8) + var13;
			var17 = (var17 << 8) + var9;
			var6.setColor(var2, var17);
			return var2;
		}
	}
	
	@Override
	public int getRecipeSize(){
		return 0;
	}
	
	@Override
	public ItemStack getRecipeOutput(){
		return null;
	}
	
}
