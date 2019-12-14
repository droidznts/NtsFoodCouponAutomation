package com.example.foodcouponautomation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import in.myinnos.androidscratchcard.ScratchCard;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class ProfileActivity extends AppCompatActivity implements RatingDialogListener {

    private static final String TAG = ProfileActivity.class.getCanonicalName();
    String phoneNumber;
    TextView mobileNumber;
    TextView redeemButton;
    TextView tvScratch;
    ScratchCard scratch;
    FirebaseFirestore db;
    String uid;
    Calendar currentTimeCal;
    int hourOfTheDay;
    int hour;
    TextView tvTypeOfFood, tvTiming;
    CardView cardCoupon;
    ImageView ivRating;

    String bfStartTime;
    String bfEndTime;

    String lunStartTime;
    String lunEndTime;

    String dinStartTime;
    String dinEndTime;
    TextView tvDate;

    QueryDocumentSnapshot userDetails;
    Button adminOps;
    private final static int BF = 0;
    private final static int LUNCH = 1;
    private final static int DINNER = 2;
    private final static int DEFAULT_NO_COUPON = 2;

    String mealConsumptionQuery = "";

    private int currentTimeFood = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        db = FirebaseFirestore.getInstance();
        currentTimeCal = Calendar.getInstance();
        hourOfTheDay = currentTimeCal.get(Calendar.HOUR_OF_DAY);
        hour = currentTimeCal.get(Calendar.HOUR);


        // get saved phone number
        SharedPreferences prefs = getApplicationContext().getSharedPreferences("USER_PREF",
                Context.MODE_PRIVATE);
        phoneNumber = prefs.getString("phoneNumber", NULL);
        uid = prefs.getString("uid", NULL);

        mobileNumber = findViewById(R.id.mobileNumber);
        mobileNumber.setText(phoneNumber);
        redeemButton = findViewById(R.id.redeemButton);
        scratch = findViewById(R.id.scratch);
        tvTypeOfFood = findViewById(R.id.tvTypeOfFood);
        tvTiming = findViewById(R.id.tvTiming);
        adminOps = findViewById(R.id.adminOps);
        cardCoupon = findViewById(R.id.cardCoupon);
        ivRating = findViewById(R.id.ivRating);
        tvDate = findViewById(R.id.tvDate);
        tvScratch = findViewById(R.id.scratch_text);

        getMealTimeTable();



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
                if (visiblePercent > 0.3) {
                    scratch.setVisibility(View.INVISIBLE);
                    tvScratch.setVisibility(View.INVISIBLE);
                }
            }
        });

        redeemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserDatabase();
