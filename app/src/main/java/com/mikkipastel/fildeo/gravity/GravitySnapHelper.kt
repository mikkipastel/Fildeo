package com.mikkipastel.fildeo.gravity

import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class GravitySnapHelper @JvmOverloads constructor(gravity: Int, enableSnapLastItem: Boolean = false, snapListener: SnapListener? = null) : androidx.recyclerview.widget.LinearSnapHelper() {

    private val delegate: GravityDelegate = GravityDelegate(gravity, enableSnapLastItem, snapListener)

    @Throws(IllegalStateException::class)
    override fun attachToRecyclerView(recyclerView: androidx.recyclerview.widget.RecyclerView?) {
        delegate.attachToRecyclerView(recyclerView)
        super.attachToRecyclerView(recyclerView)
    }

    override fun calculateDistanceToFinalSnap(layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager,
                                              targetView: View): IntArray? {
        return delegate.calculateDistanceToFinalSnap(layoutManager, targetView)
    }

    override fun findSnapView(layoutManager: androidx.recyclerview.widget.RecyclerView.LayoutManager): View? {
        return delegate.findSnapView(layoutManager)
    }

    interface SnapListener {
        fun onSnap(position: Int)
    }
}