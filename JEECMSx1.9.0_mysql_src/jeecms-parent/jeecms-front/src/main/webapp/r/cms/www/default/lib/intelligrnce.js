
var srcUrl = ''
var formId=''
// console.log('${base}')
/**
 * 添加一个对象用以储存值-hyt
*/
var fieldData = {}
/**
 * 添加一个数组用以储存必填项
*/
var ruleFields = []
var resUrl = ''
// 储存获取到的数据
var hrFormData = {},hasId = false
// 存储地址
var areaList = [],areaItems = []
var smartSkin = {
    bgConfig: {
      bgType: 2,
      bgImage: '',
      bgImage2: '',
      bgImageUrl: '',
      phoneImgUrl: '',
      alignType: 'left top',
      opacity: 100,
      isRepeat: 2,
      bgColor: '#F0F0F0'
    },
    headConfig: {
      bgImage: '',
      bgImageUrl: '',
      phoneImgUrl: '',
      bgImage2: ''
    },
    contConfig: {
      bgColor: '#ffffff',
      hasBorder: 1,
      borderColor: '#e8e8e8',
      borderWidth: 1,
      borderRadius: 0,
      hasBody: 1,
      bodyWidth: 1000
    },
    componentConfig: {
      bgColor: '#F0F0F0',
      fontStyle: {
        fontSize: 16,
        fontWigth: 400,
        fontColor: '#333333'
      },
      fontKey: {
        fontSize: 14,
        fontWigth: 400,
        fontColor: '#333333'
      },
      fontHelp: {
        fontSize: 12,
        fontWigth: 400,
        fontColor: '#999999'
      },
      displayType: 2,
      moduleDisplay: 'left',
      titleWidth: 20
    },
    subConfig: {
      text: '提交',
      fontStyle: {
        fontSize: 14,
        fontWigth: 400,
        fontColor: '#ffffff'
      },
      bgColor: '#1ec6df',
      hasBorder: 1,
      borderColor: '#ffffff',
      borderWidth: 1,
      borderRadius: 4,
      btnWidth: 80,
      btnHeight: 32
    }
  }
