package com.marsproject.terraformingmars.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import com.marsproject.terraformingmars.TerraformingMarsMod;

public record MarsEnvironmentSyncPayload(
        double radiation,
        double atmospherePressurePercent,
        double oxygenPercent,
        double temperatureCelsius,
        double waterPercent,
        double biologyPercent,
        boolean canLive,
        double terraformProgress
) implements CustomPacketPayload {

    public static final Type<MarsEnvironmentSyncPayload> TYPE =
            new Type<>(new ResourceLocation(TerraformingMarsMod.MODID, "mars_environment_sync"));

    public static final StreamCodec<ByteBuf, MarsEnvironmentSyncPayload> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public MarsEnvironmentSyncPayload decode(ByteBuf buf) {
                    return new MarsEnvironmentSyncPayload(
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readDouble(),
                            buf.readBoolean(),
                            buf.readDouble()
                            );
                }

                @Override
                public void encode(ByteBuf buf, MarsEnvironmentSyncPayload payload) {
                    buf.writeDouble(payload.radiation());
                    buf.writeDouble(payload.atmospherePressurePercent());
                    buf.writeDouble(payload.oxygenPercent());
                    buf.writeDouble(payload.temperatureCelsius());
                    buf.writeDouble(payload.waterPercent());
                    buf.writeDouble(payload.biologyPercent());
                    buf.writeBoolean(payload.canLive());
                    buf.writeDouble(payload.terraformProgress());
                }
            };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}