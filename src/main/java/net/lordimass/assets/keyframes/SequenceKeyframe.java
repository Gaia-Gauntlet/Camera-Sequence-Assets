package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;

public abstract class SequenceKeyframe {
    @Nonnull
    public static CodecMapCodec<SequenceKeyframe> CODEC = new CodecMapCodec<>();

    @Nonnull public static final BuilderCodec<@NotNull SequenceKeyframe> ABSTRACT_CODEC = BuilderCodec
        .abstractBuilder(SequenceKeyframe.class)
        .appendInherited(new KeyedCodec<>("Title", Codec.STRING),
            (keyframe, title) -> keyframe.title = title,
            SequenceKeyframe::getTitle,
            (cameraKeyframe, parent) -> cameraKeyframe.title = parent.title
        )
        .add()
        .appendInherited(new KeyedCodec<>("Notes", Codec.STRING),
            (keyframe, notes) -> keyframe.notes = notes,
            SequenceKeyframe::getNotes,
            (cameraKeyframe, parent) -> cameraKeyframe.notes = parent.notes
        )
        .add()
        .build();

    @Getter String title;
    @Getter String notes;
}
