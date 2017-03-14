package com.liberty.library;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.StringDef;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import com.liberty.library.bean.BodyPartF;
import com.liberty.library.util.XmlUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by liberty on 2017/3/14.
 */

public class MedicineBodyView extends AppCompatImageView {
    private final String TAG="MedicineBodyView";
    private static final int MAX_ALPHA=255;
    private int alpha=MAX_ALPHA;
    private float radiusInner,radiusMiddel;
    private PointF point;
    private boolean isInArea;
    private BodyPartF body;
    private RectF mEmptyRectF=new RectF();
    private LinkedHashSet<String> mKeys;
    private Paint linePaint;
    private Paint coverPaint;
    private Paint ripplePaintInner,ripplePaintMiddle;
    private @GENDER String gender=MALE_FRONT;
    private Map<String,Bitmap> bodyBitmapList;
    private Map<String,BodyPartF> bodyPartFs;
    private OnClickListener mOnClickListener;

    public static final String MALE_FRONT="male_front";
    public static final String MALE_BACK="male_back";
    public static final String FEMALE_BACK="female_back";
    public static final String FEMALE_FRONT="female_front";

    @StringDef({MALE_FRONT,MALE_BACK,FEMALE_BACK,FEMALE_FRONT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface GENDER{}

    public MedicineBodyView(Context context) {
        super(context);
        init();
    }

    public MedicineBodyView(Context context, AttributeSet set){
        super(context,set);
        init();
    }

    protected boolean checkInArea(MotionEvent event){
        float x=event.getX();
        float y=event.getY();
        Matrix matrix=getImageMatrix();
        float[] values=new float[9];
        matrix.getValues(values);
        float translateX=values[2];
        float translateY=values[5];
        float scaleX=values[0];
        float scaleY=values[4];
        float newX=(x-translateX)/scaleX;
        float newY=(y-translateY)/scaleY;
        Log.d(TAG,"x="+x+"  y="+y);
        HashMap<String,BodyPartF.Part> parts=body.getPartHashMap();
        Set<String> ids=parts.keySet();
        mKeys.clear();
        for (String id:ids){
            if (parts.get(id).isInArea(mEmptyRectF,newX,newY)){
                mKeys.add(id);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action=event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_CANCEL:
            {
//                alpha=MAX_ALPHA;
//                ripplePaintMiddle.setAlpha(alpha);
//                ripplePaintInner.setAlpha(alpha);
//                point.set(event.getX(),event.getY());
                Log.d(TAG, "---onTouchEvent---");
                isInArea = checkInArea(event);
                if (isInArea) {
                    if (mOnClickListener!=null){
                        for (String key:mKeys){
                            mOnClickListener.onClick(this,body.getPart(key));
                        }
                    }
                    getParent().requestDisallowInterceptTouchEvent(true);
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_UP:
            {
                handler.sendEmptyMessage(0);
//                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            }
        }
        return false;
    }

    private void init(){
        linePaint=new Paint();
        linePaint.setAntiAlias(true);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(3);
        linePaint.setColor(getResources().getColor(R.color.line_color));
        coverPaint=new Paint();
        coverPaint.setAntiAlias(true);
        coverPaint.setStyle(Paint.Style.FILL);
        coverPaint.setColor(getResources().getColor(R.color.cover_color));

        point=new PointF();
        float radius= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,15,getResources().getDisplayMetrics());
        radiusInner=radius;
        radiusMiddel=radius;
        bodyPartFs=new HashMap<>();
        bodyBitmapList=new HashMap<>();
        mKeys=new LinkedHashSet<String>();
        body= XmlUtil.readFromXmlF(getContext(), R.xml.male_front);
        bodyPartFs.put(MALE_FRONT,body);
//        body= XmlUtil.readFromXmlF(getContext(), R.xml.male_back);
        InputStream inputStream=null;
        try {
//            inputStream=getResources().getAssets().open("male_back.png");
            /**
             * 从assets中获取男性正面图片并转换为bitmap
             */
            inputStream=getResources().getAssets().open("male_front.png");
            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
            bodyBitmapList.put(MALE_FRONT,bitmap);
//            setScaleType(ScaleType.FIT_CENTER);
            setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void flushState(){
        alpha-=20;
        if (alpha<0){
            alpha=0;
        }
        ripplePaintInner.setAlpha(alpha);
        ripplePaintMiddle.setAlpha(alpha);
    }

    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    invalidate();
                    break;
                default:
                    break;
            }
        }
    };

    /**
     *正面背面转换
     */
    public void exchange(){
        switch (gender){
            case MALE_FRONT:
            {
                gender=MALE_BACK;
                Bitmap bitmap=bodyBitmapList.get(gender);
                body=bodyPartFs.get(gender);
                if (bitmap==null&&body==null){
                    body= XmlUtil.readFromXmlF(getContext(), R.xml.male_back);
                    bodyPartFs.put(gender,body);
                    InputStream inputStream=null;
                    try {
                        inputStream=getResources().getAssets().open("male_back.png");
                        bitmap= BitmapFactory.decodeStream(inputStream);
                        bodyBitmapList.put(gender,bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                setImageBitmap(bitmap);
            }
            break;
            case MALE_BACK:
            {
                gender=MALE_FRONT;
                Bitmap bitmap=bodyBitmapList.get(gender);
                body=bodyPartFs.get(gender);
                if (bitmap==null&&body==null){
                    body= XmlUtil.readFromXmlF(getContext(), R.xml.male_front);
                    bodyPartFs.put(gender,body);
                    InputStream inputStream=null;
                    try {
                        inputStream=getResources().getAssets().open("male_front.png");
                        bitmap= BitmapFactory.decodeStream(inputStream);
                        bodyBitmapList.put(gender,bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                setImageBitmap(bitmap);
            }
            break;
            case FEMALE_FRONT:
            {
                gender=FEMALE_BACK;
                Bitmap bitmap=bodyBitmapList.get(gender);
                body=bodyPartFs.get(gender);
                if (bitmap==null&&body==null){
                    body= XmlUtil.readFromXmlF(getContext(), R.xml.female_back);
                    bodyPartFs.put(gender,body);
                    InputStream inputStream=null;
                    try {
                        inputStream=getResources().getAssets().open("female_back.png");
                        bitmap= BitmapFactory.decodeStream(inputStream);
                        bodyBitmapList.put(gender,bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                setImageBitmap(bitmap);
            }
            break;
            case FEMALE_BACK:
            {
                gender=FEMALE_FRONT;
                Bitmap bitmap=bodyBitmapList.get(gender);
                body=bodyPartFs.get(gender);
                if (bitmap==null&&body==null){
                    body= XmlUtil.readFromXmlF(getContext(), R.xml.female_front);
                    bodyPartFs.put(gender,body);
                    InputStream inputStream=null;
                    try {
                        inputStream=getResources().getAssets().open("female_front.png");
                        bitmap= BitmapFactory.decodeStream(inputStream);
                        bodyBitmapList.put(gender,bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                setImageBitmap(bitmap);
            }
            break;
        }
    }

    /**
     * 性别转换
     */
    public void gender(){
        switch (gender){
            case MALE_FRONT:
            {
                gender=FEMALE_FRONT;
                Bitmap bitmap=bodyBitmapList.get(gender);
                body=bodyPartFs.get(gender);
                if (bitmap==null&&body==null){
                    body= XmlUtil.readFromXmlF(getContext(), R.xml.female_front);
                    bodyPartFs.put(gender,body);
                    InputStream inputStream=null;
                    try {
                        inputStream=getResources().getAssets().open("female_front.png");
                        bitmap= BitmapFactory.decodeStream(inputStream);
                        bodyBitmapList.put(gender,bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                setImageBitmap(bitmap);
            }
            break;
            case MALE_BACK:{
                gender=FEMALE_BACK;
                Bitmap bitmap=bodyBitmapList.get(gender);
                body=bodyPartFs.get(gender);
                if (bitmap==null&&body==null){
                    body= XmlUtil.readFromXmlF(getContext(), R.xml.female_back);
                    bodyPartFs.put(gender,body);
                    InputStream inputStream=null;
                    try {
                        inputStream=getResources().getAssets().open("female_back.png");
                        bitmap= BitmapFactory.decodeStream(inputStream);
                        bodyBitmapList.put(gender,bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                setImageBitmap(bitmap);
            }
            break;
            case FEMALE_FRONT:{
                gender=MALE_FRONT;
                Bitmap bitmap=bodyBitmapList.get(gender);
                body=bodyPartFs.get(gender);
                if (bitmap==null&&body==null){
                    body= XmlUtil.readFromXmlF(getContext(), R.xml.male_front);
                    bodyPartFs.put(gender,body);
                    InputStream inputStream=null;
                    try {
                        inputStream=getResources().getAssets().open("male_front.png");
                        bitmap= BitmapFactory.decodeStream(inputStream);
                        bodyBitmapList.put(gender,bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                setImageBitmap(bitmap);
            }
            break;
            case FEMALE_BACK:
            {
                gender=MALE_BACK;
                Bitmap bitmap=bodyBitmapList.get(gender);
                body=bodyPartFs.get(gender);
                if (bitmap==null&&body==null){
                    body= XmlUtil.readFromXmlF(getContext(), R.xml.male_back);
                    bodyPartFs.put(gender,body);
                    InputStream inputStream=null;
                    try {
                        inputStream=getResources().getAssets().open("male_back.png");
                        bitmap= BitmapFactory.decodeStream(inputStream);
                        bodyBitmapList.put(gender,bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                setImageBitmap(bitmap);
            }
            break;
        }
    }

    public void setOnClickListener(OnClickListener listener){
        mOnClickListener=listener;
    }

    public interface OnClickListener{
        public void onClick(View view, BodyPartF.Part part);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInArea){
            for (String key:mKeys){
                BodyPartF.Part part=body.getPart(key);
                    if (part!=null){
                        Path path=part.getPath();
                        Path path1=new Path();
                        path1.addPath(path);
                        path1.transform(getImageMatrix());
                        canvas.drawPath(path1,coverPaint);
                        canvas.drawPath(path1,linePaint);
                    }

            }
        }
    }

    /**
     * 获取当前性别的字符串值
     * @return
     */
    public String getGender(){
        String genderStr=gender;
        Log.d("xxxxx","genderstr="+genderStr);
        if (genderStr.contains("female")){
            return "female";
        }else if (genderStr.contains("female")){
            return "male";
        }
        return "male";
    }
}
