package com.eri.mfd.views;

import com.eri.mfd.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.BlurMaskFilter.Blur;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;

public class MagneticHistory extends View {

	Paint paint;
	Rect bounds;
	Rect srcRect;
	Rect destRect;
	Point backgroundPiece;
	Bitmap backgroundBitmap;
	
	int mHistoryValues[] = null;
	int mHistoryValueIndex = 0;
	int mMagneticValue = 0;
	int mHistoryValuesMin = 0;
	int mHistoryValuesMax = 0;
	
	Typeface tfont;
	
	public MagneticHistory(Context context,AttributeSet attrs) {
		super(context,attrs);
		tfont = Typeface.createFromAsset(context.getAssets(),"fonts/Days.otf");
		paint=new Paint();
		paint.setTypeface(tfont);
		paint.setAntiAlias(true);
        paint.setTextSize(14);
        paint.setColor(0xFF000000);
        
        bounds = new Rect();
        srcRect = new Rect();
        destRect = new Rect();
        backgroundPiece = new Point();
        
        backgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_bit);
        
        setPadding(3, 3, 3, 3);
	}
	
	private Canvas canvas;
    private Bitmap bitmap;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (bitmap != null) {
            bitmap .recycle();
        }
        canvas= new Canvas();
        if (h == 0) h = 1;
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);        
        mHistoryValues = new int[w];
        mHistoryValueIndex = 0;
        canvas.setBitmap(bitmap);
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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }
    
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) 
        {
            result = specSize;
        } else 
        {            
            result = (int) 100 + getPaddingLeft() + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) 
            {
                result = Math.min(result, specSize);
            }
        }

        return result;
    } 
    
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) 
        {
            result = specSize;
        } else 
        {
            result = (int) (100) + getPaddingTop() + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) 
            {       
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
    
    public void destroy() {
        if (bitmap != null) {
            bitmap.recycle();
        }
    }
    
    @Override
    public void onDraw(Canvas c) 
    {  
    	if (backgroundBitmap == null) return; 
    			
    	float height = getHeight();
		float width = getWidth();
		float y = 0;
		float oy = 0;
		float lMax = mHistoryValuesMax-mHistoryValuesMin;
		if (lMax < 10) lMax = 10;
		
		if (mHistoryValues == null) return;		
		
    	paint.setColor(Color.BLACK);
    	canvas.drawRect(0, 0, width, height, paint);
    	
    	paint.setColor(Color.GREEN);
    	if (mHistoryValuesMax-mHistoryValuesMin != 0)
    	{
    		for(int i = 0;i<mHistoryValues.length;i++)
    		{
    			y = ((mHistoryValues[i] - mHistoryValuesMin) * height) / (lMax);
    			if (i > 0) c.drawLine(i-1, height - oy, i, height - y, paint);
    			oy = y;    			
    		}
    	}
    	paint.setColor(Color.WHITE);
		paint.setMaskFilter(new BlurMaskFilter(2, Blur.INNER));
    	c.drawText("Maks. "+String.valueOf(mHistoryValuesMax)+"uT", getPaddingLeft(), getPaddingTop() + 16 , paint);
    	c.drawText(String.valueOf(mMagneticValue)+"uT", width-getPaddingRight()-paint.measureText(String.valueOf(mMagneticValue)+"uT"), getPaddingTop() + 16 , paint);
    	     
    }
}
