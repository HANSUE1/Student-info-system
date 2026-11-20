package com.example.myapplication;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

/** Shared animations, so every screen moves the same way. */
final class Motion {
    /** Fast start, long gentle stop: things arrive confidently and settle. */
    static final Interpolator EMPHASIZED = new PathInterpolator(0.16f, 1f, 0.3f, 1f);

    private Motion() {
    }

    /** False when the user turned on "Remove animations" in the phone's accessibility settings. */
    static boolean enabled(Context context) {
        return Settings.Global.getFloat(context.getContentResolver(),
                Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f;
    }

    /**
     * Fades views in while they rise 24dp, one after another.
     * Views end fully visible, and with animations off Android jumps straight to that end state.
     */
    static void riseIn(View... views) {
        float rise = 24 * views[0].getResources().getDisplayMetrics().density;
        for (int i = 0; i < views.length; i++) {
            View v = views[i];
            v.setAlpha(0f);
            v.setTranslationY(rise);
            v.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setStartDelay(100L + i * 50L)
                    .setDuration(550)
                    .setInterpolator(EMPHASIZED);
        }
    }

    /** Short side-to-side shake that points at a field with an error. */
    static void shake(View v) {
        float d = 8 * v.getResources().getDisplayMetrics().density;
        ObjectAnimator.ofFloat(v, View.TRANSLATION_X, 0f, d, -d, d * 0.6f, -d * 0.6f, d * 0.3f, 0f)
                .setDuration(400)
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
