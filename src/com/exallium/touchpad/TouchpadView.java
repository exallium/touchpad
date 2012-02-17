package com.exallium.touchpad;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class TouchpadView extends View {
	private static String TAG = "TouchpadView";
	private static int INVALID_POINTER_ID;
	private int mActivePointerId = INVALID_POINTER_ID;
	private int mMoves;
	private int count = 0;
	private int max_down = 0;
	
	public TouchpadView(Context context) {
		super(context);
	}
	
	public TouchpadView(Context context, AttributeSet params) {
		super(context, params);
	}
	
	
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		switch(action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			Log.d(TAG, "ACTION_DOWN");
			mActivePointerId = ev.getPointerId(0);
			break;
		case MotionEvent.ACTION_UP:
			Log.d(TAG, "ACTION_UP " + count);
			mActivePointerId = INVALID_POINTER_ID;
		case MotionEvent.ACTION_CANCEL:
			Log.d(TAG, "ACTION_CANCEL" + count);
			mActivePointerId = INVALID_POINTER_ID;
			
			if (count == 1 && mMoves < 5) {
				Log.d(TAG, "GOT SINGLE CLICK");
			}
			
			if(count == 2 && mMoves < 5) {
				Log.d(TAG, "GOT MULTI CLICK " + max_down);	
			}
			
			max_down = 0;
			mMoves = 0;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			Log.d(TAG, "ACTION_POINTER_DOWN " + 
					((action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT));
			break;
		case MotionEvent.ACTION_POINTER_UP:
			Log.d(TAG, "ACTION_POINTER_DOWN " + 
					((action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_ID_SHIFT));
		case MotionEvent.ACTION_MOVE:
			final int pointerIndex = ev.getPointerId(mActivePointerId);
			Log.d(TAG, "ACTION_MOVE " + pointerIndex + " totaldown: " + ev.getPointerCount());
			if (mMoves < 10) mMoves++;
			break;
		}

		count = ev.getPointerCount();
		max_down = ev.getPointerCount() > max_down ? ev.getPointerCount() : max_down;
		return true;
	}

}
