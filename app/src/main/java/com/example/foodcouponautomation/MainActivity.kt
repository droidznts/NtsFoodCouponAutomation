package com.example.foodcouponautomation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.firestore.FirebaseFirestore



class MainActivity : AppCompatActivity() {

    var TAG = MainActivity::class.java.canonicalName
    val db = FirebaseFirestore.getInstance()
    var isValidMobileNumber = false
    var list:List<User>  = ArrayList<User>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        db.collection("users")
            .get()
            .addOnCompleteListener {
                    list ->
                if(list.isSuccessful){

                    var documents = list.result?.documents

                    Log.i(TAG,"vdshcvh");
                    Log.i(TAG, list.result?.documents?.size.toString());
                    Log.i(TAG, list.result?.documents?.get(0)?.id);
                    Log.i(TAG, list.result?.documents?.get(0)?.get("mob")?.toString());

                    isValidMobileNumber = list.result?.documents?.get(0)?.get("mob")?.toString().equals(etMobileNumber.text.toString())

                }else{
                    Log.i(TAG,"cancel");
                }

            }


        submitButton.setOnClickListener {

            if(isUserAuthenticated()){
                startActivity(Intent(this,CouponsActivity::class.java))
            }

        }





    }

    private fun isUserAuthenticated(): Boolean {


        Log.i(TAG, isValidMobileNumber.toString());
        Log.i(TAG, etMobileNumber.text.toString());


        return isValidMobileNumber
    }
}
