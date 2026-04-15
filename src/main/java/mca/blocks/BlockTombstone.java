package mca.blocks;

import mca.core.MCA;
import mca.enums.EnumGender;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BlockTombstone extends Block implements ITileEntityProvider {
    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    public static final AxisAlignedBB GRAVELLING_SHAPE = new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 0.0625, 0.9375);
    public static final AxisAlignedBB UPRIGHT_SHAPE = new AxisAlignedBB(0.125, 0, 0.4375, 0.875, 0.9375, 0.5625)
            .union(new AxisAlignedBB(0.0625, 0, 0.375, 0.9375, 0.125, 0.625));
    public static final AxisAlignedBB CROSS_SHAPE = new AxisAlignedBB(0.375, 0, 0.125, 0.625, 1.75, 0.25)
            .union(new AxisAlignedBB(-0.0625, 1.125, 0.125, 1.0625, 1.3125, 0.25));
    public static final AxisAlignedBB SLANTED_SHAPE = new AxisAlignedBB(0, 0, 0.125, 1, 0.4375, 0.875);
    public static final AxisAlignedBB WALL_SHAPE = new AxisAlignedBB(0.0625, 0.0625, 0, 0.9375, 0.9375, 0.0625);

    private final Map<EnumFacing, AxisAlignedBB> shapes;

    private final int lineWidth;
    private final int maxNameHeight;
    private final Vec3d nameplateOffset;
    private final boolean requiresSolid;
    private final float rotation;
    private final AxisAlignedBB baseShape;

    public BlockTombstone(Material material, int lineWidth, int maxNameHeight, Vec3d nameplateOffset, float rotation, boolean requiresSolid, AxisAlignedBB baseShape) {
        super(material);
        this.lineWidth = lineWidth;
        this.maxNameHeight = maxNameHeight;
        this.nameplateOffset = nameplateOffset;
        this.rotation = rotation;
        this.requiresSolid = requiresSolid;
        this.baseShape = baseShape;

        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        setCreativeTab(MCA.creativeTab);

        shapes = Arrays.stream(EnumFacing.values())
                .filter(d -> d.getAxis() != EnumFacing.Axis.Y)
                .collect(Collectors.toMap(
                        Function.identity(),
                        this::rotateShape)
                );
    }

    private AxisAlignedBB rotateShape(EnumFacing facing) {
        return rotateAABB(baseShape, facing);
    }

    private AxisAlignedBB rotateAABB(AxisAlignedBB box, EnumFacing facing) {
        switch (facing) {
            case SOUTH:
                return new AxisAlignedBB(1 - box.maxX, box.minY, 1 - box.maxZ, 1 - box.minX, box.maxY, 1 - box.minZ);
            case WEST:
                return new AxisAlignedBB(box.minZ, box.minY, 1 - box.maxX, box.maxZ, box.maxY, 1 - box.minX);
            case EAST:
                return new AxisAlignedBB(1 - box.maxZ, box.minY, box.minX, 1 - box.minZ, box.maxY, box.maxX);
            case NORTH:
            default:
                return box;
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        EnumFacing facing = state.getValue(FACING);
        return shapes.getOrDefault(facing, FULL_BLOCK_AABB);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityTombstone();
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);

        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityTombstone) {
            ((TileEntityTombstone) tile).readFromStack(stack);
        }
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byHorizontalIndex(meta);
        if (facing.getAxis() == EnumFacing.Axis.Y) {
            facing = EnumFacing.NORTH;
        }
        return getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        if (requiresSolid) {
            BlockPos down = pos.down();
            return worldIn.getBlockState(down).isSideSolid(worldIn, down, EnumFacing.UP);
        }
        return super.canPlaceBlockAt(worldIn, pos);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (requiresSolid && fromPos.equals(pos.down())) {
            if (!worldIn.getBlockState(fromPos).isSideSolid(worldIn, fromPos, EnumFacing.UP)) {
                worldIn.destroyBlock(pos, true);
            }
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) {
            return true;
        }

        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityTombstone) {
            TileEntityTombstone tombstone = (TileEntityTombstone) tile;
            if (tombstone.hasEntity()) {
                playerIn.sendMessage(new TextComponentTranslation("block.mca.tombstone.contains", tombstone.getEntityName().orElse("Unknown")));
            } else {
                playerIn.sendMessage(new TextComponentTranslation("block.mca.tombstone.empty"));
            }
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityTombstone) {
            TileEntityTombstone tombstone = (TileEntityTombstone) tile;
            if (tombstone.hasEntity()) {
                ItemStack stack = new ItemStack(this);
                NBTTagCompound tag = new NBTTagCompound();
                tombstone.writeToNBT(tag);
                stack.setTagCompound(tag);
                spawnAsEntity(worldIn, pos, stack);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean canProvidePower(IBlockState state) {
        return true;
    }

    @Override
    public int getWeakPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        return blockState.getValue(FACING) == side ? getStrongPower(blockState, blockAccess, pos, side) : 0;
    }

    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {
        TileEntity tile = blockAccess.getTileEntity(pos);
        if (tile instanceof TileEntityTombstone) {
            return ((TileEntityTombstone) tile).hasEntity() ? 15 : 0;
        }
        return 0;
    }

    public int getLineWidth() {
        return lineWidth;
    }

    public int getMaxNameHeight() {
        return maxNameHeight;
    }

    public Vec3d getNameplateOffset() {
        return nameplateOffset;
    }

    public float getRotation() {
        return rotation;
    }
}
