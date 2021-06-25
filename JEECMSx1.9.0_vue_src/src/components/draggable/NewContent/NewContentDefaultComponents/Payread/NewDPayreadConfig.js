import common from '@/components/draggable/common'

export default {
  ...common,
  icon: 'jia',
  type: 'payRead',
  // 1文本2多行文本 3单选 4多选 5下拉 6日期 7单图上传 8多图上传 9视频上传 10音频上传 11附件上传 12富文本 13组织 14地址 15城市
  name: '付费阅读',
  // 字段名称
  groupType: '', // 基础、扩展、SEO
  // 分组类型
  groupIndex: 16,
  // 分组排序 左侧拖动区顺序
  isCustom: false,
  // 是否自定义字段
  index: 15,
  // 排序
  preview: 'NewDPayreadPreview',
  editor: 'NewDPayreadEditor',
  hiddenFields: [],
  disableFields: ['label', 'name'],
  value: {
    defaultValue: {
      trialReading:'',
        payPrice:'',
        payRead:1
    }, // 默认值
    label: '付费阅读', // 字段名称
    name: 'payRead', // 标签名称
    tip: '', // 帮助信息
    
    isRegister: false, // 是否应用到注册
    isRequired: false // 是否必填
  }
}
