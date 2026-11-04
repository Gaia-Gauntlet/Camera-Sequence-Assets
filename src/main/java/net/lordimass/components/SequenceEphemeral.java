package net.lordimass.components;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.Setter;

public class SequenceEphemeral implements Component<EntityStore> {
    @Getter @Setter private static ComponentType<EntityStore, SequenceEphemeral> componentType;
    public static final BuilderCodec<SequenceEphemeral> CODEC = BuilderCodec
        .builder(SequenceEphemeral.class, SequenceEphemeral::new)
        .documentation("Marks this entity as sequence ephemeral. This means that it will be automatically" +
            "cleaned up and removed from the world after the camera sequence it is part of is over.")
        .build();

    public static SequenceEphemeral INSTANCE = new SequenceEphemeral();
    public static SequenceEphemeral get() {
        return INSTANCE;
    }

    @Override
    public Component<EntityStore> clone() {
        return get();
    }
}
