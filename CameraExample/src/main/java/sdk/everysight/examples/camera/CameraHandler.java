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


import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.opengl.GLES11;
import android.util.Log;

import com.everysight.notifications.EvsToast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Created by eran on 26/01/2017.
 */

public class CameraHandler
{
    private static final int VIDEO_TIME_MS = 10 * 1000;
    private final Activity mActivity;
    public interface ICameraCallback
    {
        void pictureTakenEnded(boolean isOK,String path);
        void videoTakenEnded(boolean isOK,String path);
    }


    public CameraHandler(Activity activity)
    {
        mActivity = activity;
    }
    public void takePicture(final String path,final ICameraCallback cb)
    {
        try
        {

            int[] textures = new int[1];
            GLES11.glGenTextures(1, textures, 0);
            SurfaceTexture texture0 = new SurfaceTexture(textures[0]);

            Camera camera = Camera.open();
            camera.setPreviewTexture(texture0);

            setCameraStillsParams(camera);

            // hack to prevent first picture being bad
            camera.startPreview();
            Thread.sleep(100);

            final Object picLock = new Object();
            camera.setErrorCallback(new Camera.ErrorCallback()
            {
                @Override
                public void onError(int error, Camera camera)
                {
                    EvsToast.show(mActivity,"Picture failed");
                    cb.pictureTakenEnded(false,path);
                    synchronized (picLock)
                    {
                        picLock.notify();
                    }
                }
            });

            camera.takePicture(null, null, new
                    Camera.PictureCallback()
                    {
                        int cnt = 0;
                        @Override
                        public void onPictureTaken(byte[] data, Camera camera)
                        {
                            cnt++;
                            if(cnt==3)//only after first camera opening, take 2 dummy pictures to let the camera auto adjust
                            {
                                SavePictureToFile(path, data);
                                EvsToast.show(mActivity, "Picture taken");
                                cb.pictureTakenEnded(true,path);
                                synchronized (picLock)
                                {
                                    picLock.notify();
                                }
                            }
                            else
                            {
                                camera.takePicture(null,null,this);
                            }

                        }
                    });
            synchronized (picLock)
            {
                picLock.wait(1500);
            }
            camera.stopPreview();
            camera.release();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            cb.pictureTakenEnded(false,path);
            EvsToast.show(mActivity,"Picture exception");
        }
    }

    private void setCameraStillsParams(Camera camera) throws InterruptedException
    {
        Camera.Parameters params = camera.getParameters();

        params.set("exif-make", "Everysight");
        params.set("exif-model", "SDK");
        params.setRecordingHint(false);
        params.setPreviewFpsRange(5000, 15000);
        params.setPreviewSize(320, 240);

        params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        params.setPictureFormat(ImageFormat.JPEG);
        params.setJpegQuality(97);

        params.set("mode", "high-quality");
        params.set("temporal-bracketing", "true");
        params.set("temporal-bracketing-range-negative", "2");
        params.set("temporal-bracketing-range-positive", "2");
        params.setPictureSize(2016, 1512);
        params.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
        params.set("ipp", "nsf");
        params.set("gbce", "true");
        params.set("jpeg-thumbnail-width", "320");
        params.set("jpeg-thumbnail-height", "240");
        params.set("jpeg-thumbnail-quality", "90");
        params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        params.set("exposure", "sports");
        params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        camera.setParameters(params);
        Thread.sleep(50);
    }

    public void recordVideo(final String path,final ICameraCallback cb)
    {
        try
        {
            File f = new File(path);
            if(f.exists())
            {
                f.delete();
            }
            //assure folder exists
            new File(new File(f.getAbsolutePath()).getParent()).mkdirs();
            int[] textures = new int[1];
            GLES11.glGenTextures(1, textures, 0);
            SurfaceTexture texture0 = new SurfaceTexture(textures[0]);

            Camera camera = Camera.open();
            camera.setPreviewTexture(texture0);

            setCameraVideoParams(camera);

            MediaRecorder mrec = new MediaRecorder();
            camera.unlock();

            mrec.setCamera(camera);

            mrec.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mrec.setAudioSource(MediaRecorder.AudioSource.MIC);    // front

            mrec.setMaxDuration(VIDEO_TIME_MS);

            mrec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mrec.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            mrec.setAudioChannels(2);
            mrec.setAudioEncodingBitRate(96000);
            mrec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mrec.setAudioSamplingRate(48000);

            mrec.setVideoEncodingBitRate(20 * 1024 * 1024);
            mrec.setVideoFrameRate(60);
            mrec.setVideoSize(1280, 720);

            mrec.setOutputFile(path);

            mrec.prepare();
            final Object picLock = new Object();
            mrec.setOnInfoListener(new MediaRecorder.OnInfoListener()
            {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra)
                {
                    if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED ||
                            what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED)
                    {
                        MediaScannerConnection.scanFile(mActivity.getApplicationContext(), new String[]{path}, null, null);
                        EvsToast.show(mActivity,"Video Recording finished");
                        cb.videoTakenEnded(true,path);
                        synchronized (picLock)
                        {
                            picLock.notify();
                        }
                    }
                }
            });
            mrec.setOnErrorListener(new MediaRecorder.OnErrorListener()
            {
                @Override
                public void onError(MediaRecorder mr, int what, int extra)
                {
                    EvsToast.show(mActivity,"Video Recording error");
                    cb.videoTakenEnded(false,path);
                    synchronized (picLock)
                    {
                        picLock.notify();
                    }
                }
            });
            mrec.start();

            synchronized (picLock)
            {
                picLock.wait(VIDEO_TIME_MS*2);
            }

            try
            {
                mrec.stop();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
            try
            {
                mrec.release();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }

            camera.release();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            EvsToast.show(mActivity,"Video Recording exception");
            cb.videoTakenEnded(false,path);
        }
    }

    private void setCameraVideoParams(Camera camera) throws InterruptedException
    {
        Camera.Parameters params = camera.getParameters();
        String cameraName = params.get("camera-name");
        Log.e("XXX", "Camera Name = " + cameraName);

        params.setFocusMode(Camera.Parameters.FOCUS_MODE_INFINITY);
        params.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);
        params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        params.set("mode", "video-mode");
        params.set("ipp", "ldc-nsf");
        params.set("gbce", "true"); params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        params.set("exposure", "sports");
        params.set("sharpness", "200");
        params.setPreviewFpsRange(60000, 60000);
        params.setRecordingHint(true);
        camera.setParameters(params);
        Thread.sleep(250);
    }

    private void SavePictureToFile(String pictureFilePath, final byte[] data)
    {
        if (data == null)
        {
            return;
        }

        FileOutputStream fileOutputStream = null;
        try
        {
            File p = new File(pictureFilePath);
            if(p.exists())
            {
                p.delete();
            }
            File parent = p.getParentFile();
            if (parent.exists())
            {
                parent.mkdirs();
            }

            fileOutputStream = new FileOutputStream(pictureFilePath);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return;
        }
        BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
        try
        {
            bos.write(data, 0, data.length);

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        try
        {
            bos.close();
            MediaScannerConnection.scanFile(mActivity.getApplicationContext(), new String[]{pictureFilePath}, null, null);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
