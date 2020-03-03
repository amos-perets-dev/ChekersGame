package com.example.chekersgamepro.screens.registration

import androidx.lifecycle.ViewModelProviders

class RegistrationInjector {
    companion object {
        fun createViewModel(context: RegistrationActivity): RegistrationViewModel {
            // todo: how to build view model via constructor with params
            return ViewModelProviders.of(context).get(RegistrationViewModel::class.java)
        }
    }
}