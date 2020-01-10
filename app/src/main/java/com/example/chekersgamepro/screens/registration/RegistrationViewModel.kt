package com.example.chekersgamepro.screens.registration

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chekersgamepro.R
import com.example.chekersgamepro.RegistrationButtonStatus
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.util.CheckersApplication
import com.example.chekersgamepro.models.validation.IValidationForm
import com.example.chekersgamepro.models.validation.ValidationFormImpl
import com.example.chekersgamepro.screens.homepage.HomePageActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class RegistrationViewModel : ViewModel() {

    private val validationForm: IValidationForm = ValidationFormImpl()

    private val currentState = MutableLiveData<RegistrationButtonStatus>()

    private val isUserAdded = MutableLiveData<Intent>()

    private val compositeDisposable = CompositeDisposable()

    private val context = CheckersApplication.create()

    private var isCanAddUser = false


    fun checkUserNameExist(userName: String) {

        if (isCanAddUser) {
            compositeDisposable.add(addNewUser(userName))
        } else {
            compositeDisposable.add(validationForm.isUserCanAdd(userName)
                    .subscribe(currentState::postValue))
        }
    }

    fun checkUserNameValidAsync(userName: String) {
        compositeDisposable.add(validationForm.userNameState(userName)
                .subscribe(currentState::postValue))
    }

    private fun getTextByState(registrationButtonStatus: RegistrationButtonStatus): String {
        return when (registrationButtonStatus.ordinal) {
            RegistrationButtonStatus.CHECK_AVAILABLE.ordinal -> context.resources.getString(R.string.activity_registration_button_check_availability_text)
            RegistrationButtonStatus.REGISTRATION.ordinal -> context.resources.getString(R.string.activity_registration_button_registration_text)
            RegistrationButtonStatus.NOT_AVAILABLE.ordinal -> context.resources.getString(R.string.activity_registration_button_not_available_text)
            RegistrationButtonStatus.EMPTY.ordinal -> context.resources.getString(R.string.activity_registration_button_disable_empty_text)
            else -> context.resources.getString(R.string.activity_registration_button_error_text)
        }
    }

    private fun getColorByState(registrationButtonStatus: RegistrationButtonStatus): Int {
        return when (registrationButtonStatus.ordinal) {
            RegistrationButtonStatus.CHECK_AVAILABLE.ordinal -> context.resources.getColor(R.color.activity_registration_button_check_available_color)
            RegistrationButtonStatus.REGISTRATION.ordinal -> context.resources.getColor(R.color.activity_registration_button_registration_color)
            RegistrationButtonStatus.NOT_AVAILABLE.ordinal -> context.resources.getColor(R.color.activity_registration_button_disable_color)
            RegistrationButtonStatus.EMPTY.ordinal -> context.resources.getColor(R.color.activity_registration_button_disable_color)
            else -> context.resources.getColor(R.color.activity_registration_button_error_color)
        }
    }


    private fun addNewUser(userName: String): Disposable {
        return RepositoryManager.create()
                .addNewUser(userName)
                .doOnError {
                    Log.d("TEST_GAME", "2 doOnError")
                }
                .doOnEvent { isAdded, t2 ->
                    Log.d("TEST_GAME", "2 doOnEvent isAdded: $isAdded")
                    if (isAdded) {
                        isUserAdded.postValue(Intent(context, HomePageActivity::class.java))
                    } else {
                        currentState.postValue(RegistrationButtonStatus.ERROR)
                    }
                }
                .subscribe()
    }

    fun isUserAdded(lifecycleOwner: LifecycleOwner): Observable<Intent> =
            getObservable(isUserAdded, lifecycleOwner) as Observable<Intent>

    fun registrationFormValid(lifecycleOwner: LifecycleOwner): Observable<Boolean> =
            (getObservable(currentState, lifecycleOwner) as Observable<RegistrationButtonStatus>)
                    .map { state ->
                        state.ordinal != RegistrationButtonStatus.NOT_AVAILABLE.ordinal
                    }

    fun isUserNameValid(lifecycleOwner: LifecycleOwner): Observable<Boolean> =
            (getObservable(currentState, lifecycleOwner) as Observable<RegistrationButtonStatus>)
                    .doOnNext {state ->
                        isCanAddUser = state.ordinal == RegistrationButtonStatus.REGISTRATION.ordinal

                    }
                    .map { state ->
                        state.ordinal == RegistrationButtonStatus.REGISTRATION.ordinal
                                || state.ordinal == RegistrationButtonStatus.CHECK_AVAILABLE.ordinal
                    }

    fun getCurrentStateText(lifecycleOwner: LifecycleOwner): Observable<String> =
            (getObservable(currentState, lifecycleOwner) as Observable<RegistrationButtonStatus>)
                    .map(this::getTextByState)

    fun getColor(lifecycleOwner: LifecycleOwner): Observable<Int> =
            (getObservable(currentState, lifecycleOwner) as Observable<RegistrationButtonStatus>)
                    .map(this::getColorByState)

    private fun getObservable(liveData: MutableLiveData<*>, lifecycleOwner: LifecycleOwner): Observable<Any> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, liveData))

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}