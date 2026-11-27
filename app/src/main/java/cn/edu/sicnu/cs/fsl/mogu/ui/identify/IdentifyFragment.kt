package cn.edu.sicnu.cs.fsl.mogu.ui.identify

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import cn.edu.sicnu.cs.fsl.mogu.R
import cn.edu.sicnu.cs.fsl.mogu.ServiceLocator
import cn.edu.sicnu.cs.fsl.mogu.data.model.IdentifyResult
import cn.edu.sicnu.cs.fsl.mogu.databinding.FragmentIdentifyBinding
import cn.edu.sicnu.cs.fsl.mogu.ui.auth.LoginActivity
import cn.edu.sicnu.cs.fsl.mogu.ui.common.PredictionBinder
import cn.edu.sicnu.cs.fsl.mogu.ui.wiki.MushroomDetailActivity
import cn.edu.sicnu.cs.fsl.mogu.util.ImageLoader
import cn.edu.sicnu.cs.fsl.mogu.util.ImageStorage
import cn.edu.sicnu.cs.fsl.mogu.util.Labels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class IdentifyFragment : Fragment() {
    private var _binding: FragmentIdentifyBinding? = null
    private val binding get() = requireNotNull(_binding)
    private val viewModel: IdentifyViewModel by viewModels()

    private var currentFile: File? = null
    private var pendingCameraFile: File? = null
    private var shownRecordId = 0L
    private var pendingIdentify = false

    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && pendingIdentify) {
            currentFile?.let { viewModel.identify(it) }
        }
        pendingIdentify = false
    }

    private val pickVisualMedia = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> uri?.let(::handleGalleryUri) }

    private val takePicture = registerForActivityResult(
        ActivityResultContracts.TakePicture(),
    ) { success ->
        if (success) handleCameraSuccess()
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            launchCamera()
        } else {
            toast(getString(R.string.identify_camera_permission))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentIdentifyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnGallery.setOnClickListener {
            pickVisualMedia.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
            )
        }
        binding.btnCamera.setOnClickListener { startCamera() }
        binding.btnIdentify.setOnClickListener { startIdentify() }
        binding.btnIdentifyLogin.setOnClickListener {
            loginLauncher.launch(Intent(requireContext(), LoginActivity::class.java))
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                ServiceLocator.session.isLoggedInFlow.collect { loggedIn ->
                    binding.guestBanner.isVisible = !loggedIn
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    binding.progress.isVisible = state.loading
                    binding.btnIdentify.isEnabled = currentFile != null && !state.loading
                    binding.btnIdentify.text = getString(
                        if (state.loading) R.string.identify_recognizing else R.string.identify_start,
                    )
                    state.result?.let { result ->
                        if (result.recordId != shownRecordId) {
                            shownRecordId = result.recordId
                            renderResult(result)
                            result.fallbackReason?.let { toast(it) }
                        }
                    }
                    state.error?.let { message ->
                        toast(message)
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun handleGalleryUri(uri: Uri) {
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) { ImageStorage.copyToInternal(requireContext(), uri) }
            }.onSuccess { file -> showImage(file) }
                .onFailure { toast(it.message ?: "读取图片失败") }
        }
    }

    private fun startCamera() {
        val granted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            launchCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    private fun launchCamera() {
        val file = ImageStorage.createCameraFile(requireContext())
        pendingCameraFile = file
        takePicture.launch(ImageStorage.uriForFile(requireContext(), file))
    }

    private fun handleCameraSuccess() {
        val source = pendingCameraFile ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            runCatching {
                withContext(Dispatchers.IO) {
                    ImageStorage.copyToInternal(
                        requireContext(),
                        ImageStorage.uriForFile(requireContext(), source),
                    )
                }
            }.onSuccess { file -> showImage(file) }
                .onFailure { toast(it.message ?: "读取拍照结果失败") }
            pendingCameraFile = null
        }
    }

    private fun showImage(file: File) {
        currentFile = file
        shownRecordId = 0
        binding.ivPreview.isVisible = true
        binding.tvPreviewHint.isVisible = false
        binding.cardResult.isVisible = false
        binding.btnIdentify.isEnabled = true
        ImageLoader.load(binding.ivPreview, file.absolutePath)
    }

    private fun startIdentify() {
        val file = currentFile
        if (file == null) {
            toast(getString(R.string.identify_empty_image))
            return
        }
        if (!ServiceLocator.session.isLoggedIn) {
            pendingIdentify = true
            loginLauncher.launch(Intent(requireContext(), LoginActivity::class.java))
            return
        }
        viewModel.identify(file)
    }

    private fun renderResult(result: IdentifyResult) {
        binding.cardResult.isVisible = true
        binding.tvResultName.text = result.predictedName
        Labels.applyEdibleTag(binding.tvResultTag, result.edibleType)
        binding.tvResultConfidence.text =
            getString(R.string.identify_confidence) + "：" +
                "${Labels.confidencePercent(result.confidence)}%"
        binding.tvResultSource.text = "识别来源：" +
            if (result.source == "deepseek") getString(R.string.identify_source_deepseek)
            else getString(R.string.identify_source_mock)
        PredictionBinder.render(binding.containerTop3, result.top3) { prediction ->
            prediction.mushroomId?.let { id ->
                startActivity(
                    Intent(requireContext(), MushroomDetailActivity::class.java)
                        .putExtra(MushroomDetailActivity.EXTRA_MUSHROOM_ID, id),
                )
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
