package com.cashfree.webintegration

import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Xml
import android.view.Menu
import android.view.MenuItem
import android.webkit.ConsoleMessage
import android.webkit.ValueCallback
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
        val inputTag = "<input type=\"hidden\" name=\"%s\" value=\"%s\"/>"
        val platform = "chxx-c-x-x-x-w-x-a-" + Build.VERSION.SDK_INT
        val url = "https://sandbox.cashfree.com/pg/view/sessions/checkout"
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

        val formBody = java.lang.StringBuilder()
        formBody.append(String.format(inputTag, "hideHeader", true))
        formBody.append(String.format(inputTag, "platform", platform))
        formBody.append(
            String.format(
                inputTag,
                "payment_session_id",
                "session_-GhmpV9RRyPL5BY2wq5QRfPqJJ1BMx5QEUfmHI91ZSjls5M-OG2x8lPa8p3XC8TDakf3Y2ejPt0WyikVbXBJkNLlcP8n3Uuoe8Cs252vmbRS"
            )
        )


        val body = String.format(formTemplate.toString(), url, formBody.toString())
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
            binding.webViewContainer.evaluateJavascript("window.showVerifyUI()", ValueCallback<String?> {
                //ignore
            })
        }
    }
}