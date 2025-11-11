package com.example.cyglobaltech

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class PrintJob(
    val uid: String = "",
    val userId: String = "",
    val fileUrls: List<String> = emptyList(),
    val fileNames: List<String> = emptyList(),
    val fileCount: Int = 0,
    val status: String = "Pending",
    val createdAt: FieldValue? = null
)

class PrintUploadActivity : AppCompatActivity() {

    private lateinit var uploadButton: Button
    private lateinit var fileCountText: TextView
    private lateinit var fileUploadForm: LinearLayout
    private lateinit var fileUploadModal: LinearLayout
    private lateinit var submitButton: Button
    private lateinit var closeButton: ImageView
    private lateinit var loadingSpinner: ProgressBar

    private val PICK_FILES_REQUEST_CODE = 101
    private var selectedFiles: List<Uri> = emptyList()

    private val storage = FirebaseStorage.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var currentUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print_upload)

        currentUid = UserManager.getLoggedInUid()
        if (currentUid == null) {
            Toast.makeText(this, "You must be logged in to upload files", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        uploadButton = findViewById(R.id.upload_documents_btn)
        fileCountText = findViewById(R.id.file_count_text)
        fileUploadForm = findViewById(R.id.print_upload_form)
        fileUploadModal = findViewById(R.id.print_upload_modal)
        submitButton = findViewById(R.id.submit_print_button)
        closeButton = findViewById(R.id.close_print_modal)

        loadingSpinner = findViewById(R.id.loading_spinner)

        uploadButton.setOnClickListener { openFilePicker() }
        closeButton.setOnClickListener { fileUploadModal.visibility = View.GONE }
        submitButton.setOnClickListener { uploadAndSubmitPrintJob() }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }
        startActivityForResult(Intent.createChooser(intent, "Select Documents"), PICK_FILES_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != PICK_FILES_REQUEST_CODE || resultCode != Activity.RESULT_OK) return

        val uriList = mutableListOf<Uri>()
        data?.clipData?.let { clip ->
            for (i in 0 until clip.itemCount) clip.getItemAt(i).uri?.let { uriList.add(it) }
        } ?: data?.data?.let { uriList.add(it) }

        selectedFiles = uriList
        fileCountText.text = "${uriList.size} file(s) selected"
        fileUploadModal.visibility = View.VISIBLE
    }

    private fun uploadAndSubmitPrintJob() {
        if (selectedFiles.isEmpty()) {
            Toast.makeText(this, "Please select at least one file", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            val downloadUrls = mutableListOf<String>()
            val fileNames = mutableListOf<String>()

            try {
                for (fileUri in selectedFiles) {
                    val fileName = getFileName(fileUri)
                    fileNames.add(fileName)

                    val storageRef = storage.reference.child("print_uploads/$currentUid/$fileName-${System.currentTimeMillis()}")
                    val uploadTask = storageRef.putFile(fileUri).await()

                    val downloadUrl = storageRef.downloadUrl.await().toString()

                    downloadUrls.add(downloadUrl)
                }

                val printJob = PrintJob(
                    userId = currentUid!!,
                    fileUrls = downloadUrls,
                    fileNames = fileNames,
                    fileCount = selectedFiles.size,
                    status = "Pending",
                    createdAt = FieldValue.serverTimestamp()
                )

                db.collection("printJobs").add(printJob).await()

                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(this@PrintUploadActivity, "Successfully submitted ${selectedFiles.size} file(s)!", Toast.LENGTH_LONG).show()
                    fileUploadModal.visibility = View.GONE
                    selectedFiles = emptyList()
                    fileCountText.text = "No files selected"
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(this@PrintUploadActivity, "Upload failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        submitButton.isEnabled = !isLoading
        closeButton.isEnabled = !isLoading
        loadingSpinner.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        return fileName
    }
}