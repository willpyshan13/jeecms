/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.front.controller;

import com.jeecms.auth.domain.CoreUser;
import com.jeecms.common.base.domain.DeleteDto;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.ContentErrorCodeEnum;
import com.jeecms.common.util.StrUtils;
import com.jeecms.common.util.Zipper;
import com.jeecms.common.web.springmvc.MessageResolver;
import com.jeecms.common.web.springmvc.RealPathResolver;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.ContentAttrRes;
import com.jeecms.content.service.ContentAttrResService;
import com.jeecms.content.service.ContentFrontService;
import com.jeecms.content.service.ContentService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.resource.service.ResourcesSpaceDataService;
import com.jeecms.system.domain.SysSecret;
import com.jeecms.system.domain.SysUserSecret;
import com.jeecms.system.service.GlobalConfigService;
import com.jeecms.util.FrontUtils;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.Min;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 前台下载控制器
 * 
 * @author: ljw
 * @date: 2019年6月14日 上午9:51:27
 */
@RequestMapping(value = "/download")
@Validated
@Controller
public class DownloadController {

	private static final Logger log = LoggerFactory.getLogger(DownloadController.class);

	@Autowired
	private ContentService contentService;
	@Autowired
	private ResourcesSpaceDataService service;
	@Autowired
	private ContentAttrResService resService;
	@Autowired
	private RealPathResolver realPathResolver;
	@Autowired
	private GlobalConfigService globalConfigService;
	@Autowired
	private ContentFrontService contentFrontService;

	/**
	 * 下载附件文件
	 *
	 * @param dto
	 *            资源id数组
	 * @param response
	 *            HttpServletResponse
	 * @return ResponseInfo
	 */
	@GetMapping(value = "/o_accessory")
	public void download(@RequestBody DeleteDto dto, ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		// 可下载资源
		List<Integer> sourceIds = new ArrayList<Integer>(10);
		Integer[] ids = dto.getIds();
		boolean hasError = false;
		try {
			// 判断是否开启附件下载
			Boolean flag = globalConfigService.get().getConfigAttr().getOpenAttachmentSecurity();
			if (flag) {
				// 得到当前登录的人
				CoreUser user = SystemContextUtils.getCoreUser();
				for (Integer integer : ids) {
					// 得到资源的密级ID
					List<Integer> secrets = resService.getSecretByRes(integer);
					// 判断当前密级，与用户拥有的密级做比较
					if (!secrets.isEmpty()) {
						SysUserSecret userSecret = user.getUserSecret();
						if (userSecret != null && !userSecret.getSysSecrets().isEmpty()) {
							// 过滤附件密级，得到用户拥有的密级
							List<Integer> secrets2 = userSecret.getSysSecrets().stream()
									.filter(x -> x.getSecretType().equals(SysSecret.ANNEX_SECRET)).map(SysSecret::getId)
									.collect(Collectors.toList());
							if (secrets2.isEmpty()) {
								try {
									hasError = true;
									response.sendRedirect("../"+WebConstants.FRONT_NO_PERM);
								}catch (IOException e){
									e.printStackTrace();
								}
							}
							// 如果用户拥有的密级包含资源的密级
							if (secrets2.containsAll(secrets)) {
								sourceIds.add(integer);
							}
						} else {
							try {
								hasError = true;
								response.sendRedirect("../"+ WebConstants.FRONT_NO_PERM);
							}catch (IOException e){
								e.printStackTrace();
							}
						}
					}
				}
				if (sourceIds.isEmpty()) {
					try {
						hasError = true;
						response.sendRedirect("../"+WebConstants.FRONT_NO_PERM);
					}catch (IOException e){
						e.printStackTrace();
					}
				}
			}

		} catch (GlobalException e) {
			try {
				hasError = true;
				response.sendRedirect("error/fileNotExist");
			}catch (IOException ex){
				ex.printStackTrace();
			}
		}
		if(!hasError){
			downAttachment(sourceIds, model, request, response);
		}
	}

	@GetMapping(value = "/byContent")
	public void downloadByContentId(Integer contentId, ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		// 可下载资源
		List<Integer> sourceIds = new ArrayList<>(10);
		boolean hasError = false;
		if (contentId != null) {
			Content c = contentService.findById(contentId);
			try {
				// 判断是否开启附件下载
				Boolean flag = globalConfigService.get().getConfigAttr().getOpenAttachmentSecurity();
				Map<Integer,Integer> atts= c.getAttachmentResAttrMap();
				if (flag) {
					if(atts!=null){
						//List<Integer> resIds = ContentAttr.fetchIds(atts);
						sourceIds = getDownloadResIds(atts);
					}
				}else{
					sourceIds.addAll(atts.keySet());
				}
			} catch (GlobalException e) {
				try {
					hasError = true;
					response.sendRedirect("error/fileNotExist");
				}catch (IOException ex){
					ex.printStackTrace();
				}
			}
		}
		if(!hasError){
			downAttachment(sourceIds, model, request, response);
			try {
				contentFrontService.saveOrUpdateNum(contentId, null, ContentConstant.CONTENT_NUM_TYPE_DOWNLOADS, false);
			} catch (GlobalException e) {
				log.error("统计下载失败");
			}
		}
	}

