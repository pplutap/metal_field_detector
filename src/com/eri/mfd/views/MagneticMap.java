package com.eri.mfd.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import com.eri.mfd.R;

public class MagneticMap extends View {
	
Paint paint;
Rect bounds;
Rect srcRect;
Rect destRect;

int mMagneticValue = 0;
int mHistoryValuesMin = 0;
int mHistoryValuesMax = 0;
int mMap[][] = null;
int mX,mY;
float mfX,mfY,mfZ;
public String mDebug = "";
float mRotX;
float mRotY;
float mRotZ;

Typeface tfont;

public MagneticMap(Context context,AttributeSet attrs) {
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
    
    setPadding(3, 3, 3, 3);
}


@Override
protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    if (h <= 0) h = 1;
    if (w <= 0) w = 1;
    
    mMap = new int[w/10][h/10];
    mX = w / 2;
    mY = h / 2;
    mfX = mX;
    mfY = mY;
   
    Calibrate();
}

public void addMagneticValue(int AValue)
{
	mMagneticValue = AValue;
	if (mHistoryValuesMax < mMagneticValue) mHistoryValuesMax = mMagneticValue;
	if (mHistoryValuesMin > mMagneticValue) mHistoryValuesMin = mMagneticValue;
	
	
	if ((mMap!=null)&&(mMap.length>0)&&(mX/100>0)&&(mY/100>0)&&(mX/100<mMap.length)&&(mY/100<mMap[0].length))
	{
		mMap[mX/100][mY/100] = AValue;
		invalidate();
	}else
	{
		//reset
	    Calibrate();
	}
		
}

public void Calibrate()
{
	int h = getHeight() / 2;
	int w = getWidth() / 2;
	
	mX = w*10;
    mY = h*10;
    mMagneticValue = 0;
    mHistoryValuesMin = 0;
    mHistoryValuesMax = 0;
    clear();
    
}

public void clear()
{
	for(int x=0;x<mMap.length;x++)
		for(int y=0;y<mMap[x].length;y++)
		{
			mMap[x][y] = 0;
		}
}

public void scrollMapX(int AOffset)
{
	if (AOffset > 0)
	{
		if (AOffset > mMap.length)
		{
			clear();
			return;
		}
			
		for(int x=0;x<mMap.length - AOffset;x++)
			for(int y=0;y<mMap[x].length;y++)
				mMap[x][y] = mMap[x+AOffset][y];
		
		for(int x=mMap.length-AOffset+1;x<mMap.length;x++)
			for(int y=0;y<mMap[x].length;y++)			
				mMap[x][y] = 0;			
	}else
	{
		AOffset = -AOffset;
		if (AOffset > mMap.length)
		{
			clear();
			return;
		}
			
		for(int x=mMap.length;x>mMap.length - AOffset;x--)
			for(int y=0;y<mMap[x].length;y++)
				mMap[x+AOffset][y] = mMap[x][y];
		
		for(int x=mMap.length - AOffset - 1;x>0;x--)
			for(int y=0;y<mMap[x].length;y++)
				mMap[x+AOffset][y] = 0;
		
	}
}

public void addMovementValue(float Ax,float Ay,float Az)
{
 mfX = Ax;
 mfY = Ay;
 mfZ = Az;
 
 if(Math.abs(Ax)>0.01)
 mX -= (Ax*500);  
 
 if(Math.abs(Ay)>0.01)
 mY -= (Ay*500);
 //scrollMapX(
}

public void addRotationValue(float ARotX,float ARotY,float ARotZ)
{
	mRotX = ARotX;
	mRotY = ARotY;
	mRotZ = ARotZ;
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

}

int D = 10;

public float ScrX(float Ax,float Ay,float Az)
{
	return Ax * D / ( D + Az );
}

public float ScrY(float Ax,float Ay,float Az)
{
	return Ay * D / ( D + Az );
}

public int ValToColor(int AVal,int AMax)
{
	if (AMax == 0) return Color.BLACK;
	int v = AVal*255/AMax;
	return Color.argb(v,255,v,255);
}

@Override
public void onDraw(Canvas c) 
{  
	float height = getHeight();
	float width = getWidth();
	float lMax = mHistoryValuesMax-mHistoryValuesMin;
	if (lMax < 10) lMax = 10;
	


	paint.setColor(Color.WHITE);

	for(int x=0;x<mMap.length;x++)
		for(int y=0;y<mMap[x].length;y++)
		{			
			paint.setColor( ValToColor(mMap[x][y] - mHistoryValuesMin,mHistoryValuesMax) );
			c.drawRect(x*10,y*10,x*10+10,y*10+10, paint);
			//c.drawPoint(x, y, paint);
		}

	paint.setColor(Color.WHITE);
	c.drawLine(mX/10, mY/10-10, mX/10, mY/10+10, paint);
	c.drawLine(mX/10-10, mY/10, mX/10+10, mY/10, paint);
	
	paint.setColor(Color.WHITE);
	//paint.setMaskFilter(new BlurMaskFilter(2, Blur.INNER));
	c.drawText("Maks. "+String.valueOf(mHistoryValuesMax)+"uT", getPaddingLeft(), getPaddingTop() + 16 , paint);
	c.drawText("Min. "+String.valueOf(mHistoryValuesMin)+"uT", getPaddingLeft(), getPaddingTop() + 32 , paint);
	c.drawText(String.valueOf(mMagneticValue)+"uT", width-getPaddingRight()-paint.measureText(String.valueOf(mMagneticValue)+"uT"), getPaddingTop() + 16 , paint);
   
}
}
