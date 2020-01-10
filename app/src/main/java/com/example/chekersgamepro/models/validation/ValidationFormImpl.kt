package com.example.chekersgamepro.models.validation

import com.example.chekersgamepro.RegistrationButtonStatus
import com.example.chekersgamepro.db.repository.RepositoryManager
import io.reactivex.Single

class ValidationFormImpl : IValidationForm{

    private val repositoryManager = RepositoryManager.create()

    override fun userNameState(userName: String): Single<RegistrationButtonStatus>{
        if (userName.isEmpty()){
            return  Single.just(RegistrationButtonStatus.EMPTY)
        }

        if (repositoryManager.isUserNameExistLocallyListInvalid(userName)){
            return  Single.just(RegistrationButtonStatus.NOT_AVAILABLE)
        }

        if (repositoryManager.isUserNameExistLocallyListValid(userName)){
            return  Single.just(RegistrationButtonStatus.REGISTRATION)
        }

        return  Single.just(RegistrationButtonStatus.CHECK_AVAILABLE)

    }

    override fun isUserCanAdd(userName: String): Single<RegistrationButtonStatus> =
            repositoryManager.isUserNameExist(userName)
                    .map {isExist ->
                        if (isExist) {
                            RegistrationButtonStatus.NOT_AVAILABLE
                        } else {
                            RegistrationButtonStatus.REGISTRATION
                        }
                    }



}