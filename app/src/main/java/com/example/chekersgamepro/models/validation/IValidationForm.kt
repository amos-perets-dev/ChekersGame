package com.example.chekersgamepro.models.validation

import com.example.chekersgamepro.RegistrationButtonStatus
import io.reactivex.Single

interface IValidationForm {

    fun isUserCanAdd(userName: String) : Single<RegistrationButtonStatus>

    fun userNameState(userName: String) : Single<RegistrationButtonStatus>

}