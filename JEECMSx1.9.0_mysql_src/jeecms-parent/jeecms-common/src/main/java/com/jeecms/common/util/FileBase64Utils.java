package com.jeecms.common.util;

import sun.misc.BASE64Decoder;

import java.io.*;

public class FileBase64Utils {

    private FileBase64Utils() {
    }

    /**
     * base64字符串转化成图片
     *
     * @param base64Data  图片编码
     * @param imgFilePath 存放路径
     * @return
     * @throws IOException
     */
    public static File generateImage(String base64Data, String imgFilePath) throws IOException {
        if (base64Data == null) {
            return null;
        }
        BASE64Decoder decoder = new BASE64Decoder();
        OutputStream out = null;
        try {
            out = new FileOutputStream(imgFilePath);
            byte[] b = decoder.decodeBuffer(base64Data);
            for (int i = 0; i < b.length; ++i) {
                if (b[i] < 0) {
                    b[i] += 256;
                }
            }
            out.write(b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(out!=null){
                out.flush();
                out.close();
            }
        }
        return new File(imgFilePath);
    }
}
