package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class SoundEventKeyframe extends DelayKeyframe {
    @Nonnull
    public static final BuilderCodec<@NotNull SoundEventKeyframe> CODEC = BuilderCodec
        .builder(SoundEventKeyframe.class, SoundEventKeyframe::new, DelayKeyframe.CODEC)
        .documentation("Play a sound event.")
        .appendInherited(new KeyedCodec<>("SoundEvent", Codec.STRING),
            (c, v) -> c.soundEventKey = v,
            SoundEventKeyframe::getSoundEventKey,
            (a, b) -> a.soundEventKey = b.soundEventKey
        )
        .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
        .documentation("The sound event to play.")
        .add()
        .appendInherited(new KeyedCodec<>("SoundCategory", new EnumCodec<SoundCategory>(SoundCategory.class)),
            (c, v) -> c.soundCategory = v,
            SoundEventKeyframe::getSoundCategory,
            (a, b) -> a.soundCategory = b.soundCategory
        )
        .documentation("Sound category to play this effect in.")
        .add()
        .build();

    @Getter private String soundEventKey;
    @Getter private SoundCategory soundCategory = SoundCategory.SFX;

    @Override
    public void run(SequenceContext context, @Nullable Runnable onCompletion) {
        var soundEvent = getSoundEvent();
        if (soundEvent == null) return;
        var soundEventIndex = SoundEvent.getAssetMap().getIndex(soundEventKey);
        if (context.targetPlayer() != null) {
            playSound(context.targetPlayer(), soundEventIndex);
        } else {
            for (PlayerRef playerRef : context.world().getPlayerRefs()) {
                playSound(playerRef, soundEventIndex);
            }
        }
    }

    private void playSound(PlayerRef playerRef, int soundEventIndex) {
        SoundUtil.playLocalPlayerSoundEvent(
            playerRef,
            soundEventIndex,
            soundEventIndex,
            soundCategory
        );
    }

    @Nullable
    public SoundEvent getSoundEvent() {
        return SoundEvent.getAssetMap().getAsset(soundEventKey);
    }
}
