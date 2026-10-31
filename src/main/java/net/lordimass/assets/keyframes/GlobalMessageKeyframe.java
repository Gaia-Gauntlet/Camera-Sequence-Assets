package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.Message;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class GlobalMessageKeyframe extends GlobalKeyframe {
    @Nonnull
    public static final BuilderCodec<@NotNull GlobalMessageKeyframe> CODEC = BuilderCodec
        .builder(GlobalMessageKeyframe.class, GlobalMessageKeyframe::new)
        .documentation("Put a message in chat.")
        .appendInherited(new KeyedCodec<>("Message", Codec.STRING),
            (c, v) -> c.message = v,
            GlobalMessageKeyframe::getMessage,
            (a, b) -> a.message = b.message
        )
        .documentation("The length of this keyframe.")
        .add()
        .build();

    @Getter String message;

    public GlobalMessageKeyframe() {}

    public GlobalMessageKeyframe(String message) {
        this.message = message;
    }

    @Override
    public void run(GlobalSequenceContext context, @Nullable Runnable onCompletion) {
        var translated = Message.translation(message);
        translated.getFormattedMessage().markupEnabled = true;

        if (context.targetPlayer() != null) {
            context.targetPlayer().sendMessage(translated);
        } else {
            context.world().sendMessage(translated);
        }

        if (onCompletion != null) onCompletion.run();
    }
}
