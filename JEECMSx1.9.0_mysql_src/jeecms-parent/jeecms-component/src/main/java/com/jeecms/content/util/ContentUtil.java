package com.jeecms.content.util;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import java.util.Arrays;
import java.util.List;

/**
 * 内容工具类
 * @author: tom
 * @date: 2020/4/26 14:10   
 */
public class ContentUtil {
        public static final List<String> DOC_SUFFIX= Arrays.asList("doc","docx","xls","xlsx","ppt","pptx","txt","pdf");

    /**
     * 是否是可以转换的pdf的文档后缀格式
     * @param ext
     * @return
     */
    public static boolean isDocSuffix(String ext){
            if(DOC_SUFFIX.contains(ext)){
                return true;
            }
            return  false;
        }
}
