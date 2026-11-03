package net.lordimass.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.EasingType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import static net.lordimass.utils.TransformUtils.EASING_CODEC;

@AllArgsConstructor
@NoArgsConstructor
public class TransformingComponent implements Component<EntityStore> {
    @Getter @Setter public static ComponentType<EntityStore, TransformingComponent> componentType;
    public static final BuilderCodec<TransformingComponent> CODEC = BuilderCodec
        .builder(TransformingComponent.class, TransformingComponent::new)
        .append(new KeyedCodec<>("StartNanos", Codec.LONG),
            (c, v) -> c.startNanos = v,
            c -> c.startNanos)
        .add()
        .append(new KeyedCodec<>("EndNanos", Codec.LONG),
            (c, v) -> c.endNanos = v,
            c -> c.endNanos)
        .add()
        .append(new KeyedCodec<>("Easing", EASING_CODEC),
            (c, v) -> c.easing = v == null ? EasingType.Linear : v,
            c -> c.easing)
        .add()
        .append(new KeyedCodec<>("StartTransform", Transform.CODEC_DEGREES),
            (c, v) -> c.startTransform = v,
            c -> c.startTransform)
        .add()
        .append(new KeyedCodec<>("EndTransform", Transform.CODEC_DEGREES),
            (c, v) -> c.endTransform = v,
            c -> c.endTransform)
        .add()
        .build();

    @Getter long startNanos;
    @Getter long endNanos;
    @Getter EasingType easing = EasingType.Linear;
    @Getter Transform startTransform;
    @Getter Transform endTransform;
    @Nullable @Getter Runnable onCompletion;

    @Override
    public @Nullable Component<EntityStore> clone() {
        return new TransformingComponent(
            startNanos,
            endNanos,
            easing,
            startTransform.clone(),
            endTransform.clone(),
            onCompletion
        );
    }
}
