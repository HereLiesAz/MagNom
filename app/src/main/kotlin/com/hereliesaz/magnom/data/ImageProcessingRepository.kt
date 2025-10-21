package com.hereliesaz.magnom.data

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.hereliesaz.magnom.utils.TextParsing
import java.io.IOException
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ParsedCardData(
    val name: String?,
    val pan: String?,
    val expirationDate: String?
)

class ImageProcessingRepository(private val context: Context) {
    suspend fun processImage(uri: Uri): Result<ParsedCardData> {
        return try {
            withContext(Dispatchers.IO) {
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                val image = InputImage.fromFilePath(context, uri)
                val visionText = Tasks.await(recognizer.process(image))
                val text = visionText.text
                val parsedData = ParsedCardData(
                    name = TextParsing.parseCardholderName(text),
                    pan = TextParsing.parseCardNumber(text),
                    expirationDate = TextParsing.parseExpirationDate(text)
                )
                Result.success(parsedData)
            }
        } catch (e: IOException) {
            Result.failure(Exception("Failed to process image", e))
        }
    }
}
