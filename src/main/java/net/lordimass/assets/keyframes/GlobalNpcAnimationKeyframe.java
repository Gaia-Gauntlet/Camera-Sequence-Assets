package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import lombok.Getter;
import net.lordimass.components.NpcSelector;
import net.lordimass.utils.SequenceNpcUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GlobalNpcAnimationKeyframe extends DelayKeyframe {
    public static final BuilderCodec<@NotNull GlobalNpcAnimationKeyframe> CODEC = BuilderCodec
        .builder(GlobalNpcAnimationKeyframe.class, GlobalNpcAnimationKeyframe::new, DelayKeyframe.CODEC)
        .documentation("Move an NPC that was spawned by a sequence")
        .appendInherited(new KeyedCodec<>("Selector", NpcSelector.CODEC),
            (c, v) -> c.selector = v,
            c -> c.selector,
            (a, b) -> a.selector = b.selector)
        .documentation("Selector for the NPC(s) to move")
        .addValidator(Validators.nonNull())
        .add()
        .appendInherited(new KeyedCodec<>("EntityAnimationId", Codec.STRING),
            (c, v) -> c.animationId = v,
            c -> c.animationId,
            (a, b) -> a.animationId = b.animationId)
        .add()
        .appendInherited(new KeyedCodec<>("AnimationSlot", new EnumCodec<>(AnimationSlot.class)),
            (c, v) -> c.animationSlot = v,
            c -> c.animationSlot,
            (a, b) -> a.animationSlot = b.animationSlot)
        .add()
        .build();

    @Getter private NpcSelector selector;
    @Getter private AnimationSlot animationSlot;
    @Getter private String animationId;

    @Override
    public void run(SequenceContext ctx, @Nullable Runnable onCompletion) {
        var store = ctx.world().getEntityStore().getStore();
        SequenceNpcUtils.applyToAllMatching(
            store, selector,
            this::playAnimation,
            NPCEntity.getComponentType()
        );
        super.run(ctx, onCompletion);
    }

    private void playAnimation(
        int i,
        ArchetypeChunk<EntityStore> chunk,
        CommandBuffer<EntityStore> buffer
    ) {
        var npcEntityComponentType = NPCEntity.getComponentType();
        assert npcEntityComponentType != null;
        var npc = chunk.getComponent(i, npcEntityComponentType);
        assert npc != null;

        var ref = chunk.getReferenceTo(i);
        npc.playAnimation(ref, animationSlot, animationId, buffer);
    }
}
