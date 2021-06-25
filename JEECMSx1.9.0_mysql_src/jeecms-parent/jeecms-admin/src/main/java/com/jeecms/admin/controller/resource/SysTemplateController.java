/*
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.admin.controller.resource;

import com.jeecms.channel.domain.Channel;
import com.jeecms.channel.domain.ChannelContentTpl;
import com.jeecms.channel.service.ChannelService;
import com.jeecms.common.annotation.EncryptMethod;
import com.jeecms.common.annotation.EncryptParam;
import com.jeecms.common.constants.TplConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.exception.error.SysOtherErrorCodeEnum;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.FileUtils;
import com.jeecms.common.util.Zipper;
import com.jeecms.common.web.util.CookieUtils;
import com.jeecms.content.domain.CmsModelTpl;
import com.jeecms.content.domain.Content;
import com.jeecms.content.service.CmsModelTplService;
import com.jeecms.content.service.ContentService;
import com.jeecms.resource.domain.dto.*;
import com.jeecms.resource.domain.vo.TplVo;
import com.jeecms.resource.service.Tpl;
import com.jeecms.resource.service.TplResourceService;
import com.jeecms.resource.service.TplService;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.system.service.CmsSiteService;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.jeecms.common.constants.WebConstants.SPT;
import static com.jeecms.system.service.impl.CmsSiteServiceImpl.SITE_COOKIE;

/**
 * 模板管理- 提供给后端服务调用
 *
 * @author xiaohui
 * @version 1.0
 * @date 2019/5/6
 */
@RequestMapping("/template")
@RestController
public class SysTemplateController extends AbstractTplAndResourceController {

	private static final Logger log = LoggerFactory.getLogger(SysTemplateController.class);

	/**
	 * 模板树API
	 */
	@SuppressWarnings("unchecked")
	@GetMapping(value = "/tree")
	@SerializeField(clazz = Tpl.class, includes = {"lastModifiedDate", "filename", "size", "name", "root",
		"directory", "children"})
	public ResponseInfo tree(HttpServletRequest request) throws GlobalException {
		CmsSite site = SystemContextUtils.getSite(request);
		String root = site.getTplPath();
		return new ResponseInfo(tplService.getChild(root, site));
	}

