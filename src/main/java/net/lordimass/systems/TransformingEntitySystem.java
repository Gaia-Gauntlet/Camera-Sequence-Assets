package net.lordimass.systems;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.Dirty;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.lordimass.components.TransformingComponent;
import net.lordimass.utils.TransformUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class TransformingEntitySystem extends EntityTickingSystem<EntityStore> {

    @Override
    public void tick(float dt, int i, @NonNull ArchetypeChunk<EntityStore> chunk, @NonNull Store<EntityStore> store, @NonNull CommandBuffer<EntityStore> commandBuffer) {
        var transformingComp = chunk.getComponent(i, TransformingComponent.getComponentType());
        assert transformingComp != null;

        var transformComp = chunk.getComponent(i, TransformComponent.getComponentType());
        if (transformComp == null) return;

        // Transformation is complete, remove component to stop it ticking
        if (System.nanoTime() > transformingComp.getEndNanos()) {
            transformComp.teleportPosition(transformingComp.getEndTransform().getPosition());
            transformComp.teleportRotation(transformingComp.getEndTransform().getRotation());
            commandBuffer.removeComponent(chunk.getReferenceTo(i), TransformingComponent.getComponentType());

            var onCompletion = transformingComp.getOnCompletion();
            if (onCompletion != null) store.getExternalData().getWorld().execute(onCompletion);
            return;
        }

        var current = System.nanoTime();
        var start = transformingComp.getStartNanos();
        var end = transformingComp.getEndNanos();
        double t = ((double) (current - start)) / (end - start);

        var newTransform = TransformUtils.interpolate(
            transformComp.getTransform(),
            transformingComp.getEndTransform(),
            t,
            transformingComp.getEasing()
        );

        transformComp.setPosition(newTransform.getPosition());
        transformComp.setRotation(newTransform.getRotation());
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return TransformingComponent.getComponentType();
    }
}
