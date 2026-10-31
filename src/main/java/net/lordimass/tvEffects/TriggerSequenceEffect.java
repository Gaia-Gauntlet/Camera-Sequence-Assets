package net.lordimass.tvEffects;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import net.lordimass.assets.sequences.SequenceAsset;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;

public class TriggerSequenceEffect extends TriggerEffect {

    @Nonnull public static final BuilderCodec<@NotNull TriggerSequenceEffect> CODEC = BuilderCodec
        .builder(TriggerSequenceEffect.class, TriggerSequenceEffect::new, TriggerEffect.BASE_CODEC)
        .append(new KeyedCodec<>("Sequence", Codec.STRING),
            (s, v) -> s.sequenceKey = v,
            TriggerSequenceEffect::getSequenceKey
            )
        .documentation("The ID of a Sequence asset to play.")
        .addValidator(SequenceAsset.VALIDATOR_CACHE.getValidator())
        .add()
        .append(new KeyedCodec<>("Global", Codec.BOOLEAN),
            (s, v) -> s.global = v,
            TriggerSequenceEffect::isGlobal
        )
        .documentation("Whether to play the sequence globally, or just to the player who triggered the effect")
        .add()
        .build();

    @Getter private String sequenceKey;
    @Getter private boolean global = false;

    public TriggerSequenceEffect() {}

    @Override
    public void execute(@NonNull TriggerContext triggerContext) {
        Ref<EntityStore> ref = triggerContext.getEntityRef();
        if (ref == null) return;
        PlayerRef playerRef = ref.getStore().getComponent(ref, PlayerRef.getComponentType());
        if (playerRef == null || playerRef.getWorldUuid() == null) return;
        var world = Universe.get().getWorld(playerRef.getWorldUuid());
        if (world == null) return;

        var sequenceAsset = SequenceAsset.getAssetMap().getAsset(sequenceKey);
        if (sequenceAsset == null) return;
        if (global) sequenceAsset.play(world, null);
        else sequenceAsset.play(world, playerRef);
    }
}