	/**
	 * 模板列表
	 *
	 * @param root    路径
	 * @param request request
	 * @return ResponseInfo
	 */
	@SuppressWarnings("unchecked")
	@EncryptMethod
	@GetMapping(value = "/list")
	@MoreSerializeField({@SerializeField(clazz = TplVo.class, includes = {"lastModifiedDate", "filename",
		"size", "name", "root", "directory", "sizeUnit", "channels", "contents", "quote"}),
		@SerializeField(clazz = Channel.class, includes = {"id", "name"}),
		@SerializeField(clazz = Content.class, includes = {"id", "title"})})
	public ResponseInfo list(@EncryptParam String root, HttpServletRequest request) throws GlobalException {
		CmsSite site = SystemContextUtils.getSite(request);
		//模板路径
		String tplPath = site.getTplPath();
		if (StringUtils.isBlank(root)) {
			root = tplPath;
		} else {
			FileUtils.isValidFilename(tplPath, root);
		}
		List<TplVo> vos = new ArrayList<>();
		List<? extends Tpl> list = tplService.getChild(root, site);
		if (list == null || list.isEmpty()) {
			return new ResponseInfo(vos);
		}
		String path = root.replace(tplPath + SPT, "");
		//方案名
		String solution;
		int num = path.indexOf(SPT);
		if (num != -1) {
			solution = path.substring(0, num);
		} else {
			solution = path;
		}
		//查询路径下所有模版关联的模型
		Integer siteId = site.getId();
		List<CmsModelTpl> modelTpls = modelTplService.models(siteId, null, solution);
		List<Channel> channelList = channelService.findList(siteId, true);
		for (Tpl tpl : list) {
			TplVo vo = new TplVo(tpl.getLastModifiedDate(), tpl.getFilename(), tpl.getSize(), tpl.getName(),
				tpl.getRoot(), tpl.isDirectory(), tpl.getSizeUnit());
			//是文件进行处理
			if (!tpl.isDirectory()) {
				boolean isPcSolution = solution.equals(site.getPcSolution());
				boolean isMobileSolution = solution.equals(site.getMobileSolution());
				if (StringUtils.isNotBlank(solution)) {
					String modelPath = tpl.getName().replace(tplPath + SPT + solution + SPT, "");
					Map<String, List<CmsModelTpl>> modelTplList = modelTpls.parallelStream()
						.filter(o -> o.getTplPath().equals(modelPath))
						.filter(o -> o.getSiteId().equals(siteId))
						.collect(Collectors.groupingBy(CmsModelTpl::getTplPath));
					List<Channel> channels = new ArrayList<>();
					boolean isQuoteContent = false;
					if (isPcSolution) {
						for (String key : modelTplList.keySet()) {
							for (Channel channel : channelList) {
								channels.addAll(channelList.parallelStream().filter(o -> StringUtils.isNotBlank(o.getTplPc())).filter(o -> o.getTplPc().equals(key)).collect(Collectors.toList()));
								List<ChannelContentTpl> contentTpls = channel.getContentTpls();
								if (contentTpls != null && !contentTpls.isEmpty()) {
									List<ChannelContentTpl> collect = contentTpls.parallelStream()
										.filter(ChannelContentTpl::getSelect)
										.filter(o -> key.equals(o.getTplPc()))
										.collect(Collectors.toList());
									if (!collect.isEmpty()) {
										isQuoteContent = true;
									}
								}
							}
						}
					} else if (isMobileSolution) {
						for (String key : modelTplList.keySet()) {
							for (Channel channel : channelList) {
								channels.addAll(channelList.parallelStream().filter(o -> StringUtils.isNotBlank(o.getTplMobile())).filter(o -> o.getTplMobile().equals(key)).collect(Collectors.toList()));
								List<ChannelContentTpl> contentTpls = channel.getContentTpls();
								if (contentTpls != null && !contentTpls.isEmpty()) {
									List<ChannelContentTpl> collect = null;
									collect = contentTpls.parallelStream()
										.filter(ChannelContentTpl::getSelect)
										.filter(o -> key.equals(o.getTplMobile()))
										.collect(Collectors.toList());
									if (!collect.isEmpty()) {
										isQuoteContent = true;
									}
								}
							}
						}
					}
					if (isPcSolution && !isQuoteContent) {
						long count = contentService.countByTpl(siteId,modelPath,null);
						if (count > 0) {
							isQuoteContent = true;
						}
					} else if (isMobileSolution && !isQuoteContent) {
						long count = contentService.countByTpl(siteId,null ,modelPath);
						if (count > 0) {
							isQuoteContent = true;
						}
					}
					if (!channels.isEmpty()) {
						//被栏目引用
						vo.setQuote(1);
						vo.setChannels(channels.parallelStream().distinct().filter(o -> o.getSiteId().equals(siteId)).collect(Collectors.toList()));
					} else if (isQuoteContent) {
						//被内容引用
						vo.setQuote(2);
					} else {
						if (isPcSolution) {
							String pcTpl = site.getPcHomePageTemplates();
							if (pcTpl.equalsIgnoreCase(modelPath)) {
								//被首页引用
								vo.setQuote(3);
							}
						} else if (isMobileSolution) {
							String mobileTpl = site.getMobileHomePageTemplates();
							if (mobileTpl.equalsIgnoreCase(modelPath)) {
								//被首页引用
								vo.setQuote(3);
							}
						}
					}
				}
			}
			vos.add(vo);
		}
		return new ResponseInfo(vos);
	}

