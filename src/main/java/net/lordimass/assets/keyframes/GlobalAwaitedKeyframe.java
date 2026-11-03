package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public abstract class GlobalAwaitedKeyframe extends GlobalKeyframe {

    @Nonnull public static final BuilderCodec<@NotNull GlobalAwaitedKeyframe> ABSTRACT_CODEC = BuilderCodec
        .abstractBuilder(GlobalAwaitedKeyframe.class, GlobalKeyframe.ABSTRACT_CODEC)
        .documentation("A keyframe that may take time to execute.")
        .appendInherited(new KeyedCodec<>("WaitForCompletion", Codec.BOOLEAN),
            (c, v) -> c.waitForCompletion = v,
            GlobalAwaitedKeyframe::isWaitForCompletion,
            (a, b) -> a.waitForCompletion = b.waitForCompletion
        )
        .documentation("Whether the sequence should wait for this ")
        .add()
        .build();

    @Getter boolean waitForCompletion = true;

    public GlobalAwaitedKeyframe() {}

    public GlobalAwaitedKeyframe(boolean waitForCompletion) {
        this.waitForCompletion = waitForCompletion;
    }
}
