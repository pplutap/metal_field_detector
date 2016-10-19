package com.eri.mfd.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Typeface;
import android.view.SurfaceView;

import com.eri.mfd.R;
//import android.graphics.Matrix;
//import android.opengl.Matrix;

public class DrawView extends SurfaceView{

    private Paint paint = new Paint();
    Typeface tfont;
    
    private static android.graphics.Matrix matrix = null;
    private static Bitmap bitmap = null;
    //
	int mHistoryValues[] = null;
	int mHistoryValueIndex = 0;
	int mMagneticValue = 0;
	int mHistoryValuesMin = 0;
	int mHistoryValuesMax = 0;

	float mDirection = 0;
	float[] mRotationMatrix = null;
    float arrS[] = new float[4];
    float arrE[] = new float[4];
    float arrL[] = new float[4];
    float arrR[] = new float[4];
    //
    float arrSr[] = new float[4];
    float arrEr[] = new float[4];
    float arrLr[] = new float[4];
    float arrRr[] = new float[4];

    
    private void initArrow()
    {
       int ASize = 80;
       
       arrS[0] = 0;//x
       arrS[1] = 0;//y
       arrS[2] = 0;//z
       arrS[3] = 1;
       
       arrE[0] = 0;
       arrE[1] = -ASize;
       arrE[2] = 0;
       arrE[3] = 1;
       
       arrL[0] = -ASize / 3;
       arrL[1] = 0;
       arrL[2] = 0;
       arrL[3] = 1;
       
       arrR[0] = ASize / 3;
       arrR[1] = 0;
       arrR[2] = 0;
       arrR[3] = 1;
       
    }
    
    public void setRotationMatrix(float[] AMatrix)
    {
    	float[] lMatrix = AMatrix.clone();
    	mRotationMatrix = AMatrix.clone();
    	
    	android.opengl.Matrix.invertM(mRotationMatrix, 0, lMatrix, 0);
    }
    
    public void setDirection(float ADirection)
    {
    	mDirection = ADirection;
    }
    
    
	public DrawView(Context context) {
		super(context);

		initArrow();
		
		matrix = new android.graphics.Matrix();
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.rose);
		
        paint.setARGB(255, 200, 255, 0);
        paint.setTextSize(18);
        tfont = Typeface.createFromAsset(context.getAssets(),"fonts/Days.otf");
    	paint.setTypeface(tfont);
    	paint.setAntiAlias(true);

