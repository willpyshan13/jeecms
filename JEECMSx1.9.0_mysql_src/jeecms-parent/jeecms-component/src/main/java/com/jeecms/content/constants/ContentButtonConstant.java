/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.constants;

import com.google.common.collect.ImmutableMap;

import java.util.*;

import static com.jeecms.common.constants.WebConstants.ADMIN_PREFIX;

/**
 * 内容按钮常量
 *
 * @author: chenming
 * @date: 2020/2/27 22:28
 */
public class ContentButtonConstant {
    /**
     * 强制发布
     */
    public static final int OPERATE_FORCE_PUBLISH = 20;
    /**
     * 撤回(撤回后为初稿状态)
     */
    public static final int OPERATE_RECALL = 21;
    /**
     * 预览
     */
    public static final int OPERATE_PREVIEW = 22;
    /**
     * 浏览
     */
    public static final int OPERATE_BROWSE = 23;
    /**
     * 推送到站群
     */
    public static final int OPERATE_PUSH_SITES = 24;
    /**
     * 推送到微信
     */
    public static final int OPERATE_PUSH_WECHAT = 25;
    /**
     * 推送到微博
     */
    public static final int OPERATE_PUSH_WEIBO = 26;
    /**
     * 内容删除
     */
    public static final int OPERATE_DELETE = 27;
    /**
     * 置顶
     */
    public static final int OPERATE_STICKY = 28;
    /**
     * 取消置顶
     */
    public static final int OPERATE_NOT_STICKY = 29;
    /**
     * 复制
     */
    public static final int OPERATE_COPY = 30;
    /**
     * 引用
     */
    public static final int OPERATE_QUOTE = 31;
    /**
     * 取消引用
     */
    public static final int OPERATE_NOT_QUOTE = 32;
    /**
     * 排序
     */
    public static final int OPERATE_SORT = 33;
    /**
     * 相关内容
     */
    public static final int OPERATE_RELATED_CONTENT = 34;
    /**
     * 版本
     */
    public static final int OPERATE_VERSION = 35;
    /**
     * 操作记录
     */
    public static final int OPERATE_RECORDING = 36;
    /**
     * 出档
     */
    public static final int OPERATE_OUTOFFILE = 37;
    /**
     * 浏览记录
     */
    public static final int OPERATE_BROWSE_RECORDS = 38;

    /**
     * 保存：存为初稿、存为草稿
     */
    public static List<Integer> SAVE = Arrays.asList(ContentConstant.STATUS_FIRST_DRAFT, ContentConstant.STATUS_DRAFT);
    /**
     * 操作行为1：撤回(撤回后为初稿状态)、发布、提交审核、强制发布、下线
     */
    public static List<Integer> OPERTING_ONE = Arrays.asList(ContentButtonConstant.OPERATE_RECALL, ContentConstant.STATUS_PUBLISH,
            ContentConstant.STATUS_FLOWABLE, ContentButtonConstant.OPERATE_FORCE_PUBLISH, ContentConstant.STATUS_NOSHOWING);
    /**
     * 预览、浏览
     */
//    public static List<Integer> SCAN = Arrays.asList(ContentButtonConstant.OPERATE_PREVIEW, ContentButtonConstant.OPERATE_BROWSE);
    public static List<Integer> SCAN = Arrays.asList(ContentButtonConstant.OPERATE_PREVIEW);

