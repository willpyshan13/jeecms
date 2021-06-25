package com.jeecms.common.util;

import cn.hutool.core.util.ReUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;


/**
 * XssUtil 工具类
 * @author: tom
 * @date: 2018年11月26日 下午5:40:39
 * @Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice
 *             仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
public class XssUtil {
	static Logger logger = LoggerFactory.getLogger(XssUtil.class);
	static Pattern scriptPattern = Pattern.compile("<script>(.*?)</script>", Pattern.CASE_INSENSITIVE);
//	static Pattern scriptPatternSrc = Pattern.compile("src[\r\n]*=[\r\n]*\\\'(.*?)\\\'",
//			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	static Pattern scriptPatternSrc = Pattern.compile("src=\"(.*?)",Pattern.CASE_INSENSITIVE );

	static Pattern scriptPatternHref = Pattern.compile("href=\"(.*?)",Pattern.CASE_INSENSITIVE );

	static Pattern singleScriptPattern = Pattern.compile("</script>", Pattern.CASE_INSENSITIVE);
	static Pattern singleBeginScriptPattern = Pattern.compile("<script(.*?)>",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	static Pattern singleBeginIframePattern = Pattern.compile("(.*?)<iframe(.*?)>",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	static Pattern criptPattern = Pattern.compile("eval\\((.*?)\\)",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	static Pattern expressionPattern = Pattern.compile("expression\\((.*?)\\)",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	static Pattern javascriptPattern = Pattern.compile("javascript:", Pattern.CASE_INSENSITIVE);
	//alert
	static Pattern alertPattern = Pattern.compile("(.*?)alert(.*?)", Pattern.CASE_INSENSITIVE);

	static Pattern importPattern = Pattern.compile("(.*?)import(.*?)", Pattern.CASE_INSENSITIVE);

	static Pattern functionPattern = Pattern.compile("(.*?)function(.*?)", Pattern.CASE_INSENSITIVE);

	static Pattern vbscriptPattern = Pattern.compile("vbscript:", Pattern.CASE_INSENSITIVE);

	static Pattern onScriptPattern = Pattern.compile("on(.*?)=['|\"](.*?)['|\"]",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

	static Set<String> rechTextClasses = new HashSet<String>();

	static {
		rechTextClasses.add("Article");
		rechTextClasses.add("ProductTxt");
		rechTextClasses.add("ProductDTO");
	}

	public static Set<String> getRechTextClassNames() {
		return rechTextClasses;
	}

	/**
	 * 是否富文本类型
	 * @Title: isRechTextClass
	 * @param className 类名 
	 * @return: boolean
	 */
	public static boolean isRechTextClass(String className) {
		if (StringUtils.isBlank(className)) {
			return false;
		}
		for (String r : rechTextClasses) {
			if (className.endsWith(r)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 清理xss特殊字符
	 *
	 * @Title: cleanXSS
	 * @param className
	 *            富文本类名
	 * @param value 过滤的字符串
	 * @return: String
	 */
	public static String cleanXSS(String className, String value) {
		if (isRechTextClass(className)) {
			return value;
		} else {
			return cleanXSS(value);
		}
	}

	/**
	 * 清理xss特殊字符
	 * @Title: cleanXSS
	 * @param value 过滤的字符串
	 * @return: String
	 */
	public static String cleanXSS(String value) {
		if (value != null) {
			// 避免script 标签
			value = scriptPattern.matcher(value).replaceAll("");

			// 避免src形式的表达式
			value = scriptPatternSrc.matcher(value).replaceAll("");

			// 避免href形式的表达式
			value = scriptPatternHref.matcher(value).replaceAll("");
			// 删除单个的 </script> 标签
			value = singleScriptPattern.matcher(value).replaceAll("");

			// 删除单个的<script ...> 标签
			value = singleBeginScriptPattern.matcher(value).replaceAll("");
			// 删除单个的<iframe ...> 标签
			value = singleBeginIframePattern.matcher(value).replaceAll("");
			// 避免 eval(...) 形式表达式
			value = criptPattern.matcher(value).replaceAll("");

			// 避免 e­xpression(...) 表达式
			value = expressionPattern.matcher(value).replaceAll("");

			// 避免 javascript: 表达式
			value = javascriptPattern.matcher(value).replaceAll("");

			value = alertPattern.matcher(value).replaceAll("");

			value = importPattern.matcher(value).replaceAll("");

			value = functionPattern.matcher(value).replaceAll("");

			// 避免 vbscript: 表达式
			value = vbscriptPattern.matcher(value).replaceAll("");
			// 避免 onXX= 表达式
			value = onScriptPattern.matcher(value).replaceAll("");

			if (value.contains("%df") || value.contains("%22") || value.contains("%3E") ||value.contains("%3e")
					|| value.contains("%3C") || value.contains("%3c")
					|| value.contains("<") || value.contains(">") || value.contains("\"")
					|| value.contains("'")|| value.contains(" and ")
					|| value.contains(" or ") || value.contains("1=1") || value.contains("(") || value.contains(")")) {
				value.replace("%df","");
				value.replace("%22","");
				value.replace("%3E","");
				value.replace("%3e","");
				value.replace("%3C","");
				value.replace("%3c","");
				value.replace("<","");
				value.replace(">","");
				value.replace("'","");
				value.replace(" and ","");
				value.replace(" or ","");
				value.replace("1=1","");
				value.replace("(","");
				value.replace(")","");
			}

		}
		return value;
	}

	public static void main(String[] args) {
		String value = "英语学习资料<img src=1 onerror=alert(/xss/)>";
		System.out.println(alertPattern.pattern());
		boolean isMatch = ReUtil.isMatch(alertPattern.pattern(), value);
		System.out.println(isMatch);

		String val = "111<iframe src=\"http://www.baidu.com\" width=\"1000px\" height=\"1000px\"></iframe>";
		System.out.println(containXss(val));
	}

	public static boolean containXss(String value) {
		if (value != null) {
			// 避免script 标签
			if (ReUtil.isMatch(scriptPattern.pattern(), value)) {
				return true;
			}
			// 避免src形式的表达式
			if (ReUtil.isMatch(scriptPatternSrc.pattern(), value)) {
				return true;
			}
			// 避免href形式的表达式
			if (ReUtil.isMatch(scriptPatternHref.pattern(), value)) {
				return true;
			}
			// 删除单个的 </script> 标签
			if (ReUtil.isMatch(singleScriptPattern.pattern(), value)) {
				return true;
			}
			// 删除单个的<script ...> 标签
			if (ReUtil.isMatch(singleBeginScriptPattern.pattern(), value)) {
				return true;
			}
			// 删除单个的<iframe ...> 标签
			if (ReUtil.isMatch(singleBeginIframePattern.pattern(), value)) {
				return true;
			}
			// 避免 eval(...) 形式表达式
			if (ReUtil.isMatch(criptPattern.pattern(), value)) {
				return true;
			}
			// 避免 e­xpression(...) 表达式
			if (ReUtil.isMatch(expressionPattern.pattern(), value)) {
				return true;
			}
			// 避免 javascript: 表达式
			if (ReUtil.isMatch(javascriptPattern.pattern(), value)) {
				return true;
			}
			if (ReUtil.isMatch(alertPattern.pattern(), value)) {
				return true;
			}
			if (ReUtil.isMatch(importPattern.pattern(), value)) {
				return true;
			}
			if (ReUtil.isMatch(functionPattern.pattern(), value)) {
				return true;
			}
			// 避免 vbscript: 表达式
			if (ReUtil.isMatch(vbscriptPattern.pattern(), value)) {
				return true;
			}
			if (ReUtil.isMatch(onScriptPattern.pattern(), value)) {
				return true;
			}
			if (value.contains("%df") || value.contains("%22") || value.contains("%3E") || value.contains("%3e")
                    || value.contains("%3C") || value.contains("%3c") || value.contains(" and ")
					|| value.contains(" or ") || value.contains("1=1")) {
				return true;
			}

		}
		return false;
	}

}
