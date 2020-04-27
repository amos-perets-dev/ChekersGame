package com.example.chekersgamepro.screens.registration.validation

import com.example.chekersgamepro.screens.registration.RegistrationStatus
import io.reactivex.Single

interface IValidationForm {

    fun isUserCanAdd(userName: String) : Single<RegistrationStatus>

    fun userNameState(userName: String) : Single<RegistrationStatus>

}