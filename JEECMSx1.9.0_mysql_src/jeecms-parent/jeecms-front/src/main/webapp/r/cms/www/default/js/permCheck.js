
function genPermCheck(aid,bid,base) {
  let url = base || '';
  let contentId = Number(aid) || '';
  let channelId = Number(bid) || '';
  console.log(url,contentId,channelId);
  $.ajax({
      url: url + "/permCheck",
      data: {
          contentId:contentId || '',
          channelId:channelId || ''
      },
      success: function (result) {
        if (result.data == 3||result.data == 4){
          location.href = url + "/common-errorauth.htm";
        } else if(result.data!=1){
          location.href = url + "/login";
        }
      }
  });
}