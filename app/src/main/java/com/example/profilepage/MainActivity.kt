package com.example.profilepage

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.profilepage.databinding.ActivityMainBinding
import com.example.profilepage.databinding.DialogLayoutBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var startActivityLauncher: ActivityResultLauncher<Intent>
    private lateinit var galleryLauncher: ActivityResultLauncher<String>
    private lateinit var profile:Profile
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startActivityLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if(result.resultCode== RESULT_OK){
                val bitmap =(result.data?.extras?.get("data") as? Bitmap
                    ?: return@registerForActivityResult)
                binding.imgViewProfilePic.setImageBitmap(bitmap)
            }
        }
        galleryLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ){uri: Uri?->
            if(uri!=null){
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,uri)
                binding.imgViewProfilePic.setImageBitmap(bitmap)
            }
        }
        binding.btnCamera.setOnClickListener {
            showDialog()
        }
        binding.floatingActionButton.setOnClickListener {
            showUpdateDialog()
        }
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
            galleryLauncher.launch("image/*")
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
