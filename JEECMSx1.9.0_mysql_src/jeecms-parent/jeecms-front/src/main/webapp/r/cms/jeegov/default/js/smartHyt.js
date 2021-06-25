
			var formData = new FormData();
			var subData = null
			var code;
			var sessionId = ''
			var base = localStorage.getItem('base') || '';
			
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
							left: "10px"
					},
					message: "这是一条消息", //消息内容,默认为"这是一条消息"
					type: "normal", //消息的类型，还有success,error,warning等，默认为normal
			})
			function wechatSign() {
				var url = document.URL;
				if(url){
					api.GET('/wechat/sign', {
						"url": url
					}, function(data) {
						if (data.code === 200) {
							shareJs(data.data)
						} else {
							myMessage.add(data.message, 'warning');
						}
					})
				}
			}
		
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
						title: "分享的标题",
						desc: "分享的描述",
						link: location.href, //分享页面地址,不能为空，这里可以传递参数！！！！！！！
						imgUrl: (base||'http://cm.ngrok2.xiaomiqiu.cn')+'${tag_bean.shareLogoUrl!}', //分享是封面图片，不能为空
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
			
			//通过文件名，返回文件的后缀名
			function fileType(name) {
					var nameArr = name.split(".");
					return nameArr[nameArr.length - 1].toLowerCase();
			};
			// 删除上传文件
			function delFile(e,name,i){
				formData['attr_'+name].value.splice(i,1);
				$(e.target).parent('.file-item').remove();
			};
			// 重置必填数据
			function resetFormDataRequired(name){
				for(var i in formData) {
					if(i.indexOf('other_') != -1 && i != name){
						formData[i].isRequired = false
					}
				}
			}
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
				var arr = ['',/^[\u4e00-\u9fa5]{0,}$/,/^[A-Za-z]+$/,/^[+-]?(0|([1-9]\d*))(\.\d+)?$/,/^[A-Za-z0-9]+/,/^[1-9]\d*$/,/^[\u4E00-\u9FA5A-Za-z0-9]+$/,/^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$/,/^1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\d{8}$/,/^((\d{3,4}-)|\d{3.4}-)?\d{7,8}$/,/^(1([38][0-9]|4[579]|5[0-3,5-9]|6[6]|7[0135678]|9[89])\d{8}||((\d{3,4}-)|\d{3.4}-)?\d{7,8})$/,/^[1-9]\d{5}[1-9]\d{3}((0\d)|(1[0-2]))(([0|1|2]\d)|3[0-1])\d{4}$/,/[1-9]\d{5}(?!\d)/];
				var s = arr[parseInt(type)||0]
				if(s&&s.test(val)){
					return true
				} else {
					return false
				}
			};
			function subForm(){
				var data = new FormData();
					data.append('questionnaireId','${tag_bean.id!}')
					var canSub = true
					for(var item in formData){
						if(formData[item].isRequired&&!formData[item].value){
							if($('.c-message-notice').length === 0||$('.c-message-notice')[$('.c-message-notice').length -1].style.display === 'none'){
								myMessage.add('请认真填写所有必填项', 'warning');
							}
							canSub = false
							return
						} else {
							if(formData[item].value instanceof Array&&formData[item].value.length){
								if(formData[item].value[0] instanceof Object){
									 for(var file in formData[item].value) {
										//  console.log(file)
									 	data.append(item,formData[item].value[file])
									 }
								} else {
									data.append(item,formData[item].value.join(','))
								}
							} else if(formData[item].value!==null) {
								data.append(item,formData[item].value||'')
							}
						}
					}
					if(canSub){
						var verification = "${tag_bean.isVerification?string('true','false')}";
						if(verification == 'true'){
							$('.mode-code').css('display','block');
								genCapatch();
								subData = data
						} else {
							if('${tag_bean.status!}' == 4){
								$('.mode-success').css('display','block')
							} else {
								submitData(data)
							}
						}
					}
			};
			function submitData(data){
				// console.log(data.get('attr_1262'));
				$.ajax({
						type: 'post',
						url: base+'/questionnaire',
						dataType: 'json',
						data: data,
						cache: false,
						processData: false,
						async: false,
						contentType:false,
						headers: {
								'JEECMS-Auth-Token': localStorage.getItem('JEECMS-Auth-Token'),
						},
						success: function (result) {
								if (result.code == 200) {
									var i = 3
									if('${tag_bean.processType!}' == 1){//显示文字信息
										$('.mode-result').css('display','block')
										$('.mode-message').html('${tag_bean.prompt!}'||'此次投票已经结束，感谢您的参与！')
										$('.mode-info').html((i--)+'秒后返回投票列表页')
										var timer = setInterval(function(){
											$('.mode-info').html((i--)+'秒后返回投票列表页')
										},1000)
										setTimeout(() => {
											clearInterval(timer)
											window.location.href = '[@cms_channel path='tptc' siteId='1'][#if tag_bean??]${base}${tag_bean.url!}[/#if][/@cms_channel]'
										}, 2000);
									} else if('${tag_bean.processType!}' == 2) {//跳转到指定页面
											window.location.href = '${base}${tag_bean.prompt!}'
									} else {//显示结果
										$('.mode-result').css('display','block')
										$('.mode-info').html((i--)+'秒后返回投票结果页')
										var timer = setInterval(function(){
											$('.mode-info').html((i--)+'秒后返回投票结果页')
										},1000)
										setTimeout(() => {
											clearInterval(timer)
											window.location.href = '${base}/interact-result.htm?id=${id!}'
											$('.mode-info').html('返回中...')
										}, 2000);
									}
									
								}else{
                  myMessage.add(result.message, 'error');
                }
								// result
						},
						error: function (xhr, textStatus, errorThrown) {
							myMessage.add(errorThrown, 'error');
						}
				});
			};
			function genCapatch() {
					$.ajax({
							url: base+"/common/kaptcha",
							data: {},
							success: function (result) {
								$('#getCapatchImg').html('<img class="t-img" id="capatchImg" src="'+"data:image/png;base64," + result.data.img+'" onclick="genCapatch()" />')
								sessionId=result.data.sessionId
							}
					});
			};
			function createCode(){
					code = '';
					var codeLength = 4;
					var codeV = $(".letter"); 
					var arr = new Array('a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r', 's','t','u','v','w','x','y','z'); 
					for(var i = 0; i < codeLength; i++){   
						var index = Math.floor(Math.random()*26);  
						code += arr[index]; 
					} 
					codeV.text(code);
				};
			function initContent(){
				var len = $('.t-cont-ques').length
					var obj = {}
					[#list tag_bean.subjects as sub]
					obj['attr_${sub.id!}'] = {
						value:null,
						isRequired:"${sub.isAnswer?string('true','false')}"=='true'?true:false
					};
          [#if sub.type == 1||sub.type == 4]
            [#list sub.option.options as c]
              var isDefault = '${c.isDefault?string("true","false")}'
              if(isDefault == 'true'){
								obj['attr_${sub.id!}'].value = '${c.id!}';
              }
            [/#list]
          [#elseif sub.type == 2]
          obj['attr_${sub.id!}'].value = []
          [#list sub.option.options as c]
              var isDefault = '${c.isDefault?string("true","false")}'
              if(isDefault == 'true'){
								obj['attr_${sub.id!}'].value.push('${c.id!}');
              }
            [/#list]
          [/#if]
					[#if sub.type == 5||sub.type == 1||sub.type == 2||sub.type == 4]
						[#list  sub.option.options as a]
						[#if a.isEemty??&&a.isEemty]
						obj['other_${a.id!}'] = {
							value:'',
							isRequired:false
						};
						[/#if]
						[/#list]
					[/#if]
					[#if sub.type == 5]
						var html = ''
						[#list sub.option.options as a]
						[#if a.children??&&a.children?size>0]
							html+='<div class="relative cascade-child child_${a.name!}"><i class="iconfont iconxialasanjiaofuhao select-icon"></i>'+
							'<select class="t-select t-w130 cascade-ques" data-name="${sub.id!}" data-level="1" placeholder="请选择">	[#list a.children as b]'+
							'<option class="cascade-option" value="${b.name!}" selected>${b.name!}</option>[/#list]<option class="cascade-option" value="" selected>请选择</option></select></div>'
							[#list a.children as v]
							[#if v.children??&&v.children?size>0]
								html+='<div class="relative cascade-children children_${v.name!}"><i class="iconfont iconxialasanjiaofuhao select-icon"></i>'+
								'<select class="t-select t-w130 cascade-ques" data-name="${sub.id!}" data-level="2" placeholder="请选择">	[#list v.children as b]'+
								'<option class="cascade-option" value="${b.name!}" selected>${b.name!}</option>[/#list]<option class="cascade-option" value="" selected>请选择</option></select></div>'
								[#list v.children as c]
								[#if c.children??&&c.children?size>0]
									html+='<div class="relative cascade-childrens childrens_${c.name!}"><i class="iconfont iconxialasanjiaofuhao select-icon"></i>'+
									'<select class="t-select t-w130 cascade-ques" data-name="${sub.id!}" data-level="3" placeholder="请选择">	[#list c.children as b]'+
									'<option class="cascade-option" value="${b.name!}" selected>${b.name!}</option>[/#list]<option class="cascade-option" value="" selected>请选择</option></select></div>'
								[/#if]
								[/#list]
							[/#if]
							[/#list]
						[/#if]
						[/#list]
						$('.cascade_${sub.id!}').append(html)
					[/#if]
					[/#list]
					formData = obj
				$('.t-vote-wrap').css({
					backgroundColor:'${tag_bean.bgConfig.bgType!}'==1?'':'${tag_bean.bgConfig.bgColor!}',
					backgroundImage:'${tag_bean.bgConfig.bgType!}'==1?'url(' + 
						(('${tag_bean.bgConfig.bgImageUrl!}').indexOf('//')>-1?
						'${tag_bean.bgConfig.bgImageUrl!}':'${base}${tag_bean.bgConfig.bgImageUrl!}') + ')':'',
					backgroundPosition:'${tag_bean.bgConfig.alignType!}',
					backgroundRepeat:'${tag_bean.bgConfig.isRepeat!}'==1? 'repeat' : 'no-repeat',
					opacity: Number('${tag_bean.bgConfig.opacity!}') / 100
				});
				$('.t-vote-cont').css({
					backgroundColor:'${tag_bean.contConfig.bgColor!"#fff"}',
					borderColor:'${tag_bean.contConfig.hasBorder!}'==1?'${tag_bean.contConfig.borderColor!"#e8e8e8"}':'#e8e8e8',
					borderWidth:'${tag_bean.contConfig.hasBorder!}'==1?'${tag_bean.contConfig.borderWidth!"1px"}':'1px',
					borderRadius: '${tag_bean.contConfig.borderRadius!"0"}' + "px"
				});
				$('.t-vote-title').css({
					color:'${tag_bean.fontConfig.titleStyle.fontColor!"#333"}',
					fontSize:'${tag_bean.fontConfig.titleStyle.fontSize!24}'+'px',
					fontWeight:'${tag_bean.fontConfig.titleStyle.fontWigth!600}',
					textAlign:'${tag_bean.fontConfig.titleStyle.fontAlign!"center"}'
				})
				$('.t-vote-detail').css({
					color:'${tag_bean.fontConfig.descStyle.fontColor!"#333"}',
					fontSize:'${tag_bean.fontConfig.descStyle.fontSize!14}'+'px',
					fontWeight:'${tag_bean.fontConfig.descStyle.fontWigth!400}',
					textAlign:'${tag_bean.fontConfig.descStyle.fontAlign!"left"}'
				});
				$('.t-cont-ques .t-cont-title').css({
					color:'${tag_bean.fontConfig.stemStyle.fontColor!"#333"}',
					fontSize:'${tag_bean.fontConfig.stemStyle.fontSize!14}'+'px',
					fontWeight:'${tag_bean.fontConfig.stemStyle.fontWigth!400}'
				});
				$('.t-cont-ques .t-cont-radios').css({
					color:'${tag_bean.fontConfig.optStyle.fontColor!"#333"}',
					fontSize:'${tag_bean.fontConfig.optStyle.fontSize!14}'+'px',
					fontWeight:'${tag_bean.fontConfig.optStyle.fontWigth!400}'
				});
				$('.sub-btn').html('${tag_bean.subConfig.text!}');
				$('.sub-btn').css({
					backgroundColor:'${tag_bean.subConfig.bgColor!}',
					width:'${tag_bean.subConfig.btnWidth!}px',
					height:'${tag_bean.subConfig.btnHeight!}px',
          lineHeight:'${tag_bean.subConfig.btnHeight!}px',
					borderColor:'${tag_bean.subConfig.borderColor!}',
					borderRadius:'${tag_bean.subConfig.borderRadius!}',
					borderWidth:'${tag_bean.subConfig.hasBorder}'==1?'${tag_bean.subConfig.borderWidth!}px':'0px',
					fontSize:'${tag_bean.subConfig.fontStyle.fontSize!}',
					fontWeight:'${tag_bean.subConfig.fontStyle.fontWigth!}',
					color:'${tag_bean.subConfig.fontStyle.fontColor!}'
				});
			}
			$(function(){
				var ua =navigator.userAgent.toLowerCase();
				var isWeixin = ua.indexOf('micromessenger') != -1;
				// 只有微信能打开
				// if(isOnlyWechat == 'true'&&!isWeixin)// 提交
				$('.sub-btn').click(function (e) {
						subForm();
				});
			})
			