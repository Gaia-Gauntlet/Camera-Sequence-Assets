package net.lordimass;

import com.creditor.Creditor;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import net.lordimass.assets.*;
import net.lordimass.assets.keyframes.*;
import net.lordimass.assets.sequences.CameraSequenceAsset;
import net.lordimass.assets.sequences.GlobalSequenceAsset;
import net.lordimass.assets.sequences.SequenceAsset;
import net.lordimass.command.CameraSequenceCommand;
import net.lordimass.components.Ephemeral;
import net.lordimass.components.TransformingComponent;
import net.lordimass.components.SequenceEntityComponent;
import net.lordimass.resources.WorldSequenceStore;
import net.lordimass.systems.TransformingEntitySystem;
import net.lordimass.tvEffects.TriggerSequenceEffect;

import java.util.logging.Level;

public class Main extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public Main(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void start() {
        LOGGER.at(Level.INFO).log("Starting Camera Sequence Assets!");

        Creditor.start(this);
    }

    @Override
    protected void setup() {
        LOGGER.at(Level.INFO).log("Setting up Camera Sequence Assets!");

        this.getAssetRegistry().register(
            HytaleAssetStore.builder(SequenceAsset.class, new DefaultAssetMap<>())
                .setPath(SequenceAsset.ASSET_PATH)
                .setCodec(SequenceAsset.CODEC)
                .setKeyFunction(SequenceAsset::getId)
                .build()
        );

        WorldSequenceStore.setResourceType(getEntityStoreRegistry().registerResource(
            WorldSequenceStore.class, WorldSequenceStore.ID, WorldSequenceStore.CODEC
        ));

        Ephemeral.setComponentType(getEntityStoreRegistry().registerComponent(
            Ephemeral.class, "Ephemeral", Ephemeral.CODEC));
        SequenceEntityComponent.setComponentType(getEntityStoreRegistry().registerComponent(
            SequenceEntityComponent.class, "SequenceEntity", SequenceEntityComponent.CODEC));
        TransformingComponent.setComponentType(getEntityStoreRegistry().registerComponent(
            TransformingComponent.class, "Transforming", TransformingComponent.CODEC));

        getEntityStoreRegistry().registerSystem(new TransformingEntitySystem());

        SequenceAsset.CODEC.register("CameraSequence", CameraSequenceAsset.class, CameraSequenceAsset.CODEC);
        SequenceAsset.CODEC.register("GlobalSequence", GlobalSequenceAsset.class, GlobalSequenceAsset.CODEC);

        CameraKeyframe.CODEC.register("CameraKeyframe", CameraKeyframe.Keyframe.class, CameraKeyframe.Keyframe.CODEC);
        CameraKeyframe.CODEC.register("CameraKeyframeLookingAt", CameraKeyframe.KeyframeLookingAt.class, CameraKeyframe.KeyframeLookingAt.CODEC);
        CameraKeyframe.CODEC.register("DelayKeyframe", DelayKeyframe.class, DelayKeyframe.CODEC);
        CameraKeyframe.CODEC.register("MessageKeyframe", MessageKeyframe.class, MessageKeyframe.CODEC);
        CameraKeyframe.CODEC.register("SoundEventKeyframe", SoundEventKeyframe.class, SoundEventKeyframe.CODEC);

        GlobalKeyframe.CODEC.register("DelayKeyframe", DelayKeyframe.class, DelayKeyframe.CODEC);
        GlobalKeyframe.CODEC.register("MessageKeyframe", MessageKeyframe.class, MessageKeyframe.CODEC);
        GlobalKeyframe.CODEC.register("SoundEventKeyframe", SoundEventKeyframe.class, SoundEventKeyframe.CODEC);
        GlobalKeyframe.CODEC.register("GlobalSpawnNpcKeyframe", GlobalSpawnNpcKeyframe.class, GlobalSpawnNpcKeyframe.CODEC);
        GlobalKeyframe.CODEC.register("GlobalTransformNpcKeyframe", GlobalTransformNpcKeyframe.class, GlobalTransformNpcKeyframe.CODEC);

        DepthOfFieldSettingsAsset.register(this);

        var commandRegistry = getCommandRegistry();
        commandRegistry.registerCommand(new CameraSequenceCommand());
        TriggerEffect.CODEC.register("TriggerCameraSequence", TriggerSequenceEffect.class, TriggerSequenceEffect.CODEC);
        Creditor.setup(this);
    }

    @Override
    protected void shutdown() {
        LOGGER.at(Level.INFO).log("Shutting down Camera Sequence Assets!");
    }
}
