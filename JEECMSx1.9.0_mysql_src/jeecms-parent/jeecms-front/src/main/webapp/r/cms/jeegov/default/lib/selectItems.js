
var trAreaList = [],// 获取到的数据
tabId = 1,
fieldName = '',
codeItms = {// 省市区列表
    areaA:[],
    areaB:[],
    areaC:[],
    areaD:[],
},
selectValues=[],// 省市区拼接名称
selectObj = {//省市区code
    areaA:'',
    areaB:'',
    areaC:'',
    areaD:'',
},
prt={
    key:'',
    name:''
},
tabs=[],
trType=''
// 显示
function toggleItemAll(prop,tabList,data,itemAll,name,type){
  trAreaList = data
  fieldName = name
  prt = prop
  tabs = tabList.list
  trType = type
  $(itemAll).find(".s-item").remove()
  // var l = $(itemAll).find(".s-item").length
  // if(l < 1){
      var itemTab = ''
      for(var i= 0 ; i<tabList.list.length;i++ ){
          if(tabList.list[i].id === 1){
              itemTab += '<div class="s-item classBgSHow" id="'+(name+'Item'+tabList.list[i].id)+'" '+
              'onClick="bandTabData('+tabList.list[i].id+')">'+tabList.list[i].name+'</div>'
          }else {
              itemTab += '<div class="s-item " id="'+(name+'Item'+tabList.list[i].id)+'" '+
              ' onClick="bandTabData('+tabList.list[i].id+')">'+tabList.list[i].name+'</div>'
          }
      }
      $(itemAll).find(".ul-item-tab").append(itemTab)
      setAreaItems(data,'bandSJData',true)
  // } else if(codeItms.areaA.length) {
  //     $('#'+fieldName+'Item'+1).addClass("classBgSHow").siblings().removeClass("classBgSHow");
  //     setAreaItems(codeItms.areaA,'bandSJData',true)
  // } else {
  //     $(itemAll).find(".ul-item-tab").append(itemTab)
  //     setAreaItems(data,'bandSJData')
  // }
}
/*列表赋值*/
function setAreaItems(arr,eve,status,key){
    var list = arr || []
    var listHtml = ''
    $(".ul-item-list li").remove()
    for(var i= 0 ; i<list.length;i++ ){
        listHtml += '<li '+(key == list[i][prt.key]?'class="t-red"':'')+' onClick='+eve+'("'+list[i][prt.key]+'","'+list[i][prt.name]+'")>'+list[i][prt.name]+'</li>'
    }
    $("."+fieldName+'All .ul-item-list').append(listHtml)
    if((tabs.length == selectValues.length&&!status)||!list.length){
        $('#'+fieldName+'Area').siblings('.item-all').toggle();
        var  b = $('#'+fieldName+'Area').siblings('.item-all').css('display')
        if(b === 'block'){
            $('#'+fieldName+'Area'+" .p-select-icon").addClass('layui-icon-up').removeClass('layui-icon-down')
        } else {
            $('#'+fieldName+'Area'+" .p-select-icon").addClass('layui-icon-down').removeClass('layui-icon-up')
            $("#"+ fieldName + 'Area ' + '.select-item').remove()
            $("#"+fieldName+'Code').text(selectValues.join('-'))
            getFormValue(fieldName,trType,{value:selectObj,names:selectValues})
        }
    }
    
};
// 点击tab选择
function bandTabData(tab,name){
    tabId = tab
    $('#'+fieldName+'Item'+tabId).addClass("classBgSHow").siblings().removeClass("classBgSHow");
    $('.ul-item-list li').remove()
    if(tabId == 1){
        if(selectObj.areaA.length){
            setAreaItems(codeItms.areaA,'bandSJData',true,selectObj.areaA)
        } else {
            setAreaItems(trAreaList,'bandSJData',true)
        }
    } else if(tabId == 2){
        setAreaItems(codeItms.areaB,'bandSQData',true,selectObj.areaB)
    } else if(tabId == 3) {
        setAreaItems(codeItms.areaC,'bandXJData',true,selectObj.areaC)
    } else if(tabId == 4) {
        setAreaItems(codeItms.areaD,'bandDCData',true,selectObj.areaD)
    } else{
        setAreaItems([])
    }
}

