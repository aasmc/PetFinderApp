package ru.aasmc.petfinderapp.report.presentation

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ru.aasmc.petfinderapp.databinding.FragmentReportDetailBinding
import java.io.File
import java.io.RandomAccessFile
import java.net.URL
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import javax.net.ssl.HttpsURLConnection

class ReportDetailFragment : Fragment() {

    companion object {
        private const val API_URL = "https://example.com/?send_report"
        private const val REPORT_APP_ID = 46341L
        private const val REPORT_PROVIDER_ID = 46341L
        private const val REPORT_SESSION_KEY = "session_key_in_next_chapter"
    }

    object ReportTracker {
        var reportNumber = AtomicInteger()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                selectImageFromGallery()
            }
        }

    private val selectImageFromGalleryResult =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            getFileName(uri)
        }

    @Volatile
    private var isSendingReport = false

    private var _binding: FragmentReportDetailBinding? = null
    private val binding: FragmentReportDetailBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportDetailBinding.inflate(inflater, container, false)
        binding.sendButton.setOnClickListener {
            sendReportPressed()
        }

        binding.uploadPhotoButton.setOnClickListener {
            uploadPhotoPressed()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    override fun onPause() {
        clearCaches()
        super.onPause()
    }

    private fun clearCaches() {
        context?.cacheDir?.deleteRecursively()
        context?.externalCacheDir?.deleteRecursively()
    }

    private fun setupUI() {
        binding.detailsEdtxtview.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.detailsEdtxtview.setRawInputType(InputType.TYPE_CLASS_TEXT)
    }

    private fun sendReportPressed() {
        if (!isSendingReport) {
            isSendingReport = true

            // 1. Save report
            var reportString = binding.categoryEdtxtview.text.toString()
            reportString += " : "
            reportString += binding.detailsEdtxtview.text.toString()
            val reportID = UUID.randomUUID().toString()

            context?.let { theContext ->
                //TODO: Replace below for encrypting the file
                val file = File(theContext.filesDir?.absolutePath, "$reportID.txt")
                file.bufferedWriter().use {
                    it.write(reportString)
                }
            }
            //TODO: Test your custom encryption here
            //testCustomEncryption(reportString)

            ReportTracker.reportNumber.incrementAndGet()

            // 2. Send report
            val postParameters = mapOf(
                "application_id" to REPORT_APP_ID * REPORT_PROVIDER_ID,
                "report_id" to reportID,
                "report" to reportString
            )
            if (postParameters.isNotEmpty()) {
                // send
                val connection = URL(API_URL).openConnection() as HttpsURLConnection
                // ...
            }

            isSendingReport = false
            context?.let {
                val report = "Report: ${ReportTracker.reportNumber.get()}"
                val toast =
                    Toast.makeText(
                        it,
                        "Thank you for your report. $report",
                        Toast.LENGTH_LONG
                    )
                toast.show()
            }
            val imm =
                activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view?.windowToken, 0)
        }
    }

    private fun testCustomEncryption(reportString: String) {

    }


    private fun uploadPhotoPressed() {
        context?.let {
            if (ContextCompat.checkSelfPermission(
                    it, READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // request permission, if we have none
                requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
            } else {
                // we have permission, select image
                selectImageFromGallery()
            }
        }
    }

    private fun selectImageFromGallery() =
        selectImageFromGalleryResult.launch("image/*")

    private fun getFileName(selectedImageUri: Uri?) {
        selectedImageUri?.let { selectedImage ->
            val isValid = isValidJPEGAtPath(selectedImage)
            if (isValid) {
                val fileNameColumn = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
                val nameCursor = activity?.contentResolver?.query(selectedImage, fileNameColumn,
                null, null, null)
                nameCursor?.moveToFirst()
                val nameIndex = nameCursor?.getColumnIndex(fileNameColumn[0])
                var filename = ""
                nameIndex?.let {
                    filename = nameCursor.getString(it)
                }

                binding.uploadStatusTextview.text = filename
            } else {
                val toast = Toast.makeText(context, "Please choose a JPEG image", Toast.LENGTH_LONG)
                toast.show()
            }
        }
    }

    private fun isValidJPEGAtPath(selectedImage: Uri): Boolean {
        var success = false
        val file = File(context?.cacheDir, "temp.jpg")
        val inputStream = activity?.contentResolver?.openInputStream(selectedImage)
        val outputStream = activity?.contentResolver?.openOutputStream(
            Uri.fromFile(file)
        )
        outputStream?.let {
            inputStream?.copyTo(it)

            val randomAccessFile = RandomAccessFile(file, "r")
            val length = randomAccessFile.length()
            val lengthError = (length < 10L)
            val start = ByteArray(2)
            randomAccessFile.readFully(start)
            randomAccessFile.seek(length - 2)
            val end = ByteArray(2)
            randomAccessFile.readFully(end)
            success = !lengthError && start[0].toInt() == -1 && start[1].toInt() == -40 &&
                    end[0].toInt() == -1 && end[1].toInt() == -39

            randomAccessFile.close()
            outputStream.close()
        }
        inputStream?.close()
        file.delete()
        return success
    }
}





















