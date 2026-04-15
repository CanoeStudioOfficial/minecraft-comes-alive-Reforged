package mca.blocks;

import net.minecraft.block.BlockFire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockInfernalFlame extends BlockFire {
    public BlockInfernalFlame() {
        super();
        this.setHardness(0.0F);
        this.setLightLevel(1.0F);
        this.setTickRandomly(true);
    }

    @Override
    public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        // 地狱火焰不会熄灭，不需要调用super.updateTick
        // 这样可以防止火焰自然熄灭
    }

    @Override
    public int tickRate(World worldIn) {
        // 更快的tick速率
        return 10;
    }
}
