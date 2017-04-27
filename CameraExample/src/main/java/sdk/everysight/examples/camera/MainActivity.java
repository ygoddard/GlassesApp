/*
 * This work contains files distributed in Android, such files Copyright (C) 2016 The Android Open Source Project
 *
 * and are Licensed under the Apache License, Version 2.0 (the "License"); you may not use these files except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
*/

package sdk.everysight.examples.camera;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.everysight.carousel.EvsCarouselActivity;
import com.everysight.common.carouselm.CarouselBehavior;
import com.everysight.common.carouselm.ItemInfo;
import com.everysight.common.carouselm.OnCarouselItemClickListener;
import com.everysight.environment.EvsConsts;
import com.everysight.notifications.EvsToast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/*
This is a standard Android camera example
*/
public class MainActivity extends EvsCarouselActivity
{
	private final String TAG = "CameraSample";
	private TextView mCxtCenterLable = null;
	private TextView mMenuLable = null;
	private ArrayList<ItemInfo> mMainMenu = null;
	private CarouselBehavior mMainCarouselBehavior;
	private CameraHandler mCamera;
	private boolean mIsCameraOpen = false;
	private AnimatorSet mAnim;
	private ImageView mImage;

	/******************************************************************/
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_layout);

		mCxtCenterLable = (TextView) this.findViewById(R.id.centerLabel);
		mMenuLable = (TextView) this.findViewById(R.id.menuLabel);
		mImage = (ImageView) this.findViewById(R.id.imageView);
		mCamera = new CameraHandler(this);

		ObjectAnimator scanAnim = ObjectAnimator.ofFloat(mMenuLable, View.ALPHA, 0f).setDuration(1000);
		scanAnim.setRepeatMode(ObjectAnimator.REVERSE);
		scanAnim.setRepeatCount(ObjectAnimator.INFINITE);
		mAnim = new AnimatorSet();
		mAnim.play(scanAnim);

		initMainMenu();

		/**** OpenCV example ****/
		AssetManager assetManager = this.getResources().getAssets();
		String storageDirectory = Environment.getExternalStorageDirectory().toString();
		copyAssetFolder(assetManager, "images", storageDirectory + "/assetsImages");
		Bitmap bitmap = InitImageFromFile();

		if (!OpenCVLoader.initDebug()) {
			Log.d("aa", "Failed to initialize OpenCV.");
		}

		Mat rgba = new Mat();
		Utils.bitmapToMat(bitmap, rgba);
		Imgproc.erode(rgba, rgba,
				Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)),
				new Point(-1,-1),
				1);
		Utils.matToBitmap(rgba, bitmap);
		rgba.release();
		//TODO - to call bitmap.recycle() when finished
		/**** end of OpenCV example ****/
	}

	private Bitmap InitImageFromFile() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inMutable = true;
		String storageDirectory = Environment.getExternalStorageDirectory().toString();
		String imagePath = storageDirectory + "/assetsImages/" + "GOPR5092.jpg";
		return BitmapFactory.decodeFile(imagePath, options);
	}

	private void initMainMenu()
	{
		mMainCarouselBehavior = new CarouselBehavior();
		mMainCarouselBehavior.setSelectionAnimation(CarouselBehavior.CarouselSelectionAnimationType.fullAnimation);
		mMainCarouselBehavior.setOpeningAnimationType(CarouselBehavior.CarouselOpeningAnimationType.applicationMenu);

		mMainMenu = new ArrayList<>();

		ItemInfo item = new ItemInfoExe("Back", getResources().getDrawable(R.drawable.icon_back))
		{
			@Override
			public void execute()
			{
			}
		};
		mMainMenu.add(item);
		 item = new ItemInfoExe("Camera", getResources().getDrawable(R.drawable.ic_app_camera))
		{
			@Override
			public void execute()
			{
				openCamera();
			}
		};
		mMainMenu.add(item);



		item = new ItemInfoExe("Video", getResources().getDrawable(R.drawable.ic_video_camera))
		{
			@Override
			public void execute()
			{
				openVideo();
			}
		};
		mMainMenu.add(item);
	}

	private void openCamera()
	{
		if(mIsCameraOpen)
		{
			EvsToast.show(this,"Camera is already active");
			return;
		}
		mMenuLable.setText("Taking a picture");
		if (!mAnim.isRunning())
		{
			mAnim.start();
		}
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				mIsCameraOpen = true;

				mCamera.takePicture(
						new File(EvsConsts.EVS_PICTURES_DIR, "PIC_" + System.currentTimeMillis() + ".jpg").getAbsolutePath(),
						new CameraHandler.ICameraCallback()
						{
							@Override
							public void pictureTakenEnded(final boolean isOK,final String path)
							{
								mIsCameraOpen = false;
								MainActivity.this.runOnUiThread(new Runnable()
								{
									@Override
									public void run()
									{
										mMenuLable.setText("Tap to open menu");
										if(isOK)
										{
											mImage.setImageURI(Uri.fromFile(new File(path)));
										}
										if (mAnim.isRunning())
										{
											mAnim.end();
											mMenuLable.setAlpha(1);
										}
									}
								});
							}

							@Override
							public void videoTakenEnded(boolean isOK,final String path)
							{

							}
						});
			}
		}).start();
	}

	private void openVideo()
	{
		if(mIsCameraOpen)
		{
			EvsToast.show(this,"Camera is already active");
			return;
		}
		mMenuLable.setText("Recording a video");
		if (!mAnim.isRunning())
		{
			mAnim.start();
		}
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				mCamera.recordVideo(new File(EvsConsts.EVS_VIDEO_DIR, "MOV_" + System.currentTimeMillis() + ".mp4").getAbsolutePath(),
						new CameraHandler.ICameraCallback()
						{
							@Override
							public void pictureTakenEnded(boolean isOK,final String path)
							{

							}

							@Override
							public void videoTakenEnded(boolean isOK,final String path)
							{
								mIsCameraOpen = false;
								MainActivity.this.runOnUiThread(new Runnable()
								{
									@Override
									public void run()
									{
										mMenuLable.setText("Tap to open menu");
										if (mAnim.isRunning())
										{
											mAnim.end();
											mMenuLable.setAlpha(1);
										}
									}
								});
							}
						});
			}
		}).start();
	}


	/******************************************************************/
	@Override
	public void onDestroy()
	{
		// clean up once we're done
		super.onDestroy();

	}

	@Override
	protected void onDownCompleted()
	{
		super.onDownCompleted();
		if(!isCarouselOpened())
		{
			mMenuLable.setVisibility(View.VISIBLE);
		}
	}


	@Override
	public void onTap()
	{
		super.onTap();
		if(!isCarouselOpened())
		{
			openMainMenu();
			return;
		}

	}

	private void openMainMenu()
	{
		mMenuLable.setVisibility(View.GONE);
		openMenuCarousel(true, mMainMenu, (ViewGroup) findViewById(R.id.SelectSettingsCarousel), new OnCarouselItemClickListener()
		{
			@Override
			public void onItemClick(int i, ItemInfo itemInfo)
			{
				((ItemInfoExe)itemInfo).execute();
				closeMenuCarousel();
				mMenuLable.setVisibility(View.VISIBLE);
			}
		},mMainCarouselBehavior);
	}

	private static boolean copyAssetFolder(AssetManager assetManager,
										   String fromAssetPath, String toPath) {
		try {
			String[] files = assetManager.list(fromAssetPath);
			new File(toPath).mkdirs();
			boolean res = true;
			for (String file : files)
				if (file.contains("."))
					res &= copyAsset(assetManager,
							fromAssetPath + "/" + file,
							toPath + "/" + file);
				else
					res &= copyAssetFolder(assetManager,
							fromAssetPath + "/" + file,
							toPath + "/" + file);
			return res;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


	private static boolean copyAsset(AssetManager assetManager,
									 String fromAssetPath, String toPath) {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = assetManager.open(fromAssetPath);
			new File(toPath).createNewFile();
			out = new FileOutputStream(toPath);
			copyFile(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private static void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while((read = in.read(buffer)) != -1){
			out.write(buffer, 0, read);
		}
	}
}
