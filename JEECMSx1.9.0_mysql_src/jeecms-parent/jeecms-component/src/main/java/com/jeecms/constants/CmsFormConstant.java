package com.jeecms.constants;
/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * 表单字段常量
 * @author: tom
 * @date: 2020/1/6 15:39   
 */
public class CmsFormConstant {
    /**提交成功后处理方式（1显示文字信息2显示信件编号及查询码 ）*/
    public static final Short PROCESS_TYPE_TIP=1;
    /**提交成功后处理方式（1显示文字信息 2显示信件编号及查询码 2跳转到指定页面）*/
    public static final Short PROCESS_TYPE_DISPLAY_NUMBER =2;
    public static final Short PROCESS_TYPE_RELOCATION =2;

    public static final String GROUP_INPUT="input";
    public static final String GROUP_LAYOUT="layout";
    /**应用场景-1领导信箱*/
    public static final Short FORM_SCENE_LETTER=1;
    /**应用场景-2智能表单*/
    public static final Short FORM_SCENE_FORM =2;
    /***信件状态（1待办理 2已回复 3流转中 ）**/
    public static final Integer LETTER_STATU_UNPROCESS=1;
    public static final Integer LETTER_STATU_REPLY=2;
    public static final Integer LETTER_STATU_IN_PROCESS=3;


    /***表单状态 （发布状态 0未发布 1已发布 2暂停 )**/
    public static final Short FORM_STATU_NO_PUBLISH=0;
    public static final Short FORM_STATU_PUBLISH=1;
    public static final Short FORM_STATU_STOP =2;

    /***表单展示状态 （表单状态 0未发布 1进行中 2已结束 )**/
    public static final Short FORM_VIEW_STATU_NO_PUBLISH=0;
    public static final Short FORM_VIEW_STATU_PUBLISH=1;
    public static final Short FORM_VIEW_STATU_STOP =2;

    public static final Short LETTER_INI_SCORE=0;
    /**默认评分*/
    public static final Short LETTER_DEF_SCORE=10;
    /**提交次数限制单位（1只能2每小时3每天 0不限制）*/
    public static final Short SUBMIT_LIMT_UNIT_ONE=1;
    public static final Short SUBMIT_LIMT_UNIT_HOUR=2;
    public static final Short SUBMIT_LIMT_UNIT_DAY=3;
    public static final Short SUBMIT_LIMT_UNIT_NO=0;

    /***信件排序（1创建时间倒序 2 回复时间倒序 3选登时间倒序 4评价时间倒序 5删除时间倒序 6ID升序）**/
    public static final Integer ORDER_BY_CREATE_TIME_DESC=1;
    public static final Integer ORDER_BY_REPLY_TIME_DESC=2;
    public static final Integer ORDER_BY_OPEN_TIME_DESC=3;
    public static final Integer ORDER_BY_SCORE_TIME_DESC=4;
    public static final Integer ORDER_BY_RECYCLE_TIME_DESC=5;
    public static final Integer ORDER_BY_ID_ASC=6;
    /***满意度评价设置（1开启且公开评价结果 2开启但不公开评价结果 3不开启）**/
    public static final Short SCORE_SET_OPEN_SCORE=1;
    public static final Short SCORE_SET_OPEN=2;
    public static final Short SCORE_SET_CLOSE=3;

    /**
     * 信件自动评分 3天
     */
    public static final Short SCORE_AUTO_DAY = 3;

    /***统计-查询周期（0 全年 1第一季度 2 第2季度  3第3季度  4 第4季度  5上半年 6下半年 7 -18分别代表 1-12月）**/
    public static final Integer STATIC_TIME_UNIT_QUART_0 = 0;
    public static final Integer STATIC_TIME_UNIT_QUART_1=1;
    public static final Integer STATIC_TIME_UNIT_QUART_2=2;
    public static final Integer STATIC_TIME_UNIT_QUART_3=3;
    public static final Integer STATIC_TIME_UNIT_QUART_4=4;
    public static final Integer STATIC_TIME_UNIT_YEAR_BEGIN=5;
    public static final Integer STATIC_TIME_UNIT_YEAR_END=6;

