package info.thinkmore.android.graffiti;

import android.app.Activity;
import android.graphics.Color;
import android.widget.RadioButton;

import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(manifest="./AndroidManifest.xml",emulateSdk=18)
public class MainActivityTest {

    void clickAndCheckColor( Activity activity, DrawingView dv, int buttonId, int color ){
        RadioButton rb_btn = (RadioButton) activity.findViewById( buttonId );
        rb_btn.performClick();
        assertEquals( color, dv.getPaintColor() );
    }

  @org.junit.Test
  public void changeColor() throws Exception {
    Activity activity = Robolectric.setupActivity(MainActivity_.class);

    DrawingView dv = (DrawingView) activity.findViewById( R.id.drawing );
    assertEquals( Color.RED, dv.getPaintColor() );

    clickAndCheckColor( activity, dv, R.id.rb_black, Color.BLACK );
    clickAndCheckColor( activity, dv, R.id.rb_red, Color.RED );
    clickAndCheckColor( activity, dv, R.id.rb_green, Color.GREEN );
  }
}
