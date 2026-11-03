package net.lordimass.components;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import lombok.Setter;

public class Ephemeral implements Component<EntityStore> {
    @Getter @Setter private static ComponentType<EntityStore, Ephemeral> componentType;
    public static final BuilderCodec<Ephemeral> CODEC = BuilderCodec
        .builder(Ephemeral.class, Ephemeral::new)
        .documentation("Marks this entity as ephemeral. This means that it will be automatically" +
            "cleaned up and removed from the world on load. It only exists temporarily.")
        .build();

    public static Ephemeral INSTANCE = new Ephemeral();

    // TODO: Make entities with the ephemeral component be actually ephemeral. Right now this just
    //  serves as a marker component to remove entities at the end of a camera sequence.

    public static Ephemeral get() {
        return INSTANCE;
    }

    @Override
    public Component<EntityStore> clone() {
        return get();
    }
}
