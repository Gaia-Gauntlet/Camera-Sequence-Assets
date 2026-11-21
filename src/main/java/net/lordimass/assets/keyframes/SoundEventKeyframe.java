package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
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
        .addValidatorLate(SoundEvent.VALIDATOR_CACHE.getValidator()::late)
        .documentation("The sound event to play.")
        .add()
        .appendInherited(new KeyedCodec<>("SoundCategory", new EnumCodec<>(SoundCategory.class)),
            (c, v) -> c.soundCategory = v,
            SoundEventKeyframe::getSoundCategory,
            (a, b) -> a.soundCategory = b.soundCategory
        )
        .documentation("Sound category to play this effect in.")
        .add()
        .appendInherited(new KeyedCodec<>("Position", Vector3dUtil.CODEC),
            (c, v) -> c.position = v,
            c -> c.position,
            (a, b) -> a.position = b.position)
        .documentation("The position to play the sound from. If not provided, the sound will be " +
            "played to the player directly. Currently, the Hytale client seems to do this by " +
            "playing it at the player's actual location (i.e. not the camera location) so if the " +
            "camera is too far from the player's real location, the sound may not be heard.")
        .add()
        .build();

    @Getter private String soundEventKey;
    @Getter private SoundCategory soundCategory = SoundCategory.SFX;
    @Getter private Vector3d position;

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

        super.run(context, onCompletion);
    }

    private void playSound(PlayerRef playerRef, int soundEventIndex) {
        var ref = playerRef.getReference();
        if (position != null && ref != null) {
            var store = ref.getStore();
            SoundUtil.playSoundEvent3dToPlayer(
                ref,
                soundEventIndex,
                soundCategory,
                position.x, position.y, position.z,
                store);
            return;
        }
        SoundUtil.playSoundEvent2dToPlayer(
            playerRef,
            soundEventIndex,
            soundCategory
        );
    }

    @Nullable
    public SoundEvent getSoundEvent() {
        return SoundEvent.getAssetMap().getAsset(soundEventKey);
    }
}