    /**
     * 设置类型
     */
    public static List<Integer> SETTYPE = new ArrayList<Integer>();
    /**
     * 推送：推送到站群、推送到微信、推送到微博
     */
    public static List<Integer> PUSH = Arrays.asList(ContentButtonConstant.OPERATE_PUSH_SITES, ContentButtonConstant.OPERATE_PUSH_WECHAT,
            ContentButtonConstant.OPERATE_PUSH_WEIBO);
    /**
     * 操作行为2：删除
     */
    public static List<Integer> OPERTING_TWO = Arrays.asList(ContentButtonConstant.OPERATE_DELETE);
    /**
     * 更多：置顶、取消置顶、复制、引用、取消引用、排序、相关内容、版本、操作记录、归档、出档
     */
//    public static List<Integer> MORE = Arrays.asList(ContentButtonConstant.OPERATE_STICKY, ContentButtonConstant.OPERATE_NOT_STICKY,
//            ContentButtonConstant.OPERATE_COPY, ContentButtonConstant.OPERATE_QUOTE, ContentButtonConstant.OPERATE_NOT_QUOTE,
//            ContentButtonConstant.OPERATE_SORT, ContentButtonConstant.OPERATE_RELATED_CONTENT, ContentButtonConstant.OPERATE_VERSION,
//            ContentButtonConstant.OPERATE_RECORDING, ContentConstant.STATUS_PIGEONHOLE, ContentButtonConstant.OPERATE_OUTOFFILE);
    public static List<Integer> MORE = Arrays.asList(ContentButtonConstant.OPERATE_STICKY, ContentButtonConstant.OPERATE_NOT_STICKY,
            ContentButtonConstant.OPERATE_COPY, ContentButtonConstant.OPERATE_QUOTE, ContentButtonConstant.OPERATE_NOT_QUOTE,
            ContentButtonConstant.OPERATE_SORT, ContentButtonConstant.OPERATE_RELATED_CONTENT, ContentButtonConstant.OPERATE_VERSION,
            ContentButtonConstant.OPERATE_RECORDING, ContentConstant.STATUS_PIGEONHOLE,OPERATE_BROWSE_RECORDS);

    /**
     * 按钮图标
     */
    public static final Map<Integer, String> BUTTON_ICON = ImmutableMap.<Integer, String>builder().put(ContentConstant.STATUS_FIRST_DRAFT, "baocun")
            .put(ContentConstant.STATUS_DRAFT, "cunweicaogao").put(ContentButtonConstant.OPERATE_RECALL, "huanyuan")
            .put(ContentConstant.STATUS_PUBLISH, "fabu").put(ContentConstant.STATUS_FLOWABLE, "shenhe").put(ContentButtonConstant.OPERATE_QUOTE, "yinyong")
            .put(ContentButtonConstant.OPERATE_FORCE_PUBLISH, "fabu").put(ContentConstant.STATUS_NOSHOWING, "xiaxian")
            .put(ContentButtonConstant.OPERATE_PREVIEW, "yulang").put(ContentButtonConstant.OPERATE_BROWSE, "liulan")
            .put(ContentButtonConstant.OPERATE_PUSH_SITES, "tuisongdaozhanqun").put(ContentButtonConstant.OPERATE_PUSH_WECHAT, "tuisongdaoweixin")
            .put(ContentButtonConstant.OPERATE_PUSH_WEIBO, "tuijiandaoweibo").put(ContentButtonConstant.OPERATE_DELETE, "delete")
            .put(ContentButtonConstant.OPERATE_STICKY, "zhiding").put(ContentButtonConstant.OPERATE_NOT_STICKY, "zhiding")
            .put(ContentButtonConstant.OPERATE_COPY, "fuzhilaiyuanlangmu").put(ContentButtonConstant.OPERATE_NOT_QUOTE, "quxiaoyinyong")
            .put(ContentButtonConstant.OPERATE_SORT, "paixu1").put(ContentButtonConstant.OPERATE_RELATED_CONTENT, "neirongguanli")
            .put(ContentButtonConstant.OPERATE_VERSION, "banben").put(ContentButtonConstant.OPERATE_RECORDING, "caozuojilu")
            .put(ContentConstant.STATUS_PIGEONHOLE, "guidang").put(ContentButtonConstant.OPERATE_OUTOFFILE, "chudang")
            .put(OPERATE_BROWSE_RECORDS,"liulan").build();

