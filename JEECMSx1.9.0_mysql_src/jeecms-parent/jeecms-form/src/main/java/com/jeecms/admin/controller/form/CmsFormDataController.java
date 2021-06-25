/**
 * @Copyright:  江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.admin.controller.form;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.hutool.core.io.FileUtil;
import com.google.gson.internal.LinkedHashTreeMap;
import com.jeecms.auth.domain.CoreUser;
import com.jeecms.common.base.controller.BaseController;
import com.jeecms.common.base.domain.DeleteDto;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.jsonfilter.annotation.MoreSerializeField;
import com.jeecms.common.jsonfilter.annotation.SerializeField;
import com.jeecms.common.page.Paginable;
import com.jeecms.common.page.PaginableRequest;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.MyDateUtils;
import com.jeecms.common.util.Zipper;
import com.jeecms.common.web.util.RequestUtils;
import com.jeecms.constants.CmsFormConstant;
import com.jeecms.form.domain.CmsFormDataAttrEntity;
import com.jeecms.form.domain.CmsFormDataAttrResEntity;
import com.jeecms.form.domain.CmsFormDataEntity;
import com.jeecms.form.domain.dto.CmsFormDataReadDto;
import com.jeecms.form.domain.vo.CmsFormDataAttrImgVo;
import com.jeecms.form.domain.vo.CmsFormResVo;
import com.jeecms.form.service.CmsFormDataService;
import com.jeecms.interact.domain.CmsFormEntity;
import com.jeecms.interact.domain.CmsFormItemEntity;
import com.jeecms.interact.service.CmsFormService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.resource.service.ResourcesSpaceDataService;
import com.jeecms.system.domain.Area;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.util.*;

/**
 * 表单数据库控制器
 * @author: tom
 * @date: 2020/2/19 15:17
 */
@RestController
@RequestMapping("/smartFormData")
public class CmsFormDataController extends BaseController<CmsFormEntity, Integer> {

	private static final Logger log = LoggerFactory.getLogger(CmsFormDataController.class);

	@Autowired
	private CmsFormDataService service;
	@Autowired
	private CmsFormService formService;
	@Autowired
	private ResourcesSpaceDataService resourcesSpaceDataService;

	/**1048576 excel最大行数*/
	private Paginable paginable = new PaginableRequest(0,1048576, Sort.Direction.DESC,"id");

	@PostConstruct
	public void init() {
		String[] queryParams = {};
		super.setQueryParams(queryParams);
	}

	/**
	 * 查询表单数据列表
	 * @param formId 表单id
	 * @param isRead 是否已读
	 * @param proviceCode 省份
	 * @param cityCode 城市
	 * @param isPc 是否pc
	 * @param username 用户名
	 * @param ip ip
	 * @param createTimeMin 创建时间小值
	 * @param createTimeMax 创建时间大值
	 * @param pageable
	 * @return
	 */
	@MoreSerializeField({
			@SerializeField(clazz = CmsFormDataEntity.class, excludes = {"form","fileDatas","attachments","attrs","attr","attrLabelMap"}),
			@SerializeField(clazz = CoreUser.class, includes = {"id","username","realname"}),
			@SerializeField(clazz = Area.class, includes = {"id","areaCode","areaName"}),
			@SerializeField(clazz = CmsFormDataAttrEntity.class, excludes = {"data"}),
			@SerializeField(clazz = CmsFormDataAttrResEntity.class, excludes = {"attr"}),
			@SerializeField(clazz = ResourcesSpaceData.class, includes = {"id","resourceType","alias",
					"dimensions","size","url","resourceDate","width","height"}),
	})
	@GetMapping(value = "/getPage")
	public ResponseInfo getPage(Integer formId, Boolean isRead, String proviceCode, String cityCode, Boolean isPc, String username,
								String ip, Date createTimeMin, Date createTimeMax,Pageable pageable) {
		Page<CmsFormDataEntity> page = service.getPage(formId,isRead,proviceCode,cityCode,isPc,username,ip,
				null,null,null,createTimeMin,createTimeMax,pageable);
		return new ResponseInfo(page);
	}

