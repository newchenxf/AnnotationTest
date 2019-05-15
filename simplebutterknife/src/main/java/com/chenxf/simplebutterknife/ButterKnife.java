package com.chenxf.simplebutterknife;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.util.Log;
import android.view.View;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.List;

import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.PUBLIC;
import static java.lang.reflect.Modifier.STATIC;

public final class ButterKnife {
    private ButterKnife() {
        throw new AssertionError();
    }

    private static final String TAG = "ButterKnife";
    private static boolean debug = false;

    /**
     * Control whether debug logging is enabled.
     */
    public static void setDebug(boolean debug) {
        ButterKnife.debug = debug;
    }

    /**
     * BindView annotated fields and methods in the specified {@link Activity}. The current content
     * view is used as the view root.
     *
     * @param target Target activity for view binding.
     */
    @NonNull
    @UiThread
    public static Unbinder bind(@NonNull Activity target) {
        View sourceView = target.getWindow().getDecorView();
        return bind(target, sourceView);
    }


    /**
     * BindView annotated fields and methods in the specified {@code target} using the {@code source}
     * {@link View} as the view root.
     *
     * @param target Target class for view binding.
     * @param source View root on which IDs will be looked up.
     */
    @NonNull
    @UiThread
    public static Unbinder bind(@NonNull Object target, @NonNull View source) {
        List<Unbinder> unbinders = new ArrayList<>();
        Class<?> targetClass = target.getClass();
        if ((targetClass.getModifiers() & PRIVATE) != 0) {
            throw new IllegalArgumentException(targetClass.getName() + " must not be private.");
        }

        while (true) {
            String clsName = targetClass.getName();
            if (clsName.startsWith("android.") || clsName.startsWith("java.")
                    || clsName.startsWith("androidx.")) {
                break;
            }

            for (Field field : targetClass.getDeclaredFields()) {
                int unbinderStartingSize = unbinders.size();
                Unbinder unbinder;

                unbinder = parseBindView(target, field, source);
                if (unbinder != null) unbinders.add(unbinder);

                if (unbinders.size() - unbinderStartingSize > 1) {
                    throw new IllegalStateException(
                            "More than one bind annotation on " + targetClass.getName() + "." + field.getName());
                }
            }

            targetClass = targetClass.getSuperclass();
        }

        if (unbinders.isEmpty()) {
            if (debug) Log.d(TAG, "MISS: Reached framework class. Abandoning search.");
            return Unbinder.EMPTY;
        }

        if (debug) Log.d(TAG, "HIT: Reflectively found " + unbinders.size() + " bindings.");
        return new CompositeUnbinder(unbinders);
    }

    private static @Nullable
    Unbinder parseBindView(Object target, Field field, View source) {
        BindView bindView = field.getAnnotation(BindView.class);
        if (bindView == null) {
            return null;
        }
        validateMember(field);

        int id = bindView.value();
        Class<?> viewClass = field.getType();
        if (!View.class.isAssignableFrom(viewClass) && !viewClass.isInterface()) {
            throw new IllegalStateException(
                    "@BindView fields must extend from View or be an interface. ("
                            + field.getDeclaringClass().getName()
                            + '.'
                            + field.getName()
                            + ')');
        }

        String who = "field '" + field.getName() + "'";
        Object view = Utils.findOptionalViewAsType(source, id, who, viewClass);
        trySet(field, target, view);

        return new FieldUnbinder(target, field);
    }

    private static <T extends AccessibleObject & Member> void validateMember(T object) {
        int modifiers = object.getModifiers();
        if ((modifiers & (PRIVATE | STATIC)) != 0) {
            throw new IllegalStateException(object.getDeclaringClass().getName()
                    + "."
                    + object.getName()
                    + " must not be private or static");
        }
        if ((modifiers & PUBLIC) == 0) {
            object.setAccessible(true);
        }
    }

    static void trySet(Field field, Object target, @Nullable Object value) {
        try {
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to assign " + value + " to " + field + " on " + target, e);
        }
    }

}