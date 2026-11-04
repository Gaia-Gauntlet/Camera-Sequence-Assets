package net.lordimass.assets.sequences;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.*;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import net.lordimass.assets.keyframes.CameraKeyframe;
import net.lordimass.assets.keyframes.SequenceContext;
import net.lordimass.assets.keyframes.SequenceKeyframe;
import net.lordimass.components.SequenceEphemeral;
import net.lordimass.components.SequenceEntityComponent;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

public abstract class SequenceAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, SequenceAsset>> {
    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public static final String ASSET_PATH = "CameraSequence/Sequence";

    private static AssetStore<String, SequenceAsset, DefaultAssetMap<String, SequenceAsset>> assetStore;

    public static final AssetCodecMapCodec<String, SequenceAsset> CODEC = new AssetCodecMapCodec<>(
        Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
    );

    @Nonnull
    public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(SequenceAsset.class, CODEC);

    @Nonnull
    public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);

    @Nonnull public static final BuilderCodec<@NotNull SequenceAsset> ABSTRACT_CODEC =
        BuilderCodec.abstractBuilder(SequenceAsset.class)
            .appendInherited(new KeyedCodec<>("Keyframes", new ArrayCodec<>(SequenceKeyframe.CODEC, SequenceKeyframe[]::new)),
                (asset, keyframes) -> asset.keyframes = keyframes,
                SequenceAsset::getKeyframes,
                (a, b) -> a.keyframes = b.keyframes
            )
            .addValidator(Validators.nonNull())
            .addValidator(Validators.nonEmptyArray())
            .add()
            .build();

    public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(SequenceAsset::getAssetStore));

    AssetExtraInfo.Data data;
    @Getter String id;
    @Getter SequenceKeyframe[] keyframes = new CameraKeyframe[0];

    public SequenceAsset() {}

    public SequenceAsset(
        @Nonnull String id,
        @Nonnull SequenceKeyframe initialKeyframe
    ) {
        this.id = id;
        this.keyframes = new SequenceKeyframe[]{initialKeyframe};
    }

    public SequenceAsset(
        @Nonnull String id,
        @Nonnull SequenceKeyframe[] keyframes
    ) {
        if (keyframes.length == 0) {
            throw new IllegalArgumentException("Keyframes must contain at least one element");
        }
        this.id = id;
        this.keyframes = keyframes;
    }

    public static AssetStore<String, SequenceAsset, DefaultAssetMap<String, SequenceAsset>> getAssetStore() {
        if (assetStore == null) assetStore = AssetRegistry.getAssetStore(SequenceAsset.class);
        return assetStore;
    }

    public static DefaultAssetMap<String, SequenceAsset> getAssetMap() {
        return getAssetStore().getAssetMap();
    }

    // Ok technically kinda goes against the whole idea of immutability but then again, the asset was made the only way to play animations T-T
    public void setKeyframes(@Nonnull SequenceKeyframe[] keyframes) {
        if (keyframes.length == 0) {
            throw new IllegalArgumentException("Keyframes must contain at least one element");
        }
        this.keyframes = keyframes;
    }

    public void play(@Nonnull World world, @Nullable PlayerRef playerRef, @Nullable Runnable onComplete) {
        var ctx = new SequenceContext(world, playerRef, UUID.randomUUID());
        Runnable wrappedOnComplete = () -> onComplete(ctx, onComplete);

        if (this instanceof PlayerSequenceAsset playerSeq) {
            if (playerRef == null) playerSeq.play(ctx, world, wrappedOnComplete);
            else playerSeq.play(ctx, playerRef, wrappedOnComplete);
        }

        else if (this instanceof GlobalSequenceAsset globalSeq) {
            globalSeq.play(ctx, wrappedOnComplete);
        }

        else {
            if (playerRef != null) {
                playerRef.sendMessage(Message.raw("Unsupported sequence type!"));
            } else {
                LOGGER.atWarning().log("Couldn't play unsupported camera sequence type!");
            }
        }
    }

    public void play(@Nonnull World world, @Nullable PlayerRef playerRef) {
        play(world, playerRef, null);
    }

    private void onComplete(SequenceContext ctx, @Nullable Runnable onComplete) {
        ctx.unregister();

        // Clean up any spawned entities
        Store<EntityStore> store = ctx.world().getEntityStore().getStore();
        store.forEachEntityParallel(
            Query.and(SequenceEphemeral.getComponentType(), SequenceEntityComponent.getComponentType()),
            (i, chunk, commandBuffer) -> cleanUpEntity(ctx, i, chunk, commandBuffer)
        );

        if (onComplete != null) onComplete.run();
    }

    private static void cleanUpEntity(SequenceContext ctx, int i, ArchetypeChunk<EntityStore> chunk, CommandBuffer<EntityStore> commandBuffer) {
        // Only clean up entities for this current sequence.
        var seqEntityComp = chunk.getComponent(i, SequenceEntityComponent.getComponentType());
        assert seqEntityComp != null;
        if (!seqEntityComp.getSequenceId().equals(ctx.sequenceId())) return;

        commandBuffer.removeEntity(chunk.getReferenceTo(i), RemoveReason.REMOVE);
    }

    public double getDurationSeconds() {
        double duration = 0;
        for (SequenceKeyframe keyframe : keyframes) {
            duration += keyframe.getDurationSeconds();
        }
        return duration;
    }

    public abstract SequenceAsset clone();
}
