<template>
  <base-form
    class="d-radio-editor-container z-draggable-editor new-content"
    ref="baseForm"
    :showSubmitButton="false"
    :form="value"
    :formItems="getFormItems"
    :labelWidth="labelWidth"
    :rules="rules"
    label-position="top"
    :popperAppendToBody='false'
  >
    
  </base-form>
</template>

<script>
import editorMixin from '@/components/draggable/Mixin/editorMixin'

export default {
  name: 'NewDRewardEditor',
  mixins: [editorMixin],
  data () {
    return {
      labelWidth: '95px',
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
          prop: 'tip',
          label: '帮助信息：',
          placeholder: '请输入内容',
          class:'holder_css',
          maxlength: 50
        },
        
        {
          prop: 'isRequired',
          label: '是否必填：',
          type: 'radio',
          itemClass: 'width',
          options: this.$enums.boolYes
        },
        
      ],
      rules: {
        label: [
          this.$rules.required()
        ],
        name: [
          this.$rules.enNumLine(),
          this.$rules.required()
        ],
        isRequired: [
          this.$rules.required()
        ],
        isRegister: [
          this.$rules.required()
        ]
      }
    }
  }
}
</script>
<style lang="scss">
  .d-radio-editor-container.z-draggable-editor.new-content{
    .holder_css{
      .el-input__inner::-webkit-input-placeholder { /* WebKit browsers */ 
        font-weight: 400 !important;
      }
      .el-input__inner:-moz-placeholder { /* Mozilla Firefox 4 to 18 */ 
        font-weight: 400 !important; 
      }
      .el-input__inner::-moz-placeholder { /* Mozilla Firefox 19+ */ 
        font-weight: 400 !important; 
      }
      .el-input__inner:-ms-input-placeholder { /* Internet Explorer 10+ */ 
        font-weight: 400 !important; 
      }
    }
    .option-wrap{
      .option-item.new-cont-option.flex{
        display: flex;
        flex-direction: row;
        align-items: center;
        padding-bottom: 10px;
        &:last-of-type{
          padding-bottom: 6px;
        }
      }
      .sortable-drag{
        border: none;
        box-shadow: rgba(0, 0, 0, 0.2) 0px 0px 20px;
        padding: 5px 10px !important;
        background-color: rgba(255,255,255,0.7);
        border-radius: 4px;
      }
    }
    .icon.flex {
      display: flex;
      flex-direction: row;
      align-items: center;
      margin-left: 10px;
      svg {
        fill: #787878;
        margin-right: 10px;
        cursor: pointer;
      }
    }
  }
</style>
