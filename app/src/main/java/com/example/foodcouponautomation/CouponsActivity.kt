package com.example.foodcouponautomation

import `in`.myinnos.androidscratchcard.ScratchCard
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.android.synthetic.main.activity_coupons.*


class CouponsActivity: AppCompatActivity() {


    var alert: AlertDialog.Builder? = null
    val dialog: AlertDialog? = null
    var TAG = CouponsActivity::class.java.canonicalName

    val db = FirebaseFirestore.getInstance()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        alert = AlertDialog.Builder(this@CouponsActivity)

        val inflater = layoutInflater

        val alertLayout = inflater.inflate(R.layout.layout_custom_dialog, null)


//        var letsEatButton = alertLayout.findViewById<TextView>(R.id.btnLetsEat)

        alert!!.setTitle("Enter your Employee Id")
        // this is set the view from XML inside AlertDialog
        alert!!.setView(alertLayout)
        // disallow cancel of AlertDialog on click of back button and outside touch
        alert!!.setCancelable(false)

        setContentView(R.layout.activity_coupons)

        scratch.setOnScratchListener(object : ScratchCard.OnScratchListener {
            override fun onScratch(scratchCard: ScratchCard?, visiblePercent: Float) {

                if (visiblePercent > 0.3){
                    Toast.makeText(this@CouponsActivity,"Revealed",Toast.LENGTH_SHORT).show()

                    scratch.visibility = View.GONE
                }
            }

        })


        alert!!.setPositiveButton("Lets eat", object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {

                if (dialog != null) {
                    dialog.dismiss()

                    redeemButton.visibility = View.GONE


                    db.collection("meal_consumption_details")
                        .get()
                        .addOnCompleteListener {
                                snapShot ->
                            if(snapShot.isSuccessful){
//                                var documentsList =


                                updateUserData(snapShot)

                        }

                }
            }
            }
        })


        redeemButton.setOnClickListener {

            val dialog = alert!!.create()
            dialog.show()
        }

       /* letsEatButton.setOnClickListener {
            if (dialog != null) {
                dialog.dismiss()
                Toast.makeText(this@CouponsActivity,"Reveal",Toast.LENGTH_SHORT).show()
            }
        }*/


    }

    private fun updateUserData(snapShot: Task<QuerySnapshot>) {


        var documents = snapShot.result?.documents

        if (documents != null) {
            Log.i(TAG, documents.get(0).get("emp_id").toString());



            db.collection("users")
                .get()
                .addOnCompleteListener {

                }

        }


    }
}