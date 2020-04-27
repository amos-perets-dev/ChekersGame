package com.example.chekersgamepro.screens.registration

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.example.chekersgamepro.util.TouchListener
import com.example.chekersgamepro.util.keybord_util.KeyboardUtil
import com.example.chekersgamepro.util.keybord_util.KeyboardUtilData
import com.jakewharton.rxbinding2.widget.RxTextView
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_registration.*


class RegistrationActivity : AppCompatActivity() {

    private lateinit var registrationViewModel : RegistrationViewModel

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.chekersgamepro.R.layout.activity_registration)
        registrationViewModel = ViewModelProviders.of(this).get<RegistrationViewModel>(RegistrationViewModel::class.java)

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
                        // Change the registration button to enable or disable
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

                KeyboardUtil(registration_activity)
                        .getObservableKeyboardOpen()
                        .subscribe(this::changeTranslationRegistrationButton)
        )

        // Set the click on the registration button
        button_check_validation_registration
                .setOnTouchListener(TouchListener(View.OnClickListener { registrationViewModel.checkUserNameExist(edit_text_user_name_registration.text.toString()) }
                        , 0.6f))


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
                .withLayer()
                .translationY(if (keyboardUtilData.isOpen) -(keyboardUtilData.keybordHeight.toFloat()) else 0f)
                .start()
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
                    .withLayer()
                    .alpha(1f)
                    .withStartAction {
                        edit_text_user_name_registration.error = resources.getString(com.example.chekersgamepro.R.string.activity_registration_user_name_helper_text)
                    }
                    .setDuration(700)
                    .start()
        }
    }
}
