package com.example.chekersgamepro.screens.homepage.menu.computer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import com.example.chekersgamepro.R
import com.example.chekersgamepro.screens.homepage.menu.ComputerGameInjector
import com.example.chekersgamepro.screens.homepage.menu.online.BaseFragment
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.computer_game_fragment.*


class ComputerGameFragment : BaseFragment() {

    override fun getTitle() = getString(R.string.activity_home_page_computer_game_title_text)

    override fun getLayoutResId(): Int = R.layout.computer_game_fragment

    override fun getActionOkButtonText() = getString(R.string.activity_home_page_computer_game_button_start_title_text)

    override fun getActionOkButtonVisibility() = View.VISIBLE


    private val compositeDisposable = CompositeDisposable()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val computerGameViewModel = ComputerGameInjector().createViewModelActivity(requireActivity())

        val lottieFileLevel = lottie_file_level
        lottieFileLevel.setAnimation("level_up_speed.json")

        lottieFileLevel.progress = 25F
        lottieFileLevel.setMaxFrame(25)
        lottieFileLevel.setMinFrame(1)

        seek_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(p0: SeekBar?, progress: Int, p2: Boolean) {
                lottieFileLevel.frame = 25 - progress
                text_view_level_title.text = computerGameViewModel.getTextLevel(progress)

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })

        getOnClickActionOk()
                ?.flatMap {
                    computerGameViewModel.onClickComputerGame(text_view_level_title.text)
                }
                ?.subscribe(this::startComputerGame, Throwable::printStackTrace)?.let {
                    compositeDisposable.add(
                            it
                    )
                }
    }

    private fun startComputerGame(intent: Intent) {
        startActivityForResult(intent, 55)
    }

    override fun onDestroyView() {
        compositeDisposable.dispose()
        super.onDestroyView()
    }
}