/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.front.controller;

import com.jeecms.auth.domain.CoreUser;
import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.domain.ChannelAttr;
import com.jeecms.channel.domain.ChannelAttrRes;
import com.jeecms.channel.domain.ChannelExt;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.base.controller.BaseTreeController;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.UserErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.content.domain.ChannelTxt;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.jeecms.common.constants.WebConstants.ARRAY_SPT;

/**
 * 栏目控制层
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019/7/19 17:40
 */

@RestController
@RequestMapping("/channel")
public class ChannelController extends BaseTreeController<Channel, Integer> {

	@Autowired
	private ChannelService channelService;

	/**
	 * 栏目列表
	 *
	 * @param parentId 父id（不传查询顶级栏目）
	 * @param request  {@link HttpServletRequest}
	 * @return Channel
	 */
	@GetMapping("/list")
	@SerializeField(clazz = Channel.class, includes = {"id", "name", "path", "description", "link", "linkTarget", "url"})
	public ResponseInfo list(Integer parentId, Integer count, HttpServletRequest request) {
		/**如果传递了父栏目id则忽略当前站点id参数，否则用户需要查询子站的栏目查询不到*/
		Integer siteId = SystemContextUtils.getSiteId(request);
		if(parentId!=null){
			siteId = null;
		}
		List<Channel> channels = channelService.findListByParentId(
				siteId , parentId, false, false);
		Stream<Channel> sorted = channels.parallelStream().filter(o -> !o.getHasDeleted() && !o.getRecycle())
			.sorted(Comparator.comparing(Channel::getSortNum));
		if (count != null) {
			sorted = sorted.limit(count);
		}
		channels = sorted.collect(Collectors.toList());
		return new ResponseInfo(channels);
	}

	/**
	 * 栏目列表
	 *
	 * @return Channel
	 */
	@GetMapping("/getByIds")
	@SerializeField(clazz = Channel.class, includes = {"id", "name", "path", "description", "link", "linkTarget", "url"})
	public ResponseInfo get(String ids) {
		List<Channel> list;
		Integer[] channelIds = getIntArray(ids);
		if (ArrayUtils.isNotEmpty(channelIds)) {
			list = channelService.findAllById(Arrays.asList(channelIds));
			list = list.parallelStream().sorted(Comparator.comparing(Channel::getSortNum)).collect(Collectors.toList());
		} else {
			list = new ArrayList<>(0);
		}
		return new ResponseInfo(list);
	}

	/**
	 * 栏目树
	 * @param request {@link HttpServletRequest}
	 * @return ResponseInfo
	 * @throws GlobalException 异常
	 */
	@GetMapping("/tree")
	public ResponseInfo channelTree(HttpServletRequest request) throws GlobalException {
		CoreUser coreUser = SystemContextUtils.getCoreUser();
		if (coreUser == null) {
			return new ResponseInfo(UserErrorCodeEnum.THE_USER_NOT_LOGIN.getCode(),
				UserErrorCodeEnum.THE_USER_NOT_LOGIN.getDefaultMessage());
		}
		Integer siteId = SystemContextUtils.getSiteId(request);
		// 获取到
		List<Channel> contributeChannels  = channelService.getChanelByRecycle(siteId, false);
		if (coreUser.getAdmin() || (coreUser.getUserGroup() != null && coreUser.getUserGroup().getIsAllChannelContribute())) {
			for (Channel contributeChannel : contributeChannels) {
				if (!contributeChannel.getContribute()) {
					contributeChannel.setOperatingContribute(false);
				}
			}
		} else {
			List<Channel> userContribute = coreUser.getUserGroup() != null
				? coreUser.getUserGroup().getContributeChannels() : null;
			List<Integer> channelIds = new ArrayList<Integer>();
			if (userContribute != null && userContribute.size() > 0) {
				channelIds = userContribute.stream().map(Channel::getId).collect(Collectors.toList());
			}
			Boolean isIdSize = channelIds.size() > 0 ? true : false;
			for (Channel contributeChannel : contributeChannels) {
				if (isIdSize) {
					if (!channelIds.contains(contributeChannel.getId())) {
						contributeChannel.setOperatingContribute(false);
					}
				} else {
					contributeChannel.setOperatingContribute(false);
				}
			}
		}
		if (contributeChannels.size() > 0) {
			contributeChannels = contributeChannels.stream()
				.sorted(Comparator.comparing(Channel::getSortNum)
					.thenComparing(
						Comparator.comparing(
							Channel::getCreateTime)))
				.collect(Collectors.toList());
			return new ResponseInfo(super.getChildTree(contributeChannels, false, "id","name","operatingContribute"));
		}
		return new ResponseInfo();
	}

	/**
	 * 获取栏目详情
	 *
	 * @param id     栏目id
	 * @param siteId 站点id
	 * @param path   栏目访问地址
	 * @return
	 */
	@GetMapping("/get")
	@MoreSerializeField({
			@SerializeField(clazz = Channel.class, includes = {"id", "name", "path", "description", "link", "linkTarget",
					"url", "attr","modelId","viewSum","channelExt","txtMap"}),
			@SerializeField(clazz = ChannelExt.class, includes = {"seoDescription", "seoKeywork", "seoTitle", "txt", "resourcesSpaceData"}),
			@SerializeField(clazz = ChannelTxt.class, includes = {"attrKey", "attrTxt"}),
			@SerializeField(clazz = ChannelAttr.class, excludes = {"channel", "cmsOrg","city","area"}),
			@SerializeField(clazz = ChannelAttrRes.class, excludes = {"channelAttr"}),
			@SerializeField(clazz = ResourcesSpaceData.class, excludes = {"uploadFtp", "uploadOss", "users",
					"space", "user"}),
	})
	public ResponseInfo get(Integer id, Integer siteId, String path) {
		Channel channel = channelService.get(id, siteId, path);
		return new ResponseInfo(channel);
	}

	private Integer[] getIntArray(String str) {
		if (StringUtils.isBlank(str)) {
			return new Integer[0];
		}
		String[] arr = StringUtils.split(str, ARRAY_SPT);
		Integer[] ids = new Integer[arr.length];
		int i = 0;
		for (String s : arr) {
			ids[i++] = Integer.valueOf(s);
		}
		return ids;
	}
}
