package net.lordimass.assets.sequences;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import net.lordimass.assets.keyframes.GlobalKeyframe;
import net.lordimass.assets.keyframes.GlobalSequenceContext;
import net.lordimass.assets.keyframes.SequenceKeyframe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GlobalSequenceAsset extends SequenceAsset {

    @Nonnull public static final BuilderCodec<@NotNull GlobalSequenceAsset> CODEC =
        BuilderCodec.builder(
                GlobalSequenceAsset.class,
                GlobalSequenceAsset::new,
                SequenceAsset.ABSTRACT_CODEC
            )
            .appendInherited(new KeyedCodec<>("Keyframes", new ArrayCodec<>(GlobalKeyframe.CODEC, GlobalKeyframe[]::new)),
                (asset, keyframes) -> asset.keyframes = keyframes,
                SequenceAsset::getKeyframes,
                (a, b) -> a.keyframes = b.keyframes
            )
            .addValidator(Validators.nonNull())
            .addValidator(Validators.nonEmptyArray())
            .add()
            .build();

    public GlobalSequenceAsset() {
        super();
    }

    public GlobalSequenceAsset(String id, GlobalKeyframe initialKeyframe) {
        super(id, initialKeyframe);
    }

    public GlobalSequenceAsset(String id, GlobalKeyframe[] keyframes) {
        super(id, keyframes);
    }


    public void play(@Nonnull GlobalSequenceContext context, @Nullable Runnable onComplete) {
        var frames = new ArrayList<>(Arrays.asList((GlobalKeyframe[]) keyframes));
        play0(context, onComplete, frames);
    }

    private void play0(GlobalSequenceContext context, @Nullable Runnable onComplete, List<GlobalKeyframe> frames) {
        if (frames.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }
        var frame = frames.removeFirst();
        frame.run(context, () -> play0(context, onComplete, frames));
    }

    @Override
    public SequenceAsset clone() {
        return new GlobalSequenceAsset(id, (GlobalKeyframe[]) keyframes);
    }
}
