package net.lordimass.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import net.lordimass.assets.keyframes.GlobalSequenceContext;
import net.lordimass.assets.keyframes.SequenceKeyframe;
import net.lordimass.assets.sequences.GlobalSequenceAsset;
import net.lordimass.assets.sequences.PlayerSequenceAsset;
import net.lordimass.assets.sequences.SequenceAsset;

import java.awt.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CameraSequenceCommand extends AbstractTargetPlayerCommand {
    final RequiredArg<SequenceAsset> sequenceArg = withRequiredArg("sequence", "The ID of the camera sequence asset to play back",
        new AssetArgumentType<>("CameraAsset", SequenceAsset.class,
            "Camera Asset"));
    final OptionalArg<Integer> keyFrameArg = withOptionalArg("Keyframe", "Keyframe to jump to", ArgTypes.INTEGER);;
    final FlagArg globalArg = withFlagArg("Global", "Whether to play this sequence globally, or just to the target player.");

    public CameraSequenceCommand() {
        super("camerasequence", "Play back a camera sequence asset.");
        this.addAliases("cinematic");

        addSubCommand(new NewCommand());
        addSubCommand(new RemoveCommand());
    }

    @Override
    protected void execute(@Nonnull CommandContext commandContext, @Nullable Ref<EntityStore> sourceRef,
            @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef,
            @Nonnull World world, @Nonnull Store<EntityStore> store) {

        var originalSeq = sequenceArg.get(commandContext);
        if (originalSeq == null) {
            commandContext.sendMessage(
                    Message.raw("Couldn't find camera sequence '" + sequenceArg.get(commandContext).getId() + "'")
                            .color(Color.RED));
            return;
        }
        var seq = originalSeq.clone();

        var frame = keyFrameArg.get(commandContext);

        Runnable onComplete = () -> {
            commandContext.sendMessage(Message.raw("Camera sequence completed").color(Color.GREEN));
        };

        if (frame != null) { // play from the single frame instead
            var frames = seq.getKeyframes();
            if (frame < 0 || frame >= frames.length) {
                commandContext.sendMessage(
                        Message.raw("Frame " + frame + " is out of range! Max is " + frames.length));
                return;
            }

            SequenceKeyframe[] reorderedFrames = new SequenceKeyframe[frames.length - frame];
            System.arraycopy(frames, frame, reorderedFrames, 0, reorderedFrames.length);
            seq.setKeyframes(reorderedFrames);

            commandContext.sendMessage(
                    Message.raw("Playing sequence '" + sequenceArg.get(commandContext).getId() + "' from frame " + frame + " to player."));
            return;
        } else {
            commandContext.sendMessage(
                Message.raw("Playing sequence '" + sequenceArg.get(commandContext).getId() + "' to player."));
        }

        if (!globalArg.get(commandContext)) seq.play(world, playerRef, onComplete);
        else seq.play(world, null, onComplete);
    }
}
