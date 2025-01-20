package com.loankuber.app.ui.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.loankuber.app.AppDatabase
import com.loankuber.app.DetailActivity
import com.loankuber.app.R
import com.loankuber.app.dao.Customer
import com.loankuber.app.dao.CustomerDao
import com.loankuber.app.utils.SharedPrefsUtil
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

    // This is to make a small delay before searching for the customer
    private var debounceHandler: Handler = Handler(Looper.getMainLooper())
    private var debounceRunnable: Runnable? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = AppDatabase.getDatabase(requireContext())
        customerDao = db.customerDao()

        parentActivity = requireActivity() as DetailActivity

        dataFetchProgress = ProgressDialog(requireContext()).apply {
            setMessage("Please wait, syncing data...")
            setCancelable(false)
        }

        val nameField = view.findViewById<TextView>(R.id.name)
        val agentName = view.findViewById<TextView>(R.id.agent_name)
        val loanNumberField = view.findViewById<TextView>(R.id.loan_number)
        val selectBtn = view.findViewById<Button>(R.id.select_customer)
        dropdownMenu = view.findViewById(R.id.dropdown_menu)

        if(parentActivity.name != null && parentActivity.loanNumber!=null){
            nameField.text = "Name: ${parentActivity.name}"
            loanNumberField.text = "Loan number: ${parentActivity.loanNumber}"
        }

        agentName.text = "Hi "+SharedPrefsUtil.getInstance(requireContext())?.getString(SharedPrefsUtil.AGENT_NAME)

        if(!dataFetched){
            fetchAndStoreData()
        }
        setupDropdownForSearch()

        dropdownMenu.onItemClickListener = AdapterView.OnItemClickListener { parent, dropdownView, position, id ->
            val loanNumber = parent.getItemAtPosition(position).toString()
            CoroutineScope(Dispatchers.IO).launch {
                val name = db.customerDao().getCustomerNameByLoanNumber(loanNumber)
                withContext(Dispatchers.Main) {
                    nameField.text = "Name: $name"
                    loanNumberField.text = "Loan number: $loanNumber"
                    parentActivity.setCustomerDetails(name, loanNumber)
                }
            }
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
        CoroutineScope(Dispatchers.IO).launch {
            val data = customerDao.searchCustomers(20)
            withContext(Dispatchers.Main) {
                val adapter = ArrayAdapter(requireContext(), com.loankuber.library.R.layout.dropdown_menu_item, data)
                dropdownMenu.setAdapter(adapter)
                adapter.notifyDataSetChanged()
            }
        }



        dropdownMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    if (it.isNotEmpty()) {
                        debounceRunnable?.let { runnable -> debounceHandler?.removeCallbacks(runnable) }
                        debounceRunnable = Runnable {
                            CoroutineScope(Dispatchers.IO).launch {
                                val data = customerDao.searchCustomersByLoanNumber(it.toString())
                                withContext(Dispatchers.Main) {
                                    val adapter = ArrayAdapter(requireContext(), com.loankuber.library.R.layout.dropdown_menu_item, data)
                                    dropdownMenu.setAdapter(adapter)
                                    adapter.notifyDataSetChanged()
                                }
                            }
                        }
                        debounceHandler.postDelayed(debounceRunnable!!, 300)
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

}