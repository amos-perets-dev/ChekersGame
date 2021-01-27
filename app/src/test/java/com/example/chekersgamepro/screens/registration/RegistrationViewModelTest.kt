package com.example.chekersgamepro.screens.registration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.chekersgamepro.db.repository.Repository
import com.example.chekersgamepro.screens.registration.validation.IValidationForm
import com.example.chekersgamepro.resources.AppResources
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mockito

class RegistrationViewModelTest {

    @get:Rule
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var repo: Repository
    private lateinit var resources: AppResources
    private lateinit var validator: IValidationForm
    private lateinit var viewModel: RegistrationViewModel

    @Before
    fun setup() {
//        repo = Mockito.mock(Repository::class.java)
//        resources = Mockito.mock(AppResources::class.java)
//        validator = Mockito.mock(IValidationForm::class.java)
//        viewModel = RegistrationViewModel(repo, resources, validator)
    }

    fun verifyNoMore() {
        Mockito.verifyNoMoreInteractions(repo, resources, validator)
    }

    @Test
    fun checkUserNameExist() {

    }

    @Test
    fun checkUserNameValidAsync_emptyUserName() {
//        val userName = ""
//
//        Mockito.`when`(validator.userNameState(userName)).thenReturn(Single.just(RegistrationStatus.EMPTY))
//        // viewModel. // todo check that in subscriber arrives right answer
//
//        viewModel.checkUserNameValidAsync(userName)
//
//        Mockito.verify(validator, Mockito.times(1)).userNameState(userName)
//
//        // val captor = ArgumentCaptor.forClass(Callback::class.java)
//        // Mockito.verify(validator, Mockito.times(1)).requestSomething(param, captor.capture())
//        // captor.value.doStuff()
//
//        verifyNoMore()
    }

    @Test
    fun isUserAdded() {
    }

    @Test
    fun registrationFormValid() {
    }

    @Test
    fun isUserNameValid() {
    }

    @Test
    fun getCurrentStateText() {
    }

    @Test
    fun getColor() {
    }

    @Test
    fun onCleared() {
    }

    interface Callback {
        fun doStuff()
    }
}