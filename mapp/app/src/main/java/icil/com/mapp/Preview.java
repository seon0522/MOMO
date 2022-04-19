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

    private ImageButton mPose1, mPose2;
    private ImageView mPose1View, mPose2View;

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

    //Preview = 카메라 기능 / TextureView 카메라의 화면을 보여준다
    public Preview(Context context, TextureView textureView, ImageButton captureBtn, ImageButton galleyBtn, ImageButton timerBtn, ImageButton gridBtn, TextView timerView, ImageView gridView, Button poseBtn, LinearLayout poseBar, final ImageButton pose1, final ImageView pose1View,final ImageButton pose2, final ImageView pose2View, RelativeLayout reLayout) {
            Log.d("태그", "Preview-> Preview() start");
            mContext     = context;
        mTextureView = textureView;
        mTimerView = timerView;
        mGridView = gridView;
        mPose1View = pose1View;
        mPose2View = pose2View;
        mRelativeLayout = reLayout;
        mCaptureBtn = captureBtn;
        mGalleyBtn = galleyBtn;
        mPoseBtn = poseBtn;
        mTimerBtn = timerBtn;
        mGridBtn = gridBtn;
        mPoseBar = poseBar;
        mPose1 = pose1;
        mPose2 = pose2;

        //pose1눌렀을 때
        mPose1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pose1On_Off == 0) {
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


        //타이머 모드 on_off
        mTimerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownOn_Off = (countDownOn_Off ==1) ? 0 : 1;
            }
        });

        //포즈모드 on_off
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

        //그리드 모드 on_off
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


        //갤러리 호출
        mGalleyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(MediaStore.Images.Media.CONTENT_TYPE);

                mContext.startActivity(intent);




            }
        });

        // 촬영버튼을 눌렀을 때
        mCaptureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("태그", "Preview-> onClick() start");
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
                Log.d("태그", "Preview-> onClick() end");
            }
        });

        Log.d("태그", "Preview-> Preview() end");
    }


    public void setOnCallbackListener(CallbackInterface callbackListener) {
        Log.d("태그", "Preview-> setOnCallbackListener() start");
        mCallbackInterface = callbackListener;
        Log.d("태그", "Preview-> setOnCallbackListener() end");
    }

    public void openCamera() {
        Log.d("태그", "Preview-> openCamera() Start");

        // 카메라 기능 시키기
        CameraManager manager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            // 전/후면 카메라 정보를 가져온 것처럼 사진 크기에 대한 정보도 Characteristics통해서 가져올 수 있다.
            // 인자 CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            //리턴 타입 StreamConfigurationMap
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(mCameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mPreviewSize = map.getOutputSizes(SurfaceTexture.class)[0];

            /*저장소 권한 동의 - 저장소 권한을 얻지 못한 상태라면 권한 요청을 하도록 하는 코드*/
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
        Log.d("태그", "Preview-> openCamera() End");
    }

    //카메라 다시 켜기
    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d("태그", "Preview-> onSurfaceTextureAvailable() start");
            Log.d("태그", "onSurfaceTextureAvailable, width=" + width + ", height=" + height);
            mWidth = width;
            openCamera();
            Log.d("태그", "Preview-> onSurfaceTextureAvailable() end");
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d("태그", "onSurfaceTextureSizeChanged");
            Log.d("태그", "Preview-> onSurfaceTextureSizeChanged()");
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            //뷰어 파괴됨
            Log.d("태그","Preview-> onSurfaceTextureDestroyed()");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            //화면 바뀌는 거 실시간 감지
        }
    };

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice camera) {
            Log.d("태그", "Preview-> onOpened() Start");
            mCameraDevice = camera;
            startPreview();
            Log.d("태그", "Preview-> onOpened() end");
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.d("태그", "onDisconnected()");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.d("태그", "onError()");
        }
    };

    //프리뷰 가져오기
    protected void startPreview() {
        Log.d("태그", "Preview-> startPreview() Start");
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            Log.d("태그", "startPreview fail, return");
        }

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        if (null == texture) {
            Log.d("태그", "texture is null, return");
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
            //프리뷰 업데이트
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.d("태그", "Preview-> onConfigured() start");
                    mPreviewSession = session;
                    updatePreview();
                    Log.d("태그", "Preview-> onConfigured() end");
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Log.d("태그", "Preview-> onConfigureFailed() start");
                    Toast.makeText(mContext, "onConfigureFailed", Toast.LENGTH_LONG).show();
                    Log.d("태그", "Preview-> onConfigureFailed() end");
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d("태그", "Preview-> startPreview() end");
    }

    protected void updatePreview() {
        Log.d("태그", "Preview-> updatePreview() start");
        if (null == mCameraDevice) {
            Log.d("태그", "updatePreview error, return");
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
        Log.d("태그", "Preview-> updatePreview() end");
    }

    private Runnable mDelayPreviewRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("태그", "Preview-> run() start");
            startPreview();
            Log.d("태그", "Preview-> run() end");
        }
    };

    protected void takePicture() {
        Log.d("태그", "Preview-> takePicture() start");

        if (null == mCameraDevice) { //카메라가 없다
            Log.d("태그", "mCameraDevice is null, return");
            return;
        }

        try { //카메라가 있다

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

            //파일이름
            final File file = new File(Environment.getExternalStorageDirectory() + "/DCIM", "pic_" + dateFormat.format(date) + ".jpg");

            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Log.d("태그", "Preview-> onImageAvailable() start");
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
                    Log.d("태그", "Preview-> onImageAvailable() end");
                }

                private void save(byte[] bytes) throws IOException {
                    Log.d("태그", "Preview-> save() start");

                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                    Log.d("태그", "Preview-> save() end");
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
                    Log.d("태그", "Preview-> onCaptureCompleted() start");
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(mContext, "저장되었습니다^0^:" + file, Toast.LENGTH_SHORT).show();
                    delayPreview.postDelayed(mDelayPreviewRunnable, 1000);
                    mCallbackInterface.onSave(file);
                    startPreview();
                    //캡쳐가 완료되면 저장
                    Log.d("태그", "Preview-> onCaptureCompleted() end");
                }
            };

            mCameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    Log.d("태그", "Preview-> onConfigured() start");
                    try {
                        session.capture(captureBuilder.build(), captureListener, backgroudHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    Log.d("태그", "Preview-> onConfigured() end");
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, backgroudHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d("태그", "Preview-> takePicture() end");
    }

    //다시 실행
    public void setSurfaceTextureListener() {
        Log.d("태그", "Preview-> setSurfaceTextureListener() start");
        mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        Log.d("태그", "Preview-> setSurfaceTextureListener() end");
    }

    public void onResume() {
        Log.d("태그", "Preview-> onResume() start");
        setSurfaceTextureListener();
        Log.d("태그", "Preview-> onResume() end");
    }

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    public void onPause() {
        //화면이 전환될 때 실행
        Log.d("태그", "Preview-> onPause() start");
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
                Log.d("태그", "CameraDevice Close");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            mCameraOpenCloseLock.release(); //오류던 정상이던 무조건 실행
        }
        Log.d("태그", "Preview-> onPause() end");
    }
} //public class Preview END