package com.vikashsinghapp.lockin.presentation.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import com.vikashsinghapp.lockin.BuildConfig
import com.vikashsinghapp.lockin.R
import timber.log.Timber

fun composeEmailWithUris(context: Context, feedback: String, imageUris: List<Uri> = emptyList()) {
    val selectorIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri() // only email apps should handle this
    }

    val emailIntent =
        Intent(if (imageUris.isNotEmpty()) Intent.ACTION_SEND_MULTIPLE else Intent.ACTION_SEND).apply {
//            type = "message/rfc822" // Email MIME type
            putExtra(Intent.EXTRA_EMAIL, arrayOf("mrvikashsingh64@gmail.com"))
            putExtra(
                Intent.EXTRA_SUBJECT,
                "${context.getString(R.string.app_name)} ${context.getString(R.string.app)}, ${
                    context.getString(R.string.feedback)
                }"
            )

            val deviceInfo =
                "${context.applicationContext.packageName}, App Version: ${BuildConfig.VERSION_NAME}, " +
                        "Model: ${Build.MODEL}, SDK: ${Build.VERSION.SDK_INT} (${Build.VERSION.RELEASE})"
            putExtra(
                Intent.EXTRA_TEXT,
                "$deviceInfo\n\n${context.getString(R.string.hey)}, ${
                    context.getString(
                        R.string.vikash_singh
                    )
                }!\n" +
                        feedback
            )
            if (imageUris.isNotEmpty()) {
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUris))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            selector = selectorIntent

        }
    try {
        context.startActivity(
            Intent.createChooser(
                emailIntent,
                context.getString(R.string.pick_an_email_provider)
            )
        )
        Timber.tag("InApp Feedback").d("startActivity try block")
    } catch (e: ActivityNotFoundException) {
        e.printStackTrace()
        Timber.tag("InApp Feedback").d("startActivity catch block")
    }
}

fun Context.shareApp() {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, "Share LockIn")
        putExtra(
            Intent.EXTRA_TEXT,
            "I've been using LockIn to kill my screen time. It locks your schedule and literally forces you to type an excuse if you try to quit a focus session. It's brutal but it works.\nTry it: https://play.google.com/store/apps/details?id=com.vikashsinghapp.lockin"
        )
    }
    startActivity(Intent.createChooser(shareIntent, "Share LockIn"))
}

//fun getFileUri(context: Context, path: String): Uri {
//    val file = File(path)
//    return FileProvider.getUriForFile(
//        context,
//        "${context.packageName}.fileprovider",
//        file
//    )
//}