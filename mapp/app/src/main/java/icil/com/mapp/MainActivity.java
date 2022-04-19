package icil.com.mapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;


// Activity 수명 주기 상태 간의 전환하기 위해 Activity 클래스는 6가지 콜백으로 구성된 핵심 세트
// (onCreate()->시작됨, onStart(), onResume(), onPause(), onStop(), onDestroy())를 제공합니다.
// Activity가 새로운 상태에 들어가면 시스템은 각 콜백을 호출합니다.
// Activity의 복잡한 정도에 따라, 모든 수명 주기 메서드를 구현할 필요가 없는 경우도 있습니다.

public class MainActivity extends AppCompatActivity implements CallbackInterface {
    private TextureView mCameraTextureView;
    private Preview mPreview;
    private TextView mCameraTimerView;
    private ImageView mCameraGridView;
    private LinearLayout mCameraPoseBar;
    private RelativeLayout mRelativeLayout;
    private ImageButton mCameraCaptureButton;
    private ImageButton mCameraGalleyButton;
    private Button mCameraPoseButton;
    private ImageButton mCameraGridButton;
    private ImageButton mCameraCountButton;
    private ImageButton mCameraPose1, mCameraPose2;
    private ImageView mCameraPoseView1, mCameraPoseView2;

    static final int REQUEST_CAMERA = 1;
    static final int REQUEST_STORAGE = 2;
    static int number = 0;

    @Override
    //Activity의 이전 저장 상태가 포함된 Bundle 객체
    //이번에 처음 생성된 Activity인 경우 Bundle 객체의 값은 null

    protected void onCreate(Bundle savedInstanceState) {
        Log.d("태그", "MainActivity-> onCreate() start");
        super.onCreate(savedInstanceState); //부모의 onCreate메소드를 오버라이딩

        //상태바 제거
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main); //부모의 메소드 호출, XML 레이아웃 파일은 파일의 리소스 ID를 전달

        mCameraCaptureButton = (ImageButton) findViewById(R.id.capture); //형변환
        mCameraGalleyButton = (ImageButton) findViewById(R.id.galley);
        mCameraPoseButton = (Button) findViewById(R.id.pose);
        mCameraGridButton = (ImageButton) findViewById(R.id.grid);
        mCameraCountButton = (ImageButton) findViewById(R.id.timer);
        mRelativeLayout    = findViewById(R.id.layout);
        //갤러리
        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),GalleyActivity.class);
                startActivityForResult(intent,101);
            }
        });


        mCameraTextureView = (TextureView) findViewById(R.id.cameraTextureView);
        mCameraTimerView = (TextView) findViewById(R.id.timerView);
        mCameraGridView = (ImageView) findViewById(R.id.gridView);
        mCameraPoseBar = (LinearLayout) findViewById(R.id.posebar);
        mCameraPose1 = (ImageButton) findViewById(R.id.pose1);
        mCameraPoseView1 = (ImageView) findViewById(R.id.poseview1);
        mCameraPose2 = (ImageButton) findViewById(R.id.pose2);
        mCameraPoseView2 = (ImageView) findViewById(R.id.poseview2);


        mPreview = new Preview(this, mCameraTextureView, mCameraCaptureButton, mCameraGalleyButton, mCameraCountButton, mCameraGridButton,mCameraTimerView, mCameraGridView, mCameraPoseButton, mCameraPoseBar, mCameraPose1, mCameraPoseView1,mCameraPose2, mCameraPoseView2, mRelativeLayout);
        mPreview.setOnCallbackListener(this);

        mCameraGridView.setVisibility(View.INVISIBLE);
        mCameraPoseBar.setVisibility(View.INVISIBLE);

        mCameraPoseView1.setVisibility(View.INVISIBLE);
        mCameraPoseView2.setVisibility(View.INVISIBLE);

        int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionStorage == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MainActivity.REQUEST_STORAGE);
        }

        Log.d("태그", "MainActivity-> onCreate() end");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 101){
            if(data != null){
                String name = data.getStringExtra("name");
                    mPreview.onPause();
                    mPreview.openCamera();
                if(name != null){
                    Toast.makeText(this,"응답" + name,Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("태그", "MainActivity-> onRequestPermissionsResult() start");
        switch (requestCode) {
            case REQUEST_CAMERA:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.CAMERA)) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            Log.d("태그", "카메라 권한 있음");
                            mPreview.openCamera();
                        } else {
                            Log.d("태그", "카메라 권한 없음");
                            Toast.makeText(this, "실행할 수 있는 카메라 권한이 있어야 합니다", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
            case REQUEST_STORAGE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            Log.d("태그", "저장소 권한 있음");
                            mPreview.openCamera();
                        } else {
                            Log.d("태그", "저장소 권한 없음");
                            Toast.makeText(this, "실행할 수 있는 저장소 권한이 있어야 합니다", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    }
                }
                break;
        }
        Log.d("태그", "MainActivity-> onRequestPermissionsResult() end");
    }

    @Override
    protected void onResume() {
        Log.d("태그", "MainActivity-> onResume() start");
        super.onResume();
        mPreview.onResume();
        Log.d("태그", "MainActivity-> onResume() end");
    }

    @Override
    protected void onPause() {
        Log.d("태그", "MainActivity-> onPause() start");
        super.onPause();
        mPreview.onPause();
        Log.d("태그", "MainActivity-> onPause() end");
    }

    @Override
    public void onSave(File filePath) {
        Log.d("태그", "MainActivity-> onSave() start");
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(filePath));
        sendBroadcast(intent);
        Log.d("태그", "MainActivity-> onSave() end");

    }
} //public class MainActivity END
