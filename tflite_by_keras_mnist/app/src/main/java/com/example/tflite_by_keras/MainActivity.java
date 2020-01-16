package com.example.tflite_by_keras;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private static final int FROM_ALBUM = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //인텐트의 결과는 onActivityResult함수에서 수신


        findViewById(R.id.button).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(intent.ACTION_GET_CONTENT);
                startActivityForResult(intent,FROM_ALBUM);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode != FROM_ALBUM || resultCode != RESULT_OK) {
            return;
        }
        try {
            //선택한 이미지에서 비트맵 생성
            InputStream stream = getContentResolver().openInputStream(data.getData());
            Bitmap bmp = BitmapFactory.decodeStream(stream);
            stream.close();

            ImageView iv = findViewById(R.id.photo);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setImageBitmap(bmp);

            float[][] bytes_img = new float[1][784];

            for (int y = 0; y < 28; y++) {
                for (int x = 0; x < 28; x++) {
                    int pixel = bmp.getPixel(x, y);
                    bytes_img[0][y * 28 + x] = (pixel & 0xff) / (float) 255;
                }
            }
            Interpreter tf_lite = getTfliteInterpreter("mnist.tflite");

            float[][] output = new float[1][10];
            tf_lite.run(bytes_img, output);

            Log.d("predict", Arrays.toString(output[0]));

            //텍스트뷰 10개. 0~9 사이의 숫자 예측
            int[] id_array = {R.id.result0, R.id.result1, R.id.result2, R.id.result3, R.id.result4,
                    R.id.result5, R.id.result6, R.id.result7, R.id.result8, R.id.result9};


            int result = 0;

            for (int i = 0; i < 10; i++) {
                TextView tv = findViewById(id_array[i]);
                tv.setText(String.format("%.5f", output[0][i]));
                if(result < 10 * output[0][i])
                    result = i;
            }
            TextView displayResult = findViewById(R.id.result);
            displayResult.setText("예측 값 : " +result);

        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    private Interpreter getTfliteInterpreter(String modelPath){
        try{
            return new Interpreter(loadModelFile(MainActivity.this,modelPath));
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    //모델을 읽어오는 함수, tflite 홈페이지에 수록.
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}
