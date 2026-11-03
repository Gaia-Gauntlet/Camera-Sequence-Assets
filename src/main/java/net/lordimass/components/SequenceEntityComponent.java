package net.lordimass.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class SequenceEntityComponent implements Component<EntityStore> {
    @Getter @Setter private static ComponentType<EntityStore, SequenceEntityComponent> componentType;
    public static final BuilderCodec<SequenceEntityComponent> CODEC = BuilderCodec.builder(
        SequenceEntityComponent.class, SequenceEntityComponent::new
    )
        .append(new KeyedCodec<>("SequenceId", Codec.UUID_STRING),
            (c, v) -> c.sequenceId = v,
            SequenceEntityComponent::getSequenceId
            )
        .add()
        .append(new KeyedCodec<>("NpcSelector", NpcSelector.CODEC),
            (c, v) -> c.selector = v,
            SequenceEntityComponent::getSelector
        )
        .add()
        .build();

    @Getter private UUID sequenceId;
    @Getter private NpcSelector selector;

    public SequenceEntityComponent() {}

    public SequenceEntityComponent(UUID sequenceId, NpcSelector selector) {
        this.sequenceId = sequenceId;
        this.selector = selector;
    }

    @Override
    public @Nullable Component<EntityStore> clone() {
        return new SequenceEntityComponent(sequenceId, selector.clone());
    }
}
