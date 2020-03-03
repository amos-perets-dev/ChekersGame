package com.example.chekersgamepro.models.validation

import com.example.chekersgamepro.RegistrationStatus
import io.reactivex.Single

interface IValidationForm {

    fun isUserCanAdd(userName: String) : Single<RegistrationStatus>

    fun userNameState(userName: String) : Single<RegistrationStatus>

}