	/**
	 * 获取文内容
	 *
	 * @param name    文件名
	 * @param request HttpServletRequest
	 * @return ResponseInfo
	 * @throws GlobalException 异常
	 */
	@GetMapping()
	@EncryptMethod
	@SerializeField(clazz = Tpl.class, includes = {"filename", "root", "source", "sizeUnit", "models"})
	public ResponseInfo get(@EncryptParam String name, HttpServletRequest request) throws GlobalException {
		CmsSite site = SystemContextUtils.getSite(request);
		validBlank(name);
		validDelete(name, site.getTplPath(), site);
		Tpl file = tplService.get(name, site);
		return new ResponseInfo(file);
	}

	/**
	 * 添加模板
	 *
	 * @param dto     路径
	 * @param request HttpServletRequest
	 * @param result  BindingResult
	 * @return ResponseInfo
	 * @throws GlobalException 异常
	 */
	@PostMapping()
	@EncryptMethod
	public ResponseInfo save(@Valid @RequestBody TplReSourceDto dto, HttpServletRequest request,
							 BindingResult result) throws GlobalException {
		validateBindingResult(result);
		CmsSite site = SystemContextUtils.getSite(request);
		String root = dto.getRoot();
		if (StringUtils.isBlank(root)) {
			root = site.getTplPath();
		}
		String name = root + "/" + dto.getFilename() + TplConstants.TPL_SUFFIX;
		validSave(name, site.getTplPath(), site);
		dto.setRoot(root);
		tplService.save(dto, site);
		return new ResponseInfo();
	}

	/**
	 * 修改模板
	 *
	 * @param dto     模板资源Dto
	 * @param request HttpServletRequest
	 * @param result  BindingResult
	 * @return ResponseInfo
	 * @throws GlobalException 异常
	 */
	@PutMapping()
	@EncryptMethod
	public ResponseInfo update(@Valid @RequestBody TplUpdateDto dto, HttpServletRequest request,
							   BindingResult result) throws GlobalException {
		validateBindingResult(result);
		CmsSite site = SystemContextUtils.getSite(request);
		String oldName = dto.getRoot();
		String root = oldName.substring(0, dto.getRoot().lastIndexOf("/"));
		String newName = root + "/" + dto.getFilename() + TplConstants.TPL_SUFFIX;
		if (newName.equalsIgnoreCase(oldName)) {
			//新名称和旧名称一样，只校验原文件是否存在
			validDelete(oldName, site.getTplPath(), site);
		} else {
			//新名称和旧名称不一样，校验原文件是否存在及新文件是否存在
			newName = root + "/" + dto.getFilename() + TplConstants.TPL_SUFFIX;
			validReName(oldName, newName, site.getTplPath(), site);
		}
		dto.setFilename(newName);
		tplService.update(dto, site);
		return new ResponseInfo();
	}

	/**
	 * 删除文件
	 *
	 * @param nameDto 名称
	 * @param request HttpServletRequest
	 * @param result  BindingResult
	 * @return ResponseInfo
	 * @throws GlobalException 异常
	 */
    @PostMapping("/delete")
	@EncryptMethod
	public ResponseInfo delete(@Valid @RequestBody TplDeleteDto nameDto, HttpServletRequest request,
							   BindingResult result) throws GlobalException {
		validateBindingResult(result);
		CmsSite site = SystemContextUtils.getSite(request);
		// 验证名字是否合法路径，文件是否存在
		for (String n : nameDto.getNames()) {
			validDelete(n, site.getTplPath(), site);
		}
		tplService.delete(nameDto.getNames(), site);
		return new ResponseInfo();
	}

	/**
	 * 修改模板名称
	 *
	 * @param dto     模板、资源重命名Dto
	 * @param request HttpServletRequest
	 * @param result  BindingResult
	 * @return ResponseInfo
	 * @throws GlobalException 异常
	 */
	@PutMapping("/rename")
	@EncryptMethod
	public ResponseInfo rename(@Valid @RequestBody TplRenameDto dto, HttpServletRequest request,
							   BindingResult result) throws GlobalException {
		validateBindingResult(result);
		CmsSite site = SystemContextUtils.getSite(request);
		validReName(dto.getFileName(), dto.getNewName(), site.getTplPath(), site);
		tplService.rename(dto.getFileName(), dto.getNewName(), site);
		return new ResponseInfo();
	}