//                showDialog();

            }
        });


        ivRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();

            }
        });
    }

    private void getMealTimeTable() {

        db.collection("meal_timetable").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {

                                bfStartTime = document.get("breakfast.start").toString();
                                bfEndTime = document.get("breakfast.end").toString();

                                lunStartTime = document.get("lunch.start").toString();
                                lunEndTime = document.get("lunch.end").toString();

                                dinStartTime = document.get("dinner.start").toString();
                                dinEndTime = document.get("dinner.end").toString();

                                try {
                                    updateCouponAccordingTime();


                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Something went wrong", Toast.LENGTH_LONG);
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void updateCouponAccordingTime() throws ParseException {


        int month = currentTimeCal.get(Calendar.MONTH) + 1;
        tvDate.setText(currentTimeCal.get(Calendar.DAY_OF_MONTH)+"/"+month+"/"+currentTimeCal.get(Calendar.YEAR));
        if(currentTimeCal.get(Calendar.AM_PM) == Calendar.PM){
            tvTiming.setText((hour+":"+currentTimeCal.get(Calendar.MINUTE) +" PM"));

        }else {
            tvTiming.setText((hour+":"+currentTimeCal.get(Calendar.MINUTE)+" AM"));
        }


        if(hourOfTheDay>=7 && hourOfTheDay<=9){

            tvTypeOfFood.setText("Breakfast");
            cardCoupon.setCardBackgroundColor(getResources().getColor(R.color.bf_blue));
            currentTimeFood = BF;
            mealConsumptionQuery = "bf_coupn";
            redeemButton.setVisibility(View.VISIBLE);


        }else if(hourOfTheDay >= 12 && hourOfTheDay <= 15){
            tvTypeOfFood.setText("Lunch");
            cardCoupon.setCardBackgroundColor(getResources().getColor(R.color.lunch_yellow));

            currentTimeFood = LUNCH;
            mealConsumptionQuery = "lun_coupn";
            redeemButton.setVisibility(View.VISIBLE);



        }else if(hourOfTheDay >= 20 && hourOfTheDay <= 23){
            tvTypeOfFood.setText("Dinner");
            cardCoupon.setCardBackgroundColor(getResources().getColor(R.color.dinner_pink));

            currentTimeFood = DINNER;
            mealConsumptionQuery = "dine_coupn";
            redeemButton.setVisibility(View.VISIBLE);


        }else {
            tvTypeOfFood.setText("No Coupon");
            redeemButton.setVisibility(View.INVISIBLE);
            currentTimeFood = DEFAULT_NO_COUPON;

        }


        getUserDetails();







    }


    private boolean isTimeBetween(String argStartTime,
                                  String argEndTime) throws ParseException {

        SimpleDateFormat time_format = new SimpleDateFormat("HH:mm:ss");
        String argCurrentTime = time_format.format(Calendar.getInstance().getTime());

        String reg = "^([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";

        if (argStartTime.matches(reg) && argEndTime.matches(reg)) {
            boolean valid = false;

            java.util.Date startTime = new SimpleDateFormat("HH:mm:ss")
                    .parse(argStartTime);
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(startTime);

            java.util.Date currentTime = new SimpleDateFormat("HH:mm:ss")
                    .parse(argCurrentTime);
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(currentTime);

            java.util.Date endTime = new SimpleDateFormat("HH:mm:ss")
                    .parse(argEndTime);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(endTime);

            //
            if (currentTime.compareTo(endTime) < 0) {

                currentCalendar.add(Calendar.DATE, 1);
                currentTime = currentCalendar.getTime();

            }

            if (startTime.compareTo(endTime) < 0) {

                startCalendar.add(Calendar.DATE, 1);
                startTime = startCalendar.getTime();

            }
            //
            if (currentTime.before(startTime)) {

                System.out.println(" Time is Lesser ");

                valid = false;
            } else {

                if (currentTime.after(endTime)) {
                    endCalendar.add(Calendar.DATE, 1);
                    endTime = endCalendar.getTime();

                }

                if (currentTime.before(endTime)) {
                    valid = true;
                } else {
                    valid = false;
                }

            }
            return valid;

        } else {
            throw new IllegalArgumentException(
                    "Not a valid time, expecting HH:MM:SS format");
        }
    }


    private void getUserDetails() {

        db.collection("users").whereEqualTo("uid", uid).get()
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

                                    /*    if((Long) document.getData().get("bf_coupn") == 0){
                                            redeemButton.setVisibility(View.INVISIBLE);

                                        }*/
                                        boolean isCouponPresent = (Long) document.getData().get("bf_coupn") > 0;
                                        redeemButton.setVisibility(isCouponPresent ? View.VISIBLE : View.INVISIBLE);


                                    }else if(currentTimeFood == LUNCH)
                                    {
                                        boolean isCouponPresent = (Long) document.getData().get("lun_coupn") > 0;
                                        redeemButton.setVisibility(isCouponPresent ? View.VISIBLE : View.INVISIBLE);
                                        /*if((Long) document.getData().get("lun_coupn") == 0){
                                            redeemButton.setVisibility(View.INVISIBLE);
                                        }*/

                                    }else if(currentTimeFood == DINNER){
                                        boolean isCouponPresent = (Long) document.getData().get("dine_coupn") > 0;
                                        redeemButton.setVisibility(isCouponPresent ? View.VISIBLE : View.INVISIBLE);
                                        /*if((Long) document.getData().get("dine_coupn") == 0){
                                            redeemButton.setVisibility(View.INVISIBLE);
                                        }*/

                                    }/*else {

                                            redeemButton.setVisibility(View.INVISIBLE);
                                    }*/


                                }


                            db.collection("meal_consumption_details").whereEqualTo("uid",uid).get()
                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                            if(task.isSuccessful()){


                                                for (QueryDocumentSnapshot document : task.getResult()) {

                                                    boolean isCouponAvailable = false;

                                                    if(document.get(mealConsumptionQuery).toString().equalsIgnoreCase("false")){
                                                        redeemButton.setVisibility(View.VISIBLE);
                                                    }else {
                                                        redeemButton.setVisibility(View.INVISIBLE);

                                                    }





//                                                    Log.i(TAG, " meal consumption" + document.get(mealConsumptionQuery).toString());
                                                    Log.i(TAG, " meal consumption" + document.get("lun_coupn").toString());
                                                    Log.i(TAG, " dine consumption" + document.get("dine_coupn").toString());


                                                    Log.i(TAG, " current time " + currentTimeCal.get(Calendar.DAY_OF_MONTH));
                                                    Log.i(TAG, " current time " + currentTimeCal.get(Calendar.MONTH));
                                                    Log.i(TAG, " current time " + currentTimeCal.get(Calendar.YEAR));


                                                }
                                            }
                                        }
                                    });



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

       /* db.collection("meal_consumption_details").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                queryDocumentSnapshots.getDocuments().get(0)
            }
        });*/


        final CollectionReference meal_consumption_details = db.collection("meal_consumption_details");
        meal_consumption_details.whereEqualTo("uid",uid).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){


                            for (QueryDocumentSnapshot document : task.getResult()) {


                                boolean isCouponUsed = false;

                                final CollectionReference users = db.collection("users");
                                final CollectionReference mealConsumption = db.collection("users");
                                Map<String, Object> mealInfo = new HashMap<>();

                                mealInfo.put(mealConsumptionQuery,true);

                                meal_consumption_details.document(document.getId()).update(mealInfo);



                                users.whereEqualTo("uid",uid).get().addOnCompleteListener(
                                        new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                                                if(task.isSuccessful()){

                                                    for (QueryDocumentSnapshot document : task.getResult()) {

                                                        final CollectionReference mealConsumption = db.collection("users");
                                                        Map<String, Object> updateUSerMeal = new HashMap<>();

                                                        updateUSerMeal.put(mealConsumptionQuery, ((Long)document.get(mealConsumptionQuery)-1) );

                                                        users.document(document.getId()).update(updateUSerMeal);
                                                    }
                                                }
                                            }
                                        }
                                );




                                redeemButton.setVisibility(View.INVISIBLE);



//                                document.get(mealConsumptionQuery).toString().equalsIgnoreCase("false");

//                                document.get(mealConsumptionQuery)
//                                document.getId()
                                Log.i(TAG, " meal consumption" + document.get(mealConsumptionQuery));
                                Log.i(TAG, " meal consumption" + document.get("lun_coupn"));
                            }
                        }
                    }
                });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signout,menu);
        return true;
    }
}
