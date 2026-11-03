package net.lordimass.components;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.Objects;

public class NpcSelector {
    public static final BuilderCodec<NpcSelector> CODEC = BuilderCodec
        .builder(NpcSelector.class, NpcSelector::new)
        .append(new KeyedCodec<>("SelectorClassName", Codec.STRING),
            (c, v) -> c.selectorClassName = v,
            c -> c.selectorClassName)
        .documentation("An arbitrary class to be given to the NPC, can be used as a one-to-many " +
            "selector in other keyframes.")
        .add()
        .append(new KeyedCodec<>("SelectorId", Codec.STRING),
            (c, v) -> c.selectorId = v,
            c -> c.selectorId)
        .documentation("An arbitrary id to be given to the NPC, can be used as a one-to-one " +
            "selector in other keyframes.")
        .add()
        .build();

    @Getter @Nullable private String selectorClassName;
    @Getter @Nullable private String selectorId;

    public NpcSelector() {}

    public NpcSelector(@Nullable String selectorClassName, @Nullable String selectorId) {
        this.selectorClassName = selectorClassName;
        this.selectorId = selectorId;
    }

    public boolean matches(NpcSelector other) {
        if (this.selectorId != null && Objects.equals(this.selectorId, other.selectorId)) return true;
        if (this.selectorClassName != null && Objects.equals(this.selectorClassName, other.selectorClassName)) return true;
        return false;
    }

    public NpcSelector clone() {
        return new NpcSelector(selectorClassName, selectorId);
    }
}