	/**
	 * 创建文件夹
	 *
	 * @param dto     创建文件夹Dto
	 * @param request HttpServletRequest
	 * @param result  BindingResult
	 * @return ResponseInfo
	 * @throws GlobalException 异常
	 */
	@PostMapping(value = "/dir")
	@EncryptMethod
	public ResponseInfo saveDir(@Valid @RequestBody TplSaveDirDto dto, HttpServletRequest request,
								BindingResult result) throws GlobalException {
		validateBindingResult(result);
		CmsSite site = SystemContextUtils.getSite(request);
		String root = dto.getRoot();
		if (StringUtils.isBlank(root)) {
			root = site.getTplPath();
		}
		dto.setRoot(root);
		validSave(root + "/" + dto.getDirName(), root, site);
		tplService.saveDir(dto, site);
		return new ResponseInfo();
	}

	/**
	 * 校验目录是否唯一
	 *
	 * @param dirName 目录名称
	 * @param root    目录路径
	 * @return true 唯一 false 不唯一
	 * @throws GlobalException 异常
	 */
	@GetMapping(value = "/unique/dirName")
	@EncryptMethod
	public ResponseInfo checkByDirName(String dirName, @EncryptParam String root,
									   HttpServletRequest request) throws GlobalException {
		CmsSite site = SystemContextUtils.getSite(request);
		if (root == null) {
			root = site.getTplPath();
		}
		return new ResponseInfo(!vldExistFile(root + SPT + dirName, site));
	}

	/**
	 * 校验文件是否唯一
	 *
	 * @param filename 文件名称
	 * @param root     原文件地址
	 * @return true 唯一 false 不唯一
	 * @throws GlobalException 异常
	 */
	@GetMapping(value = "/unique/filename")
	public ResponseInfo checkByFilename(@NotBlank @RequestParam String filename, @NotBlank @RequestParam String root,
										HttpServletRequest request) throws GlobalException {
		CmsSite site = SystemContextUtils.getSite(request);
		int num = root.lastIndexOf(SPT);
		if (root.substring(num).equalsIgnoreCase(filename)) {
			return new ResponseInfo(true);
		} else {
			root = root.substring(0, num);
		}
		return new ResponseInfo(!vldExistFile(root + SPT + filename, site));
	}

	/**
	 * 校验方案名是否唯一
	 *
	 * @param name    方案名称
	 * @param request {@link HttpServletRequest}
	 * @return true 唯一 false 不唯一
	 * @throws GlobalException 异常
	 */
	@GetMapping(value = "/unique/name")
	@EncryptMethod
	public ResponseInfo checkByName(@EncryptParam String name, HttpServletRequest request) throws GlobalException {
		CmsSite site = SystemContextUtils.getSite(request);
		String root = site.getTplPath();
		return new ResponseInfo(!vldExistFile(root + SPT + name, site));
	}

	/**
	 * 导入模板（zip）
	 *
	 * @param file    文件
	 * @param request HttpServletRequest
	 * @return ResponseInfo
	 * @throws GlobalException 异常
	 */
	@PostMapping(value = "/import")
	public ResponseInfo tplImport(@RequestParam(value = "uploadFile", required = false) MultipartFile file,
								  Boolean isCover, HttpServletRequest request) throws GlobalException {
		validateFile(file);
		CmsSite site = SystemContextUtils.getSite(request);
		String filename = file.getOriginalFilename();
		filename = filename.substring(filename.lastIndexOf(".") + 1);
		if (!FileUtils.isValidZipExt(filename)) {
			return new ResponseInfo(SysOtherErrorCodeEnum.UPLOAD_FORMAT_ERROR.getCode(),
				SysOtherErrorCodeEnum.UPLOAD_FORMAT_ERROR.getDefaultMessage());
		}
		try {
			File tempFile = File.createTempFile(file.getName(), "temp");
			file.transferTo(tempFile);
			resourceService.imoport(tempFile, site, isCover);
			tempFile.delete();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
			return new ResponseInfo(SysOtherErrorCodeEnum.IMPORT_TEMPLATE_ERROR.getCode(),
				SysOtherErrorCodeEnum.IMPORT_TEMPLATE_ERROR.getDefaultMessage());
		}
		return new ResponseInfo();
	}

