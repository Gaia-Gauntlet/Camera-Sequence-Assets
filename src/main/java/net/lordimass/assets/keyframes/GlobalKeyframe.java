package net.lordimass.assets.keyframes;

import javax.annotation.Nullable;

public abstract class GlobalKeyframe extends SequenceKeyframe {
    abstract public void run(GlobalSequenceContext context, @Nullable Runnable onCompletion);

    public void run(GlobalSequenceContext context) {
        run(null);
    }
}
