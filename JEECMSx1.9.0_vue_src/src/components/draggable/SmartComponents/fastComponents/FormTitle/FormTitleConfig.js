import common from '@/components/draggable/common'

export default {
  ...common,
  icon: '',
  type: 'formTitle',
  // 1手机号2邮箱3座机号4真实姓名5生日6性别7地址8组织9年龄10身份证号11城市12传真
  name: '信件标题',
  // 字段名称
  groupType: 'input', // 基础、扩展、SEO
  // 分组类型
  groupIndex: 99,
  // 分组排序 左侧拖动区顺序
  isCustom: false,
  // 是否自定义字段
  // 是否可删除
  canDelete: false,
  disableFields: ['isRequired'],
  index: 99,
  // 排序
  preview: 'FormTitlePreview',
  editor: 'FormTitleEditor',
  value: {
    defaultValue: '', // 默认值
    label: '信件标题', // 字段名称
    placeholder: '请输入内容', // 默认提示文字
    tip: '', // 帮助信息
    isLengthLimit: false, // 是否字数限制
    min: '', // 最小长度
    max: 50, // 最大长度
    isInputLimit: false, // 是否输入限制
    inputLimit: '', // 限制类型
    width: 80, // 组件宽度
    isRegister: false, // 是否应用到注册
    isRequired: true // 是否必填
  }
}
