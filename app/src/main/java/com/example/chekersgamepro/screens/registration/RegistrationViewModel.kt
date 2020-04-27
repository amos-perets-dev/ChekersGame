package com.example.chekersgamepro.screens.registration

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.chekersgamepro.R
import com.example.chekersgamepro.db.repository.RepositoryManager
import com.example.chekersgamepro.checkers.CheckersApplication
import com.example.chekersgamepro.screens.registration.validation.IValidationForm
import com.example.chekersgamepro.screens.registration.validation.ValidationFormImpl
import com.example.chekersgamepro.screens.homepage.HomePageActivity
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class RegistrationViewModel/*(private val repositoryManager : Repository, private val resources: AppResources,
                            private val validationForm: IValidationForm)*/ : ViewModel() {

    private var currentState = MutableLiveData<RegistrationStatus>()

    private val isUserAdded = MutableLiveData<Intent>()

    private val compositeDisposable = CompositeDisposable()

    private val resources = CheckersApplication.create().resources

    private val repositoryManager = RepositoryManager.create()

    private val validationForm: IValidationForm = ValidationFormImpl()

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

    private fun getTextByState(registrationStatus: RegistrationStatus): String {
        return when (registrationStatus.ordinal) {
            RegistrationStatus.CHECK_AVAILABLE.ordinal -> resources.getString(R.string.activity_registration_button_check_availability_text)
            RegistrationStatus.REGISTRATION.ordinal -> resources.getString(R.string.activity_registration_button_registration_text)
            RegistrationStatus.NOT_AVAILABLE.ordinal -> resources.getString(R.string.activity_registration_button_not_available_text)
            RegistrationStatus.EMPTY.ordinal -> resources.getString(R.string.activity_registration_button_disable_empty_text)
            else -> resources.getString(R.string.activity_registration_button_error_text)
        }
    }

    private fun getColorByState(registrationStatus: RegistrationStatus): Int {
        return when (registrationStatus.ordinal) {
            RegistrationStatus.CHECK_AVAILABLE.ordinal -> resources.getColor(R.color.activity_registration_button_check_available_color)
            RegistrationStatus.REGISTRATION.ordinal -> resources.getColor(R.color.activity_registration_button_registration_color)
            RegistrationStatus.NOT_AVAILABLE.ordinal -> resources.getColor(R.color.activity_registration_button_disable_color)
            RegistrationStatus.EMPTY.ordinal -> resources.getColor(R.color.activity_registration_button_disable_color)
            else -> resources.getColor(R.color.activity_registration_button_error_color)
        }
    }


    private fun addNewUser(userName: String): Disposable {
        return repositoryManager
                .addNewUser(userName)
                .doOnError { Log.d("TEST_GAME", "2 doOnError") }
                .doOnEvent { status, t2 ->
                    when(status.ordinal){
                        RegistrationStatus.REGISTED.ordinal -> isUserAdded.postValue(Intent(CheckersApplication.create().applicationContext, HomePageActivity::class.java))
                        RegistrationStatus.ERROR.ordinal -> currentState.postValue(RegistrationStatus.ERROR)
                        else -> currentState.postValue(RegistrationStatus.NOT_AVAILABLE)
                    }
                }
                .subscribe()
    }

    fun isUserAdded(lifecycleOwner: LifecycleOwner): Observable<Intent> =
            (getObservable(isUserAdded, lifecycleOwner) as Observable<*>)
                    .cast(Intent::class.java)

    fun registrationFormValid(lifecycleOwner: LifecycleOwner): Observable<Boolean> =
            (getObservable(currentState, lifecycleOwner) as Observable<*>)
                    .cast(RegistrationStatus::class.java)
                    .map { state ->
                        state.ordinal != RegistrationStatus.NOT_AVAILABLE.ordinal
                    }

    fun isUserNameValid(lifecycleOwner: LifecycleOwner): Observable<Boolean> =
            (getObservable(currentState, lifecycleOwner) as Observable<*>)
                    .cast(RegistrationStatus::class.java)
                    .doOnNext {state ->
                        isCanAddUser = state.ordinal == RegistrationStatus.REGISTRATION.ordinal

                    }
                    .map { state ->
                        state.ordinal == RegistrationStatus.REGISTRATION.ordinal
                                || state.ordinal == RegistrationStatus.CHECK_AVAILABLE.ordinal
                    }

    fun getCurrentStateText(lifecycleOwner: LifecycleOwner): Observable<String> =
            (getObservable(currentState, lifecycleOwner) as Observable<*>)
                    .cast(RegistrationStatus::class.java)
                    .map(this::getTextByState)

    fun getColor(lifecycleOwner: LifecycleOwner): Observable<Int> =
            (getObservable(currentState, lifecycleOwner) as Observable<*>)
                    .cast(RegistrationStatus::class.java)
                    .map(this::getColorByState)

    private fun getObservable(liveData: MutableLiveData<*>, lifecycleOwner: LifecycleOwner): Observable<Any> =
            Observable.fromPublisher(LiveDataReactiveStreams.toPublisher(lifecycleOwner, liveData))

    override fun onCleared() {
        Log.d("TEST_GAME", "RegistrationViewModel -> onCleared")
        compositeDisposable.dispose()
        super.onCleared()
    }
}