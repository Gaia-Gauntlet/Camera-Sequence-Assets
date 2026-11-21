package net.lordimass.assets.keyframes;

import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;

public class TeleportPlayerHistoryKeyframe extends GlobalKeyframe {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<@NotNull TeleportPlayerHistoryKeyframe> CODEC = BuilderCodec
        .builder(TeleportPlayerHistoryKeyframe.class, TeleportPlayerHistoryKeyframe::new, GlobalKeyframe.ABSTRACT_CODEC)
        .documentation("Teleport the player back (or forwards) in their Teleport history")
        .appendInherited(new KeyedCodec<>("Forwards", Codec.INTEGER),
            (c, v) -> c.forwards = v,
            c -> c.forwards,
            (a, b) -> a.forwards = b.forwards)
        .add()
        .appendInherited(new KeyedCodec<>("Backwards", Codec.INTEGER),
            (c, v) -> c.backwards = v,
            c -> c.backwards,
            (a, b) -> a.backwards = b.backwards)
        .add()
        .build();

    @Getter private int forwards;
    @Getter private int backwards;

    @Override
    public void run(SequenceContext context, @Nullable Runnable onCompletion) {
        var world = context.world();
        var targetPlayers = new HashSet<PlayerRef>();

        if (context.targetPlayer() != null) {
            targetPlayers.add(context.targetPlayer());
        } else {
            targetPlayers.addAll(world.getPlayerRefs());
        }
        for (PlayerRef targetPlayer : targetPlayers) {
            var ref = targetPlayer.getReference();
            if (ref == null) continue;
            var store = ref.getStore();
            final var history = store.ensureAndGetComponent(ref, TeleportHistory.getComponentType());

            var count = forwards - backwards;
            if (count > 0) history.forward(ref, count);
            else history.back(ref, -1*count);
        }

        if (onCompletion != null) onCompletion.run();
    }
}
