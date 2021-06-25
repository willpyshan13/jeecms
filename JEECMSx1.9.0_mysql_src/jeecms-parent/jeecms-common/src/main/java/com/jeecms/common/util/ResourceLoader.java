package com.jeecms.common.util;

import cn.hutool.core.io.IoUtil;
import org.springframework.boot.system.ApplicationHome;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class ResourceLoader {


    public static String appHome() {
        ApplicationHome home = new ApplicationHome();
        return home.getDir().getAbsolutePath() + File.separator;
    }

    public static InputStream loadExtResource(String filename)  {
        String appHome = appHome();
        System.out.println(appHome);
        try {
            return new FileInputStream(appHome + filename);
        } catch (FileNotFoundException e) {
            return null;
        }
    }

    public static InputStream loadClasspathResource(String resourceName)  {
        return ResourceLoader.class.getClassLoader().getResourceAsStream(resourceName);
    }

    public static InputStream loadResource(String resourceName) {
        InputStream in = loadExtResource(resourceName);
        return in == null ? loadClasspathResource(resourceName) : in;
    }

    public static String loadResourceStr(String resourceName) {
        byte[] bytes;
        try {
            bytes = IoUtil.readBytes(loadResource(resourceName));
            return new String(bytes);
        } catch (Exception e) {
            return null;
        }
    }
}
