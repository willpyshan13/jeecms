var open = {};
//存储打赏的钱数
var howMuch = 0;
//判断现在正在支付的是内容还是打赏
var flagReading = null;
var aliPayFrom = null
// 控制选择什么支付
var money1 = '';
//判断是否登录
var isLo = false;
var sessionId = ''
//可用余额
var balance = 0
//判断是否购买了
var id = null,
    payPrice = null,
    realPayRead = null,
    realPayPraise = null
var flag = false
var errorNumber = 0
var Max = 0
var Min = 0
var remember = false
var record = 0
//判断是否开启了自定义金额
var priceCustomize = false
//判断用户是否登录
var timer = null;
var timer2 = null;
api.GET('/member/memberinfo', {}, function (result) {
    if (result.code === 200) {
        isLo = true
        allText();
        getAssetDisplayData();
    }
    mBank()
})

function mBank() {
    if (isLo) {
        $('.mBank').removeClass('none')
    } else {
        $('.mBank').addClass('none')
    }

}
var orderID1 = 1;
var orderID2 = 1;
var base2 = ''
function getData1(id1, payPrice1, realPayRead1, realPayPraise1,base) {
    base2 = base
    //id 内容id 
    id = id1
    payPrice = payPrice1 //payPrice付费阅读的价格  
    realPayRead = realPayRead1 //realPayRead 是否开启付费阅读  
    realPayPraise = realPayPraise1 //realPayPraise是否开启赞赏
    //先判断是否开启付费阅读 
    if (realPayRead === 'true') {
        realPayReadTrue()
    } else if (realPayPraise === 'true') {
        realPayPraiseTrue()
    }

}
//开启付费阅读调用的函数
function realPayReadTrue() {
    allText()
    setTimeout(function(){
        if (record === 0) {
            var paidStr = $('.context').html()
            payPrice = returnFloat(payPrice)
            paidStr = "<div style='position:relative'>" + paidStr + "<span class='paidMask'></span></div>"
            var paidStr1 = "<p class='paidContent'>本文需要付费才能阅读全部内容</p><button id='paidButton1'>￥" + payPrice + " 阅读全文</button>"
            paidStr = paidStr + paidStr1
            readMessage(1, paidStr)
        } else {
            realPayPraiseTrue()
        }
    },1000)
}
//开启了赞赏功能
function realPayPraiseTrue() {
    if (realPayPraise === 'true') {
        allText()
        setTimeout(function(){
            var paidStr = $('.context').html()
            var paidStr1 = "<p class='paidContent paidButton' id='paidButton2'><i class='iconfont iconzanshang2'></i>赞赏</p>"
            paidStr = paidStr + paidStr1
            readMessage(2, paidStr)
        },1000)
    }
}
//获取图片列表信息
function readMessage(num, paidStr) {
    api.GET('/payIncomDetail/headImage/list', {
        contentId: id,
        type: num
    }, function (result) {
        if (result.code === 200) {

            if (result.data.sum !== 0) {
                var str = "";
                if(result.data.showSum === 1){
                    for (var index = 0; index < result.data.headImages.length; index++) {
                        if(result.data.headImages[index]){
                            str = str + "<img class='NumberOfPayers' src=" + result.data.headImages[index] + " />"
                        }else{
                            str = str + "<img class='NumberOfPayers' src=" +base2+ "/images/touxiang.jpg />"
                        }
                    }
                }
                var stra = '';
                if(num===1){
                    stra='人阅读'
                }else if(num===2){
                    stra='人已赞赏'
                }
                if (result.data.showSum === 1) {
                    str = "<div class='paidLine'><span></span><b><b class='paidRed'>" + result.data.sum + "</b>"+stra+"</b><span></span></div><div class='paidImg'>" + str + "</div>"
                } else {
                   
                }
                if (result.data.sum > 20 && result.data.showSum === 1) {
                    str = str + "<div class='expandAll'><span>展开全部<span class='transrouter'><i class='layui-icon close pointer layui-icon-prev'></i></span></span></div>"
                }
                paidStr = paidStr + str
                $('.context').html(paidStr)
                //点击展开全部
                $('.expandAll span').on('click', function () {
                    flag = !flag
                    if (flag) {
                        $('.paidImg').css('max-height', '10000px')
                        $('.transrouter').addClass('transrouterT')
                    } else {
                        $('.paidImg').css('max-height', '110px')
                        $('.transrouter').removeClass('transrouterT')
                    }
                })
                if (num === 1) {
                    // 点击阅读付费
                    $('#paidButton1').on('click', function () {
                        flagReading = 'nr'
                        if (isLo) {
                            var obj = {}
                            obj.contentId = parseInt(id)
                            obj.type = 1
                            api.POST('/pay/order/onlineOrder', obj, function (result) {
                                if (result.code === 200) {
                                    clearTimeout(timer2)
                                    timer2 = setTimeout(function () {
                                        $('#Refresh3').removeClass('none')
                                        $('#Refresh2').removeClass('none')
                                        $('#Refresh1').removeClass('none')
                                    }, 7200000)
                                    orderID1 = result.data.id
                                    
                                    if (open.wechatOpen) {
                                        payType('wx', 1)
                                    }else if (open.aliPayOpen) {
                                        payType('zfb', 1)
                                    }else{
                                        $('.PopupWindow').eq(0).removeClass('none')
                                    }
                                }else{
                                    myMessage.add(result.data.originalMessage, 'warning');
                                }
                            })
                        } else {
                            $('.PopupWindow').eq(4).removeClass('none')
                        }
                    })
                } else {
                    $('#paidButton2').on('click', buttonTwo)
                }
            } else {
                $('.context').html(paidStr)
                if (num === 1) {
                    flagReading = 'nr'
                    // 点击阅读付费
                    $('#paidButton1').on('click', function () {
                        if (isLo) {
                            var obj = {}
                            obj.contentId = parseInt(id)
                            obj.type = 1
                            api.POST('/pay/order/onlineOrder', obj, function (result) {
                                if (result.code === 200) {
                                    clearTimeout(timer2)
                                    timer2 = setTimeout(function () {
                                        $('#Refresh3').removeClass('none')
                                        $('#Refresh2').removeClass('none')
                                        $('#Refresh1').removeClass('none')
                                    }, 7200000)
                                    orderID1 = result.data.id
                                   
                                    if (open.wechatOpen) {
                                        payType('wx', 1)
                                    }else if (open.aliPayOpen) {
                                        payType('zfb', 1)
                                    }else{
                                        $('.PopupWindow').eq(0).removeClass('none')
                                    }
                                    
                                }else{
                                    myMessage.add(result.data.originalMessage, 'warning');
                                    
                                }
                            })
                        } else {
                            $('.PopupWindow').eq(4).removeClass('none')
                        }
                    })
                } else {
                    $('#paidButton2').on('click', buttonTwo)
                }
            }
        }
    })
}
// 点击阅读的提交
$('#Submit1').on('click', function () {
    //先支付阅读弹窗
    var objData = {
        orderId: parseInt(orderID1),
        payType: 101,
        payClient: 1,
        paySource: 1
    }
    $('#Submit1').attr("disabled",true);
    api.POST('/pay', objData, function (result) {
        if (result.code === 200) {
        $('#Submit2').attr("disabled",false);
            loop()
        }else{
            myMessage.add(result.data.originalMessage, 'warning');
        }
    })
})
// 点击赞赏
function buttonTwo() {
    $('#Submit1').attr('disabled', false)
    borderColor()
    flagReading = 'zs'
    api.GET('/payIncomDetail/config/view', {}, function (result) {
        if (result.code === 200) {
            var str = "",
                str1 = "",
                str2 = ""
            if (result.data.hasLogin === 1) {
                if (isLo) {
                    $('.PopupWindow').eq(1).removeClass('none')
                    //开启自定义
                    if (result.data.priceCustomize === 0) {
                        $('#priceCustomize').addClass('none')
                        priceCustomize = false
                    } else {
                        priceCustomize = true
                    }
                    for (var index = 0; index < result.data.price.length; index++) {
                        str1 = str1 + "<li><p>￥<span>" + result.data.price[index] + "</span></p></li>"
                    }
                } else {
                    
                    $('.PopupWindow').eq(4).removeClass('none')
                }
            } else {
                $('.PopupWindow').eq(1).removeClass('none')
                //开启自定义
                if (result.data.priceCustomize === 0) {
                    $('#priceCustomize').addClass('none')
                    priceCustomize = false
                } else {
                    priceCustomize = true
                }
                for (var index = 0; index < result.data.price.length; index++) {
                    str1 = str1 + "<li><p>￥<span>" + result.data.price[index] + "</span></p></li>"
                }
            }
            $('.Appreciation ul').html(str1)
            $('.Appreciation ul li').eq(0).addClass('borderBlue').siblings().removeClass('borderBlue')
            howMuch = $('.Appreciation ul li').eq(0).find('span').html()
            howMuch = returnFloat(howMuch)
            $('#priceCustomize').val('')
            $('#priceCustomize').removeClass('borderBlue')
            $('.symbol').addClass('none')
            $('#Submit2').html(`确定支付￥${howMuch}`)
            disabledd()
            AppreciationLi();
            borderColor()
            Max = result.data.priceMax
            Min = result.data.priceMin
        }
    })
}

