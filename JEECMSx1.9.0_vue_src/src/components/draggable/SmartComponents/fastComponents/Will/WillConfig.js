import common from '@/components/draggable/common'

export default {
  ...common,
  icon: 'dangwugongkai',
  type: 'publicWill',
  // 1手机号2邮箱3座机号4真实姓名5生日6性别7地址8组织9年龄10身份证号11城市12传真
  name: '公开意愿',
  // 字段名称
  groupType: 'input', // 基础、扩展、SEO
  // 分组类型
  groupIndex: 1,
  // 分组排序 左侧拖动区顺序
  isCustom: true,
  // 是否自定义字段
  disableFields: ['isRequired'],
  index: 1,
  // 排序
  // 是否可删除
  canDelete: true,
  preview: 'WillPreview',
  editor: 'WillEditor',
  value: {
    defaultValue: {
      value: '',
      otherValue: ''
    }, // 默认值
    label: '公开意愿', // 字段名称
    name: 'publicWill', // 标签名称
    tip: '如果您选择"是",我们可能将对您的写信内容及办理结果进行公示!', // 帮助信息
    options: [
      {
        value: 1,
        label: '是'
      },
      {
        value: 2,
        label: '否'
      }
    ],
    radioBtns: 3,
    disRadioBtn: true,
    // isOtherOption: false,
    // isOtherOptionRequired: false,
    // otherOptionLabel: '其他',
    // otherOption: {
    //   value: 999,
    //   label: ''
    // },
    isRegister: false, // 是否应用到注册
    isRequired: true // 是否必填
  }
}
