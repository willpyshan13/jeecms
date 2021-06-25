var formId;
var fileImgVal=[]; //保存图片上传的值
var fileTextVal=[]; //保存文件上传的值
var selectItem = {
    tab:{
        list:[{id: 1,name: '省份'},
            {id: 2,name: '城市'},
            {id: 3,name: '县区'}]
    },
}
window.URL = window.URL || window.webkitURL;

function getItemData (item,fieldType,componentConfig){
    layui.use(['layer', 'form', 'upload','laydate'], function(){
        var layer = layui.layer,
        upload = layui.upload,
        laydate = layui.laydate,
        form = layui.form;
        var rightWidth = '80%';
        var content = JSON.parse(item.content);
        var contentHtml = ''; // 创建右边内容区域html
        var optionHtml = ''; // 下拉框类option
        var ortherOptionHtml = '';
        var defValue = item.defValue?JSON.parse(item.defValue):'';//默认数据
        var options = item.options // options内容
        var optionWidth = 95/content.value.radioBtns+'%'; // 列数
        var fileTypes = content.value.type || [];// 附件类型
        var fileHtml = '';// 附件类型限制
        var fileSizeHtml = ''
        if(componentConfig.moduleDisplay == 'top') {
            rightWidth = '100%'
        } else {
            rightWidth = content.value.width?content.value.width+'%':'80%'
        };
         /**
          * 默认为input类型 radio,radioImage,checkbox,imageCheckbox,code,select,cascade
          *
          */
        switch(fieldType){
            case 'radio':
                /* radio 单选框*/
                contentHtml = '<div>'
                for(var b=0; b<options.length;b++){
                    contentHtml+= '<div class="p-radio-width t-inline" style="width:'+optionWidth+';'+(b>=content.value.radioBtns?'margin-top:20px':'')+'">'+
                    '<input type="radio" data-name="'+item.field+'" name="'+item.field+'"'+
                    (defValue.value == options[b].value?'checked':'')+
                    ' class="p-radio-item" value="'+options[b].value+'" title="'+options[b].label +'">'+
                    '</div>'
                }
                // 如果其他选项存在
                if(content.value.isOtherOption){
                    contentHtml+='<div class="p-radio-width t-inline" style="width:'+optionWidth+';'+(b>=content.value.radioBtns?'margin-top:20px':'')+'">'+
                    '<input type="radio" data-name="'+item.field+'" name="'+item.field+'"'+
                    ' class="p-radio-item" value="'+content.value.otherOption.value+'" title="'+content.value.otherOptionLabel +'">'+
                    '<input style="display:none" class="option-input t-input layui-input" data-name="'+item.field+'Txt" type="text" maxlength="50" placeholder="请输入">'+
                    '</div>'
                }
                contentHtml+= '</div>'+(item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')
                $("#"+item.field).append(contentHtml)
            break
            case 'checkbox':
                /* 多选*/
                contentHtml = '<div>'
                for(var b=0; b<options.length;b++){
                    contentHtml+= '<div class="p-radio-width t-inline" style="width:'+optionWidth+';'+(b>=content.value.radioBtns?'margin-top:20px':'')+'">'+
                    '<input type="checkbox" data-name="'+item.field+'" lay-skin="primary" '+
                     (defValue.value.indexOf(options[b].value) > -1?'checked':'')+
                    ' value="'+options[b].value+'" title="'+options[b].label +'">'+
                    '</div>'
                }
                // 如果其他选项存在
                if(content.value.isOtherOption){
                    contentHtml+='<div class="p-radio-width t-inline" style="width:'+optionWidth+';'+(b>=content.value.radioBtns?'margin-top:20px':'')+'">'+
                    '<input type="checkbox" data-name="'+item.field+'"  lay-skin="primary" '+
                    ' class="p-radio-item" value="'+content.value.otherOption.value+'" title="'+content.value.otherOptionLabel +'">'+
                    '<input style="display:none" class="option-input t-input layui-input" data-name="'+item.field+'Txt" type="text" maxlength="50" placeholder="请输入">'+
                    '</div>'
                }
                contentHtml+= '</div>'+(item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')
                $("#"+item.field).append(contentHtml)
            break
            case 'imageCheckbox':
                /* 图片多选*/
                contentHtml = '<div class="imgItem">'
                for(var b=0; b<options.length;b++){
                    contentHtml+= '<div class="p-list">'+
                        '<div class="p-showBg">'+
                            '<img src="'+getImageUrls(options[b].picUrl)+'">'+
                        '</div>'+
                        ' <input type="checkbox" data-name="'+item.field+'" lay-skin="primary"'+
                        (defValue.value.indexOf(options[b].value) > -1?'checked':'')+
                        ' value="'+options[b].value+'" title="'+options[b].label +'">'+
                    '</div>'
                }
                contentHtml+= '</div>'+(item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')
                $("#"+item.field).append(contentHtml)
            break
            case 'imageRadio':
                /* 图片单选*/
                contentHtml = '<div class="imgItem">'
                for(var b=0; b<options.length;b++){
                    contentHtml+= '<div class="p-list">'+
                        '<div class="p-showBg">'+
                            '<img src="'+getImageUrls(options[b].picUrl)+'">'+
                        '</div>'+
                        ' <input type="radio" data-name="'+item.field+'" name="'+item.field+'" lay-skin="primary"'+
                        (defValue.value == options[b].value?'checked':'')+
                        ' value="'+options[b].value+'" title="'+options[b].label +'">'+
                    '</div>'
                }
                contentHtml+= '</div>'+(item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')
                $("#"+item.field).append(contentHtml)
            break
            case 'fileUpload':
                /* 附件上传*/
                contentHtml =
                '<div class="p-file-data file-data" >'+
                '<div class="file-data-bg upload '+(item.field+'Upload')+'" data-name="'+item.field+'">上传附件</div>'+
                (item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')+
                '<input type="file" multiple class="file-data-but upload-input" value="" />'+
                '</div>'+
                '<div class="fileImgBg '+(item.field+'Bg')+'"></div>'+
                '<div id="file0Text" class="p-file-text"></div>'
                $("#"+item.field).append(contentHtml)
            break
            case 'imageUpload':
                /* 单图上传*/
                for(var t = 0;t<fileTypes.length;t++){
                    fileHtml+=('image/'+fileTypes[t]+',');
                }
                fileSizeHtml = content.value.size?'(文件大小不超过'+content.value.size+content.value.unit+')':''
                contentHtml +=
                '<p class="font-400">支持'+fileTypes.join(',')+' 等图片格式'+fileSizeHtml+'</p>'+
                '<div class="p-video-item '+(item.field+'Upload')+'">'+
                '<div class="item-icon"><i class="layui-icon iconfont jee-shangchuantupian"></i></div>'+
                '<p class="p-video-tip">点击上传图片</p><p class="t-99">建议100 * 100PX </p></div>'+
                '<div class="fileImgBg '+(item.field+'Bg')+'"></div>'+
                '<input type="file" accept="'+(fileHtml||"image/*")+'" class="file-data-but upload-input" value="" />'
                contentHtml+= (item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')
                $("#"+item.field).append(contentHtml)
            break
            case 'multiImageUpload':
                /* 多图上传*/
                for(var t = 0;t<fileTypes.length;t++){
                    fileHtml+=('image/'+fileTypes[t]+',');
                }
                contentHtml += '<div class="p-file-data file-data" >'+
                '<div class="file-data-bg upload '+(item.field+'Upload')+'" data-name="'+item.field+'">多图上传</div>'+
                (item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')+
                '<input type="file" accept="'+(fileHtml||"image/*")+'" multiple class="file-data-but upload-input" value="" />'+
                '</div>'+
                '<div class="fileImgBg '+(item.field+'Bg')+'"></div>'
                $("#"+item.field).append(contentHtml)
            break
            case 'audioUpload':
                /* 音频上传*/
                for(var t = 0;t<fileTypes.length;t++){
                    fileHtml+=('audio/'+fileTypes[t]+',');
                }
                fileSizeHtml = content.value.size?'(文件大小不超过'+content.value.size+content.value.unit+')':''
                contentHtml = '<p class="font-400">支持'+fileTypes.join(',')+' 等音频格式'+fileSizeHtml+'</p><div class="p-video-item '+(item.field+'Upload')+'">'+
                    '<div class="item-icon"><i class="layui-icon iconfont jee-yinpin"></i></div>'+
                    '<p class="p-video-tip">点击上传音频</p></div><input accept="'+(fileHtml||"audio/*")+'" type="file" class="file-data-but upload-input" value="" />'+
                    '<audio class="'+(item.field+'Audio')+'" src="/i/song.ogg" ></audio>'
                contentHtml+= (item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')
                $("#"+item.field).append(contentHtml);
            break
            case 'videoUpload':
                /*视频上传*/
                for(var t = 0;t<fileTypes.length;t++){
                    fileHtml+=('video/'+fileTypes[t]+',');
                }
                fileSizeHtml = content.value.size?'(文件大小不超过'+content.value.size+content.value.unit+')':''
                contentHtml= '<p class="font-400">支持'+fileTypes.join(',')+' 等视频格式 '+fileSizeHtml+'</p><div class="p-video-item '+(item.field+'Upload')+'">'+
                    '<div class="item-icon"><i class="layui-icon iconfont jee-shangchuanicon"></i> </div>'+
                    '<p class="p-video-tip">点击上传视频</p></div><input  accept="'+(fileHtml||"video/*")+'" type="file" class="file-data-but upload-input" value="" />'
                contentHtml+= (item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')
                $("#"+item.field).append(contentHtml);
            break
            case 'input':
                /*文本*/
                contentHtml= '<div style="width:'+rightWidth+'">'+
                '<input type="text" data-name="'+item.field+'" '+ setRule(item,content)+
                ' placeholder="'+item.placeholder+'" class="layui-input t-input"  value="'+defValue+'" />'+
                (item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')+
                '</div>'
                $("#"+item.field).append(contentHtml);
            break
            case 'textarea':
                /*多行文本*/
                contentHtml= '<div style="width:'+rightWidth+'">'+
                '<textarea data-name="'+item.field+'" '+ setRule(item,content)+
                ' placeholder="'+item.placeholder+'" class="layui-textarea t-input" value="'+defValue+'" ></textarea>'+
                (item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')+
                '</div>'
                $("#"+item.field).append(contentHtml);
            break
            case 'select':
                /*下拉框*/
                for(var b=0; b<options.length;b++){
                    optionHtml += '<option '+(defValue.value== options[b].value?'selected':'')+' value="'+options[b].value+'">'+options[b].label+'</option>'
                }
                // 如果其他选项存在
                if(content.value.isOtherOption){
                    optionHtml += '<option value="'+content.value.otherOption.value+'">'+content.value.otherOption.label+'</option>'
                    ortherOptionHtml = '<input style="width:214px;margin-top:10px;display:none" class=" t-input layui-input" '+
                    ' data-name="'+item.field+'Txt" type="text" maxlength="50" placeholder="请输入">'
                }
                contentHtml= '<div style="width:'+rightWidth+'">'+
                '<select class="layui-select" data-name="'+item.field+'" name="'+item.field+'"'+
                ' lay-verify="required"><option value=""></option>'+optionHtml+'</select>'+ ortherOptionHtml +
                (item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')+
                '</div>'
                $("#"+item.field).append(contentHtml);
                console.log(1111111111111111)
            break
            case 'code':
                /*验证码*/
                contentHtml= '<div style="width:'+rightWidth+'">'+
                '<div><input type="text" maxlength="4" data-name="captcha" required="'+item.isRequired+'"'+
                ' lay-verify="required" placeholder="'+item.placeholder+'" class="layui-input t-code t-input" />'+
                '<div class="code-box "><img class="t-img" id="'+(item.field+'Capatch')+'" /></div></div>'+
                (item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')+
                '</div>'
                $("#"+item.field).append(contentHtml);
                delete fieldData[item.field]
                ruleFields.splice(ruleFields.indexOf(item.field,1))
                fieldData.captcha = ''
                if(item.isRequired){
                    ruleFields.push('captcha')
                }
                genCapatch(item.field+'Capatch')
                $('.code-box').on('click',function(e){
                    genCapatch(item.field+'Capatch')
                })
            break
            case 'datetime':
                /*日期*/
                var dateTime = defValue instanceof Object?defValue.value:defValue
                var formatType = content.value.type == 'datetime'?getTimeType(content.value.format)+' '+getTimeType(content.value.accuracy):( content.value.type == 'date'?getTimeType(content.value.format):getTimeType(content.value.accuracy));
                var formatType2 = content.value.type == 'datetime'?content.value.format+' '+content.value.accuracy:( content.value.type == 'date'?content.value.format:content.value.accuracy);
                // 如果有默认值
                if(content.value.isDefaultNow) {
                    dateTime = moment(new Date().getTime()).format(formatType)
                } else if (dateTime) {
                    dateTime = moment(Number(dateTime)).format(formatType)
                };
                contentHtml = '<div class="datetime" style="width:'+rightWidth+'">'+
                '<input type="text" class="layui-input t-input" placeholder="请选择" data-name="'+item.field+'"'+
                ' id="'+(item.field+'Time')+'" />'+
                (item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')+
                '</div>'
                $("#"+item.field).append(contentHtml);
                fieldData[item.field] = dateTime
                laydate.render({
                    elem: '#'+item.field+'Time', //指定元素
                    format: formatType2, //可任意组合
                    value: dateTime,
                    theme: '#45A5FF',
                    type:content.value.type,
                    done: function(date){
                        fieldData[item.field] = date
                    }
                });
            break
            case 'datetimerange':
                /*日期区间*/
                console.log(content)
                contentHtml = '<div class="datetime" style="width:'+rightWidth+'">'+
                '<input type="text" class="layui-input t-input" placeholder="请选择" data-name="'+item.field+'"'+
                ' id="'+(item.field+'Time')+'" />'+
                (item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')+
                '</div>'
                $("#"+item.field).append(contentHtml);
                laydate.render({
                    elem: '#'+item.field+'Time', //指定元素
                    format: content.value.type == 'datetime'?content.value.format+' '+content.value.accuracy:( content.value.type == 'date'?content.value.format:content.value.accuracy),
                    range: true,
                    theme: '#45A5FF',
                    type:content.value.type,
                    done: function(date){
                        fieldData[item.field] = date
                    }
                });
            break
            case 'cascade':
                /*级联*/
                contentHtml =
                '<div class="all-show layui-input" id="'+(item.field+'Area')+'" '+
                ' style="width:'+rightWidth+'"'+
                ' data-type="cascade">'+
                    '<p>'+
                        '<label class="select-item">请选择</label>'+
                        '<label id="'+(item.field+'Code')+'" style="padding-right:10px"></label>'+
                        '<i class="layui-icon layui-icon-triangle-d p-select-icon"></i>'+
                    '</p>'+
                '</div>'+
                '<div class="item-all '+(item.field+'All')+'">'+
                    '<div class="all-ul-item">'+
                        '<div class="ul-item-tab"></div>'+
                        '<ul class="ul-item-list">'+
                        '</ul>'+
                    '</div>'+
                '</div>'
                contentHtml+= (item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')+'</div>'
                $("#"+item.field).append(contentHtml);
            break
            case 'city':
                /*城市*/
                if(defValue.provinceCode||(defValue.areaArry&&defValue.areaArry.length)){
                    areaItems.push({
                        value:defValue.provinceCode?defValue:{provinceCode:defValue.areaArry[0]||"",cityCode:defValue.areaArry[1]||''},
                        name:item.field,
                        type:'city'
                    })
                }
                contentHtml =
                '<div class="all-show layui-input" id="'+(item.field+'Area')+'" data-type="city" '+
                '  style="min-width:200px;width:max-content">'+
                    '<p>'+
                        '<label class="select-item">请选择</label>'+
                        '<label id="'+(item.field+'Code')+'" style="padding-right:10px"></label>'+
                        '<i class="layui-icon layui-icon-triangle-d p-select-icon"></i>'+
                    '</p>'+
                '</div>'+
                '<div class="item-all '+(item.field+'All')+'">'+
                    '<div class="all-ul-item">'+
                        '<div class="ul-item-tab"></div>'+
                        '<ul class="ul-item-list">'+
                        '</ul>'+
                    '</div>'+
                '</div>'
                contentHtml+= (item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')+'</div>'
                $("#"+item.field).append(contentHtml);
                if(item.isRequired){
                    ruleFields.push(item.field+'P')
                }
            break
            case 'address':
                /*地址*/
                if(defValue.provinceCode||(defValue.areaArry&&defValue.areaArry.length)){
                    areaItems.push({
                        value:defValue.provinceCode?defValue:{provinceCode:defValue.areaArry[0]||"",cityCode:defValue.areaArry[1]||'',areaCode:defValue.areaArry[2]||'',address:defValue.address},
                        name:item.field,
                        type:'address'
                    })
                }
                contentHtml =
                '<div class="all-show layui-input" id="'+(item.field+'Area')+'" data-type="address" '+
                ' style="min-width:200px;width:max-content;vertical-align: middle;display: inline-block;">'+
                    '<p>'+
                        '<label class="select-item">请选择</label>'+
                        '<label id="'+(item.field+'Code')+'" style="padding-right:10px"></label>'+
                        '<i class="layui-icon layui-icon-triangle-d p-select-icon"></i>'+
                    '</p>'+
                '</div><input type="text" data-name="'+item.field+'" required="'+item.isRequired+'"'+
                'maxlength="120"'+
                ' style="vertical-align: middle;display: inline-block;margin-left:10px;width:51.5%"'+
                ' lay-verify="required" placeholder="详细地址" class="layui-input t-input" />'+
                '<div class="item-all '+(item.field+'All')+'">'+
                    '<div class="all-ul-item">'+
                        '<div class="ul-item-tab"></div>'+
                        '<ul class="ul-item-list">'+
                        '</ul>'+
                    '</div>'+
                '</div>'
                contentHtml+= (item.tipText?'<div class="t-tip">'+item.tipText+'</div>':'')+'</div>'
                $("#"+item.field).append(contentHtml);
                if(item.isRequired){
                    ruleFields.push(item.field+'P')
                }
                break
                case 'desc':
                    /*文本描述*/
                    contentHtml = '<div class="t-label">'+item.itemLabel+'</div>'
                    $("#"+item.field).append(contentHtml);
                    /*label-样式*/
                    $("#"+item.field+' .t-label').css({
                        'font-size':content.value.fontStyle.fontSize+'px',
                        'color': content.value.fontStyle.fontColor,
                        'font-weight': content.value.fontStyle.fontWigth,
                        'width': content.value.width+'%',
                        'text-align': content.value.titleAilgn == '1'?'left':(content.value.titleAilgn == '2'?'center':'right'),
                        'line-height':1.5
                    })
                break
                case 'bar':
                    /*分割条*/
                    contentHtml = '<div style="width:'+
                        content.value.width+'%;margin:'+(content.value.lineAilgn === 2?'auto':'0')+';'+
                        'position:'+(content.value.lineAilgn===3?'absolute':'relative')+';right:0;">'+
                        '<div  class="t-label '+fieldType+'" >'+item.itemLabel+'</div>'+
                        (content.value.tip?'<div class="t-tip" '+
                        'style="text-align:'+(content.value.tipAilgn===2?'center':(content.value.tipAilgn===3?'right':'left'))+';">'+content.value.tip+'</div>':'')+
                        '</div>'
                    $("#"+item.field).append(contentHtml);
                    /*label-样式*/
                    $("#"+item.field+' .t-label').css({
                        'border-bottom':'5px solid'+content.value.lineColor,
                        'padding-bottom':item.itemLabel?'8px':'0px',
                        'text-align':content.value.titleAilgn===2?'center':(content.value.titleAilgn===3?'right':'left'),
                        'border-color':content.value.lineColor,
                        'font-size':componentConfig.fontStyle.fontSize+'px',
                        'color': componentConfig.fontStyle.fontColor,
                        'font-weight': componentConfig.fontStyle.fontWigth
                    })
                break
                case 'line':
                    /*分割线*/
                    contentHtml = '<div style="width:'+
                        content.value.width+'%;margin:'+(content.value.lineAilgn === 2?'auto':'0')+';'+
                        'position:'+(content.value.lineAilgn===3?'absolute':'relative')+';right:0;">'+
                        '<div class="t-label '+fieldType+'" >'+item.itemLabel+'</div>'+
                        (content.value.tip?'<div class="t-tip" '+
                        'style="text-align:'+(content.value.tipAilgn===2?'center':(content.value.tipAilgn===3?'right':'left'))+'">'+content.value.tip+'</div>':'')+
                        '</div>'
                    $("#"+item.field).append(contentHtml);
                    /*label-样式*/
                    $("#"+item.field+' .t-label').css({
                        'border-bottom':'1px '+content.value.borderStyle+' '+content.value.lineColor,
                        'padding-bottom':item.itemLabel?'8px':'0px',
                        'text-align':content.value.titleAilgn===2?'center':(content.value.titleAilgn===3?'right':'left'),
                        'border-color':content.value.lineColor,
                        'font-size':componentConfig.fontStyle.fontSize+'px',
                        'color': componentConfig.fontStyle.fontColor,
                        'font-weight': componentConfig.fontStyle.fontWigth
                    })
                break
            default:
                // contentHtml = ''
                // $("#"+item.field).append(contentHtml);
        }
        /*样式*/
        $('.p-itemData .imgItem > .p-list').css({
            'margin-right':componentConfig.moduleDisplay == 'top'?'110px':'58px'
        })
        /*帮助信息-样式*/
        $('.p-itemData .t-tip').css({
            color:componentConfig.fontHelp.fontColor,
            fontSize:componentConfig.fontHelp.fontSize,
            fontWeight:componentConfig.fontHelp.fontWigth
        });
        /*字段-样式*/
        $('.p-itemData .p-right,.p-itemData .t-input').css({
            color:componentConfig.fontKey.fontColor,
            fontSize:componentConfig.fontKey.fontSize,
            fontWeight:componentConfig.fontKey.fontWigth
        });
        /*输入框-样式*/
        $('.layui-input, .layui-form-select,.layui-form-select .layui-input').css({
            height:'initial',
            lineHeight:componentConfig.displayType>2?'43px':(componentConfig.displayType<2?'33px':'38px')
        });
        /*输入框-样式*/
        $('.layui-textarea').css({
            height:componentConfig.displayType>2?'85px':(componentConfig.displayType<2?'75px':'80px')
        });
        /*样式 over*/
        // html插入
        form.render();


        // 值的获取-hyt

        /**
         * 监听单选事件
         */
        form.on('radio', function(data){
            var name = data.elem.getAttribute('data-name')
            getFormValue(name,'text',data)
            if(data.elem.checked&&data.value==999){
                // 如果其他选项需要必填
                if(content.value.isOtherOption&&content.value.isOtherOptionRequired == true){
                    ruleFields.push(name+'Txt')
                }
            } else {
               // 如果其他选项需要必填
               if(content.value.isOtherOption&&content.value.isOtherOptionRequired == true){
                    if(ruleFields.indexOf(name+'Txt') > -1){
                        ruleFields.splice(ruleFields.indexOf(name+'Txt'),1)
                    }
                }
            }
        });
        /**
         * 监听多选事件
         */
        form.on('checkbox', function(data){
            var name = data.elem.getAttribute('data-name')
            getFormValue(name,'check',data)
            if(data.elem.checked&&data.value==999){
                // 如果其他选项需要必填
                if(content.value.isOtherOption&&content.value.isOtherOptionRequired == true){
                    ruleFields.push(name+'Txt')
                }
            } else {
               // 如果其他选项需要必填
               if(content.value.isOtherOption&&content.value.isOtherOptionRequired == true){
                    if(ruleFields.indexOf(name+'Txt') > -1){
                        ruleFields.splice(ruleFields.indexOf(name+'Txt'),1)
                    }
                }
            }
        });
        /**
         * 监听下拉事件
         */
        form.on('select', function(data){
            var name = data.elem.getAttribute('data-name')
            getFormValue(name,'text',data)
            if(data.value == 999){
                $(this).parent().parent().siblings('.layui-input').css('display','block')
                // 如果其他选项需要必填
                if(content.value.isOtherOption&&content.value.isOtherOptionRequired == true){
                    ruleFields.push(name+'Txt')
                }
            } else {
                $(this).parent().parent().siblings('.layui-input').css('display','none')
                // 如果其他选项需要必填
                if(content.value.isOtherOption&&content.value.isOtherOptionRequired == true){
                    if(ruleFields.indexOf(name+'Txt') > -1){
                        ruleFields.splice(ruleFields.indexOf(name+'Txt'),1)
                    }
                }
            }
        });

        /*监听input-事件*/
        $('.layui-form-item .t-input').on('change',function(e){
            var name = $(this).attr('data-name')
            if($(this).val()&&!fieldData[name] || fieldData[name]!=$(this).val()){
                // fieldData[name] = $(this).val()
                verification(content,name,$(this).val(),this);
            }
        });

        /*上传-触发input的click事件*/
        $('.'+item.field+'Upload').on('click',function(e){
            $(this).siblings('.upload-input').click()
        });
        /*上传-input的change事件*/
        $('#'+item.field+' .upload-input').on('change',function(e){
            getFileLimit(e,item.field,content,this,fieldType)
        });

        // 级联
        $('#'+item.field+'Area').on('click',function(e){
            var type = $(this).attr('data-type')
            var b = $(this).siblings('.item-all').css('display')
            if(b === 'none'){
                $('.item-all').css('display','none')
                $(this).siblings('.item-all').css('display','block')
            } else {
                $(this).siblings('.item-all').css('display','none')
            }
            if(type == 'city'){
                toggleItemAll(
                    {key:'areaCode',name:'areaName'},
                    {list:[{id: 1,name: '省份'},{id: 2,name: '城市'}]},
                    areaList,
                    $(this).siblings('.item-all'),
                    item.field,type)
            } else if (type == 'address'){
                toggleItemAll({key:'areaCode',name:'areaName'},
                selectItem.tab,areaList,$(this).siblings('.item-all'),item.field,type)
            } else {
                var data = content.option
                var loop = function (arr,i){
                    for(var t = 0;t<arr.length;t++){
                        if(arr[t].children&&arr[t].children.length){
                            i = loop(arr[t].children,i+1)
                        }
                    }
                    return i

                }
                var l = loop(data.options,1)
                var r = [{id: 1,name: '一级'},{id: 2,name: '二级'},{id: 3,name: '三级'},{id: 4,name: '四级'}]
                toggleItemAll(
                    {key:'sortNum',name:'name'},
                    {list:r.slice(0,l)},
                    data.options,
                    $(this).siblings('.item-all'),
                    item.field,type)
            }


        });

    });
}
// 输入格式验证
function ruleValue(type,val){
    // ['','只能输入中文',
    // '只能输入英文',
    // '只能输入数字(整数或小数)',
    // '只能输入英文、数字',
    // '只能输入整数',
    // '只能输入中文、英文、数字',
    // '只能输入邮箱',
    // '只能输入手机号',
    // '只能输入座机号',
    // '只能输入手机或座机号',
    // '只能输入身份证号',
    // '只能输入邮政编码']
    var arr = ['',/^[\u4e00-\u9fa5]{0,}$/,/^[A-Za-z]+$/,/^[+-]?(0|([1-9]\d*))(\.\d+)?$/,/^[A-Za-z0-9]+$/,/^[1-9]\d*$/,/^[\u4E00-\u9FA5A-Za-z0-9]+$/,/^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/,/^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\d{8}$/,/^((\d{3,4}-)|\d{3.4}-)?\d{7,8}$/,/^(1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\d{8}||((\d{3,4}-)|\d{3.4}-)?\d{7,8})$/,/^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{4}$/,/[1-9]\d{5}(?!\d)/];
    var s = arr[parseInt(type)||0]
    if(s&&s.test(val)){
        return true
    } else {
        return false
    }
}
function verification(content,name,val,input){
    var verify = ['','chinese','english','float','enNum','number','cnEnNum','email','mobile','phone','phoneAll','identity','postal'];
    var type = verify.indexOf(content.value.inputLimit);
    if(content.value.isInputLimit&&content.value.inputLimit&&!this.ruleValue(type,val)){
        $(input).val('');
        fieldData[name] = '';
        var arr = ['','只能输入中文','只能输入英文','只能输入数字(整数或小数)','只能输入英文、数字','只能输入整数','只能输入中文、英文、数字','只能输入邮箱','只能输入手机号','只能输入座机号','只能输入手机或座机号','只能输入身份证号','只能输入邮政编码'];
        layer.msg(arr[type]);
    } else {
        fieldData[name] = val;
    }
}

/** 获取验证码*/
function genCapatch(name){
    $.ajax({
        url: srcUrl+"/common/kaptcha",
        data: {},
        success: function (result) {
            $('#'+name).attr('src',"data:image/png;base64," + result.data.img);
            fieldData.sessionId=result.data.sessionId
        }
    });
};
/*获取 图片地址*/
function getImageUrls(url){
    if(url.indexOf('http') >-1){
        return url
    } else {
        return srcUrl+url
    }
}

/*重置表单验证规则*/
function setRule(item,content){
    var html = ''
    var rule = 'lay-verify="'
    if(item.isRequired){
        html+='required ';
        rule+='required';
    };
    if(content.value.isLengthLimit){
        html+='maxlength="'+content.value.max+'" '
    } else {
        html+='maxlength="120" '
    }
    if(content.value.isInputLimit){
        if(item.isRequired){
            rule+='|'
        }
        switch(content.value.inputLimit){
            case 'float':
                rule+='number'
            break
            case 'mobile':
                rule+='number'
            break
            case 'postal':
                rule+='number'
            break
            case 'phone':
                rule+='tel'
            break
            case 'phoneAll':
                rule+='telOrPhone'
            break
            default:
                rule+=content.value.inputLimit
        }
    };
    rule+='"'
    return html + rule
};

/**
 * 获取表单值-hyt
*/
function getFormValue (name,type,data){
    switch(type){
        case 'text':
            // 普通类型：单选，文本
            fieldData[name] = data.value
        break
        case 'check':
            // 多选框类型
            if(!fieldData[name]){
                fieldData[name] = []
            }
            if(data.elem.checked){
                fieldData[name].push(data.value)
            }else{
                for(var i=0; i<fieldData[name].length;i++){
                    if(data.value == fieldData[name][i]){
                        fieldData[name].splice(i,1)
                    }
                }
            }
        break
        case 'city':
            /*城市*/
            console.log(data)
            fieldData[name+'P'] = data.value.areaA
            fieldData[name+'C'] = data.value.areaB
            delete fieldData[name]
            if(ruleFields.indexOf(name) > -1){
                ruleFields.splice(ruleFields.indexOf(name),1)
            }
        break
        case 'cascade':
            /*级联*/
            fieldData[name] = data.names.join(',')
        break
        case 'address':
            /*地址*/
            fieldData[name+'P'] = data.value.areaA
            fieldData[name+'C'] = data.value.areaB
            fieldData[name+'A'] = data.value.areaC
        break
        case 'file':
            /*文件*/
            if(!fieldData[name]){
                fieldData[name] = []
            }
            if(data.value){
                fieldData[name].push(data.value)
            }
        break
        default:
            console.log(data)
    }
}
/**
 * 附件上传 包括视频 音频文件等
 */
function fileChange (name,files,input) {
    for(var i = 0;i<files.length;i++){
        var fileType = files[i].type.split('/')
        getFormValue(name,'file',{value:files[i]})
        if(fileType[0] === 'image'){
            var fileReader = new FileReader();
            //对读取到的图片编码
            fileReader.onload = function(en){
                base64Pic = en.target.result;
                var blobUrl = window.URL.createObjectURL(dataURItoBlob(base64Pic))
                showImgHtml(name,blobUrl,input);
            }
            fileReader.readAsDataURL(files[i]);
        } else {
            fileTextVal =  files[i]
            var fileText = '';
            fileText +='<div class="file-text-list upload-list">'+
                '<label>'+files[i].name+'</label>'+
                '<i class="layui-icon iconfont p-icon-show jee-liulangwanbi"></i>'+
            '<div>'
            $("#"+name +' .p-file-text').append(fileText)
            $("#"+name+' .upload-list>.p-icon-show').hover(function() {
                $(this).addClass('jee-quxiao').removeClass('jee-liulangwanbi')
            }, function() {
                $(this).addClass('jee-liulangwanbi').removeClass('jee-quxiao')
            })
            // 删除上传
            $("#"+name+" .p-icon-show").on("click",function (e) {
                $(input).val('')
                fieldData[name].splice($(this).parent('.file-text-list').index(),1)
                $(this).parents('.file-text-list').remove();
            })
        }
    }
}
function showImgHtml(name,url,input){
    var imgHtml = '<div class="t-img-show">'+
            '<div class="img-box"><img class=" t-img" value='+url+' src="'+url+'"/></div>'+
            '<div class="t-img-mode">'+
            '<i class="layui-icon iconfont jee-guanbi img-close"></i>'+
            '<div class="img-add"><i class="layui-icon iconfont add icon jee-chongxinshangchuan"></i></div>'+
            '</div>'
        '</div>'
    $("#"+name+' .fileImgBg').append(imgHtml)
    /*鼠标移入*/
    $("#"+name+' .t-img-show').hover(function() {
        $(this).children('.t-img-mode').css('display','block');
    }, function() {
        $(this).children('.t-img-mode').css('display','none');
    });
     // 删除上传
     $("#"+name+" .img-close").on("click",function (e) {
        var idx = $("#"+name+' .t-img-show').index($(this).parents('.t-img-show'))
        fieldData[name].splice(idx,1)
        // console.log(idx)
        console.log(fieldData[name].length)
        $(input).val('');
        $(this).parents('.t-img-show').remove();
        $('#'+name + ' .p-video-item').css({'display':'block'});
     })
     // 重新上传
     $("#"+name+" .img-add .add").on("click",function (e) {
        var idx = $("#"+name+' .t-img-show').index($(this).parents('.t-img-show'))
        fieldData[name].splice(idx,1)
        $(input).val('');
        $(this).parents('.t-img-show').remove()
        $(input).click();
    })
}
/**
 * @param {*} base64Data 文件上传解码专用
 */
function dataURItoBlob(base64Data) {
    var byteString;
    if (base64Data.split(',')[0].indexOf('base64') >= 0) {
        byteString = atob(base64Data.split(',')[1]);//base64 解码
    } else {
        byteString = unescape(base64Data.split(',')[1]);
    }
    var mimeString = base64Data.split(',')[0].split(':')[1].split(';')[0];//mime类型 -- image/png
    var ia = new Uint8Array(byteString.length);//创建视图
    for (var i = 0; i < byteString.length; i++) {
        ia[i] = byteString.charCodeAt(i);
    }
    var blob = new Blob([ia], {
        type: mimeString
    });
    return blob;
}
/*视频*/
function videoChange (name,files,input) {
    var fileReader = new FileReader();
    var file  = files[0]
    fileReader.readAsDataURL(file);
    getFormValue(name,'file',{value:file})
    var videoSrc = ''
    fileReader.onload = function(e){
        console.log(e)
        base64Pic = fileReader.result;
        var blobUrl = window.URL.createObjectURL(dataURItoBlob(base64Pic))
        videoSrc +='<div class="p-video-list upload-list">'+
            '<div class="p-video-show">'+
                '<div class="p-video-close"><i class="layui-icon iconfont jee-guanbi video-list-close" ></i></div>'+
                '<div class="p-video-play" data-name="'+blobUrl+'">'+
                '<i class="layui-icon iconfont  jee-bofang video-list-play"></i>'+
                '</div>'+
                '<p>'+file.name+'</p>'+
                '<div class="p-video-add">'+
                    '<div class="video-add-bg"><i class="layui-icon iconfont video-list-upload jee-chongxinshangchuan"></i></div>'+
                '</div>'+
            '</div>'+
            '<div  class="p-video-showhide">'+
                '<video  src="'+blobUrl+'" ></video>'+
            '</div>'+
        '</div>'
        $("#"+name).append(videoSrc)
        $("#"+name+'>.p-video-item').css("display",'none')

        // 鼠标移动显示隐藏播放键
        $("#"+name+'>.upload-list').hover(function() {
            $(this).children('.p-video-show').css('display','block');
        }, function() {
            $(this).children('.p-video-show').css('display','none');
        });

        // 点击播放按钮弹出弹窗
        $("#"+name+' .video-list-play').on("click",function () {
            console.log($(this).parent().attr('data-name'))
            var videoSrcItem = $(this).parent().attr('data-name')
            console.log($(this).parent().attr('data-fileName'))
            $('.layertextBgShow').css('display','block');
            $('.layerBgShow').css('display','block');
            bandLayerBgShow(videoSrcItem)
         })
         // 删除上传
         $("#"+name+" .video-list-close").on("click",function (e) {
            var file = $(input)
            file.val('');
            $(this).parents('.p-video-list').remove()
            $('#'+name + ' .p-video-item').css({'display':'block'});
         })
         // 重新上传
         $("#"+name+" .video-list-upload").on("click",function (e) {
            var file = $(input)
            file.val('');
            $(this).parents('.p-video-list').remove()
            $('#'+name + ' .p-video-item').css({'display':'block'});
            $(input).click();
        })


    }
}
// 播放视频
function bandLayerBgShow(serUrl){
    console.log(serUrl)
    $('.layertextBgShow').css('display','block');
    $('.layerBgShow').css('display','block');
    document.getElementById("p-showVideo").src = serUrl
}
//关闭视频弹窗
function bandLayerBgHide() {
    var video = document.getElementById('p-showVideo');
        if(video.paused){
        //如果已暂停则播放
            video.play(); //播放控制
        }else{ // 已播放点击则暂停
            video.pause(); //暂停控制
        }
    $('.layertextBgShow').css('display','none');
    $('.layerBgShow').css('display','none');

}
function audioChange (name,files,input) {
    var fileReader = new FileReader();
    var file  = files[0]
    fileReader.readAsDataURL(files[0]);
    getFormValue(name,'file',{value:file})
    var audioSrc = ''
    fileReader.onload = function(e){
        base64Pic = fileReader.result;
        var blobUrl = window.URL.createObjectURL(dataURItoBlob(base64Pic))
        audioSrc +='<div class="p-video-list audioListItem upload-list" id="">'+
            '<div class="p-video-show">'+
                '<div class="p-video-close"><i class="layui-icon iconfont jee-guanbi audio-list-close"></i></div>'+
                '<div class="p-video-play"  data-name="'+blobUrl+'"><i class="layui-icon iconfont p-jee-play jee-bofang video-list-play"></i></div>'+
                '<p>'+file.name+'</p>'+
                '<div class="p-video-add">'+
                    '<div class="video-add-bg"><i class="layui-icon iconfont jee-chongxinshangchuan audio-list-upload"></i></div>'+
                '</div>'+
            '</div>'+
            '<div id="p-audioItem" class="p-audio-showhide">'+
                '<div class="p-audio-play"><i class="layui-icon iconfont jee-bofang audio-list-play p-jee-play"></i></div>'+
                '<p>'+file.name+'</p>'+
                '<div class="p-audio-add">'+
                    '<div class="audio-add-bg">'+
                    '<i class="layui-icon iconfont jee-tingzhianniu p-jee-play"></i>'+
                    // '<div class="bg-audio-icon">'+
                    //     '<img src="'+resUrl+'/lib/img/progress.png">'+
                    //     '<div class="gray-div"></div>'+
                    //     '<div class="audioBgShow"></div>'+
                    // '</div>'+
                    '</div>'+
                '</div>'+
            '</div>'+
        '</div>'
        $("#"+name).append(audioSrc)
        $("#"+name+'>.p-video-item').css("display",'none')

        // 鼠标移动显示隐藏播放键
        $("#"+name+' .audioListItem').hover(function() {
            $(this).children('.p-video-show').css('display','block');
            $("#"+name+' .p-audio-showhide').css('display','none');
        }, function() {
            $(this).children('.p-video-show').css('display','none');
            $("#"+name+' .p-audio-showhide').css('display','block');
        });
        var audioValue =  $(input).siblings('.'+name+'Audio')[0];
        var audioUploadFle = $(input)
        // 点击播放按钮
        $("#"+name +" .p-jee-play").on("click",function () {
            var audioSrcItem = $(this).parent().attr('data-name')
            if(audioValue.paused){
                audioValue.src = audioSrcItem
                $(this).addClass('jee-zanting').removeClass('jee-bofang')
                $("#"+name+' .p-jee-play').addClass('jee-zanting').removeClass('jee-bofang')
                audioValue.play();
                getTimeWidth(audioValue,true)

            }else{
                $(this).addClass('jee-bofang').removeClass('jee-zanting ')
                $("#"+name+' .p-jee-play').addClass('jee-bofang').removeClass('jee-zanting')
                audioValue.pause();
                getTimeWidth(audioValue,false)
            }
         })
         // 删除上传
         $("#"+name+" .audio-list-close").on("click",function (e) {
            audioUploadFle.val('')
            audioValue.pause()
            audioValue.src = ''
            $("#"+name+' >.p-video-item').css("display",'block')
            $(this).parents('.p-video-list').remove()

         })
         // 重新上传
         $("#"+name+" .audio-list-upload").on("click",function (e) {
            audioUploadFle.val('')
            audioValue.pause()
            audioValue.src = ''
            $(this).parents('.p-video-list').remove()
            $("#"+name+'>.p-video-item').css("display",'block')
            $(input).click();
        })
    }
}

var audioWidth // 播放音频进度条的宽度
var timer //定时器
function getTimeWidth (audio, status) {
    if (status) {
    timer = setInterval(function() {
        audioWidth = ((audio.currentTime || 0) / audio.duration) * 100 + '%'
        $('.audioBgShow').css('width',audioWidth);
        console.log(audioWidth)
        if(audioWidth == '100%'){
            clearInterval(timer)
            $('.p-jee-play').addClass('jee-bofang').removeClass('jee-zanting')
        }
      }, 3000)
    } else {
      clearInterval(timer)
    }
}

/* 单图上传*/
function imageChange(name,files,input){
    fieldData[name] = []
    $('#'+name + ' .p-video-item').css({'display':'none'});
    var fileReader = new FileReader();
    var file  = files[0]
    var fileType = file.type.split('/')
    fileReader.readAsDataURL(files[0]);
    getFormValue(name,'file',{value:file})
    $(input).val('')
    if(fileType[0] === 'image'){
        //对读取到的图片编码
        fileReader.onload = function(e){
            base64Pic = fileReader.result;
            var blobUrl = window.URL.createObjectURL(dataURItoBlob(base64Pic))
            showImgHtml(name,blobUrl,input);
        }
    }
}
/* 多图上传*/
function multiImageChange(name,files,input){
    for(var i = 0;i<files.length;i++){
        var fileReader = new FileReader();
        var fileType = files[i].type.split('/')
        getFormValue(name,'file',{value:files[i]})
        if(fileType[0] === 'image'){
            //对读取到的图片编码
            fileReader.onload = function(en){
                base64Pic = en.target.result;
                var blobUrl = window.URL.createObjectURL(dataURItoBlob(base64Pic))
                showImgHtml(name,blobUrl,input);
            }
        }
        fileReader.readAsDataURL(files[i]);
    }
}
/*hyt-上传类别*/
function getFileLimit(e,name,data,input,type){
    var files  = e.target.files
    var canUpload = true;
    var obj = {
        typeLimit:data.value.typeLimit,//限制类型 1 不限制,2 允许类型 ,3 禁用类型
        fileTypes:data.value.type||(data.value.typeLimit&&data.value.typeLimit >1?(data.value.typeLimit>2?
            data.value.disableType.split(','):data.value.enableType.split(',')):[]),// 除附件以外其他允许类型
        disableType:data.value.disableType,// 禁用类型
        enableType:data.value.enableType,// 允许类型
        size:data.value.size,// 文件限制大小
        unit:data.value.unit,// 大小单位
        limitNum:data.value.limit?Number(data.value.limit):0,// 限制数量
        name:data.value.name // 标识
    }
    console.log(obj)
    if(files){
        console.log(files.length)
        console.log(obj.limitNum)
        console.log(fieldData[obj.name]?fieldData[obj.name].length:fieldData[obj.name])
        // 判断文件数量是否大于限制数量
        if(obj.limitNum > 0){
            if((fieldData[obj.name] &&(fieldData[obj.name].length >= obj.limitNum) ||files.length > obj.limitNum )){
                canUpload = false
                layer.msg('文件数量不得大于'+obj.limitNum);
                return
            }
        }
        /*循环上传附件*/
        for(var i = 0;i<files.length;i++){
            // 单个文件大小
            var fileSize = (files[i].size / 1024).toFixed(0);
            var filetypeTr = files[i].name.substring(files[i].name.lastIndexOf(".") + 1);
            /*附件-限制类型*/
            if(obj.typeLimit){
                if(obj.typeLimit == 2&&obj.fileTypes.indexOf(filetypeTr) <0){
                    canUpload = false
                    layer.msg('不支持这种类型的文件');
                    return
                } else if(obj.typeLimit == 3&&obj.fileTypes.indexOf(filetypeTr) >-1){
                    canUpload = false
                    layer.msg('不支持这种类型的文件');
                    return
                }
            } else {
                /*附件以外的其他文件上传*/
                if(obj.fileTypes &&obj.fileTypes.length){
                    if(obj.fileTypes.indexOf(filetypeTr) <0){
                        canUpload = false
                        layer.msg('不支持这种类型的文件');
                        return
                    }
                }
            }
            /*限制大小*/
            if(obj.size){
                var	sizeNum = obj.unit == 'MB'?obj.size*1024:obj.size
                if(fileSize>sizeNum){
                    canUpload = false
                    layer.msg('文件大小不得大于'+obj.size+(obj.unit == 'MB'?'MB':'KB'));
                    return
                }
            }
        }
    }

    if(canUpload){
        switch(type){
            case 'videoUpload':
                videoChange(name,files,input);
            break
            case 'audioUpload':
                audioChange(name,files,input);
            break
            case 'fileUpload':
                fileChange(name,files,input);
            break
            case 'imageUpload':
                imageChange(name,files,input);
            break
            case 'multiImageUpload':
                multiImageChange(name,files,input);
            break
        }
    }
};
/*转换时间格式*/
function getTimeType(type){
    switch(type){
        case 'yyyy-MM-dd':
        return 'YYYY-MM-DD'
        case 'yyyy-MM':
        return 'YYYY-MM'
        case 'yyyy':
        return 'YYYY'
        case 'MM-dd':
        return 'MM-DD'
        case 'MM':
        return type
        case 'dd':
        return 'DD'
        case 'HH:mm:ss':
        return type
        case 'HH:mm':
        return type
        case 'HH':
        return type
    }
}