	@GetMapping(value = "/byField")
	public void downloadByContentIdAndField(@Min(value = 1 ,message = "参数错误")Integer contentId, String field, ModelMap model,
											HttpServletRequest request, HttpServletResponse response) {
		// 可下载资源
		List<Integer> sourceIds = new ArrayList<>(10);
		boolean hasError = false;
		if (contentId != null) {
			Content c = contentService.findById(contentId);
			try {
				// 判断是否开启附件下载
				Boolean flag = globalConfigService.get().getConfigAttr().getOpenAttachmentSecurity();
				if(c != null){
					Map<Integer,Integer> atts= c.getAttachmentResAttrMapByField(field);
					if (flag) {
						if(atts!=null){
							//List<Integer> resIds = ContentAttr.fetchIds(atts);
							sourceIds = getDownloadResIds(atts);
						}
					}else{
						sourceIds.addAll(atts.keySet());
					}
				}
				if (sourceIds.isEmpty()) {
					try {
						hasError = true;
						response.sendRedirect("../"+WebConstants.FRONT_NO_PERM);
					}catch (IOException e){
						e.printStackTrace();
					}
				}
			} catch (GlobalException e) {
				try {
					hasError = true;
					response.sendRedirect("error/fileNotExist");
				}catch (IOException ex){
					ex.printStackTrace();
				}
			}
			if(!hasError){
				downAttachment(sourceIds, model, request, response);
				try {
					contentFrontService.saveOrUpdateNum(contentId, null, ContentConstant.CONTENT_NUM_TYPE_DOWNLOADS, false);
				} catch (GlobalException e) {
					log.error("统计下载失败");
				}
			}
		}
	}


	@GetMapping(value = "/error/permission")
	public String downloadError( ModelMap model, HttpServletRequest request,
								HttpServletResponse response) {
		model.put("msg",MessageResolver.getMessage(ContentErrorCodeEnum.CONTENT_DOWNLOAD_NOT_PERMISSIONS.getCode()));
		return FrontUtils.systemError(request, response, model);
	}

	@GetMapping(value = "/error/fileNotExist")
	public String downloadFileMission( ModelMap model, HttpServletRequest request,
								 HttpServletResponse response) {
		model.put("msg",MessageResolver.getMessage(ContentErrorCodeEnum.CONTENT_DOWNLOAD_FILE_EXIST.getCode()));
		return FrontUtils.systemError(request, response, model);
	}

	private List<Integer> getDownloadResIds(Map<Integer,Integer>toDownResMap) throws GlobalException {
		CoreUser user = SystemContextUtils.getCoreUser();
		List<Integer> canDownResIds = new ArrayList<>();
		Set<Integer> toDownResIds = toDownResMap.keySet();
		for (Integer id : toDownResIds) {
			// 得到资源的密级ID
			ContentAttrRes res = resService.getSecretByResWithAttrId(id,toDownResMap.get(id));
			if(res==null){
				/** 附件未设置密级 */
				canDownResIds.add(id);
			}else{
				Integer secret = res.getSecretId();
				// 判断当前密级，与用户拥有的密级做比较
				if (secret!=null&&user != null) {
						SysUserSecret userSecret = user.getUserSecret();
						if (userSecret != null && !userSecret.getSysSecrets().isEmpty()) {
							// 过滤附件密级，得到用户拥有的密级
							List<Integer> userResSecrets = userSecret.getSysSecrets().stream()
									.filter(x -> x.getSecretType().equals(SysSecret.ANNEX_SECRET)).map(SysSecret::getId)
									.collect(Collectors.toList());
							// 如果用户拥有的密级包含资源的密级
							if (userResSecrets != null && userResSecrets.contains(secret)) {
								canDownResIds.add(id);
							}
						}
				}
			}
		}
		return canDownResIds;
	}

