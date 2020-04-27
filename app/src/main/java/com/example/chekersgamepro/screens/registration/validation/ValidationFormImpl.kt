package com.example.chekersgamepro.screens.registration.validation

import com.example.chekersgamepro.screens.registration.RegistrationStatus
import com.example.chekersgamepro.db.repository.RepositoryManager
import io.reactivex.Single

class ValidationFormImpl : IValidationForm{

    private val repositoryManager = RepositoryManager.create()

    override fun userNameState(userName: String): Single<RegistrationStatus>{
        if (userName.isEmpty()){
            return  Single.just(RegistrationStatus.EMPTY)
        }

        if (repositoryManager.isUserNameExistLocallyListInvalid(userName)){
            return  Single.just(RegistrationStatus.NOT_AVAILABLE)
        }

        if (repositoryManager.isUserNameExistLocallyListValid(userName)){
            return  Single.just(RegistrationStatus.REGISTRATION)
        }

        return  Single.just(RegistrationStatus.CHECK_AVAILABLE)

    }

    override fun isUserCanAdd(userName: String): Single<RegistrationStatus> =
            repositoryManager.isUserNameExist(userName)
                    .map {isExist ->
                        if (isExist) {
                            RegistrationStatus.NOT_AVAILABLE
                        } else {
                            RegistrationStatus.REGISTRATION
                        }
                    }



}