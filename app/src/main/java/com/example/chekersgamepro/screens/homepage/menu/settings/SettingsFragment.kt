package com.example.chekersgamepro.screens.homepage.menu.settings

import android.os.Bundle
import android.view.View
import com.example.chekersgamepro.R
import com.example.chekersgamepro.screens.homepage.menu.online.BaseFragment
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.internal.functions.Functions
import kotlinx.android.synthetic.main.settings_fragment.view.*

class SettingsFragment : BaseFragment() {

    override fun getTitle() = getString(R.string.activity_home_page_settings_title_text)

    override fun getLayoutResId(): Int = R.layout.settings_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val settingsViewModel = SettingsInjector()
                .createViewModelActivity(activity!!)

        val spinnerLanguages = view.spinner_languages

        val switchCompatOnOffSound = view.switch_compat_on_off_sound

        val switchCompatOnOffChat = view.switch_compat_on_off_chat

        compositeDisposableOnDestroyed.addAll(
                settingsViewModel
                        .getSettingData()
                        .subscribe {
                            spinnerLanguages.setSelection(it.language)
                            switchCompatOnOffSound.isChecked = (it.isSound )
                            switchCompatOnOffChat.isChecked = (it.isChat)
                        },

                RxView.clicks(view.button_save_settings)
                        .distinctUntilChanged()
                        .subscribe {
                            val selectedItemLanguagePosition = spinnerLanguages.selectedItemPosition
                            val isCheckedChat = switchCompatOnOffChat.isChecked
                            val isCheckedSound = switchCompatOnOffSound.isChecked
                            settingsViewModel.onClickAddSettings(selectedItemLanguagePosition, isCheckedSound, isCheckedChat)
                        },

                settingsViewModel.isSave()
                        .subscribe (Functions.actionConsumer(this::dismissAllowingStateLoss))
        )
    }

}