	private void downAttachment(List<Integer> ids, ModelMap model, HttpServletRequest request,
			HttpServletResponse response) {
		// 出现下载的对话框
		response.setContentType("application/x-download;charset=UTF-8");
		InputStream input = null;
		OutputStream output = null;
		boolean succDownload = false;
        File file = null;
        List<Zipper.FileEntry> fileEntries = new ArrayList<>();
		try {
			if (ids.size() == 1) {
				ResourcesSpaceData spaceData = service.findById(ids.get(0));
				if(spaceData!=null){
					String filename = spaceData.getAlias()+spaceData.getSuffix();
					RequestUtils.setDownloadHeader(response, new String(filename.getBytes(), "ISO8859-1"));
                    file = service.getFileWithFilename(spaceData, null);
					if(file.exists()){
						input = new FileInputStream(file);
						output = response.getOutputStream();
						byte[] buff = new byte[1024];
						int len = 0;
						while ((len = input.read(buff)) > -1) {
							output.write(buff, 0, len);
						}
						succDownload = true;
					}
				}
			} else {
				String filename = "";

				/**选取的资源可能存在重名的别名，此处需要处理*/
				Map<Integer,String>resAliasMap = new HashMap<>();
				for (Integer id : ids) {
					ResourcesSpaceData data = service.findById(id);
					String fileName = data.getAlias();
					String url = data.getUrl();
					String suffix = url.substring(url.lastIndexOf("."));
					if(fileName.lastIndexOf(".")==-1){
						fileName += suffix;
					}
					resAliasMap.put(data.getId(),fileName);
				}
				/***处理重名resAliasMap val*/
				Collection<String> resFilenames = new ArrayList<>();
				int i=1;
				for(Map.Entry<Integer,String>resAlia:resAliasMap.entrySet()){
					if(resFilenames.contains(resAlia.getValue())){
						resAliasMap.put(resAlia.getKey(),i+++resAlia.getValue());
					}
					resFilenames.add(resAlia.getValue());
				}
				for (Integer id : ids) {
					ResourcesSpaceData spaceData = service.findById(id);
					if(spaceData!=null){
						filename = spaceData.getAlias();
                        fileEntries.addAll(export(spaceData, resAliasMap.get(spaceData.getId())));
					}
				}
				if(StringUtils.isBlank(filename)){
					filename = System.currentTimeMillis()+".zip";
				}else{
					filename +=".zip";
				}
				RequestUtils.setDownloadHeader(response, filename);
				output = response.getOutputStream();
                if (fileEntries.size() > 0) {
					succDownload = true;
                    Zipper.zip(output, fileEntries, "GBK");
				}
			}

		} catch (IOException e) {
		} finally {
			if(!succDownload){
				try {
					response.sendRedirect("error/fileNotExist");
				}catch (IOException ex){
					ex.printStackTrace();
				}
			}
			if (output != null) {
				try {
					output.close();
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
            /**删除临时文件*/
            if (file != null && file.exists()) {
                file.delete();
            }
            if (!fileEntries.isEmpty()) {
                for (Zipper.FileEntry fileEntry : fileEntries) {
                    File f = fileEntry.getFile();
                    if (f.exists()) {
                        f.delete();
                    }
                }
            }
		}
	}

	/**
	 * 文库文档下载
	 * 
	 * @param contentId
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/o_doc_download")
	public void downloadLibrary(@Min(value = 1 ,message = "参数错误")String contentId, HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> model = new HashMap<String, Object>(1);
		if (contentId == null) {
			try {
				response.sendRedirect("error/fileNotExist");
			}catch (IOException ex){
				ex.printStackTrace();
			}
		}else{
			if(!StrUtils.isNumeric(contentId)){
				try {
					response.sendRedirect("error/fileNotExist");
				}catch (IOException ex){
					ex.printStackTrace();
				}
			}
			InputStream input = null;
			OutputStream output = null;
			try {
				Content content = contentService.findById(Integer.parseInt(contentId));
				if (content == null) {
					try {
						response.sendRedirect("error/fileNotExist");
					}catch (IOException ex){
						ex.printStackTrace();
					}
				}
				String pdfUrl = content.getContentExt().getDocResource().getUrl();
				if (StringUtils.isBlank(pdfUrl)) {
					try {
						response.sendRedirect("error/fileNotExist");
					}catch (IOException ex){
						ex.printStackTrace();
					}
				}
				String filename = pdfUrl.substring(pdfUrl.lastIndexOf("/"));
				// 出现下载的对话框
				response.setContentType("application/x-download;charset=UTF-8");
				RequestUtils.setDownloadHeader(response, filename);
				File file = new File(realPathResolver.get(pdfUrl));
				// 下载后的名称
				input = new FileInputStream(file);
				output = response.getOutputStream();
				byte[] buff = new byte[1024];
				int len = 0;
				while ((len = input.read(buff)) > -1) {
					output.write(buff, 0, len);
				}
			} catch (IOException e) {
				try {
					response.sendRedirect("error/fileNotExist");
				}catch (IOException ex){
					ex.printStackTrace();
				}
			} finally {
				if (output != null) {
					try {
						output.close();
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
			try {
				contentFrontService.saveOrUpdateNum(Integer.parseInt(contentId), null, ContentConstant.CONTENT_NUM_TYPE_DOWNLOADS, false);
			} catch (GlobalException e) {
				log.error("统计下载失败");
			}
		}
	}

	/**
	 * 将文件导入集合
	 * 
	 * @Title: export
	 *            资源地址
	 * @return
	 */
	private List<Zipper.FileEntry> export(ResourcesSpaceData spaceData,String filename) {
		List<Zipper.FileEntry> fileEntrys = new ArrayList<>();
		File file =service.getFileWithFilename(spaceData, filename);
		if(file.exists()){
			fileEntrys.add(new Zipper.FileEntry("", "", file));
		}
		return fileEntrys;
	}

}
