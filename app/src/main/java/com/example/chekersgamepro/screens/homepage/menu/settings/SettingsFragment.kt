package com.example.chekersgamepro.screens.homepage.menu.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chekersgamepro.R
import com.example.chekersgamepro.screens.homepage.menu.online.BaseFragment
import com.example.chekersgamepro.screens.homepage.menu.settings.model.ILanguageItem
import kotlinx.android.synthetic.main.language_item.view.*
import kotlinx.android.synthetic.main.settings_fragment.view.*

class SettingsFragment : BaseFragment() {

    override fun getTitle() = getString(R.string.activity_home_page_settings_title_text)
    override fun getActionOkButtonText() = getString(R.string.activity_home_page_menu_settings_save_text)

    override fun getActionOkButtonVisibility() = View.VISIBLE

    override fun getLayoutResId(): Int = R.layout.settings_fragment

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val settingsViewModel = SettingsInjector()
                .createViewModelActivity(requireActivity())

        val languageItemContainer = view.language_item_container
        val textViewLanguage = languageItemContainer.text_view_language
        val flagIcon = languageItemContainer.flag_icon
        val arrowIcon = view.arrow_icon


        val checkBoxSound = view.check_box_sound

        val checkBoxChat = view.check_box_chat
        val recyclerViewLanguages = view.recycler_view_languages

        val languagesAdapter = settingsViewModel.getLanguagesAdapter()
        recyclerViewLanguages.layoutManager = LinearLayoutManager(activity)
        recyclerViewLanguages.adapter = languagesAdapter

        var isOpen = false

        languageItemContainer.setOnClickListener {

            recyclerViewLanguages.animate()
                    .translationY(if (isOpen) (-(recyclerViewLanguages.measuredHeight + (languageItemContainer.measuredHeight * 1.15))).toFloat() else 0F)
                    .setDuration(300)
                    .start()

            arrowIcon
                    .animate()
                    .rotation(if (isOpen) 0F else 180F )
                    .setDuration(200)
                    .start()

            val visibility = if (isOpen) View.VISIBLE else View.GONE
            checkBoxSound.visibility = visibility
            checkBoxChat.visibility = visibility

            isOpen = !isOpen

        }


        compositeDisposableOnDestroyed.addAll(
                settingsViewModel
                        .getSettingData()
                        .subscribe {

                            val isSoundChecked = it.isSound
                            checkBoxSound.isChecked = isSoundChecked
                            if (isSoundChecked) {
                                checkBoxSound.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_volume_on, 0)
                            } else{
                                checkBoxSound.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_volume_off, 0)
                            }
                            checkBoxChat.isChecked = isSoundChecked


                            val isChatChecked = it.isChat
                            checkBoxChat.isChecked = isChatChecked
                            if (isChatChecked) {
                                checkBoxChat.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_chat_enable, 0)
                            } else{
                                checkBoxChat.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_chat_disable, 0)
                            }
                            checkBoxChat.isChecked = isChatChecked
                        },


                settingsViewModel.isSave()
                        .subscribe {
                            dismissAllowingStateLoss()
                        },

                getOnClickActionOk()
                        ?.flatMapObservable { settingsViewModel.getSelectedItem() }
                        ?.map(ILanguageItem::getPosition)
                        ?.subscribe { selectedItem ->
                            Log.d("TEST_GAME", "SettingsFragment getOnClickActionOk subscribe")

                            val isCheckedChat = checkBoxChat.isChecked
                            val isCheckedSound = checkBoxSound.isChecked
                            settingsViewModel.onClickAddSettings(selectedItem, isCheckedSound, isCheckedChat)
                        },

                settingsViewModel.getSelectedItem()
                        .subscribe { model ->
                            textViewLanguage.text = model.getName()
                            flagIcon.setBackgroundResource(model.getIcon())
                        }
        )

        checkBoxSound.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                compoundButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_volume_on, 0)
            } else{
                compoundButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_volume_off, 0)
            }
        }

        checkBoxChat.setOnCheckedChangeListener { compoundButton, isChecked ->
            if (isChecked) {
                compoundButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_chat_enable, 0)
            } else{
                compoundButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_chat_disable, 0)
            }
        }
    }

}