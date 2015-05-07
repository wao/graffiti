package info.thinkmore.android.graffiti;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.common.collect.*;
import com.google.java.contract.Invariant;
import com.google.java.contract.Requires;

public class DrawingView extends View {
    final static String TAG = "DrawingView";
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

    final public static int INIT_POINT_NUM_PER_LINE = 4000;

    @Invariant("lastIndex < points.length")
    static class Points{
        float[] points = new float[INIT_POINT_NUM_PER_LINE];
        int lastIndex = 0;

        Points reset(){
            lastIndex = 0;
            return this;
        }

        void enlargePointsArrayIfNeeded(){
            if( lastIndex + 4 >= points.length ){
                float[] newPoints = Arrays.copyOf( points, points.length * 2 );
                points = newPoints;
            }
        }

        @Requires("( lastIndex >= 2 ) && ( lastIndex % 4 == 2 ) ")
        Points addPoint( float x, float y ){
            enlargePointsArrayIfNeeded();
            points[lastIndex] = x;
            points[lastIndex+1] = y;
            lastIndex += 2;
            if( lastIndex != 2 ){
                points[lastIndex] = x;
                points[lastIndex+1] = y;
                lastIndex += 2;
            }
            return this;
        }

        Points finish( float x, float y ){
            enlargePointsArrayIfNeeded();
            points[lastIndex] = x;
            points[lastIndex+1] = y;
            lastIndex += 2;
            return this;
        }
    }

    static class Pool{
        List<Points> elements = Lists.newLinkedList();

        Points get(){
            if( elements.isEmpty() ){
                return new Points();
            }
            else{
                return elements.remove(0);
            }
        }

        Pool put(Points element){
            elements.add(element);
            return this;
        }

        Pool putAll( Collection<Points> eles ){
            elements.addAll( eles );
            return this;
        }

    }

    static class DrawingLines{
        Pool pool = new Pool();

        Map<Integer,Points> pointsMap = Maps.newHashMap();

        @Requires( "pointsMap.containsKey(id)" )
            Points getPointsFromId(int id){
                return pointsMap.get(id);
            }

        @Requires( "pointsMap.containsKey(id)" )
            void removePointsForId(int id){
                pool.put( pointsMap.remove(id) );
            }

        @Requires( "!pointsMap.containsKey(id)" )
            Points newPointsForId(int id){
                Points points = pool.get();
                pointsMap.put( id, points );
                return points;
            }

        @Getter @Setter int currentColor;

        DrawingLines(int currentColor ){
            this.currentColor = currentColor;
        }

        DrawingLines(){
            this( Color.BLACK );
        }

        //Lines add( int id, float value ){
        //Points points = getPointsFromId( id );

        //if( points.lastIndex >= INIT_POINT_NUM_PER_LINE ){
        //return this; //ignore 
        //}

        //points.points[points.lastIndex++] = value;
        //return this;
        //}
        DrawingLines drawPoints( Canvas canvas, Paint paint ){
            paint.setColor( currentColor );

            for( Points points : pointsMap.values() ){
                //Log.v( TAG, String.format( "DrawPoints: %d ( %d )", points.lastIndex, currentColor ) );
                for( int i = 0; i < points.lastIndex - 2; i += 2 ){
                    //Log.v( TAG, String.format( "points (%f, %f)", points.points[i], points.points[i+1] ) );
                }
                canvas.drawLines( points.points, 0, points.lastIndex - 2, paint );
            }
            return this;
        }

        void clear(){
            pool.putAll( pointsMap.values() );
            pointsMap.clear();
        }
    }

    static class Lines{
        List<Line> lines = Lists.newArrayList();

        //int currentColor;

        //public void setCurrentColor(int value){
        //currentColor = value;
        //}

        //public int getCurrentColor(){
        //return currentColor;
        //}

        Lines clear(){
            lines.clear();
            return this;
        }

        Lines newLine(Points points, int pointsColor){
            lines.add( Line.newInstance( points.points, points.lastIndex, pointsColor ) );
            return this;
        }



        Lines drawLines( Canvas canvas, Paint paint ){
            for( Line p : lines ){
                p.draw( canvas, paint );
            }
            return this;
        }

    }


    Lines lines = new Lines();
    DrawingLines drawingLines = new DrawingLines();

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

    void handlePointers(MotionEvent event, int excludeIndex){
        for( int pointerIndex = 0; pointerIndex < event.getPointerCount(); ++pointerIndex ){
            if( pointerIndex != excludeIndex ){
                int id = event.getPointerId(pointerIndex);
                //Log.v( TAG, String.format( "Handle pointer: %d ( %d )", pointerIndex, id ) );
                Points points = drawingLines.getPointsFromId( id );
                points.addPoint( event.getX( pointerIndex ), event.getY( pointerIndex ) );
            }
        }
    }

    void handlePointerMove(MotionEvent event){
        for( int pointerIndex = 0; pointerIndex < event.getPointerCount(); ++pointerIndex ){
            int id = event.getPointerId(pointerIndex);
            //Log.v( TAG, String.format( "Handle move pointer: %d ( %d )", pointerIndex, id ) );
            Points points = drawingLines.getPointsFromId( id );
            for( int pointerHistoryIndex = 0;  pointerHistoryIndex < event.getHistorySize(); ++pointerHistoryIndex ){
                //Log.v( TAG, String.format("At time %d:", event.getHistoricalEventTime(pointerHistoryIndex) ) );
                points.addPoint( event.getHistoricalX( pointerIndex, pointerHistoryIndex ), event.getHistoricalY( pointerIndex, pointerHistoryIndex ) );
            }

            points.addPoint( event.getX( pointerIndex ), event.getY( pointerIndex ) );
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        final int action = event.getAction();
        switch(action & MotionEvent.ACTION_MASK ) {
                //{
                    //drawingLines.newPointsForId(event.getPointerId(0)).reset( event.getX(0), event.getY(0) );
                //}
                //break;

            case MotionEvent.ACTION_DOWN:
                {
                    //Log.v( TAG, String.format( "Action Down: %d", 0 ) );
                    drawingLines.newPointsForId(event.getPointerId(0)).reset();
                }
                break;
                    

            case MotionEvent.ACTION_POINTER_DOWN:
                {
                    final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK ) >>  MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    drawingLines.newPointsForId(event.getPointerId(pointerIndex)).reset();
                    //Log.v( TAG, String.format( "Action Down: %d", pointerIndex ) );
                    handlePointers( event, pointerIndex );
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                {
                    final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK ) >>  MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                    //Log.v( TAG, String.format( "Action up: %d", pointerIndex ) );
                    Points points = drawingLines.getPointsFromId(event.getPointerId(pointerIndex)).finish( event.getX(pointerIndex), event.getY(pointerIndex));
                    handlePointers( event, pointerIndex );
                    lines.newLine( points, drawingLines.getCurrentColor() );
                    drawingLines.removePointsForId( event.getPointerId(pointerIndex) );
                }
                break;

            case MotionEvent.ACTION_MOVE:
                {
                    handlePointerMove( event );
                }
                break;
        }

        invalidate();
                                             
        return true;
    }

    public void setPaintColor(int color){
        drawingLines.setCurrentColor( color );
    }

    public int getPaintColor(){
        return drawingLines.getCurrentColor();
    }

    public void clearCanvas(){
        lines.clear();
        drawingLines.clear();
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw( canvas );
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor( Color.GREEN );
        paint.setStrokeWidth(10);
        lines.drawLines( canvas, paint );
        drawingLines.drawPoints( canvas, paint );
    }
}
