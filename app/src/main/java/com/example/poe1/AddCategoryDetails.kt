
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.ByteArrayOutputStream

@Composable
fun AddAndViewCategoryDetails(navController: NavHostController, userName: String, categoryId: String) {
    val database = Firebase.database
    val categoryDetailsRef = database.getReference("users").child(userName).child("categories").child(categoryId).child("details")

    var goal by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dateOfAcquisition by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<Bitmap?>(null) }
    var showCategoryDetails by remember { mutableStateOf(false) }
    var categoryDetails by remember { mutableStateOf<List<CategoryDetail>>(emptyList()) }

    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            photoUri = bitmap
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                photoUri = it
            }
        }
    }

    LaunchedEffect(Unit) {
        categoryDetailsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val details = mutableListOf<CategoryDetail>()
                for (detailSnapshot in snapshot.children) {
                    val detail = detailSnapshot.getValue(CategoryDetail::class.java)
                    if (detail != null) {
                        details.add(detail)
                    }
                }
                categoryDetails = details
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load details: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(text = "Add Category Detail", fontSize = 35.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = goal,
            onValueChange = { newText -> goal = newText },
            label = { Text(text = "Goal") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { newText -> description = newText },
            label = { Text(text = "Description") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = dateOfAcquisition,
            onValueChange = { newText -> dateOfAcquisition = newText },
            label = { Text(text = "Date of Acquisition") }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureLauncher.launch(takePictureIntent)
        }) {
            Text(text = "Take Photo")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            imagePickerLauncher.launch("image/*")
        }) {
            Text(text = "Pick Photo from Gallery")
        }
        Spacer(modifier = Modifier.height(16.dp))

        photoUri?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.size(100.dp))
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = {
            if (goal.isNotEmpty() && description.isNotEmpty() && dateOfAcquisition.isNotEmpty() && photoUri != null) {
                val detailId = categoryDetailsRef.push().key
                if (detailId != null) {
                    val baos = ByteArrayOutputStream()
                    photoUri?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    val photoBase64 = java.util.Base64.getEncoder().encodeToString(data)

                    val categoryDetail = CategoryDetail(goal, description, dateOfAcquisition, photoBase64)
                    categoryDetailsRef.child(detailId).setValue(categoryDetail)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Detail Added Successfully", Toast.LENGTH_SHORT).show()
                                goal = ""
                                description = ""
                                dateOfAcquisition = ""
                                photoUri = null
                            } else {
                                Toast.makeText(context, "Failed to Add Detail: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
            }
        }) {
            Text(text = "Add Detail")
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            showCategoryDetails = !showCategoryDetails
        }) {
            Text(text = if (showCategoryDetails) "Hide Details" else "View Details")
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (showCategoryDetails) {
            categoryDetails.forEach { detail ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    Text(
                        text = "Goal: ${detail.goal}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(text = "Description: ${detail.description}")
                    Text(text = "Date of Acquisition: ${detail.dateOfAcquisition}")
                    detail.photoBase64?.let { base64 ->
                        val imageBytes = java.util.Base64.getDecoder().decode(base64)
                        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier.size(100.dp)
                        )
                        Spacer(modifier = Modifier.height(15.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

data class CategoryDetail(
    val goal: String = "",
    val description: String = "",
    val dateOfAcquisition: String = "",
    val photoBase64: String? = null
)
