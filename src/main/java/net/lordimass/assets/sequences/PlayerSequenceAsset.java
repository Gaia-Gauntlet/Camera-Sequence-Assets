package net.lordimass.assets.sequences;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import net.lordimass.assets.keyframes.SequenceKeyframe;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class PlayerSequenceAsset extends SequenceAsset {

    public static final BuilderCodec<PlayerSequenceAsset> CODEC = BuilderCodec.abstractBuilder(
            PlayerSequenceAsset.class, SequenceAsset.ABSTRACT_CODEC)
        .build();

    public PlayerSequenceAsset() {
        super();
    }

    public PlayerSequenceAsset(String id, SequenceKeyframe initialKeyframe) {
        super(id, initialKeyframe);
    }

    public PlayerSequenceAsset(String id, SequenceKeyframe[] keyframes) {
        super(id, keyframes);
    }

    public abstract void play(@Nonnull PlayerRef playerRef, @Nullable Runnable onComplete);

    public void play(@Nonnull World world, @Nullable Runnable onComplete) {
        for (PlayerRef playerRef : world.getPlayerRefs()) {
            play(playerRef, onComplete);
        }
    }
}
