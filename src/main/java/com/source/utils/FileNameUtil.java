package com.source.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * Created with IntelliJ IDEA.
 *
 * @author Source
 * @date 2021/07/27/10:13
 * @Description:
 */
public class FileNameUtil {

    private static final String IMAGEJPEG = "image/jpeg";

    private static final String VIDEOMP4 = "video/mp4";

    public static boolean isPicture(String objectName) {
        if (StringUtils.isNotBlank(objectName) && IMAGEJPEG.equals(objectName)) {
            return true;
        }
        return false;
    }

    public static boolean isVideo(String objectName) {
        if (StringUtils.isNotBlank(objectName) && VIDEOMP4.equals(objectName)) {
            return true;
        }
        return false;
    }
}
