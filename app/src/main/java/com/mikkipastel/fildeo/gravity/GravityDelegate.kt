package com.mikkipastel.fildeo.gravity

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import android.view.Gravity
import android.view.View

internal class GravityDelegate @JvmOverloads constructor(private val gravity: Int, private var snapLastItem: Boolean = false,
                                                         private val listener: GravitySnapHelper.SnapListener? = null) {

    private var verticalHelper: OrientationHelper? = null
    private var horizontalHelper: OrientationHelper? = null
    private var isRtlHorizontal: Boolean = false
    private var snapping: Boolean = false

    private var mWidthFactor = 0.95f

    private val mScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (newState == RecyclerView.SCROLL_STATE_SETTLING) {
                snapping = false
            }
            if (newState == RecyclerView.SCROLL_STATE_IDLE && snapping && listener != null) {
                val position = getSnappedPosition(recyclerView)
                if (position != RecyclerView.NO_POSITION) {
                    listener.onSnap(position)
                }
                snapping = false
            }
        }
    }

    init {
        if (gravity != Gravity.START && gravity != Gravity.END
                && gravity != Gravity.BOTTOM && gravity != Gravity.TOP) {
            throw IllegalArgumentException("Invalid gravity value. Use START " + "| END | BOTTOM | TOP constants")
        }
    }

    fun attachToRecyclerView(recyclerView: RecyclerView?) {
        if (recyclerView != null) {
            recyclerView.onFlingListener = null
            if ((gravity == Gravity.START || gravity == Gravity.END)) {
                isRtlHorizontal = recyclerView.context.resources.configuration
                        .layoutDirection == View.LAYOUT_DIRECTION_RTL
            }
            if (listener != null) {
                recyclerView.addOnScrollListener(mScrollListener)
            }
        }
    }

    fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager,
                                     targetView: View): IntArray {
        val out = IntArray(2)

        if (layoutManager.canScrollHorizontally()) {
            if (gravity == Gravity.START) {
                out[0] = distanceToStart(targetView, getHorizontalHelper(layoutManager), false)
            } else { // END
                out[0] = distanceToEnd(targetView, getHorizontalHelper(layoutManager), false)
            }
        } else {
            out[0] = 0
        }

        if (layoutManager.canScrollVertically()) {
            if (gravity == Gravity.TOP) {
                out[1] = distanceToStart(targetView, getVerticalHelper(layoutManager), false)
            } else { // BOTTOM
                out[1] = distanceToEnd(targetView, getVerticalHelper(layoutManager), false)
            }
        } else {
            out[1] = 0
        }

        return out
    }

    fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        var snapView: View? = null
        if (layoutManager is LinearLayoutManager) {
            when (gravity) {
                Gravity.START -> snapView = getHorizontalHelper(layoutManager)?.let { findStartView(layoutManager, it) }
                Gravity.END -> snapView = getHorizontalHelper(layoutManager)?.let { findEndView(layoutManager, it) }
                Gravity.TOP -> snapView = getVerticalHelper(layoutManager)?.let { findStartView(layoutManager, it) }
                Gravity.BOTTOM -> snapView = getVerticalHelper(layoutManager)?.let { findEndView(layoutManager, it) }
            }
        }
        snapping = snapView != null
        return snapView
    }

    private fun distanceToStart(targetView: View, helper: OrientationHelper?, fromEnd: Boolean): Int {
        return if (isRtlHorizontal && !fromEnd) {
            distanceToEnd(targetView, helper, true)
        } else helper!!.getDecoratedStart(targetView) - helper.startAfterPadding

    }

    private fun distanceToEnd(targetView: View, helper: OrientationHelper?, fromStart: Boolean): Int {
        return if (isRtlHorizontal && !fromStart) {
            distanceToStart(targetView, helper, true)
        } else helper!!.getDecoratedEnd(targetView) - helper.endAfterPadding

    }

    /**
     * Returns the first view that we should snap to.
     *
     * @param layoutManager the recyclerview's layout manager
     * @param helper        orientation helper to calculate view sizes
     * @return the first view in the LayoutManager to snap to
     */
    private fun findStartView(layoutManager: RecyclerView.LayoutManager,
                              helper: OrientationHelper): View? {

        if (layoutManager is LinearLayoutManager) {
            val firstChild = layoutManager.findFirstVisibleItemPosition()
            var offset = 1

            if (layoutManager is GridLayoutManager) {
                offset += layoutManager.spanCount - 1
            }

            if (firstChild == RecyclerView.NO_POSITION) {
                return null
            }

            val child = layoutManager.findViewByPosition(firstChild)

            val visibleWidth: Float

            // We should return the child if it's visible width
            // is greater than 0.5 of it's total width.
            // In a RTL configuration, we need to check the start point and in LTR the end point
            if (isRtlHorizontal) {
                visibleWidth = (helper.totalSpace - helper.getDecoratedStart(child)).toFloat() / helper.getDecoratedMeasurement(child)
            } else {
                visibleWidth = helper.getDecoratedEnd(child).toFloat() / helper.getDecoratedMeasurement(child)
            }

            // If we're at the end of the list, we shouldn't snap
            // to avoid having the last item not completely visible.
            val endOfList = layoutManager
                    .findLastCompletelyVisibleItemPosition() == layoutManager.getItemCount() - 1

            return if (visibleWidth > mWidthFactor && !endOfList) {
                child
            } else if (snapLastItem && endOfList) {
                child
            } else if (endOfList) {
                null
            } else {
                // If the child wasn't returned, we need to return
                // the next view close to the start.
                layoutManager.findViewByPosition(firstChild + offset)
            }
        }

        return null
    }

    private fun findEndView(layoutManager: RecyclerView.LayoutManager,
                            helper: OrientationHelper): View? {

        if (layoutManager is LinearLayoutManager) {
            val lastChild = layoutManager.findLastVisibleItemPosition()
            var offset = 1

            if (layoutManager is GridLayoutManager) {
                offset += layoutManager.spanCount - 1
            }

            if (lastChild == RecyclerView.NO_POSITION) {
                return null
            }

            val child = layoutManager.findViewByPosition(lastChild)

            val visibleWidth: Float

            if (isRtlHorizontal) {
                visibleWidth = helper.getDecoratedEnd(child).toFloat() / helper.getDecoratedMeasurement(child)
            } else {
                visibleWidth = (helper.totalSpace - helper.getDecoratedStart(child)).toFloat() / helper.getDecoratedMeasurement(child)
            }

            // If we're at the start of the list, we shouldn't snap
            // to avoid having the first item not completely visible.
            val startOfList = layoutManager
                    .findFirstCompletelyVisibleItemPosition() == 0

            return if (visibleWidth > mWidthFactor && !startOfList) {
                child
            } else if (snapLastItem && startOfList) {
                child
            } else if (startOfList) {
                null
            } else {
                // If the child wasn't returned, we need to return the previous view
                layoutManager.findViewByPosition(lastChild - offset)
            }
        }
        return null
    }

    fun getSnappedPosition(recyclerView: RecyclerView?): Int {
        val layoutManager = recyclerView!!.layoutManager

        if (layoutManager is LinearLayoutManager) {
            if (gravity == Gravity.START || gravity == Gravity.TOP) {
                return layoutManager.findFirstCompletelyVisibleItemPosition()
            } else if (gravity == Gravity.END || gravity == Gravity.BOTTOM) {
                return layoutManager.findLastCompletelyVisibleItemPosition()
            }
        }

        return RecyclerView.NO_POSITION
    }

    private fun getVerticalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper? {
        if (verticalHelper == null) {
            verticalHelper = OrientationHelper.createVerticalHelper(layoutManager)
        }
        return verticalHelper
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper? {
        if (horizontalHelper == null) {
            horizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return horizontalHelper
    }
}