package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.codec.validation.validator.NonNullValidator;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.EasingType;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import net.lordimass.components.NpcSelector;
import net.lordimass.components.TransformingComponent;
import net.lordimass.utils.SequenceNpcUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

import static net.lordimass.utils.TransformUtils.EASING_CODEC;

@NoArgsConstructor
@AllArgsConstructor
public class GlobalTransformNpcKeyframe extends DelayKeyframe {
    public static final BuilderCodec<@NotNull GlobalTransformNpcKeyframe> CODEC = BuilderCodec
        .builder(GlobalTransformNpcKeyframe.class, GlobalTransformNpcKeyframe::new, DelayKeyframe.CODEC)
        .documentation("Move an NPC that was spawned by a sequence")
        .appendInherited(new KeyedCodec<>("Selector", NpcSelector.CODEC),
            (c, v) -> c.selector = v,
            c -> c.selector,
            (a, b) -> a.selector = b.selector)
        .documentation("Selector for the NPC(s) to move")
        .addValidator(Validators.nonNull())
        .add()
        .appendInherited(new KeyedCodec<>("TargetTransform", Transform.CODEC_DEGREES),
            (c, v) -> c.targetTransform = v,
            c -> c.targetTransform,
            (a, b) -> a.targetTransform = b.targetTransform)
        .add()
        .appendInherited(new KeyedCodec<>("RelativeToNpc", Codec.BOOLEAN),
            (c, v) -> c.relativeToNpc = v,
            c -> c.relativeToNpc,
            (a, b) -> a.relativeToNpc = b.relativeToNpc)
        .documentation("Whether the movement should be relative to the current position/rotation " +
            "of the NPC, or global.")
        .add()
        .appendInherited(new KeyedCodec<>("Easing", EASING_CODEC),
            (c, v) -> c.easing = v == null ? EasingType.Linear : v,
            GlobalTransformNpcKeyframe::getEasing,
            (a, b) -> a.easing = b.easing
        )
        .documentation("The type of easing that should be used for the movement.")
        .add()
        .build();

    @Getter private NpcSelector selector;
    @Getter private Transform targetTransform = new Transform(new Vector3d(), new Rotation3f(0,0,0));
    @Getter private boolean relativeToNpc;
    @Getter private EasingType easing = EasingType.Linear;

    @Override
    public void run(SequenceContext ctx, @Nullable Runnable onCompletion) {
        var store = ctx.world().getEntityStore().getStore();
        SequenceNpcUtils.applyToAllMatching(
            store, selector,
            (i, chunk, buffer) -> startTransformation(onCompletion, i, chunk, buffer),
            TransformComponent.getComponentType()
        );
    }

    private void startTransformation(
        Runnable onCompletion,
        int i,
        ArchetypeChunk<EntityStore> chunk,
        CommandBuffer<EntityStore> buffer
    ) {
        long start = System.nanoTime();
        long end = start + ((long) getDurationSeconds()) * 1000000000;

        var transformComp = chunk.getComponent(i, TransformComponent.getComponentType());
        assert transformComp != null;
        var startTransform = transformComp.getTransform().clone();
        var endTransform = targetTransform.clone();
        if (relativeToNpc) {
            endTransform.setPosition(new Vector3d(startTransform.getPosition()).add(targetTransform.getPosition()));
            endTransform.setRotation(new Rotation3f(startTransform.getRotation()).add(targetTransform.getRotation()));
        }

        Ref<EntityStore> ref = chunk.getReferenceTo(i);
        if (chunk.getComponent(i, TransformingComponent.getComponentType()) != null) {
            buffer.removeComponent(ref, TransformingComponent.getComponentType());
        }

        buffer.addComponent(
            ref, TransformingComponent.getComponentType(),
            new TransformingComponent(
                start, end,
                easing,
                startTransform, endTransform,
                onCompletion
            )
        );
    }
}
