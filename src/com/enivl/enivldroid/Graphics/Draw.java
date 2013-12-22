package com.enivl.enivldroid.Graphics;

import com.enivl.enivldroid.Settings.PollModbus;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

public class Draw extends View{
	private int xmax;
	private int ymax;
	
	private Paint paint; 
	
	private float ballRadius; // Ball's radius
	private float ballX;  // Ball's center (x,y)
	private float ballY;
	private RectF ballBounds;      // Needed for Canvas.drawOval
	
	private boolean[] values = new boolean[14];
	
	private String statusMsg;
	
	private static Point upRight, upLeft, downRight, downLeft, alarm;{
		upRight = new Point(xmax/2,ymax/10);
	  upLeft = new Point(xmax/2,9*ymax/10);
	  downRight = new Point(3*xmax/4,ymax/10);
	  downLeft = new Point(3*xmax/4,9*ymax/10);
	  alarm =new Point(7*xmax/8,19*ymax/20);}
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
		  for (int i=0;i<14;i++){
			  values[i] = Boolean.parseBoolean(PollModbus.modbusValues[i].toString());
		  }
		   
		  ballRadius=ymax/60;
		  upRight.set(xmax/2,ymax/10);
		  upLeft.set(xmax/2,9*ymax/10);
		  downRight.set(3*xmax/4,ymax/10);
		  downLeft.set(3*xmax/4,9*ymax/10);
		  alarm.set(7*xmax/8,4*ymax/5);
		  
		  // Draw horizontal positions
		  int color;
		  ballX=xmax/8;
		  
		  for (int i=0;i<10;i++){
			  if (values[i]==true) color = Color.GREEN; else color = Color.BLACK;
			  paint.setColor(color);
			  ballY=ymax*(2*i+1)/20;
			  ballBounds.set(ballX-ballRadius, ballY-ballRadius, ballX+ballRadius, ballY+ballRadius);
			  canvas.drawOval(ballBounds, paint);
		  }
	      // Draw vertical positions

		  if (values[13]==true) color = Color.GREEN; else color = Color.BLACK;
		  paint.setColor(color);
		  ballBounds.set(upRight.x-ballRadius, upRight.y-ballRadius, upRight.x+ballRadius, upRight.y+ballRadius);
		  canvas.drawOval(ballBounds, paint);
	      
		  if (values[11]==true) color = Color.GREEN; else color = Color.BLACK;
		  paint.setColor(color);
		  ballBounds.set(upLeft.x-ballRadius, upLeft.y-ballRadius, upLeft.x+ballRadius, upLeft.y+ballRadius);
		  canvas.drawOval(ballBounds, paint);
		  
		  if (values[12]==true) color = Color.GREEN; else color = Color.BLACK;
		  paint.setColor(color);
		  ballBounds.set(downRight.x-ballRadius, downRight.y-ballRadius, downRight.x+ballRadius, downRight.y+ballRadius);
		  canvas.drawOval(ballBounds, paint);
		  
		  if (values[10]==true) color = Color.GREEN; else color = Color.BLACK;
		  paint.setColor(color);
		  ballBounds.set(downLeft.x-ballRadius, downLeft.y-ballRadius, downLeft.x+ballRadius, downLeft.y+ballRadius);
		  canvas.drawOval(ballBounds, paint);
		  
		  // Draw alarm position
		  paint.setColor(Color.BLACK);
		  ballBounds.set(alarm.x-ballRadius, alarm.y-ballRadius, alarm.x+ballRadius, alarm.y+ballRadius);
		  canvas.drawOval(ballBounds, paint);
		  statusMsg = "Alarm";
		  canvas.rotate(270, alarm.x+ballRadius, 19*ymax/20);
		  canvas.drawText(statusMsg, alarm.x+ballRadius, 19*ymax/20, paint);
		  canvas.rotate(90, alarm.x+ballRadius, 19*ymax/20);
	   // Draw the status mode
	      statusMsg = "Mode:";
	      canvas.rotate(270, alarm.x+ballRadius, ymax/5);
	      canvas.drawText(statusMsg, alarm.x+ballRadius, ymax/5, paint);
	      //canvas.rotate(270, alarm.x-ballRadius, ymax/10);
	      
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

