package net.lordimass.assets.sequences;

import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.camera.CameraKeyframeBuilder;
import com.hypixel.hytale.server.core.modules.camera.CameraSequenceBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import lombok.Getter;
import net.lordimass.assets.keyframes.CameraKeyframe;
import net.lordimass.assets.keyframes.SequenceKeyframe;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;

import static net.lordimass.utils.UiUtils.hideUI;
import static net.lordimass.utils.UiUtils.showUI;

public class CameraSequenceAsset extends PlayerSequenceAsset {

    @Nonnull public static final BuilderCodec<@NotNull CameraSequenceAsset> CODEC =
        BuilderCodec.builder(
            CameraSequenceAsset.class,
            CameraSequenceAsset::new,
            PlayerSequenceAsset.ABSTRACT_CODEC
        )
            .appendInherited(new KeyedCodec<>("Keyframes", new ArrayCodec<>(CameraKeyframe.CODEC, CameraKeyframe[]::new)),
                (asset, keyframes) -> asset.keyframes = keyframes,
                (asset) -> (CameraKeyframe[]) asset.getKeyframes(),
                (a, b) -> a.keyframes = b.keyframes
            )
            .addValidator(Validators.nonNull())
            .addValidator(Validators.nonEmptyArray())
            .add()
            .appendInherited(new KeyedCodec<>("BaseFov", Codec.FLOAT),
                (asset, baseFov) -> asset.baseFov = baseFov,
                CameraSequenceAsset::getBaseFov,
                (a, b) -> a.baseFov = b.baseFov
            )
            .add()
            .appendInherited(new KeyedCodec<>("HideUi", Codec.BOOLEAN),
                (asset, hideUI) -> asset.hideUI = hideUI,
                CameraSequenceAsset::isHideUI,
                (a, b) -> a.hideUI = b.hideUI
            )
            .documentation("Whether the rest of the UI should be hidden while the effect is playing.")
            .add()
            .build();


    @Getter private Float baseFov;
    @Getter private boolean hideUI;

    public CameraSequenceAsset() {
        super();
    }

    public CameraSequenceAsset(
        @Nonnull String id,
        @Nonnull CameraKeyframe initialKeyframe,
        @Nullable Float baseFov,
        boolean hideUI
    ) {
        super(id, initialKeyframe);

        this.baseFov = baseFov != null ? baseFov : 60;
        this.hideUI = hideUI;
    }

    public CameraSequenceAsset(
        @Nonnull String id,
        @Nonnull CameraKeyframe[] keyframes,
        @Nullable Float baseFov,
        boolean hideUI
    ) {
        super(id, keyframes);

        this.baseFov = baseFov != null ? baseFov : 60;
        this.hideUI = hideUI;
    }

    /**
     * Build the camera sequence builder object with all the keyframes.
     * WARNING: If <code>HideUI</code> is enabled, this will not take effect here.
     */
    private CameraSequenceBuilder buildSequenceBuilder(@Nullable PlayerRef playerRef, @Nullable Runnable onComplete) {
        CameraSequenceBuilder seqBuilder = new CameraSequenceBuilder()
            .baseFov(baseFov)
            .onComplete((playerRef1) -> {
                if (onComplete != null) onComplete.run();
                showUI(playerRef1);
            });

        // Add keyframes
        for (CameraKeyframe keyframe : (CameraKeyframe[]) keyframes) {
            Vector3d position = keyframe.isRelativeToPlayer() && playerRef != null
                ? new Vector3d(playerRef.getTransform().getPosition()).add(keyframe.getPosition())
                : keyframe.getPosition();
            CameraKeyframeBuilder keyframeBuilder = new CameraKeyframeBuilder(
                keyframe.getDurationSeconds(),
                keyframe.getEasing()
            );

            if (keyframe.getFov() != null) keyframeBuilder.fov(keyframe.getFov());
            if (keyframe.getDepthOfFieldSettingsAsset() != null) keyframeBuilder.depthOfField(keyframe.getDepthOfFieldSettingsAsset().getDepthOfFieldSettings());
            if (keyframe instanceof CameraKeyframe.Keyframe) {
                Rotation3f look = keyframe.isRelativeToPlayer() && playerRef != null
                    ? new Rotation3f(playerRef.getHeadRotation()).add(((CameraKeyframe.Keyframe) keyframe).getLookRadians())
                    : ((CameraKeyframe.Keyframe) keyframe).getLookRadians();
                look.x = ((CameraKeyframe.Keyframe) keyframe).getLookRadians().x; // Pitch should never be relative.
                look.z = ((CameraKeyframe.Keyframe) keyframe).getLookRadians().z; // Roll should never be relative.
                float yaw = playerRef != null ? playerRef.getHeadRotation().yaw() : 0;
                position = keyframe.isRelativeToPlayer() && playerRef != null
                    ? new Vector3d(playerRef.getTransform().getPosition()).add(new Vector3d(
                    -keyframe.getPosition().x*Math.sin(yaw) + keyframe.getPosition().z*Math.cos(yaw),
                    keyframe.getPosition().y,
                    -keyframe.getPosition().x*Math.cos(yaw) - keyframe.getPosition().z*Math.sin(yaw)
                )) : keyframe.getPosition();
                seqBuilder = seqBuilder.keyframe(keyframeBuilder
                    .position(position)
                    .look(look)
                );
            } else if (keyframe instanceof CameraKeyframe.KeyframeLookingAt) {
                Vector3d lookAtPoint = keyframe.isRelativeToPlayer() && playerRef != null
                    ? playerRef.getTransform().getPosition().add(((CameraKeyframe.KeyframeLookingAt) keyframe).getLookAtPoint())
                    : ((CameraKeyframe.KeyframeLookingAt) keyframe).getLookAtPoint();
                seqBuilder = seqBuilder.keyframe(keyframeBuilder
                    .position(position)
                    .lookAt(lookAtPoint)
                );
            }
        }
        // Add flags to return camera control to player afterwards.
        return seqBuilder.addFlag((byte)1).addFlag((byte)2).addFlag((byte)4);
    }

    public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(SequenceAsset::getAssetStore));

    /**
     * Build the camera sequence builder object with all the keyframes.
     * WARNING: If <code>HideUI</code> is enabled, this will not take effect here.
     */
    private CameraSequenceBuilder buildSequenceBuilder(@Nullable PlayerRef playerRef) {
        return buildSequenceBuilder(playerRef, null);
    }

    public void play(@Nonnull PlayerRef playerRef, @Nullable Runnable onComplete) {
        if (hideUI) hideUI(playerRef);
        buildSequenceBuilder(playerRef, onComplete).sendTo(playerRef);
    }

    public void play(@Nonnull PlayerRef playerRef) {
        play(playerRef, null);
    }

    public void addKeyframe(CameraKeyframe keyframe) {
        CameraKeyframe[] updated = Arrays.copyOf((CameraKeyframe[]) keyframes, keyframes.length + 1);
        updated[keyframes.length] = keyframe;
        keyframes = updated;
    }

    public void removeKeyframe(int keyframe) {
        keyframes[keyframe] = null;
        var listFrames = new ArrayList(Arrays.stream(keyframes).toList());
        listFrames.remove(keyframe);
        this.keyframes = (CameraKeyframe[]) listFrames.toArray(new CameraKeyframe[keyframes.length-1]);
    }

    public CameraSequenceAsset clone() {
        return new CameraSequenceAsset(id, (CameraKeyframe[]) keyframes, baseFov, hideUI);
    }
}