// 接口地址-用以区分页面
var apiObj = {
    detailApi:"/smartForm/",//详情
    saveApi:"/smartForm",// 提交表单
    viewApi:"/smartForm/view/",//添加浏览量
    shareApi:"/wechat/sign",// 微信分享
    name:'form',
    params:{
        typeId:''
    }
}
// 初始化
function layUiUse (id,url,trUrl,isId,params){
    apiObj = params||apiObj
    console.log(apiObj)
    if(formId == id){
        return false
    }
    formId = id
    
    srcUrl = url||''
    resUrl = url+trUrl || url+'/r/cms/www/default'
    hasId = !!isId
    getRefreshView(id);
    getAreaData();
    // 初始化
    $("#fieldForm .layui-form-item").remove();
    fieldData = {};
    hrFormData={};
    ruleFields=[];
    layui.use(['layer', 'form'], function(){
        var layer = layui.layer,
        form = layui.form;
        $.ajax({
            type:'GET',
            url: url+apiObj.detailApi+id,
            success: function(res){
                if(res.code == 200){
                    var listData = res.data.items
                    var componentConfig =  res.data.componentConfig?JSON.parse(res.data.componentConfig):smartSkin.componentConfig
                    var contConfig =  res.data.contConfig?JSON.parse(res.data.contConfig):smartSkin.contConfig
                    var subConfig = res.data.subConfig?JSON.parse(res.data.subConfig):smartSkin.subConfig
                    var bgConfig = res.data.bgConfig?JSON.parse(res.data.bgConfig):smartSkin.bgConfig
                    var headConfig = res.data.headConfig?JSON.parse(res.data.headConfig):smartSkin.headConfig
                    var titleWidth;
                    showWeChat(res.data);
                    hrFormData = res.data
                    fieldData.formId = res.data.id
                    fieldData.title = res.data.title
                    /*背景样式*/
                    $('.t-smart-wrap .t-smart-bg').css({
                        backgroundColor:bgConfig.bgType==1?'':bgConfig.bgColor,
                        backgroundImage:bgConfig.bgType==1?'url(' + (bgConfig.bgImageUrl || bgConfig.bgImageUrlSelect) + ')':'',
                        backgroundPosition:bgConfig.alignType,
                        backgroundRepeat:bgConfig.isRepeat==1? 'repeat' : 'no-repeat',
                        opacity: bgConfig.opacity / 100,
                        zIndex:1
                    });
                    /*页眉*/
                    $('.t-smart-head').css({
                        'background-position': 'center center',
                        'background-attachment': 'scroll',
                        'background-size': 'cover',
                        '-webkit-background-size': 'cover',
                        'background-repeat': 'no-repeat',
                        'background-image':'url("'+srcUrl+(headConfig.bgImageUrl||headConfig.bgImageUrlSelect)+'")',
                        'height':headConfig.bgImageUrl||headConfig.bgImageUrlSelect?'150px':'0px'
                    })
                    // $('.t-smart-head .t-img').attr('src',headConfig.bgImageUrl||headConfig.bgImageUrlSelect);
                    /*组件样式*/
                    /*提交按钮*/
                    $('.sub-btn-wrap').css({
                        'text-align': 'center',
                        'padding-bottom':'50px',
                        paddingTop:componentConfig.displayType>2?'15px':(componentConfig.displayType<2?'25px':'20px')
                    })
                    $('.sub-btn').html(subConfig.text)
                    $('.sub-btn').css({
                        'width': subConfig.btnWidth,
                        'height':subConfig.btnHeight,
                        'border-width':subConfig.hasBorder==1?subConfig.borderWidth:'0px',
                        'background-color':subConfig.bgColor||'#E30B20',
                        'border-radius':subConfig.hasBorder==1?subConfig.borderRadius+'px':'0px',
                        'border-color':subConfig.hasBorder==1?subConfig.borderColor:'#E30B20',
                        'font-size':subConfig.fontStyle.fontSize,
                        'color':subConfig.fontStyle.fontColor||'#ffffff',
                        'line-height':subConfig.btnHeight+'px',
                        'font-weight':subConfig.fontStyle.fontWigth
                    });
                    $('.t-smart-wrap .container').css({
                        width:contConfig.bodyWidth,
                        zIndex:2,
                        position:'relative'
                    })
                    /*内容区域*/
                    $('.p-itemData').css({
                        width:contConfig.bodyWidth,
                        borderColor:contConfig.borderColor,
                        borderWidth:contConfig.hasBorder==1?contConfig.borderWidth+'px':'0px',
                        borderRadius:contConfig.borderRadius+'px',
                        backgroundColor:contConfig.bgColor,
                        borderStyle:'solid',
                        paddingTop:componentConfig.displayType>2?'15px':(componentConfig.displayType<2?'25px':'20px')
                    })
                    if(listData.length>0){
                        $('.sub-btn-wrap').css({
                            'display':'block'
                        })
                    } else {
                        $('.sub-btn-wrap').css({
                            'display':'none'
                        })
                    }
                    /*数据item-循环*/
                    for(var i = 0; i<listData.length; i++){
                        var defaultValue =  listData[i].defValue? listData[i].defValue.replace(/\s*/g,""):''
                        // 组件配置 str.;
                        var content = listData[i].content?JSON.parse(listData[i].content):'';
                        var defValue = defaultValue?JSON.parse(listData[i].defValue):''
                        // 列表开始创建
                        var fieldType = 'input'; // 默认为input类型 radio，imageRadio，checkbox，imageCheckbox，code，select，cascade
                        fieldType = getType(listData[i].dataType); // 统一type
                        var layoutTypes = ['bar','line','desc']; // 布局组件
                        var formItemHtml = '';
                        var requiredHtml = listData[i].isRequired? '<span class="t-red">*</span>':'';
                        var classItem = ''
                        /*不同类别添加class*/
                        if (fieldType == 'fileUpload'||fieldType == 'multiImageUpload'){
                            classItem = 'fileItem'
                        } else if (fieldType == 'videoUpload'||fieldType == 'audioUpload'||fieldType == 'imageUpload'){
                            classItem = 'videoItem'
                        }
                        /**
                         * 当组件是否为布局组件时，进行不同的操作
                         */
                        if (!Array.prototype.indexOf){
                            Array.prototype.indexOf = function(elt /*, from*/){
                                var len = this.length >>> 0;
                                var from = Number(arguments[1]) || 0;
                                from = (from < 0) ? Math.ceil(from) :
                                Math.floor(from); if (from < 0) from += len;
                                for (; from < len; from++) {
                                    if
                                    (from in this && this[from] === elt)
                                    return from;
                                } return -1;
                            };
                        }
                        if(layoutTypes.indexOf(fieldType) > -1){
                            formItemHtml = '<div class="layui-form-item standard-type">'+
                            '<div id="'+listData[i].field+'" style="position:relative"></div></div>'
                            // 创建列表
                            $("#fieldForm").append(formItemHtml);
                            getItemData(listData[i],fieldType,componentConfig,resUrl);
                        } else {
                            /*表单组件*/
                            formItemHtml = '<div class="layui-form-item standard-type type'+componentConfig.displayType+' '+componentConfig.moduleDisplay+'" >'+
                            '<div class="layui-form-label p-left t-label '+fieldType+'" >'+requiredHtml+listData[i].itemLabel+':'+'</div>'+
                            '<div class="layui-input-block p-right '+fieldType+' '+classItem+
                            '" id="'+listData[i].field+'"></div></div>'
                            /** 在对象中创建组件字段赋值-hyt */
                            if(defValue instanceof Object){
                                fieldData[listData[i].field] = defValue.value
                            } else {
                                fieldData[listData[i].field] = null
                            }
                            if(listData[i].isRequired){
                                ruleFields.push(listData[i].field)
                            };
                            // 创建列表
                            $("#fieldForm").append(formItemHtml);
                            getItemData(listData[i],fieldType,componentConfig,resUrl);
                        };
                    }
                    /*label-样式*/
                    $('.p-right .t-label').css({
                        'padding':'0',
                        'font-size':componentConfig.fontStyle.fontSize+'px',
                        'color': componentConfig.fontStyle.fontColor,
                        'font-weight': componentConfig.fontStyle.fontWigth,
                        'width': componentConfig.moduleDisplay == 'top'?'100%':componentConfig.titleWidth+'%',
                        'text-align': componentConfig.moduleDisplay === 'top'?'left':componentConfig.moduleDisplay,
                        'padding-right':'18px',
                        'box-sizing':'border-box',
                        'padding-bottom':componentConfig.moduleDisplay == 'top'?'8px':0,
                        'float':componentConfig.moduleDisplay == 'top'?'inherit':'left'
                    });
                    /*content-样式*/
                    $('.p-right').css({
                        'margin-left':componentConfig.moduleDisplay == 'top'?0:componentConfig.titleWidth+'%',
                    })
                    /*组件-样式*/
                    $('.p-itemData .layui-form .layui-form-item.standard-type').css({
                        padding: componentConfig.displayType>2?'25px 40px':(componentConfig.displayType<2?'15px 40px':'20px 40px')
                    })
                } else {
                    location.href = srcUrl+'/error404'
                }
            },
            error:function(err){
                location.href = srcUrl+'/error404'
            }
        });
        console.log(form)
    });
}
$(function () {
    setTimeout(function() {
        $('.layui-form-select .layui-input').height($('.layui-input').height())
    }, 500);
    // settimeout(function()),500)
    function initCookies() {
        var cookie = document.cookie,
            items = cookie.split(";"),
            keys = {};
        items.forEach(function(item) {
            var kv = item.split('=');
            keys[$.trim(kv[0])] = $.trim(kv[1]);
        });
        return keys;
    }
    /*提交*/
    $('.sub-btn').click(function(){
        if(hasId){
            return
        }
        var formSubData = new FormData()
        console.log(fieldData)
        console.log(ruleFields)
        if(apiObj.name == 'form' && hrFormData.viewStatus !== 1){
            layer.msg('表单未发布，无法提交！')
            return
        }
        var isSub = false
        for(var i =0;i<ruleFields.length;i++){
            console.log(fieldData[ruleFields[i]])
            if(!fieldData[ruleFields[i]]){
                isSub = true
                layer.msg('请认真填写必填项！');
               return
            } else if(fieldData[ruleFields[i]] instanceof Array && fieldData[ruleFields[i]].length == 0){
                isSub = true
                layer.msg('请认真填写必填项！');
            }
        }
        if(!isSub){
            layer.msg('提交中', {icon: 16,shade: 0.3});
            for(var item in fieldData){
                if(fieldData[item] instanceof Array&&fieldData[item].length){
                    if(fieldData[item][0] instanceof Object){
                         for(var file in fieldData[item]) {
                            formSubData.append(item,fieldData[item][file])
                         }
                    } else {
                        for(var i = 0;i<fieldData[item].length;i++) {
                            formSubData.append(item,fieldData[item][i])
                         }
                    }
                } else if(fieldData[item]) {
                    formSubData.append(item,fieldData[item]||'')
                }
            }
            if(apiObj.name == 'letter'){
                formSubData.append('letterTypeId',formId)
            }
            if(!apiObj.saveApi){return}
            $.ajax({
                type:'POST',
                headers: {
                    'X-XSRF-TOKEN':initCookies()['X-XSRF-TOKEN'] || initCookies()['XSRF-TOKEN']
                },
                url: srcUrl+apiObj.saveApi,
                processData: false,   // jQuery不要去处理发送的数据
                contentType: false,   // jQuery不要去设置Content-Type请求头
                data:formSubData,
                success:function(res){
                    if(res.code == 200){
                        layer.closeAll();
                        if(res.data.processType == 2){
                            if(apiObj.name == 'form'){
                                location.href = res.data.prompt
                            } else {
                                $('.mode-letter').css('display','block');
                                $('.mode-letter .mode-message').html('提交成功，请记住信件的查询码及编号，以便后期查询');
                                $('.mode-letter .number').html(res.data.msg.number);
                                $('.mode-letter .queryPass').html(res.data.msg.queryPass);
                            }
                        } else {
                            $('.mode-result').css('display','block');
					        $('.mode-result .mode-message').html(res.data.prompt||(apiObj.name == 'form'?'提交成功，感谢您的参与！':'提交成功,感谢你的来信！'));
                        }
                    } else if(res.code == 100){
                        location.href = srcUrl+'/error404'
                    } else {
                        genCapatch(item.field+'Capatch')
                        layer.msg(res.message);
                    }
                },
                error: function (xhr, textStatus, errorThrown) {
                    console.log(errorThrown, 'error');
                }
            })
        }

    });
    /*关闭弹窗*/
    $('.mode-btn').click(function(){
        $('.mode-result').css('display','none')
        $('.mode-letter').css('display','none');
        setTimeout(function(){
            location.reload()
        }, 50);
    });
    $('.mode-close').click(function(){
        $('.mode-letter').css('display','none');
        $('.mode-result').css('display','none')
    });
});
/*
*   audioUpload 音频上传 imageUpload 单图上传 multiImageUpload 多图上传 fileUpload 附件上传 videoUpload 视频上传 cascade 级联选择 select 下拉 address 地址 city 城市
    checkbox 复选框 imageRadio 图片单选 imageCheckbox 图片多选  radio 单选 sex 性别 code 验证码 input 文本 datetimerange 日期区间 datetime 日期时间 birthday 生日 age 年龄
    mobile 手机号 phone 座机号 textarea 多行文本 email 邮箱  fax 传真  identity 身份证号 link 链接地址 realname 真实姓名
    divisionBar 分割条  divisionLine 分割线  textDesc 文本描述

    // 领导信箱 formTitle 信件标题 formDesc 信件内容 publicWill 公开意愿 
*/
/*获取类型type*/
function getType(type){
    switch(type){
        case 'radio':// 单选框
            return 'radio'
        case 'imageCheckbox':// 图片多选
            return 'imageCheckbox'
        case 'checkbox':// 多选
            return 'checkbox'
        case 'sex':// 性别
            return 'radio'
        case 'imageRadio':// 图片单选
            return 'imageRadio'
        case 'select':// 下拉
            return 'select'
        case 'code':// 验证码
            return 'code'
        case 'textarea':// 多行文本
            return 'textarea'
        case 'divisionBar':// 分割条
            return 'bar'
        case 'divisionLine':// 分割块
            return 'line'
        case 'textDesc':// 文字描述
            return 'desc'
        case 'imageUpload':// 单图上传
            return 'imageUpload'
        case 'multiImageUpload':// 多图上传
            return 'multiImageUpload'
        case 'fileUpload':// 附件上传
            return 'fileUpload'
        case 'videoUpload':// 视频上传
            return 'videoUpload'
        case 'audioUpload':// 音频上传
            return 'audioUpload'
        case 'cascade':// 级联
            return 'cascade'
        case 'city':// 城市
            return 'city'
        case 'address':// 地址
            return 'address'
        case 'datetime':// 日期
            return 'datetime'
        case 'birthday':// 生日
            return 'datetime'
        case 'datetimerange':// 日期区间
            return 'datetimerange'
        case 'publicWill':// -领导信箱-公开意愿
            return 'radio'
        case 'formDesc':// -领导信箱-信件内容
            return 'textarea'
        default:
            return 'input'
    }
};
var timer = null
/*刷新浏览量*/
function getRefreshView(id){
    timer = setInterval(function(){
        if(apiObj.viewApi){
            $.ajax({
                type:'GET',
                url: srcUrl+apiObj.viewApi+id,
                success:function(res){
                    clearInterval(timer)
                },
                error: function (xhr, textStatus, errorThrown) {
                    console.log(errorThrown, 'error');
                    clearInterval(timer)
                }
            })
        }
    },3000)
};
/*微信*/
var smartDataObj = {}
function showWeChat(data){
    var isOnlyWechat = data.isOnlyWechat
    var ua =navigator.userAgent.toLowerCase();
    var isWeixin = ua.indexOf('micromessenger') != -1;
    smartDataObj = data
    // 只有微信能打开
    if(isOnlyWechat&&!isWeixin){
        $('.t-nothing').show();
        $('.t-smart-wrap').hide();
        $('.nothing-img').html('<img class="t-img" src="'+srcUrl+'/common/qrcode/360?val='+data.url+'" />');
    } else {
        $('.t-smart-wrap').show();
        $('.t-nothing').hide();
        if(isWeixin){
            wechatSign();//请求微信接口-分享
        }
    }
}
function wechatSign() {
    var url = document.URL;
    if(url){
        if(vapiObj.shareApi){
            $.ajax({
                type:'GET',
                url: srcUrl+apiObj.shareApi,
                data:{"url": url},
                success:function(data){
                    if (data.code === 200) {
                        shareJs(data.data)
                    } else {
                        layer.msg(data.message);
                    }
                },
                error: function (xhr, textStatus, errorThrown) {
                    console.log(errorThrown, 'error');
                }
            })
        }
    }
}
/*微信分享*/
function shareJs(jssdk, options) {
    wx.config({
            debug: true,//是否开启调试功能，这里关闭！
            appId: jssdk.appId,//appid
            timestamp: parseInt(jssdk.timestamp), //时间戳
            nonceStr: jssdk.nonceStr, //生成签名的随机字符串
            signature: jssdk.signature,//签名
            jsApiList: [
                    "onMenuShareTimeline",
                    "onMenuShareAppMessage"
            ]
    });
    var defaults = {
            title: smartDataObj.title,
            desc: smartDataObj.description,
            link: location.href, //分享页面地址,不能为空，这里可以传递参数！！！！！！！
            imgUrl: (srcUrl||'http://cm.ngrok2.xiaomiqiu.cn')+smartDataObj.shareLogoUrl, //分享是封面图片，不能为空
            success: function () { }, //分享成功触发
            cancel: function () { } //分享取消触发，需要时可以调用
    }
    // 合并对象，后面的替代前面的！
    options = Object.assign({}, defaults, options);
    console.log(options)
    wx.ready(function () {
            var thatopts = options;
            // 分享到朋友圈
            wx.onMenuShareTimeline({
                    title: thatopts.title, // 分享标题
                    desc: thatopts.desc, // 分享描述
                    link: thatopts.link, // 分享链接
                    imgUrl: thatopts.imgUrl, // 分享图标
                    success: function () {
                            // alert("成功");
                    },
                    cancel: function () {
                            // alert("失败")
                    }
            });
            // 分享给朋友
            wx.onMenuShareAppMessage({
                    title: thatopts.title, // 分享标题
                    desc: thatopts.desc, // 分享描述
                    link: thatopts.link, // 分享链接
                    imgUrl: thatopts.imgUrl, // 分享图标
                    success: function () {
                            // alert("成功");
                    },
                    cancel: function () {
                            // alert("失败")
                    }
            });
    });
}
/**
 * 初始化始数据 接口对接 城市信息
 */
