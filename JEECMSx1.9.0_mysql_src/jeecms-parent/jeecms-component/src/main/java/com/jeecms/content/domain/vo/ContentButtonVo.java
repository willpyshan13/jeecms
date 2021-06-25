/**
 * @Copyright: 江西金磊科技发展有限公司  All rights reserved.Notice 仅限于授权后使用，禁止非授权传阅以及私自用于商业目的。
 */

package com.jeecms.content.domain.vo;

import com.jeecms.channel.domain.Channel;
import com.jeecms.common.constants.WebConstants;
import com.jeecms.content.constants.ContentButtonConstant;
import com.jeecms.content.constants.ContentConstant;
import com.jeecms.content.domain.Content;
import com.jeecms.content.domain.dto.ContentButtonDto;
import com.jeecms.content.util.ContentInitUtils;
import com.jeecms.system.domain.CmsSiteConfig;
import com.jeecms.system.domain.ContentType;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 内容按钮显示VO
 *
 * @author: chenming
 * @date: 2020/2/26 17:40
 */
public class ContentButtonVo {
    private List<ContentButtonDto> dtos;

    public List<ContentButtonDto> getDtos() {
        return dtos;
    }

    public void setDtos(List<ContentButtonDto> dtos) {
        this.dtos = dtos;
    }

    /**
     * 请求状态：新增
     */
    public static final int REQUEST_STATUS_SAVE = 1;
    /**
     * 请求状态：修改
     */
    public static final int REQUEST_STATUS_DETAILS = 2;

    /**
     * TODO 内容删除的api有两种可能：1. 非审核中的内容加入回收站 2. 审核中的内容直接物理删除
     */



    /**
     * 初始化按钮展示vo
     * @param content   内容对象
     * @param forceReleaseButton    是否存在强制提交按钮
     * @param config    全局配置(已发布内容是否允许编辑发布)
     * @param status    操作状态：1-新增内容获取按钮 2-修改内容获取按钮
     * @param types     内容类型
     * @param channel   栏目对象
     * @return  ContentButtonVo
     */
    public static ContentButtonVo initButtonVo(Content content, Boolean forceReleaseButton, CmsSiteConfig config, Integer status, List<ContentType> types, Channel channel,Boolean quote) {
        // 通过操作状态获取到操作枚举聚合
        List<ContentButtonConstant.OperatePiece> operatePieces = getOperatePieces(status,content);
        List<ContentButtonDto> dtos = new ArrayList<ContentButtonDto>(operatePieces != null ? operatePieces.size() : 1);
        for (ContentButtonConstant.OperatePiece operatePiece : operatePieces) {
            ContentButtonDto dto = new ContentButtonDto();
            dto.setText(ContentButtonConstant.OperatePiece.getPieceText(operatePiece));
            dto.setKey(ContentButtonConstant.OperatePiece.getContentButtonKey(operatePiece));
            dto.setIcon(ContentButtonConstant.OperatePiece.getPieceIcon(operatePiece));
            //通过操作块获取到操作的具体list集合
            List<Integer> operateRows = conversionOperateRows(operatePiece, status, content, channel, forceReleaseButton);
            if (ContentButtonConstant.OperatePiece.settypePiece.equals(operatePiece) && CollectionUtils.isEmpty(operateRows)) {
                List<Integer> contentTypeIds = null;
                List<ContentType> contentTypes = content.getContentTypes();
                if (!CollectionUtils.isEmpty(contentTypes)) {
                    contentTypeIds = contentTypes.stream().map(ContentType::getId).collect(Collectors.toList());
                }
                List<ContentButtonDto.ContentButtionSubsetDto> subsetDtos = new ArrayList<>(contentTypes.size());
                for (ContentType type : types) {
                    ContentButtonDto.ContentButtionSubsetDto subsetDto = dto.new ContentButtionSubsetDto();
                    if (contentTypeIds != null && contentTypeIds.contains(type.getId())) {
                        subsetDto.setText("取消"+type.getTypeName());
                        subsetDto.setContentTypeStatus(true);
                    } else {
                        subsetDto.setText(type.getTypeName());
                        subsetDto.setContentTypeStatus(false);
                    }
                    subsetDto.setContentTypeId(type.getId());
                    subsetDto.setButtonPopUposType("Custom");
                    subsetDto.setContentTypeIcon(type.getLogoResource());
                    subsetDto.setClick(content.getTypeContentAble());
                    ContentButtonDto.ContentButtionSubsetDto.ApiPort apiPort = subsetDto.new ApiPort(WebConstants.ADMIN_PREFIX+"/contentext/operation", "POST");
                    subsetDto.setPort(apiPort);
                    subsetDtos.add(subsetDto);
                    if (quote) {
                        subsetDto.setClick(false);
                    }

                }
                dto.setSubset(subsetDtos);
                dtos.add(dto);
                continue;
            }
            List<ContentButtonDto.ContentButtionSubsetDto> subsetDtos = new ArrayList<>(operateRows.size());
            for (Integer operateRow : operateRows) {
                ContentButtonDto.ContentButtionSubsetDto subsetDto = dto.new ContentButtionSubsetDto();
                subsetDto.setText(ContentButtonConstant.BUTTON_TEXT.get(operateRow));
                subsetDto.setIcon(ContentButtonConstant.BUTTON_ICON.get(operateRow));
                ContentButtonDto.ContentButtionSubsetDto.ApiPort apiPort = subsetDto.new ApiPort();
                if (ContentButtonConstant.Api.CONTENT_UPDATE.contains(operateRow)) {
                    apiPort.setApiUrl(WebConstants.ADMIN_PREFIX+"/content");
                    apiPort.setRequestMethod("PUT");
                } else {
                    if (content != null && ContentConstant.STATUS_SMART_AUDIT == content.getStatus()) {
                        apiPort.setApiUrl(WebConstants.ADMIN_PREFIX+"/contentext");
                        apiPort.setRequestMethod("DELETED");
                    } else {
                        Map<String, String> apiMap = ContentButtonConstant.Api.CONTENT_UPDATE_STATUS.get(operateRow);
                        if (apiMap != null) {
                            apiPort.setApiUrl(apiMap.keySet().iterator().next());
                            apiPort.setRequestMethod(apiMap.values().iterator().next());
                        }
                    }
                }
                subsetDto.setButtonPopUposType(ContentButtonConstant.BUTTON_POP_UPS_TYPE.get(operateRow));
                subsetDto.setKey(ContentButtonConstant.FONT_END_NEED_KEY.get(operateRow));
                subsetDto.setPort(apiPort);
                if(content != null) {
                    Boolean click = ContentInitUtils.operatePurview(operateRow, content, config);
                    subsetDto.setClick(click != null ? click : true);
                } else {
                    subsetDto.setClick(ContentInitUtils.operatePurview(operateRow, channel));
                }
                subsetDtos.add(subsetDto);
                if (quote) {
                    if(Arrays.asList(ContentButtonConstant.OPERATE_PREVIEW,ContentButtonConstant.OPERATE_BROWSE).contains(operateRow)) {
                        subsetDto.setClick(true);
                    } else {
                        subsetDto.setClick(false);
                    }
                }

            }
            dto.setSubset(subsetDtos);
            dtos.add(dto);
        }
        ContentButtonVo vo = new ContentButtonVo();
        vo.setDtos(dtos);
        return vo;
    }

