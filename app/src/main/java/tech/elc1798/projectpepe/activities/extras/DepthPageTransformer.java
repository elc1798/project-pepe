package tech.elc1798.projectpepe.activities.extras;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Android ViewPager pager animation, taken from: https://developer.android.com/training/animation/screen-slide.html
 * See link for graphical demo of the visuals of the animation
 */
public class DepthPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;
    private static final float ALPHA_INVISIBLE = 0.0f;
    private static final float ALPHA_OPAQUE = 1.0f;
    private static final float NORMAL_SIZE_SCALE_FACTOR = 1.0f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) {                // [-Infinity,-1)
            view.setAlpha(ALPHA_INVISIBLE); // This page is way off-screen to the left.
        } else if (position <= 0) {         // [-1,0]
            view.setAlpha(ALPHA_OPAQUE);    // Use the default slide transition when moving to the left page
            view.setTranslationX(0);
            view.setScaleX(NORMAL_SIZE_SCALE_FACTOR);
            view.setScaleY(NORMAL_SIZE_SCALE_FACTOR);
        } else if (position <= 1) {         // (0,1]
            view.setAlpha(1 - position);    // Fade the page out.

            // Counteract the default slide transition
            view.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
        } else {                            // (1,+Infinity]
            view.setAlpha(ALPHA_INVISIBLE); // This page is way off-screen to the right.
        }
    }
}