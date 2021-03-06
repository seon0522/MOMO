package icil.com.mapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Semaphore;

public class Preview extends Thread {

    private Size mPreviewSize;
    private Context mContext;
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private TextureView mTextureView;
    private TextView mTimerView;
    private String mCameraId = "0";
    private ImageButton mCaptureBtn;
    private ImageButton mGalleyBtn;
    private Button mPoseBtn;
    private ImageButton mTimerBtn;
    private ImageButton mGridBtn;
    private RelativeLayout mRelativeLayout;

    private ImageButton mPose1, mPose2,mPose3, mPose4,mPose5;
    private ImageView mPose1View, mPose2View, mPose3View, mPose4View, mPose5View;

    private ImageButton mPose[];
    private ImageView mPoseView[];

    private ImageView mGridView;
    private LinearLayout mPoseBar;

    private int mWidth;
    private CallbackInterface mCallbackInterface;
    int countDownOn_Off = 0;
    int gridOn_Off = 0;
    int poseOn_Off = 0;
    int pose1On_Off = 0;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray(4);

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    //Preview = ????????? ?????? / TextureView ???????????? ????????? ????????????
    public Preview(Context context, TextureView textureView, ImageButton captureBtn, ImageButton galleyBtn, ImageButton timerBtn, ImageButton gridBtn,
                   TextView timerView, ImageView gridView, Button poseBtn, LinearLayout poseBar,
                   ImageButton pose1, ImageView poseView1,
                   ImageButton pose2, ImageView poseView2,
                   ImageButton pose3, ImageView poseView3,
                   ImageButton pose4, ImageView poseView4,
                   ImageButton pose5, ImageView poseView5,RelativeLayout reLayout) {
            Log.d("??????", "Preview-> Preview() start");
            mContext     = context;
        mTextureView = textureView;
        mTimerView = timerView;
        mGridView = gridView;
        mPose1View = poseView1;
        mPose2View = poseView2;
        mPose3View = poseView3;
        mPose4View = poseView4;
        mPose5View = poseView5;

        mRelativeLayout = reLayout;
        mCaptureBtn = captureBtn;
        mGalleyBtn = galleyBtn;
        mPoseBtn = poseBtn;
        mTimerBtn = timerBtn;
        mGridBtn = gridBtn;
        mPoseBar = poseBar;
        mPose1 = pose1;
        mPose2 = pose2;
        mPose3 = pose3;
        mPose4 = pose4;
        mPose5 = pose5;

        //pose1????????? ???
        mPose1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pose1On_Off == 0) {
//                    ??????
                    pose1On_Off = 1;
                    mPose1View.setVisibility(View.VISIBLE);
                    mRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getAction()){
                                case  MotionEvent.ACTION_DOWN :
                                case  MotionEvent.ACTION_MOVE :
                                case  MotionEvent.ACTION_UP :
                                    mPose1View.setX(event.getX());
                                    mPose1View.setY(event.getY());
                            }
                            return true;
                        }
                    });
                } else {
                    pose1On_Off = 0;
                    mPose1View.setVisibility(View.INVISIBLE);
                }
            }
        });
        //pose2????????? ???
        mPose2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pose1On_Off == 0) {
                    pose1On_Off = 1;
                    mPose2View.setVisibility(View.VISIBLE);
                    mRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getAction()){
                                case  MotionEvent.ACTION_DOWN :
                                case  MotionEvent.ACTION_MOVE :
                                case  MotionEvent.ACTION_UP :
                                    mPose2View.setX(event.getX());
                                    mPose2View.setY(event.getY());
                            }
                            return true;
                        }
                    });
                } else {
                    pose1On_Off = 0;
                    mPose2View.setVisibility(View.INVISIBLE);
                }
            }
        });

        //pose3????????? ???
        mPose3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pose1On_Off == 0) {
                    pose1On_Off = 1;
                    mPose3View.setVisibility(View.VISIBLE);
                    mRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getAction()){
                                case  MotionEvent.ACTION_DOWN :
                                case  MotionEvent.ACTION_MOVE :
                                case  MotionEvent.ACTION_UP :
                                    mPose3View.setX(event.getX());
                                    mPose3View.setY(event.getY());
                            }
                            return true;
                        }
                    });
                } else {
                    pose1On_Off = 0;
                    mPose3View.setVisibility(View.INVISIBLE);
                }
            }
        });

        //pose1????????? ???
        mPose4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pose1On_Off == 0) {
                    pose1On_Off = 1;
                    mPose4View.setVisibility(View.VISIBLE);
                    mRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getAction()){
                                case  MotionEvent.ACTION_DOWN :
                                case  MotionEvent.ACTION_MOVE :
                                case  MotionEvent.ACTION_UP :
                                    mPose4View.setX(event.getX());
                                    mPose4View.setY(event.getY());
                            }
                            return true;
                        }
                    });
                } else {
                    pose1On_Off = 0;
                    mPose4View.setVisibility(View.INVISIBLE);
                }
            }
        });


        //pose5????????? ???
        mPose5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pose1On_Off == 0) {
                    pose1On_Off = 1;
                    mPose5View.setVisibility(View.VISIBLE);
                    mRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getAction()){
                                case  MotionEvent.ACTION_DOWN :
                                case  MotionEvent.ACTION_MOVE :
                                case  MotionEvent.ACTION_UP :
                                    mPose5View.setX(event.getX());
                                    mPose5View.setY(event.getY());
                            }
                            return true;
                        }
                    });
                } else {
                    pose1On_Off = 0;
                    mPose5View.setVisibility(View.INVISIBLE);
                }
            }
        });