	/**
	 * 导出模板
	 *
	 * @param solution      模板方案
	 * @param request  HttpServletRequest
	 * @param response HttpServletResponse
	 * @throws GlobalException 异常
	 */
	@GetMapping(value = "/export")
	public void export(String solution, HttpServletRequest request,
					   HttpServletResponse response) throws GlobalException {
		validBlank(solution);
		CmsSite site = siteService.getByCookie(request);
        CookieUtils.addCookie(request, response, SITE_COOKIE, site.getId().toString(), null, null, null);
		try {
			List<Zipper.FileEntry> fileEntrys = resourceService.export(site, solution);
			response.setContentType("application/x-download;charset=UTF-8");
			response.addHeader("Content-disposition", "filename=template-" + solution + ".zip");
			// 模板一般都在windows下编辑，所以默认编码为GBK
			Zipper.zip(response.getOutputStream(), fileEntrys, "GBK");
		} catch (IOException e) {
			log.error("export template error!", e);
		}
	}

	/**
	 * 上传模板
	 *
	 * @param root    路径
	 * @param file    模板文件
	 * @param isCover true 替换 false 保留
	 * @param request HttpServletRequest
	 * @return ResponseInfo
	 * @throws GlobalException 异常
	 */
	@PostMapping("/upload")
	@EncryptMethod
	public ResponseInfo upload(@RequestParam(value = "uploadFile", required = false) MultipartFile file,
							  @EncryptParam String root, Boolean isCover, HttpServletRequest request) throws GlobalException {
		validBlank(file);
		CmsSite site = SystemContextUtils.getSite(request);
		String filename = file.getOriginalFilename();
		filename = filename.substring(filename.lastIndexOf("."));
		//如果不是html文件则不让上传
		if (!TplConstants.TPL_SUFFIX.equalsIgnoreCase(filename)) {
			return new ResponseInfo(SysOtherErrorCodeEnum.UPLOAD_FORMAT_ERROR.getCode(),
				SysOtherErrorCodeEnum.UPLOAD_FORMAT_ERROR.getDefaultMessage());
		}
		try {
			//判断该文件是否允许上传
			if (!FileUtils.checkFileIsValid(file.getInputStream())) {
				return new ResponseInfo(SysOtherErrorCodeEnum.UPLOAD_FORMAT_ERROR.getCode(),
					SysOtherErrorCodeEnum.UPLOAD_FORMAT_ERROR.getDefaultMessage());
			}
		} catch (IOException e) {
			log.error(e.getMessage());
			return new ResponseInfo(SysOtherErrorCodeEnum.UPLOAD_IO_ERROR.getCode(),
				SysOtherErrorCodeEnum.UPLOAD_IO_ERROR.getDefaultMessage());
		}
		if (StringUtils.isBlank(root)) {
			root = site.getTplPath();
		} else {
			FileUtils.isValidFilename(site.getTplPath(), root);
		}
		validateFile(file);
		tplService.save(root, file, site, isCover);
		return new ResponseInfo();
	}

	@Autowired
	private TplService tplService;
	@Autowired
	private TplResourceService resourceService;
	@Autowired
	private CmsModelTplService modelTplService;
	@Autowired
	private ChannelService channelService;
	@Autowired
	private ContentService contentService;
	@Autowired
	private CmsSiteService siteService;
}
