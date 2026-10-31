package net.lordimass.assets.keyframes;

import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;

import javax.annotation.Nullable;

public record GlobalSequenceContext(
    World world,
    @Nullable PlayerRef targetPlayer
) {
}
