/*
@Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.common.util;

import cn.hutool.http.HtmlUtil;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xiaohui
 * @date 2021/3/11 16:25
 */
public class SubStringUtil {

    public static void main(String[] args) {
        System.out.println(interceptTxt("3213<div style=\">'\" title=\"<'\">131<img>22<dir>测试截取字符串</div>fdaf</div></div>", 6));
    }

    public static String interceptTxt(String txt, Integer num) {
        //如果文本为空直接返回
        if (StringUtils.isBlank(txt)) {
            return "";
        }
        if (num == null) {
            num = 0;
        }
        String regex = "<(?:[^\"'>]|\"[^\"]*\"|'[^']*')*>";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(txt);
        String txt1 = txt;
        List<TxtKeep> txtKeeps = new ArrayList<>();
        //正则匹配到所有html标签并分组
        while (matcher.find()) {
            TxtKeep txtKeep = new TxtKeep();
            //把html标签替换成占位符，便于后面分割
            txt1 = txt1.replace(matcher.group(0), "$jeecms$");
            txtKeep.setHtml(matcher.group(0));
            txtKeeps.add(txtKeep);
        }
        /*
            长度为0说明没有html标签,
            文本长度大于num，则截取，否则直接返回
         */
        if (txtKeeps.isEmpty()) {
            return txt.length() >= num ? txt.substring(0, num) : txt;
        }
        //根据分隔符分组， 存在符号$需要转义，不然会报错
        String[] txtSplits = txt1.split("\\$jeecms\\$");
        int allLength = 0;
        int i = 0;
        //遍历分割后的文本
        for (String txtSplit : txtSplits) {
            TxtKeep txtKeep = txtKeeps.get(i);
            //计算时需要把转义后的文本转义回来，不然长度会有问题
            txtSplit = HtmlUtil.unescape(txtSplit);
            txtKeep.setTxt(txtSplit);
            txtKeep.setSort(i + 1);
            //该段文本长度
            int txtLength = txtSplit.length();
            txtKeep.setLength(txtLength);
            //总共需要截取的长度减去之前处理的总文本长度=当前文本需要截取的长度
            int substringLength = num - allLength;
            //处理的总文本长度
            allLength += txtLength;
            i++;
            //如果处理后的总文本长度大于等于需要截取的文本长度则不需要处理
            if (allLength >= num) {
                txtKeep.setStop(true);
                //当前文本需要截取的长度
                txtKeep.setSubStringLength(substringLength);
                break;
            }
        }
        StringBuilder result = new StringBuilder();
        for (TxtKeep txtKeep : txtKeeps) {
            String lastTxt = txtKeep.getTxt();
            //如果当前文本需要截取的长度 > 0 则截取， 反之直接拼接
            if (txtKeep.getSubStringLength() > 0) {
                String substring = lastTxt.substring(0, txtKeep.getSubStringLength());
                result.append(HtmlUtil.escape(substring));
            } else {
                result.append(HtmlUtil.escape(lastTxt));
            }
            result.append(txtKeep.getHtml());
            if (txtKeep.getStop()) {
                break;
            }
        }
        String s = result.toString();
        //补全单标签
        Document document = Jsoup.parseBodyFragment(s);
        return document.body().html();
    }

    private static class TxtKeep {
        /**
         * html 标签
         */
        private String html;
        /**
         * 序号（貌似没用上）
         */
        private int sort;
        /**
         * 文本
         */
        private String txt;
        /**
         * 文本长度
         */
        private int length;
        /**
         * 是否停止遍历
         */
        private boolean stop = false;
        /**
         * 要截取的字符 0表示不截取 所有的字符都需要
         */
        private int subStringLength = 0;

        public String getHtml() {
            return html;
        }

        public void setHtml(String html) {
            this.html = html;
        }

        public int getSort() {
            return sort;
        }

        public void setSort(int sort) {
            this.sort = sort;
        }

        public String getTxt() {
            return txt;
        }

        public void setTxt(String txt) {
            this.txt = txt;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public boolean getStop() {
            return stop;
        }

        public void setStop(boolean stop) {
            this.stop = stop;
        }

        public int getSubStringLength() {
            return subStringLength;
        }

        public void setSubStringLength(int subStringLength) {
            this.subStringLength = subStringLength;
        }

        @Override
        public String toString() {
            return "TxtKeep{" +
                    "html='" + html + '\'' +
                    ", sort=" + sort +
                    ", txt='" + txt + '\'' +
                    ", length=" + length +
                    ", stop=" + stop +
                    ", subStringLength=" + subStringLength +
                    '}';
        }
    }

}
