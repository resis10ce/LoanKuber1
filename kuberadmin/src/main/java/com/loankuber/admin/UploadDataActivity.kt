package com.loankuber.admin

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.loankuber.admin.databinding.ActivityUploadDataBinding
import com.loankuber.library.CustomerStaticData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class UploadDataActivity : AppCompatActivity() {

    private val dataList = ArrayList<CustomerStaticData>()

    private lateinit var binding: ActivityUploadDataBinding
    private val READ_REQUEST_CODE = 42

    private lateinit var firestore: FirebaseFirestore

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_upload_data)

        firestore = FirebaseFirestore.getInstance()

        title = "Upload Data"

        progressDialog = ProgressDialog(this).apply {
            setMessage("Uploading data...")
        }

        binding.selectFile.setOnClickListener {
            selectCsvFile()
        }

        binding.uploadData.setOnClickListener {
            if(dataList.size == 0){
                Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            uploadDataToFirestore()
        }
    }

    private fun uploadDataToFirestore() {
        progressDialog.show()
        val collectionRef = firestore.collection("CustomerStaticData")
        CoroutineScope(Dispatchers.IO).launch {
            for (customer in dataList) {
                try {
                    collectionRef.document(customer.loanNumber).set(customer).await()
                    withContext(Dispatchers.Main){
                        progressDialog.setMessage(getProgressMessage(dataList.indexOf(customer) + 1))
                    }
                } catch (e: Exception){}
            }
            withContext(Dispatchers.Main) {
                binding.sample.text = ""
                binding.selectedFile.text = "No file selected"
                binding.summary.text = "No sample data to show"
                dataList.clear()
                Toast.makeText(this@UploadDataActivity, "Data uploaded", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        }
    }

    private fun getProgressMessage(current: Int): String {
        return "Uploading data $current of ${dataList.size}"
    }

    private fun selectCsvFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, READ_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK) {
            resultData?.data?.also { uri ->
                readCsvFile(uri)
            }
        }
    }

    private fun readCsvFile(uri: Uri) {
        binding.selectedFile.text = "File: "+uri.lastPathSegment?.split('/')?.last()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                var lineNumber = 0
                var sampleData = ""
                dataList.clear()
                while (reader.readLine().also { line = it } != null) {
                    if(lineNumber == 0){
                        val headers = line?.split(',')

                        if(headers == null || headers.count() < 3){
                            Toast.makeText(this, "Format not correct", Toast.LENGTH_SHORT).show()
                            return
                        }
                        lineNumber++
                        continue
                    }
                    val fields = line?.split(',')
                    val loanNumber = fields?.get(0).toString()
                    val name = fields?.get(1).toString()
                    val phoneNumber = fields?.get(2).toString()

                    dataList.add(CustomerStaticData(loanNumber, name, phoneNumber))

                    if(lineNumber <= 3){
                        sampleData = sampleData + line + "\n"
                    }
                    lineNumber++
                }
                binding.summary.text = "Total Customers: ${dataList.size}"
                binding.sample.text = sampleData
            }
        }
    }
}