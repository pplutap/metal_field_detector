package com.eri.mfd.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class MagneticValue extends View {

	Paint paint;
	Rect bounds;
	Rect srcRect;
	Rect destRect;
	int mMagneticValue;
	int mPulse = 0;
	Typeface tfont;
		
		
	public MagneticValue(Context context,AttributeSet attrs) {
		super(context,attrs);
		
		tfont = Typeface.createFromAsset(context.getAssets(),"fonts/Days.otf");
		paint=new Paint();
		paint.setAntiAlias(true);
        paint.setTextSize(16);
        paint.setTypeface(tfont);
        paint.setColor(0xFF000000);
        
        bounds = new Rect();
        srcRect = new Rect();
        destRect = new Rect();
        setPadding(3, 3, 3, 3);
	}
	
	private Canvas canvas;
    private Bitmap bitmap;

	public void setMagneticValue(int AValue)
	{
		mMagneticValue = AValue;			
		mPulse+=15;
		if (mPulse > 90) mPulse = 0;
		invalidate();
	}
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (bitmap != null) {
            bitmap .recycle();
        }
        canvas= new Canvas();
        if (h == 0) h = 1;
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
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
    	float height = getHeight();
		float width = getWidth();
		float lMinSize = Math.min(width-10, height-10);
		
		//c.clipRect(0, 0, width, height, Region.Op.REPLACE); 
		if (mMagneticValue > 0)
		{
			float lMaxValue = 200;
			float lMagneticValueRadius = (mMagneticValue * (lMinSize / 2)) / lMaxValue;  
			
			if (lMagneticValueRadius > lMinSize) lMagneticValueRadius = lMinSize;  
			
			RadialGradient gradient = new RadialGradient(
					width / 2, height / 2, lMagneticValueRadius, 
					Color.argb(255,12, 255-mPulse, 0),
		            Color.argb(0,12, 255-mPulse, 0),
		            android.graphics.Shader.TileMode.CLAMP);
			
		    paint.setDither(true);
		    paint.setShader(gradient);

	    	c.drawCircle(width / 2,height/2,lMagneticValueRadius,paint);
		}
		
		paint.setShader(null);
		//paint.setDither(true);
		paint.setTextSize(38);
		paint.setColor(Color.WHITE);
		paint.setMaskFilter(new BlurMaskFilter(5, Blur.INNER));
        c.drawText(
        		String.valueOf(mMagneticValue) + " uT", 
        		getPaddingLeft() + (width+getPaddingLeft()+getPaddingRight()) / 2 - paint.measureText(String.valueOf(mMagneticValue) + " uT")/2 , 
        		getPaddingTop() + (height+getPaddingTop()+getPaddingBottom()) / 2 - 20 , 
        		paint); 

        bounds.set(0,0,bitmap.getWidth(),bitmap.getHeight());
        //c.drawBitmap(bitmap,bounds, bounds, paint);      
    }

}
