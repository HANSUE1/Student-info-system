package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import com.google.android.material.color.MaterialColors;
import com.google.android.material.transition.platform.MaterialContainerTransform;
import com.google.android.material.transition.platform.MaterialContainerTransformSharedElementCallback;

/** Shared animations, so every screen moves the same way. */
final class Motion {
    /** Fast start, long gentle stop: things arrive confidently and settle. */
    static final Interpolator EMPHASIZED = new PathInterpolator(0.16f, 1f, 0.3f, 1f);

    private Motion() {
    }

    /** Fades a view in while it rises 16dp. With animations off Android jumps straight to the end state. */
    static void riseIn(View v) {
        v.setAlpha(0f);
        v.setTranslationY(16 * v.getResources().getDisplayMetrics().density);
        v.animate().alpha(1f).translationY(0f).setDuration(250).setInterpolator(EMPHASIZED);
    }

    /** Call before super.onCreate on a screen whose views other screens grow out of. */
    static void growOut(Activity activity) {
        activity.setExitSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        activity.getWindow().setSharedElementsUseOverlay(false);
    }

    /** Call before super.onCreate: the screen grows out of the tapped view and shrinks back into it on close. */
    static void growFrom(Activity activity, String transitionName) {
        activity.findViewById(android.R.id.content).setTransitionName(transitionName);
        activity.setEnterSharedElementCallback(new MaterialContainerTransformSharedElementCallback());
        activity.getWindow().setSharedElementEnterTransition(containerTransform(activity, 300));
        activity.getWindow().setSharedElementReturnTransition(containerTransform(activity, 250));
    }

    private static MaterialContainerTransform containerTransform(Activity activity, long durationMs) {
        MaterialContainerTransform transform = new MaterialContainerTransform();
        transform.addTarget(android.R.id.content);
        transform.setDuration(durationMs);
        // Solid surface color while morphing, so nothing shows through the growing shape.
        transform.setAllContainerColors(MaterialColors.getColor(activity,
                com.google.android.material.R.attr.colorSurface, 0));
        return transform;
    }

    /** Short side-to-side shake that points at a field with an error. */
    static void shake(View v) {
        float d = 8 * v.getResources().getDisplayMetrics().density;
        ObjectAnimator.ofFloat(v, View.TRANSLATION_X, 0f, d, -d, d * 0.6f, -d * 0.6f, d * 0.3f, 0f)
                .setDuration(300)
                .start();
    }

    /** Screen change deeper into the app: new screen slides in from the right. */
    @SuppressWarnings("deprecation") // replacement (overrideActivityTransition) needs API 34; minSdk is 24
    static void forward(Activity activity) {
        activity.overridePendingTransition(R.anim.forward_enter, R.anim.forward_exit);
    }

    /** Screen change back out: the previous screen slides in from the left. */
    @SuppressWarnings("deprecation")
    static void back(Activity activity) {
        activity.overridePendingTransition(R.anim.back_enter, R.anim.back_exit);
    }
}
