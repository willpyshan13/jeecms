package com.jeecms.resource.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.jeecms.auth.domain.CoreRole;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.auth.service.CoreRoleService;
import com.jeecms.auth.service.CoreUserService;
import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.constants.UploadEnum.UploadServerType;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.IllegalParamExceptionInfo;
import com.jeecms.common.exception.UploadExceptionInfo;
import com.jeecms.common.upload.FileRepository;
import com.jeecms.common.web.springmvc.RealPathResolver;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.resource.dao.ResourcesSpaceDataDao;
import com.jeecms.resource.domain.ResourcesSpace;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.resource.domain.UploadFtp;
import com.jeecms.resource.domain.UploadOss;
import com.jeecms.resource.domain.dto.ResourcesSpaceShareDto;
import com.jeecms.resource.service.ResourcesSpaceDataService;
import com.jeecms.resource.service.ResourcesSpaceService;
import com.jeecms.system.domain.CmsOrg;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.service.CmsOrgService;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ResourcesSpace service实现
 *
 * @author: tom
 * @date: 2019年5月9日 上午10:15:31
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class ResourcesSpaceDataServiceImpl extends BaseServiceImpl<ResourcesSpaceData, ResourcesSpaceDataDao, Integer>
		implements ResourcesSpaceDataService {
	private static final Logger log = LoggerFactory.getLogger(ResourcesSpaceDataServiceImpl.class);
	@Autowired
	private ResourcesSpaceService spaceService;
	@Autowired
	private CoreUserService userService;
	@Autowired
	private CmsOrgService orgService;
	@Autowired
	private CoreRoleService roleService;
	@Autowired
	private RealPathResolver realPathResolver;

	@Override
	public Page<ResourcesSpaceData> getPage(String alias, Integer userId, Integer storeResourcesSpaceId,
											Short resourceType, Short shareStatus, Date beginCreateTime, Date endCreateTime,
											Pageable pageable) {
		return dao.getList(alias, userId, storeResourcesSpaceId, resourceType, shareStatus, beginCreateTime,
				endCreateTime, pageable);
	}

	@Override
	public Page<ResourcesSpaceData> getPage(Integer userId, String alias, String shareUser, Short resourceType,
											Integer storeResourcesSpaceId, Integer uId, Pageable pageable) {
		return dao.getShareList(userId, alias, shareUser, resourceType, storeResourcesSpaceId, uId, pageable);
	}

	@Override
	public List<ResourcesSpaceData> getByUserId(Integer userId) {
		return dao.getByUserId(userId);
	}

	@Override
	public ResourcesSpaceData save(Integer coreUserId, Integer spaceId, String fileName, Integer size, String url,
                                   String dimensions, Short resourceType, boolean isDisplay, Integer siteId, Integer duration, UploadFtp uploadFtp,
                                   UploadOss uploadOss, Integer videoCoverId) throws GlobalException {
		ResourcesSpaceData data = new ResourcesSpaceData();
		data.init();
		data.setResourceType(resourceType);
		data.setDimensions(dimensions);
		data.setSize(size);
		data.setUrl(url);
		if (url != null) {
			data.setSuffix(url.substring(url.lastIndexOf(".")));
		}
		data.setResourceDate(duration);
		if (fileName.length() > 50) {
			fileName = fileName.substring(0, 50);
		}
		String alias = filename(fileName, null, coreUserId, 0, spaceId);
		data.setAlias(alias);
		if (spaceId != null) {
			ResourcesSpace space = spaceService.get(spaceId);
			data.setStoreResourcesSpaceId(spaceId);
			data.setSpace(space);
		}
		if (coreUserId != null) {
			CoreUser user = userService.findById(coreUserId);
			data.setUser(user);
			data.setUserId(coreUserId);
			if (user != null) {
				data.setCreateUser(user.getUsername());
			}
		}
		if(videoCoverId!=null){
			data.setVideoCoverId(videoCoverId);
			data.setVideoCover(findById(videoCoverId));
		}
		data.setDisplay(isDisplay);
		data.setUploadFtp(uploadFtp);
		data.setUploadOss(uploadOss);
		return save(data);
	}

	@Override
	public List<String> selectUrlsByIds(Iterable<Integer> targetIds) throws GlobalException {
		List<ResourcesSpaceData> resources = dao.findAllById(targetIds);
		return convertUrl(resources);
	}

	/**
	 * 转换路径 通过实体集合转换成路径集合
	 *
	 * @param resources 资源实体列表
	 * @return List
	 */
	public List<String> convertUrl(List<ResourcesSpaceData> resources) {
		List<String> urls = null;
		if (!ObjectUtils.isEmpty(resources)) {
			urls = new ArrayList<String>();
			for (ResourcesSpaceData resource : resources) {
				urls.add(resource.getUrl());
			}
		}
		return urls;
	}

	@Override
	public List<ResourcesSpaceData> findByIds(Integer[] ids) throws GlobalException {
		return dao.findByIds(ids, false);
	}

	@Override
	public ResourcesSpaceData findByIdAndHasDeleted(Integer id) throws GlobalException {
		return dao.findByIdAndHasDeleted(id, false);
	}

	@Override
	public ResourcesSpaceData saveTest(ResourcesSpaceData data) throws GlobalException {
		if (data.getUrl() != null) {
			data.setSuffix(data.getUrl().substring(data.getUrl().lastIndexOf(".")));
		}
		return super.save(data);
	}

	@Override
	public void share(ResourcesSpaceShareDto dto) throws GlobalException {
		// 用户保存组织，角色，用户数组中获取到的用户对象
		Set<CoreUser> set = new HashSet<>();
		List<Integer> orgIds = dto.getOrgIds();
		List<Integer> roleIds = dto.getRoleIds();
		List<Integer> userIds = dto.getUserIds();
		// 如果都为空，抛出异常
		if (CollUtil.isNotEmpty(orgIds) || CollUtil.isNotEmpty(roleIds) || CollUtil.isNotEmpty(userIds)) {
			// 遍历组织获取用户添加到set集合中
			if (CollUtil.isNotEmpty(orgIds)) {
				List<CmsOrg> cmsOrgs = orgService.findAllById(orgIds);
				for (CmsOrg cmsOrg : cmsOrgs) {
					List<CoreUser> coreUsers = cmsOrg.getUsers();
					set.addAll(coreUsers);
				}
			}
			// 遍历角色获取用户添加到set集合中
			if (CollUtil.isNotEmpty(roleIds)) {
					List<CoreRole> coreRoles = roleService.findAllById(roleIds);
				for (CoreRole coreRole : coreRoles) {
					List<CoreUser> coreUsers = coreRole.getUsers();
					set.addAll(coreUsers);
				}
			}
			// 获取到角色赋值到set集合中
			if (CollUtil.isNotEmpty(userIds)) {
				List<CoreUser> coreUsers = userService.findAllById(userIds);
				set.addAll(coreUsers);
			}
			// set长度不为0则不操作
			if (!set.isEmpty()) {
					List<ResourcesSpaceData> resourcesSpaceDatas = super.findAllById(Arrays.asList(dto.getIds()));
				for (ResourcesSpaceData resourcesSpaceData : resourcesSpaceDatas) {
					List<CoreUser> users = resourcesSpaceData.getUsers();
					// 先判断是否存在共享列表，存在则先清空
					if (users != null) {
						resourcesSpaceData.getUsers().clear();
					}
					List<CoreUser> list = new ArrayList<>(set);
					resourcesSpaceData.setShareStatus(ResourcesSpaceData.STATUS_SHARED);
					resourcesSpaceData.setUsers(list);
					resourcesSpaceData.setShareTime(Calendar.getInstance().getTime());
					// 修改资源空间状态为下属资源为被分享
					spaceService.shareChildShared(resourcesSpaceData.getStoreResourcesSpaceId(), list);
				}
			}
		} else {
			throw new GlobalException(new IllegalParamExceptionInfo());
		}

	}

	@Override
	public List<ResourcesSpaceData> unShare(Integer[] ids) throws GlobalException {
		List<ResourcesSpaceData> list = new ArrayList<>();
		Set<Integer> spaces = new HashSet<>();
		List<ResourcesSpaceData> datas = super.findAllById(Arrays.asList(ids));
		for (ResourcesSpaceData data : datas) {
			data.setShareStatus(ResourcesSpaceData.STATUS_NOT_SHARED);
			if (data.getUsers() != null) {
				data.getUsers().clear();
			}
			spaces.add(data.getStoreResourcesSpaceId());
			list.add(data);
		}
		if (!spaces.isEmpty()) {
			spaceService.unShareSpace(spaces.toArray(new Integer[spaces.size()]));
		}
		super.batchUpdate(list);
		return list;
	}

	@Override
	public void unShare(List<ResourcesSpaceData> list) throws GlobalException {
		for (ResourcesSpaceData spaceData : list) {
			spaceData.setShareStatus(ResourcesSpaceData.STATUS_NOT_SHARED);
			if (spaceData.getUsers() != null) {
				spaceData.getUsers().clear();
			}
			update(spaceData);
		}
	}

	@Override
	public List<ResourcesSpaceData> shareAll(List<CoreUser> set, List<ResourcesSpaceData> dataList)
			throws GlobalException {
		int i = 0;
		if (dataList != null) {
			for (ResourcesSpaceData data : dataList) {
				// 设置分享用户，已存在先清除
				if (data.getUsers() != null) {
					data.getUsers().clear();
				}
				data.setUsers(new ArrayList<CoreUser>(set));
				// 设置状态为已分享
				data.setShareStatus(ResourcesSpaceData.STATUS_SHARED);
				data.setShareTime(Calendar.getInstance().getTime());
				dataList.set(i, data);
				i++;
			}
			batchUpdate(dataList);
		}
		return new ArrayList<>(0);
	}

	@Override
	public List<CoreUser> findUserByShareStatus(Short shareStatus) {
		return dao.findUserByShareStatus(shareStatus);
	}

	@Override
	public List<ResourcesSpaceData> findBySpaceId(Integer spaceId) {
		return dao.findBySpaceId(spaceId);
	}

	@Override
	public void updateToDisplay(List<ResourcesSpace> spaces, Boolean display) throws GlobalException {
		if (spaces != null && spaces.size() > 0) {
			List<Integer> list = spaces.parallelStream().map(ResourcesSpace::getId).collect(Collectors.toList());
			List<ResourcesSpaceData> dates = dao.findBySpaceIds(list);
			for (ResourcesSpaceData date : dates) {
				date.setDisplay(false);
			}
			batchUpdate(dates);
		}
	}

	@Override
	public void copyBatch(Integer[] ids, Integer userId, Integer paceId, CmsSite site)
			throws GlobalException {
		List<ResourcesSpaceData> list = findAllById(Arrays.asList(ids));
		List<ResourcesSpaceData> spaceDataList = findBySpaceId(paceId);
		List<String> spaceString = new ArrayList<String>(spaceDataList.size());
		for (ResourcesSpaceData spaceData : spaceDataList) {
			spaceString.add(spaceData.getAlias());
		}
		List<ResourcesSpaceData> beans = new ArrayList<ResourcesSpaceData>();
		for (ResourcesSpaceData spaceData : list) {
			File fromFile = getFileWithFilename(spaceData, null);
			String fileUrl = doUpload(fromFile, site);
			ResourcesSpaceData bean = new ResourcesSpaceData();
			bean.setUserId(userId);
			String alias = spaceData.getAlias();
			alias = changeAlias(alias, spaceString, 0);
			bean.setAlias(alias);
			bean.setUrl(fileUrl);
			bean.setSuffix(spaceData.getUrl().substring(spaceData.getUrl().lastIndexOf(".")));
			bean.setSize(spaceData.getSize());
			bean.setDimensions(spaceData.getDimensions());
			bean.setRefCount(0);
			bean.setResourceType(spaceData.getResourceType());
			bean.setShareStatus(ResourcesSpaceData.STATUS_NOT_SHARED);
			bean.setResourceDate(spaceData.getResourceDate());
			bean.setStoreResourcesSpaceId(paceId);
			bean.setDisplay(true);
			bean.setSortNum(10);
			beans.add(bean);
		}
		super.saveAll(beans);
	}

	private String doUpload(File file, CmsSite site) throws GlobalException {
		if (site == null) {
			site = SystemContextUtils.getSite(RequestUtils.getHttpServletRequest());
		}
		String fileUrl = null;
		String siteUploadPath = WebConstants.UPLOAD_PATH + site.getPath();
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(file);
		} catch (IOException e1) {
			throw new GlobalException(new UploadExceptionInfo());
		}
		String origName = file.getName();
		String ext = FilenameUtils.getExtension(origName).toLowerCase(Locale.ENGLISH);
		try {
			/*** 如果选择了优先FTP并且设置了FTP,则使用FTP上传; 否则如果设置了OSS,则使用OSS; 都没设置则使用本地上传 */
			if (UploadServerType.ftp.equals(site.getUploadServerType()) && site.getUploadFtp() != null) {
				UploadFtp ftp = site.getUploadFtp();
				String ftpUrl = ftp.getUrl();
				fileUrl = ftp.storeByExt(siteUploadPath, ext, inputStream);
				// 加上url前缀
				fileUrl = ftpUrl + fileUrl;
			} else if (UploadServerType.oss.equals(site.getUploadServerType())
					&& site.getUploadOss() != null) {
				UploadOss oss = site.getUploadOss();
				fileUrl = oss.storeByExt(siteUploadPath, ext, inputStream);
			} else {
				fileUrl = fileRepository.storeByExt(siteUploadPath, ext, file);
				String ctx = site.getContextPath();
				if (StringUtils.isNotBlank(ctx)) {
					fileUrl = ctx + fileUrl;
				}
			}
		} catch (Exception e) {
			throw new GlobalException(new UploadExceptionInfo());
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}
		return fileUrl;
	}

	@Override
	public List<ResourcesSpaceData> deleteFile(Integer[] ids) throws GlobalException {
		List<ResourcesSpaceData> dataList = super.physicalDelete(ids);
		try {
			for (ResourcesSpaceData spaceData : dataList) {
				UploadFtp ftp = spaceData.getUploadFtp();
				UploadOss oss = spaceData.getUploadOss();
				if (ftp != null) {
					String ftpUrl = ftp.getUrl();
					String fileUrl = spaceData.getUrl().substring(ftpUrl.length());
					ftp.deleteFile(fileUrl);
				} else if (oss != null) {
					String fileUrl = spaceData.getUrl().substring(oss.getAccessDomain().length());
					oss.deleteFile(fileUrl);
				} else {
					File file = new File(realPathResolver.get(spaceData.getUrl()));
					file.delete();
				}
			}
		}catch (Exception e) {
			log.info("忽略文件删除的错误");
		}
		return dataList;
	}

	private String changeAlias(String alias, List<String> list, int i) {
		String newAlias = i != 0 ? alias + "(" + (i) + ")" : alias;

		// 如果文件别名不可用
		if (list.contains(newAlias)) {
			newAlias = changeAlias(alias, list, i + 1);
		}
		return newAlias;
	}

	@Override
	public String filename(String alias, Integer id, Integer userId, int i, Integer storeResourcesSpaceId) {
		if (StringUtils.isBlank(alias)) {
			// 文件别名空白抛出异常, 不然会导致递归死循环
			throw new IllegalArgumentException("文件别名不可为空白字符");
		}
		/**扩展名截取放最后截取拼接*/
		String ext= "";
		if(alias.contains(".")){
			int extIndex = alias.lastIndexOf(".");
			ext = alias.substring(extIndex);
			alias = alias.substring(0,extIndex);
		}
		String newAlias = i != 0 ? alias + "(" + (i) + ")" : alias;
		// 如果文件别名不可用
		if (!checkByAlias(newAlias, id, userId, storeResourcesSpaceId)) {
			/**查询重名数量*/
			Long count = dao.countByAlias(alias+"(",userId,storeResourcesSpaceId);
			Integer countInt = count.intValue() +1;
			newAlias = filename(newAlias, id, userId, countInt, storeResourcesSpaceId);
		}
		newAlias = newAlias+ext;
		return newAlias;
	}

	@Override
	public Long countByAliasPrefix(String alias, Integer userId, Integer storeResourcesSpaceId) {
		return null;
	}

	/**
	 * 判断文件别名是否可被使用 如果文件别名为空白, 该文件别名不可用 如果相同的所属用户下文件别名已存在, 该文件别名不可用(该别名已有主) 否则该文件别名可用(该别名野生) 如果相同的所属用户下文件别名已存在,
	 * 但是该条资源的id和传入的资源id相同, 该文件别名依然可用(该别名本来就属于该id)
	 *
	 * @param alias                 文件别名
	 * @param id                    资源id
	 * @param userId                用户id
	 * @param storeResourcesSpaceId 资源空间id
	 * @return boolean 如果文件别名可用返回true, 否则返回false
	 */
	@Override
	public boolean checkByAlias(String alias, Integer id, Integer userId, Integer storeResourcesSpaceId) {
		if (StringUtils.isBlank(alias)) {
			return false;
		}

		ResourcesSpaceData spaceData = dao.findByAlias(alias, userId, storeResourcesSpaceId);

		return spaceData == null || Objects.equals(id, spaceData.getId());

	}

	@Override
	public File getFileWithFilename(ResourcesSpaceData data, String filename) {
		InputStream input = null;
		CloseableHttpResponse httpResponse = null;
		HttpEntity responseEntity;
		String url = data.getUrl();
		String suffix = url.substring(url.lastIndexOf("."));
		File file = null;
		try {
			/** 生成文件夹随机，但是文件名是别名的文件 */
			String path = System.getProperty("java.io.tmpdir") + "/" + System.currentTimeMillis();
//			String fileName = String.valueOf(System.currentTimeMillis());
//			file = new File(path, fileName + suffix);
			/**别名有可能重名*/
			String fileName = data.getAlias();
			if(fileName.lastIndexOf(".")==-1){
				fileName = fileName + suffix;
			}
			/**用户指定了为准，否则则直接从别名中拿*/
			if(StringUtils.isNotBlank(filename)){
				fileName = filename;
			}
			file = new File(path, fileName);
			if (data.isLocalRes()) {
				FileUtils.copyFile(new File(realPathResolver.get(data.getUrl())), file);
			} else {
				// 获得Http客户端
				CloseableHttpClient httpClient = HttpClientBuilder.create().build();
				// 创建Get请求
				HttpGet httpGet = new HttpGet(data.getUrl());
				// 响应模型
				// 由客户端执行(发送)Get请求
				httpResponse = httpClient.execute(httpGet);
				// 从响应模型中获取响应实体
				responseEntity = httpResponse.getEntity();
				if (responseEntity != null && HttpServletResponse.SC_OK == httpResponse.getStatusLine()
						.getStatusCode()) {
					input = responseEntity.getContent();
					FileUtils.copyInputStreamToFile(input, file);
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		} finally {
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}
		return file;
	}

	@Override
	public File getFile(ResourcesSpaceData data, String path) {
		InputStream input = null;
		CloseableHttpResponse httpResponse = null;
		HttpEntity responseEntity;
		String url = data.getUrl();
		String suffix = url.substring(url.lastIndexOf("."));
		File file = null;
		try {
			String fileName = data.getAlias();
			file = new File(path, fileName + suffix);
			if (data.isLocalRes()) {
				FileUtils.copyFile(new File(realPathResolver.get(data.getUrl())), file);
			} else {
				// 获得Http客户端
				CloseableHttpClient httpClient = HttpClientBuilder.create().build();
				// 创建Get请求
				HttpGet httpGet = new HttpGet(data.getUrl());
				// 响应模型
				// 由客户端执行(发送)Get请求
				httpResponse = httpClient.execute(httpGet);
				// 从响应模型中获取响应实体
				responseEntity = httpResponse.getEntity();
				if (responseEntity != null && HttpServletResponse.SC_OK == httpResponse.getStatusLine()
						.getStatusCode()) {
					input = responseEntity.getContent();
					FileUtils.copyInputStreamToFile(input, file);
				}
			}
		} catch (IOException e) {
			log.error(e.getMessage());
		} finally {
			if (httpResponse != null) {
				try {
					httpResponse.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					log.error(e.getMessage());
				}
			}
		}
		return file;
	}

	@Override
	public List<ResourcesSpaceData> findBySysPic(Integer querySence) {
		return dao.findBySysPic(querySence);
	}

	@Autowired
	private FileRepository fileRepository;

}