    public static final Integer STATIC_TIME_UNIT_MONTH_1=7;
    public static final Integer STATIC_TIME_UNIT_MONTH_2=8;
    public static final Integer STATIC_TIME_UNIT_MONTH_3=9;
    public static final Integer STATIC_TIME_UNIT_MONTH_4=10;

    public static final Integer STATIC_TIME_UNIT_MONTH_5=11;
    public static final Integer STATIC_TIME_UNIT_MONTH_6=12;
    public static final Integer STATIC_TIME_UNIT_MONTH_7=13;
    public static final Integer STATIC_TIME_UNIT_MONTH_8=14;

    public static final Integer STATIC_TIME_UNIT_MONTH_9=15;
    public static final Integer STATIC_TIME_UNIT_MONTH_10=16;
    public static final Integer STATIC_TIME_UNIT_MONTH_11=17;
    public static final Integer STATIC_TIME_UNIT_MONTH_12=18;

    /**回复效率统计分组 1 人 2信箱*/
    public static final Integer STATIC_REPLY_GROUP_BY_USER=1;
    public static final Integer STATIC_REPLY_GROUP_BY_BOX=2;
    /**工作量统计排序 1回复数量降序 2回复效率降序 3满意度降序*/
    public static final Integer STATIC_REPLY_ORDER_BY_COUNT_DESC=1;
    public static final Integer STATIC_REPLY_ORDER_BY_TIME_DESC=2;
    public static final Integer STATIC_REPLY_ORDER_BY_SCORE_DESC=3;

    public static final Map<Short,String> LOG_INFO_SCORESET_MAP = ImmutableMap.<Short, String>builder()
            .put(SCORE_SET_OPEN_SCORE, "开启且公开评价结果")
            .put(SCORE_SET_OPEN, "开启但不公开评价结果")
            .put(SCORE_SET_CLOSE, "不开启")
            .build();

    public static final Map<Boolean,String> LOG_INFO_BOOLEAN_MAP = ImmutableMap.<Boolean, String>builder()
            .put(Boolean.TRUE, "（是）")
            .put(Boolean.FALSE, "（否）")
            .build();

    /***信件办理 （1通过审核 2 审核驳回 3删除 4还原 5撤销办理 ）**/
    public static final Integer LETTER_LOG_TYPE_PASS=1;
    public static final Integer LETTER_LOG_TYPE_REJECT=2;
    public static final Integer LETTER_LOG_TYPE_RECYCLE=3;
    public static final Integer LETTER_LOG_TYPE_REDUCE=4;
    public static final Integer LETTER_LOG_TYPE_REVOKEREPLY=5;


    /**组件名（唯一）*/
    public static final String FIELD = "name";
    /**默认值*/
    public static final String ITEM_LABEL = "label";
    /**选项值*/
    public static final String ITEM_VALUE = "value";
    /**默认值*/
    public static final String DEF_VALUE = "defaultValue";
    /**是否必填*/
    public static final String IS_REQUIRED = "isRequired";
    /**默认提示文字*/
    public static final String PLACEHOLDER = "placeholder";
    /**辅助提示文字*/
    public static final String TIP_TEXT = "tip";
    /**组件类型*/
    public static final String INPUT_TYPE = "type";
    /**组件是否自定义*/
    public static final String IS_CUSTOM = "isCustom";
    /**当前组件的显示排列顺序*/
    public static final String INDEX = "index";
    /**组件值对象key*/
    public static final String VALUE = "value";
    /**组件分组（layout布局、input元素）*/
    public static final String ELE_GROUP = "groupType";
    /**option下pic属性*/
    public static final String OPTION_PIC = "pic";


