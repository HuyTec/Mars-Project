package com.marsproject.terraformingmars.network;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record MarsWeatherSyncPayload(
        int weatherType,
        int intensity,
        int ticksRemaining,
        double windX,
        double windZ
) implements CustomPacketPayload {
    public static final Type<MarsWeatherSyncPayload> TYPE =
            new Type<>(new ResourceLocation(TerraformingMarsMod.MODID, "mars_weather_sync"));

    public static final StreamCodec<ByteBuf, MarsWeatherSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public MarsWeatherSyncPayload decode(ByteBuf buffer) {
            return new MarsWeatherSyncPayload(
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readInt(),
                    buffer.readDouble(),
                    buffer.readDouble()
            );
        }

        @Override
        public void encode(ByteBuf buffer, MarsWeatherSyncPayload payload) {
            buffer.writeInt(payload.weatherType());
            buffer.writeInt(payload.intensity());
            buffer.writeInt(payload.ticksRemaining());
            buffer.writeDouble(payload.windX());
            buffer.writeDouble(payload.windZ());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
