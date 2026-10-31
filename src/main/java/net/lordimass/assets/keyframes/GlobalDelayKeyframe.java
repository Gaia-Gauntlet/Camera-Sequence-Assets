package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class GlobalDelayKeyframe extends GlobalAwaitedKeyframe {
    @Nonnull public static final BuilderCodec<@NotNull GlobalDelayKeyframe> CODEC = BuilderCodec
        .builder(GlobalDelayKeyframe.class, GlobalDelayKeyframe::new)
        .documentation("Wait for the given length of time before executing the next keyframe")
        .appendInherited(new KeyedCodec<>("Duration", Codec.FLOAT),
            (c, v) -> c.duration = v,
            GlobalDelayKeyframe::getDuration,
            (a, b) -> a.duration = b.duration
        )
        .documentation("The length of this keyframe.")
        .add()
        .build();

    @Getter float duration;

    public GlobalDelayKeyframe() {
        super();
    }

    public GlobalDelayKeyframe(boolean waitForCompletion, float duration) {
        super(waitForCompletion);
        this.duration = duration;
    }

    @Override
    public void run(GlobalSequenceContext context, @Nullable Runnable onCompletion) {
        if (onCompletion == null) return;

        context.world().scheduleAfter(
            onCompletion,
            (long) (duration * 1000000),
            TimeUnit.MICROSECONDS
        );
    }
}
