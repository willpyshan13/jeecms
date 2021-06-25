import axios from '../axios'
import paycontent from '../urls/paycontent.js'

export default {
     // 付费配置
  fetchPayConfig (data) {
    return axios.get(paycontent.paycontentPayConfig, data)
  },
  fetchPayConfigSave (data) {
    return axios.post(paycontent.paycontentPayConfig, data)
  },
  // 趋势分析概况
  fetchPayStatistics(data) {
    return axios.get(paycontent.paycontentPayStatistics,data)
  },
   // 付费统计-用户top10
   fetchPayStatisticsUserTop(data) {
    return axios.get(paycontent.paycontentUserTop,data)
  },
  // 付费统计-内容top10
  fetchPayStatisticsContentTop(data) {
    return axios.get(paycontent.paycontentContentTop,data)
  },
  // 趋势分析分页
  fetchpaycontentPayStatisticsDetail(data){
    return axios.get(paycontent.paycontentPayStatisticsDetail,data)
  },
  // 付费概况
  fetchpaycontentPaystatisticstop(data){
    return axios.get(paycontent.paycontentPaystatisticstop,data)
  },
  // 付费图表
  fetchpaycontentPayStatisticsVchart(data){
    return axios.get(paycontent.paycontentPayStatisticsVchart,data)
  },

  // 用户账户统计-按余额
  fetchUserStatisticBalance(data){
    return axios.get(paycontent.paycontentUserStatisticBalance+`?page=${data.page}&size=${data.size}`,data)
  },
  // 按余额-统计
  fetchBalanceStatistic(data){
    return axios.get(paycontent.paycontentBalanceStatistic,data)
  },
  // 按收益/按提现
  fetchEarnings(data){
    return axios.post(paycontent.paycontentEarnings+`?page=${data.page}&size=${data.size}`,data)
  },
  // 按收益/按提现统计
  fetchEarningsStatistic(data){
    return axios.post(paycontent.paycontentEarningsStatistic,data)
  },
  // 收益明细统计
  fetchpaycontentEarningsStatisticDetail(data){
    return axios.post(paycontent.paycontentEarningsStatisticDetail,data)
  },
  // 提现明细统计
  fetchWithdrawStatistic(data){
    return axios.post(paycontent.paycontentWithdrawStatistic,data)
  },
  // 收益明细详情
  fetchpaycontentEarningsDetail(data){
    return axios.post(paycontent.paycontentEarningsDetail+`?page=${data.page}&size=${data.size}`,data)
  },
  // 提现明细详情
  fetchpaycontentWithdrawDetail(data){
    return axios.post(paycontent.paycontentWithdrawDetail+`?page=${data.page}&size=${data.size}`,data)
  },
  // 按收益导出
  fetchpaycontentEarningsExport(data){
    return axios.download(paycontent.paycontentEarningsExport,data)
  },
  // 按提现导出
  fetchpaycontentWithdrawExport(data){
    return axios.download(paycontent.paycontentWithdrawExport,data)
  },
  // 收益明细导出
  fetchpaycontentEarningsDetailExport(data){
    return axios.download(paycontent.paycontentEarningsDetailExport,data)
  },
  // 提现明细导出
  fetchpaycontentWithdrawDetailExport(data){
    return axios.download(paycontent.paycontentWithdrawDetailExport,data)
  },

  // 提现管理
  // 提现审核
  fetchpaycontentCashManagementAudit(data){
    return axios.post(paycontent.paycontentCashManagementAudit+`?page=${data.page}&size=${data.size}`,data)
  },
  // 提现成功
  fetchpaycontentCashmanagementSuccess(data){
    return axios.post(paycontent.paycontentCashmanagementSuccess+`?page=${data.page}&size=${data.size}`,data)
  },
  // 提现成功统计
  fetchpaycontentCashmanagementSuccessStatistic(data){
    return axios.post(paycontent.paycontentCashmanagementSuccessStatistic,data)
  },
  // 提现失败
  fetchpaycontentCashmanagementfailed(data){
    return axios.post(paycontent.paycontentCashmanagementfailed+`?page=${data.page}&size=${data.size}`,data)
  },
  // 提现失败导出
  fetchpaycontentCashmanagementfailedExport(data){
    return axios.download(paycontent.paycontentCashmanagementfailedExport,data)
  },
  // 提现成功导出
  fetchpaycontentCashmanagementSuccessExport(data){
    return axios.download(paycontent.paycontentCashmanagementSuccessExport,data)
  },
  // 审核通过
  fetchpaycontentCashmanagementAuditPass(id){
    return axios.get(paycontent.paycontentCashmanagementAuditPass+`/${id}`)
  },
  // 驳回操作
  fetchpaycontentCashmanagementRejected(data){
    return axios.get(paycontent.paycontentCashmanagementRejected+`/${data.id}`,data)
  },
  fetchpaycontentCashmanagementfailedAgain(id){
    return axios.get(paycontent.paycontentCashmanagementfailedAgain+`/${id}`)
  },

  // 支付明细
  // 按内容
  fetchpaycontentPaydetailContent(data){
    return axios.get(paycontent.paycontentPaydetailContent,data)
  },
  // 按内容统计
  fetchpaycontentPaydetailContentSum(data){
    return axios.get(paycontent.paycontentPaydetailContentSum,data)
  },
  // 按订单
  fetchpaycontentPaydetailOrder(data){
    return axios.get(paycontent.paycontentPaydetailOrder+`?${data.first&&data.numlength>1?"channelIds="+data.first+"&"+"channelIds="+data.second:data.first&&data.numlength==1?"channelIds="+data.first:'channelIds='}`,data)
  },
  // 按订单统计
  fetchpaycontentPaydetailOrderSum(data){
    return axios.get(paycontent.paycontentPaydetailOrderSum,data)
  },
  // 内容明细
  fetchpaycontentPaydetailContentDetail(data){
    return axios.get(paycontent.paycontentPaydetailContentDetail,data)
  },
  // 内容明细概况
  fetchpaycontentPaydetailContentDetailSurvey(data){
    return axios.get(paycontent.paycontentPaydetailContentDetailSurvey,data)
  },
  // 按栏目
  fetchpaycontentPaydetailColumn(data){
    return axios.get(paycontent.paycontentPaydetailColumn,data)
  },
  // 按栏目概况
  fetchpaycontentPaydetailColumnSurvey(data){
    return axios.get(paycontent.paycontentPaydetailColumnSurvey,data)
  },
  // 站点级联
  fetchpaycontentSitesTree(data){
    return axios.get(paycontent.paycontentSitesTree,data)
  },
  // 栏目树
  fetchpaycontentAllTree(data){
    return axios.get(paycontent.paycontentAllTree,data)
  }
}