package com.example.chekersgamepro.screens.registration

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.text.Editable
import androidx.lifecycle.ViewModelProviders

import com.example.chekersgamepro.R
import com.example.chekersgamepro.util.keybord_util.KeyboardUtil
import com.example.chekersgamepro.util.keybord_util.KeyboardUtilData
import com.google.common.base.Functions
import com.jakewharton.rxbinding2.view.RxView
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_registration.*

class RegistrationActivity : AppCompatActivity() {

    private val registrationViewModel by lazy {
        ViewModelProviders.of(this).get(RegistrationViewModel::class.java)
    }

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        compositeDisposable.addAll(

                // Get the text for the registration button
                registrationViewModel.getCurrentStateText(this)
                        .skip(1)
                        .subscribe(button_check_validation_registration::setText),

                // Get the color for the registration button
                registrationViewModel.getColor(this)
                        .skip(1)
                        .subscribe(button_check_validation_registration::setBackgroundColor),

                // Get the registration form state valid / invalid
                registrationViewModel.registrationFormValid(this)
                        .skip(1)
                        .subscribe(this::showRegistrationButtonErrorText),

                // Get if the user name valid or not
                // and change the button by the validation state
                registrationViewModel.isUserNameValid(this)
                        .doOnNext(button_check_validation_registration::setEnabled)
                        .doOnNext(button_check_validation_registration::setClickable)
                        .subscribe(),

                registrationViewModel.isUserAdded(this)
                        .subscribe(this::startActivity),

                // Set the user name from the edit text
                RxTextView
                        .textChanges(edit_text_user_name_registration)
                        .map(CharSequence::toString)
                        .subscribe(registrationViewModel::checkUserNameValidAsync),

                // Set the click on the registration button
                RxView
                        .clicks(button_check_validation_registration)
                        .map { edit_text_user_name_registration.text }
                        .map(Editable::toString)
                        .subscribe(registrationViewModel::checkUserNameExist),

                KeyboardUtil(registration_activity)
                        .getObservableKeyboardOpen()
                        .subscribe(this::changeTranslationRegistrationButton))


    }

    override fun startActivity(intent: Intent) {
        super.startActivity(intent)
        finish()
    }

    /**
     * Change the translation registration button when the keyboard open or close
     *
     * @param keyboardUtilData data of the keyboard {@link KeyboardUtilData}
     */
    private fun changeTranslationRegistrationButton(keyboardUtilData: KeyboardUtilData) {
        button_check_validation_registration.animate()
                .translationY(if (keyboardUtilData.isOpen) -(keyboardUtilData.keybordHeight.toFloat()) else 0f)
                .start()
    }

    /**
     * Change the registration button to enable or disable
     *
     * @param isUserNameValid {@link Boolean#TRUE} - the user name is valid(change to enable)
     *                        {@link Boolean#FALSE} - the user name is invalid(change to disable)
     */
    private fun changeRegistrationButtonEnable(isUserNameValid: Boolean) {
        button_check_validation_registration.isEnabled = isUserNameValid
        button_check_validation_registration.isClickable = isUserNameValid
    }

    /**
     * Show the error message when the user name exist
     *
     * @param isRegistrationFromValid {@link Boolean#TRUE} - the form is valid
     *                                {@link Boolean#FALSE} - the form is invalid
     */
    private fun showRegistrationButtonErrorText(isRegistrationFromValid: Boolean) {
        if (!isRegistrationFromValid) {
            button_check_validation_registration
                    .animate()
                    .alpha(1f)
                    .withStartAction {
                        edit_text_user_name_registration.error = resources.getString(R.string.activity_registration_user_name_helper_text)
                    }
                    .setDuration(700)
                    .start()
        }
    }
}
