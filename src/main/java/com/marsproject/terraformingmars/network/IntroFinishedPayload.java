package com.marsproject.terraformingmars;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record IntroFinishedPayload() implements CustomPacketPayload {

    public static final Type<IntroFinishedPayload> TYPE =
            new Type<>(new ResourceLocation("terraforming_mars", "intro_finished"));

    public static final StreamCodec<RegistryFriendlyByteBuf, IntroFinishedPayload> STREAM_CODEC =
            StreamCodec.unit(new IntroFinishedPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}