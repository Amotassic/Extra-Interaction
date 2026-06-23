package com.amotassic.extra_interaction.network;

import com.amotassic.extra_interaction.ExtraInteraction;
import com.amotassic.extra_interaction.interaction.EntityTalkData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

public record SendTalkData(Map<Identifier, String> talkData) implements CustomPacketPayload {
    public static final Type<SendTalkData> TYPE = new Type<>(ExtraInteraction.id("send_talk_data"));
    public static final StreamCodec<FriendlyByteBuf, SendTalkData> CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, ByteBufCodecs.STRING_UTF8),
            SendTalkData::talkData, SendTalkData::new
    );

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {return TYPE;}

    public static void handle(SendTalkData packet) {
        EntityTalkData.loadEntityTalk(packet.talkData());
    }
}
