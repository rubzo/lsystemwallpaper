package eu.whrl.lsystemwallpaper;

import java.util.LinkedList;
import java.util.List;

import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class LSystemDrawingService extends WallpaperService {
	
	public static final String TAG = "LSystemLW";
	
	@Override
	public Engine onCreateEngine() {
		return new LSystemDrawingEngine();
	}
	
	public enum DrawingState {
		PREPARE,
		ERROR,
		DRAW,
		FADE
	}
	
	private class LSystemDrawingEngine extends Engine 
		implements SharedPreferences.OnSharedPreferenceChangeListener {
		
		private final Handler handler = new Handler();
		private final Runnable drawRunner = new Runnable() {
			@Override
			public void run() {
				draw();
			}

		};
		
		private boolean visible;

		private LSystem lsystem;
		private int currentCommand;
		
		private DrawingPosition drawPos;
		private List<DrawingPosition> drawPosStack = new LinkedList<DrawingPosition>();
		
		private DrawingPosition originDrawPos;
		
		private List<Path> tailLines;
		private Path currentTailLine;
		
		private DrawingState state;
		
		private Paint tailPaint;
		private Paint headPaint;
		private int bgColor;
		
		private int savedWidth;
		private float scalingFactor;
		
		private LSystemDescription lsDesc;
		
		private SharedPreferences preferences;
		
		private int refreshSpeed;
		
		class LSystemGenerator extends AsyncTask<LSystemDescription,Void,LSystem> {

			private long generateLSystemStartTime;
			
			@Override
			protected LSystem doInBackground(LSystemDescription... params) {
				if (params.length == 1) { 
					LSystemDescription d = params[0];
					generateLSystemStartTime = System.currentTimeMillis();
					LSystem lsystem = new LSystem(d.iterations, 
							d.turnAngle, 
							d.startState, 
							d.functions);
					return lsystem;
				}
				return null;
			}

			@Override
			protected void onPostExecute(LSystem l) {
				if (l != null) {
					lsystem = l;
					long generateLSystemEndTime = System.currentTimeMillis();
					Log.d(LSystemDrawingService.TAG,
							String.format("Took %.4fs to generate LSystem", (generateLSystemEndTime - generateLSystemStartTime)/1000.0f));
					long prepareForDrawingStartTime = System.currentTimeMillis();
					prepareForDrawing();
					long prepareForDrawingEndTime = System.currentTimeMillis();
					Log.d(LSystemDrawingService.TAG,
							String.format("Took %.4fs to prepare for drawing", (prepareForDrawingEndTime - prepareForDrawingStartTime)/1000.0f));
					state = DrawingState.DRAW;
				} else {
					state = DrawingState.ERROR;
				}
			}
		}
		
		public LSystemDrawingEngine() {
			// Get our preferences, and make sure we know if anything changes.
			preferences = LSystemDrawingService.this.getSharedPreferences(WallpaperPreferencesActivity.name, MODE_PRIVATE);
            preferences.registerOnSharedPreferenceChangeListener(this);
			
            refreshSpeed = Integer.parseInt(preferences.getString(WallpaperPreferencesActivity.refreshSpeedKeyName, "200"));
            
            // Call the real constructor
			initOrReset();
			
			// Set our drawing handler
			handler.post(drawRunner);
		}
		
		private void readPreferences() {
			// Pick tail color
			tailPaint.setAntiAlias(true);
			tailPaint.setStyle(Paint.Style.STROKE);
			tailPaint.setStrokeWidth(4f);
			tailPaint.setColor(preferences.getInt(WallpaperPreferencesActivity.tailColorKeyName, Color.WHITE));
			
			// Pick head/turtle color
			headPaint.setAntiAlias(true);
			headPaint.setStyle(Paint.Style.STROKE);
			headPaint.setStrokeWidth(5f);
			headPaint.setColor(preferences.getInt(WallpaperPreferencesActivity.headColorKeyName, Color.GREEN));
			
			// Pick background color
			bgColor = preferences.getInt(WallpaperPreferencesActivity.bgColorKeyName, Color.BLACK);
			
			// Pick L-System
			String lsystemName = preferences.getString(WallpaperPreferencesActivity.lsystemKeyName, 
					WallpaperPreferencesActivity.lsystemDefaultValue);
			
			lsDesc = LSystemCatalogue.get(lsystemName);
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
			savedWidth = width;
		}

		@Override
		public void onTouchEvent(MotionEvent event) {
			
		}

		private void draw() {
			SurfaceHolder holder = getSurfaceHolder();
			Canvas canvas = null;
			try {
				canvas = holder.lockCanvas();
				canvas.drawColor(bgColor);
				if (canvas != null) {
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
				handler.postDelayed(drawRunner, refreshSpeed);
			}
		}
		
		private void drawOlderLines(Canvas canvas) {
			for (Path tailLine : tailLines) {
				canvas.drawPath(tailLine, tailPaint);
			}
		}
		
		private void fadeLSystem(Canvas canvas) {
			int newAlpha = tailPaint.getAlpha() - 16;
			if (newAlpha < 0) {
				changeToDrawState();
				return;
			}
			tailPaint.setAlpha(newAlpha);
			drawOlderLines(canvas);
		}
		
		private void drawLSystem(Canvas canvas) {
			
			drawOlderLines(canvas);
			
			float newX = drawPos.x;
			float newY = drawPos.y;
			
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
						drawPos.angle += ((LSystem.Turn)cmd).angle;
					}
					
					if (cmd instanceof LSystem.BranchStart) {
						drawPosStack.add(drawPos.copy());
					}
					
					if (cmd instanceof LSystem.BranchEnd) {
						if (drawPosStack.size() > 0) {
							drawPos = drawPosStack.remove(drawPosStack.size()-1);
							currentTailLine = new Path();
							currentTailLine.moveTo(drawPos.x, drawPos.y);
							tailLines.add(currentTailLine);
						} else {
							Log.w("LSystem", "Encountered branch end with no matching branch start, skipping.");
						}
					}

					currentCommand++;
					if (currentCommand == lsystem.commands.length) {
						changeToFadeState();
						return;
					}
					cmd = lsystem.commands[currentCommand];
				}
			}
			
			// Calculate the destination of the move.
			float distance = ((LSystem.Move)cmd).dist * scalingFactor;
			double radians = Math.toRadians(drawPos.angle);
			newX = drawPos.x + (float) (Math.cos(radians)*distance);
			newY = drawPos.y + (float) (Math.sin(radians)*distance);
			
			// Draw our new line
			canvas.drawLine(drawPos.x, drawPos.y, newX, newY, headPaint);
			
			currentTailLine.lineTo(newX, newY);
			
			// Update our position
			drawPos.x = newX;
			drawPos.y = newY;
			
			// Move onto the next command.
			currentCommand++;
			if (currentCommand == lsystem.commands.length) {
				changeToFadeState();
			}
		}
		
		private void changeToFadeState() {
			currentCommand = 0;
			state = DrawingState.FADE;
		}
		
		private void changeToDrawState() {
			tailLines.clear();
			currentTailLine = new Path();
			currentTailLine.moveTo(originDrawPos.x, originDrawPos.y);
			tailLines.add(currentTailLine);
			drawPos = originDrawPos.copy();
			tailPaint.setAlpha(255);
			state = DrawingState.DRAW;
		}
		
		private void prepareForDrawing() {	
			// First thing we must do is run through all the commands ahead of time,
			// and work out how much space we need to draw the L-System
			DrawingPosition fakeDrawPos = new DrawingPosition(0, 0, 0);
			List<DrawingPosition> fakeDrawPosStack = new LinkedList<DrawingPosition>();
			int fakeCurrentCommand = 0;
			
			// The BB corners
			DrawingPosition tlBound = new DrawingPosition(0, 0, 0);
			DrawingPosition trBound = new DrawingPosition(0, 0, 0);
			DrawingPosition blBound = new DrawingPosition(0, 0, 0);
			DrawingPosition brBound = new DrawingPosition(0, 0, 0);

			// Go through all the commands...
			while (fakeCurrentCommand != lsystem.commands.length) {
			
				LSystem.Command cmd = lsystem.commands[fakeCurrentCommand];

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
							fakeDrawPos.angle += ((LSystem.Turn)cmd).angle;
						}

						if (cmd instanceof LSystem.BranchStart) {
							fakeDrawPosStack.add(fakeDrawPos.copy());
						}

						if (cmd instanceof LSystem.BranchEnd) {
							if (fakeDrawPosStack.size() > 0) {
								fakeDrawPos = fakeDrawPosStack.remove(fakeDrawPosStack.size()-1);
							}
						}

						fakeCurrentCommand++;
						if (fakeCurrentCommand == lsystem.commands.length) {
							found = true;
						} else {
							cmd = lsystem.commands[fakeCurrentCommand];
						}
					}
				}

				// Are we finished "drawing" yet?
				if (fakeCurrentCommand != lsystem.commands.length) {
					// Calculate the destination of the move.
					float distance = ((LSystem.Move)cmd).dist;
					double radians = Math.toRadians(fakeDrawPos.angle);
					fakeDrawPos.x += (float) (Math.cos(radians)*distance);
					fakeDrawPos.y += (float) (Math.sin(radians)*distance);

					if (fakeDrawPos.x < tlBound.x) {
						tlBound.x = fakeDrawPos.x;
						blBound.x = fakeDrawPos.x;
					}
					if (fakeDrawPos.y < tlBound.y) {
						tlBound.y = fakeDrawPos.y;
						trBound.y = fakeDrawPos.y;
					}

					if (fakeDrawPos.x > trBound.x) {
						trBound.x = fakeDrawPos.x;
						brBound.x = fakeDrawPos.x;
					}
					if (fakeDrawPos.y > blBound.y) {
						blBound.y = fakeDrawPos.y;
						brBound.y = fakeDrawPos.y;
					}

					// Move onto the next command.
					fakeCurrentCommand++;
				}
			}
			
			// Now we calculate the bounds
			float xLength = trBound.x - tlBound.x;
			float yLength = tlBound.y - blBound.y;
			
			// Make 'em square
			if (xLength > yLength) {
				yLength = xLength;
			} else if (yLength > xLength) {
				xLength = yLength;
			}
			
			// Calculate origin point as ratio of the bounding box
			float originX = (-tlBound.x) / xLength;
			float originY = (-tlBound.y) / yLength;
			
			// Set our drawing positions
			drawPos = new DrawingPosition(50.0f + (originX * (savedWidth-100)), 
					200.0f + (originY * (savedWidth-100)), 
					0.0f);
			originDrawPos = drawPos.copy();
			
			// Calculate our scaling factor
			scalingFactor = (savedWidth-100) / xLength;
			
			// Finally, add the tail lines
			currentTailLine = new Path();
			currentTailLine.moveTo(drawPos.x, drawPos.y);
			tailLines = new LinkedList<Path>();
			tailLines.add(currentTailLine);
		}
		
		public void initOrReset() {
			visible = true;
			lsystem = null;
			currentCommand = 0;
			
			drawPos = null;
			drawPosStack = new LinkedList<DrawingPosition>();
			
			originDrawPos = null;
			
			state = DrawingState.PREPARE;
			
			tailPaint = new Paint();
			headPaint = new Paint();
			
			readPreferences();
			new LSystemGenerator().execute(lsDesc);
		}

		@Override
		public void onSharedPreferenceChanged(
				SharedPreferences sharedPreferences, String key) {
			if (key != null && 
					(key.equals(WallpaperPreferencesActivity.lsystemKeyName)
					|| key.equals(WallpaperPreferencesActivity.headColorKeyName)
					|| key.equals(WallpaperPreferencesActivity.tailColorKeyName)
					|| key.equals(WallpaperPreferencesActivity.bgColorKeyName)
							) ) {
				initOrReset();
			} else if (key != null && key.equals(WallpaperPreferencesActivity.refreshSpeedKeyName)) {
				refreshSpeed = Integer.parseInt(sharedPreferences.getString(WallpaperPreferencesActivity.refreshSpeedKeyName, "200"));
			}
		}
	} 
}
