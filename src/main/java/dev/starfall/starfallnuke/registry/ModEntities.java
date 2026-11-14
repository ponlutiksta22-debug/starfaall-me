package dev.starfall.starfallnuke.registry;

import dev.starfall.starfallnuke.StarfallNuke;
import dev.starfall.starfallnuke.entity.StarfallProjectile;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, StarfallNuke.MOD_ID);

    public static final RegistryObject<EntityType<StarfallProjectile>> STARFALL_PROJECTILE =
            ENTITY_TYPES.register("starfall_projectile", () ->
                    EntityType.Builder.<StarfallProjectile>of(StarfallProjectile::new, MobCategory.MISC)
                            .sized(0.5F, 0.5F)
                            .clientTrackingRange(8)
                            .updateInterval(10)
                            .build(StarfallNuke.MOD_ID + ":starfall_projectile"));
}
