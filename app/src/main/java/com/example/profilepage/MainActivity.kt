package com.example.profilepage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.profilepage.databinding.ActivityMainBinding
import com.example.profilepage.databinding.DialogLayoutBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var startActivityLauncher: ActivityResultLauncher<Intent>
//    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var storageRef:StorageReference
    private lateinit var firebaseFirestore: FirebaseFirestore
    private var imageUri: Uri? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initStorage()

        startActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if(result.resultCode== RESULT_OK){
                val bitmap =(result.data?.extras?.get("data") as? Bitmap
                    ?: return@registerForActivityResult)
                binding.imgViewProfilePic.setImageBitmap(bitmap)
            }
        }
//        galleryLauncher = registerForActivityResult(
//            ActivityResultContracts.GetContent()
//        ){uri: Uri?->
//            if(uri!=null){
//                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,uri)
//                binding.imgViewProfilePic.setImageBitmap(bitmap)
//            }
//        }

        binding.btnCamera.setOnClickListener {
            showDialog()
        }
        binding.floatingActionButton.setOnClickListener {
            showUpdateDialog()
        }
        binding.btnAdd.setOnClickListener {
            addProfile()
        }
    }

    //function for opening the gallery
    private val resultLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()){
        imageUri= it
        binding.imgViewProfilePic.setImageURI(it)
    }
    private fun addProfile() {
        storageRef = storageRef.child(System.currentTimeMillis().toString())
        imageUri?.let{
            storageRef.putFile(it).addOnCompleteListener{task->
                if(task.isSuccessful){
                    storageRef.downloadUrl.addOnSuccessListener {uri->
                        var fullName= binding.txtFullName.text.toString()
                        var address = binding.txtLocation.text.toString()
                        var email = binding.editTxtEmail.text.toString()
                        var phone = binding.editTxtPhone.text.toString()
                        var twitter= binding.editTxtTwitter.text.toString()
                        var facebook=binding.editTxtFacebook.text.toString()
                        var imageLink =uri.toString()
                        val db = Firebase.firestore
                        Toast.makeText(applicationContext,"Profile Added!",Toast.LENGTH_SHORT).show()
                        // Create a new user with a first and last name
                        val user = hashMapOf(
                            "fullName" to fullName,
                            "address"  to address,
                            "email"    to email,
                            "phone"    to phone,
                            "twitter"  to twitter,
                            "facebook" to facebook,
                            "imageLink" to imageLink)

                        // Add a new document with a generated ID
                        db.collection("userProfile")
                            .add(user)
                            .addOnSuccessListener { _ ->
                                Toast.makeText(applicationContext,"SUCCESS!",Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(applicationContext,"FAILURE!",Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                else{
                    Toast.makeText(applicationContext,"FAILED!",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initStorage() {
        //initialize firebase objects
        storageRef = FirebaseStorage.getInstance().reference.child("Images")
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    private fun showDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Update Profile Picture")
        dialogBuilder.setMessage("Choose an action")
        dialogBuilder.setPositiveButton("Camera"){dialog,_->
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),100)
            }else{
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityLauncher.launch(intent)
            }
            dialog.dismiss()
        }
        dialogBuilder.setNegativeButton("Gallery"){dialog,_->
            resultLauncher.launch("image/*")
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }
    private fun showUpdateDialog() {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Update Profile")

        val dialogLayout=layoutInflater.inflate(R.layout.dialog_layout,null)
        val dialogBinding=DialogLayoutBinding.bind(dialogLayout)
        alertDialogBuilder.setView(dialogLayout)

        alertDialogBuilder.setPositiveButton("Ok"){dialog,_->
            val fname= dialogBinding.edFname.text.toString()
            val lname = dialogBinding.edlastname.text.toString()
            val address = dialogBinding.edAddress.text.toString()
            val email= dialogBinding.edEmail.text.toString()
            val phone = dialogBinding.edPhone.text.toString()
            val twitter = dialogBinding.edTwitter.text.toString()
            val facebook = dialogBinding.edFacebook.text.toString()
            val newProfile= Profile(0,fname, lname,address,email,phone,twitter,facebook)
            update(newProfile)
        }
        alertDialogBuilder.setNegativeButton("Cancel"){dialog,_->
            dialog.dismiss()
        }
        val alertDialog:AlertDialog=alertDialogBuilder.create()
        alertDialog.show()


    }
    private fun update(newProfile: Profile) {
        binding.txtFullName.text = "${newProfile.firstName} ${newProfile.lastName}"
        binding.txtLocation.text= newProfile.address
        binding.editTxtEmail.setText(newProfile.email)
        binding.editTxtPhone.setText(newProfile.phone)
        binding.editTxtTwitter.setText(newProfile.twitter)
        binding.editTxtFacebook.setText(newProfile.facebook)

    }
}
