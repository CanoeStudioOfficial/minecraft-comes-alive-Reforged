package mca.client.render;

import mca.blocks.BlockTombstone;
import mca.blocks.TileEntityTombstone;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;

public class TileEntityTombstoneRenderer extends TileEntitySpecialRenderer<TileEntityTombstone> {

    @Override
    public void render(TileEntityTombstone te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!te.hasEntity()) {
            return;
        }

        FontRenderer fontRenderer = getFontRenderer();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5, y + 0.5, z + 0.5);

        EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockTombstone.FACING);
        float rotation = -facing.getHorizontalAngle();
        GlStateManager.rotate(rotation, 0, 1, 0);

        if (te.getBlockType() instanceof BlockTombstone) {
            BlockTombstone block = (BlockTombstone) te.getBlockType();
            GlStateManager.rotate(block.getRotation(), 1, 0, 0);

            Vec3d offset = block.getNameplateOffset();
            GlStateManager.translate(offset.x / 100.0, offset.y / 100.0, offset.z / 100.0);
        }

        GlStateManager.scale(0.010416667F, -0.010416667F, 0.010416667F);
        GlStateManager.glNormal3f(0.0F, 0.0F, -1.0F);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

        // Draw header
        String header = net.minecraft.util.text.translation.I18n.translateToLocal("block.mca.tombstone.header");
        int headerWidth = fontRenderer.getStringWidth(header);
        fontRenderer.drawString(header, -headerWidth / 2, 0, 0xFFFFFFFF);

        // Draw entity name
        String name = te.getEntityName().orElse("Unknown");
        int nameWidth = fontRenderer.getStringWidth(name);
        fontRenderer.drawString(name, -nameWidth / 2, 15, 0xFFFFFFFF);

        // Draw footer based on gender
        String footer = net.minecraft.util.text.translation.I18n.translateToLocal("block.mca.tombstone.footer." + te.getGender().getStrName());
        int footerWidth = fontRenderer.getStringWidth(footer);
        fontRenderer.drawString(footer, -footerWidth / 2, 30, 0xFFFFFFFF);

        GlStateManager.popMatrix();
    }

    @Override
    public boolean isGlobalRenderer(TileEntityTombstone te) {
        return false;
    }
}
