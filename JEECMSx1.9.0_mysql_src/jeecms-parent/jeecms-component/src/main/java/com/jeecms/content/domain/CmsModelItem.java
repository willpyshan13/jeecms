/*
 * * @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
 * Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.content.domain;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.jeecms.common.base.domain.AbstractDomain;
import com.jeecms.common.util.StrUtils;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.content.constants.CmsModelConstant;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/***
 * 
 * @Description:模型字段表 domain The persistent class for the jc_model_item database table.
 * @author: tom
 * @date: 2018年11月10日 上午9:35:58
 */
@Entity
@Table(name = "jc_model_item")
@NamedQuery(name = "CmsModelItem.findAll", query = "SELECT c FROM CmsModelItem c")
public class CmsModelItem extends AbstractDomain<Integer> implements Serializable {
        private static final long serialVersionUID = 1L;

        public static final String SORT_WEIGHT_NAME = "sortWeight";
        
        private Integer id;
        /** 模型ID */
        private Integer modelId;
        /** 字段 */
        private String field;
        /** 名称 */
        private String itemLabel;
        /** 排序 */
        private Integer sortNum;
        /** 默认值 */
        private String defValue;
        /** 是否必填项 */
        private Boolean isRequired;
        /** 默认提示文字 */
        private String placeholder;
        /** 帮助信息 */
        private String tipText;
        /** 数据类型 */
        private String dataType;
        /** 分组类型 */
        private String groupType;
        /** 是否自定义字段 */
        private Boolean isCustom;
        /** 字段详细参数,json格式 */
        private String content;
        
        private Integer blockName;
        
        private Integer sortWeight;
        
        @SuppressWarnings("unused")
        private JSONObject contentObj;
        
        /** 对应前台传入json的key值 */
        public static final String COMPENT_VAL_KEY = "value";
        //模型字段分组公用key
        public static final String FORM_LIST_BASE = "formListBase";
        public static final String FORM_LIST_EXTEND = "formListExtend";
        //栏目模型字段分组key
        public static final String FORM_LIST_SEO = "formListSeo";
        //内容模型分组key
        public static final String FORM_LIST_TITLE = "formListTitle";
        public static final String FORM_LIST_CONTENT = "formListContent";
        
        public static final String[] CHANNEL_GROUP_TYPE = {FORM_LIST_BASE,FORM_LIST_EXTEND,FORM_LIST_SEO};
        public static final String[] CONTENT_GROUP_TYPE = {FORM_LIST_BASE,FORM_LIST_EXTEND};
        public static final String[] MEMBER_GROUP_TYPE = {FORM_LIST_BASE};
        /**模型id*/
        public static final String MODEL_ID = "modelId";
        /**组件名（唯一）*/
        public static final String FIELD = "name";
        /**默认值*/
        public static final String ITEM_LABEL = "label";
        /**默认值*/
        public static final String DEF_VALUE = "defaultValue";
        /**是否必填*/
        public static final String IS_REQUIRED = "isRequired";
        /**默认提示文字*/
        public static final String PLACEHOLDER = "placeholder";
        /**辅助提示文字*/
        public static final String TIP_TEXT = "tip";
        /**组件类型*/
        public static final String DATE_TYPE = "type";
        /**组件是否自定义*/
        public static final String IS_CUSTOM = "isCustom";
        /**当前组件的显示排列顺序*/
        public static final String INDEX = "index";
        /**组件值对象key*/
        public static final String VALUE = "value";
        /**是否应用到注册*/
        public static final String IS_REGISTER = "isRegister";
        private CmsModel model;

        public CmsModelItem() {
        }

        /**
         * 获取限制文件上传大小
         * 
         * @Title: getLimitFileSize
         * @return: Integer
         */
        @Transient
        public Integer getLimitFileSize() {
                JSONObject itemAttr = getContentObj();
                if (itemAttr != null) {
                        Object fileSizeObj = itemAttr.get(CmsModelConstant.FIELD_ATTR_LIMIT_FILE_SIZE);
                        if (StrUtils.isNumeric(fileSizeObj)) {
                                return Integer.parseInt((String) fileSizeObj);
                        }
                }
                return 0;
        }

