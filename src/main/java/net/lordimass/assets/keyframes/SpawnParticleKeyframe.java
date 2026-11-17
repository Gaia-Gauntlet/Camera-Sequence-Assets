package net.lordimass.assets.keyframes;

import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.Color;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.codec.ProtocolCodecs;
import com.hypixel.hytale.server.core.codec.protocol.ColorCodec;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ParticleSystemExistsValidator;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

public class SpawnParticleKeyframe extends GlobalKeyframe {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final BuilderCodec<@NotNull SpawnParticleKeyframe> CODEC = BuilderCodec
        .builder(SpawnParticleKeyframe.class, SpawnParticleKeyframe::new, GlobalKeyframe.ABSTRACT_CODEC)
        .documentation("Spawn a particle effect")
        .appendInherited(new KeyedCodec<>("ParticleName", Codec.STRING),
            (c, v) -> c.particleName = v,
            c -> c.particleName,
            (a, b) -> a.particleName = b.particleName)
        .addValidator(new AssetKeyValidator<>(ParticleSystem::getAssetStore))
        .add()
        .appendInherited(new KeyedCodec<>("TargetTransform", Transform.CODEC_DEGREES),
            (c, v) -> c.targetTransform = v,
            c -> c.targetTransform,
            (a, b) -> a.targetTransform = b.targetTransform)
        .add()
        .appendInherited(new KeyedCodec<>("Scale", Codec.FLOAT),
            (c, v) -> c.scale = v,
            c -> c.scale,
            (a, b) -> a.scale = b.scale)
        .add()
        .appendInherited(new KeyedCodec<>("Color", ProtocolCodecs.COLOR),
            (c, v) -> c.color = v,
            c -> c.color,
            (a, b) -> a.color = b.color)
        .add()
        .appendInherited(new KeyedCodec<>("MaxDuration", Codec.FLOAT),
            (c, v) -> c.maxDuration = v,
            c -> c.maxDuration,
            (a, b) -> a.maxDuration = b.maxDuration)
        .add()
        .appendInherited(new KeyedCodec<>("RelativeToPlayer", Codec.BOOLEAN),
            (c, v) -> c.relativeToPlayer = v,
            c -> c.relativeToPlayer,
            (a, b) -> a.relativeToPlayer = b.relativeToPlayer)
        .documentation("Whether the particle spawn should be relative to the player, or global.")
        .add()
        .build();

    @Getter private String particleName;
    @Getter private Transform targetTransform = new Transform(new Vector3d(), new Rotation3f(0,0,0));
    @Getter private float scale = 1;
    @Nullable @Getter private Color color;
    @Getter float maxDuration;
    @Getter private boolean relativeToPlayer;

    @Override
    public void run(SequenceContext context, @Nullable Runnable onCompletion) {
        var world = context.world();
        var targetPlayers = new HashSet<PlayerRef>();

        if (relativeToPlayer && context.targetPlayer() != null) {
            targetPlayers.add(context.targetPlayer());
        } else if (relativeToPlayer) {
            targetPlayers.addAll(world.getPlayerRefs());
        }

        var spawnLocations = new HashSet<Transform>();
        for (PlayerRef targetPlayer : targetPlayers) {
            var calculatedTargetTransform = targetTransform.clone();
            var ref = targetPlayer.getReference();
            if (ref == null) continue;
            var store = ref.getStore();
            if (relativeToPlayer) {
                var playerTransformComp = store.getComponent(ref, TransformComponent.getComponentType());
                var playerHeadRotation = store.getComponent(ref, HeadRotation.getComponentType());
                if (playerTransformComp == null || playerHeadRotation == null) continue;
                calculatedTargetTransform.getPosition().add(playerTransformComp.getPosition());
                calculatedTargetTransform.getRotation().add(playerHeadRotation.getRotation());
            }
            spawnLocations.add(calculatedTargetTransform);
        }
        if (spawnLocations.isEmpty()) spawnLocations.add(targetTransform);

        for (Transform spawnLocation : spawnLocations) {
            var pos = spawnLocation.getPosition();
            var rot = spawnLocation.getRotation();
            var targetRefs = new ArrayList<Ref<EntityStore>>();
            for (PlayerRef targetPlayer : targetPlayers.isEmpty() ? world.getPlayerRefs() : targetPlayers) {
                targetRefs.add(targetPlayer.getReference());
            }

            ParticleUtil.spawnParticleEffect(
                particleName,
                pos.x, pos.y, pos.z,
                rot.x, rot.y, rot.z,
                scale,
                color,
                null,
                targetRefs,
                world.getEntityStore().getStore(),
                maxDuration
            );
        }

        if (onCompletion != null) onCompletion.run();
    }
}
