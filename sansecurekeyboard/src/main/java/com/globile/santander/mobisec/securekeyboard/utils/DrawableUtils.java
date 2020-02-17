package com.globile.santander.mobisec.securekeyboard.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class DrawableUtils {

    private DrawableUtils() {
        throw new IllegalStateException("Utility class");
    }
	
	public static Drawable generateTextViewCanvas(Context context, int width, int height, int bgColor, int textColor,
	                                              String text, boolean roundBorders) {
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		
		Paint paint = new Paint();
		paint.setColor(bgColor);
		paint.setStyle(Paint.Style.FILL);
		
		RectF rect = new RectF(5, 5, width - 5, height - 5);
		if (roundBorders) {
			int cornerRadius = Math.min(width, height) / 8;//Similar to original
			canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
		} else {
			canvas.drawRect(rect, paint);
		}
		
		paint.setColor(textColor);
		paint.setAntiAlias(true);
		float textSize = height / 3.f;
        paint.setTextSize(textSize);
		paint.setTextAlign(Paint.Align.CENTER);
		canvas.drawText(text, (width / 2.f), ((height + textSize / 2.f) / 2.f), paint);
		
		return new BitmapDrawable(context.getResources(), bitmap);
	}
}