function disabledd() {
    if (parseFloat(balance) < parseFloat(howMuch)) {
        $('.mBank').attr('disabled', true)
    } else {
        $('.mBank').attr('disabled', false)
    }
}
$('.appreciate').on('click', function () {
    $('.PopupWindow').eq(1).removeClass('none')
})
borderColor()
$('.buttonAll button').on('click', function () {
    $(this).addClass('borderBlue')
    $(this).siblings().removeClass('borderBlue')
    clearInterval(timer)
    $('#Submit1').attr('disabled', false)
    if ($(this).index() === 0) {
        money1 = 'wx'
        if (flagReading === 'nr') {
            payType('wx', 1)
        }
        $('.wxQ').removeClass('none')
        $('.wxQ').siblings().addClass('none')

    } else if ($(this).index() === 1) {
        money1 = 'zfb'
        $('.zfbQ').removeClass('none')
        $('.zfbQ').siblings().addClass('none')
        if (flagReading === 'nr') {
            payType('zfb', 1)
        }
    } else if ($(this).index() === 2) {
        money1 = null
        $('.moneyQ').removeClass('none')
        $('.moneyQ').siblings().addClass('none')
    }
})

//点击重新下单
$('#Refresh3').on('click', function () {
    var objj = {contentId:parseInt(id),type:2,amount:howMuch}
    if (money1 === null) {
       

    } else if (money1 === 'wx') {
        onlineO(objj,2,2)
    } else if (money1 === 'zfb') {
        onlineO(objj,1,2)
    }
    
})
$('#Refresh2').on('click', function () {
    var objj = {contentId:parseInt(id),type:1}
    onlineO(objj,1,1)
})
$('#Refresh1').on('click', function () {
    var objj = {contentId:parseInt(id),type:1}
    onlineO(objj,2,1)
})
function onlineO(data,numO,numA){
    api.GET('/pay/order/onlineOrder',data,function(result){
        if(result.code===200){
            clearTimeout(timer2)
            timer2 = setTimeout(function () {
                $('#Refresh3').removeClass('none')
                $('#Refresh2').removeClass('none')
                $('#Refresh1').removeClass('none')
            }, 7200000)
            orderID1 = result.data.id
            var objData = {orderId:orderID1,payType:numO,payClient:1,paySource:1}
            api.POST('/pay', objData, function (result) {
                if (result.code === 200) {
                    if(numA===1){
                        if (numO === 1) {
                            loop()
                            aliPayFrom = result.data.aliPayFrom
                            qrcode2.clear(); // 清除代码
                            qrcode2.makeCode(result.data.aliPayQrCode); // 生成另外一个二维码
                        } else if (numO === 2) {
                            loop()
                            qrcode1.clear(); // 清除代码
                            qrcode1.makeCode(result.data.wxPayQrCode);
                        }
                    }else{
                        if (numO === 1) {
                            loop()
                            aliPayFrom = result.data.aliPayFrom
                            qrcode3.clear(); // 清除代码
                            qrcode3.makeCode(result.data.aliPayQrCode); // 生成另外一个二维码
                        } else if (numO === 2) {
                            loop()
                            qrcode3.clear(); // 清除代码
                            qrcode3.makeCode(result.data.wxPayQrCode);
                        }
                    }
                }else{
                    myMessage.add(result.data.originalMessage, 'warning');
                }
            })
        }   
    })
}
function closeRefresh() {
    $('#Refresh3').addClass('none')
    $('#Refresh2').addClass('none')
    $('#Refresh1').addClass('none')
}
//轮训订单接口 判断是否
function loop() {
    clearInterval(timer)
    // $('#Submit1').attr("disabled",true);
    // $('#Submit2').attr("disabled",true);
    timer = setInterval(function () {
        api.GET('/pay/order/query', {
            orderId: orderID1
        }, function (result) {
            if (result.code === 200) {
                if (result.data === true) {
                    clearInterval(timer)
                    getAssetDisplayData()
                    //余额支付
                    if (flagReading === 'nr') {
                        setTimeout(function(){
                            realPayPraiseTrue()
                        },500)
                        if(money1 === null){
                            $('.PopupWindow').eq(0).addClass('none')
                            $('.PopupWindow').eq(2).removeClass('none')
                            lastTime()
                            paidStrr()
                        }else{
                            $('.PopupWindow').eq(0).addClass('none')
                            $('.PopupWindow').eq(2).removeClass('none')
                            lastTime()
                            paidStrr()
                        }
                        allText()
                    } else {
                        clearInterval(timer)
                        if(money1 === null){
                            $('.PopupWindow').eq(1).addClass('none')
                            $('#priceCustomize').removeClass('priceCustomize2')
                            $('#priceCustomize').val('')
                            $('#priceCustomize').attr('placeholder','自定义')
                            $('.PopupWindow').eq(2).removeClass('none')
                            $('#priceCustomize').removeClass('fontSize2')
                            $('.symbol').addClass('none')
                            lastTime()
                            imageList()
                        }else{
                            $('.PopupWindow').eq(1).addClass('none')
                            $('.PopupWindow').eq(3).addClass('none')
                            $('.PopupWindow').eq(2).removeClass('none')
                            lastTime()
                            imageList()
                            $('#priceCustomize').removeClass('priceCustomize2')
                            $('#priceCustomize').val('')
                            $('#priceCustomize').attr('placeholder','自定义')
                            $('.symbol').addClass('none')
                            $('#priceCustomize').removeClass('fontSize2')
                        }
                    }
                    // $('#Submit2').attr("disabled",false);
                    // $('#Submit1').attr("disabled",true);
                }
            }
        })
    }, 1000)
}
function paidStrr(){
    var paidStr = $('.context').html()
    var paidStr1 = "<p class='paidContent paidButton' id='paidButton2'>赞赏</p>"
    paidStr = paidStr + paidStr1
    readMessage(2, paidStr)
}