        /**
         * 获取限制文件上传类型
         * @Title: getLimitFileType
         * @return: String
         */
        @Transient
        public String getLimitFileType() {
                JSONObject itemAttr = getContentObj();
                if (itemAttr != null) {
                        Object fileTypeObj = itemAttr.get(CmsModelConstant.FIELD_ATTR_LIMIT_FILE_TYPE);
                        if (fileTypeObj != null) {
                                return (String) fileTypeObj;
                        }
                }
                return "";
        }
        
        /**
         * 是否允许上传后缀的文件
         */
        @Transient
        public boolean isAllowVideoSuffix(String ext) {
                String suffix = getLimitFileType();
                if (StringUtils.isBlank(suffix)) {
                        return true;
                }
                String[] attr = StringUtils.split(suffix, ",");
                for (int i = 0, len = attr.length; i < len; i++) {
                        if (attr[i].equals(ext)) {
                                return true;
                        }
                }
                return false;
        }

        /**
         * 是否允许上传的文件，根据文件大小
         */
        @Transient
        public boolean isAllowVideoMaxFile(int size) {
                Integer allowPerFile = getLimitFileSize();
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

        @Id
        @TableGenerator(name = "jc_model_item", pkColumnValue = "jc_model_item", initialValue = 0, allocationSize = 10)
        @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_model_item")
        @Override
        @Column(name = "id", unique = true, nullable = false)
        public Integer getId() {
                return this.id;
        }

        @Override
        public void setId(Integer id) {
                this.id = id;
        }

        @NotNull
        @Column(name = "data_type", nullable = false)
        public String getDataType() {
                return this.dataType;
        }

        public void setDataType(String dataType) {
                this.dataType = dataType;
        }

        @NotNull
        @Column(name = "group_type", nullable = false)
        public String getGroupType() {
                return groupType;
        }

        public void setGroupType(String groupType) {
                this.groupType = groupType;
        }

        @Column(name = "def_value", length = 150)
        public String getDefValue() {
                return this.defValue;
        }

        public void setDefValue(String defValue) {
                this.defValue = defValue;
        }

        @NotBlank
        @Column(nullable = false, length = 50)
        public String getField() {
                return this.field;
        }

        public void setField(String field) {
                this.field = field;
        }

        @NotNull
        @Column(name = "is_custom", nullable = false)
        public Boolean getIsCustom() {
                return this.isCustom;
        }

        public void setIsCustom(Boolean isCustom) {
                this.isCustom = isCustom;
        }

        @NotNull
        @Column(name = "is_required", nullable = false)
        public Boolean getIsRequired() {
                return this.isRequired;
        }

        public void setIsRequired(Boolean isRequired) {
                this.isRequired = isRequired;
        }

        @NotBlank
        @Column(name = "item_label", nullable = false, length = 150)
        public String getItemLabel() {
                return this.itemLabel;
        }

        public void setItemLabel(String itemLabel) {
                this.itemLabel = itemLabel;
        }

        @Column(name = "model_id", insertable = false, updatable = false)
        public Integer getModelId() {
                return this.modelId;
        }

        public void setModelId(Integer modelId) {
                this.modelId = modelId;
        }

        @NotNull
        @Column(name = "sort_num", nullable = false)
        public Integer getSortNum() {
                return this.sortNum;
        }

        public void setSortNum(Integer sortNum) {
                this.sortNum = sortNum;
        }

        @Column(name = "placeholder", length = 50)
        public String getPlaceholder() {
                return placeholder;
        }

        public void setPlaceholder(String placeholder) {
                this.placeholder = placeholder;
        }

        @Column(name = "tip_text", length = 50)
        public String getTipText() {
                return tipText;
        }

        public void setTipText(String tipText) {
                this.tipText = tipText;
        }

        @NotNull
        @Column(name = "content")
        public String getContent() {
                return content;
        }

        public void setContent(String content) {
                this.content = content;
        }

        @Transient
        public JSONObject getContentObj() {
                return JSONObject.parseObject(content);
        }

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "model_id")
        @NotFound(action= NotFoundAction.IGNORE)
        public CmsModel getModel() {
                return model;
        }

