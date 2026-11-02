package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.validators.NPCRoleValidator;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class GlobalSpawnNPCKeyframe extends GlobalKeyframe {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<@NotNull GlobalSpawnNPCKeyframe> CODEC = BuilderCodec
        .builder(GlobalSpawnNPCKeyframe.class, GlobalSpawnNPCKeyframe::new)
        .documentation("Spawn an NPC")
        .appendInherited(new KeyedCodec<>("Role", Codec.STRING),
            (c, v) -> c.roleId = v,
            GlobalSpawnNPCKeyframe::getRoleId,
            (a, b) -> a.roleId = b.roleId)
        .addValidator(NPCRoleValidator.INSTANCE)
        .documentation("The role of the NPC to spawn.")
        .add()
        .appendInherited(new KeyedCodec<>("ClassName", Codec.STRING),
            (c, v) -> c.className = v,
            c -> c.className,
            (a, b) -> a.className = b.className)
        .documentation("An arbitrary class to be given to the NPC, can be used as a one-to-many" +
            "selector in other keyframes.")
        .add()
        .appendInherited(new KeyedCodec<>("Id", Codec.STRING),
            (c, v) -> c.id = v,
            c -> c.id,
            (a, b) -> a.id = b.id)
        .documentation("An arbitrary id to be given to the NPC, can be used as a one-to-one" +
            "selector in other keyframes.")
        .add()
        .appendInherited(new KeyedCodec<>("Position", Vector3dUtil.CODEC),
            (c, v) -> c.position = v,
            c -> c.position,
            (a, b) -> a.position = b.position)
        .documentation("The position in the world to spawn the NPC")
        .add()
        .appendInherited(new KeyedCodec<>("Rotation", Rotation3f.CODEC),
            (c, v) -> c.rotation = v,
            c -> c.rotation,
            (a, b) -> a.rotation = b.rotation)
        .documentation("The rotation of the NPC when it is spawned")
        .add()
        .build();

    @Getter private String roleId;
    @Getter private String className;
    @Getter private String id;
    @Getter private Vector3d position;
    @Getter private Rotation3f rotation = new Rotation3f(0, 0, 0);

    @Override
    public void run(SequenceContext context, @Nullable Runnable onCompletion) {
        var npcPlugin = NPCPlugin.get();
        World world = context.world();
        var store = world.getEntityStore().getStore();
        var roleIndex = npcPlugin.getIndex(roleId);
        if (roleIndex < 0) {
            LOGGER.atWarning().log("Couldn't spawn NPC with invalid role ID '"+roleId+"'");
            return;
        }
        npcPlugin.spawnEntity(store, roleIndex, position, rotation, null, this::preAddToWorld, this::postAddToWorld);
    }

    private void preAddToWorld(NPCEntity npc, Holder<EntityStore> holder, Store<EntityStore> store) {

    }

    private void postAddToWorld(NPCEntity npc, Ref<EntityStore> ref, Store<EntityStore> store) {

    }
}
