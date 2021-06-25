/* system-cms-prefix start */
const prefix = '/cmsmanager'
/* system-cms-prefix change const prefix = '/cmsadmin' system-cms-prefix change */
/* system-cms-prefix end */
export default {
    // 付费配置
    paycontentPayConfig:`${prefix}/payconfig`,
    
    // 付费统计

    // 趋势分析概况
    paycontentPayStatistics:`${prefix}/payTrendStatistic/getSurvey`,
    // 付费统计-用户top10
    paycontentUserTop:`${prefix}/payuserstatistic/getUserTopTen`,
    // 付费统计-内容top10
    paycontentContentTop:`${prefix}/payuserstatistic/getContentTopTen`,
    // 趋势分析分页
    paycontentPayStatisticsDetail:`${prefix}/payTrendStatistic/getTrendAnalysisList`,
    // 付费概况
    paycontentPaystatisticstop:`${prefix}/payTrendStatistic/getPaymentStatisticsSurvey`,
    // 付费图表
    paycontentPayStatisticsVchart:`${prefix}/payTrendStatistic/getTrendAnalysisTable`,

    // 用户账户统计
    paycontentUserStatisticBalance:`${prefix}/payuserstatistic/balance/page`,
    // 按余额统计
    paycontentBalanceStatistic:`${prefix}/payuserstatistic/balance/statistic`,
    // 按收益1/按提现2
    paycontentEarnings:`${prefix}/payuserstatistic/earnings/page`,
    // 按收益明细统计
    paycontentEarningsStatistic:`${prefix}/payuserstatistic/earnings/statistic`,
    // 收益明细统计
    paycontentEarningsStatisticDetail:`${prefix}/payuserstatistic/incomeBreakdown/statistic`,
    // 提现明细统计
    paycontentWithdrawStatistic:`${prefix}/payuserstatistic/withdraw/statistic`,
    // 收益明细详情
    paycontentEarningsDetail:`${prefix}/payuserstatistic/incomeBreakdown/page`,
    // 提现明细详情
    paycontentWithdrawDetail:`${prefix}/payuserstatistic/withdraw/page`,
    // 按收益导出
    paycontentEarningsExport:`${prefix}/payuserstatistic/total/export`,
    // 按提现导出
    paycontentWithdrawExport:`${prefix}/payuserstatistic/withdraw/export`,
    // 收益明细导出
    paycontentEarningsDetailExport:`${prefix}/payuserstatistic/incomeBreakdown/export`,
    // 提现明细导出
    paycontentWithdrawDetailExport:`${prefix}/payuserstatistic/withdrawalSubsidiary/export `,

    // 提现管理
    // 提现审核分页
    paycontentCashManagementAudit:`${prefix}/paywithdraworders/audit/page`,
    // 提现成功
    paycontentCashmanagementSuccess:`${prefix}/paywithdraworders/success/page`,
    // 提现成功统计
    paycontentCashmanagementSuccessStatistic:`${prefix}/paywithdraworders/success/statistic`,
    //提现失败
    paycontentCashmanagementfailed:`${prefix}/paywithdraworders/failure/page`,
    // 提现失败导出
    paycontentCashmanagementfailedExport:`${prefix}/paywithdraworders/failure/export`,
    // 提现成功导出
    paycontentCashmanagementSuccessExport:`${prefix}/paywithdraworders/success/export`,
    // 审核通过操作
    paycontentCashmanagementAuditPass:`${prefix}/paywithdraworders/audit`,
    // 驳回操作
    paycontentCashmanagementRejected:`${prefix}/paywithdraworders/rejected`,
    // 再次转账
    paycontentCashmanagementfailedAgain:`${prefix}/paywithdraworders/transferAgain`,

    // 支付明细
    // 按内容
    paycontentPaydetailContent:`${prefix}/paydetails/content`,
    // 按内容统计
    paycontentPaydetailContentSum:`${prefix}/paydetails/content/sum`,
    // 按订单
    paycontentPaydetailOrder:`${prefix}/paydetails/order`,
    // 按订单统计
    paycontentPaydetailOrderSum:`${prefix}/paydetails/order/sum`,
    // 内容明细
    paycontentPaydetailContentDetail:`${prefix}/paydetails/getContentDetailPage`,
    // 内容明细概况
    paycontentPaydetailContentDetailSurvey:`${prefix}/paydetails/getContentDetailSurvey`,
    // 按栏目
    paycontentPaydetailColumn:`${prefix}/paydetails/channel`,
    // 按栏目概况
    paycontentPaydetailColumnSurvey:`${prefix}/paydetails/channelSurvey`,
    // 站点级联
    paycontentSitesTree:`${prefix}/sites/noauth/tree`,
    // 栏目树
    paycontentAllTree:`${prefix}/channel/getAllTree`
}