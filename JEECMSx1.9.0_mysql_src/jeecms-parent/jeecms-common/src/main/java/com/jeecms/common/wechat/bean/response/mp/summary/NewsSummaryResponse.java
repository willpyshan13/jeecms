/**
 * * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 * 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.common.wechat.bean.response.mp.summary;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.io.Serializable;
import java.util.List;

/**
 * 获取图文群发每日数据
 * @author ljw
 * @date 2020年6月22日
 */
public class NewsSummaryResponse implements Serializable {

	private static final long serialVersionUID = 86857674455305438L;
	private List<NewsSummaryResult> list;

    public List<NewsSummaryResult> getList() {
        return list;
    }

    public void setList(List<NewsSummaryResult> list) {
        this.list = list;
    }

    public static class NewsSummaryResult {

        /** 数据的日期 */
        @XStreamAlias("ref_date")
        private String refDate;

        /**
         * 请注意：这里的msgid实际上是由msgid（图文消息id，这也就是群发接口调用后返回的msg_data_id）
         * 和index（消息次序索引）组成， 例如12003_3， 其中12003是msgid，即一次群发的消息的id；
         * 3为index，假设该次群发的图文消息共5个文章（因为可能为多图文），3表示5个中的第3个
         */
        @XStreamAlias("msgid")
        private String msgid;

        /** 标题 */
        @XStreamAlias("title")
        private String title;

        /** 图文页（点击群发图文卡片进入的页面）的阅读人数 */
        @XStreamAlias("int_page_read_user")
        private Integer intPageReadUser;

        /**图文页的阅读次数**/
        @XStreamAlias("int_page_read_count")
        private Integer intPageReadCount;

        /**原文页（点击图文页“阅读原文”进入的页面）的阅读人数，无原文页时此处数据为0**/
        @XStreamAlias("ori_page_read_user")
        private Integer oriPageReadUser;

        /**原文页的阅读次数**/
        @XStreamAlias("ori_page_read_count")
        private Integer oriPageReadCount;

        /**分享的人数**/
        @XStreamAlias("share_user")
        private Integer shareUser;

        /**分享的次数**/
        @XStreamAlias("share_count")
        private Integer shareCount;

        /**收藏的人数**/
        @XStreamAlias("add_to_fav_user")
        private Integer addToFavUser;

        /**收藏的次数**/
        @XStreamAlias("add_to_fav_count")
        private Integer addToFavCount;

		public NewsSummaryResult(){}

		public String getRefDate() {
			return refDate;
		}

		public void setRefDate(String refDate) {
			this.refDate = refDate;
		}

		public String getMsgid() {
			return msgid;
		}

		public void setMsgid(String msgid) {
			this.msgid = msgid;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public Integer getIntPageReadUser() {
			return intPageReadUser;
		}

		public void setIntPageReadUser(Integer intPageReadUser) {
			this.intPageReadUser = intPageReadUser;
		}

		public Integer getIntPageReadCount() {
			return intPageReadCount;
		}

		public void setIntPageReadCount(Integer intPageReadCount) {
			this.intPageReadCount = intPageReadCount;
		}

		public Integer getOriPageReadUser() {
			return oriPageReadUser;
		}

		public void setOriPageReadUser(Integer oriPageReadUser) {
			this.oriPageReadUser = oriPageReadUser;
		}

		public Integer getOriPageReadCount() {
			return oriPageReadCount;
		}

		public void setOriPageReadCount(Integer oriPageReadCount) {
			this.oriPageReadCount = oriPageReadCount;
		}

		public Integer getShareUser() {
			return shareUser;
		}

		public void setShareUser(Integer shareUser) {
			this.shareUser = shareUser;
		}

		public Integer getShareCount() {
			return shareCount;
		}

		public void setShareCount(Integer shareCount) {
			this.shareCount = shareCount;
		}

		public Integer getAddToFavUser() {
			return addToFavUser;
		}

		public void setAddToFavUser(Integer addToFavUser) {
			this.addToFavUser = addToFavUser;
		}

		public Integer getAddToFavCount() {
			return addToFavCount;
		}

		public void setAddToFavCount(Integer addToFavCount) {
			this.addToFavCount = addToFavCount;
		}
	}

}
