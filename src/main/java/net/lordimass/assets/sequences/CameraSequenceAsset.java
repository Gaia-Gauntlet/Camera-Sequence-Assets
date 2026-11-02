package net.lordimass.assets.sequences;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.modules.camera.CameraKeyframeBuilder;
import com.hypixel.hytale.server.core.modules.camera.CameraSequenceBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import lombok.Getter;
import net.lordimass.assets.keyframes.CameraKeyframe;
import net.lordimass.assets.keyframes.GlobalKeyframe;
import net.lordimass.assets.keyframes.SequenceContext;
import net.lordimass.assets.keyframes.SequenceKeyframe;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.lordimass.utils.UiUtils.hideUI;
import static net.lordimass.utils.UiUtils.showUI;

public class CameraSequenceAsset extends PlayerSequenceAsset {

    @Nonnull public static final BuilderCodec<@NotNull CameraSequenceAsset> CODEC =
        BuilderCodec.builder(
            CameraSequenceAsset.class,
            CameraSequenceAsset::new,
            PlayerSequenceAsset.ABSTRACT_CODEC
        )
            .appendInherited(new KeyedCodec<>("Keyframes", new ArrayCodec<>(CameraKeyframe.CODEC, SequenceKeyframe[]::new)),
                (asset, keyframes) -> asset.keyframes = keyframes,
                SequenceAsset::getKeyframes,
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
        @Nonnull SequenceKeyframe initialKeyframe,
        @Nullable Float baseFov,
        boolean hideUI
    ) {
        super(id, initialKeyframe);

        this.baseFov = baseFov != null ? baseFov : 60;
        this.hideUI = hideUI;
    }

    public CameraSequenceAsset(
        @Nonnull String id,
        @Nonnull SequenceKeyframe[] keyframes,
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
        CameraKeyframeBuilder prevKeyframeBuilder = null;
        for (var keyframe : keyframes) {

            // Add a dud frame for non-camera frames to ensure future frames still line up
            if (!(keyframe instanceof CameraKeyframe camFrame)) {
                if (prevKeyframeBuilder != null && keyframe.getDurationSeconds() != 0 && keyframe.isWaitForCompletion()) {
                    var frame = buildKeyframeWithDuration(
                        prevKeyframeBuilder,
                        (float) keyframe.getDurationSeconds()
                    );
                    seqBuilder.keyframe(frame);
                }
                continue;
            }

            CameraKeyframeBuilder keyframeBuilder = new CameraKeyframeBuilder(
                (float) camFrame.getDurationSeconds(),
                camFrame.getEasing()
            );

            if (camFrame.getFov() != null) keyframeBuilder.fov(camFrame.getFov());
            if (camFrame.getDepthOfFieldSettingsAsset() != null) keyframeBuilder.depthOfField(camFrame.getDepthOfFieldSettingsAsset().getDepthOfFieldSettings());
            if (camFrame instanceof CameraKeyframe.Keyframe) {
                var look = getLookForFrame((CameraKeyframe.Keyframe) camFrame, playerRef);
                var position = getPositionForFrame(camFrame, playerRef);
                keyframeBuilder = keyframeBuilder
                    .position(position)
                    .look(look);
                seqBuilder = seqBuilder.keyframe(keyframeBuilder);
            } else if (camFrame instanceof CameraKeyframe.KeyframeLookingAt) {
                var lookAtPoint = getLookAtPointForFrame((CameraKeyframe.KeyframeLookingAt) camFrame, playerRef);
                var position = getPositionForFrame(camFrame, playerRef);
                keyframeBuilder = keyframeBuilder
                    .position(position)
                    .lookAt(lookAtPoint);
                seqBuilder = seqBuilder.keyframe(keyframeBuilder);
            }
            prevKeyframeBuilder = keyframeBuilder;
        }
        // Add flags to return camera control to player afterwards.
        return seqBuilder.addFlag((byte)1).addFlag((byte)2).addFlag((byte)4);
    }

