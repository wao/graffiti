package info.thinkmore.android.graffiti;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioGroup;

import org.androidannotations.annotations.*;
//import butterknife.*;
import com.google.java.contract.Requires;

@EActivity(R.layout.activity_main)
public class MainActivity
    extends Activity
{

    Handler handler = new Handler();

    /*@InjectView(R.id.drawing)*/
    @ViewById(R.id.drawing)
    DrawingView drawingView;

    @ViewById(R.id.radiogroup_color)
    RadioGroup rgColor;

    @Requires("x>3")
    void testCofoja(int x){
    }



    // Called at the start of the full lifetime.
    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize activity.
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        testCofoja(1);
        
        if( Build.VERSION.SDK_INT < 16 ){
            getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN );
        }
        else{
            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener( new View.OnSystemUiVisibilityChangeListener(){
                @Override
                public void onSystemUiVisibilityChange(int visibility){
                    int flags = 0;
                    if( (visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0 ){
                        flags |= View.SYSTEM_UI_FLAG_FULLSCREEN;
                    }

                    if( (visibility & View.SYSTEM_UI_FLAG_HIDE_NAVIGATION) == 0 ){
                        Log.v( "VISIBILITY", "Need to hide navigation");
                        flags |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                    }

                    if( flags != 0 ){
                        handler.postDelayed( new Runnable(){
                            @Override
                            public void run(){
                                enableFullScreen();
                            }
                        }, 2000 );
                    }
                }
            });
        }

        setContentView(R.layout.activity_main);
        //ButterKnife.inject(this);
        //afterViews();
    }

    @SuppressLint("NewApi")
    void enableFullScreen(){
        if( Build.VERSION.SDK_INT >= 16 ){
            getWindow().getDecorView().setSystemUiVisibility( 
                    View.SYSTEM_UI_FLAG_FULLSCREEN 
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    );
        }
    }

    @Override
    public void onResume(){
        super.onResume();
       
        enableFullScreen();
    }
    
    // Called after onCreate has finished, use to restore UI state
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // Restore UI state from the savedInstanceState.
        // This bundle has also been passed to onCreate.
    }
    
    // Called to save UI state changes at the end of the active lifecycle.
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save UI state changes to the savedInstanceState.
        // This bundle will be passed to onCreate if the process is
        // killed and restarted.  
        super.onSaveInstanceState(savedInstanceState); 
    }
    

    @AfterViews
    void afterViews() {
        rgColor.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged( RadioGroup rg, int checkedId ){
                switch( checkedId ){
                    case R.id.rb_black:
                        drawingView.setPaintColor( Color.BLACK );
                        break;
                    case R.id.rb_green:
                        drawingView.setPaintColor( Color.GREEN );
                        break;
                    case R.id.rb_red:
                        drawingView.setPaintColor( Color.RED );
                        break;
                }
            }
        } );

        drawingView.setPaintColor( Color.RED );
        rgColor.check( R.id.rb_red );
    }

    //@OnClick(R.id.btn_clear)
    @Click(R.id.btn_clear)
    public void onBtnClearClicked(View clickedView){
        drawingView.clearCanvas();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater();
        return true;
    }

}
