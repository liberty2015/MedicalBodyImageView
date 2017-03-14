package com.liberty.library.bean;

import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.text.TextUtils;
import android.util.Log;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by liberty on 2017/3/14.
 */

public class BodyPartF {
    private String id;
    private String desc;
    private HashMap<String, Part> partHashMap = new HashMap<String, Part>();

    public BodyPartF() {
    }

    public BodyPartF(String id, String desc) {
        this.id = id;
        this.desc = desc;

    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setPartHashMap(HashMap<String, Part> partHashMap) {
        this.partHashMap = partHashMap;
    }

    public HashMap<String, Part> getPartHashMap() {
        return partHashMap;
    }

    public BodyPartF.Part getPart(String key) {
        return partHashMap.get(key);
    }

    public void putPart(String key, BodyPartF.Part part) {
        if (key != null && part != null)
            partHashMap.put(key, part);
    }

    /**
     * xml文件中身体对应区域
     */
    public static class Part implements Serializable {
        private float[] pts;
        private String id;
        private String desc;
        private Path path;
        private boolean isRectF;

        public Part() {

        }

        public Part(String id, String desc, float[] pts) {
            this.id = id;
            this.desc = desc;
            this.pts = pts;
            ptsToPath();
        }

        private void ptsToPath() {
            if (path == null) {
                path = new Path();
            }
            int len = pts.length;
            isRectF = len == 4;
            for (int i = 0; i < len; ) {
                if (i == 0) {
                    path.moveTo(pts[i++], pts[i++]);
                } else if (pts[i] == -1) {
                    i += 2;
                    path.moveTo(pts[i++], pts[i++]);
                } else {
                    Log.d("BodyImageView", "pts=" + pts[i + 1] + "  i=" + i);
                    path.quadTo(pts[i++], pts[i++], pts[i++], pts[i++]);
                }
            }
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getId() {
            return id;
        }

        public String getDesc() {
            return desc;
        }

        public float[] getPts() {
            return pts;
        }

        public void setPts(float[] pts) {
            this.pts = pts;
            ptsToPath();
        }

        public void setPts(String pts) {
            if (!TextUtils.isEmpty(pts)) {
                String[] arr = pts.split(",");
                int len = arr.length;
                this.pts = new float[len];
                for (int i = 0; i < len; i++) {
                    try {
                        this.pts[i] = Float.parseFloat(arr[i]);
                    } catch (Exception e) {
                        this.pts[i] = 0;
                    }
                }
                ptsToPath();
            }
        }

        public void setPath(Path path) {
            this.path = path;
        }

        public Path getPath() {
            return path;
        }

        public boolean isInArea(RectF rectF, float x, float y) {
            boolean resStatus = false;
            if (this.path != null) {
                rectF.setEmpty();
                path.computeBounds(rectF, true);
                if (isRectF) {
                    resStatus = rectF.contains(x, y);
                } else {
                    Region region = new Region();
                    region.setPath(path, region);
                    region.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
                    resStatus = region.contains((int) x, (int) y);
                }
            }
            return resStatus;
        }
    }
}
