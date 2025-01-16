package com.loankuber.app.ui.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import com.google.firebase.firestore.FirebaseFirestore
import com.loankuber.app.AppDatabase
import com.loankuber.app.DetailActivity
import com.loankuber.app.R
import com.loankuber.app.dao.Customer
import com.loankuber.app.dao.CustomerDao
import com.loankuber.library.utils.KotlinUtils.toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class GetCustomerFragment : Fragment(R.layout.fragment_get_customer) {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var db: AppDatabase
    private lateinit var customerDao: CustomerDao

    private lateinit var parentActivity: DetailActivity

    private lateinit var dropdownMenu: AutoCompleteTextView

    private lateinit var dataFetchProgress: ProgressDialog

    private var dataFetched = false

    private val options = mutableListOf("")
    private lateinit var adapter: ArrayAdapter<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())
        customerDao = db.customerDao()

        adapter = ArrayAdapter(requireContext(), com.loankuber.library.R.layout.dropdown_menu_item, options)

        parentActivity = requireActivity() as DetailActivity

        dataFetchProgress = ProgressDialog(requireContext()).apply {
            setMessage("Please wait, syncing data...")
            setCancelable(false)
        }

        val nameField = view.findViewById<EditText>(R.id.name)
        val loanNumberField = view.findViewById<EditText>(R.id.loan_number)
        val selectBtn = view.findViewById<Button>(R.id.select_customer)
        dropdownMenu = view.findViewById(R.id.dropdown_menu)

        if(!dataFetched){
            fetchAndStoreData()
        }
        setupDropdownForSearch()

        dropdownMenu.onItemClickListener = AdapterView.OnItemClickListener { parent, dropdownView, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            val name = selectedItem.substringBeforeLast(" ")
            val loanNumber = selectedItem.split(" ").last()

            nameField.setText(name)
            loanNumberField.setText(loanNumber)
            parentActivity.setCustomerDetails(name, loanNumber)

            fetchAndFillCustomerDetails(loanNumber)
        }

        dropdownMenu.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submit()
            } else {
                false
            }
        }

        selectBtn.setOnClickListener {
            submit()
        }
    }

    private fun submit(): Boolean{
        if (parentActivity.hasCustomerDetails()) {
            parentActivity.showNextFragment()
            return true
        }
        toast("Please select a customer")
        return false
    }

    private fun fetchAndStoreData() {
        dataFetchProgress.show()

        CoroutineScope(Dispatchers.IO).launch {

            val result = firestore.collection("CustomerStaticData").get().await()

            val documents = result.documents
            val customers = documents.map { document ->
                val loanNumber = document.getString("loanNumber") ?: ""
                val name = document.getString("name") ?: ""
                val searchText = "$name $loanNumber"
                Customer(loanNumber, name, searchText)
            }

            customerDao.insertAll(customers)
            dataFetchProgress.dismiss()
            dataFetched = true

            CoroutineScope(Dispatchers.Main).launch {
                setupDropdownForSearch()
            }

        }

    }

    private fun setupDropdownForSearch() {
        dropdownMenu.setAdapter(adapter)
        CoroutineScope(Dispatchers.IO).launch {
            val data = customerDao.searchCustomers(10)
            withContext(Dispatchers.Main) {
                adapter.clear()
                adapter.addAll(data)
                adapter.notifyDataSetChanged()
            }
        }
        dropdownMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.isNotEmpty()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val data = customerDao.searchCustomers(it.toString())
                            withContext(Dispatchers.Main) {
                                adapter.clear()
                                adapter.addAll(data)
                                adapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchAndFillCustomerDetails(loanNumber: String) {
        CoroutineScope(Dispatchers.IO).launch {

            val doc = firestore.collection("CustomerStaticData").document(loanNumber).get().await()

            if(doc.exists()) {
                val name = doc.getString("name")
                val loanNumber = doc.getString("loanNumber")
                val address = doc.getString("address")
                val phone = doc.getString("phone")


            }
            else {
                toast("No data found for loan number $loanNumber")
            }
        }
    }

}