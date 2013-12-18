package fr.cmoatoto.miraslide.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.Chronometer;

/**
 * This is just a Chronometer class but the TextSize will adapt itself as the height of the view.
 * 
 * @author CmoaToto
 * 
 */
public class MyChronometer extends Chronometer {

	public MyChronometer(Context context) {
		super(context);
	}

	public MyChronometer(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MyChronometer(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		setTextSize(TypedValue.COMPLEX_UNIT_PX, 2 * h / 3);
	}

}
