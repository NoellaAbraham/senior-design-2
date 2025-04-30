package com.example.sd2

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ResourcePopupDialogFragment : DialogFragment() {

    private lateinit var webView: WebView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_resource_popup, null)
        webView = view.findViewById(R.id.webView)
        val closeButton = view.findViewById<Button>(R.id.closeButton)
        val downloadPdfButton = view.findViewById<Button>(R.id.downloadPdfButton)
        val progressBar = view.findViewById<ProgressBar>(R.id.loadingProgress)

        val link = arguments?.getString("LINK")

        if (link != null) {
            webView.settings.javaScriptEnabled = true
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    progressBar.visibility = View.GONE
                }
            }
            webView.loadUrl(link)

            downloadPdfButton.setOnClickListener {
                createWebPagePrint(link)
            }
        } else {
            progressBar.visibility = View.GONE
            webView.loadData("Invalid link", "text/html", "utf-8")
        }

        closeButton.setOnClickListener {
            dismiss()
        }

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .create()
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    private fun createWebPagePrint(fileName: String) {
        try {
            val printManager = requireContext().getSystemService(Context.PRINT_SERVICE) as PrintManager
            val adapter: PrintDocumentAdapter = webView.createPrintDocumentAdapter("WebPage")

            val jobName = "WebPage_${System.currentTimeMillis()}"

            printManager.print(
                jobName,
                adapter,
                PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(PrintAttributes.Resolution("RESOLUTION_ID", Context.PRINT_SERVICE, 300, 300))
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()
            )

            Toast.makeText(requireContext(), "Saving page as PDF...", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "PDF generation failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    companion object {
        fun newInstance(link: String): ResourcePopupDialogFragment {
            val fragment = ResourcePopupDialogFragment()
            val args = Bundle()
            args.putString("LINK", link)
            fragment.arguments = args
            return fragment
        }
    }
}
