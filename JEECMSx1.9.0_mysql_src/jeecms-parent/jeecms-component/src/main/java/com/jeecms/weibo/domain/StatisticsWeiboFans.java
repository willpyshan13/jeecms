/*
* @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/
package com.jeecms.weibo.domain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * 微博粉丝统计
* @author ljw
* @version 1.0
* @date 2020-06-23
*/
@Entity
@Table(name = "jc_statistics_weibo_fans")
public class StatisticsWeiboFans implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    /** 微博用户ID */
    private String weiboUid;
    /** 粉丝数 */
    private  Integer fansCount;
    /** 统计日期 */
    private Date statisticsDay;
	public StatisticsWeiboFans() {}
	
    @Id
    @Column(name = "id", nullable = false, length = 11)
    @TableGenerator(name = "jc_statistics_weibo_fans", pkColumnValue = "jc_statistics_weibo_fans", initialValue = 1, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_statistics_weibo_fans")
    public Integer getId() {
        return this.id;
    }

    public void setId (Integer id) {
        this.id = id;
    }

    @Column(name = "u_id", nullable = false, length = 50)
    public String getWeiboUid() {
        return weiboUid;
    }

    public void setWeiboUid(String weiboUid) {
        this.weiboUid = weiboUid;
    }

    @Column(name = "fans_count", nullable = false, length = 11)
    public Integer getFansCount() {
        return fansCount;
    }

    public void setFansCount (Integer fansCount) {
        this.fansCount = fansCount;
    }
    
    @Column(name = "statistics_day", nullable = false, length = 10)
    public Date getStatisticsDay() {
        return statisticsDay;
    }

    public void setStatisticsDay (Date statisticsDay) {
        this.statisticsDay = statisticsDay;
    }
    


}