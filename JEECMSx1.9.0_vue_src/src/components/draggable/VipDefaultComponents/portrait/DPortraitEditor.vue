<script>
import compoment from '@/components/draggable/CustomComponents/ImageUpload/DImageUploadEditor'
export default {
  name: 'DPortraitEditor',
  extends: compoment,
  data(){
    return{
      formItems: [
        {
          prop: 'label',
          label: '字段名称：',
          maxlength: 50
        },
        {
          prop: 'name',
          label: '标签名称：',
          maxlength: 50
        },
        {
          prop: 'type',
          label: '限制图片类型：',
          type: 'select',
          itemClass:"imgcss",
          multiple: true,
          options: this.$enums.imageType
        },
        {
          prop: 'size',
          label: '图片大小不超过：',
          type: 'slot',
          options: this.$enums.size
        },
        {
          prop: 'imgSize',
          label: '图片建议宽高：',
          itemClass: 'width',
          type: 'slot'
        },
        {
          prop: 'tip',
          label: '帮助信息：',
          placeholder: '请输入内容',
          maxlength: 50
        },
        {
          prop: 'isRequired',
          label: '是否必填：',
          itemClass: 'width',
          type: 'radio',
          options: this.$enums.boolYes
        },
        {
          prop: 'isRegister',
          label: '应用到会员注册：',
          itemClass: 'width',
          type: 'radio',
          options: this.$enums.boolYes
        }
      ],
    }
  }
}
</script>
