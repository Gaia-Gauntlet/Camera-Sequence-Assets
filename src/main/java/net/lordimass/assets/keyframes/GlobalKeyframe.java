package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;

import javax.annotation.Nullable;

public abstract class GlobalKeyframe extends SequenceKeyframe {

    public static CodecMapCodec<SequenceKeyframe> CODEC = new CodecMapCodec<>();

    abstract public void run(GlobalSequenceContext context, @Nullable Runnable onCompletion);

    public void run(GlobalSequenceContext context) {
        run(context, null);
    }
}
