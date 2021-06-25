package com.jeecms.statistics.domain.vo;

import java.math.BigDecimal;

/**
 * 网站概况vo（也用于趋势统计中计算和）
 * 
 * @author: chenming
 * @date: 2019年7月5日 下午3:00:16
 */
public class StatisitcsOverviewVo {
	/** PV值 */
	private Integer pv;
	/** UV值 */
	private Integer uv;
	/** IP值 */
	private Integer ip;
	/** 跳出率-精确到小数点后4位 */
	private BigDecimal depthNum;
	/** 平均访问时长 */
	private Integer time;
	/** 跳出率-精确到小数点后4位 */
	private String depthNumString;
	/** 平均访问时长 */
	private String timeString;


	public StatisitcsOverviewVo(Integer pv, Integer uv, Integer ip, BigDecimal depthNum, Integer time) {
		super();
		this.pv = pv;
		this.uv = uv;
		this.ip = ip;
		this.depthNum = depthNum;
		this.time = time;
	}

	public StatisitcsOverviewVo(Integer pv, Integer uv, Integer ip, String depthNumString, String timeString) {
		super();
		this.pv = pv;
		this.uv = uv;
		this.ip = ip;
		this.depthNumString = depthNumString;
		this.timeString = timeString;
	}

	public StatisitcsOverviewVo() {
		super();
	}

	public Integer getPv() {
		return pv;
	}

	public void setPv(Integer pv) {
		this.pv = pv;
	}

	public Integer getUv() {
		return uv;
	}

	public void setUv(Integer uv) {
		this.uv = uv;
	}

	public Integer getIp() {
		return ip;
	}

	public void setIp(Integer ip) {
		this.ip = ip;
	}

	public BigDecimal getDepthNum() {
		return depthNum;
	}

	public void setDepthNum(BigDecimal depthNum) {
		this.depthNum = depthNum;
	}

	public Integer getTime() {
		return time;
	}

	public void setTime(Integer time) {
		this.time = time;
	}

	public String getDepthNumString() {
		return depthNumString;
	}

	public void setDepthNumString(String depthNumString) {
		this.depthNumString = depthNumString;
	}

	public String getTimeString() {
		return timeString;
	}

	public void setTimeString(String timeString) {
		this.timeString = timeString;
	}
}