// obj.type = 2
// api.POST('http://192.168.0.185:8080/pay/order',obj,function(result){
//     orderID2 = result.id
// })
// 调用支付二维码
function payType(str, num) {
    var payType2 = 1
    var data = {}
    //阅读的付费
    if (str === 'wx') {
        payType2 = 2
    } else if (str === 'zfb') {
        payType2 = payType2
        data.returnUrl = window.location.href
    } else {

    }
    data.orderId = parseInt(orderID1)
    data.payType = payType2
    data.payClient = 1
    data.paySource = 1
    api.POST('/pay', data, function (result) {
        if (result.code === 200) {
            $('.PopupWindow').eq(0).removeClass('none')
            if (payType2 === 1) {
                loop()
                aliPayFrom = result.data.aliPayFrom
                qrcode2.clear(); // 清除代码
                qrcode2.makeCode(result.data.aliPayQrCode); // 生成另外一个二维码
            } else if (payType2 === 2) {
                loop()
                qrcode1.clear(); // 清除代码
                qrcode1.makeCode(result.data.wxPayQrCode);
            }
        }else{
            myMessage.add(result.data.originalMessage, 'warning');
        }

    })
}
$('#loginNew').on('click', function () {
    $('body').append(aliPayFrom)
})

//微信二维码
var qrcode1 = new QRCode("wxQroucd", {
    text: "http://www.runoob.com",
    width: 123,
    height: 123,
    colorDark: "#000000",
    colorLight: "#ffffff",
    correctLevel: QRCode.CorrectLevel.H
});
// 支付宝二维码、
var qrcode2 = new QRCode("zfbQroucd", {
    text: "http://www.w3cschool.cc",
    width: 123,
    height: 123,
    colorDark: "#000000",
    colorLight: "#ffffff",
    correctLevel: QRCode.CorrectLevel.H
});
// 支付宝二维码、
var qrcode3 = new QRCode("qrcodepop", {
    text: "http://www.w3cschool.cc",
    width: 123,
    height: 123,
    colorDark: "#000000",
    colorLight: "#ffffff",
    correctLevel: QRCode.CorrectLevel.H
});

