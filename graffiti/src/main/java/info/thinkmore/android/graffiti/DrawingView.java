package info.thinkmore.android.graffiti;

import java.util.Arrays;
import java.util.List;

//import lombok.Getter;
//import lombok.Setter;

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

public class DrawingView extends View {
    static class Line{
        float[] points;
        int color;

        private Line(float[] datas, int length, int color ){
            points = Arrays.copyOf( datas, length );
            this.color = color;
        }

        public static Line newInstance(float[] datas, int length, int color ){
            return new Line( datas, length, color );
        }
        
        public Line draw( Canvas canvas, Paint paint ){
            paint.setColor( color );
            canvas.drawLines( points, 0, points.length, paint );
            return this;
        }
    }


    static class Lines{
        final public static int MAX_POINT_NUM_PER_LINE = 4000;
        List<Line> lines = Lists.newArrayList();

        float[] points = new float[MAX_POINT_NUM_PER_LINE];

        int lastIndex = 0;

        //@Getter @Setter int currentColor;
        int currentColor;

        public void setCurrentColor(int value){
            currentColor = value;
        }

        public int getCurrentColor(){
            return currentColor;
        }

        Lines(int currentColor ){
            this.currentColor = currentColor;
        }

        Lines(){
            this( Color.BLACK );
        }

        Lines clear(){
            lastIndex = 0;
            lines.clear();
            return this;
        }

        Lines newLine(){
            lines.add( Line.newInstance( points, lastIndex, currentColor ) );
            lastIndex = 0;
            return this;
        }

        Lines add( float value ){
            if( lastIndex >= 4000 ){
                newLine();
            }

            points[lastIndex++] = value;
            return this;
        }

        Lines drawLines( Canvas canvas, Paint paint ){
            for( Line p : lines ){
                p.draw( canvas, paint );
            }

            paint.setColor( currentColor );

            canvas.drawLines( points, 0, lastIndex, paint );
            return this;
        }
    }


    Lines lines = new Lines();

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


    final static String SUPERSTATE = "SUPERSTATE";
    @Override
    public Parcelable onSaveInstanceState() {
        Bundle state=new Bundle();
        state.putParcelable(SUPERSTATE, super.onSaveInstanceState()); 
        //state.putInt(COLOR, getColor());

        return(state);
    }

    @Override
    public void onRestoreInstanceState(Parcelable ss) {
        Bundle state=(Bundle)ss;
        super.onRestoreInstanceState(state.getParcelable(SUPERSTATE));

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


                    lines.add( mLastX );
                    lines.add( mLastY );
                    lines.add( x );
                    lines.add( y );

                    mLastX = x;
                    mLastY = y;
                }
                break;

            case MotionEvent.ACTION_UP:
                {
                    final float x = event.getX();
                    final float y = event.getY();

                    lines.add( mLastX );
                    lines.add( mLastY );
                    lines.add( x );
                    lines.add( y );

                    lines.newLine();
                    //points.add( x );
                    //points.add( y );
                }
                break;
        }

        invalidate();
                                             
        return true;
    }

    public void setPaintColor(int color){
        lines.setCurrentColor( color );
    }

    public int getPaintColor(){
        return lines.getCurrentColor();
    }

    public void clearCanvas(){
        lines.clear();
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw( canvas );
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor( Color.GREEN );
        paint.setStrokeWidth(10);
        lines.drawLines( canvas, paint );
    }
}
