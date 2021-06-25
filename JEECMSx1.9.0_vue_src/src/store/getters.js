const getters = {
  user: state => state.app.user,
  areaOptions: state => state.system.areaOptions,
  areaOptionsId: state => state.system.areaOptionsId,
  setting: state => state.app.setting,
  routings: state => state.app.routings,
  config: state => state.config,
  system: state => state.system,
  addRoutes: state => state.permission.addRoutes,
  routes: state => state.permission.routes,
  memberInfo: state => state.system.member,
  userSecurityOptions: state => state.system.userSecurityOptions,
  contentSecurityOptions: state => state.system.contentSecurityOptions,
  accessorySecurityOptions: state => state.system.accessorySecurityOptions,
  ftpOptions: state => state.system.ftpOptions,
  ossOptions: state => state.system.ossOptions,
  columnOptions: state => state.column.columnOptions,
  contentColumnOptions: state => state.column.contentColumnOptions,
  columnModelOptions: state => state.column.columnModelOptions,
  content: state => state.content,
  contentModelOptions: state => state.content.contentModelOptions,
  organizeOptions: state => state.system.organizeOptions,
  organizeOptionsAll: state => state.system.organizeOptionsAll,
  roleOptions: state => state.system.roleOptions,
  memberLevelOptions: state => state.vip.memberLevelOptions,
  memberGroupOptions: state => state.vip.memberGroupOptions,
  resourceOptions: state => state.system.resourceOptions,
  shareResourceOptions: state => state.system.shareResourceOptions,
  ownerTree: state => state.config.ownerTree,
  ownSiteOptions: state => state.config.ownSiteOptions,
  currentSiteId: state => state.config.currentSiteId,
  currentSiteName: state => state.config.currentSiteName,
  siteBaseConfig: state => state.config.siteBaseConfig,
  siteExtendConfig: state => state.config.siteExtendConfig,
  wechatInfoList: state => state.content.wechatInfoList,
  weiboinfoList: state => state.content.weiboinfoList,
  weiboinfoNoauthList: state => state.content.weiboinfoNoauthList,
  tencentList: state => state.social.tencentList,
  wechatNoauthList: state => state.social.wechatNoauthList,
  wechatTags: state => state.wechat.wechatTags,
  workflowOptions: state => state.config.workflowOptions,
  issueOrgOptions: state => state.config.issueOrgOptions,
  issueYearOptions: state => state.config.issueYearOptions,
  smsIsGloable: state => state.system.smsIsGloable,
  smsIsEnable: state => state.system.smsIsEnable,
  emailIsGloable: state => state.system.emailIsGloable,
  emailIsEnable: state => state.system.emailIsEnable,
  detailData: state => state.interact.detailData,
  smartDetailData: state => state.interact.smartDetailData,
  mailBoxDetail: state => state.interact.mailBoxDetail,
  closeLayerState: state => state.interact.closeLayerState,
  mailboxLetterTypes: state => state.interact.mailboxLetterTypes,
  mailboxLetterBoxs: state => state.interact.mailboxLetterBoxs
}
export default getters