//回车登录
// $('body').keydown(function (event) {
//     if (event.keyCode == 13) {
//         $('.alogin').click();
//     }
// });

$(function () {
    genCapatch();
    $("#getCapatchImg").on("click", function (event) {
        genCapatch();
    });
});

//获取验证码
function genCapatch() {
    api.GET('/common/kaptcha', {}, function (result) {
        $('#capatchImg').attr('src', "data:image/png;base64," + result.data.img)
        sessionId = result.data.sessionId
    })
}

function initCookies() {
    var cookie = document.cookie,
        items = cookie.split(";"),
        keys = {};
    items.forEach(function (item) {
        var kv = item.split('=');
        keys[$.trim(kv[0])] = $.trim(kv[1]);
    });
    return keys;
}

$('#remember').on('click', function () {
    remember = !remember
    if (remember) {
        genCapatch()
        $('.code').removeClass('none')
    } else {
        $('.code').addClass('none')
    }
})

function getLogin() {
    if (!$('#identity').val() || !$('#desStr').val()) {
        myMessage.add('用户名或密码不能为空', 'warning');
        return
    }
    if (remember) {
        if ($('#captcha').val() == '') {
            myMessage.add('请输入验证码', 'warning');
            return
        }
    }
    var obj = {};
    obj.identity = $('#identity').val();
    var obj2 = {};
    obj2.pStr = $('#desStr').val();
    var password = JSON.stringify(obj2);
    obj.desStr = desEncrypt(password);
    obj.captcha = $('#captcha').val();
    obj.rememberMe = $('#remember').is(':checked');
    if (sessionId) {
        obj.sessionId = sessionId
    }

    $.ajax({
        type: "POST",
        url: window.location.protocol + '//' + window.location.host + "/login",
        headers: {
            'X-XSRF-TOKEN': initCookies()['X-XSRF-TOKEN'] || initCookies()['XSRF-TOKEN']
        },
        data: obj,
        success: function (result) {
            if (result.code == 200) {
                //登录成功
                if (result.data['JEECMS-Auth-Token']) {
                    myMessage.add('登录成功', 'success');
                    localStorage.setItem('JEECMS-Auth-Token', result.data['JEECMS-Auth-Token']);
                    isLo = true;
                    mBank()
                    
                    getAssetDisplayData()
                    $('.PopupWindow').eq(4).addClass('none')
                    if(flagReading=='zs'){
                        $('#paidButton2').click()
                    }else{
                        allText(true);
                    }
                } else if (result.nextNeedCaptcha) {
                    genCapatch();
                    $('.code').css('display', 'block');
                    $('.fast-login').css('bottom', '130px');
                    myMessage.add(result.data.originalMessage, 'error');
                } else {
                    genCapatch();
                    myMessage.add(result.data.originalMessage, 'error');
                }

            } else if (result.code == 14504) {
                genCapatch();
                myMessage.add(result.data.originalMessage, 'warning');
            } else {
                genCapatch();
                errorNumber += 1
                if (errorNumber >= 3) {
                    $('.code').css('display', 'block');
                    $('.fast-login').css('bottom', '130px');
                }
                myMessage.add(result.data.originalMessage, 'error');
            }
        }
    });
}