	/**
	 * 获取详情
	 *
	 * @Title: 获取详情
	 * @param: @param id
	 * @param: @throws GlobalException
	 * @return: ResponseInfo
	 */
	@MoreSerializeField({
			@SerializeField(clazz = CmsFormDataEntity.class, excludes = {"form","fileDatas","attachments","attrs","attr","attrLabelMap"}),
			@SerializeField(clazz = CoreUser.class, includes = {"id","username","realname"}),
			@SerializeField(clazz = Area.class, includes = {"id","areaCode","areaName"}),
			@SerializeField(clazz = CmsFormDataAttrEntity.class, excludes = {"data"}),
			@SerializeField(clazz = CmsFormDataAttrResEntity.class, excludes = {"attr"}),
			@SerializeField(clazz = ResourcesSpaceData.class, includes = {"id","resourceType","alias",
					"dimensions","size","url","resourceDate","width","height"}),
	})
	@GetMapping(value = "/{id:[0-9]+}")
	public ResponseInfo get(@PathVariable("id") Integer id) {
		return new ResponseInfo(service.findById(id));
	}


	@PutMapping
	public ResponseInfo read(@RequestBody @Valid CmsFormDataReadDto dto,
							   BindingResult result) throws GlobalException {
		validateBindingResult(result);
		List<CmsFormDataEntity>datas = service.findAllById(dto.getIds());
		for(CmsFormDataEntity d:datas){
			d.setIsRead(dto.getRead());
		}
		service.batchUpdate(datas);
		return new ResponseInfo();
	}

	@PostMapping(value = "/export")
	public ResponseInfo postExport(Integer formId, Boolean isRead, String proviceCode, String cityCode, Boolean isPc, String username,
							   String ip, Date createTimeMin, Date createTimeMax,
							   HttpServletRequest request, HttpServletResponse response) throws GlobalException {
		return export(formId,isRead,proviceCode,cityCode,isPc,username,ip,createTimeMin,createTimeMax,request,response);
	}

