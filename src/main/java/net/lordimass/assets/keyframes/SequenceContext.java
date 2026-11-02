package net.lordimass.assets.keyframes;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import net.lordimass.resources.WorldSequenceStore;

import javax.annotation.Nullable;
import java.util.UUID;

public record SequenceContext(
    World world,
    @Nullable PlayerRef targetPlayer,
    UUID sequenceId
) {
    public SequenceContext(World world, PlayerRef targetPlayer, UUID sequenceId) {
        this.world = world;
        this.targetPlayer = targetPlayer;
        this.sequenceId = sequenceId;

        var worldSequenceStore = world.getEntityStore().getStore().getResource(WorldSequenceStore.getResourceType());
        worldSequenceStore.getRunningSequences().add(sequenceId);
    }

    public void unregister() {
        var worldSequenceStore = world.getEntityStore().getStore().getResource(WorldSequenceStore.getResourceType());
        worldSequenceStore.getRunningSequences().remove(sequenceId);
    }
}
