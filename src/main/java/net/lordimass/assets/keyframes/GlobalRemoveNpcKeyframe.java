package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import net.lordimass.components.NpcSelector;
import net.lordimass.components.SequenceEntityComponent;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class GlobalRemoveNpcKeyframe extends GlobalKeyframe {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<@NotNull GlobalRemoveNpcKeyframe> CODEC = BuilderCodec
        .builder(GlobalRemoveNpcKeyframe.class, GlobalRemoveNpcKeyframe::new, GlobalKeyframe.ABSTRACT_CODEC)
        .documentation("Remove an NPC spawned by a sequence.")
        .appendInherited(new KeyedCodec<>("NpcSelector", NpcSelector.CODEC),
            (c, v) -> c.selector = v,
            c -> c.selector,
            (a, b) -> a.selector = b.selector)
        .documentation("Selector for the NPC to remove")
        .add()
        .build();

    @Getter private NpcSelector selector = new NpcSelector();

    @Override
    public void run(SequenceContext ctx, @Nullable Runnable onCompletion) {
        World world = ctx.world();
        var store = world.getEntityStore().getStore();
        store.forEachEntityParallel(SequenceEntityComponent.getComponentType(), this::despawn);

        if (onCompletion != null) onCompletion.run();
    }

    private void despawn(int i, ArchetypeChunk<EntityStore> chunk, CommandBuffer<EntityStore> commandBuffer) {
        var seqEntityComp = chunk.getComponent(i, SequenceEntityComponent.getComponentType());
        assert seqEntityComp != null;
        if (!seqEntityComp.getSelector().matches(selector)) return;
        var ref = chunk.getReferenceTo(i);
        commandBuffer.removeEntity(ref, RemoveReason.REMOVE);
    }
}
