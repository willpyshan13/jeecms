/**
 *  江西金磊科技发展有限公司  All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.common.security;

import com.jeecms.common.exception.GlobalException;

/**
 * 密码认证service
 * @author: tom
 * @date:   2018年3月20日 下午2:13:38   
 */
public interface PasswdService {
        
	/**
	 * 密码
	 * @param saltBytes 混淆码数组
	 * @param rawPass 明文密码
	 * @return 密码
	 */
	public String entryptPassword(byte[] saltBytes, String rawPass);
	
	/**
	 * 判断密码是否有效
	 * @param rawPass 明文密码
	 * @param salt 混淆码
	 * @param encPass 加密密码
	 * @return boolean类型
	 */
	public boolean isPasswordValid(String rawPass, String salt, String encPass);

	/**
	 * 更改加密方式
	 * @param smCredentialsDigest 是否采用国密算法
	 * @param password  更改后初始化用户的密码
	 */
	void changeCredentialsDigest(boolean smCredentialsDigest, String password) throws GlobalException;


	/**
	 * 解密
	 * @param encryptText 密文
	 * @return
	 */
	String decrypt(String encryptText);

	/**
	 * 加密
	 * @param plainText 明文
	 * @return
	 */
	String encrypt(String plainText);
}
