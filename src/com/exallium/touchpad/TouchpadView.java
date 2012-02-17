package com.exallium.touchpad;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class TouchpadView extends View {
	private static String TAG = "TouchpadView";
	private static int INVALID_POINTER_ID;
	
	private static int PORT = 48000;
	private static String HOST = "192.168.2.11";
	private static String LEFT_CLICK = "click 1";
	
	private int mActivePointerId = INVALID_POINTER_ID;
	private int mMoves;
	private int count = 0;
	private int max_down = 0;
	private int prevx = -1;
	private int prevy = -1;
	private DataOutputStream out;
	
	private Socket socket;
	
	public TouchpadView(Context context) {
		super(context);
		
		initTCP();
	}

	public TouchpadView(Context context, AttributeSet params) {
		super(context, params);
		this.getWidth();
		this.getHeight();
		initTCP();
	}
	
	private void initTCP() {
		try {
			socket = new Socket(HOST, PORT);
			socket.setSendBufferSize(1024);
			out = new DataOutputStream(socket.getOutputStream());
		} catch (UnknownHostException e) {
			Toast.makeText(this.getContext(), "Unknown Host", Toast.LENGTH_LONG).show();
			e.printStackTrace();
			socket = null;
			out = null;
		} catch (IOException e) {
			Toast.makeText(this.getContext(), "IO Exception, are you sure the server is running?", Toast.LENGTH_LONG).show();
			e.printStackTrace();
			socket = null;
			out = null;
		}
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
				try {
					out.writeBytes(LEFT_CLICK);
				} catch (IOException e) {
					Toast.makeText(getContext(), "Cannot Send.  Please reconnect", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
			}
			
			if(count == 2 && mMoves < 5) {
				Log.d(TAG, "GOT MULTI CLICK " + max_down);	
			}
			
			prevx = -1;
			prevy = -1;
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
			if (mMoves < 20) mMoves++;
			if (mMoves == 20) mMoves = 10;
			
			if (mMoves % 2 == 0) {
				final int x = (int) ev.getX(mActivePointerId);
				final int y = (int) ev.getY(mActivePointerId);
				
				float xmove = (x - prevx) / (float) this.getWidth();
				float ymove = (y - prevy) / (float) this.getHeight();
				
				if (prevx == -1 && prevy == -1) {
					xmove = 0;
					ymove = 0;
				}
				
				try {
					out.writeBytes("move relative " + xmove + " " + ymove);
					Log.d(TAG, "" + xmove + " " + ymove);
				} catch (IOException e) {
					Toast.makeText(getContext(), "Cannot Send.  Please reconnect", Toast.LENGTH_LONG).show();
					e.printStackTrace();
				}
				
				prevx = x;
				prevy = y;
			}
			
			break;
		}

		count = ev.getPointerCount();
		max_down = ev.getPointerCount() > max_down ? ev.getPointerCount() : max_down;
		return true;
	}

}
