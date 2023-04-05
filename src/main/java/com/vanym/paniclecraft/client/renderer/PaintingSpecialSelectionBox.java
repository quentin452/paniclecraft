package com.vanym.paniclecraft.client.renderer;

import java.awt.Color;
import java.util.stream.Stream;
import java.util.stream.Stream.Builder;

import org.lwjgl.opengl.GL11;

import com.vanym.paniclecraft.core.component.painting.IPaintingTool;
import com.vanym.paniclecraft.core.component.painting.PaintingSide;
import com.vanym.paniclecraft.core.component.painting.Picture;
import com.vanym.paniclecraft.core.component.painting.WorldPicturePoint;
import com.vanym.paniclecraft.core.component.painting.WorldPictureProvider;
import com.vanym.paniclecraft.utils.GeometryUtils;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PaintingSpecialSelectionBox {
    
    protected boolean onlyCancel = false;
    
    protected Color color = null;
    
    public PaintingSpecialSelectionBox() {
        this(false);
    }
    
    public PaintingSpecialSelectionBox(boolean onlyCancel) {
        this(false, null);
    }
    
    public PaintingSpecialSelectionBox(boolean onlyCancel, Color color) {
        this.onlyCancel = onlyCancel;
        this.color = color;
    }
    
    public void setOnlyCancel(boolean onlyCancel) {
        this.onlyCancel = onlyCancel;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
    
    @SubscribeEvent
    public void drawSelectionBox(DrawBlockHighlightEvent event) {
        final RayTraceResult target = event.getTarget();
        if (target == null || target.typeOfHit != RayTraceResult.Type.BLOCK) {
            return;
        }
        EntityPlayer player = event.getPlayer();
        ItemStack stack = Stream.of(EnumHand.MAIN_HAND, EnumHand.OFF_HAND)
                                .map(player::getHeldItem)
                                .filter(s->!s.isEmpty())
                                .findFirst()
                                .orElseGet(()->ItemStack.EMPTY);
        Item item = stack.getItem();
        if (!(item instanceof IPaintingTool)) {
            return;
        }
        IPaintingTool tool = (IPaintingTool)item;
        if (!tool.getPaintingToolType(stack).isPixelSelector()) {
            return;
        }
        BlockPos pos = target.getBlockPos();
        Picture picture = new WorldPicturePoint(
                WorldPictureProvider.ANYTILE,
                player.world,
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                target.sideHit.getIndex()).getPicture();
        if (picture == null) {
            return;
        }
        event.setCanceled(true);
        if (this.onlyCancel) {
            return;
        }
        PaintingSide pside = PaintingSide.getSide(target.sideHit);
        double radius = tool.getPaintingToolRadius(stack, picture);
        int width = picture.getWidth();
        int height = picture.getHeight();
        Vec3d inBlockVec = GeometryUtils.getInBlockVec(target);
        Vec3d inPictureVec = pside.axes.toSideCoords(inBlockVec);
        double outline = 0.002D;
        double zOutline = inPictureVec.z + outline;
        int px = (int)(inPictureVec.x * width);
        int py = (int)(inPictureVec.y * height);
        int max = (int)Math.ceil(radius);
        Builder<AxisAlignedBB> pictureLinesBuilder = Stream.builder();
        {
            boolean[] lasty = new boolean[max * 2 + 1];
            final int offset = max;
            for (int iy = -max; iy <= max; ++iy) {
                boolean lastx = false;
                for (int ix = -max; ix <= max; ++ix) {
                    int cx = px + ix;
                    int cy = py + iy;
                    int nx = cx / width + (cx % width < 0 ? -1 : 0);
                    int ny = cy / height + (cy % height < 0 ? -1 : 0);
                    boolean candraw = (ix * ix + iy * iy < radius * radius)
                        && picture.canEdit(picture.getNeighborPicture(nx, ny));
                    if (candraw != lastx) {
                        AxisAlignedBB line = new AxisAlignedBB(
                                (double)(cx + 0) / width,
                                (double)(cy + 0) / height,
                                zOutline,
                                (double)(cx + 0) / width,
                                (double)(cy + 1) / height,
                                zOutline);
                        pictureLinesBuilder.add(line);
                    }
                    if (candraw != lasty[ix + offset]) {
                        AxisAlignedBB line = new AxisAlignedBB(
                                (double)(cx + 0) / width,
                                (double)(cy + 0) / height,
                                zOutline,
                                (double)(cx + 1) / width,
                                (double)(cy + 0) / height,
                                zOutline);
                        pictureLinesBuilder.add(line);
                        
                    }
                    lastx = candraw;
                    lasty[ix + offset] = candraw;
                }
            }
        }
        Stream<AxisAlignedBB> pictureLines = pictureLinesBuilder.build();
        double dx = player.lastTickPosX +
                    (player.posX - player.lastTickPosX) * (double)event.getPartialTicks();
        double dy = player.lastTickPosY +
                    (player.posY - player.lastTickPosY) * (double)event.getPartialTicks();
        double dz = player.lastTickPosZ +
                    (player.posZ - player.lastTickPosZ) * (double)event.getPartialTicks();
        Stream<AxisAlignedBB> frameLines = pictureLines.map(b->pside.axes.fromSideCoords(b)
                                                                         .offset(pos.getX(),
                                                                                 pos.getY(),
                                                                                 pos.getZ())
                                                                         .offset(-dx, -dy, -dz));
        this.drawLines(frameLines);
    }
    
    protected void drawLines(Stream<AxisAlignedBB> lines) {
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA,
                                 GL11.GL_ONE, GL11.GL_ZERO);
        GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.4F);
        GL11.glLineWidth(2.0F);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDepthMask(false);
        lines.forEach(box->this.drawLine(box));
        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }
    
    protected void drawLine(AxisAlignedBB box) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buf = tessellator.getBuffer();
        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        if (this.color != null) {
            buf.putColor4(this.color.getRGB());
        }
        if (box.minX != box.maxX) {
            buf.pos(box.minX, box.minY, box.minZ).endVertex();
            buf.pos(box.maxX, box.minY, box.minZ).endVertex();
        }
        if (box.minY != box.maxY) {
            buf.pos(box.minX, box.minY, box.minZ).endVertex();
            buf.pos(box.minX, box.maxY, box.minZ).endVertex();
        }
        if (box.minZ != box.maxZ) {
            buf.pos(box.minX, box.minY, box.minZ).endVertex();
            buf.pos(box.minX, box.minY, box.maxZ).endVertex();
        }
        tessellator.draw();
    }
}
