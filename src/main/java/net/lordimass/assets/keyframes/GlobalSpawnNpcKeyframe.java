package net.lordimass.assets.keyframes;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.entity.Frozen;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.validators.NPCRoleValidator;
import lombok.Getter;
import net.lordimass.components.Ephemeral;
import net.lordimass.components.NpcSelector;
import net.lordimass.components.SequenceEntityComponent;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class GlobalSpawnNpcKeyframe extends GlobalKeyframe {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    @Nonnull
    public static final BuilderCodec<@NotNull GlobalSpawnNpcKeyframe> CODEC = BuilderCodec
        .builder(GlobalSpawnNpcKeyframe.class, GlobalSpawnNpcKeyframe::new)
        .documentation("Spawn an NPC")
        .appendInherited(new KeyedCodec<>("Role", Codec.STRING),
            (c, v) -> c.roleId = v,
            GlobalSpawnNpcKeyframe::getRoleId,
            (a, b) -> a.roleId = b.roleId)
        .addValidator(NPCRoleValidator.INSTANCE)
        .documentation("The role of the NPC to spawn.")
        .add()
        .appendInherited(new KeyedCodec<>("NpcSelector", NpcSelector.CODEC),
            (c, v) -> c.selector = v,
            c -> c.selector,
            (a, b) -> a.selector = b.selector)
        .documentation("Selector for the NPC which can be used to select this NPC in other keyframes")
        .add()
        .appendInherited(new KeyedCodec<>("Transform", Transform.CODEC_DEGREES),
            (c, v) -> c.transform = v,
            c -> c.transform,
            (a, b) -> a.transform = b.transform)
        .add()
        .appendInherited(new KeyedCodec<>("Ephemeral", Codec.BOOLEAN),
            (c, v) -> c.ephemeral = v,
            c -> c.ephemeral,
            (a, b) -> a.ephemeral = b.ephemeral)
        .documentation("Whether this entity should be removed after the sequence is complete.")
        .add()
        .appendInherited(new KeyedCodec<>("Frozen", Codec.BOOLEAN),
            (c, v) -> c.frozen = v,
            c -> c.frozen,
            (a, b) -> a.frozen = b.frozen)
        .documentation("Whether this entity should be frozen on spawn.")
        .add()
        .build();

    @Getter private String roleId;
    @Getter private NpcSelector selector = new NpcSelector();
    @Getter private Transform transform = new Transform(new Vector3d(), new Rotation3f(0,0,0));
    @Getter private boolean ephemeral;
    @Getter private boolean frozen;

    @Override
    public void run(SequenceContext ctx, @Nullable Runnable onCompletion) {
        var npcPlugin = NPCPlugin.get();
        World world = ctx.world();
        var store = world.getEntityStore().getStore();
        var roleIndex = npcPlugin.getIndex(roleId);
        if (roleIndex < 0) {
            LOGGER.atWarning().log("Couldn't spawn NPC with invalid role ID '"+roleId+"'");
            return;
        }
        npcPlugin.spawnEntity(store, roleIndex, transform.getPosition(), transform.getRotation(), null,
            (NPCEntity npc, Holder<EntityStore> holder, Store<EntityStore> store1) -> preAddToWorld(ctx, npc, holder, store1),
            (NPCEntity npc, Ref<EntityStore> ref, Store<EntityStore> store1) -> postAddToWorld(ctx, npc, ref, store1)
        );

        if (onCompletion != null) onCompletion.run();
    }

    private void preAddToWorld(SequenceContext ctx, NPCEntity npc, Holder<EntityStore> holder, Store<EntityStore> store) {
        if (frozen) holder.addComponent(Frozen.getComponentType(), Frozen.get());
        if (ephemeral) holder.addComponent(Ephemeral.getComponentType(), Ephemeral.get());

        holder.addComponent(SequenceEntityComponent.getComponentType(), new SequenceEntityComponent(
            ctx.sequenceId(), selector
        ));
    }

    private void postAddToWorld(SequenceContext ctx, NPCEntity npc, Ref<EntityStore> ref, Store<EntityStore> store) {

    }
}
