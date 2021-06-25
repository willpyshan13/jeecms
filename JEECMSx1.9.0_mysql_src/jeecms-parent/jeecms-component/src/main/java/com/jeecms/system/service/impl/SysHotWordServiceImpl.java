/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.system.service.impl;

import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.IllegalParamExceptionInfo;
import com.jeecms.common.exception.error.SettingErrorCodeEnum;
import com.jeecms.content.domain.ContentTxt;
import com.jeecms.system.dao.SysHotWordDao;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.domain.SysHotWord;
import com.jeecms.system.domain.SysHotWordCategory;
import com.jeecms.system.domain.dto.HotWordDto;
import com.jeecms.system.service.SysHotWordService;
import org.apache.commons.lang.StringUtils;
import org.htmlparser.Node;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.ParserException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 热词Service实现类
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019-04-28
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class SysHotWordServiceImpl extends BaseServiceImpl<SysHotWord, SysHotWordDao, Integer>
	implements SysHotWordService {

	@Override
	@Transactional(rollbackFor = Exception.class, readOnly = true)
	public Page<SysHotWord> getPage(String hotWord, Integer siteId, Integer hotWordCategoryId, Integer channelId,
									Pageable pageable) {
		return dao.getPage(hotWord, siteId, hotWordCategoryId, channelId, pageable);
	}

	@Override
	@Transactional(rollbackFor = Exception.class, readOnly = true)
	public List<SysHotWord> getList(String hotWord, Integer siteId, Integer hotWordCategoryId, Integer channelId) {
		return dao.getList(hotWord, siteId, hotWordCategoryId, channelId);
	}

	@Override
	public void saveBatch(HotWordDto dto, Integer siteId, Set<String> set) throws GlobalException {
		List<SysHotWord> list = new ArrayList<>();
		List<SysHotWord> updateList = new ArrayList<>();
		for (String str : set) {
			if (StringUtils.isBlank(str)) {
				continue;
			}
			// 判断是否有该热词，没有有则添加
			if (checkByHotWord(str, null)) {
				SysHotWord sysHotWord = new SysHotWord();
				init(dto, siteId, list, str, sysHotWord);
			} else {
				// 有该热词，判断处理方式
				if (HotWordDto.DEAL_WITH_COVER.equals(dto.getDealWithType())) {
					// 覆盖原有热词
					SysHotWord sysHotWord = dao.findByHotWord(str);
					init(dto, siteId, updateList, str, sysHotWord);
				}
			}
		}
		if (!list.isEmpty()) {
			super.saveAll(list);
		}
		if (!updateList.isEmpty()) {
			super.batchUpdate(updateList);
		}
	}

	private void init(HotWordDto dto, Integer siteId, List<SysHotWord> list, String str, SysHotWord sysHotWord) {
		sysHotWord.setHotWord(str);
		sysHotWord.setHotWordCategoryId(dto.getHotTermCategoryId());
		sysHotWord.setIsTargetBlank(dto.getTargetBlank());
		sysHotWord.setLinkUrl(dto.getLinkUrl());
		sysHotWord.setRemark(dto.getRemark());
		sysHotWord.setSiteId(siteId);
		sysHotWord.setUseCount(0);
		sysHotWord.setClickCount(0);
		list.add(sysHotWord);
	}

	@Override
	public SysHotWord update(HotWordDto dto) throws GlobalException {
		if (checkByHotWord(dto.getHotWord(), dto.getId())) {
			throw new GlobalException(new IllegalParamExceptionInfo(
				SettingErrorCodeEnum.HOT_WORD_ALREADY_EXIST.getDefaultMessage(),
				SettingErrorCodeEnum.HOT_WORD_ALREADY_EXIST.getCode()));
		}
		SysHotWord sysHotWord = findById(dto.getId());
		sysHotWord.setHotWord(dto.getHotWord());
		sysHotWord.setIsTargetBlank(dto.getTargetBlank());
		sysHotWord.setLinkUrl(dto.getLinkUrl());
		sysHotWord.setRemark(dto.getRemark());

		return update(sysHotWord);
	}

	@Override
	@Transactional(rollbackFor = Exception.class, readOnly = true)
	public boolean checkByHotWord(String hotWord, Integer id) {
		if (StringUtils.isBlank(hotWord)) {
			return true;
		}
		SysHotWord sysHotWord = dao.findByHotWord(hotWord);
		if (sysHotWord == null) {
			return true;
		} else {
			if (id == null) {
				return false;
			}
			return sysHotWord.getId().equals(id);
		}
	}

	@Transactional(readOnly = true)
	@Override
	public String attachKeyword(Integer channelId, String txt, CmsSite site) {
		if (StringUtils.isBlank(txt)) {
			return txt;
		}
		List<SysHotWord> list = getList(null, null, null, channelId);
		List<SysHotWord> hotWordList = dao.findByApplyScope(site.getId(), SysHotWordCategory.CATEGORY_RANGE_ALL);
		list.addAll(hotWordList);
		list = list.parallelStream().distinct().collect(Collectors.toList());
		int len = list.size();
		if (len <= 0) {
			return txt;
		}
		String[] searchArr = new String[len];
		String[] replacementArr = new String[len];
		int i = 0;
		for (SysHotWord k : list) {
			String hotWord = k.getHotWord();
			searchArr[i] = hotWord;
			Boolean targetBlank = k.getIsTargetBlank();
			//<a target="_blank" href="http://www.baidu.com">新窗口打开</a>
			String url = k.getLinkUrl();
			if (targetBlank) {
				replacementArr[i] = "<a target=\"_blank\" href=" + url + " onclick=\"$.get('" + site.getUrl() + "hotWord/click?id=" + k.getId() + "');\">" + hotWord + "</a>";
			} else {
				replacementArr[i] = "<a href=" + url + " onclick=\"$.get('" + site.getUrl() + "hotWord/click?id=" + k.getId() + "');\">" + hotWord + "</a>";
			}
			//replacementArr[i] = k.getLinkUrl();
			i++;
		}
		try {
			Lexer lexer = new Lexer(txt);
			Node node;
			StringBuilder sb = new StringBuilder((int) (txt.length() * 1.2));
			while ((node = lexer.nextNode()) != null) {
				if (node instanceof TextNode) {
					sb.append(StringUtils.replaceEach(node.toHtml(), searchArr, replacementArr));
				} else {
					sb.append(node.toHtml());
				}
			}
			return sb.toString();
		} catch (ParserException e) {
			throw new RuntimeException(e);
		}
	}

	@Transactional(readOnly = true)
	@Override
	public void totalUserCount(Integer channelId, List<ContentTxt> contentTxts, Integer siteId) throws GlobalException {
		List<SysHotWord> list = getList(null, null, null, channelId);
		List<SysHotWord> hotWordList = dao.findByApplyScope(siteId, SysHotWordCategory.CATEGORY_RANGE_ALL);
		list.addAll(hotWordList);
		list = list.parallelStream().distinct().collect(Collectors.toList());
		int len = list.size();
		if (len <= 0) {
			return;
		}
		for (SysHotWord k : list) {
			String hotWord = k.getHotWord();
			for (ContentTxt contentTxt : contentTxts) {
				String txt = contentTxt.getAttrTxt();
				if (StringUtils.isNotBlank(txt)) {
					int count = StringUtils.countMatches(txt, hotWord);
					int userCount = k.getUseCount() != null ? k.getUseCount() : 0;
					k.setUseCount(userCount + count);
				}
			}
		}
		batchUpdate(list);
	}

	@Override
	public void click(Integer siteId, Integer id) throws GlobalException {
		SysHotWord hotWord = dao.getOne(id);
		hotWord.setClickCount(hotWord.getClickCount() != null ? hotWord.getClickCount() + 1 : 1);
		update(hotWord);
	}

	@Override
	public List<SysHotWord> delete(Integer[] categoryIds, Integer siteId) throws GlobalException {
		List<SysHotWord> byHotWordCategoryIdInAndSiteId = dao.findByHotWordCategoryIdInAndSiteId(categoryIds, siteId);
		Integer[] ids = new Integer[byHotWordCategoryIdInAndSiteId.size()];
		for (int i = 0; i < byHotWordCategoryIdInAndSiteId.size(); i++) {
			ids[i] = byHotWordCategoryIdInAndSiteId.get(i).getId();
		}
		super.physicalDelete(ids);
		return byHotWordCategoryIdInAndSiteId;
	}
}