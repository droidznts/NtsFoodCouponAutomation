package com.example.foodcouponautomation

import `in`.myinnos.androidscratchcard.ScratchCard
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_coupons.*

class CouponsActivity: AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_coupons)

        scratch.setOnScratchListener(object : ScratchCard.OnScratchListener {
            override fun onScratch(scratchCard: ScratchCard?, visiblePercent: Float) {

                if (visiblePercent > 0.3){
                    Toast.makeText(this@CouponsActivity,"Revealed",Toast.LENGTH_SHORT).show()
                }
            }

        })
    }
}