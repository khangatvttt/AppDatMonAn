package com.example.projectdatmonan.Database

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.util.UUID

class FirebaseStorageDB {
    val storage = FirebaseStorage.getInstance()
    val storageRef: StorageReference = storage.reference

    fun uploadImageToFirebase(imageUri: Uri, onComplete: (String?) -> Unit) {
        // Create a unique name for the image
        val fileName = "${UUID.randomUUID()}.jpg"

        // Reference to the location where the image will be uploaded
        val imageRef = storageRef.child(fileName)

        // Upload the image
        val uploadTask: UploadTask = imageRef.putFile(imageUri)

        // Listen for success or failure of the upload
        uploadTask.addOnSuccessListener {
            // Image uploaded successfully
            imageRef.downloadUrl.addOnSuccessListener { uri ->
                // Get the download URL for the image
                onComplete(uri.toString())
            }
        }.addOnFailureListener { exception ->
            onComplete(null)
        }
    }
}