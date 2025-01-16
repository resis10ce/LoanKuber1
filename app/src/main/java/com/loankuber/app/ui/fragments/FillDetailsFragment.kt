package com.loankuber.app.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.Filter
import android.widget.TextView
import com.google.android.material.datepicker.MaterialDatePicker
import com.loankuber.app.DetailActivity
import com.loankuber.app.R
import com.loankuber.library.utils.KotlinUtils.toast
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class FillDetailsFragment : Fragment(R.layout.fragment_fill_details) {

    private lateinit var parentActivity: DetailActivity

    private lateinit var dropdownMenu: AutoCompleteTextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentActivity = requireActivity() as DetailActivity

        val customerName = view.findViewById<TextView>(R.id.fill_details_customer_name)
        val loanNumber = view.findViewById<TextView>(R.id.fill_details_loan_number)
        val ptpDateView = view.findViewById<EditText>(R.id.ptp_date)
        val nextDateView = view.findViewById<EditText>(R.id.next_visit_date)
        val continueBtn = view.findViewById<Button>(R.id.fill_details_continue)
        dropdownMenu = view.findViewById(R.id.dropdown_menu)
        setupDropdownValues()

        customerName.text = parentActivity.name
        loanNumber.text = parentActivity.loanNumber

        ptpDateView.setOnClickListener {
            showPtpDatePicker(ptpDateView)
        }

        nextDateView.setOnClickListener {
            showNextDatePicker(nextDateView)
        }

        if(parentActivity.outcome == "PTP"){
            ptpDateView.visibility = View.VISIBLE
        } else {
            ptpDateView.visibility = View.GONE
        }

        dropdownMenu.onItemClickListener = AdapterView.OnItemClickListener { parent, dropdownView, position, id ->
            val selectedItem = parent.getItemAtPosition(position).toString()
            if(selectedItem == "PTP") {
                ptpDateView.visibility = View.VISIBLE
            } else {
                ptpDateView.visibility = View.GONE
            }
            parentActivity.outcome = selectedItem
        }

        continueBtn.setOnClickListener {
            if(parentActivity.hasFilledDetails())
                parentActivity.showNextFragment()
            else
                toast("Please fill all the details")
        }
    }

    private fun setupDropdownValues() {
        val options = listOf("Money received", "PTP", "Customer refused to pay", "Customer unavailable")
        val adapter =  object : ArrayAdapter<String>(requireContext(), com.loankuber.library.R.layout.dropdown_menu_item, options) {
            override fun getFilter(): Filter {
                return object : Filter() {
                    override fun performFiltering(constraint: CharSequence?): FilterResults {
                        val filterResults = FilterResults()
                        filterResults.values = options
                        filterResults.count = options.size
                        return filterResults
                    }

                    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                        notifyDataSetChanged()
                    }
                }
            }
        }
        dropdownMenu.setAdapter(adapter)
    }

    private fun showPtpDatePicker(selectedDateView: EditText) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select PTP date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // Optional: Set initial selection
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = format.format(calendar.time)

            selectedDateView.setText(formattedDate)
            parentActivity.setPtpDate(formattedDate)
        }

        datePicker.show(childFragmentManager, "datePicker")
    }

    private fun showNextDatePicker(selectedDateView: EditText) {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select next visit date")
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds()) // Optional: Set initial selection
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.timeInMillis = selection
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val formattedDate = format.format(calendar.time)

            selectedDateView.setText(formattedDate)
            parentActivity.setNextVisitDate(formattedDate)
        }

        datePicker.show(childFragmentManager, "datePicker")
    }

}