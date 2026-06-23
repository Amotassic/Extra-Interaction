package com.amotassic.extra_interaction.network;

import com.amotassic.extra_interaction.ExtraInteraction;
import com.amotassic.extra_interaction.interaction.Interactions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record SendInteraction(String name, Object blockPosOrEntityId) implements CustomPacketPayload {
    public static final Type<SendInteraction> TYPE = new Type<>(ExtraInteraction.id("send_interaction"));
    public static final StreamCodec<FriendlyByteBuf, SendInteraction> CODEC =
            StreamCodec.ofMember(SendInteraction::write, SendInteraction::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {return TYPE;}

    public SendInteraction(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readBoolean() ? buf.readInt() : buf.readBlockPos());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        switch (blockPosOrEntityId) {
            case Integer id -> {
                buf.writeBoolean(true);
                buf.writeInt(id);
            }
            case Entity entity -> {
                buf.writeBoolean(true);
                buf.writeInt(entity.getId());
            }
            case BlockPos pos -> {
                buf.writeBoolean(false);
                buf.writeBlockPos(pos);
            }
            case null, default -> {
                buf.writeBoolean(true);
                buf.writeInt(-1);
            }
        }
    }

    public static void handle(Player player, SendInteraction packet) {
        Interactions.applyAction(player, packet.blockPosOrEntityId(), packet.name());
    }
}