        public void setModel(CmsModel model) {
                this.model = model;
        }

        @Column(name = "block_name", length = 255)
		public Integer getBlockName() {
			return blockName;
		}

		public void setBlockName(Integer blockName) {
			this.blockName = blockName;
		}

		@Column(name = "sort_weight", length = 11)
		public Integer getSortWeight() {
			return sortWeight;
		}

		public void setSortWeight(Integer sortWeight) {
			this.sortWeight = sortWeight;
		}

        @Override
        public boolean equals(Object o) {
                if (this == o) {
                        return true;
                }
                if (o == null || getClass() != o.getClass()) {
                        return false;
                }

                CmsModelItem that = (CmsModelItem) o;

                if (!Objects.equals(id, that.id)) {
                        return false;
                }
                if (!Objects.equals(modelId, that.modelId)) {
                        return false;
                }
                if (!Objects.equals(field, that.field)) {
                        return false;
                }
                if (!Objects.equals(itemLabel, that.itemLabel)) {
                        return false;
                }
                if (!Objects.equals(sortNum, that.sortNum)) {
                        return false;
                }
                if (!Objects.equals(defValue, that.defValue)) {
                        return false;
                }
                if (!Objects.equals(isRequired, that.isRequired)) {
                        return false;
                }
                if (!Objects.equals(placeholder, that.placeholder)) {
                        return false;
                }
                if (!Objects.equals(tipText, that.tipText)) {
                        return false;
                }
                if (!Objects.equals(dataType, that.dataType)) {
                        return false;
                }
                if (!Objects.equals(groupType, that.groupType)) {
                        return false;
                }
                if (!Objects.equals(isCustom, that.isCustom)) {
                        return false;
                }
                if (!Objects.equals(content, that.content)) {
                        return false;
                }
                if (!Objects.equals(blockName, that.blockName)) {
                        return false;
                }
                if (!Objects.equals(sortWeight, that.sortWeight)) {
                        return false;
                }
                if (!Objects.equals(contentObj, that.contentObj)) {
                        return false;
                }
                return Objects.equals(model, that.model);
        }

        @Override
        public int hashCode() {
                int result = id != null ? id.hashCode() : 0;
                result = 31 * result + (modelId != null ? modelId.hashCode() : 0);
                result = 31 * result + (field != null ? field.hashCode() : 0);
                result = 31 * result + (itemLabel != null ? itemLabel.hashCode() : 0);
                result = 31 * result + (sortNum != null ? sortNum.hashCode() : 0);
                result = 31 * result + (defValue != null ? defValue.hashCode() : 0);
                result = 31 * result + (isRequired != null ? isRequired.hashCode() : 0);
                result = 31 * result + (placeholder != null ? placeholder.hashCode() : 0);
                result = 31 * result + (tipText != null ? tipText.hashCode() : 0);
                result = 31 * result + (dataType != null ? dataType.hashCode() : 0);
                result = 31 * result + (groupType != null ? groupType.hashCode() : 0);
                result = 31 * result + (isCustom != null ? isCustom.hashCode() : 0);
                result = 31 * result + (content != null ? content.hashCode() : 0);
                result = 31 * result + (blockName != null ? blockName.hashCode() : 0);
                result = 31 * result + (sortWeight != null ? sortWeight.hashCode() : 0);
                result = 31 * result + (contentObj != null ? contentObj.hashCode() : 0);
                result = 31 * result + (model != null ? model.hashCode() : 0);
                return result;
        }
}
