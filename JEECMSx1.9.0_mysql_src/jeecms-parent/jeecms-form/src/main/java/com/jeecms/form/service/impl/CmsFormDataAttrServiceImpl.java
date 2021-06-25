package com.jeecms.form.service.impl;/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

import com.jeecms.common.base.service.BaseServiceImpl;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.ueditor.ResourceType;
import com.jeecms.content.service.impl.ContentDocServiceImpl;
import com.jeecms.form.dao.CmsFormDataAttrDao;
import com.jeecms.form.dao.CmsFormDataAttrResDao;
import com.jeecms.form.domain.CmsFormDataAttrEntity;
import com.jeecms.form.domain.CmsFormDataAttrResEntity;
import com.jeecms.form.service.CmsFormDataAttrResService;
import com.jeecms.form.service.CmsFormDataAttrService;
import com.jeecms.resource.domain.ResourcesSpaceData;
import com.jeecms.system.domain.CmsSite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 表单属性service实现类
 * @author: tom
 * @date: 2020/1/9 20:58
 */
@Service
@Transactional(rollbackFor = Exception.class)
public class CmsFormDataAttrServiceImpl extends BaseServiceImpl<CmsFormDataAttrEntity, CmsFormDataAttrDao,Integer> implements CmsFormDataAttrService {

     /** 将转化后的PDF上传到服务器文件，存在/u/cms/*下面 **/
    /**
     * 如果选择了优先FTP并且设置了FTP,则使用FTP上传; 否则如果设置了OSS,则使用OSS; 都没设置则使用本地上传
     *
     * @throws Exception 异常
     **/
    @Override
    @Async("asyncServiceExecutor")
    public String uploadDoc(Integer dataAttrId, CmsSite site) throws GlobalException {
        CmsFormDataAttrEntity data = findById(dataAttrId);
        String fileUrl = "";
        if(data!=null){
            ResourcesSpaceData space = data.getResourcesSpaceData();
            /**只有文件类型需要转换pdf  视频、音频、图片无需转换pdf*/
            if(space!=null&& ResourceType.RESOURCE_TYPE_ANNEX.equals(space.getResourceType())){
                fileUrl = contentDocService.conversionDoc(space, site);
                data.setPdfPath(fileUrl);
                //修改pdf文件路径
                update(data);
            }
        }
        return fileUrl;
    }

    @Autowired
    private ContentDocServiceImpl contentDocService;
}
