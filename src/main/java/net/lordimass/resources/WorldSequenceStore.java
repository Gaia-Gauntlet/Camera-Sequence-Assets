package net.lordimass.resources;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.set.SetCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WorldSequenceStore implements Resource<EntityStore> {
    public static final String ID = "WorldSequenceStore";
    @Getter @Setter private static ResourceType<EntityStore, WorldSequenceStore> resourceType;
    public static final BuilderCodec<@NotNull WorldSequenceStore> CODEC = BuilderCodec
        .builder(WorldSequenceStore.class, WorldSequenceStore::new)
        .append(new KeyedCodec<>("RunningSequences", new SetCodec<>(Codec.UUID_STRING, HashSet::new, false)),
            (c, v) -> c.runningSequences = v,
            c -> c.runningSequences
        )
        .documentation("List of UUIDs for currently running sequences.")
        .add()
        .build();

    @Getter private Set<UUID> runningSequences = new HashSet<>();

    @Override
    public @Nullable Resource<EntityStore> clone() {
        var comp = new WorldSequenceStore();
        comp.runningSequences = new HashSet<>(this.runningSequences);
        return comp;
    }
}
