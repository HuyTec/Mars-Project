package com.marsproject.terraformingmars.world;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Optional;

/** Places the shared Mars landing base once and remembers its landing position. */
public final class MarsBaseManager extends SavedData {

    private static final String DATA_NAME = "terraforming_mars_base";
    private static final int PLACEMENT_VERSION = 3;
    private static final int TARGET_BASE_Y = 65;
    private static final ResourceLocation BASE_ID = new ResourceLocation(
            TerraformingMarsMod.MODID, "mars_base");

    private boolean generated;
    private int placementVersion;
    private BlockPos origin = BlockPos.ZERO;
    private BlockPos landingPos = BlockPos.ZERO;

    private MarsBaseManager() {
    }

    private static MarsBaseManager load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        MarsBaseManager data = new MarsBaseManager();
        data.generated = tag.getBoolean("Generated");
        data.placementVersion = tag.getInt("PlacementVersion");
        data.origin = readPos(tag, "Origin");
        data.landingPos = readPos(tag, "LandingPos");
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        tag.putBoolean("Generated", generated);
        tag.putInt("PlacementVersion", placementVersion);
        writePos(tag, "Origin", origin);
        writePos(tag, "LandingPos", landingPos);
        return tag;
    }

    /** Generates the base if required and returns a safe arrival position. */
    public static Optional<BlockPos> ensureGenerated(ServerLevel marsLevel) {
        ServerLevel overworld = marsLevel.getServer().overworld();
        SavedData.Factory<MarsBaseManager> factory = new SavedData.Factory<>(
                MarsBaseManager::new, MarsBaseManager::load, null);
        MarsBaseManager data = overworld.getDataStorage().computeIfAbsent(factory, DATA_NAME);

        if (data.generated && data.placementVersion >= PLACEMENT_VERSION) {
            return Optional.of(data.landingPos);
        }
        if (data.generated) {
            TerraformingMarsMod.LOGGER.warn(
                    "Mars base was generated with placement version {} at {}. "
                            + "Generating corrected version {} without deleting the old structure.",
                    data.placementVersion, data.origin, PLACEMENT_VERSION);
            data.generated = false;
        }

        Optional<StructureTemplate> optionalTemplate = marsLevel.getStructureManager().get(BASE_ID);
        if (optionalTemplate.isEmpty()) {
            TerraformingMarsMod.LOGGER.error(
                    "Missing structure {}. Expected data/{}/structures/mars_base.nbt",
                    BASE_ID, TerraformingMarsMod.MODID);
            return Optional.empty();
        }

        StructureTemplate template = optionalTemplate.get();
        Vec3i size = template.getSize();
        if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) {
            TerraformingMarsMod.LOGGER.error("Structure {} is empty", BASE_ID);
            return Optional.empty();
        }

        BlockPos sharedSpawn = marsLevel.getSharedSpawnPos();
        int originX = sharedSpawn.getX() - size.getX() / 2;
        int originZ = sharedSpawn.getZ() - size.getZ() / 2;
        int originY = basePlacementY(marsLevel, size);
        BlockPos origin = new BlockPos(originX, originY, originZ);

        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(Rotation.NONE)
                .setIgnoreEntities(false);
        boolean placed = template.placeInWorld(
                marsLevel, origin, origin, settings, RandomSource.create(), 2);
        if (!placed) {
            TerraformingMarsMod.LOGGER.error("Could not place Mars base {} at {}", BASE_ID, origin);
            return Optional.empty();
        }

        data.generated = true;
        data.placementVersion = PLACEMENT_VERSION;
        data.origin = origin;
        data.landingPos = findSafeLandingPosition(marsLevel, origin, size);
        data.setDirty();

        TerraformingMarsMod.LOGGER.info(
                "Generated Mars base {} at {}; player landing position is {}",
                BASE_ID, data.origin, data.landingPos);
        return Optional.of(data.landingPos);
    }

    private static int basePlacementY(ServerLevel level, Vec3i size) {
        int minimumOriginY = level.getMinBuildHeight();
        int maximumOriginY = level.getMaxBuildHeight() - size.getY() - 1;
        return Math.max(minimumOriginY, Math.min(TARGET_BASE_Y, maximumOriginY));
    }

    private static BlockPos findSafeLandingPosition(ServerLevel level, BlockPos origin, Vec3i size) {
        int centerX = origin.getX() + size.getX() / 2;
        int centerZ = origin.getZ() + size.getZ() / 2;
        BlockPos center = new BlockPos(centerX, origin.getY(), centerZ);
        BlockPos best = null;
        double bestDistance = Double.MAX_VALUE;

        for (int y = origin.getY() + 1; y < origin.getY() + size.getY(); y++) {
            for (int x = origin.getX(); x < origin.getX() + size.getX(); x++) {
                for (int z = origin.getZ(); z < origin.getZ() + size.getZ(); z++) {
                    BlockPos feet = new BlockPos(x, y, z);
                    if (!hasRoomForPlayer(level, feet)) {
                        continue;
                    }
                    double distance = feet.distSqr(center);
                    if (distance < bestDistance) {
                        best = feet;
                        bestDistance = distance;
                    }
                }
            }
        }

        if (best != null) {
            return best;
        }
        return new BlockPos(centerX, origin.getY() + size.getY() + 1, centerZ);
    }

    private static boolean hasRoomForPlayer(ServerLevel level, BlockPos feet) {
        return level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()
                && !level.getBlockState(feet.below()).getCollisionShape(level, feet.below()).isEmpty();
    }

    private static void writePos(CompoundTag tag, String key, BlockPos pos) {
        CompoundTag posTag = new CompoundTag();
        posTag.putInt("X", pos.getX());
        posTag.putInt("Y", pos.getY());
        posTag.putInt("Z", pos.getZ());
        tag.put(key, posTag);
    }

    private static BlockPos readPos(CompoundTag tag, String key) {
        CompoundTag posTag = tag.getCompound(key);
        return new BlockPos(posTag.getInt("X"), posTag.getInt("Y"), posTag.getInt("Z"));
    }
}