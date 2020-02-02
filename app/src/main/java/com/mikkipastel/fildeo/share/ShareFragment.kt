package com.mikkipastel.fildeo.share

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.mikkipastel.fildeo.R
import kotlinx.android.synthetic.main.fragment_share.*
import java.io.File
import android.content.Intent
import androidx.core.content.FileProvider
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.mikkipastel.fildeo.BuildConfig
import com.mikkipastel.fildeo.utils.MotionTouchListener

class ShareFragment: Fragment() {

    private lateinit var mPlayer: SimpleExoPlayer

    private lateinit var filterFilepath : String

    companion object {
        const val ARG_KEY_URI = "preview:uri"

        fun newInstance(uri: String): ShareFragment {
            val fragment = ShareFragment()
            val bundle = Bundle()
            bundle.putString(ARG_KEY_URI, uri)
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_share, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        filterFilepath = arguments!!.getString(ARG_KEY_URI)

        val uriFile = FileProvider.getUriForFile(
                context!!,
                BuildConfig.APPLICATION_ID + ".provider",
                File(filterFilepath)
        )

        btnBackground.apply {
            setOnTouchListener(MotionTouchListener())
            setOnClickListener {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "video/*"
                    putExtra(Intent.EXTRA_STREAM, uriFile)
                }
                startActivity(Intent.createChooser(shareIntent, "Share video to.."))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        initializePlayer()
    }

    override fun onPause() {
        super.onPause()
        releasePlayer()
    }

    private fun initializePlayer() {
        mPlayer = ExoPlayerFactory.newSimpleInstance(
                context,
                DefaultRenderersFactory(context),
                DefaultTrackSelector(),
                DefaultLoadControl())

        playerView.player = mPlayer
        mPlayer.playWhenReady = true

        mPlayer.prepare(ProgressiveMediaSource.Factory(DefaultDataSourceFactory(context, context?.getString(R.string.app_name)))
                .createMediaSource(Uri.fromFile(File(filterFilepath))), true, false)
    }

    private fun releasePlayer() {
        mPlayer.stop()
        mPlayer.release()
    }
}