    /**是否开启字符长度限制*/
    public static final String FIELD_ATTR_IS_LENGTH_LIMIT = "isLengthLimit";
    /**字符长度最大限制*/
    public static final String FIELD_ATTR_LENGTH_MAX = "max";
    /**字符长度最小限制*/
    public static final String FIELD_ATTR_LENGTH_MIN = "min";
    /**是否开启表单正则限制*/
    public static final String FIELD_ATTR_INPUT_LIMIT = "isInputLimit";
    /**表单正则限制名称*/
    public static final String FIELD_ATTR_INPUT_LIMIT_RULE = "inputLimit";
    /**限制文件类型*/
    public static final String FIELD_ATTR_FILE_LIMIT_TYPE = "type";
    /**限制文件大小*/
    public static final String FIELD_ATTR_FILE_LIMIT_SIZE= "size";
    /**单选 多选，下拉 属性值*/
    public static final String FIELD_ATTR_OPTIONS= "options";
    /**单选 多选，其他属性值*/
    public static final String FIELD_ATTR_OTHER_OPTIONS= "otherOptionLabel";
    /**下拉 其他属性值*/
    public static final String FIELD_ATTR_SELECT_OTHER_OPTIONS= "otherOption";
    /**限制文件大小单位*/
    public static final String FIELD_ATTR_FILE_LIMIT_SIZE_UNIT= "unit";
    public static final String FIELD_ATTR_FILE_LIMIT_SIZE_UNIT_KB= "KB";
    public static final String FIELD_ATTR_FILE_LIMIT_SIZE_UNIT_MB= "MB";
    /**限制文件数量*/
    public static final String FIELD_ATTR_FILE_LIMIT_NUM = "limit";
    /**限制文件设置规则种类1 不限制 2设置允许的类型 3设置不允许的类型 */
    public static final String FIELD_ATTR_FILE_LIMIT_TYPE_UNIT = "typeLimit";
    public static final Integer FIELD_ATTR_FILE_LIMIT_TYPE_UNIT_NO = 1;
    public static final Integer FIELD_ATTR_FILE_LIMIT_TYPE_UNIT_ALLOW = 2;
    public static final Integer FIELD_ATTR_FILE_LIMIT_TYPE_UNIT_FOBBIDEN = 3;

    /**限制文件设 设置允许的类型  */
    public static final String FIELD_ATTR_FILE_LIMIT_ENABLE= "enableType";
    /**限制文件设 设置不允许的类型  */
    public static final String FIELD_ATTR_FILE_LIMIT_DISABLE_TYPE= "disableType";


    /***类型（1-单资源 2-多资源 3-普通字符  4-所在地   5-城市 6单选 下拉选择类型 7图片单选 8图片多选类型 9多选）*/
    public static final Short FIELD_ATTR_FILE_PROP_RES = 1;
    public static final Short FIELD_ATTR_FILE_PROP_RESES = 2;
    public static final Short FIELD_ATTR_FILE_PROP_INPUT = 3;
    public static final Short FIELD_ATTR_FILE_PROP_ADDRESS= 4;
    public static final Short FIELD_ATTR_FILE_PROP_CITY = 5;
    public static final Short FIELD_ATTR_FILE_PROP_ITEM= 6;
    public static final Short FIELD_ATTR_FILE_PROP_IMG_RADIO= 7;
    public static final Short FIELD_ATTR_FILE_PROP_IMG_CHECKBOX= 8;
    public static final Short FIELD_ATTR_FILE_PROP_ITEM_MULTI= 9;
    /**默认字段名-验证码*/
    public static final String FIELD_DEF_CAPTCHA = "code";
    /**背景图片url*/
    public static final String FIELD_DEF_BG_IMG_URL = "bgImageUrl";
    /**背景图片ID*/
    public static final String FIELD_DEF_BG_IMG = "bgImage";

