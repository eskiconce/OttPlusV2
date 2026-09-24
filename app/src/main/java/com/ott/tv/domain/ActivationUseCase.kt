package com.ott.tv.domain

import com.ott.tv.data.model.ActivationStatus
import com.ott.tv.data.model.ActivationStatusResponse
import com.ott.tv.data.model.RegisterDeviceResponse
import com.ott.tv.data.repository.ActivationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ActivationUseCase(private val repository: ActivationRepository) {

    operator fun invoke(deviceName: String): Flow<ActivationUiState> = flow {
        emit(ActivationUiState.Loading("Solicitando código de activación…"))

        val registerResult = repository.registerDevice(deviceName)
        if (registerResult.isFailure) {
            emit(ActivationUiState.Error(mapError(registerResult.exceptionOrNull())))
            return@flow
        }

        val registerData: RegisterDeviceResponse = registerResult.getOrNull()!!
        emit(
            ActivationUiState.WaitingForUser(
                activationCode = registerData.activationCode,
                message = "Esperando activación…"
            )
        )

        val activationResult = repository.waitForActivation(
            activationCode = registerData.activationCode
        )

        if (activationResult.isSuccess) {
            val response: ActivationStatusResponse = activationResult.getOrNull()!!
            emit(
                ActivationUiState.Activated(
                    accessToken = response.accessToken.orEmpty(),
                    message = "Dispositivo registrado correctamente"
                )
            )
        } else {
            emit(ActivationUiState.Error(mapError(activationResult.exceptionOrNull())))
        }
    }

    private fun mapError(throwable: Throwable?): String {
        return when (throwable) {
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException -> "Sin conexión a internet"
            is com.ott.tv.data.repository.ActivationExpiredException -> "El código de activación expiró"
            else -> throwable?.message ?: "Ocurrió un error inesperado"
        }
    }
}

sealed class ActivationUiState {
    data class Loading(val message: String) : ActivationUiState()
    data class WaitingForUser(val activationCode: String, val message: String) : ActivationUiState()
    data class Activated(val accessToken: String, val message: String) : ActivationUiState()
    data class Error(val message: String) : ActivationUiState()
}
