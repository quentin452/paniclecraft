package ee_man.mod3.client.renderer.item;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import ee_man.mod3.tileEntity.TileEntityCannon;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.ForgeVersion;

@SideOnly(Side.CLIENT)
public class ItemRendererCannon implements IItemRenderer{
	
	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type){
		return true;
	}
	
	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper){
		return true;
	}
	
	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data){
		TileEntityCannon tileCannon = new TileEntityCannon();
		if(type.equals(ItemRenderType.ENTITY)){
			GL11.glTranslatef(-0.25F, -0.2F, -0.25F);
			float var11 = 0.55F;
			GL11.glScalef(var11, var11, var11);
			tileCannon.height = (byte)item.stackSize;
		}
		else
			if(type.equals(ItemRenderType.EQUIPPED) || (ForgeVersion.getBuildVersion() >= 687 ? type.equals(ItemRenderType.EQUIPPED_FIRST_PERSON) : false)){
				GL11.glTranslatef(0.0F, 0.5F, 0.0F);
				tileCannon.direction = 135;
				if(data[1] instanceof Entity){
					Entity entity = (Entity)data[1];
					int var12 = (int)entity.rotationPitch;
					if(var12 < 0)
						var12 = 0;
					tileCannon.height = (byte)var12;
				}
			}
			else
				if(type.equals(ItemRenderType.INVENTORY)){
					float var12 = 1.1F;
					GL11.glScalef(var12, var12, var12);
					tileCannon.height = (byte)item.stackSize;
				}
				else
					if(type.equals(ItemRenderType.FIRST_PERSON_MAP)){
						
					}
		TileEntityRenderer.instance.renderTileEntityAt(tileCannon, 0, 0, 0, 0);
	}
	
}
