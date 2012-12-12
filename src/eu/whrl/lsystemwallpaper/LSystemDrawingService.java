package eu.whrl.lsystemwallpaper;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class LSystemDrawingService extends WallpaperService {

	@Override
	public Engine onCreateEngine() {
		return new LSystemDrawingEngine();
	}
	
	public enum DrawingState {
		DRAW,
		FADE
	}
	
	private class LSystemDrawingEngine extends Engine {
		
		private final Handler handler = new Handler();
		private final Runnable drawRunner = new Runnable() {
			@Override
			public void run() {
				draw();
			}

		};
		
		private Paint tailPaint = new Paint();
		private Paint headPaint = new Paint();
		
		private boolean visible = true;

		private LSystem lsystem = null;
		private float x = 50.0f;
		private float y = 200.0f;
		private float angle = 0.0f;
		private int currentCommand = 0;
		
		private float originX;
		private float originY;
		
		private Path tailLine;
		
		DrawingState state = DrawingState.DRAW;
		
		public LSystemDrawingEngine() {			
			tailPaint.setAntiAlias(true);
			tailPaint.setColor(Color.GRAY);
			tailPaint.setStyle(Paint.Style.STROKE);
			tailPaint.setStrokeWidth(4f);
			
			headPaint.setAntiAlias(true);
			headPaint.setColor(Color.MAGENTA);
			headPaint.setStyle(Paint.Style.STROKE);
			headPaint.setStrokeWidth(5f);
			
			String[] functions = new String[3];
			functions[0] = "f::20";
			functions[1] = "l:+rf-lfl-fr+:0";
			functions[2] = "r:-lf+rfr+fl-:0"; 
			lsystem = new LSystem(5, 90.0f, "l", functions);
			
			originX = x;
			originY = y;
			
			tailLine = new Path();
			tailLine.moveTo(originX, originY);
			
			handler.post(drawRunner);
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			this.visible = visible;
			if (visible) {
				handler.post(drawRunner);
			} else {
				handler.removeCallbacks(drawRunner);
			}
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			this.visible = false;
			handler.removeCallbacks(drawRunner);
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			
		}

		private void draw() {
			SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = null;
			try {
				canvas = holder.lockCanvas();
				if (canvas != null) {
					canvas.drawColor(Color.BLACK);
					if (state == DrawingState.DRAW) {
						drawLSystem(canvas);
					} else if (state == DrawingState.FADE) {
						fadeLSystem(canvas);
					}
				}
			} finally {
				if (canvas != null)
					holder.unlockCanvasAndPost(canvas);
			}
			handler.removeCallbacks(drawRunner);
			if (visible) {
				handler.postDelayed(drawRunner, 200);
			}
		}
		
		private void drawOlderLines(Canvas canvas) {
			canvas.drawPath(tailLine, tailPaint);
		}
		
		private void fadeLSystem(Canvas canvas) {
			int newAlpha = tailPaint.getAlpha() - 16;
			if (newAlpha < 0) {
				changeToDraw();
				return;
			}
			tailPaint.setAlpha(newAlpha);
			drawOlderLines(canvas);
		}
		
		private void drawLSystem(Canvas canvas) {
			
			drawOlderLines(canvas);
			
			float newX = x;
			float newY = y;
			
			LSystem.Command cmd = lsystem.commands[currentCommand];
			
			boolean found = false;
			
			// Find the next command that is a move that isn't distance 0.
			while (!found) {
				if ((cmd instanceof LSystem.Move)) {
					if (((LSystem.Move)cmd).dist > 0.0f) {
						found = true;
					}
				}
				
				if (!found) {
					if (cmd instanceof LSystem.Turn) {
						angle += ((LSystem.Turn)cmd).angle;
					}

					currentCommand++;
					if (currentCommand == lsystem.commands.length) {
						changeToFade();
						return;
					}
					cmd = lsystem.commands[currentCommand];
				}
			}
			
			// Calculate the destination of the move.
			float distance = ((LSystem.Move)cmd).dist;
			double radians = Math.toRadians(angle);
			newX = x + (float) (Math.cos(radians)*distance);
			newY = y + (float) (Math.sin(radians)*distance);
			
			// Draw our new line
			canvas.drawLine(x, y, newX, newY, headPaint);
			
			tailLine.lineTo(newX, newY);
			
			// Update our position
			x = newX;
			y = newY;
			
			// Move onto the next command.
			currentCommand++;
			if (currentCommand == lsystem.commands.length) {
				changeToFade();
			}
		}
		
		private void changeToFade() {
			currentCommand = 0;
			state = DrawingState.FADE;
		}
		
		private void changeToDraw() {
			tailLine = new Path();
			tailLine.moveTo(originX, originY);
			x = originX;
			y = originY;
			tailPaint.setAlpha(255);
			state = DrawingState.DRAW;
		}
	} 
}