package net.lordimass.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class SequenceEntityComponent implements Component<EntityStore> {
    public static final BuilderCodec<SequenceEntityComponent> CODEC = BuilderCodec.builder(
        SequenceEntityComponent.class, SequenceEntityComponent::new
    )
        .append(new KeyedCodec<>("SequenceId", Codec.UUID_STRING),
            (c, v) -> c.sequenceId = v,
            SequenceEntityComponent::getSequenceId
            )
        .add()
        .append(new KeyedCodec<>("ClassName", Codec.STRING),
            (c, v) -> c.className = v,
            SequenceEntityComponent::getClassName
        )
        .add()
        .append(new KeyedCodec<>("Id", Codec.STRING),
            (c, v) -> c.id = v,
            SequenceEntityComponent::getId
        )
        .add()
        .build();

    @Getter private UUID sequenceId;
    @Getter private String className;
    @Getter private String id;

    public SequenceEntityComponent() {}

    public SequenceEntityComponent(UUID sequenceId, String className, String id) {
        this.sequenceId = sequenceId;
        this.className = className;
        this.id = id;
    }

    @Override
    public @Nullable Component<EntityStore> clone() {
        return new SequenceEntityComponent(sequenceId, className, id);
    }
}
