package mca.client.particle;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleSimpleAnimated;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class ParticleInteraction extends ParticleSimpleAnimated {
    protected ParticleInteraction(World world, double x, double y, double z) {
        super(world, x, y, z, 0, 0, 0);
        this.motionX *= 0.01F;
        this.motionY *= 0.01F;
        this.motionZ *= 0.01F;
        this.motionY += 0.1D;
        this.particleScale *= 1.5F;
        this.particleMaxAge = 20;
        this.canCollide = false;
        this.setParticleTextureIndex(0);
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        } else {
            if (this.posY == this.prevPosY) {
                this.motionX *= 1.1D;
                this.motionZ *= 1.1D;
            }

            this.motionX *= 0.86F;
            this.motionY *= 0.86F;
            this.motionZ *= 0.86F;

            if (this.onGround) {
                this.motionX *= 0.7F;
                this.motionZ *= 0.7F;
            }
        }
    }

    @Override
    public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        float f = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
        this.particleScale = 0.3F * (1.0F - f);
        super.renderParticle(buffer, entityIn, partialTicks, rotationX, rotationZ, rotationYZ, rotationXY, rotationXZ);
    }

    @Override
    public int getBrightnessForRender(float partialTick) {
        return 15728880;
    }

    public static class Factory implements IParticleFactory {
        @Override
        public Particle createParticle(int particleID, World world, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int... args) {
            ParticleInteraction particle = new ParticleInteraction(world, x, y + 0.5D, z);
            particle.setRBGColorF(1.0F, 1.0F, 1.0F);
            return particle;
        }
    }
}