$('#Submit3').on('click', function () {
    window.location.href = window.location.protocol + '//' + window.location.host + '/register.htm'
})

// 获取全部文本
function allText(flag) {
    api.GET('/content/txts', {contentId:id}, function (result) {
        if (result.code === 200) {
            if (Object.keys(result.data).length !== 0) {
                //fuguo费用或者是作者登录
                var i = 0;
                Object.keys(result.data).forEach((key) => {
                    i++;
                    if(i==1){
                        $('.context').html(result.data[key]) // foo
                    }
                })
                record = 1
            } else {
                //未付费
                record = 0
            }
            if(flag==true){
                if(record = 1){
                    paidStrr();
                }else{
                    $('#paidButton1').click()
                }
            }
        }
    })
}

function qqLogin() {
    location.href = '${base}/thirdParty/qq/pc'
}

function weiboLogin() {
    location.href = '${base}/thirdParty/sina/pc'
}

function weixinLogin() {
    location.href = '${base}/thirdParty/wechat/pc'
}
$('#priceCustomize').on('blur', function () {
    $('.Appreciation ul li').removeClass('borderBlue');
    var strr = $(this).val();
    strr = parseFloat(strr)
    if (strr <= Max && strr >= Min) {
        $(this).val(strr)
        strr = returnFloat(strr)
        howMuch = strr
    } else if (strr > Max) {
        strr = Max
        $(this).val(strr)
        strr =returnFloat(strr)
        howMuch = strr
    } else {
        strr = Min
        $(this).val(strr)
        strr = returnFloat(strr)
        howMuch = strr
    }
    if($(this).val()!=''){
        $('#priceCustomize').addClass('borderBlue')
        $('.symbol').removeClass('none')
        $(this).attr('placeholder','00.00')
    }else{
        $('#priceCustomize').removeClass('borderBlue')
        $('.symbol').removeClass('none')
        $(this).attr('placeholder','自定义')
        $(this).removeClass('priceCustomize2')
        $('#priceCustomize').removeClass('fontSize2')
    }
    disabledd()
    $('#Submit2').html(`确定支付￥${howMuch}`)
})
$('#priceCustomize').on('input',function(){
    if($(this).val()!=''){
        $('#priceCustomize').addClass('borderBlue')
        $('.symbol').removeClass('none')
        var str = $(this).val()
        var strAry = str.split('.')
        if(strAry.length>1){
            if(strAry[1].length>2){
                $(this).val(Number(str).toFixed(2))
            }
        } 
    }else{
        $('#priceCustomize').removeClass('borderBlue')
        $('.symbol').removeClass('none')
        $(this).attr('placeholder','00.00')
    }
})
$('#priceCustomize').on('focus',function(){
    $('.Appreciation ul li').removeClass('borderBlue');
    $(this).addClass('priceCustomize2')
    $('.symbol').removeClass('none')
    $(this).attr('placeholder','00.00')
    $('#priceCustomize').addClass('fontSize2')
})
//点击阅读的关闭
$('#close1').on('click', function () {
    closeRefresh()
    clearInterval(timer)
    borderColor()
    $('.wxQ').removeClass('none')
    $('.wxQ').siblings().addClass('none')
    $('.PopupWindow').eq(0).addClass('none')
})

