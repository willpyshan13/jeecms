package com.jeecms.common.util;

import java.math.BigDecimal;

/**
 * 单位工具类
 *     (重量 * 1000)单位g
 *     (价格 * 100)单位分
 * @author pss
 * @version 1.0
 * @date 2019/11/09
 * @copyright 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class UnitUtils {

    private static final BigDecimal bd100 = BigDecimal.valueOf(100.0);
    private static final BigDecimal bd1000 = BigDecimal.valueOf(1000.0);
    private static final BigDecimal bd10000= BigDecimal.valueOf(10000.0);


    /**
     * 千克转换为克
     * @param num
     * @return long
     * @author pss
     */
    public static long convertKgToG(BigDecimal num) {
        return num.multiply(bd1000).longValue();
    }

    /**
     * 千克转换为克
     *
     * @param num
     * @return long
     * @author pss
     */
    public static long convertKgToG(String num) {
        return convertKgToG(Double.parseDouble(num));
    }

    /**
     * 千克转换为克
     *
     * @param num
     * @return long
     * @author pss
     */
    public static long convertKgToG(double num) {
        return convertKgToG(BigDecimal.valueOf(num));
    }


    /**
     * 克转换为千克
     * @param num
     * @return double
     * @author pss
     */
    public static BigDecimal convertGToKg(long num){
        BigDecimal bdFen = BigDecimal.valueOf(num);
        return convertGToKg(bdFen);
    }

    /**
     * k转千克，保留整数（运费方式为件数时）
     *
     * @param num
     * @return
     */
    public static BigDecimal intConvertGToKg(long num) {
        BigDecimal bdFen = BigDecimal.valueOf(num);
        return intConvertGToKg(bdFen);
    }



    /**
     * 把分转换为元, 并保留2位小数
     *
     * @param fen
     * @return java.math.BigDecimal
     * @author Zhu Kaixiao
     * @date 2019/11/13 14:00
     */
    public static BigDecimal convertFenToYuan(long fen) {
        return convertFenToYuan(fen, 2);
    }

    public static BigDecimal convertFenToYuan(long fen, int scale) {
        BigDecimal bdFen = BigDecimal.valueOf(fen);
        return convertFenToYuan(bdFen, scale);
    }

    public static String convertFenToYuanStr(long fen) {
        BigDecimal bdFen = BigDecimal.valueOf(fen);
        return convertFenToYuan(bdFen).toString();
    }

    /**
     * 把分转换为元, 并保留2位小数
     *
     * @param fen
     * @return java.math.BigDecimal
     * @author Zhu Kaixiao
     * @date 2019/11/13 14:00
     */
    public static BigDecimal convertFenToYuan(double fen) {
        return convertFenToYuan(fen, 2);
    }

    public static BigDecimal convertFenToYuan(double fen, int scale) {
        BigDecimal bdFen = BigDecimal.valueOf(fen);
        return convertFenToYuan(bdFen, scale);
    }

    /**
     * 把分转换为元, 并保留2位小数
     *
     * @param fen
     * @return java.math.BigDecimal
     * @author Zhu Kaixiao
     * @date 2019/11/13 14:00
     */
    public static BigDecimal convertFenToYuan(float fen) {
        return convertFenToYuan(fen, 2);
    }

    public static BigDecimal convertFenToYuan(float fen, int scale) {
        BigDecimal bdFen = BigDecimal.valueOf(fen);
        return convertFenToYuan(bdFen, scale);
    }

    /**
     * 把分转换为元, 并保留2位小数
     *
     * @param fen
     * @return java.math.BigDecimal
     * @author Zhu Kaixiao
     * @date 2019/11/13 14:00
     */
    public static BigDecimal convertFenToYuan(int fen) {
        return convertFenToYuan(fen, 2);
    }

    public static BigDecimal convertFenToYuan(int fen, int scale) {
        BigDecimal bdFen = BigDecimal.valueOf(fen);
        return convertFenToYuan(bdFen, scale);
    }
    /**
     * 把分转换为元, 并保留2位小数
     *
     * @param bdFen
     * @return java.math.BigDecimal
     * @author Zhu Kaixiao
     * @date 2019/11/13 14:00
     */
    public static BigDecimal convertFenToYuan(BigDecimal bdFen) {
        return convertFenToYuan(bdFen, 2);
    }

    public static BigDecimal convertFenToYuan(BigDecimal bdFen, int scale) {
        BigDecimal bdYuan = bdFen.divide(bd100, scale, BigDecimal.ROUND_DOWN);
        return bdYuan;
    }



    /**
     * 把毫转换为元, 并保留4位小数
     *
     * @param hao
     * @return java.math.BigDecimal
     * @author Zhu Kaixiao
     * @date 2019/11/13 14:00
     */
    public static BigDecimal convertHaoToYuan(long hao) {
        return convertHaoToYuan(hao, 4);
    }

    public static BigDecimal convertHaoToYuan(long hao, int scale) {
        BigDecimal bdFen = BigDecimal.valueOf(hao);
        return convertHaoToYuan(bdFen, scale);
    }

    public static String convertHaoToYuanStr(long hao) {
        BigDecimal bdFen = BigDecimal.valueOf(hao);
        return convertHaoToYuan(bdFen).toString();
    }


    /**
     * 把毫转换为元, 并保留4位小数
     *
     * @param hao
     * @return java.math.BigDecimal
     * @author Zhu Kaixiao
     * @date 2019/11/13 14:00
     */
    public static BigDecimal convertHaoToYuan(double hao) {
        return convertHaoToYuan(hao, 4);
    }

    public static BigDecimal convertHaoToYuan(double hao, int scale) {
        BigDecimal bdFen = BigDecimal.valueOf(hao);
        return convertHaoToYuan(bdFen, scale);
    }

    /**
     * 把毫转换为元, 并保留4位小数
     *
     * @param hao
     * @return java.math.BigDecimal
     * @author Zhu Kaixiao
     * @date 2019/11/13 14:00
     */
    public static BigDecimal convertHaoToYuan(float hao) {
        return convertHaoToYuan(hao, 4);
    }

    public static BigDecimal convertHaoToYuan(float hao, int scale) {
        BigDecimal bdFen = BigDecimal.valueOf(hao);
        return convertHaoToYuan(bdFen, scale);
    }


    /**
     * 把毫转换为元，并且4位小数
     * @param hao
     * @return
     */
    public static BigDecimal convertHaoToYuan(int hao) {
        return convertHaoToYuan(hao, 4);
    }

    public static BigDecimal convertHaoToYuan(int hao, int scale) {
        BigDecimal bdFen = BigDecimal.valueOf(hao);
        return convertHaoToYuan(bdFen, scale);
    }

    /**
     * 把毫转换为元, 并保留2位小数
     *
     * @param bdHao
     * @return java.math.BigDecimal
     * @author Zhu Kaixiao
     * @date 2019/11/13 14:00
     */
    public static BigDecimal convertHaoToYuan(BigDecimal bdHao) {
        return convertHaoToYuan(bdHao, 4);
    }

    public static BigDecimal convertHaoToYuan(BigDecimal bdHao, int scale) {
        BigDecimal bdYuan = bdHao.divide(bd10000, scale, BigDecimal.ROUND_DOWN);
        return bdYuan;
    }



    /**
     * 克转千克
     * @param bdFen
     * @return
     */
    public static BigDecimal convertGToKg(BigDecimal bdFen) {
        BigDecimal bdKg = bdFen.divide(bd1000, 3, BigDecimal.ROUND_UNNECESSARY);
        return bdKg;
    }

    /**
     * 克转千克（取整）
     *
     * @param bdFen
     * @return
     */
    public static BigDecimal intConvertGToKg(BigDecimal bdFen) {
        BigDecimal bdKg = bdFen.divide(bd1000, 0, BigDecimal.ROUND_UNNECESSARY);
        return bdKg;
    }

    /**
     * 把元转成分, 只取2位小数
     *
     * @param yuan 元
     * @return long
     * @author Zhu Kaixiao
     * @date 2019/11/13 14:05
     */
    public static long convertYuanToFen(String yuan) {
        return convertYuanToFen(Double.parseDouble(yuan));
    }

    public static long convertYuanToFen(double yuan) {
        BigDecimal bdYuan = BigDecimal.valueOf(yuan);
        return convertYuanToFen(bdYuan);
    }

    public static long convertYuanToFen(float yuan) {
        BigDecimal bdYuan = BigDecimal.valueOf(yuan);
        return convertYuanToFen(bdYuan);
    }

    /**
     * 把元转成分, 只取2位小数
     *
     * @param bdYuan
     * @return long
     * @author Zhu Kaixiao
     * @date 2019/11/13 14:12
     */
    public static long convertYuanToFen(BigDecimal bdYuan) {
        BigDecimal bdFen = bdYuan.multiply(bd100);
        return bdFen.longValue();
    }


    public static long convertYuanToHao(String yuan) {
        return convertYuanToHao(Double.parseDouble(yuan));
    }

    public static long convertYuanToHao(double yuan) {
        BigDecimal bdYuan = BigDecimal.valueOf(yuan);
        return convertYuanToHao(bdYuan);
    }

    /**
     * 把元转成毫, 只取4位小数
     *
     * @param bdYuan
     * @return long
     * @author Zhu Kaixiao
     * @date 2019/11/13 14:12
     */
    public static long convertYuanToHao(BigDecimal bdYuan) {
        BigDecimal bdFen = bdYuan.multiply(bd10000);
        return bdFen.longValue();
    }

    public static void main(String[] args) {
        long l = convertYuanToHao("3.00");
        System.out.println(convertHaoToYuan(l));
    }
}
