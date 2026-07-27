package com.marsproject.terraformingmars.block;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import java.util.List;

/**
 * Immutable properties shared by every placed solar array of the same tier.
 *
 * <p>{@code dustAccumulationRate} is efficiency penalty accumulated per
 * server tick. {@code maxDustPenalty} is a fraction in the range 0..1.</p>
 */
public record SolarArrayType(
        int baseWattage,
        float dustAccumulationRate,
        float maxDustPenalty,
        float modelScale,
        double width,
        double height,
        double depth,
        ResourceLocation model,
        ResourceLocation texture,
        ResourceLocation animation,
        List<MultiblockPart> parts
) {
    public SolarArrayType {
        parts = List.copyOf(parts);
        if (baseWattage < 0) {
            throw new IllegalArgumentException("baseWattage must be non-negative");
        }
        if (dustAccumulationRate < 0.0F) {
            throw new IllegalArgumentException("dustAccumulationRate must be non-negative");
        }
        if (maxDustPenalty < 0.0F || maxDustPenalty > 1.0F) {
            throw new IllegalArgumentException("maxDustPenalty must be between 0 and 1");
        }
        if (modelScale <= 0.0F || width <= 0.0 || height <= 0.0 || depth <= 0.0) {
            throw new IllegalArgumentException("Dimensions and modelScale must be positive");
        }
    }

    public VoxelShape shape() {
        double halfWidth = width / 2.0;
        double halfDepth = depth / 2.0;
        return Shapes.create(
                0.5 - halfWidth, 0.0, 0.5 - halfDepth,
                0.5 + halfWidth, height, 0.5 + halfDepth
        );
    }
}