    /**
     * 通过操作状态获取到操作块
     * @param status    操作类型
     * @param content   内容对象
     * @return List<ContentButtonConstant.OperatePiece>
     */
    private static List<ContentButtonConstant.OperatePiece> getOperatePieces(Integer status,Content content) {
        List<ContentButtonConstant.OperatePiece> operatePieces = null;
        switch (status) {
            case ContentButtonVo.REQUEST_STATUS_SAVE:
                operatePieces = ContentButtonConstant.SAVE_OPERATEPIECE;
                break;
            case ContentButtonVo.REQUEST_STATUS_DETAILS:
                operatePieces = ContentButtonConstant.OperatePiece.getStatusOperatePiece(content.getStatus());
                break;
            default:
                break;
        }
        return operatePieces;
    }

    /**
     * 通过操作块获取到操作的具体list集合
     * @param operatePiece  操作枚举
     * @param status        操作状态
     * @param content       内容对象
     * @param channel       栏目对象
     * @param forceReleaseButton    是否展示强制发布按钮
     * @return List<Integer>
     */
    private static List<Integer> conversionOperateRows(ContentButtonConstant.OperatePiece operatePiece,Integer status,Content content,Channel channel,Boolean forceReleaseButton) {
        List<Integer> operateRows = null;
        switch (status) {
            // 如果此次操作是新增
            case ContentButtonVo.REQUEST_STATUS_SAVE:
                if (ContentButtonConstant.OperatePiece.savePiece.equals(operatePiece)) {
                    operateRows = ContentButtonConstant.SAVE;
                }
                if (ContentButtonConstant.OperatePiece.opertingOnePiece.equals(operatePiece)) {
                    if (channel.getRealWorkflowId() != null) {
                        operateRows = ContentButtonConstant.SAVE_WORKFLOW_OPERATE;
                    } else {
                        operateRows = ContentButtonConstant.SAVE_OPERATE;
                    }
                }
                break;
            case ContentButtonVo.REQUEST_STATUS_DETAILS:
                operateRows = ContentInitUtils.getOperatePieces(operatePiece, content, forceReleaseButton);
                break;
            default:
                break;
        }
        return operateRows;
    }
}