/*
* @Copyright:  江西金磊科技发展有限公司  All rights reserved. 
* Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
*/
package com.jeecms.publish.domain;


import com.jeecms.common.base.domain.AbstractIdDomain;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * 内容发布统计记录表
* @author ljw
* @version 1.0
* @date 2020-06-04
*/
@Entity
@Table(name = "jc_statistics_publish")
public class StatisticsPublish extends AbstractIdDomain<Integer> implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer id;
    /**站点ID**/
    private Integer siteId;
    /** 计数 */
    private  Integer numbers;
    /** 统计类型（1.内容发布数 2.微信文章发布数 3.微博文章发布数，4.新增评论数 5. 新增栏目数） */
    private  Integer types;
    /** 统计时间 */
    private Date statisticsDay;
	public StatisticsPublish() {}
	
    @Override
    @Id
    @Column(name = "id", nullable = false, length = 11)
    @TableGenerator(name = "jc_statistics_publish", pkColumnValue = "jc_statistics_publish", initialValue = 1, allocationSize = 10)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "jc_statistics_publish")
    public Integer getId() {
        return this.id;
    }

    public void setId (Integer id) {
        this.id = id;
    }

    @Column(name = "site_id", nullable = false, length = 11)
    public Integer getSiteId() {
        return siteId;
    }

    public void setSiteId(Integer siteId) {
        this.siteId = siteId;
    }

    @Column(name = "numbers", nullable = false, length = 11)
    public Integer getNumbers() {
        return numbers;
    }

    public void setNumbers (Integer numbers) {
        this.numbers = numbers;
    }
    
    @Column(name = "types", nullable = false, length = 6)
    public Integer getTypes() {
        return types;
    }

    public void setTypes (Integer types) {
        this.types = types;
    }
    
    @Column(name = "statistics_day", nullable = false, length = 10)
    public Date getStatisticsDay() {
        return statisticsDay;
    }

    public void setStatisticsDay (Date statisticsDay) {
        this.statisticsDay = statisticsDay;
    }
    


}