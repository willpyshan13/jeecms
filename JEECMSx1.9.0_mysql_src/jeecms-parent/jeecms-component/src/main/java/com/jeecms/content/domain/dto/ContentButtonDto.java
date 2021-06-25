/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.domain.dto;

import com.jeecms.resource.domain.ResourcesSpaceData;

import java.util.List;

/**
 * @author: chenming
 * @date: 2020/2/26 21:29
 */
public class ContentButtonDto {
    /**
     * 按钮txt名称
     */
    private String text;
    /**
     * 按钮显示的key(前端需要：dropdown-下拉框样式、link-普通点击样式)
     */
    private String key;
    /**
     * 前端对应按钮图标标识
     */
    private String icon;


    /**
     * 内容按钮子集
     */
    List<ContentButtionSubsetDto> subset;

    public List<ContentButtionSubsetDto> getSubset() {
        return subset;
    }

    public void setSubset(List<ContentButtionSubsetDto> subset) {
        this.subset = subset;
    }

    /**
     * 内容按钮子集(内容按钮存在两层的层级结构，此为子集的层级结构)
     */
    public class ContentButtionSubsetDto extends ContentButtonDto {
        /**
         * api请求对象
         */
        private ApiPort port;
        /**
         * 内容类型管理
         */
        private ResourcesSpaceData contentTypeIcon;
        /**
         * 是否可以点击
         */
        private Boolean click;

        private Boolean contentTypeStatus;

        private String buttonPopUposType;


        private Integer contentTypeId;

        public Integer getContentTypeId() {
            return contentTypeId;
        }

        public void setContentTypeId(Integer contentTypeId) {
            this.contentTypeId = contentTypeId;
        }

        public String getButtonPopUposType() {
            return buttonPopUposType;
        }

        public void setButtonPopUposType(String buttonPopUposType) {
            this.buttonPopUposType = buttonPopUposType;
        }

        public Boolean getContentTypeStatus() {
            return contentTypeStatus;
        }

        public void setContentTypeStatus(Boolean contentTypeStatus) {
            this.contentTypeStatus = contentTypeStatus;
        }

        public ApiPort getPort() {
            return port;
        }

        public void setPort(ApiPort port) {
            this.port = port;
        }

        public ResourcesSpaceData getContentTypeIcon() {
            return contentTypeIcon;
        }

        public void setContentTypeIcon(ResourcesSpaceData contentTypeIcon) {
            this.contentTypeIcon = contentTypeIcon;
        }

        public Boolean getClick() {
            return click;
        }

        public void setClick(Boolean click) {
            this.click = click;
        }
        /**
         * api请求对象
         */
        public class ApiPort {
            /**
             * api请求url
             */
            private String apiUrl;
            /**
             * 请求方式：GET、POST、PUT、DELETED
             */
            private String requestMethod;

            public ApiPort() {
            }

            public ApiPort(String apiUrl, String requestMethod) {
                this.apiUrl = apiUrl;
                this.requestMethod = requestMethod;
            }

            public String getApiUrl() {
                return apiUrl;
            }

            public void setApiUrl(String apiUrl) {
                this.apiUrl = apiUrl;
            }

            public String getRequestMethod() {
                return requestMethod;
            }

            public void setRequestMethod(String requestMethod) {
                this.requestMethod = requestMethod;
            }
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }


}