//点击赞赏的关闭
$('#close2').on('click', function () {
    $('#Submit2').attr("disabled",false);
    closeRefresh()
    clearInterval(timer)
    borderColor()
    $('.PopupWindow').eq(1).addClass('none')
    $('#priceCustomize').removeClass('priceCustomize2')
    $('#priceCustomize').val('')
    $('#priceCustomize').attr('placeholder','自定义')
    $('.symbol').addClass('none')
    $('#priceCustomize').removeClass('fontSize2')
})
//点击付费成功的关闭
$('#close3').on('click', function () {
    closeRefresh()
    clearInterval(ttt)
    clearInterval(timer)
    borderColor()
    $('.PopupWindow').eq(2).addClass('none')
})
//点击赞赏的支付按钮
$('#Submit2').on('click', function () {
    $('.deterMoney span').html('￥'+howMuch)
    //先关闭赞赏弹窗
    $('#Submit2').attr("disabled",true);
    if (money1 === null) {
        if(parseFloat(howMuch) > parseFloat(balance)){
            myMessage.add('余额不足', 'warning');
            $('#Submit2').attr("disabled",false);
            return
        }
        order()

    } else if (money1 === 'wx') {
        order()
        $('#qrcode1').html('微信扫码')
    } else if (money1 === 'zfb') {
        order()
        $('#qrcode1').html('支付宝扫码')
    }
})
// 调用订单接口
function order() {
    var obj = {}
    obj.contentId = parseInt(id)
    obj.type = 2
    obj.amount = howMuch
    if (money1 === '') {
        $('#Submit2').attr("disabled",false);
        return
    }
    api.POST('/pay/order/onlineOrder', obj, function (result) {
        
        if (result.code === 200) {
            clearTimeout(timer2)
            timer2 = setTimeout(function () {
                $('#Refresh3').removeClass('none')
                $('#Refresh2').removeClass('none')
                $('#Refresh1').removeClass('none')
            }, 7200000)
            orderID1 = result.data.id
            var payObj = {}
            payObj.payClient = 1
            payObj.paySource = 1
            payObj.orderId = orderID1
            if (money1 === 'wx') {
                payObj.payType = 2
            } else if (money1 === 'zfb') {
                payObj.payType = 1
                payObj.returnUrl = window.location.href
            } else {
                payObj.payType = 101
            }
            api.POST('/pay', payObj, function (result) {
                $('#Submit2').attr("disabled",false);
                if (result.code === 200) {
                    if (money1 === null) {
                        loop()
                    } else {
                        $('.PopupWindow').eq(1).addClass('none')
                        $('.PopupWindow').eq(3).removeClass('none')
                        clearInterval(timer)
                        loop()
                        //余额支付
                        qrcode3.clear(); // 清除代码
                        if (money1 === 'wx') {
                            qrcode3.makeCode(result.data.wxPayQrCode)
                        } else if (money1 === 'zfb') {
                            qrcode3.makeCode(result.data.aliPayQrCode)
                        }
                    }
                }else{
                    myMessage.add(result.data.originalMessage, 'warning');
                }
            })
        }else{
            $('#Submit2').attr("disabled",false);
        }
    })
}
var ttt = ''

