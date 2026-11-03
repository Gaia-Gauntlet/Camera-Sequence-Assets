package net.lordimass.utils;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.function.consumer.IntBiObjectConsumer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.lordimass.components.NpcSelector;
import net.lordimass.components.SequenceEntityComponent;

import javax.annotation.Nullable;

public class SequenceNpcUtils {
    private SequenceNpcUtils() {}

    public static void applyToAllMatching(
        Store<EntityStore> store,
        NpcSelector selector,
        IntBiObjectConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> consumer,
        @Nullable Query<EntityStore> query
    ) {
        var fullQuery = query == null
            ? SequenceEntityComponent.getComponentType()
            : Query.and(query, SequenceEntityComponent.getComponentType());
        store.forEachEntityParallel(fullQuery,
            (i, chunk, buffer) -> applyIfMatchingSelector(selector, consumer, i, chunk, buffer));
    }

    public static void applyToAllMatching(
        Store<EntityStore> store,
        NpcSelector selector,
        IntBiObjectConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> consumer
    ) {
        applyToAllMatching(store, selector, consumer, null);
    }

    private static void applyIfMatchingSelector(
        NpcSelector selector,
        IntBiObjectConsumer<ArchetypeChunk<EntityStore>, CommandBuffer<EntityStore>> consumer,
        int index,
        ArchetypeChunk<EntityStore> chunk,
        CommandBuffer<EntityStore> buffer
    ) {
        var sequenceEntityComp = chunk.getComponent(index, SequenceEntityComponent.getComponentType());
        assert sequenceEntityComp != null;
        var otherSelector = sequenceEntityComp.getSelector();
        if (!selector.matches(otherSelector)) return;
        consumer.accept(index, chunk, buffer);
    }
}