    /**
     * 按钮text显示文本
     */
    public static final Map<Integer, String> BUTTON_TEXT = ImmutableMap.<Integer, String>builder().put(ContentConstant.STATUS_FIRST_DRAFT, "存为初稿")
            .put(ContentConstant.STATUS_DRAFT, "存为草稿").put(ContentButtonConstant.OPERATE_RECALL, "撤回(撤回后为初稿状态)")
            .put(ContentConstant.STATUS_PUBLISH, "发布").put(ContentConstant.STATUS_FLOWABLE, "提交审核").put(ContentButtonConstant.OPERATE_QUOTE, "引用")
            .put(ContentButtonConstant.OPERATE_FORCE_PUBLISH, "强制发布").put(ContentConstant.STATUS_NOSHOWING, "下线")
            .put(ContentButtonConstant.OPERATE_PREVIEW, "预览").put(ContentButtonConstant.OPERATE_BROWSE, "浏览")
            .put(ContentButtonConstant.OPERATE_PUSH_SITES, "推送到站群").put(ContentButtonConstant.OPERATE_PUSH_WECHAT, "推送到微信")
            .put(ContentButtonConstant.OPERATE_PUSH_WEIBO, "推送到微博").put(ContentButtonConstant.OPERATE_DELETE, "删除")
            .put(ContentButtonConstant.OPERATE_STICKY, "置顶").put(ContentButtonConstant.OPERATE_NOT_STICKY, "取消置顶")
            .put(ContentButtonConstant.OPERATE_COPY, "复制").put(ContentButtonConstant.OPERATE_NOT_QUOTE, "取消引用")
            .put(ContentButtonConstant.OPERATE_SORT, "排序").put(ContentButtonConstant.OPERATE_RELATED_CONTENT, "相关内容")
            .put(ContentButtonConstant.OPERATE_VERSION, "版本").put(ContentButtonConstant.OPERATE_RECORDING, "操作记录")
            .put(ContentConstant.STATUS_PIGEONHOLE, "归档").put(ContentButtonConstant.OPERATE_OUTOFFILE, "出档")
            .put(OPERATE_BROWSE_RECORDS,"访问记录").build();


    public interface Api {
        List<Integer> CONTENT_UPDATE = Arrays.asList(ContentConstant.STATUS_FIRST_DRAFT,ContentConstant.STATUS_DRAFT,ContentConstant.STATUS_PUBLISH,ContentButtonConstant.OPERATE_FORCE_PUBLISH);
        Map<Integer,Map<String,String>> CONTENT_UPDATE_STATUS = ImmutableMap.<Integer, Map<String,String>>builder()
                .put(ContentButtonConstant.OPERATE_RECALL,ImmutableMap.of(ADMIN_PREFIX + "/task/revoke","GET"))
                .put(ContentConstant.STATUS_FLOWABLE,ImmutableMap.of(ADMIN_PREFIX + "/content/submit","POST"))
                .put(ContentConstant.STATUS_NOSHOWING,ImmutableMap.of(ADMIN_PREFIX + "/contentext/status","POST"))
                .put(ContentButtonConstant.OPERATE_PUSH_SITES,ImmutableMap.of(ADMIN_PREFIX + "/content/push/sites","PUT"))
                .put(ContentButtonConstant.OPERATE_PUSH_WECHAT,ImmutableMap.of(ADMIN_PREFIX + "/contentext/preview","POST"))
                .put(ContentButtonConstant.OPERATE_PUSH_WEIBO,ImmutableMap.of(ADMIN_PREFIX + "/weiboarticlepush","POST"))
                .put(ContentButtonConstant.OPERATE_STICKY,ImmutableMap.of(ADMIN_PREFIX + "/contentext/top","POST"))
                .put(ContentButtonConstant.OPERATE_NOT_STICKY,ImmutableMap.of(ADMIN_PREFIX + "/contentext/top","POST"))
                .put(ContentButtonConstant.OPERATE_QUOTE,ImmutableMap.of(ADMIN_PREFIX + "/contentext/quote","POST"))
                .put(ContentButtonConstant.OPERATE_NOT_QUOTE,ImmutableMap.of(ADMIN_PREFIX + "/contentext/quote","POST"))
                .put(ContentButtonConstant.OPERATE_SORT,ImmutableMap.of(ADMIN_PREFIX + "/contentext/sort","POST"))
                .put(ContentConstant.STATUS_PIGEONHOLE,ImmutableMap.of(ADMIN_PREFIX + "/contentext/status","POST"))
                .put(ContentButtonConstant.OPERATE_OUTOFFILE,ImmutableMap.of(ADMIN_PREFIX + "/contentext/file","POST"))
                .put(ContentButtonConstant.OPERATE_DELETE,ImmutableMap.of(ADMIN_PREFIX + "/contentext/rubbish","POST"))
                .build();

    }