    private static Rotation3f getLookForFrame(CameraKeyframe.Keyframe frame, @Nullable PlayerRef playerRef) {
        Rotation3f look = frame.isRelativeToPlayer() && playerRef != null
            ? new Rotation3f(playerRef.getHeadRotation()).add(frame.getLookRadians())
            : frame.getLookRadians();
        look.x = frame.getLookRadians().x; // Pitch should never be relative.
        look.z = frame.getLookRadians().z; // Roll should never be relative.
        return look;
    }

    private static Vector3d getLookAtPointForFrame(CameraKeyframe.KeyframeLookingAt frame, @Nullable PlayerRef playerRef) {
        return frame.isRelativeToPlayer() && playerRef != null
            ? playerRef.getTransform().getPosition().add(frame.getLookAtPoint())
            : frame.getLookAtPoint();
    }

    private static Vector3d getPositionForFrame(CameraKeyframe frame, @Nullable PlayerRef playerRef) {
        if (frame instanceof CameraKeyframe.Keyframe camFrame) {
            float yaw = playerRef != null ? playerRef.getHeadRotation().yaw() : 0;
            return camFrame.isRelativeToPlayer() && playerRef != null
                ? new Vector3d(playerRef.getTransform().getPosition()).add(new Vector3d(
                -camFrame.getPosition().x*Math.sin(yaw) + camFrame.getPosition().z*Math.cos(yaw),
                camFrame.getPosition().y,
                -camFrame.getPosition().x*Math.cos(yaw) - camFrame.getPosition().z*Math.sin(yaw)
            )) : camFrame.getPosition();
        } else {
            return frame.isRelativeToPlayer() && playerRef != null
                ? new Vector3d(playerRef.getTransform().getPosition()).add(frame.getPosition())
                : frame.getPosition();
        }
    }

    private static CameraKeyframeBuilder buildKeyframeWithDuration(CameraKeyframeBuilder frame, float duration) {
        var builtFrame = frame.build();
        var newFrame = new CameraKeyframeBuilder(duration, builtFrame.easing);
        if (builtFrame.depthOfField != null) newFrame = newFrame.depthOfField(builtFrame.depthOfField);
        if (builtFrame.fov != null) newFrame = newFrame.fov(builtFrame.fov);
        if (builtFrame.look != null) newFrame = newFrame.look(
            new Rotation3f(builtFrame.look.pitch, builtFrame.look.yaw, builtFrame.look.roll)
        );
        if (builtFrame.lookAtPoint != null) newFrame = newFrame.lookAt(
            new Vector3d(builtFrame.lookAtPoint.x, builtFrame.lookAtPoint.y, builtFrame.lookAtPoint.z)
        );
        if (builtFrame.position != null) newFrame = newFrame.position(
            new Vector3d(builtFrame.position.x, builtFrame.position.y, builtFrame.position.z)
        );
        return newFrame;
    }

    @Override
    void play(SequenceContext ctx, @Nonnull PlayerRef playerRef, @Nullable Runnable onComplete) {
        if (hideUI) hideUI(playerRef);
        buildSequenceBuilder(playerRef, onComplete).sendTo(playerRef);

        // Schedule running global keyframes in time
        var worldUuid = playerRef.getWorldUuid();
        if (worldUuid == null) return;
        var world = Universe.get().getWorld(worldUuid);
        if (world == null) return;
        for (SequenceKeyframe keyframe : keyframes) {
            if (!(keyframe instanceof GlobalKeyframe gFrame)) continue;
            world.scheduleAfter(
                () -> gFrame.run(ctx),
                (long) (keyframe.getStartTimestamp(this.keyframes) * 1000000),
                TimeUnit.MICROSECONDS
            );
        }
    }

    public void addKeyframe(SequenceKeyframe keyframe) {
        var updated = Arrays.copyOf(keyframes, keyframes.length + 1);
        updated[keyframes.length] = keyframe;
        keyframes = updated;
    }

    public void removeKeyframe(int keyframe) {
        keyframes[keyframe] = null;
        var listFrames = new ArrayList(Arrays.stream(keyframes).toList());
        listFrames.remove(keyframe);
        this.keyframes = (SequenceKeyframe[]) listFrames.toArray(new CameraKeyframe[keyframes.length-1]);
    }

    public CameraSequenceAsset clone() {
        return new CameraSequenceAsset(id, keyframes, baseFov, hideUI);
    }
}
