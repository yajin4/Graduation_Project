package com.example.cocktailproject.dialogFragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.example.cocktailproject.R
import java.lang.IllegalStateException

class ARGuideDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        /*return super.onCreateDialog(savedInstanceState)*/
        return activity?.let {
            val builder=AlertDialog.Builder(it)
            builder.setMessage(getString(R.string.guide))
                .setTitle(R.string.guide_title)
                .setPositiveButton("확인",
                DialogInterface.OnClickListener { dialog, which ->  })

            builder.create()

        } ?: throw IllegalStateException("Activity cannot be null")

    }
}