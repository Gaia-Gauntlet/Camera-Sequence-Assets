package net.lordimass.utils;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.EasingType;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.joml.Vector3d;

public class TransformUtils {
    public static final Codec<EasingType> EASING_CODEC = new EnumCodec<>(EasingType.class);

    private TransformUtils() {}

    public static Transform getEyeTransform(Ref<EntityStore> ref, ComponentAccessor<EntityStore> componentAccessor) {
        assert ref != null;
        var transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());
        assert transformComponent != null;
        var transform = transformComponent.getTransform().clone();
        var headTransform = componentAccessor.getComponent(ref, HeadRotation.getComponentType());
        assert headTransform != null;
        var model = componentAccessor.getComponent(ref, ModelComponent.getComponentType());
        assert model != null;
        transform.getPosition().add(0, model.getModel().getEyeHeight(), 0);

        var rot = headTransform.getRotation().clone();
        rot.setYaw((float) Math.toDegrees(rot.yaw()));
        rot.setPitch((float) Math.toDegrees(rot.pitch()));
        rot.setRoll((float) Math.toDegrees(rot.roll()));

        transform.setRotation(rot);
        return transform;
    }

    public static double interpolate(double start, double end, double t) {
        return start + (end - start) * t;
    }

    public static double interpolate(double start, double end, double t, EasingType easing) {
        return interpolate(start, end, ease(t, easing));
    }

    public static Vector3d interpolate(Vector3d start, Vector3d end, double t) {
        return new Vector3d(
            interpolate(start.x, end.x, t),
            interpolate(start.y, end.y, t),
            interpolate(start.z, end.z, t)
        );
    }

    public static Vector3d interpolate(Vector3d start, Vector3d end, double t, EasingType easing) {
        return interpolate(start, end, ease(t, easing));
    }

    public static Rotation3f interpolate(Rotation3f start, Rotation3f end, double t) {
        return new Rotation3f(
            (float) interpolate(start.x, end.x, t),
            (float) interpolate(start.y, end.y, t),
            (float) interpolate(start.z, end.z, t)
        );
    }

    public static Rotation3f interpolate(Rotation3f start, Rotation3f end, double t, EasingType easing) {
        return interpolate(start, end, ease(t, easing));
    }

    public static Transform interpolate(Transform start, Transform end, double t) {
        return new Transform(
            interpolate(start.getPosition(), end.getPosition(), t),
            interpolate(start.getRotation(), end.getRotation(), t)
        );
    }

    public static Transform interpolate(Transform start, Transform end, double t, EasingType easing) {
        return interpolate(start, end, ease(t, easing));
    }

    private static double ease(double t, EasingType easingType) {
        switch (easingType) {

            // QUADRATIC
            case QuadIn:
                return t * t;
            case QuadOut:
                return 1 - (1 - t) * (1 - t);
            case QuadInOut:
                return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;

            // CUBIC
            case CubicIn:
                return t * t * t;
            case CubicOut:
                return 1 - Math.pow(1 - t, 3);
            case CubicInOut:
                return t < 0.5 ? 4 * t * t * t : 1 - Math.pow(-2 * t + 2, 3) / 2;

            // QUARTIC
            case QuartIn:
                return t * t * t * t;
            case QuartOut:
                return 1 - Math.pow(1 - t, 4);
            case QuartInOut:
                return t < 0.5 ? 8 * t * t * t * t : 1 - Math.pow(-2 * t + 2, 4) / 2;

            // QUINTIC
            case QuintIn:
                return t * t * t * t * t;
            case QuintOut:
                return 1 - Math.pow(1 - t, 5);
            case QuintInOut:
                return t < 0.5 ? 16 * t * t * t * t * t : 1 - Math.pow(-2 * t + 2, 5) / 2;

            // SINE
            case SineIn:
                return 1 - Math.cos((t * Math.PI) / 2);
            case SineOut:
                return Math.sin((t * Math.PI) / 2);
            case SineInOut:
                return -(Math.cos(Math.PI * t) - 1) / 2;

            // EXPONENTIAL
            case ExpoIn:
                return t == 0 ? 0 : Math.pow(2, 10 * t - 10);
            case ExpoOut:
                return t == 1 ? 1 : 1 - Math.pow(2, -10 * t);
            case ExpoInOut:
                if (t == 0) return 0;
                if (t == 1) return 1;
                return t < 0.5 ? Math.pow(2, 20 * t - 10) / 2 : (2 - Math.pow(2, -20 * t + 10)) / 2;

            // CIRCULAR
            case CircIn:
                return 1 - Math.sqrt(1 - t * t);
            case CircOut:
                return Math.sqrt(1 - Math.pow(t - 1, 2));
            case CircInOut:
                return t < 0.5
                    ? (1 - Math.sqrt(1 - Math.pow(2 * t, 2))) / 2
                    : (Math.sqrt(1 - Math.pow(-2 * t + 2, 2)) + 1) / 2;

            // BACK
            case BackIn: {
                double c1 = 1.70158;
                double c3 = c1 + 1;
                return c3 * t * t * t - c1 * t * t;
            }
            case BackOut: {
                double c1 = 1.70158;
                double c3 = c1 + 1;
                return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
            }
            case BackInOut: {
                double c1 = 1.70158;
                double c2 = c1 * 1.525;
                return t < 0.5
                    ? (Math.pow(2 * t, 2) * ((c2 + 1) * 2 * t - c2)) / 2
                    : (Math.pow(2 * t - 2, 2) * ((c2 + 1) * (t * 2 - 2) + c2) + 2) / 2;
            }

            // ELASTIC
            case ElasticIn: {
                if (t == 0) return 0;
                if (t == 1) return 1;
                double c4 = (2 * Math.PI) / 3;
                return -Math.pow(2, 10 * t - 10) * Math.sin((t * 10 - 10.75) * c4);
            }
            case ElasticOut: {
                if (t == 0) return 0;
                if (t == 1) return 1;
                double c4 = (2 * Math.PI) / 3;
                return Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * c4) + 1;
            }
            case ElasticInOut: {
                if (t == 0) return 0;
                if (t == 1) return 1;
                double c5 = (2 * Math.PI) / 4.5;
                double sin = Math.sin((20 * t - 11.125) * c5);
                return t < 0.5
                    ? -(Math.pow(2, 20 * t - 10) * sin) / 2
                    : (Math.pow(2, -20 * t + 10) * sin) / 2 + 1;
            }

            // BOUNCE
            case BounceIn:
                return 1 - bounceOutHelper(1 - t);
            case BounceOut:
                return bounceOutHelper(t);
            case BounceInOut:
                return t < 0.5
                    ? (1 - bounceOutHelper(1 - 2 * t)) / 2
                    : (1 + bounceOutHelper(2 * t - 1)) / 2;

            default:
                return t;
        }
    }
    
    private static double bounceOutHelper(double t) {
        double n1 = 7.5625;
        double d1 = 2.75;

        if (t < 1 / d1) {
            return n1 * t * t;
        } else if (t < 2 / d1) {
            return n1 * (t -= 1.5 / d1) * t + 0.75;
        } else if (t < 2.5 / d1) {
            return n1 * (t -= 2.25 / d1) * t + 0.9375;
        } else {
            return n1 * (t -= 2.625 / d1) * t + 0.984375;
        }
    }
}
