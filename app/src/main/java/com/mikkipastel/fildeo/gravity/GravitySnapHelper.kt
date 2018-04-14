package com.mikkipastel.fildeo.gravity

import android.support.v7.widget.LinearSnapHelper
import android.support.v7.widget.RecyclerView
import android.view.View

class GravitySnapHelper @JvmOverloads constructor(gravity: Int, enableSnapLastItem: Boolean = false, snapListener: SnapListener? = null) : LinearSnapHelper() {

    private val delegate: GravityDelegate = GravityDelegate(gravity, enableSnapLastItem, snapListener)

    @Throws(IllegalStateException::class)
    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        delegate.attachToRecyclerView(recyclerView)
        super.attachToRecyclerView(recyclerView)
    }

    override fun calculateDistanceToFinalSnap(layoutManager: RecyclerView.LayoutManager,
                                              targetView: View): IntArray? {
        return delegate.calculateDistanceToFinalSnap(layoutManager, targetView)
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        return delegate.findSnapView(layoutManager)
    }

    interface SnapListener {
        fun onSnap(position: Int)
    }
}