function lastTime() {
    var timeout = 3
    clearInterval(ttt)
    if(flagReading === 'nr'){
        $('#succssMessage').html('支付成功')
    }else{
        $('#succssMessage').html('赞赏成功')
    }
    $('.paySuccess p').eq(1).html(timeout + '秒后关闭')
    ttt = setInterval(function () {
        timeout--;
        if (timeout == 0) {
            clearInterval(ttt)
            $('.PopupWindow').eq(2).addClass('none')
        }
        $('.paySuccess p').eq(1).html(timeout + '秒后关闭')
    }, 1000)
}
//点击二维码的关闭
$('#close4').on('click', function () {
    closeRefresh()
    clearInterval(timer)
    borderColor()
    $('.PopupWindow').eq(3).addClass('none')
})

function borderColor() {
    if (open.wechatOpen) {
        money1 = 'wx'
        $('.buttonAll:eq(0) button').eq(0).addClass('borderBlue')
        $('.buttonAll:eq(0) button').eq(0).siblings().removeClass('borderBlue')
        $('.buttonAll:eq(1) button').eq(0).addClass('borderBlue')
        $('.buttonAll:eq(1) button').eq(0).siblings().removeClass('borderBlue')
    } else if (open.aliPayOpen) {
        money1 = 'zfb'
        $('.buttonAll:eq(0) button').eq(0).addClass('borderBlue')
        $('.buttonAll:eq(0) button').eq(0).siblings().removeClass('borderBlue')
        $('.buttonAll:eq(1) button').eq(0).addClass('borderBlue')
        $('.buttonAll:eq(1) button').eq(0).siblings().removeClass('borderBlue')
    } else if (!$('.mBank').attr('disabled')) {
        money1 = null
        $('.buttonAll:eq(0) button').eq(0).addClass('borderBlue')
        $('.buttonAll:eq(0) button').eq(0).siblings().removeClass('borderBlue')
        $('.buttonAll:eq(1) button').eq(0).addClass('borderBlue')
        $('.buttonAll:eq(1) button').eq(0).siblings().removeClass('borderBlue')
    } else {
        money1 = '';
    }
}
//点击二维码的回退
$('#close5').on('click', function () {
    closeRefresh()
    clearInterval(timer)
    $('.PopupWindow').eq(3).addClass('none')
    $('.PopupWindow').eq(1).removeClass('none')
})
//点击登录的关闭
$('#close6').on('click', function () {
    borderColor()
    $('.PopupWindow').eq(4).addClass('none')
})

