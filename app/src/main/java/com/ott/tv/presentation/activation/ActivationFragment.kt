package com.ott.tv.presentation.activation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ott.tv.R
import com.ott.tv.data.api.OttApiClient
import com.ott.tv.data.api.OttApiService
import com.ott.tv.data.repository.ActivationRepository
import com.ott.tv.data.storage.AuthTokenStore
import com.ott.tv.data.storage.DeviceIdProvider
import com.ott.tv.domain.ActivationUiState
import com.ott.tv.domain.ActivationUseCase
import kotlinx.coroutines.launch

class ActivationFragment : Fragment() {

    private lateinit var viewModel: ActivationViewModel

    private lateinit var titleText: TextView
    private lateinit var subtitleText: TextView
    private lateinit var codeLabelText: TextView
    private lateinit var activationCodeText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var retryButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = requireContext()
        val apiService = OttApiClient.create<OttApiService>()
        val repository = ActivationRepository(
            apiService = apiService,
            deviceIdProvider = DeviceIdProvider(context),
            tokenStore = AuthTokenStore(context)
        )
        viewModel = ActivationViewModel(ActivationUseCase(repository))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_activation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        titleText = view.findViewById(R.id.tv_title)
        subtitleText = view.findViewById(R.id.tv_subtitle)
        codeLabelText = view.findViewById(R.id.tv_code_label)
        activationCodeText = view.findViewById(R.id.tv_activation_code)
        progressBar = view.findViewById(R.id.progress_bar)
        statusText = view.findViewById(R.id.tv_status)
        retryButton = view.findViewById(R.id.btn_retry)

        retryButton.setOnClickListener {
            startActivationFlow()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                }
            }
        }

        if (savedInstanceState == null) {
            startActivationFlow()
        }
    }

    private fun startActivationFlow() {
        retryButton.visibility = View.GONE
        viewModel.startActivation()
    }

    private fun renderState(state: ActivationUiState) {
        when (state) {
            is ActivationUiState.Loading -> {
                showProgress(state.message)
            }
            is ActivationUiState.WaitingForUser -> {
                showActivationCode(state.activationCode, state.message)
            }
            is ActivationUiState.Activated -> {
                showSuccess(state.message)
                navigateToMainScreen()
            }
            is ActivationUiState.Error -> {
                showError(state.message)
            }
        }
    }

    private fun showProgress(message: String) {
        progressBar.visibility = View.VISIBLE
        statusText.visibility = View.VISIBLE
        statusText.text = message
        activationCodeText.visibility = View.GONE
        codeLabelText.visibility = View.GONE
        retryButton.visibility = View.GONE
    }

    private fun showActivationCode(code: String, message: String) {
        progressBar.visibility = View.VISIBLE
        statusText.visibility = View.VISIBLE
        statusText.text = message
        codeLabelText.visibility = View.VISIBLE
        activationCodeText.visibility = View.VISIBLE
        activationCodeText.text = formatCode(code)
        retryButton.visibility = View.GONE
    }

    private fun showSuccess(message: String) {
        progressBar.visibility = View.GONE
        statusText.visibility = View.VISIBLE
        statusText.text = message
        retryButton.visibility = View.GONE
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        statusText.visibility = View.VISIBLE
        statusText.text = message
        retryButton.visibility = View.VISIBLE
        activationCodeText.visibility = View.GONE
        codeLabelText.visibility = View.GONE
    }

    private fun formatCode(code: String): String {
        return code.chunked(3).joinToString("-")
    }

    private fun navigateToMainScreen() {
        // TODO: launch MainActivity once implemented
    }
}
