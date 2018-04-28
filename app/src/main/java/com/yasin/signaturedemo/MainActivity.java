package com.yasin.signaturedemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText mContainer;
    private SignatureView mSignView;
    private List<Bitmap> bitmaps = new ArrayList<>();
    private List<File> uploadFiles = new ArrayList<>();
    private TextView tv;
    private Bitmap comma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContainer = (EditText) findViewById(R.id.ll_container);
        mSignView = (SignatureView) findViewById(R.id.id_sign);
        tv = (TextView) findViewById(R.id.tv);
        ImageView ivComma = (ImageView) findViewById(R.id.iv_comma);

        comma = BitmapFactory.decodeResource(this.getResources(), R.drawable.number_punctuation);

        //设置edittext关闭键盘，但显示光标
        disableShowSoftInput(mContainer);

        ivComma.setOnClickListener(this);

        //删除文字图片存储文件夹中的历史图片（实际用的时候，根据情况确定删除的时机）
        deleteBitmap(getExternalCacheDir() + "signature/",true);

        mSignView.setSignatureCallBack(new SignatureView.ISignatureCallBack() {
            @Override
            public void onSignCompeleted(View view, Bitmap bitmap) {
                String fileDir = getExternalCacheDir() + "signature/";
                String path = fileDir + SystemClock.elapsedRealtime() + ".png";
                File file = new File(fileDir);
                if (!file.exists()) {
                    file.mkdir();
                }
                bitmaps.add(bitmap);
                try {
                    mSignView.save(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                uploadFiles.add(new File(path));
                drawBitmaps(bitmap);
                showFiles();
            }
        });
    }

    /**
     * 设置edittext关闭键盘，但显示光标
     */
    public static void disableShowSoftInput(EditText editText) {
        if (android.os.Build.VERSION.SDK_INT <= 10) {
            editText.setInputType(InputType.TYPE_NULL);
        } else {
            Class<EditText> cls = EditText.class;
            Method method;
            try {
                method = cls.getMethod("setShowSoftInputOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {
                // TODO: handle exception
            }

            try {
                method = cls.getMethod("setSoftInputShownOnFocus", boolean.class);
                method.setAccessible(true);
                method.invoke(editText, false);
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    /**
     * 讲文字图片写入edittext
     *
     * @param b 文字图片
     */
    private void drawBitmaps(Bitmap b) {
        ImageSpan imgSpan = new ImageSpan(this, b);
        SpannableString spanString = new SpannableString("i");
        spanString.setSpan(imgSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        String space = " ";//添加空格，解决光标被遮挡问题

        Editable edit = mContainer.getEditableText();//获取EditText的文字
        if (getEditTextCursorIndex(mContainer) < 0 || getEditTextCursorIndex(mContainer) >= edit.length()) {
            edit.append(spanString);//在edittext末尾添加
            edit.append(space);
        } else {
            if (getEditTextCursorIndex(mContainer) % 2 == 0) {
                edit.insert(getEditTextCursorIndex(mContainer), spanString);//光标所在位置插入文字
                edit.insert(getEditTextCursorIndex(mContainer), space);
            } else {
                edit.insert(getEditTextCursorIndex(mContainer), space);
                edit.insert(getEditTextCursorIndex(mContainer), spanString);//光标所在位置插入文字
            }

        }
    }

    /**
     * 展示文字图片存储位置
     */
    private void showFiles() {
        tv.setText(Arrays.toString(uploadFiles.toArray()));
    }

    /**
     * 获取EditText光标所在的位置
     */
    private int getEditTextCursorIndex(EditText mEditText) {
        return mEditText.getSelectionStart();
    }

    /**
     * 删除的点击事件
     */
    public void delete(View view) {
        Log.e("bitmaps.size()", String.valueOf(bitmaps.size()));
        if (bitmaps.size() > 0) {//在有文字图片时才执行删除操作
            deleteText(mContainer);
        }
    }

    /**
     * 向EditText指定光标位置删除字符串
     */
    private void deleteText(EditText mEditText) {
        if (getEditTextCursorIndex(mEditText) == 0) {//光标在首位时不做处理
            return;
        }

        bitmaps.remove(bitmaps.size() - 1);
        File file = uploadFiles.get(uploadFiles.size() - 1);
        uploadFiles.remove(file);
        file.delete();
        showFiles();

        Log.e("CursorIndex", String.valueOf(getEditTextCursorIndex(mEditText)));
        //删除需对加入的空格做处理
        if ((getEditTextCursorIndex(mEditText) % 2) == 1) {//光标在文字图片后，空格前，删除光标前一个和后一个
            mEditText.getText().delete(getEditTextCursorIndex(mEditText) - 1, getEditTextCursorIndex(mEditText) + 1);
        } else {//光标在文字图片前，空格后，删除光标前两个
            mEditText.getText().delete(getEditTextCursorIndex(mEditText) - 2, getEditTextCursorIndex(mEditText));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_comma:
                addPunctuation(comma);
                break;
        }
    }

    /**
     * 添加标点符号
     * @param bitmap 标点图片
     */
    private void addPunctuation(Bitmap bitmap) {
        String fileDir = getExternalCacheDir() + "signature/";
        String path = fileDir + SystemClock.elapsedRealtime() + ".png";
        File file = new File(fileDir);
        if (!file.exists()) {
            file.mkdir();
        }
        bitmaps.add(bitmap);
        uploadFiles.add(new File(path));
        drawBitmaps(bitmap);
        showFiles();
    }

    /**
     * 递归删除图片文件夹下的所有文件及文件夹
     * @param filePath 文件夹路径
     * @param deleteThisPath 是否删除这个路径下的所有文件
     */
    private void deleteBitmap(String filePath, boolean deleteThisPath){
            if (!TextUtils.isEmpty(filePath)) {
                try {
                    File file = new File(filePath);
                    if (file.isDirectory()) { //目录
                        File files[] = file.listFiles();
                        for (int i = 0; i < files.length; i++) {
                            deleteBitmap(files[i].getAbsolutePath(), true);
                        }
                    }
                    if (deleteThisPath) {
                        if (!file.isDirectory()) { //如果是文件，删除
                            file.delete();
                        } else { //目录
                            if (file.listFiles().length == 0) { //目录下没有文件或者目录，删除
                                file.delete();
                            }
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
    }
}