// 选择省份或直辖市
function bandSJData(key,name){
    tabId = 2
    $('#'+fieldName+'Item'+tabId).addClass("classBgSHow").siblings().removeClass("classBgSHow");
    /*初始化列表和选中值*/
    selectValues = [];
    if(key&&key!=selectObj.areaA){
        /*数据赋值*/
        selectObj = {
            areaA:key,
            areaB:'',
            areaC:'',
            areaD:'',
        };
        /*列表赋值*/
        for(var i=0; i<trAreaList.length;i++){
            if(key == trAreaList[i][prt.key]){
                codeItms = {
                    areaA:trAreaList,
                    areaB:trAreaList[i].children,
                    areaC:[],
                    areaD:[]
                }
                selectValues[0] = trAreaList[i][prt.name]
                setAreaItems(trAreaList[i].children,'bandSQData')
            }
        }
    } else if(key&&key == selectObj.areaA) {
        /*数据赋值*/
        selectObj = {
            areaA:key,
            areaB:'',
            areaC:'',
            areaD:'',
        };
        selectValues[0] = name
        setAreaItems(codeItms.areaB,'bandSQData')
    } else {
        setAreaItems([])
    }
}
//点击选择市或直辖市中的县
function bandSQData(key,name){
    tabId = 3
    $('#'+fieldName+'Item'+tabId).addClass("classBgSHow").siblings().removeClass("classBgSHow");
    if(key&&key!=selectObj.areaB){
        /*数据赋值*/
        selectObj.areaB = key
        selectObj.areaC = ''
        selectObj.areaD = ''
        /*列表赋值*/
        for(var i=0; i<codeItms.areaB.length;i++){
            if(key == codeItms.areaB[i][prt.key]){
                codeItms.areaC = codeItms.areaB[i].children
                codeItms.areaD = []
                selectValues[1] =codeItms.areaB[i][prt.name]
                setAreaItems(codeItms.areaB[i].children,'bandXJData')
            }
        }
    } else if(key&&key == selectObj.areaB) {
        /*数据赋值*/
        selectObj.areaB = key
        selectObj.areaC = ''
        selectObj.areaD = ''
        selectValues[1] = name
        setAreaItems(codeItms.areaB,'bandXJData')
    } else {
        setAreaItems([])
    }
}
// 3
function bandXJData(key,name){
    tabId = 4
    $('#'+fieldName+'Item'+tabId).addClass("classBgSHow").siblings().removeClass("classBgSHow");
    if(key&&key!=selectObj.areaC){
        /*数据赋值*/
        selectObj.areaC = key
        selectObj.areaD = ''
        /*列表赋值*/
        for(var i=0; i<codeItms.areaC.length;i++){
            if(key == codeItms.areaC[i][prt.key]){
                codeItms.areaD = codeItms.areaC[i].children
                selectValues[2] = codeItms.areaC[i][prt.name]
                setAreaItems(codeItms.areaC[i].children,'bandDCData')
            }
        }
    } else if(key&&key == selectObj.areaC) {
        /*数据赋值*/
        selectObj.areaC = key
        selectObj.areaD = ''
        selectValues[2] = name
        setAreaItems(codeItms.areaC,'bandDCData')
    } else {
        setAreaItems([])
    }
}
// 4
function bandDCData(key,name){
    if(key&&key!=selectObj.areaD){
        /*数据赋值*/
        selectObj.areaD = key
        /*列表赋值*/
        for(var i=0; i<codeItms.areaD.length;i++){
            if(key == codeItms.areaD[i][prt.key]){
                selectValues[3] = codeItms.areaD[i][prt.name]
                setAreaItems([])
            }
        }
    } else if(key&&key == selectObj.areaD) {
        /*数据赋值*/
        selectObj.areaD = key
        selectValues[3] = name
        setAreaItems([])
    } else {
        setAreaItems([])
    }
}


