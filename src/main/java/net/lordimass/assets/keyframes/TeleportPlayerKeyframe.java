package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.EasingType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.lordimass.components.NpcSelector;
import net.lordimass.components.TransformingComponent;
import net.lordimass.utils.SequenceNpcUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;

import static net.lordimass.utils.TransformUtils.EASING_CODEC;

public class TeleportPlayerKeyframe extends GlobalKeyframe {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<@NotNull TeleportPlayerKeyframe> CODEC = BuilderCodec
        .builder(TeleportPlayerKeyframe.class, TeleportPlayerKeyframe::new, GlobalKeyframe.ABSTRACT_CODEC)
        .documentation("Teleport the player to a new location")
        .appendInherited(new KeyedCodec<>("TargetTransform", Transform.CODEC_DEGREES),
            (c, v) -> c.targetTransform = v,
            c -> c.targetTransform,
            (a, b) -> a.targetTransform = b.targetTransform)
        .add()
        .appendInherited(new KeyedCodec<>("HeadRotation", Rotation3f.PARTIAL_TELEPORT_CODEC),
            (c, v) -> c.headRotation = v,
            c -> c.headRotation,
            (a, b) -> a.headRotation = b.headRotation)
        .add()
        .appendInherited(new KeyedCodec<>("RelativeToPlayer", Codec.BOOLEAN),
            (c, v) -> c.relativeToPlayer = v,
            c -> c.relativeToPlayer,
            (a, b) -> a.relativeToPlayer = b.relativeToPlayer)
        .documentation("Whether the movement should be relative to the current position/rotation " +
            "of the player, or global.")
        .add()
        .build();

    @Getter private Transform targetTransform = new Transform(new Vector3d(), new Rotation3f(0,0,0));
    @Getter @Nullable private Rotation3f headRotation;
    @Getter private boolean relativeToPlayer;

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
            var calculatedTargetTransform = targetTransform.clone();
            var ref = targetPlayer.getReference();
            if (ref == null) continue;
            var store = ref.getStore();
            if (relativeToPlayer) {
                var playerTransformComp = store.getComponent(ref, TransformComponent.getComponentType());
                if (playerTransformComp == null) continue;
                calculatedTargetTransform.getPosition().add(playerTransformComp.getPosition());
                calculatedTargetTransform.getRotation().add(playerTransformComp.getRotation());
            }
            var teleport = Teleport.createForPlayer(world, targetTransform);
            if (headRotation != null) teleport.setHeadRotation(headRotation);
            store.addComponent(ref, Teleport.getComponentType(), teleport);
        }

        if (onCompletion != null) onCompletion.run();
    }
}
