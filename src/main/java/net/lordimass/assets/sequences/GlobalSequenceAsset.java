package net.lordimass.assets.sequences;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInteraction;
import net.lordimass.assets.keyframes.CameraKeyframe;
import net.lordimass.assets.keyframes.SequenceKeyframe;

import javax.annotation.Nullable;

public abstract class GlobalSequenceAsset extends SequenceAsset {

    public static final BuilderCodec<GlobalSequenceAsset> CODEC = BuilderCodec.abstractBuilder(
            GlobalSequenceAsset.class, SequenceAsset.ABSTRACT_CODEC)
        .build();

    public GlobalSequenceAsset() {
        super();
    }

    public GlobalSequenceAsset(String id, SequenceKeyframe initialKeyframe) {
        super(id, initialKeyframe);
    }

    public GlobalSequenceAsset(String id, SequenceKeyframe[] keyframes) {
        super(id, keyframes);
    }

    public abstract void play(@Nullable Runnable onComplete);

    public void play() {
        play(null);;
    };
}
