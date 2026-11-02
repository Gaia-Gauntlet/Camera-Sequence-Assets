package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class DelayKeyframe extends GlobalAwaitedKeyframe {
    @Nonnull public static final BuilderCodec<@NotNull DelayKeyframe> CODEC = BuilderCodec
        .builder(DelayKeyframe.class, DelayKeyframe::new)
        .documentation("Wait for the given length of time before executing the next keyframe")
        .appendInherited(new KeyedCodec<>("Duration", Codec.DOUBLE),
            (c, v) -> c.durationSeconds = v,
            DelayKeyframe::getDurationSeconds,
            (a, b) -> a.durationSeconds = b.durationSeconds
        )
        .documentation("The length of this keyframe.")
        .add()
        .build();

    @Getter double durationSeconds;

    public DelayKeyframe() {
        super();
    }

    public DelayKeyframe(boolean waitForCompletion, float durationSeconds) {
        super(waitForCompletion);
        this.durationSeconds = durationSeconds;
    }

    @Override
    public void run(GlobalSequenceContext context, @Nullable Runnable onCompletion) {
        if (onCompletion == null) return;

        context.world().scheduleAfter(
            onCompletion,
            (long) (durationSeconds * 1000000),
            TimeUnit.MICROSECONDS
        );
    }
}
