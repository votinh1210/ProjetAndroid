package com.enivl.enivldroid.Graphics;

import com.enivl.enivldroid.Settings.EnivlDroidList;
import com.enivl.enivldroid.Settings.PollModbus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

public class Draw extends View{
	private int xmin=0;
	private int ymin=0;
	private int xmax;
	private int ymax;
	
	private Paint paint; 
	
	private float ballRadius; // Ball's radius
	private float ballX;  // Ball's center (x,y)
	private float ballY;
	private RectF ballBounds;      // Needed for Canvas.drawOval
	
	private int red, green, blue;
	
	private String statusMsg;

	   // Constructor
	public Draw(Context context) {
	   super(context);
	   ballBounds = new RectF();
	   paint = new Paint();
	// Set the font face and size of drawing text
	   paint.setTypeface(Typeface.MONOSPACE);
	   paint.setTextSize(16);
	}
	// Called back to draw the view. Also called by invalidate().
	   @Override
	protected void onDraw(Canvas canvas) {
	      // Draw the ball
		   red = Integer.parseInt(PollModbus.modbusValues[0].toString());
		   green = Integer.parseInt(PollModbus.modbusValues[1].toString());
		   blue = Integer.parseInt(PollModbus.modbusValues[2].toString());
		   
		   ballX=(xmax-xmin)/2;
		   ballY=(ymax-ymin)/2;
		   ballRadius=Math.min(ballX-20, ballY-40);
		  
	      ballBounds.set(ballX-ballRadius, ballY-ballRadius, ballX+ballRadius, ballY+ballRadius);
		  int color = Color.rgb(red, green, blue);
	      paint.setColor(color);
	      canvas.drawOval(ballBounds, paint);
	      
	   // Draw the status message
	      paint.setColor(Color.BLACK);
	      
	      statusMsg = "RGB("+red+","+green+","+blue+")";
	      canvas.drawText(statusMsg, 10, 30, paint);
	      
	      
	      // Update the position of the ball, including collision detection and reaction.
	      //update();
	  
	      // Delay
	      try {  
	         Thread.sleep(30);  
	      } catch (InterruptedException e) { }
	      
	      invalidate();  // Force a re-draw
	}
	
	public void onSizeChanged(int w,int h,int oldw, int oldh){
		xmax=w-1;
		ymax=h-1;
	}
	   
}

