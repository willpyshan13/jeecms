package com.jeecms.interact.domain;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.jeecms.common.base.domain.AbstractDomain;
import com.jeecms.common.util.StrUtils;
import com.jeecms.constants.CmsFormConstant;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.*;

/**
 *
 * @author: tom
 * @date: 2020/1/3 18:36   
 */
@Entity
@Table(name = "jc_ex_form_item")
public class CmsFormItemEntity extends AbstractDomain<Integer> {
    private Integer id;
    private Integer formId;
    private String field;
    private String itemLabel;
    private Integer sortNum;
    private String defValue;
    private Boolean isRequired;
    private String placeholder;
    private String tipText;
    private String dataType;
    private String groupType;
    private Boolean isCustom;
    private String content;

    private CmsFormEntity form;

    /**
     * 获取限制文件上传大小
     *
     * @Title: getLimitFileSize
     * @return: Integer
     */
    @Transient
    @JSONField(serialize = false)
    public Integer getLimitFileSize() {
        JSONObject itemAttr = getItemValueJson();
        if (itemAttr != null) {
            Object fileSizeObj = itemAttr.get(CmsFormConstant.FIELD_ATTR_FILE_LIMIT_SIZE);
            if (StrUtils.isNumeric(fileSizeObj)) {
                return Integer.parseInt((String) fileSizeObj);
            }
        }
        return 0;
    }

    /**
     * 获取限制文件上传大小单元
     *
     * @Title: getLimitFileSize
     * @return: Integer
     */
    @Transient
    @JSONField(serialize = false)
    public String getLimitFileSizeUnit() {
        JSONObject itemAttr = getItemValueJson();
        if (itemAttr != null) {
            return itemAttr.getString(CmsFormConstant.FIELD_ATTR_FILE_LIMIT_SIZE_UNIT);
        }
        return CmsFormConstant.FIELD_ATTR_FILE_LIMIT_SIZE_UNIT_MB;
    }

    /**
     * 获取限制文件上传数量
     *
     * @Title: getLimitFileNumber
     * @return: Integer 0不限制
     */
    @Transient
    @JSONField(serialize = false)
    public Integer getLimitFileNumber() {
        JSONObject itemAttr = getItemValueJson();
        if (itemAttr != null) {
            String fileNumber = itemAttr.getString(CmsFormConstant.FIELD_ATTR_FILE_LIMIT_NUM);
            if(StringUtils.isNotBlank(fileNumber)&&StrUtils.isNumeric(fileNumber)){
                return Integer.parseInt(fileNumber);
            }
            return 0;
        }
        return 0;
    }

    /**
     * 是否是饼图显示字段
     * 当组件为单选、多选、图片单选、图片多选、下拉、级联、城市、性别时，使用饼图展示
     * @return
     */
    @Transient
    @JSONField(serialize = false)
    public boolean isPipField(){
        if(CmsFormConstant.FIELD_TYPE_IMG_CHECKBOX.equals(getDataType())){
            return true;
        }
        if(CmsFormConstant.FIELD_TYPE_IMG_RADIO.equals(getDataType())){
            return true;
        }
        if(CmsFormConstant.SINGLE_CHOOSE.equals(getDataType())){
            return true;
        }
        if(CmsFormConstant.MANY_CHOOSE.equals(getDataType())){
            return true;
        }
        if(CmsFormConstant.DROP_DOWN.equals(getDataType())){
            return true;
        }
        if(CmsFormConstant.CASCADE.equals(getDataType())){
            return true;
        }
        if(CmsFormConstant.CITY.equals(getDataType())){
            return true;
        }
        if(CmsFormConstant.SEX.equals(getDataType())){
            return true;
        }
        return false;
    }

    /**
     * 获取限制文件上传类型
     * @Title: getLimitFileType
     * @return: String
     */
    @Transient
    @JSONField(serialize = false)
    public List<String>  getLimitFileType() {
        JSONObject itemAttr = getItemValueJson();
        if (itemAttr != null) {
            if (CmsFormConstant.SINGLE_CHART_UPLOAD.equals(getDataType())
                    ||CmsFormConstant.MANY_CHART_UPLOAD.equals(getDataType())
                    ||CmsFormConstant.AUDIO_UPLOAD.equals(getDataType())
                    ||CmsFormConstant.VIDEO_UPLOAD.equals(getDataType())) {
                /**图片、视频、音频*/
                JSONArray fileTypeObj = itemAttr.getJSONArray(CmsFormConstant.FIELD_ATTR_FILE_LIMIT_TYPE);
                List<String> list = JSONObject.parseArray(fileTypeObj.toJSONString(),String.class);
                return list;
            }
            /**附件*/
            if(CmsFormConstant.ANNEX_UPLOAD.equals(getDataType())){
                String fileTypeObj = itemAttr.getString(CmsFormConstant.FIELD_ATTR_FILE_LIMIT_ENABLE);
                if(StringUtils.isNotBlank(fileTypeObj)){
                    List<String> list = Arrays.asList(fileTypeObj.split(","));
                    return list;
                }
            }
        }
        return Arrays.asList();
    }

