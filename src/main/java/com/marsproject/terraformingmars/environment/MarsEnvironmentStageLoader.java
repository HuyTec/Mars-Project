package com.marsproject.terraformingmars.environment;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import com.marsproject.terraformingmars.TerraformingMarsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MarsEnvironmentStageLoader extends SimpleJsonResourceReloadListener {

    private static final String FOLDER = "mars_environment";
    private static List<MarsEnvironmentStage> stages = List.of();

    public MarsEnvironmentStageLoader() {
        super(new Gson(), FOLDER);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resourceList,
                         ResourceManager resourceManager, ProfilerFiller profiler) {
        List<MarsEnvironmentStage> loaded = new ArrayList<>();

        resourceList.forEach((id, json) ->
                MarsEnvironmentStage.CODEC.parse(JsonOps.INSTANCE, json)
                        .resultOrPartial(error ->
                                TerraformingMarsMod.LOGGER.error("Loi doc mars_environment {}: {}", id, error))
                        .ifPresent(loaded::add));

        loaded.sort(Comparator.comparingDouble(MarsEnvironmentStage::progress));
        stages = List.copyOf(loaded);
        TerraformingMarsMod.LOGGER.info("Da nap {} mars environment stage(s)", stages.size());
    }

    public static List<MarsEnvironmentStage> getStages() {
        return stages;
    }
}