function AppreciationLi() {
    $('.Appreciation ul li').off('click')
    $('.Appreciation ul li').click('on', function () {
        howMuch = $(this).find('span').html()
        howMuch = returnFloat(howMuch)
        $('#Submit2').html(`确定支付￥${howMuch}`)
        disabledd()
        $('#priceCustomize').removeClass('borderBlue')
        $(this).addClass('borderBlue').siblings().removeClass('borderBlue')
        $('#priceCustomize').removeClass('priceCustomize2')
        $('#priceCustomize').val('')
        $('#priceCustomize').attr('placeholder','自定义')
        $('#priceCustomize').removeClass('fontSize2')
        $('.symbol').addClass('none')
    })
}
//获取用户的可用余额
function getAssetDisplayData() {
    api.GET('/member/memberAssetDisplay/getAssetDisplayData', {}, function (result) {
        if (result.code === 200) {
            if (result.data.balance) {
                balance = returnFloat(result.data.balance)
            } else {
                balance = 0
            }
            $('#balance1').html(balance)
            $('#balance2').html(balance)
            if (parseFloat(balance) < parseFloat(payPrice)) {
                $('.mBank').attr('disabled', true)
            } else {
                $('.mBank').attr('disabled', false)
            }
        } else {
            balance = 0
            $('#balance2').html(0)
            $('.mBank').attr('disabled', true)
        }
    })
}
//获取图片列表
function imageList() {
    api.GET('/payIncomDetail/headImage/list', {
        contentId: id,
        type: 2
    }, function (result) {
        if (result.code === 200) {
            var str = ''
            var str2 = ''
            var a = $('.paidLine>b').text()
            if(result.data.showSum===1){
                if(a.indexOf("赞赏")!== -1){
                    $('.paidLine>b').html("<b class='paidRed'>" + result.data.sum + "</b>人已赞赏")
                    
                }else{
                    $('.paidRed').html(result.data.sum)
                }
                for (var index = 0; index < result.data.headImages.length; index++) {
                    if(result.data.headImages[index]){
                        str = str + "<img class='NumberOfPayers' src=" + result.data.headImages[index] + " />"
                    }else{
                        str = str + "<img class='NumberOfPayers' src=" +base2+ "/images/touxiang.jpg />"
                    }
                }
            }else{
                $('.paidLine').addClass('none')
                $('.paidImg').addClass('none')
            }
            $('.expandAll').addClass('none')
            if (result.data.sum > 20 && $('.expandAll')) {
                str2 = "<div class='expandAll'><span>展开全部<span class='transrouter'><i class='layui-icon close pointer layui-icon-prev'></i></span></span></div>"
                $('.context').append(str2)
                $('.expandAll span').on('click', function () {
                    flag = !flag
                    if (flag) {
                        $('.paidImg').css('max-height', '10000px')
                        $('.transrouter').addClass('transrouterT')
                    } else {
                        $('.paidImg').css('max-height', '110px')
                        $('.transrouter').removeClass('transrouterT')
                    }
                })
            }
            $('.paidImg').html(str)
        }
    })
}
//判断是否开启了微信支付 支付宝支付
openPay()

function openPay() {
    api.GET('/payIncomDetail/config/view', {}, function (result) {
        if (result.code===200) {
            if(result.data.wechatOpen==0){
                open.wechatOpen = false
            }else{
                open.wechatOpen = true
            }
            if(result.data.aliPayOpen==0){
                open.aliPayOpen = false
            }else{
                open.aliPayOpen = true
            }
            if (!open.wechatOpen) {
                $('.buttonAll:eq('+0+') button').eq(0).addClass('none')
                $('.buttonAll:eq('+1+') button').eq(0).addClass('none')
                $('.wxQ').addClass('none')
            }
            if (!open.aliPayOpen) {
                $('.buttonAll:eq('+0+') button').eq(1).addClass('none')
                $('.buttonAll:eq('+1+') button').eq(1).addClass('none')
                $('.zfbQ').addClass('none')
            }
        }
    })
}
function returnFloat(value){
    var num2 = parseFloat(value)
    var ary = []
    if(isNaN(num2)){
        num2 = ''
    }else{
        num2 = num2.toString()
        if(num2.includes('.')){
            ary = num2.split('.')
            if(ary.length>=2){
                if(ary[1].length>=4){
                    ary[1] = ary[1].substring(0,4)
                    num2=ary.join('.')
                }
            }
        }
    }
    return num2
}