    /***
     * 获取附件不允许上传类型
     * @return
     */
    @Transient
    @JSONField(serialize = false)
    public List<String> getNotLimitFileType() {
        JSONObject itemAttr = getItemValueJson();
        if (itemAttr != null&&CmsFormConstant.ANNEX_UPLOAD.equals(getDataType())) {
            /**附件*/
            String fileTypeObj = itemAttr.getString(CmsFormConstant.FIELD_ATTR_FILE_LIMIT_DISABLE_TYPE);
            if(StringUtils.isNotBlank(fileTypeObj)){
                List<String> list = Arrays.asList(fileTypeObj.split(","));
                return list;
            }
        }
        return Arrays.asList();
    }

    /**
     * 是否允许上传后缀的文件
     */
    @Transient
    @JSONField(serialize = false)
    public boolean isAllowSuffix(String ext) {
        List<String> allowSuffixs = getLimitFileType();
        /**图片、视频、音频*/
        if (CmsFormConstant.SINGLE_CHART_UPLOAD.equals(getDataType())
                ||CmsFormConstant.MANY_CHART_UPLOAD.equals(getDataType())
                ||CmsFormConstant.AUDIO_UPLOAD.equals(getDataType())
                ||CmsFormConstant.VIDEO_UPLOAD.equals(getDataType())){
            if (allowSuffixs.size()<=0) {
                return false;
            }
            for (int i = 0, len = allowSuffixs.size(); i < len; i++) {
                if (allowSuffixs.get(i).equals(ext)) {
                    return true;
                }
            }
        }
        if(CmsFormConstant.ANNEX_UPLOAD.equals(getDataType())){
                Integer allowType = getItemValueJson().getInteger(CmsFormConstant.FIELD_ATTR_FILE_LIMIT_TYPE_UNIT);
                /**不限制*/
                if(CmsFormConstant.FIELD_ATTR_FILE_LIMIT_TYPE_UNIT_NO.equals(allowType)){
                    return true;
                }else  if(CmsFormConstant.FIELD_ATTR_FILE_LIMIT_TYPE_UNIT_ALLOW.equals(allowType)){
                    if(allowSuffixs.size()<=0){
                        return false;
                    }
                    /**允许的类型*/
                    for (int i = 0, len = allowSuffixs.size(); i < len; i++) {
                        if (allowSuffixs.get(i).equals(ext)) {
                            return true;
                        }
                    }
                }else  if(CmsFormConstant.FIELD_ATTR_FILE_LIMIT_TYPE_UNIT_FOBBIDEN.equals(allowType)){
                    /**不允许的类型*/
                    List<String> notAllowSuffixs = getNotLimitFileType();
                    for (int i = 0, len = notAllowSuffixs.size(); i < len; i++) {
                        if (notAllowSuffixs.get(i).equals(ext)) {
                            return false;
                        }
                    }
                    return true;
                }
        }
        return false;
    }

    /**
     * 是否允许上传的文件，根据文件大小
     * @param size KB单位
     * @return
     */
    @Transient
    @JSONField(serialize = false)
    public boolean isAllowMaxFile(Long size) {
        Integer allowPerFile = getLimitFileSize();
        if(CmsFormConstant.FIELD_ATTR_FILE_LIMIT_SIZE_UNIT_MB.equals(getLimitFileSizeUnit())){
            allowPerFile = 1024*allowPerFile;
        }
        if (null == allowPerFile || 0 == allowPerFile) {
            return true;
        } else {
            return allowPerFile >= size;
        }
    }


    /**
     * 获取单选多选下拉选择的options JSON数组
     *
     * @Title: getLimitFileSize
     * @return: Integer
     */
    @Transient
    public JSONArray getOptions() {
        JSONObject itemAttr = getItemValueJson();
        JSONArray optionArray = new JSONArray();
        if (itemAttr != null) {
            return itemAttr.getJSONArray(CmsFormConstant.FIELD_ATTR_OPTIONS);
        }
        return optionArray;
    }

    /***
     * 获取其他选项label
     * @return
     */
    @Transient
    public String getOtherOptionLabel() {
        JSONObject itemAttr = getItemValueJson();
        if (itemAttr != null) {
            /**下拉的结构不同*/
            if(CmsFormConstant.DROP_DOWN.equals(getDataType())){
                return itemAttr.getJSONObject(CmsFormConstant.FIELD_ATTR_SELECT_OTHER_OPTIONS).getString(CmsFormConstant.ITEM_LABEL);
            }
            return itemAttr.getString(CmsFormConstant.FIELD_ATTR_OTHER_OPTIONS);
        }
        return "其他";
    }

