package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import lombok.Getter;
import net.lordimass.assets.sequences.SequenceAsset;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PlaySequenceKeyframe extends GlobalAwaitedKeyframe {
    public static final BuilderCodec<@NonNull PlaySequenceKeyframe> CODEC = BuilderCodec
        .builder(PlaySequenceKeyframe.class, PlaySequenceKeyframe::new, GlobalAwaitedKeyframe.ABSTRACT_CODEC)
        .appendInherited(new KeyedCodec<>("Sequence", SequenceAsset.CHILD_ASSET_CODEC),
            (c, v) -> c.sequenceName = v,
            c -> c.sequenceName,
            (a, b) -> a.sequenceName = b.sequenceName)
        .add()
        .appendInherited(new KeyedCodec<>("Global", Codec.BOOLEAN),
            (c, v) -> c.global = v,
            c -> c.global,
            (a, b) -> a.global = b.global)
        .documentation("Whether this sequence should be played to all players in the world, irrespective of context.")
        .add()
        .build();

    @Getter private String sequenceName;
    @Getter private boolean global;

    @Override
    public void run(SequenceContext context, @Nullable Runnable onCompletion) {
        var seq = getSequence();
        if (seq == null) {
            if (onCompletion != null) onCompletion.run();
            return;
        }
        if (global) {
            seq.play(context.world(), null, onCompletion);
        } else {
            seq.play(context.world(), context.targetPlayer(), onCompletion);
        }
    }

    @Override
    public double getDurationSeconds() {
        var seq = getSequence();
        if (seq == null) return 0;
        return seq.getDurationSeconds();
    }

    @Nullable
    private SequenceAsset getSequence() {
        return SequenceAsset.getAssetMap().getAsset(sequenceName);
    }
}