    /**
     * 领导信箱表单默认JSON字段
     */
    public static final String LETTER_FORM_INIT_FIELD_JSON = "[{\"dragable\":true,\"type\":\"formTitle\",\"name\":\"留言标题\",\"groupType\":\"input\",\"groupIndex\":99,\"isCustom\":false,\"canDelete\":false,\"index\":99,\"preview\":\"FormTitlePreview\",\"editor\":\"FormTitleEditor\",\"disableFields\":[\"isRequired\"],\"value\":{\"defaultValue\":\"\",\"label\":\"留言标题\",\"name\":\"title\",\"placeholder\":\"请输入内容\",\"tip\":\"\",\"isLengthLimit\":false,\"min\":\"\",\"max\":\"\",\"isInputLimit\":false,\"inputLimit\":\"\",\"width\":80,\"isRegister\":false,\"isRequired\":true}},{\"dragable\":true,\"type\":\"formDesc\",\"name\":\"留言内容\",\"groupType\":\"input\",\"groupIndex\":98,\"isCustom\":true,\"canDelete\":false,\"index\":98,\"preview\":\"FormDescPreview\",\"editor\":\"FormDescEditor\",\"disableFields\":[\"isRequired\"],\"value\":{\"defaultValue\":\"\",\"label\":\"留言内容\",\"name\":\"content\",\"placeholder\":\"请输入内容\",\"tip\":\"\",\"isLengthLimit\":false,\"max\":255,\"width\":80,\"isInputLimit\":false,\"inputLimit\":\"\",\"isRegister\":false,\"isRequired\":true}}]";



    /**题目类型-图片多选*/
    public static final String FIELD_TYPE_IMG_CHECKBOX = "imageCheckbox";
    /**题目类型-图片单选*/
    public static final String FIELD_TYPE_IMG_RADIO = "imageRadio";
    /**题目类型-图片选项类型追加的json key*/
    public static final String FIELD_OPTION_IMG_URL = "imageUrl";
    /** 单行文本 */
    public static final String TEXT = "input";
    /** 多行文本 */
    public static final String TEXTS = "textarea";
    /** 单选 */
    public static final String SINGLE_CHOOSE = "radio";
    /**
     * 公开意愿
     */
    public static final String PUBLIC_WILL = "publicWill";
    /** 多选 */
    public static final String MANY_CHOOSE = "checkbox";
    /** 选择框 */
    public static final String DROP_DOWN = "select";
    /** 级联选择 */
    public static final String CASCADE = "cascade";
    /** 日期 */
    public static final String DATE = "dateTime";
    /** 日期 区间*/
    public static final String DATE_SECTION = "dateTimeSection";
    /** 单图上传 */
    public static final String SINGLE_CHART_UPLOAD = "imageUpload";
    /** 多图上传 */
    public static final String MANY_CHART_UPLOAD = "multiImageUpload";
    /** 视频上传 */
    public static final String VIDEO_UPLOAD = "videoUpload";
    /** 音频上传 */
    public static final String AUDIO_UPLOAD = "audioUpload";
    /** 附件上传 */
    public static final String ANNEX_UPLOAD = "fileUpload";
    /** 富文本 */
    public static final String RICH_TEXT = "ueditor";
    /** 组织 */
    public static final String TISSUE = "organize";
    /** 所在地 */
    public static final String ADDRESS = "address";
    /** 城市 */
    public static final String CITY = "city";
    /** 内容：正文*/
    public static final String CONTENT_TXT = "content";
    /** 性别*/
    public static final String SEX = "sex";
    /**其他选项值*/
    public static final String OTHER_OPTION = "999";


    /**
     * 字段名称标题
     */
    public static final String FIELD_DEF_NAME_TITLE = "title";
    public static final String FIELD_DEF_NAME_CONTENT = "content";

    /**
     * 按时显示
     */
    public static final int GROUP_HOUR = 1;
    /**
     * 按天显示
     */
    public static final int GROUP_DAY = 2;
    /**
     * 按周显示
     */
    public static final int GROUP_WEEK = 3;
    /**
     * 按月统计
     */
    public static final int GROUP_MOUTH = 4;


}