    /**
     * 获取单选多选下拉选择的options map key为选项值 value为label
     *
     * @Title: getOptionMap
     * @return: Integer
     */
    @Transient
    @JSONField(serialize = false)
    public Map<String,String> getOptionMap() {
        Map<String,String>opMap = new HashMap<>();
        JSONArray optionArray = getOptions();
        if(optionArray!=null){
            for(int i=0;i<optionArray.size();i++){
                JSONObject op = optionArray.getJSONObject(i);
                String label = op.getString(CmsFormConstant.ITEM_LABEL);
                String value = op.getString(CmsFormConstant.ITEM_VALUE);
                opMap.put(value,label);
            }
        }
        return opMap;
    }

    /**
     * 获取选项lable(适用于单选 多选、下拉)
     * @param val
     * @return
     */
    @Transient
    @JSONField(serialize = false)
    public String getOptionLabel(String val){
        if(StringUtils.isNotBlank(val)){
            if(CmsFormConstant.OTHER_OPTION.equals(val)){
                return getOtherOptionLabel();
            }else{
                return getOptionMap().get(val);
            }
        }
        return "";
    }

    /**
     * 获取图片单选多选下拉选择的options map key为选项值 value为pic id
     *
     * @Title: getOptionPicMap
     * @return: Integer
     */
    @Transient
    @JSONField(serialize = false)
    public Map<String,Integer> getOptionPicMap() {
        Map<String,Integer>opMap = new HashMap<>();
        JSONArray optionArray = getOptions();
        if(optionArray!=null){
            for(int i=0;i<optionArray.size();i++){
                JSONObject op = optionArray.getJSONObject(i);
                Integer pic = op.getInteger(CmsFormConstant.OPTION_PIC);
                String value = op.getString(CmsFormConstant.ITEM_VALUE);
                opMap.put(value,pic);
            }
        }
        return opMap;
    }



    @Transient
    @JSONField(serialize = false)
    public JSONObject getItemValueJson() {
        return JSONObject.parseObject(content).getJSONObject("value");
    }

    @Transient
    @JSONField(serialize = false)
    public JSONObject getItemJson() {
        return JSONObject.parseObject(content);
    }

    @Id
    @Column(name = "form_item_id")
    @TableGenerator(name = "jc_ex_form_item", pkColumnValue = "jc_ex_form_item", initialValue = 0, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_ex_form_item")
    public Integer getId() {
        return id;
    }

    public void setId(Integer formItemId) {
        this.id = formItemId;
    }

    @Basic
    @Column(name = "form_id")
    public Integer getFormId() {
        return formId;
    }

    public void setFormId(Integer formId) {
        this.formId = formId;
    }

    @Basic
    @Column(name = "field")
    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    @Basic
    @Column(name = "item_label")
    public String getItemLabel() {
        return itemLabel;
    }

    public void setItemLabel(String itemLabel) {
        this.itemLabel = itemLabel;
    }

    @Basic
    @Column(name = "sort_num")
    public Integer getSortNum() {
        return sortNum;
    }

    public void setSortNum(Integer sortNum) {
        this.sortNum = sortNum;
    }

    @Basic
    @Column(name = "def_value")
    public String getDefValue() {
        /**PC前端有个解析这里有空格导致异常，原因未知，在此处去除空格解决问题*/
        if (StringUtils.isBlank(defValue)) {
            return "";
        }
        return defValue.trim();
    }

    public void setDefValue(String defValue) {
        this.defValue = defValue;
    }

    @Basic
    @Column(name = "is_required")
    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    @Basic
    @Column(name = "placeholder")
    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @Basic
    @Column(name = "tip_text")
    public String getTipText() {
        return tipText;
    }

    public void setTipText(String tipText) {
        this.tipText = tipText;
    }

    @Basic
    @Column(name = "data_type")
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @Basic
    @Column(name = "group_type")
    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    @Basic
    @Column(name = "is_custom")
    public Boolean getIsCustom() {
        return isCustom;
    }

    public void setIsCustom(Boolean isCustom) {
        this.isCustom = isCustom;
    }

    @Basic
    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", insertable = false, updatable = false)
    public CmsFormEntity getForm() {
        return form;
    }

    public void setForm(CmsFormEntity form) {
        this.form = form;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CmsFormItemEntity that = (CmsFormItemEntity) o;
        return id.equals(that.id) &&
                formId.equals(that.formId) &&
                sortNum.equals(that.sortNum) &&
                isRequired.equals(that.isRequired) &&
                groupType.equals(that.groupType) &&
                isCustom.equals(that.isCustom) &&
                Objects.equals(field, that.field) &&
                Objects.equals(itemLabel, that.itemLabel) &&
                Objects.equals(defValue, that.defValue) &&
                Objects.equals(placeholder, that.placeholder) &&
                Objects.equals(tipText, that.tipText) &&
                Objects.equals(dataType, that.dataType) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, formId, field, itemLabel, sortNum, defValue, isRequired, placeholder, tipText, dataType, groupType, isCustom, content);
    }
}
