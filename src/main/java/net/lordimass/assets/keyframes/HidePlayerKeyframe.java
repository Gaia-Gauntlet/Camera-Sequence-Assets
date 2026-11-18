package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;

public class HidePlayerKeyframe extends GlobalKeyframe {

    public static final BuilderCodec<HidePlayerKeyframe> CODEC = BuilderCodec
        .builder(HidePlayerKeyframe.class, HidePlayerKeyframe::new, GlobalKeyframe.ABSTRACT_CODEC)
        .appendInherited(new KeyedCodec<>("Hide", Codec.BOOLEAN),
            (c, v) -> c.hide = v,
            c -> c.hide,
            (a, b) -> a.hide = b.hide)
        .add()
        .build();

    boolean hide = true;

    @Override
    public void run(SequenceContext context, @Nullable Runnable onCompletion) {
        var targets = new HashSet<PlayerRef>();
        if (context.targetPlayer() != null) targets.add(context.targetPlayer());
        else targets.addAll(context.world().getPlayerRefs());

        var invisEffect = EntityEffect.getAssetMap().getAsset("Invisible");
        if (invisEffect == null) return;
        var invisIndex = EntityEffect.getAssetMap().getIndex(invisEffect.getId());

        for (PlayerRef target : targets) {
            var ref = target.getReference();
            if (ref == null || !ref.isValid()) continue;
            var store = ref.getStore();
            var effectController = store.ensureAndGetComponent(ref, EffectControllerComponent.getComponentType());
            if (hide) {
                effectController.addInfiniteEffect(ref, invisIndex, invisEffect, store);
            } else {
                effectController.removeEffect(ref, invisIndex, store);
            }
        }

        if (onCompletion != null) onCompletion.run();
    }
}