	@GetMapping(value = "/export")
	public ResponseInfo export(Integer formId, Boolean isRead, String proviceCode, String cityCode, Boolean isPc, String username,
					   String ip, Date createTimeMin, Date createTimeMax,
					   HttpServletRequest request, HttpServletResponse response) throws GlobalException {
		if(formId!=null){
			CmsFormEntity form = formService.findById(formId);
			if(form!=null){
					List<CmsFormDataEntity> list = service.getList(formId,isRead,proviceCode,cityCode,isPc,username,ip,
							null,null,null,createTimeMin,createTimeMax,paginable);
					String name = form.getTitle();
					if (list == null) {
                        list = new ArrayList<>();
					}
					ExportParams exportParams = new ExportParams();
					exportParams.setSheetName(name);
					response.setContentType("application/x-download;charset=UTF-8");
					FileOutputStream fos = null;
					InputStream input = null;
					OutputStream output = null;
                List<Map<String, String>> mapList = new ArrayList<>();
                List<ExcelExportEntity> entityList = new ArrayList<>();
					Set<ResourcesSpaceData> fileRes = new HashSet<>();
					/**字段列表map*/
					List<String> fields = new ArrayList<>();
					Boolean isFile = false;
					fields.add("参与人");
					fields.add("参与时间");
					fields.add("网络设备");
					fields.add("IP地址");
					fields.add("地域");
					for (CmsFormItemEntity itemEntity: form.getInputItems()) {
						fields.add(itemEntity.getItemLabel());
					}
					if(list.size()>0){
						for (CmsFormDataEntity data : list) {
							Map<String, String> map = new LinkedHashTreeMap<>();
							fileRes.addAll(data.getFileDatas());
							String user = data.getCreateUser();
							if(WebConstants.ANONYMOUSUSER.equals(data.getCreateUser())){
								user ="匿名";
							}
							map.put("参与人", user);
							map.put("参与时间", MyDateUtils.formatDate(data.getCreateTime(), MyDateUtils.COM_Y_M_D_H_M_S_PATTERN));
							map.put("网络设备", data.getSystemInfo());
							map.put("IP地址", data.getIp());
							map.put("地域", data.getAddress());
							Map<String, Object> attr = data.getAttrLabelMap();
							for (CmsFormItemEntity itemEntity: form.getInputItems()) {
								//for(String key:attr.keySet()){
								String key = itemEntity.getItemLabel();
								Object val = attr.get(key);
								if(val!=null){
									if(val instanceof  Collection){
										StringBuffer buffer = new StringBuffer();
										/**图片多选和其他选项值不同类型*/
										if(CmsFormConstant.FIELD_TYPE_IMG_CHECKBOX.equals(itemEntity.getDataType())){
											Collection<CmsFormDataAttrImgVo> valSet = (Collection<CmsFormDataAttrImgVo>)val;
											for(CmsFormDataAttrImgVo s:valSet){
												buffer.append(s.getLabel()).append(",");
											}
										}else{
											for(Object v:(Collection<Object>)val){
												if(v instanceof  CmsFormResVo){
													CmsFormResVo s = (CmsFormResVo)v;
													buffer.append(s.getResAlias()).append(",");
												}else{
													buffer.append(v).append(",");
												}
											}
										}
										map.put(key,buffer.toString());
									}else if(val instanceof  CmsFormResVo){
										CmsFormResVo s= (CmsFormResVo)val;
										map.put(key,s.getResAlias());
									}else if(val instanceof  CmsFormDataAttrImgVo){
										CmsFormDataAttrImgVo s= (CmsFormDataAttrImgVo)val;
										map.put(key,s.getLabel());
									} else{
										map.put(key,val.toString());
									}
								}
								//}
							}
							mapList.add(map);
							if (!fileRes.isEmpty()) {
								isFile = true;
							}
						}
						for (String key : fields) {
							ExcelExportEntity exportEntity = new ExcelExportEntity(key, key, 50);
							//设置是否换行
							exportEntity.setWrap(true);
							entityList.add(exportEntity);
						}
					}
					File file = null;
					Workbook workbook = ExcelExportUtil.exportBigExcel(exportParams, entityList, mapList);
					try {


                        String fileName = FileUtil.getTmpDirPath() + WebConstants.SPT + name + ".xlsx";
                        fos = new FileOutputStream(fileName);
						workbook.write(fos);
                        file = new File(fileName);
						if (isFile) {

                            List<Zipper.FileEntry> fileEntries = new ArrayList<>();
							fileEntries.add(new Zipper.FileEntry("", "", file));
							//打包资源
							if (fileRes.size() > 0) {
								for (ResourcesSpaceData resource : fileRes) {
									File temp = resourcesSpaceDataService.getFileWithFilename(resource, null);
									if(temp.exists()){
										fileEntries.add(new Zipper.FileEntry("file", "", temp));
									}
								}
							}
							RequestUtils.setDownloadHeader(response, new String(name.getBytes(), "ISO8859-1") + ".zip");
							Zipper.zip(response.getOutputStream(), fileEntries, "UTF-8");
						} else {
							//不是附件 直接下载xlsx
							RequestUtils.setDownloadHeader(response, new String(name.getBytes(), "ISO8859-1") + ".xlsx");
							input = new FileInputStream(file);
							output = response.getOutputStream();
							byte[] buff = new byte[1024];
							int len = 0;
							while ((len = input.read(buff)) > -1) {
								output.write(buff, 0, len);
							}
						}
					} catch (IOException e) {
						log.error("导出失败，", e);
					} finally {
						try {
							if (fos != null) {
								fos.close();
							}
						} catch (IOException e) {
							log.error(e.getMessage());
						}
						try {
							if (output != null) {
								output.close();
							}
						} catch (IOException e) {
							log.error(e.getMessage());
						}
						try {
							if (input != null) {
								input.close();
							}
						} catch (IOException e) {
							log.error(e.getMessage());
						}
						ExcelExportUtil.closeExportBigExcel();
						if (file != null) {
							file.delete();
						}
					}
				}
			}
		return null;
	}

	/**
	 * 删除
	 *
	 * @Title: 删除
	 * @param: @param ids
	 * @param: @return
	 * @param: @throws GlobalException
	 * @return: ResponseInfo
	 */
    @PostMapping("/delete")
	public ResponseInfo delete(@RequestBody @Valid DeleteDto dels,
								   BindingResult result) throws GlobalException {
		validateBindingResult(result);
		service.physicalDelete(dels.getIds());
		return new ResponseInfo();
	}


}