//        mPose2.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(pose1On_Off == 0) {
//                    pose1On_Off = 1;
//                    mPose2View.setVisibility(View.VISIBLE);
//                    mRelativeLayout.setOnTouchListener(new View.OnTouchListener() {
//                        @Override
//                        public boolean onTouch(View v, MotionEvent event) {
//                            switch (event.getAction()){
//                                case  MotionEvent.ACTION_DOWN :
//                                case  MotionEvent.ACTION_MOVE :
//                                case  MotionEvent.ACTION_UP :
//                                    mPose2View.setX(event.getX());
//                                    mPose2View.setY(event.getY());
//                            }
//                            return true;
//                        }
//                    });
//                } else {
//                    pose1On_Off = 0;
//                    mPose2View.setVisibility(View.INVISIBLE);
//                }
//            }
//        });


        //????????? ?????? on_off
        mTimerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownOn_Off = (countDownOn_Off ==1) ? 0 : 1;
            }
        });

        //???????????? on_off
        mPoseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(poseOn_Off == 0) {
                    poseOn_Off = 1;
                    mPoseBar.setVisibility(View.VISIBLE);
                } else {
                    poseOn_Off = 0;
                    mPoseBar.setVisibility(View.INVISIBLE);
                }
            }
        });

        //????????? ?????? on_off
        mGridBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gridOn_Off == 0) {
                    gridOn_Off = 1;
                    mGridView.setVisibility(View.VISIBLE);
                } else {
                    gridOn_Off = 0;
                    mGridView.setVisibility(View.INVISIBLE);
                }
            }
        });


        //????????? ??????
        mGalleyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);

                mContext.startActivity(intent);




            }
        });

        // ??????????????? ????????? ???
        mCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("??????", "Preview-> onClick() start");
                if(countDownOn_Off == 0){
                    takePicture();
                }
                else{
                    mTimerView.setText("");
                    CountDownTimer countDownTimer = new CountDownTimer(5000,1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            mTimerView.setText(String.format(Locale.getDefault(),"%d",(millisUntilFinished/1000)+1));
                        }
                        @Override
                        public void onFinish() {
                            mTimerView.setText("cheese");
                        }
                }.start();
                    Handler mHandler = new Handler();
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            takePicture();
                            mTimerView.setText("");
                        }
                    },6000);
                }
                Log.d("??????", "Preview-> onClick() end");
            }
        });

        Log.d("??????", "Preview-> Preview() end");
    }


    public void setOnCallbackListener(CallbackInterface callbackListener) {
        Log.d("??????", "Preview-> setOnCallbackListener() start");
        mCallbackInterface = callbackListener;
        Log.d("??????", "Preview-> setOnCallbackListener() end");
    }

    public void openCamera() {
        Log.d("??????", "Preview-> openCamera() Start");

        // ????????? ?????? ?????????
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            // ???/?????? ????????? ????????? ????????? ????????? ?????? ????????? ?????? ????????? Characteristics????????? ????????? ??? ??????.
            // ?????? CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            //?????? ?????? StreamConfigurationMap
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

            /*????????? ?????? ?????? - ????????? ????????? ?????? ?????? ???????????? ?????? ????????? ????????? ?????? ??????*/
            int permissionCamera = ContextCompat.checkSelfPermission(mContext, Manifest.permission.CAMERA);
            int permissionStorage = ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionCamera == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.CAMERA}, MainActivity.REQUEST_CAMERA);
            } else if (permissionStorage == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions((Activity) mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.REQUEST_STORAGE);
            } else {
                manager.openCamera(mCameraId, mStateCallback, null);
            }
            /**************************************************************/

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d("??????", "Preview-> openCamera() End");
    }

    //????????? ?????? ??????
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d("??????", "Preview-> onSurfaceTextureAvailable() start");
            Log.d("??????", "onSurfaceTextureAvailable, width=" + width + ", height=" + height);
            mWidth = width;
            openCamera();
            Log.d("??????", "Preview-> onSurfaceTextureAvailable() end");
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d("??????", "onSurfaceTextureSizeChanged");
            Log.d("??????", "Preview-> onSurfaceTextureSizeChanged()");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            //?????? ?????????
            Log.d("??????","Preview-> onSurfaceTextureDestroyed()");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            //?????? ????????? ??? ????????? ??????
        }
    };

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            Log.d("??????", "Preview-> onOpened() Start");
            mCameraDevice = camera;
            startPreview();
            Log.d("??????", "Preview-> onOpened() end");
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.d("??????", "onDisconnected()");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.d("??????", "onError()");
        }
    };

    //????????? ????????????
    protected void startPreview() {
        Log.d("??????", "Preview-> startPreview() Start");
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            Log.d("??????", "startPreview fail, return");
        }

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if (null == texture) {
            Log.d("??????", "texture is null, return");
            return;
        }

        texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface surface = new Surface(texture);

        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        mPreviewBuilder.addTarget(surface);

        try {
            //????????? ????????????
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.d("??????", "Preview-> onConfigured() start");
                    mPreviewSession = session;
                    updatePreview();
                    Log.d("??????", "Preview-> onConfigured() end");
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.d("??????", "Preview-> onConfigureFailed() start");
                    Toast.makeText(mContext, "onConfigureFailed", Toast.LENGTH_LONG).show();
                    Log.d("??????", "Preview-> onConfigureFailed() end");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d("??????", "Preview-> startPreview() end");
    }

    protected void updatePreview() {
        Log.d("??????", "Preview-> updatePreview() start");
        if (null == mCameraDevice) {
            Log.d("??????", "updatePreview error, return");
        }


        mPreviewBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread thread = new HandlerThread("CameraPreview");
        thread.start();
        Handler backgroundHandler = new Handler(thread.getLooper());


        try {
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d("??????", "Preview-> updatePreview() end");
    }

    private Runnable mDelayPreviewRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("??????", "Preview-> run() start");
            startPreview();
            Log.d("??????", "Preview-> run() end");
        }
    };

    protected void takePicture() {
        Log.d("??????", "Preview-> takePicture() start");

        if (null == mCameraDevice) { //???????????? ??????
            Log.d("??????", "mCameraDevice is null, return");
            return;
        }

        try { //???????????? ??????

            Size[] jpegSizes = null;
            CameraManager cameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            if (map != null) {
                jpegSizes = map.getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }

            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            // Orientation
            int rotation = ((Activity) mContext).getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));

            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_hh_mm_ss");

            //????????????
            final File file = new File(Environment.getExternalStorageDirectory() + "/DCIM", "pic_" + dateFormat.format(date) + ".jpg");

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.d("??????", "Preview-> onImageAvailable() start");
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                            reader.close();
                        }
                    }
                    Log.d("??????", "Preview-> onImageAvailable() end");
                }

                private void save(byte[] bytes) throws IOException {
                    Log.d("??????", "Preview-> save() start");

                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                    Log.d("??????", "Preview-> save() end");
                }
            };

            HandlerThread thread = new HandlerThread("CameraPicture");
            thread.start();
            final Handler backgroudHandler = new Handler(thread.getLooper());
            reader.setOnImageAvailableListener(readerListener, backgroudHandler);

            final Handler delayPreview = new Handler();

            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request, TotalCaptureResult result) {
                    Log.d("??????", "Preview-> onCaptureCompleted() start");
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(mContext, "?????????????????????^0^:" + file, Toast.LENGTH_SHORT).show();
                    delayPreview.postDelayed(mDelayPreviewRunnable, 1000);
                    mCallbackInterface.onSave(file);
                    startPreview();
                    //????????? ???????????? ??????
                    Log.d("??????", "Preview-> onCaptureCompleted() end");
                }
            };

            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.d("??????", "Preview-> onConfigured() start");
                    try {
                        session.capture(captureBuilder.build(), captureListener, backgroudHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    Log.d("??????", "Preview-> onConfigured() end");
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, backgroudHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d("??????", "Preview-> takePicture() end");
    }

    //?????? ??????
    public void setSurfaceTextureListener() {
        Log.d("??????", "Preview-> setSurfaceTextureListener() start");
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        Log.d("??????", "Preview-> setSurfaceTextureListener() end");
    }

    public void onResume() {
        Log.d("??????", "Preview-> onResume() start");
        setSurfaceTextureListener();
        Log.d("??????", "Preview-> onResume() end");
    }

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    public void onPause() {
        //????????? ????????? ??? ??????
        Log.d("??????", "Preview-> onPause() start");
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
                Log.d("??????", "CameraDevice Close");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release(); //????????? ???????????? ????????? ??????
        }
        Log.d("??????", "Preview-> onPause() end");
    }
} //public class Preview END