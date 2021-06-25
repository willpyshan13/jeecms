
var base = localStorage.getItem('base') || '';

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

var myMessage = new MyMessage.message({
	    /*默认参数，下面为默认项*/
	    iconFontSize: "26px", //图标大小,默认为20px
	    messageFontSize: "20px", //信息字体大小,默认为12px
	    showTime: 2000, //消失时间,默认为3000
	    align: "center", //显示的位置类型center,right,left
	    positions: { //放置信息距离周边的距离,默认为10px
	        top: "10px",
	        bottom: "10px",
	        right: "10px",
					left: "10px",
	    },
		color:'#333',
	    message: "这是一条消息", //消息内容,默认为"这是一条消息"
	    type: "normal", //消息的类型，还有success,error,warning等，默认为normal
	})

	var getUrlArg = function(name) {
		var url = window.location.search;
    // 正则筛选地址栏
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)")
    // 匹配目标参数
    var result = url.substr(1).match(reg);
    //返回参数值
    return result ? decodeURIComponent(result[2]) : null;
	}
	// base = 'http://pengshansheng.51vip.biz'
	var api = {
		POST:function(url,data,callback,type) {
			$.ajax({
				url: base+url,
				method: 'POST',
				data:type=='form'?data:JSON.stringify(data),
				headers:{
					'JEECMS-Auth-Token':localStorage.getItem('JEECMS-Auth-Token'),
					'Redirect-Header':false,
					'X-XSRF-TOKEN':initCookies()['X-XSRF-TOKEN'] || initCookies()['XSRF-TOKEN'],
					'Content-Type':'application/json'
				},
				success: function (data) {
					callback(data)
				},
				error: function (xhr, textStatus, errorThrown) {
					myMessage.add(errorThrown, 'error');
				}
			})
		},
		GET:function(url,data,callback) {
			$.ajax({
				url: base+url,
				method: 'GET',
				data:data,
				headers:{'JEECMS-Auth-Token':localStorage.getItem('JEECMS-Auth-Token'),'Redirect-Header':false,'Content-Type':'application/json'},
				success: function (result) {
					callback(result)
				},
				error: function (xhr, textStatus, errorThrown) {
					myMessage.add(errorThrown, 'error');
				}
			})
		},
		PUT:function(url,data,callback) {
			$.ajax({
				url: base+url,
				method: 'PUT',
				data:JSON.stringify(data),
				headers:{'JEECMS-Auth-Token':localStorage.getItem('JEECMS-Auth-Token'),'Redirect-Header':false,'X-XSRF-TOKEN':initCookies()['X-XSRF-TOKEN'] || initCookies()['XSRF-TOKEN'],'Content-Type':'application/json'},
				success: function (result) {
					callback(result)
				},
				error: function (xhr, textStatus, errorThrown) {
					myMessage.add(errorThrown, 'error');
				}
			})
		},
		DELETE:function(url,data,callback) {
			$.ajax({
				url: base+url+'/delete',
				method: 'POST',
				data:JSON.stringify(data),
				headers:{'JEECMS-Auth-Token':localStorage.getItem('JEECMS-Auth-Token'),'Redirect-Header':false,'X-XSRF-TOKEN':initCookies()['X-XSRF-TOKEN'] || initCookies()['XSRF-TOKEN'],'Content-Type':'application/json'},
				success: function (result) {
					callback(result)
				},
				error: function (xhr, textStatus, errorThrown) {
					myMessage.add(errorThrown, 'error');
				}
			})
		}
	}

	var	desEncrypt = function(str){
		var status = true

		/**服务端定的公钥*/
		var pubkeyHex = "04AF0FCC45059AA342221352E5268614F2FF7A430497B156C0DEE6E751AB44E4957E9E69299E2CD38E25985B7BD34E0E7BBA683DE4725A6A8CD07E19BFF8BEF44D";
		var encryptData='';
		if(!status){
			$.ajax({
				url: base+"/cmsmanager/config/global/isSmEncrypt",
				async: false,
				data: {},
				success: function (result) {
					if (result.data === true) {
						var smutil = new SMutil();
						encryptData = smutil.sm2encrypt(str, pubkeyHex);
					} else {
						var cryptoKey = CryptoJS.enc.Utf8.parse('WfJTKO9S4eLkrPz2JKrAnzdb');
						var cryptoIv = CryptoJS.enc.Utf8.parse('D076D35C'.substr(0, 8));
						var encodeStr = CryptoJS.TripleDES.encrypt(str, cryptoKey, {
							iv: cryptoIv,
							mode: CryptoJS.mode.CBC,
							padding: CryptoJS.pad.Pkcs7
						});
						encryptData = encodeStr.toString();
					}
				}
			});
		} else {
			var cryptoKey = CryptoJS.enc.Utf8.parse('WfJTKO9S4eLkrPz2JKrAnzdb');
			var cryptoIv = CryptoJS.enc.Utf8.parse('D076D35C'.substr(0, 8));
			var encodeStr = CryptoJS.TripleDES.encrypt(str, cryptoKey, {
				iv: cryptoIv,
				mode: CryptoJS.mode.CBC,
				padding: CryptoJS.pad.Pkcs7
			});
			encryptData = encodeStr.toString();
		}
		return encryptData
	}