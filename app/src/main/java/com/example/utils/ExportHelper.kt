package com.example.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.data.FieldEntry
import com.example.data.MaterialItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.OutputStream

object ExportHelper {

    fun entriesToCsv(entries: List<FieldEntry>): String {
        val sb = StringBuilder()
        sb.append("Checker,TM,Block,Sub-Block,MRF Number,DRR-CODE,Type Scheme,Material,Requested,Received,Used,Remaining,Comment,Date\n")
        for (ent in entries) {
            val schemesEscaped = ent.schemes.joinToString("; ").replace("\"", "\"\"")
            for (mat in ent.materials) {
                val row = listOf(
                    "\"${ent.checker.replace("\"", "\"\"")}\"",
                    "\"${ent.tm.replace("\"", "\"\"")}\"",
                    "\"${ent.block.replace("\"", "\"\"")}\"",
                    "\"${ent.subBlock.replace("\"", "\"\"")}\"",
                    "\"${ent.mrfNumber.replace("\"", "\"\"")}\"",
                    "\"${ent.drrCode.replace("\"", "\"\"")}\"",
                    "\"$schemesEscaped\"",
                    "\"${mat.name.replace("\"", "\"\"")}\"",
                    mat.requested,
                    mat.received,
                    mat.used,
                    mat.remaining,
                    "\"${mat.comment.replace("\"", "\"\"")}\"",
                    "\"${ent.date}\""
                )
                sb.append(row.joinToString(",")).append("\n")
            }
        }
        return sb.toString()
    }

    fun entriesToJson(entries: List<FieldEntry>): String {
        return try {
            val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
            val listType = Types.newParameterizedType(List::class.java, FieldEntry::class.java)
            val adapter = moshi.adapter<List<FieldEntry>>(listType).indent("  ")
            adapter.toJson(entries)
        } catch (e: Exception) {
            e.printStackTrace()
            "[]"
        }
    }

    fun saveFileToDownloads(context: Context, filename: String, mimeType: String, content: String): Uri? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    val outputStream: OutputStream? = resolver.openOutputStream(uri)
                    outputStream?.use { it.write(content.toByteArray()) }
                    Toast.makeText(context, "Saved to Downloads: $filename", Toast.LENGTH_LONG).show()
                }
                uri
            } else {
                // Pre-Q fallback
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = java.io.File(downloadsDir, filename)
                file.writeText(content)
                Toast.makeText(context, "Saved to Downloads: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    fun generateWhatsAppText(entry: FieldEntry): String {
        val materialLines = entry.materials.mapIndexed { index, mat ->
            val serial = (index + 1).toString().padStart(2, '0')
            var line = "*$serial ${mat.name}*"
            if (mat.requested.isNotEmpty()) line += " | Req: ${mat.requested}"
            if (mat.received.isNotEmpty()) line += " | Rec: ${mat.received}"
            if (mat.used.isNotEmpty()) line += " | Used: ${mat.used}"
            if (mat.remaining.isNotEmpty()) line += " | Rem: ${mat.remaining}"
            if (mat.comment.isNotEmpty()) line += " | *Comment:* ${mat.comment}"
            line.trim()
        }.joinToString("\n")

        val schemesText = if (entry.schemes.isNotEmpty()) "*Scheme(s):* ${entry.schemes.joinToString(", ")}" else ""
        val mrfText = if (entry.mrfNumber.isNotEmpty()) "*MRF:* ${entry.mrfNumber}\n" else ""
        val drrText = if (entry.drrCode.isNotEmpty()) "*DRR:* ${entry.drrCode}\n" else ""
        
        return """
            *✅ Field Material Entry*
            ----------------------------------
            *Date:* ${entry.date}
            *Checker:* ${entry.checker}
            *TM:* ${entry.tm}
            *Block:* ${entry.block} *Sub:* ${entry.subBlock}
            $mrfText$drrText$schemesText
            
            *--- Materials Tracked ---*
            $materialLines
            ----------------------------------
            Generated by Field Material App 🚧
        """.trimIndent()
    }

    fun shareViaWhatsApp(context: Context, text: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(text))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general sharing if WhatsApp is not installed
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Info"))
        }
    }
}
