import common from '@/components/draggable/common'

export default {
  ...common,
  canDelete: false,
  icon: 'jia',
  type: 'userImgId',
  name: '头像',
  // 字段名称
  groupType: '', // 基础、扩展、SEO
  // 分组类型
  groupIndex: 12,
  // 分组排序 左侧拖动区顺序
  isCustom: false,
  // 是否自定义字段
  index: 1,
  // 排序
  preview: 'DPortraitPreview',
  editor: 'DPortraitEditor',
  hiddenFields: ['imgSize', 'isRegister'],
  disableFields: ['label', 'name'],
  value: {
    defaultValue: '', // 默认值
    label: '头像', // 字段名称
    name: 'userImgId', // 标签名称
    type: ['jpg', 'png', 'gif', 'bmp', 'jpeg'], // 图片类型
    size: '', // 图片大小
    unit: 'KB', // 图片大小单位
    width: '', // 建议宽度
    height: '', // 建议高度
    tip: '', // 帮助信息
    isRequired: false // 是否必填
  }
}