    public static final Map<Integer,String> FONT_END_NEED_KEY = ImmutableMap.<Integer,String>builder()
            .put(ContentConstant.STATUS_DRAFT,"3")
            .put(ContentConstant.STATUS_FIRST_DRAFT,"1")
            .put(OPERATE_RECALL,"17")
            .put(ContentConstant.STATUS_PUBLISH,"2")
            .put(ContentConstant.STATUS_FLOWABLE,"12")
            .put(OPERATE_FORCE_PUBLISH,"11")
            .put(ContentConstant.STATUS_NOSHOWING,"13")
            .put(OPERATE_PREVIEW,"5")
            .put(OPERATE_BROWSE,"6")
            .put(OPERATE_DELETE,"15")
            .put(OPERATE_VERSION,"7")
            .put(OPERATE_RECORDING,"8")
            .put(ContentConstant.STATUS_PIGEONHOLE,"14")
            .put(OPERATE_OUTOFFILE,"16")
            .put(OPERATE_BROWSE_RECORDS,"18").build();

    public static final List<OperatePiece> SAVE_OPERATEPIECE = Arrays.asList(OperatePiece.savePiece,OperatePiece.opertingOnePiece);

    public static final List<Integer> SAVE_WORKFLOW_OPERATE = Collections.singletonList(ContentConstant.STATUS_FLOWABLE);

    public static final List<Integer> SAVE_OPERATE = Collections.singletonList(ContentConstant.STATUS_PUBLISH);

    private static final String BUTTON_KEY_DROPDOWN = "dropdown";

    private static final String BUTTON_KEY_LINK = "link";

    public static final List<Integer> FORECE_RELEASE_STATUS = Arrays.asList(ContentConstant.STATUS_SMART_AUDIT_SUCCESS,ContentConstant.STATUS_SMART_AUDIT_FAILURE);

    /**
     * 操作
     */
    public enum OperatePiece{
        savePiece,
        opertingOnePiece,
        scanPiece,
        settypePiece,
        pushPiece,
        opertingTwoPiece,
        morePiece;

        private static Map<OperatePiece,List<Integer>> pieces = new EnumMap<>(OperatePiece.class);

        private static List<OperatePiece> dropdowns = Arrays.asList(savePiece,scanPiece,settypePiece,pushPiece,morePiece);

        private static List<OperatePiece> notModifyPiece = Arrays.asList(opertingOnePiece,scanPiece,morePiece);

        private static Map<Integer,List<OperatePiece>> statusOperatePiece = new HashMap<>();

        private static Map<OperatePiece,String> pieceIcons = new EnumMap<>(OperatePiece.class);

        private static Map<OperatePiece,String> pieceTexts = new EnumMap<>(OperatePiece.class);

