package com.marsproject.terraformingmars;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenIntroPayload() implements CustomPacketPayload {

    public static final Type<OpenIntroPayload> TYPE =
            new Type<>(new ResourceLocation("terraforming_mars", "open_intro"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenIntroPayload> STREAM_CODEC =
            StreamCodec.unit(new OpenIntroPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}