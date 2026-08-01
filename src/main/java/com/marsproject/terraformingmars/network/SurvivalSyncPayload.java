package com.marsproject.terraformingmars.network;

import com.marsproject.terraformingmars.TerraformingMarsMod;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record SurvivalSyncPayload(float thirst, double bodyTemperature,
                                  int suitOxygen, int suitOxygenCapacity, boolean suitSealed)
        implements CustomPacketPayload {

    public static final Type<SurvivalSyncPayload> TYPE = new Type<>(
            new ResourceLocation(TerraformingMarsMod.MODID, "survival_sync"));

    public static final StreamCodec<ByteBuf, SurvivalSyncPayload> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public SurvivalSyncPayload decode(ByteBuf buffer) {
            return new SurvivalSyncPayload(buffer.readFloat(), buffer.readDouble(),
                    buffer.readInt(), buffer.readInt(), buffer.readBoolean());
        }

        @Override
        public void encode(ByteBuf buffer, SurvivalSyncPayload payload) {
            buffer.writeFloat(payload.thirst());
            buffer.writeDouble(payload.bodyTemperature());
            buffer.writeInt(payload.suitOxygen());
            buffer.writeInt(payload.suitOxygenCapacity());
            buffer.writeBoolean(payload.suitSealed());
        }
    };

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