        static {
            pieces.put(savePiece,SAVE);
            pieces.put(opertingOnePiece,OPERTING_ONE);
            pieces.put(scanPiece,SCAN);
            pieces.put(settypePiece,SETTYPE);
            pieces.put(pushPiece,PUSH);
            pieces.put(opertingTwoPiece,OPERTING_TWO);
            pieces.put(morePiece,MORE);

            pieceIcons.put(savePiece,"baocun");
            pieceIcons.put(scanPiece,"yulang");
            pieceIcons.put(settypePiece,"shezhileixing");
            pieceIcons.put(pushPiece,"icontuisong");

            pieceTexts.put(savePiece,"保存");
            pieceTexts.put(scanPiece,"预览");
            pieceTexts.put(settypePiece,"设置类型");
            pieceTexts.put(pushPiece,"推送");
            pieceTexts.put(morePiece,"更多");

            statusOperatePiece.put(ContentConstant.STATUS_DRAFT,Arrays.asList(savePiece,opertingOnePiece,scanPiece,settypePiece,pushPiece,opertingTwoPiece,morePiece));
            statusOperatePiece.put(ContentConstant.STATUS_FIRST_DRAFT,Arrays.asList(savePiece,opertingOnePiece,scanPiece,settypePiece,pushPiece,opertingTwoPiece,morePiece));
            statusOperatePiece.put(ContentConstant.STATUS_FLOWABLE,Arrays.asList(opertingOnePiece,scanPiece,opertingTwoPiece));
            statusOperatePiece.put(ContentConstant.STATUS_WAIT_PUBLISH,Arrays.asList(savePiece,opertingOnePiece,scanPiece,settypePiece,pushPiece,opertingTwoPiece,morePiece));
            statusOperatePiece.put(ContentConstant.STATUS_PUBLISH,Arrays.asList(savePiece,opertingOnePiece,scanPiece,settypePiece,pushPiece,opertingTwoPiece,morePiece));
            statusOperatePiece.put(ContentConstant.STATUS_BACK,Arrays.asList(savePiece,opertingOnePiece,scanPiece,settypePiece,pushPiece,opertingTwoPiece,morePiece));
            statusOperatePiece.put(ContentConstant.STATUS_NOSHOWING,Arrays.asList(savePiece,opertingOnePiece,scanPiece,settypePiece,pushPiece,opertingTwoPiece,morePiece));
            statusOperatePiece.put(ContentConstant.STATUS_SMART_AUDIT_SUCCESS,Arrays.asList(savePiece,opertingOnePiece,scanPiece,settypePiece,pushPiece,opertingTwoPiece,morePiece));
            statusOperatePiece.put(ContentConstant.STATUS_SMART_AUDIT_FAILURE,Arrays.asList(savePiece,opertingOnePiece,scanPiece,settypePiece,pushPiece,opertingTwoPiece,morePiece));
            statusOperatePiece.put(ContentConstant.STATUS_PIGEONHOLE,Collections.singletonList(morePiece));
            statusOperatePiece.put(ContentConstant.STATUS_SMART_AUDIT,Collections.singletonList(opertingTwoPiece));
        }

        public static List<Integer> getPieces(OperatePiece operate) {
            if (notModifyPiece.contains(operate)) {
                return new ArrayList<>(pieces.get(operate));
            }
            return pieces.get(operate);
        }

        public static String getContentButtonKey(OperatePiece operate) {
            if (dropdowns.contains(operate)) {
                return BUTTON_KEY_DROPDOWN;
            }
            return BUTTON_KEY_LINK;
        }

        public static List<OperatePiece> getStatusOperatePiece(Integer contentStatus) {
            return statusOperatePiece.get(contentStatus);
        }

        public static String getPieceText(OperatePiece operatePiece) {
            return pieceTexts.get(operatePiece);
        }

        public static String getPieceIcon(OperatePiece operatePiece) {
            return pieceIcons.get(operatePiece);
        }
    }

    public static Map<Integer,String> BUTTON_POP_UPS_TYPE = ImmutableMap.<Integer,String>builder()
            .put(ContentButtonConstant.OPERATE_PUSH_SITES,"Pushstation")
            .put(ContentButtonConstant.OPERATE_PUSH_WECHAT,"Pushweixin")
            .put(ContentButtonConstant.OPERATE_PUSH_WEIBO,"Pushweibo")
            .put(ContentButtonConstant.OPERATE_STICKY,"Istop")
            .put(ContentButtonConstant.OPERATE_NOT_STICKY,"Notop")
            .put(ContentButtonConstant.OPERATE_QUOTE,"Quote")
            .put(ContentButtonConstant.OPERATE_NOT_QUOTE,"Dereference")
            .put(ContentButtonConstant.OPERATE_COPY,"Copy")
            .put(ContentButtonConstant.OPERATE_SORT,"Sort")
            .put(ContentButtonConstant.OPERATE_RELATED_CONTENT,"Relevant")
            .build();

}