function getAreaData () {
    $.ajax({
        type:'GET',
        url: srcUrl+"/area/tree",
        success: function(res){
            areaList =  res.data
            var name = ''
            var codeVal = ''
            // 地址或城市-默认值
            var loop = function (arr,code,status){
                if(code){
                    for(var a = 0;a<arr.length;a++){
                        if(code == arr[a].areaCode || code == arr[a].id){
                            name = arr[a].areaName
                            codeVal = arr[a].areaCode
                        }
                        if(arr[a].children){
                            loop(arr[a].children,code)
                        }
                    }
                } else {
                    name = ''
                    codeVal = ''
                }
                return status?codeVal:name
            }
            // cityCode provinceCode areaCode
            if(areaItems.length){
                for(var i = 0;i<areaItems.length;i++){
                    var label = ''
                    if(areaItems[i].type != 'address'){
                        label = loop(res.data,areaItems[i].value.provinceCode)+'-'+loop(res.data,areaItems[i].value.cityCode)
                    } else {
                        label = loop(res.data,areaItems[i].value.provinceCode)+'-'+loop(res.data,areaItems[i].value.cityCode)+'-'+loop(res.data,areaItems[i].value.areaCode)
                        $("#"+areaItems[i].name+' .t-input').val(areaItems[i].value.address)
                        fieldData[areaItems[i].name] = areaItems[i].value.address
                    }
                    $("#"+ areaItems[i].name + 'Area ' + '.select-item').remove()
                    $("#"+areaItems[i].name+'Code').text(label)
                    getFormValue(areaItems[i].name,areaItems[i].type,{value:{
                        areaA:loop(res.data,areaItems[i].value.provinceCode,true),
                        areaB:loop(res.data,areaItems[i].value.cityCode,true),
                        areaC:loop(res.data,areaItems[i].value.areaCode,true)
                    },names:label.split('-')})
                }
            }
        }
    })
}
