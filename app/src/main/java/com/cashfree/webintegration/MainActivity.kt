package com.cashfree.webintegration

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.Xml
import android.view.Menu
import android.view.MenuItem
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.cashfree.webintegration.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        initWebView()
        createCFPayForm()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun initWebView() {
        binding.webViewContainer.settings.run {
            javaScriptEnabled = true
            domStorageEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            allowContentAccess = false
            allowFileAccess = false
        }

        binding.webViewContainer.run {
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    Log.d(
                        "WebView Log : ",
                        consoleMessage?.message() + " at " + consoleMessage?.sourceId() + ":" + consoleMessage?.lineNumber()
                    )
                    return super.onConsoleMessage(consoleMessage)
                }
            }

            webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    Log.d("WebView onPageStarted Log : ", "$url")
                    super.onPageStarted(view, url, favicon)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    Log.d("WebView onPageFinished Log : ", "$url")
                    super.onPageFinished(view, url)
                }
            }
            addJavascriptInterface(CFJsBridge(this@MainActivity), "Android")
        }
    }


    private fun createCFPayForm() {
        val iStream = resources.openRawResource(com.cashfree.pg.core.R.raw.cashfree_pay_form)
        val formTemplate = StringBuilder()

        try {
            val reader = BufferedReader(InputStreamReader(iStream))
            var line: String?

            while ((reader.readLine().also { line = it }) != null) {
                formTemplate.append(line)
            }
            reader.close()
        } catch (ignored: IOException) {
            Log.d("HTML Exception", "${ignored.message}")
        }

        val environment  = "sandbox"
        val paymentSessionId  = "session_lTKCsYMk5ojQKRMrz1zZtnJweZ1o4rqMoUnUB70bcdWqJoh0KUcelSe2aIfZR5IjKDGYbvDgHa4JObiRJuJi8n651FEdV8eidpmKmpMLWukd"
        val body = String.format(formTemplate.toString(), environment, paymentSessionId)
        openCheckoutPage(body)
    }

    private fun openCheckoutPage(body: String) {
        binding.webViewContainer.loadDataWithBaseURL(
            "",
            body,
            "text/html",
            Xml.Encoding.UTF_8.name,
            ""
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1000) {
            binding.webViewContainer.evaluateJavascript("window.showVerifyUI()") {
                //ignore
            }
        }
    }
}