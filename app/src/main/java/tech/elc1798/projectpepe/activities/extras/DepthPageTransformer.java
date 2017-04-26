package tech.elc1798.projectpepe.activities.extras;


import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Android ViewPager pager animation, taken from: https://developer.android.com/training/animation/screen-slide.html
 */
public class DepthPageTransformer implements ViewPager.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    public void transformPage(View view, float position) {
        int pageWidth = view.getWidth();

        if (position < -1) {                // [-Infinity,-1)
            view.setAlpha(0);               // This page is way off-screen to the left.
        } else if (position <= 0) {         // [-1,0]
            view.setAlpha(1);               // Use the default slide transition when moving to the left page
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);
        } else if (position <= 1) {         // (0,1]
            view.setAlpha(1 - position);    // Fade the page out.

            // Counteract the default slide transition
            view.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE
                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);
        } else {                            // (1,+Infinity]
            view.setAlpha(0);               // This page is way off-screen to the right.
        }
    }
}