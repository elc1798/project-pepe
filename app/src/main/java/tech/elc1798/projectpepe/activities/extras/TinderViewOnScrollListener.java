package tech.elc1798.projectpepe.activities.extras;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import tech.elc1798.projectpepe.activities.TinderViewActivity;

public class TinderViewOnScrollListener extends RecyclerView.OnScrollListener {

    private static final double SNAP_TOLERANCE = 0.5;
    private static final int SCROLL_SENSITIVITY = 10;

    private enum ScrollDirection {
        LEFT, RIGHT, STOPPED
    }

    private TinderViewActivity parentActivityRef;
    private int displayWidth;
    private int scrollState;
    private ScrollDirection lastScrolledDirection;

    public TinderViewOnScrollListener(TinderViewActivity parentActivityRef) {
        this.parentActivityRef = parentActivityRef;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        parentActivityRef.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayWidth = displayMetrics.widthPixels;
    }

    /**
     * Callback method to be invoked when the RecyclerView has been scrolled. This will be
     * called after the scroll has completed.
     * <p>
     * This callback will also be called if visible item range changes after a layout
     * calculation. In that case, dx and dy will be 0.
     *
     * @param recyclerView The RecyclerView which scrolled.
     * @param dx           The amount of horizontal scroll.
     * @param dy           The amount of vertical scroll.
     */
    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        int totalItemCount = layoutManager.getItemCount();
        int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

        if (dx > 0) {
            lastScrolledDirection = ScrollDirection.RIGHT;
        } else if (dx < 0) {
            lastScrolledDirection = ScrollDirection.LEFT;
        }

        // If we are scrolling to the right, and hit the end, call for more items
        if (dx > 0 && firstVisibleItem + 1 >= totalItemCount) {
            parentActivityRef.getNextBatchOfImages();
        }

        Log.d("SCROLLER", scrollState + "");
        View v = layoutManager.findViewByPosition(firstVisibleItem);
        double scrollPercentage = (double) v.getRight() / (double) displayWidth;
        // When idle, snap to image
        if (Math.abs(dx) < SCROLL_SENSITIVITY) {
            recyclerView.scrollToPosition(firstVisibleItem + ((lastScrolledDirection == ScrollDirection.RIGHT) ? 1 : 0));
        } else if (scrollState == RecyclerView.SCROLL_STATE_IDLE) {
            if (scrollPercentage < SNAP_TOLERANCE) {
                recyclerView.smoothScrollToPosition(firstVisibleItem + 1);
            } else {
                recyclerView.smoothScrollToPosition(firstVisibleItem);
            }
        } else {
            if (dx > 0 && scrollPercentage < SNAP_TOLERANCE) {
                recyclerView.smoothScrollToPosition(firstVisibleItem + 1);
            } else if (dx < 0 && scrollPercentage > SNAP_TOLERANCE) {
                recyclerView.smoothScrollToPosition(firstVisibleItem);
            }
        }
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);

        scrollState = newState;
    }
}
