package com.liberty.library.util;

import android.content.Context;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.liberty.library.bean.BodyPartF;

import org.xmlpull.v1.XmlPullParser;

/**
 * Created by liberty on 2017/3/14.
 */

public class XmlUtil {
    public static BodyPartF readFromXmlF(Context context, int xmlId){
        BodyPartF bodyPartF=null;
        BodyPartF.Part part=null;
        XmlResourceParser xrp=context.getResources().getXml(xmlId);
        try {
            int event=xrp.getEventType();
            while (event!= XmlResourceParser.END_DOCUMENT){
                switch (event){
                    case XmlResourceParser.START_DOCUMENT:
                        bodyPartF=new BodyPartF();
                        break;
                    case XmlPullParser.START_TAG:
                        if (xrp.getName().equals("Part")){
                            part=new BodyPartF.Part();
                            part.setId(xrp.getAttributeValue(0));
                            part.setDesc(xrp.getAttributeValue(1));
                            Log.d("BodyImageView","id="+part.getId());
                            Log.d("BodyImageView","desc="+part.getDesc());
                            part.setPts(xrp.getAttributeValue(2));
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (xrp.getName().equals("Part")&&part!=null){
                            bodyPartF.putPart(part.getId(),part);
                        }
                        break;
                }
                event=xrp.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bodyPartF;
    }
}