        setWillNotDraw(false);
	}
	
    public void addMagneticValue(int AValue)
    {
    	mMagneticValue = AValue;
    	if (mHistoryValues == null) return;
    	
    	if (mHistoryValueIndex == mHistoryValues.length-1)
    	{
    		for(int i=0;i<mHistoryValues.length-1;i++)
    			mHistoryValues[i] = mHistoryValues[i+1];
    		mHistoryValues[mHistoryValueIndex] = AValue;
    	}else
    	{
    		mHistoryValues[mHistoryValueIndex] = AValue;
    		mHistoryValueIndex++;
    	}
    	
    	//Computer min/max
    	if (mHistoryValues.length > 0)
    	{
    		mHistoryValuesMax = mHistoryValues[0];
    		mHistoryValuesMin = mHistoryValues[0];
    		for(int i=1;i<mHistoryValues.length;i++)
    		{
    			if (mHistoryValuesMax < mHistoryValues[i]) mHistoryValuesMax = mHistoryValues[i];
    			if (mHistoryValuesMin > mHistoryValues[i]) mHistoryValuesMin = mHistoryValues[i];
    		}
    	}else
    	{
    		mHistoryValuesMax = 0;
    		mHistoryValuesMin = 0;
    	}
    	invalidate();
    }
	
    int D = 100;
    int cw = 0;
    int ch = 0;
    
    public float _X(float A[])
    {
    	return A[0] * D / ( D + A[2] ) + cw/2;
    }

    public float _Y(float A[])
    {
    	return A[1] * D / ( D + A[2] ) + ch/2;
    }
    
   
    
	@Override
    protected void onDraw(Canvas canvas){
		
		
		
		DrawHitogram(canvas);
		
		cw = getWidth();
		ch = getHeight();
		
		//String lStr = "Wskazanie bieguna pó³nocnego";
        //canvas.drawText(lStr, w/2-paint.measureText(lStr) / 2, 20, paint);
        
		paint.setColor(Color.WHITE);
		paint.setStrokeWidth(3);
		paint.setStrokeMiter(3);
		
		if (mRotationMatrix ==  null) return;
		android.opengl.Matrix.multiplyMV(arrSr, 0, mRotationMatrix, 0, arrS, 0);
		android.opengl.Matrix.multiplyMV(arrEr, 0, mRotationMatrix, 0, arrE, 0);
		android.opengl.Matrix.multiplyMV(arrLr, 0, mRotationMatrix, 0, arrL, 0);
		android.opengl.Matrix.multiplyMV(arrRr, 0, mRotationMatrix, 0, arrR, 0);
		
		Path arrowPath = new Path();
	      paint.setColor(Color.GREEN);
	      paint.setStyle(Style.FILL);
	      arrowPath.reset(); 
	      arrowPath.moveTo(_X(arrLr), _Y(arrLr)); 
	      arrowPath.lineTo(_X(arrEr), _Y(arrEr));
	      arrowPath.lineTo(_X(arrRr), _Y(arrRr));
	      arrowPath.lineTo(_X(arrLr), _Y(arrLr)); 
	      canvas.drawPath(arrowPath, paint);

	      DrawRose(canvas);
		/*
		canvas.drawLine(
				_X(arrSr), _Y(arrSr), 
				_X(arrEr), _Y(arrEr),
				paint);
		
		canvas.drawLine(
				_X(arrEr), _Y(arrEr), 
				_X(arrLr), _Y(arrLr),
				paint);
		
		canvas.drawLine(
				_X(arrEr), _Y(arrEr), 
				_X(arrRr), _Y(arrRr),
				paint);
			*/	
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		setSize(w);
	}
	
	 public void setSize(int w) {
        if (w == 0) w = 1;     
        mHistoryValues = new int[w];
        mHistoryValueIndex = 0;
	 }
	
	 private void DrawRose(Canvas canvas) {
	        if (canvas==null) return;

	         

	        
	        float bearing = mDirection;
	        float heading = 360-bearing;

	        int bitmapWidth = bitmap.getWidth();
	        int bitmapHeight = bitmap.getHeight();
	        
	        int canvasWidth = canvas.getWidth();
	        int canvasHeight = canvas.getHeight();
	        if (bitmapWidth>canvasWidth || bitmapHeight>canvasHeight) {        
	            //Resize the bitmap to the size of the canvas
	            bitmap = Bitmap.createScaledBitmap(bitmap, (int)(bitmapWidth*.9), (int)(bitmapHeight*.9), true);
	        }
	        
	        int bitmapX = bitmap.getWidth()/2;
	        int bitmapY = bitmap.getHeight()/2;
	        
	        int canvasX = canvas.getWidth()/2;
	        int canvasY = canvas.getHeight()/2;
	        
	        int centerX = canvasX-bitmapX;
	        int centerY = canvasY-bitmapY;
	        
	        matrix.reset();
	        matrix.setRotate(heading, bitmapX, bitmapY);
	        matrix.postTranslate(centerX, centerY);

	        canvas.drawBitmap(bitmap, matrix, null);
	    }
	 
	private void DrawHitogram(Canvas c)
	{
    	float height = 50;
    	float realheight = getHeight();
		float width = getWidth();
		float y = 0;
		float oy = 0;
		float lMax = mHistoryValuesMax-mHistoryValuesMin;
		if (lMax < 10) lMax = 10;
		
		if (mHistoryValues == null) return;		
		
    	paint.setColor(Color.GREEN);
    	if (mHistoryValuesMax-mHistoryValuesMin != 0)
    	{
    		for(int i = 0;i<mHistoryValues.length;i++)
    		{
    			y = ((mHistoryValues[i] - mHistoryValuesMin) * height) / (lMax);
    			if (i > 0) c.drawLine(i-1, realheight - oy, i, realheight - y, paint);
    			oy = y;    			
    		}
    	}
    	paint.setColor(Color.WHITE);
		paint.setMaskFilter(new BlurMaskFilter(2, Blur.INNER));
    	c.drawText("Maks. "+String.valueOf(mHistoryValuesMax)+"uT", getPaddingLeft(), getPaddingTop() + 16 , paint);
    	c.drawText(String.valueOf(mMagneticValue)+"uT", width-getPaddingRight()-paint.measureText(String.valueOf(mMagneticValue)+"uT"), getPaddingTop() + 16 , paint);
    	     
	}
}
