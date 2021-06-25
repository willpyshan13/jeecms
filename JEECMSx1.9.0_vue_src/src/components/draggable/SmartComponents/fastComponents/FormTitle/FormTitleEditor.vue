<script>
import compoment from '@/components/draggable/SmartComponents/basicComponents/Sinput/SInputEditor'
export default {
  name: 'FormTitleEditor',
  extends: compoment,
  data () {
    return {
      formItems: [
        {
          prop: 'label',
          label: '字段名称：',
          maxlength: 15,
          placeholder: '请输入内容'
        },
        {
          prop: 'placeholder',
          label: '默认提示文字：',
          maxlength: 50,
          placeholder: '请输入内容'
        },
        {
          prop: 'tip',
          label: '帮助信息：',
          maxlength: 50,
          placeholder: '请输入内容'
        },
        {
          prop: 'rules',
          label: '填写限制：',
          type: 'slot',
          class: 'margin-bottom25',
          options: this.$enums.rulesMap
        },
        {
          prop: 'width',
          label: '组件宽度(%)：',
          type: 'number',
          step: 10,
          precision: 0,
          max: 100,
          min: 10
        },
        {
          prop: 'isRequired',
          label: '是否必填：',
          type: 'text',
          itemClass: 'width',
          options: this.$enums.boolYes
        }
      ]
    }
  }
}
</script>
