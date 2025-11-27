package com.twinalyze.servicedemo.fragment

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import com.twinalyze.event.SetAnalytics
import com.twinalyze.servicedemo.LoginActivity
import com.twinalyze.servicedemo.MainActivity
import com.twinalyze.servicedemo.R
import java.io.File
import java.nio.file.Files.exists

class ProfileFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private lateinit var btnLogout: CardView
    private lateinit var btnSubmit: CardView
    private lateinit var imgProfile: ImageView
    private lateinit var txtName: TextView
    private lateinit var txtMobile: TextView
    private lateinit var edtFullName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtMobile: EditText

    private val prefs by lazy {
        requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    }

    private var lastPhotoUri: Uri? = null

    // 1) ask CAMERA permission (Android 6+)
    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) openCamera()
            else Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }

    // 2) take picture into a Uri we provide
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                // Show in UI
                imgProfile.setImageURI(lastPhotoUri)

                // Save to SharedPreferences
                lastPhotoUri?.toString()?.let { uriStr ->
                    prefs.edit().putString("captured_photo_uri", uriStr).apply()
                    Toast.makeText(requireContext(), "Saved in SharedPreferences", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Capture cancelled/failed", Toast.LENGTH_SHORT).show()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        btnLogout = view.findViewById<CardView>(R.id.btnLogout)
        btnSubmit = view.findViewById<CardView>(R.id.btnSubmit)
        imgProfile = view.findViewById<ImageView>(R.id.imgProfile)
        txtName = view.findViewById<TextView>(R.id.txtName)
        txtMobile = view.findViewById<TextView>(R.id.txtMobile)
        edtFullName = view.findViewById<EditText>(R.id.edtFullName)
        edtEmail = view.findViewById<EditText>(R.id.edtEmail)
        edtMobile = view.findViewById<EditText>(R.id.edtMobile)

        btnLogout.setOnClickListener {
            showLogoutDialog(requireContext())
        }


        // Load previously saved photo (if any)
        prefs.getString("captured_photo_uri", null)?.let { uriStr ->
            val uri = Uri.parse(uriStr)
            imgProfile.setImageURI(uri)
            lastPhotoUri = uri
        }
        // Also show them in TextViews
        txtName.text = prefs.getString("user_full_name", "")
        txtMobile.text = prefs.getString("user_mobile", "")

        edtFullName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // not needed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                txtName.text = s.toString()
//                prefs.edit().putString("user_full_name", s.toString()).apply()
            }

            override fun afterTextChanged(s: Editable?) {
                // not needed
            }
        })

        edtEmail.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//                prefs.edit().putString("user_email", s.toString()).apply()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        edtMobile.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                txtMobile.text = s.toString()
//                prefs.edit().putString("user_mobile", s.toString()).apply()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnSubmit.setOnClickListener {
            prefs.edit()
                .putString("user_full_name", edtFullName.text.toString())
                .putString("user_email", edtEmail.text.toString())
                .putString("user_mobile", edtMobile.text.toString())
                .apply()

            txtName.text = edtFullName.text.toString()
            txtMobile.text = edtMobile.text.toString()

//            edtFullName.text = null
//            edtEmail.text = null
//            edtMobile.text = null

            Toast.makeText(requireContext(), "Profile saved successfully", Toast.LENGTH_SHORT).show()
        }




        imgProfile.setOnClickListener {
            // Android 13+ still needs CAMERA runtime permission
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }



        return view
    }

    private fun showLogoutDialog(context: Context) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)

        val dialog = AlertDialog.Builder(requireContext(), R.style.CustomAlertDialog)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnNo = dialogView.findViewById<Button>(R.id.btnNo)
        val btnYes = dialogView.findViewById<Button>(R.id.btnYes)

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        btnYes.setOnClickListener {
            // Perform logout action
//            Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()

            startActivity(Intent(context, LoginActivity::class.java))

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openCamera() {
        // Create a temp file in cache/images
        val imagesDir = File(requireContext().cacheDir, "images").apply { if (!exists()) mkdirs() }
        val photoFile = File.createTempFile("capture_", ".jpg", imagesDir)

        // Get content Uri via FileProvider
        val photoUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile
        )
        lastPhotoUri = photoUri

        // Launch camera
        takePictureLauncher.launch(photoUri)
    }

    private fun deleteCachedFileForUri(uri: Uri) {
        // Works for our FileProvider cache path
        try {
            val path = uri.path ?: return
            // path looks like: /cache/images/capture_xxx.jpg (device-dependent)
            // safer: resolve real file from cacheDir
            requireContext().cacheDir.resolve("images").listFiles()?.forEach { f ->
                if (uri.toString().endsWith(f.name)) f.delete()
            }
        } catch (_: Exception) {}
    }

}