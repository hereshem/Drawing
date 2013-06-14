package com.hereshem.drawing;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class DrawActivity extends Activity {

	private PaintView pv;
	protected View layout;
	protected int progress;
	protected Dialog dialog;
	protected float stroke = 6;
	protected boolean changed = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.pv = new PaintView(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(this.pv);
		this.pv.togglePencil(true);
		
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	/*
	 * MENU METHODS
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST+6, 0, "Open");
		menu.add(Menu.NONE, Menu.FIRST+1, 0, "Save");
		menu.add(Menu.NONE, Menu.FIRST+4, 0, "Stroke");
		menu.add(Menu.NONE, Menu.FIRST+2, 0, "Pencil");
		menu.add(Menu.NONE, Menu.FIRST+3, 0, "Eraser");
		menu.add(Menu.NONE, Menu.FIRST+5, 0, "Clear");
				return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case Menu.FIRST+1:
				this.saveToFile(String.valueOf(System.currentTimeMillis()));
				changed = false;
				return true;
				
			case Menu.FIRST+2:
				this.pv.togglePencil(true);
				DrawActivity.this.pv.paint.setStrokeWidth(DrawActivity.this.stroke);
				return true;
	
			case Menu.FIRST+3:
				this.pv.togglePencil(false);
				DrawActivity.this.pv.paint.setStrokeWidth(30);
				return true;
	
			case Menu.FIRST+4:
				this.strokeDialog();
				return true;
	
			case Menu.FIRST+5:
				this.pv.clear();
				changed = false;
				return true;
			case Menu.FIRST+6:
				// ask for save
				Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);//ACTION_GET_CONTENT);//ACTION_PICK "image/*" "file/*"
				photoPickerIntent.setType("image/*");
				startActivityForResult(photoPickerIntent, 99);				
				return true;
		}
		return super.onOptionsItemSelected(item);		
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent returnedIntent) { 
	    super.onActivityResult(requestCode, resultCode, returnedIntent); 
	    switch(requestCode) { 
		    case 99:
		        if(resultCode == RESULT_OK){
		        	//this.pv.clear();
	        	    String[] projection = { MediaStore.Images.Media.DATA };
	                Cursor cursor = managedQuery(returnedIntent.getData(), projection, null, null, null);
	                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	                cursor.moveToFirst();
	                String path = cursor.getString(column_index);
	                //Toast.makeText(getApplicationContext(), "path is " + path, Toast.LENGTH_SHORT).show();
	                
	                //Bitmap bitmap = ;
	                this.pv.bgImage = getScaledBitmap(BitmapFactory.decodeFile(path));
	                //this.pv.clear();
					this.pv.invalidate();
		        }
	    }
	}
	private Bitmap getScaledBitmap(Bitmap bitmap) {
		Display display = getWindowManager().getDefaultDisplay();
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		
		if(width > display.getWidth() || height > display.getHeight()){
			float wrat = width/display.getWidth();
			float hrat = height/display.getHeight();
			if(wrat > hrat){
				height = (int) (height/wrat);
				width = (int) (width/wrat);
			}
			else{
				height = (int) (height/hrat);
				width = (int) (width/hrat);
			}
		}
		return Bitmap.createScaledBitmap(bitmap, width, height, true);
	}

	/*
	 * STROKE SETTING METHODS
	 */
	public void strokeDialog() {
		this.dialog = new Dialog(this);
		this.dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		this.layout = inflater.inflate(R.layout.stroke_dialog,
				(ViewGroup) findViewById(R.id.dialog_root_element));

		SeekBar dialogSeekBar = (SeekBar) layout
				.findViewById(R.id.dialog_seekbar);

		dialogSeekBar.setThumbOffset(convertDipToPixels(9.5f));
		dialogSeekBar.setProgress((int) this.stroke * 2);

		this.setTextView(this.layout, String.valueOf(Math.round(this.stroke)));

		dialogSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// herp
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// derp
			}

			@Override
			public void onProgressChanged(SeekBar seekBark, int progress,
					boolean fromUser) {
				DrawActivity.this.progress = progress / 2;
				DrawActivity.this
						.setTextView(DrawActivity.this.layout, "" + DrawActivity.this.progress);

				Button b = (Button) DrawActivity.this.layout
						.findViewById(R.id.dialog_button);
				b.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						DrawActivity.this.stroke = DrawActivity.this.progress;
						DrawActivity.this.pv.paint.setStrokeWidth(DrawActivity.this.stroke);
						DrawActivity.this.dialog.dismiss();
					}
				});
			}
		});

		dialog.setContentView(layout);
		dialog.show();
	}

	protected void setTextView(View layout, String s) {
		TextView text = (TextView) layout.findViewById(R.id.stroke_text);
		text.setText(s);
	}

	private int convertDipToPixels(float dip) {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		float density = metrics.density;
		return (int) (dip * density);
	}

	/*
	 * END STROKE RELATED METHODS
	 */
	public void saveToFile(String fname) {
		this.pv.setDrawingCacheEnabled(true);
		this.pv.invalidate();
		String path = Environment.getExternalStorageDirectory().toString();
		OutputStream fOut = null;
		final File file = new File(path, "drawing/" + fname + ".jpg");
		file.getParentFile().mkdirs();

		try {
			file.createNewFile();
		} catch (Exception e) {
			//Log.e("draw_save", e.toString());
		}

		try {
			fOut = new FileOutputStream(file);
		} catch (Exception e) {
			//Log.e("draw_save1", e.toString());
		}

		if (this.pv.getDrawingCache() == null) {
			//Log.e("lal", "tis null");
		}

		this.pv.getDrawingCache()
				.compress(Bitmap.CompressFormat.JPEG, 80, fOut);

		try {
			fOut.flush();
			fOut.close();
		} catch (IOException e) {
			//Log.e("draw_save1", e.toString());
		}
		Toast.makeText(DrawActivity.this, "File saved in \"/drawing/"+fname+".jpg\"", Toast.LENGTH_SHORT).show();
		
	}
	
	public class PaintView extends View {

		private Paint paint;
		private Bitmap bmp;
		private Paint bmpPaint;
		private Canvas canvas;
		@SuppressWarnings("unused")
		private Context context;
		private float mX, mY;
		private Path path;
		private static final float TOUCH_TOLERANCE = 0.8f;
		private int colour;
		private Bitmap bgImage; // image that gets loaded
		protected Boolean pencil;

		private PaintView(Context c) {
			super(c);

			setDrawingCacheEnabled(true); // to save images

			this.context = c;
			this.colour = Color.BLACK;

			this.path = new Path();
			this.bmpPaint = new Paint();
			this.paint = new Paint();
			this.paint.setAntiAlias(true);
			this.paint.setDither(true);
			this.paint.setColor(this.colour);
			this.paint.setStyle(Paint.Style.STROKE);
			this.paint.setStrokeJoin(Paint.Join.ROUND);
			this.paint.setStrokeCap(Paint.Cap.ROUND);
			this.paint.setStrokeWidth(6);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			super.onSizeChanged(w, h, oldw, oldh);
			this.bgImage = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			this.bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
			this.canvas = new Canvas(this.bmp);
		}

		private void touchStart(float x, float y) {
			this.path.reset();
			this.path.moveTo(x, y);
			this.mX = x;
			this.mY = y;
		}

		private void touchUp() {
			this.path.lineTo(mX, mY);
			// commit the path to our offscreen
			this.canvas.drawPath(this.path, paint);
			// kill this so we don't double draw
			this.path.reset();
		}

		private void touchMove(float x, float y) {
			float dx = Math.abs(x - this.mX);
			float dy = Math.abs(y - this.mY);
			if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
				// draws a quadratic curve
				this.path.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
				mX = x;
				mY = y;
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent e) {
			float x = e.getX();
			float y = e.getY();

			switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				this.touchStart(x, y);
				this.touchMove(x + 0.8f, y + 0.8f);				
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				this.touchMove(x, y);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				this.touchUp();
				changed = true;
				invalidate();
				break;
			}
			return true;
		}

		// Called on invalidate();
		@Override
		protected void onDraw(Canvas canvas) {
			canvas.drawColor(Color.WHITE);
			canvas.drawBitmap(this.bgImage, 0, 0, this.bmpPaint);
			canvas.drawBitmap(this.bmp, 0, 0, this.bmpPaint);
			canvas.drawPath(this.path, this.paint);
			
		}

		/*
		 * Menu called methods
		 */
		protected void togglePencil(Boolean b) {
			if (b) { // set pencil
				//paint.setXfermode(null);
				paint.setColor(Color.BLACK);
				this.pencil = true;
			} else { // set eraser
				//paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
				paint.setColor(Color.WHITE);
				this.pencil = false;
			}
			//DrawActivity.this.setTitle();
		}

		protected void clear() {
			this.path = new Path(); // empty path
			//this.canvas.drawColor(Color.WHITE);
			if (this.bgImage != null) {
				this.canvas.drawBitmap(this.bgImage, 0, 0, null);
			}
			this.invalidate();
		}
	}

	@Override
	public void finish() {
		if(changed){
		AlertDialog.Builder alert = new AlertDialog.Builder(DrawActivity.this);
		alert.setTitle("Save image?")
			//.setMessage("Image orientation is changed, save it?")
			.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
						DrawActivity.this.saveToFile(String.valueOf(System.currentTimeMillis()));
						changed = false;
						finish();
				}
			})
			.setNegativeButton("No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					//return;
					changed  = false;
					finish();
				}
			})
			.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					return;
				}
			})
			.show();
		}
		else{
			super.finish();
		}		
	}
}