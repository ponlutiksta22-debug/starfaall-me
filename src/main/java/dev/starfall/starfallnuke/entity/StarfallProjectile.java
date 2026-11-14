package dev.starfall.starfallnuke.entity;

import dev.starfall.starfallnuke.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.network.NetworkHooks;

import java.util.List;

public class StarfallProjectile extends ThrowableItemProjectile {
    private boolean detonated = false;

    public StarfallProjectile(EntityType<? extends StarfallProjectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.STARFALL_CORE.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            for (int i = 0; i < 4; i++) {
                double ox = (random.nextDouble() - 0.5) * 0.45;
                double oy = (random.nextDouble() - 0.5) * 0.45;
                double oz = (random.nextDouble() - 0.5) * 0.45;
                level().addParticle(ParticleTypes.FLAME, getX() + ox, getY() + oy, getZ() + oz,
                        -getDeltaMovement().x * 0.15, -getDeltaMovement().y * 0.15 + 0.03, -getDeltaMovement().z * 0.15);
                level().addParticle(ParticleTypes.END_ROD, getX() + ox, getY() + oy, getZ() + oz,
                        0.0, 0.02, 0.0);
            }
            level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(),
                    0.0, 0.05, 0.0);
        }
        if (tickCount > 240) {
            discard();
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!level().isClientSide && !detonated) {
            detonated = true;
            double x = getX();
            double y = getY();
            double z = getZ();

            level().explode(this, x, y, z, 28.0F, true, Level.ExplosionInteraction.TNT);

            double ringRadius = 11.0;
            for (int i = 0; i < 8; i++) {
                double angle = (Math.PI * 2.0 / 8.0) * i;
                double rx = x + Math.cos(angle) * ringRadius;
                double rz = z + Math.sin(angle) * ringRadius;
                level().explode(this, rx, y - 1.5, rz, 14.0F, true, Level.ExplosionInteraction.TNT);
            }

            BlockPos center = BlockPos.containing(x, y - 2, z);
            for (int dx = -6; dx <= 6; dx++) {
                for (int dz = -6; dz <= 6; dz++) {
                    if (dx * dx + dz * dz <= 36 && random.nextFloat() < 0.35F) {
                        BlockPos targetPos = center.offset(dx, -2, dz);
                        if (!level().getBlockState(targetPos).isAir()
                                && level().getBlockState(targetPos).getDestroySpeed(level(), targetPos) >= 0) {
                            level().setBlockAndUpdate(targetPos,
                                    random.nextBoolean() ? Blocks.MAGMA_BLOCK.defaultBlockState() : Blocks.OBSIDIAN.defaultBlockState());
                        }
                    }
                }
            }

            AABB area = new AABB(x - 45, y - 45, z - 45, x + 45, y + 45, z + 45);
            List<LivingEntity> entities = level().getEntitiesOfClass(LivingEntity.class, area);
            for (LivingEntity entity : entities) {
                double dist = Math.sqrt(entity.distanceToSqr(x, y, z));
                if (dist <= 45.0) {
                    entity.setSecondsOnFire(12);
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 80, 0));
                    entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 160, 1));
                    entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                }
            }

            if (level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y + 2, z, 25, 8.0, 6.0, 8.0, 0.1);
                serverLevel.sendParticles(ParticleTypes.FLAME, x, y + 4, z, 350, 12.0, 10.0, 12.0, 0.25);
                serverLevel.sendParticles(ParticleTypes.END_ROD, x, y + 8, z, 250, 14.0, 14.0, 14.0, 0.2);
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE, x, y + 12, z, 200, 7.0, 12.0, 7.0, 0.08);
            }

            level().playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 8.0F, 0.45F);
            level().playSound(null, x, y, z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.WEATHER, 8.0F, 0.6F);

            discard();
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
