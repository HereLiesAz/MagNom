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

/**
 * Data class to hold the extracted information from a card image.
 *
 * @property name The extracted cardholder name, or null if not found.
 * @property pan The extracted Primary Account Number, or null if not found.
 * @property expirationDate The extracted expiration date (MM/YY), or null if not found.
 */
data class ParsedCardData(
    val name: String?,
    val pan: String?,
    val expirationDate: String?
)

/**
 * Repository responsible for performing OCR (Optical Character Recognition) on images.
 *
 * It uses Google ML Kit's Text Recognition API to extract text from images of cards
 * and then uses regex-based parsing to identify card fields.
 *
 * @param context The application context, required for loading images from URIs.
 */
class ImageProcessingRepository(private val context: Context) {

    /**
     * Processes an image from the given URI to extract card data.
     *
     * This method runs on the IO dispatcher to avoid blocking the main thread.
     *
     * @param uri The URI of the image to process.
     * @return A [Result] containing the [ParsedCardData] on success, or an exception on failure.
     */
    suspend fun processImage(uri: Uri): Result<ParsedCardData> {
        return try {
            // Switch to IO context for heavy I/O and processing
            withContext(Dispatchers.IO) {
                // Initialize the Text Recognizer with default options (Latin script)
                val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

                // Create an InputImage object from the file URI
                val image = InputImage.fromFilePath(context, uri)

                // Process the image synchronously (blocking call, hence withContext)
                // Tasks.await is used to bridge the Play Services Task API with Kotlin Coroutines
                val visionText = Tasks.await(recognizer.process(image))

                // Extract the raw text result
                val text = visionText.text

                // Parse the raw text to find specific card fields using utility functions
                val parsedData = ParsedCardData(
                    name = TextParsing.parseCardholderName(text),
                    pan = TextParsing.parseCardNumber(text),
                    expirationDate = TextParsing.parseExpirationDate(text)
                )

                // Return the result wrapped in Result.success
                Result.success(parsedData)
            }
        } catch (e: IOException) {
            // Handle IO errors (e.g., file not found)
            Result.failure(Exception("Failed to process image", e))
        }
    }
}
