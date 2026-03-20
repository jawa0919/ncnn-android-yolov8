// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2021 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

package com.tencent.yolov8ncnn;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.io.IOException;

public class MainActivity extends Activity implements SurfaceHolder.Callback {
    public static final int REQUEST_CAMERA = 100;
    public static final int REQUEST_IMAGE = 101;

    private YOLOv8Ncnn yolov8ncnn = new YOLOv8Ncnn();
    private int facing = 0;

    private Spinner spinnerTask;
    private Spinner spinnerModel;
    private Spinner spinnerCPUGPU;
    private int current_task = 0;
    private int current_model = 0;
    private int current_cpugpu = 0;

    private SurfaceView cameraView;

    private ImageView imageView;
    private Bitmap originalBitmap;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        cameraView = (SurfaceView) findViewById(R.id.cameraview);

        cameraView.getHolder().setFormat(PixelFormat.RGBA_8888);
        cameraView.getHolder().addCallback(this);

        Button buttonSwitchCamera = (Button) findViewById(R.id.buttonSwitchCamera);
        buttonSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                cameraView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);

                int new_facing = 1 - facing;

                yolov8ncnn.closeCamera();

                yolov8ncnn.openCamera(new_facing);

                facing = new_facing;
            }
        });
        imageView = (ImageView) findViewById(R.id.imageview);
        Button buttonPickImage = (Button) findViewById(R.id.buttonPickImage);
        buttonPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });

        spinnerTask = (Spinner) findViewById(R.id.spinnerTask);
        spinnerTask.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                if (position != current_task)
                {
                    current_task = position;
                    reload();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });

        spinnerModel = (Spinner) findViewById(R.id.spinnerModel);
        spinnerModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                if (position != current_model)
                {
                    current_model = position;
                    reload();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });

        spinnerCPUGPU = (Spinner) findViewById(R.id.spinnerCPUGPU);
        spinnerCPUGPU.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id)
            {
                if (position != current_cpugpu)
                {
                    current_cpugpu = position;
                    reload();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });

        reload();
    }

    private void reload()
    {
        boolean ret_init = yolov8ncnn.loadModel(getAssets(), current_task, current_model, current_cpugpu);
        if (!ret_init)
        {
            Log.e("MainActivity", "yolov8ncnn loadModel failed");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        yolov8ncnn.setOutputWindow(holder.getSurface());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA);
        }

        yolov8ncnn.openCamera(facing);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        yolov8ncnn.closeCamera();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_IMAGE && null != data.getData()) {
            try {
                Uri uri = data.getData();
                Log.d("MainActivity", "onActivityResult: " + uri);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    originalBitmap = loadBitmapFromUri(uri);
                    if (!originalBitmap.isMutable()) {
                        originalBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    }
                    imageView.setImageBitmap(originalBitmap);
                    cameraView.setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    Bitmap resultBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    yolov8ncnn.detectBitmap(resultBitmap);
                    imageView.setImageBitmap(resultBitmap);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private Bitmap loadBitmapFromUri(Uri uri) throws IOException {
        ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
        Bitmap bitmap = ImageDecoder.decodeBitmap(source, (decoder, info, source1) -> {
            // 设置目标尺寸
            int targetWidth = 640;
            int targetHeight = 640;
            // 计算缩放比例，保持宽高比
            float ratio = Math.min((float) targetWidth / info.getSize().getWidth(),
                    (float) targetHeight / info.getSize().getHeight());
            int newWidth = (int) (info.getSize().getWidth() * ratio);
            int newHeight = (int) (info.getSize().getHeight() * ratio);
            decoder.setTargetSize(newWidth, newHeight);
            decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
        });
        if (bitmap.getConfig() != Bitmap.Config.ARGB_8888) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        } else if (!bitmap.isMutable()) {
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        }
        return bitmap;
    }
}
