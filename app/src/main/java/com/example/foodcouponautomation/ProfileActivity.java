package com.example.foodcouponautomation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import java.util.Calendar;
import java.util.Date;

import in.myinnos.androidscratchcard.ScratchCard;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class ProfileActivity extends AppCompatActivity implements RatingDialogListener {

    private static final String TAG = ProfileActivity.class.getCanonicalName();
    String phoneNumber;
    TextView mobileNumber;
    TextView redeemButton;
    ScratchCard scratch;
    FirebaseFirestore db;
    String uid;
    Calendar currentTimeCal;
    int hourOfTheDay;
    int hour;
    TextView tvTypeOfFood,tvTiming;
    CardView cardCoupon;

    QueryDocumentSnapshot userDetails;
    Button adminOps;
    private final static int BF = 0;
    private final static int LUNCH = 1;
    private final static int DINNER = 2;
    private final static int DEFAULT_NO_COUPON = 2;

    private int currentTimeFood = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        db = FirebaseFirestore.getInstance();
        currentTimeCal = Calendar.getInstance();
       hourOfTheDay  = currentTimeCal.get(Calendar.HOUR_OF_DAY);
       hour  = currentTimeCal.get(Calendar.HOUR);

        // get saved phone number
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("USER_PREF",
                Context.MODE_PRIVATE);
        phoneNumber = prefs.getString("phoneNumber", NULL);
        uid = prefs.getString("uid",NULL);

        mobileNumber = findViewById(R.id.mobileNumber);
        mobileNumber.setText(phoneNumber);
        redeemButton = findViewById(R.id.redeemButton);
        scratch = findViewById(R.id.scratch);
        tvTypeOfFood  =findViewById(R.id.tvTypeOfFood);
        tvTiming = findViewById(R.id.tvTiming);
        adminOps = findViewById(R.id.adminOps);
        cardCoupon = findViewById(R.id.cardCoupon);


//        Log.i(TAG +" Tag", String.valueOf(currentTime.get(Calendar.HOUR)));
        Log.i(TAG +" Tag", String.valueOf(currentTimeCal.get(Calendar.HOUR_OF_DAY)));
        Log.i(TAG +" Tag", String.valueOf(currentTimeCal.get(Calendar.AM_PM)));
        Log.i(TAG +" Tag", String.valueOf(currentTimeCal.get(Calendar.HOUR)));
        Log.i(TAG +" Tag", String.valueOf(currentTimeCal.get(Calendar.HOUR_OF_DAY)));


        updateCouponAccordingTime();


        getUserDetails();



        findViewById(R.id.buttonLogout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });

        scratch.setOnScratchListener(new ScratchCard.OnScratchListener() {
            @Override
            public void onScratch(ScratchCard scratchCard, float visiblePercent) {
                if(visiblePercent > 0.3){
                    scratch.setVisibility(View.INVISIBLE);
                }
            }
        });

        redeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserDatabase();
                showDialog();

            }
        });
    }

    private void updateCouponAccordingTime() {

        if(currentTimeCal.get(Calendar.AM_PM) == Calendar.PM){
            tvTiming.setText((hour+":"+ Calendar.MINUTE+" PM"));

        }else {
            tvTiming.setText((hour+":"+Calendar.MINUTE+" AM"));
        }


        if(hourOfTheDay>=7 && hourOfTheDay<=9){

            tvTypeOfFood.setText("Breakfast");
            cardCoupon.setCardBackgroundColor(getResources().getColor(R.color.bf_blue));
            currentTimeFood = BF;

        }else if(hourOfTheDay >= 12 && hourOfTheDay <= 15){
            tvTypeOfFood.setText("Lunch");
            cardCoupon.setCardBackgroundColor(getResources().getColor(R.color.lunch_yellow));

            currentTimeFood = LUNCH;


        }else if(hourOfTheDay >= 20 && hourOfTheDay <= 23){
            tvTypeOfFood.setText("Dinner");
            cardCoupon.setCardBackgroundColor(getResources().getColor(R.color.dinner_pink));

            currentTimeFood = DINNER;

        }else {
            tvTypeOfFood.setText("No Coupon");
            redeemButton.setVisibility(View.INVISIBLE);
            currentTimeFood = DEFAULT_NO_COUPON;

        }


    }

    private void getUserDetails() {

/*
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {

                        if(task.isSuccessful()){


                            if(hourOfTheDay>=7 && hourOfTheDay<=9){

                                tvTypeOfFood.setText("Breakfast");

                            }else if(hourOfTheDay >= 12 && hourOfTheDay <= 3){
                                tvTypeOfFood.setText("Lunch");


                            }else if(hourOfTheDay >= 20 && hourOfTheDay <= 23){
                                tvTypeOfFood.setText("Dinner");


                            }else {
                                tvTypeOfFood.setText("Sorry You have no coupon for now");
                            }


                            Log.i(TAG, String.valueOf(task.getResult().getDocuments().size()));
                            Log.i(TAG +" Tag", String.valueOf(currentTimeCal.get(Calendar.HOUR_OF_DAY)));

                        }

                    }
                });*/
        db.collection("users").whereEqualTo("uid",uid).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){

                                for (QueryDocumentSnapshot document : task.getResult()) {


                                    userDetails = document;
                                    Log.i(TAG +" Tag ", String.valueOf(document.getData()));
                                    Log.i(TAG +" Tag lunch", String.valueOf(document.getData().get("lun_coupn")));
                                    Log.i(TAG +" Tag dinner", String.valueOf(document.getData().get("dine_coupn")));
                                    Log.i(TAG +" Tag uid", String.valueOf(document.getData().get("uid")));


                                    if(!document.getData().get("role").equals("admin")){
                                        adminOps.setVisibility(View.INVISIBLE);
                                    }


                                    if(currentTimeFood == BF){

                                        if((Long) document.getData().get("bf_coupn") == 0){
                                            redeemButton.setVisibility(View.GONE);
                                        }

                                    }else if(currentTimeFood == LUNCH)
                                    {
                                        if((Long) document.getData().get("lun_coupn") == 0){
                                            redeemButton.setVisibility(View.GONE);
                                        }

                                    }else if(currentTimeFood == DINNER){
                                        if((Long) document.getData().get("dine_coupn") == 0){
                                            redeemButton.setVisibility(View.GONE);
                                        }

                                    }else {

                                            redeemButton.setVisibility(View.GONE);
                                    }






                                }









                                Log.i(TAG, String.valueOf(task.getResult().getDocuments().size()));
                                Log.i(TAG +" Tag", String.valueOf(currentTimeCal.get(Calendar.HOUR_OF_DAY)));
                                Log.i(TAG +" Tag", String.valueOf(task.getResult()));
                                Log.i(TAG +" Tag", String.valueOf(task.getResult().getDocuments()));
                                Log.i(TAG +" Tag", String.valueOf(task.getResult().getQuery().get()));
                                Log.i(TAG +" Tag", String.valueOf(task.getResult().getDocuments()));
//                                Log.i(TAG +" Tag", String.valueOf(task.getResult().getDocuments().get(0)));


                        }
                    }
                });
    }


    private void showDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNeutralButtonText("Later")
                .setNoteDescriptions(Arrays.asList("Very Bad", "Not good", "Quite ok", "Very Good", "Excellent !!!"))
                .setDefaultRating(2)
                .setTitle("Rate Your Meal")
                .setDescription("Please select some stars and give your feedback")
                .setCommentInputEnabled(true)
                .setStarColor(R.color.starColor)
                .setNoteDescriptionTextColor(R.color.noteDescriptionTextColor)
                .setTitleTextColor(R.color.titleTextColor)
                .setDescriptionTextColor(R.color.contentTextColor)
                .setHint("Please write your comment here ...")
                .setHintTextColor(R.color.hintTextColor)
                .setCommentTextColor(R.color.commentTextColor)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .create(ProfileActivity.this)
                .show();
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int i, @NotNull String s) {


    }

    private void updateUserDatabase() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signout,menu);
        return true;
    }
}
