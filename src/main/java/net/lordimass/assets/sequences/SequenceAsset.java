package net.lordimass.assets.sequences;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.lookup.CodecMapCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import lombok.Getter;
import net.lordimass.assets.keyframes.CameraKeyframe;
import net.lordimass.assets.keyframes.SequenceKeyframe;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.util.Arrays;

public abstract class SequenceAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, SequenceAsset>> {
    public static final String ASSET_PATH = "CameraSequence/CameraSequence";

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

    public SequenceAsset clone() {
        return null;
    }
}
