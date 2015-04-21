package info.thinkmore.android.graffiti;

import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.google.common.collect.*;

class DrawingView extends View {
    Path total_path = new Path();

    static class Points{
        List<float[]> pointsList = Lists.newArrayList();
        float[] points =  new float[4000];

        int lastIndex = 0;

        Points add( float value ){
            if( lastIndex >= 4000 ){
                pointsList.add( points );
                points = new float[4000];
                lastIndex = 0;
            }

            points[lastIndex++] = value;
            return this;
        }

        Points drawLines( Canvas canvas, Paint paint ){
            for( float[] p : pointsList ){
                canvas.drawLines( p, paint );
            }

            canvas.drawLines( points, 0, lastIndex, paint );
            return this;
        }
    }

    Points points = new Points();

    float mLastX;
    float mLastY;

    public DrawingView(Context context) {
        this( context, null );
    }

    public DrawingView(Context context, AttributeSet attrs){
        this( context, attrs, 0 );
    }

    public DrawingView(Context context, AttributeSet attrs, int defStyle){
        super( context, attrs, defStyle );

        //((Activity)getContext()).getLayoutInflater().inflate(R.layout.your_layout_id, this, true );

        /*
           if( attrs != null ){
           TypedArray a=getContext().obtainStyledAttributes(attrs, R.styleable.DrawingView, 0, 0);
        //setColor(a.getInt(R.styleable.DrawingView_initialColor, 0xFFA4C639));
        a.recycle();
           }
           */
    }


    @Override
    public Parcelable onSaveInstanceState() {
        Bundle state=new Bundle();
        //state.putParcelable(SUPERSTATE, super.onSaveInstanceState()); 
        //state.putInt(COLOR, getColor());

        return(state);
    }

    @Override
    public void onRestoreInstanceState(Parcelable ss) {
        //Bundle state=(Bundle)ss;
        //super.onRestoreInstanceState(state.getParcelable(SUPERSTATE));

        //setColor(state.getInt(COLOR));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        final int action = event.getAction();
        switch(action) {
            case MotionEvent.ACTION_DOWN:
                {
                    final float x = event.getX();
                    final float y = event.getY();

                    mLastX = x;
                    mLastY = y;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                {
                    final float x = event.getX();
                    final float y = event.getY();


                    points.add( mLastX );
                    points.add( mLastY );
                    points.add( x );
                    points.add( y );

                    mLastX = x;
                    mLastY = y;
                }
                break;

            case MotionEvent.ACTION_UP:
                {
                    final float x = event.getX();
                    final float y = event.getY();

                    mLastX = x;
                    mLastY = y;

                    //points.add( x );
                    //points.add( y );
                }
                break;
        }

        invalidate();
                                             
        return true;
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw( canvas );
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor( Color.GREEN );
        paint.setStrokeWidth(3);
        points.drawLines( canvas, paint );
    }
}
