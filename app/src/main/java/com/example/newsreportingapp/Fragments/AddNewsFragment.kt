

package com.example.newsreportingapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.newsreportingapp.databinding.AddNewsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.*

class AddNewsFragment : Fragment(R.layout.add_news) {
    private companion object {
        const val TAG = "AddNewsFragment"
        const val MAX_IMAGE_SIZE = 100 * 1024 // 100 KB
        const val COMPRESSION_QUALITY = 80 // JPEG compression quality
        const val CAMERA_REQUEST_CODE = 1001
        const val CAMERA_PERMISSION_CODE = 2001
    }

    private var _binding: AddNewsBinding? = null
    private val binding get() = _binding!!
    private var imageUri: Uri? = null
    private var base64Image: String? = null
    private var isUploading = false
    private var bitmap: Bitmap? = null

    private val firestore = FirebaseFirestore.getInstance()

    // Permission launcher
    private val requestCameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            showError("Camera permission is required to capture photos")
        }
    }

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            imageUri = it
            displaySelectedImage(it)
            lifecycleScope.launch {
                base64Image = convertUriToBase64(it) ?: run {
                    showError("Image conversion failed")
                    binding.selectedImageView.setImageResource(R.drawable.error_img)
                    null
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = AddNewsBinding.bind(view)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            sendButton.setOnClickListener {
                if (!isUploading && validateInputs()) uploadPostToFirestore()
            }
            addImageButton.setOnClickListener { pickImageFromGallery() }
            deleteButton.setOnClickListener { resetFields() }
            capturePhotoButton.setOnClickListener { checkCameraPermissionAndOpen() }
        }
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                showError("Camera permission is required to capture photos")
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            else -> {
                requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun openCamera() {
        try {
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // Remove the resolveActivity check as it's not reliable on newer Android versions
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening camera", e)
            showError("Failed to open camera: ${e.message}")
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == CAMERA_REQUEST_CODE) {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val imageBitmap = data?.extras?.get("data") as? Bitmap
                        if (imageBitmap != null) {
                            bitmap = imageBitmap
                            displayCapturedImage(imageBitmap)
                            lifecycleScope.launch {
                                base64Image = convertBitmapToBase64(imageBitmap)
                            }
                        } else {
                            Log.e(TAG, "Received null bitmap from camera")
                            showError("Failed to capture image")
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.d(TAG, "Camera capture cancelled by user")
                    }
                    else -> {
                        Log.e(TAG, "Camera capture failed with result code: $resultCode")
                        showError("Failed to capture image")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling camera result", e)
            showError("Failed to process captured image: ${e.message}")
        }
    }

    private fun displayCapturedImage(bitmap: Bitmap) {
        binding.selectedImageView.apply {
            visibility = View.VISIBLE
            setImageBitmap(bitmap)
        }
    }

    private suspend fun convertBitmapToBase64(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        try {
            val outputStream = ByteArrayOutputStream()
            var quality = COMPRESSION_QUALITY
            var byteArray: ByteArray

            do {
                outputStream.reset()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                byteArray = outputStream.toByteArray()
                quality -= 10
            } while (byteArray.size > MAX_IMAGE_SIZE && quality > 50)

            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Bitmap conversion failed", e)
            throw e
        }
    }

    private fun validateInputs(): Boolean = when {
        binding.topicInput.text.isNullOrEmpty() -> showError("Please enter a topic").let { false }
        binding.captionInput.text.isNullOrEmpty() -> showError("Please enter a caption").let { false }
        base64Image == null -> showError("Please select a valid image").let { false }
        else -> true
    }

    private fun uploadPostToFirestore() {
        isUploading = true
        setLoadingState(true)

        lifecycleScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                val post = mapOf(
                    "postId" to UUID.randomUUID().toString(),
                    "topic" to binding.topicInput.text.toString().trim(),
                    "content" to binding.captionInput.text.toString().trim(),
                    "image" to base64Image!!,
                    "status" to "pending",
                    "reporter" to (currentUser?.displayName ?: "Anonymous"),
                    "timestamp" to System.currentTimeMillis()
                )

                withContext(Dispatchers.IO) { firestore.collection("posts").add(post) }
                handleUploadSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Upload failed", e)
                showError("Upload failed: ${e.message}")
            } finally {
                isUploading = false
                setLoadingState(false)
            }
        }
    }

    private suspend fun convertUriToBase64(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close() ?: return@withContext null

            val outputStream = ByteArrayOutputStream()
            var quality = COMPRESSION_QUALITY
            var byteArray: ByteArray

            do {
                outputStream.reset()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                byteArray = outputStream.toByteArray()
                quality -= 10
            } while (byteArray.size > MAX_IMAGE_SIZE && quality > 50)

            Base64.encodeToString(byteArray, Base64.DEFAULT)
        } catch (e: Exception) {
            Log.e(TAG, "Image conversion failed", e)
            null
        }
    }

    private fun displaySelectedImage(uri: Uri) {
        binding.selectedImageView.apply {
            visibility = View.VISIBLE
            setImageURI(uri)
        }
    }

    private fun handleUploadSuccess() {
        Toast.makeText(requireContext(), "Post uploaded successfully", Toast.LENGTH_LONG).show()
        resetFields()
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun setLoadingState(loading: Boolean) {
        binding.apply {
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            sendButton.isEnabled = !loading
            addImageButton.isEnabled = !loading
            deleteButton.isEnabled = !loading
            capturePhotoButton.isEnabled = !loading
            topicInput.isEnabled = !loading
            captionInput.isEnabled = !loading
        }
    }

    private fun pickImageFromGallery() {
        imagePickerLauncher.launch("image/*")
    }

    private fun resetFields() {
        binding.apply {
            topicInput.text?.clear()
            captionInput.text?.clear()
            selectedImageView.setImageResource(0)
            selectedImageView.visibility = View.GONE
            imageUri = null
            base64Image = null
            bitmap = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}