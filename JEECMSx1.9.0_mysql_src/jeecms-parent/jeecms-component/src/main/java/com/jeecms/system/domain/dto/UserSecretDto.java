/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.domain.dto;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Objects;

/**
 * 人员密级Dto
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019/4/25
 */

public class UserSecretDto {

        /**
         * 人员密级id
         */
        private Integer id;
        /**
         * 名称
         */
        private String name;
        /**
         * 备注
         */
        private Integer remark;
        /**
         * 排序
         */
        private Integer sortNum;

        /**
         * 内容密级id数组
         */
        private List<Integer> contentSecretIds;

        /**
         * 附件密级数组
         */
        private List<Integer> annexSecretIds;

        @Override
        public String toString() {
                return "UserSecretDto{" +
                        "id=" + id +
                        ", name='" + name + '\'' +
                        ", remark=" + remark +
                        ", sortNum=" + sortNum +
                        ", contentSecretIds=" + contentSecretIds +
                        ", annexSecretIds=" + annexSecretIds +
                        '}';
        }

        public Integer getId() {
                return id;
        }

        public void setId(Integer id) {
                this.id = id;
        }

        @NotBlank
        public String getName() {
                return name;
        }

        public void setName(String name) {
                this.name = name;
        }

        public Integer getRemark() {
                return remark;
        }

        public void setRemark(Integer remark) {
                this.remark = remark;
        }

        public Integer getSortNum() {
                return sortNum;
        }

        public void setSortNum(Integer sortNum) {
                this.sortNum = sortNum;
        }

        public List<Integer> getContentSecretIds() {
                return contentSecretIds;
        }

        public void setContentSecretIds(List<Integer> contentSecretIds) {
                this.contentSecretIds = contentSecretIds;
        }

        public List<Integer> getAnnexSecretIds() {
                return annexSecretIds;
        }

        public void setAnnexSecretIds(List<Integer> annexSecretIds) {
                this.annexSecretIds = annexSecretIds;
        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                UserSecretDto that = (UserSecretDto) o;
                return Objects.equals(id, that.id) &&
                        Objects.equals(name, that.name) &&
                        Objects.equals(remark, that.remark) &&
                        Objects.equals(sortNum, that.sortNum) &&
                        Objects.equals(contentSecretIds, that.contentSecretIds) &&
                        Objects.equals(annexSecretIds, that.annexSecretIds);
        }

        @Override
        public int hashCode() {
                return Objects.hash(id, name, remark, sortNum, contentSecretIds, annexSecretIds);
        }
}
