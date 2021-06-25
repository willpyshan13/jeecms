package com.jeecms.resource.ueditor.define;

import java.util.Map;
import java.util.HashMap;

/**
 * 定义请求action类型
 * @author hancong03@baidu.com
 *
 */
@SuppressWarnings("serial")
public final class ActionMap {
	private ActionMap() {
	}

	protected static final Map<String, Integer> MAPPING = new HashMap<>();
	/**
	 * 获取配置请求
	 */
	public static final int CONFIG = 0;
	public static final int UPLOAD_IMAGE = 1;
	public static final int UPLOAD_SCRAWL = 2;
	public static final int UPLOAD_VIDEO = 3;
	public static final int UPLOAD_FILE = 4;
	public static final int CATCH_IMAGE = 5;
	public static final int LIST_FILE = 6;
	public static final int LIST_IMAGE = 7;
	
	static {
			MAPPING.put( "config", ActionMap.CONFIG );
			MAPPING.put( "uploadimage", ActionMap.UPLOAD_IMAGE );
			MAPPING.put( "uploadscrawl", ActionMap.UPLOAD_SCRAWL );
			MAPPING.put( "uploadvideo", ActionMap.UPLOAD_VIDEO );
			MAPPING.put( "uploadfile", ActionMap.UPLOAD_FILE );
			MAPPING.put( "catchimage", ActionMap.CATCH_IMAGE );
			MAPPING.put( "listfile", ActionMap.LIST_FILE );
			MAPPING.put( "listimage", ActionMap.LIST_IMAGE );
	}
	
	public static int getType ( String key ) {
		return ActionMap.MAPPING.get( key );
	}
	
}
