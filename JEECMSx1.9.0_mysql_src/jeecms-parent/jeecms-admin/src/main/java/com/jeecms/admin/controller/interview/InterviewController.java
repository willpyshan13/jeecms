/*
@Copyright: 江西金磊科技发展有限公司 All rights reserved. Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */
package com.jeecms.admin.controller.interview;

import com.alibaba.fastjson.JSONObject;
import com.jeecms.admin.controller.interview.dto.InterviewUploadDTO;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.common.exception.GlobalException;
import com.jeecms.common.response.ResponseInfo;
import com.jeecms.common.util.ChastityUtil;
import com.jeecms.common.web.springmvc.RealPathResolver;
import com.jeecms.common.web.util.HttpClientUtil;
import com.jeecms.interview.OnlineConstants;
import com.jeecms.resource.domain.dto.UploadResult;
import com.jeecms.resource.service.impl.UploadService;
import com.jeecms.system.domain.CmsSite;
import com.jeecms.util.SystemContextUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Objects;

/**
 * @author xiaohui
 * @date 2021/1/13 15:59
 */
@RestController
@RequestMapping("/interview")
public class InterviewController {

    private static final Logger logger = LoggerFactory.getLogger(InterviewController.class);

    @Autowired
    private UploadService uploadService;
    @Autowired
    private ChastityUtil chastityUtil;
    @Autowired
    private RealPathResolver realPathResolver;

    /**
     * 上传视频
     *
     * @param file     视频文件
     * @param onlineId 在线访谈id
     * @param request  HttpServletRequest
     * @return ResponseInfo
     */
    @PostMapping("/upload")
    public ResponseInfo upload(@RequestParam(value = "uploadFile", required = false) MultipartFile file, Long onlineId, HttpServletRequest request) {
        CmsSite site = SystemContextUtils.getSite(request);
        try {
            UploadResult uploadResult = uploadService.doUploadVideo(file, site);
            String appId = chastityUtil.getId();
            UploadResult videoCover = uploadResult.getVideoCover();
            String fileUrl = "";
            if (videoCover != null) {
                fileUrl = videoCover.getFileUrl();
            }
            InterviewUploadDTO dto = new InterviewUploadDTO(fileUrl, onlineId, appId, uploadResult.getFileUrl(),
                    uploadResult.getDuration(), uploadResult.getFileSize(), uploadResult.getDimensions());

            String result = HttpClientUtil.postJson(OnlineConstants.DOMAIN + OnlineConstants.UPLOAD, dto);
            if (StringUtils.isBlank(result)) {
                logger.error("调用失败,{}", result);
                return new ResponseInfo(false);
            }
            JSONObject jsonObject = JSONObject.parseObject(result);
            if (Objects.equals(jsonObject.get("code"), 200)) {
                return new ResponseInfo(true);
            } else {
                logger.error("请求失败,{}", result);
                return new ResponseInfo(false);
            }
        } catch (GlobalException e) {
            e.printStackTrace();
            logger.error("文件上传失败,{}", e.getMessage());
        } catch (IOException e) {
            logger.error("文件上传失败{},{}", e.getMessage(), OnlineConstants.DOMAIN + OnlineConstants.UPLOAD);
        }
        return new ResponseInfo(false);
    }

    /**
     * 下载
     *
     * @param videoUrl 视频地址
     * @param response HttpServletResponse
     * @return Boolean
     */
    @GetMapping("/download")
    public Boolean download(String videoUrl, HttpServletResponse response) {
        if (videoUrl != null && videoUrl.startsWith(WebConstants.INTERVIEW_PATH)) {
            // 实现文件下载
            File file = new File(realPathResolver.get(videoUrl));
            try (
                    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
                    OutputStream os = response.getOutputStream();
            ) {
                String filename = file.getName();

                // 配置文件下载
                response.setHeader("content-type", "application/octet-stream");
                response.setContentType("application/octet-stream");
                // 下载文件能正常显示中文
                response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(filename, "UTF-8"));

                IOUtils.copy(bis, os);
                os.flush();

                logger.debug("download successfully! [{}]", file);
                return true;
            } catch (Exception e) {
                logger.debug("download failed! [{}]", file);
                return false;
            }
        }
        logger.error("下载地址为空，或者不正确：{}", videoUrl);
        return false;
    }

}
