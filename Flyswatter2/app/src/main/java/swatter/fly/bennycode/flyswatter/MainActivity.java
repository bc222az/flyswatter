package swatter.fly.bennycode.flyswatter;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends ActionBarActivity implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    public Vibrator v;
    private float mAccelNoGrav;
    private float mAccelWithGrav;
    private float mLastAccelWithGrav;
    private static final String SWATTER = "swatter";
    private static final String HIT_COUNT = "hitCount";
    private static final String KILLSCORE = "killscore";
    private static final int HITS_TO_UNLOCK_BASEBALLBAT = 150;
    MediaPlayer mp;
    private int AMOUNT_OF_SWATTERS = 2; //the amount of different swatters starts on zero
    private Timer flyTimer1;
    private Timer flyTimer2;
    private Timer flyTimer3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        mAccelNoGrav = 0.00f;
        mAccelWithGrav = SensorManager.GRAVITY_EARTH;
        mLastAccelWithGrav = SensorManager.GRAVITY_EARTH;

        setPlusMinusOnClicks();
        typeOutKillScore();
        setArrowOnClicks();
        setSwatter();
        setSwipeListener();
        setTimerForFly();
        setCommercial();
        checkIfBaseballBatIsUnlocked();

    }
    private void setCommercial(){
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }
    private void setTimerForFly(){
        if(flyTimer1 != null){
            flyTimer1.cancel();
        }
        if(flyTimer2 != null){
            flyTimer1.cancel();
        }
        if(flyTimer3 != null){
            flyTimer1.cancel();
        }
        flyTimer1 = new Timer();
        flyTimer2 = new Timer();
        flyTimer3 = new Timer();
        flyTimer1.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView animatedFly = (ImageView)findViewById(R.id.animateFly1);
                        animatedFly.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 60000); //one minute
        flyTimer2.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView animatedFly = (ImageView)findViewById(R.id.animateFly2);
                        animatedFly.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 60000*2); //two minutes
        flyTimer3.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ImageView animatedFly = (ImageView)findViewById(R.id.animateFly3);
                        animatedFly.setVisibility(View.VISIBLE);
                    }
                });
            }
        }, 60000*3); //three minutes
    }
    private void hideFly(){
        ImageView animatedFly1 = (ImageView)findViewById(R.id.animateFly1);
        animatedFly1.setVisibility(View.INVISIBLE);
        ImageView animatedFly2 = (ImageView)findViewById(R.id.animateFly2);
        animatedFly2.setVisibility(View.INVISIBLE);
        ImageView animatedFly3 = (ImageView)findViewById(R.id.animateFly3);
        animatedFly3.setVisibility(View.INVISIBLE);
        setTimerForFly();
    }
    private void setSwipeListener(){
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        imageView.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()) {
            @Override
            public void onSwipeLeft() {
                changeSwatter(-1);
            }
            @Override
            public void onSwipeRight() {
                changeSwatter(1);
            }
        });
    }
    private void setPlusMinusOnClicks(){
        ImageButton plusButton = (ImageButton)findViewById(R.id.plusButton);
        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addOneToKillScore();
            }
        });
        ImageButton minusButton = (ImageButton)findViewById(R.id.minusButton);
        minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                extractOneFromKillScore();
            }
        });
    }
    private void setArrowOnClicks(){
        ImageButton leftArrow = (ImageButton)findViewById(R.id.leftArrowButton);
        leftArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSwatter(-1);
            }
        });
        ImageButton rightArrow = (ImageButton)findViewById(R.id.rightArrowButton);
        rightArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeSwatter(1);
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];
        mLastAccelWithGrav = mAccelWithGrav;
        mAccelWithGrav = android.util.FloatMath.sqrt(x * x + y * y + z * z);
        float delta = mAccelWithGrav - mLastAccelWithGrav;
        mAccelNoGrav = mAccelNoGrav * 0.9f + delta;
        if (mAccelNoGrav > 7) {
            hideFly();
            addHitCount();
            mp.start();
        }
    }
    private void typeOutKillScore(){
        TextView killScoreValue = (TextView)findViewById(R.id.killScoreValue);
        killScoreValue.setText(Integer.toString(getKillScore()));
    }
    private void addOneToKillScore(){
        int newScore = getKillScore() + 1;
        SharedPreferences.Editor editor = getSharedPreferences(KILLSCORE, MODE_PRIVATE).edit();
        editor.putInt(KILLSCORE, newScore);
        editor.commit();
        typeOutKillScore();
    }
    private void extractOneFromKillScore(){
        if(getKillScore() > 0){
            SharedPreferences.Editor editor = getSharedPreferences("killscore", MODE_PRIVATE).edit();
            editor.putInt("killscore", getKillScore() + -1);
            editor.commit();
            typeOutKillScore();
        }
    }
    private int getKillScore(){
        SharedPreferences prefs = getSharedPreferences(KILLSCORE, MODE_PRIVATE);
        return prefs.getInt(KILLSCORE, 0); //0 is the default value.
    }
    private void setSwatter(){
        ImageView imageView = (ImageView)findViewById(R.id.imageView);
        int swatterNumber = getCurrentSwatter();
        //if adding more swatters +1 on "AMOUNT_OF_SWATTERS"
        switch(swatterNumber){
            case 0 : imageView.setImageResource(R.drawable.fly_swatter);
                mp = MediaPlayer.create(getApplicationContext(), R.raw.punch_sound1);
                break;
            case 1 : imageView.setImageResource(R.drawable.newspaper);
                mp = MediaPlayer.create(getApplicationContext(), R.raw.light_punch);
                break;
            case 2 : imageView.setImageResource(R.drawable.electricflyswatter);
                mp = MediaPlayer.create(getApplicationContext(), R.raw.electric_sound);
                break;
            case 3 : imageView.setImageResource(R.drawable.baseballbat);
                mp = MediaPlayer.create(getApplicationContext(), R.raw.hard_punch);
                break;
            default:imageView.setImageResource(R.drawable.fly_swatter);
                mp = MediaPlayer.create(getApplicationContext(), R.raw.punch_sound1);
                break;
        }
    }
    private void changeSwatter(int change){
        int newSwatter = getCurrentSwatter() + change;
        if(newSwatter < 0){
            newSwatter = AMOUNT_OF_SWATTERS;
        }else if(newSwatter > AMOUNT_OF_SWATTERS){
            newSwatter = 0;
        }
        SharedPreferences.Editor editor = getSharedPreferences(SWATTER, MODE_PRIVATE).edit();
        editor.putInt(SWATTER, newSwatter);
        editor.commit();
        setSwatter();
    }
    private int getCurrentSwatter(){
        SharedPreferences prefs = getSharedPreferences(SWATTER, MODE_PRIVATE);
        return prefs.getInt(SWATTER, 0); //0 is the default value.
    }
    private void checkIfBaseballBatIsUnlocked(){
        if(getCurrentHitCount() > HITS_TO_UNLOCK_BASEBALLBAT){
            AMOUNT_OF_SWATTERS += 1;
        }
        if(getCurrentHitCount() > HITS_TO_UNLOCK_BASEBALLBAT && getCurrentHitCount() < HITS_TO_UNLOCK_BASEBALLBAT + 25){
            Toast.makeText(getApplicationContext(),"Tried the new baseball bat?",Toast.LENGTH_SHORT).show();
        }
    }
    private void addHitCount(){
        int newHitCount = getCurrentHitCount() + 1;
        SharedPreferences.Editor editor = getSharedPreferences(SWATTER, MODE_PRIVATE).edit();
        editor.putInt(HIT_COUNT, newHitCount);
        editor.commit();
        if(newHitCount == HITS_TO_UNLOCK_BASEBALLBAT){
            Toast.makeText(getApplicationContext(),"Baseball Bat unlocked",Toast.LENGTH_LONG).show();
            AMOUNT_OF_SWATTERS += 1;
        }
    }
    private int getCurrentHitCount(){
        SharedPreferences prefs = getSharedPreferences(SWATTER, MODE_PRIVATE);
        return prefs.getInt(HIT_COUNT, 0); //0 is the default value.
    }
}
