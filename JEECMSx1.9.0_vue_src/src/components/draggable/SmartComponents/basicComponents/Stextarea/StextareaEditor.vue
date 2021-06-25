<template>
  <base-form
    class="s-textarea-editor-container s-draggable-editor"
    ref="baseForm"
    :showSubmitButton="false"
    :form="value"
    :formItems="getFormItems"
    :labelWidth="labelWidth"
    :rules="rules"
    :lineShow="true"
    label-position="top"
    :popperAppendToBody='false'
  >
    <template #rules="scope">
      <div class="rules-wrap">
        <div class="rule-item">
          <el-checkbox v-model="scope.form.isLengthLimit" class="z-checkbox-single limit default">限制字数</el-checkbox>
          <div class="rule-item-length"
              v-if="scope.form.isLengthLimit"
          >
            <el-form-item
              prop='max'
              style="marginBottom: 0"
            >
              <el-input-number
                v-model.number="scope.form.max"
                size="small"
                :min="1"
                :max="120"

              ></el-input-number>
              <span class="z-append-text" style="top: 7px;left: 190px;margin-top:0">请输入1-120间的整数</span>
            </el-form-item>
          </div>
        </div>
        <div class="rule-item" :class="scope.form.isLengthLimit?'pt':''">
          <el-checkbox v-model="scope.form.isInputLimit" class="z-checkbox-single default">输入限制</el-checkbox>
          <div :class="scope.form.isInputLimit?'rule-item-input rule-h':''">
            <el-select
              v-if="scope.form.isInputLimit"
              v-model="scope.form.inputLimit"
              popper-class="jee-border"
              size="small"
              :popper-append-to-body="false"
              placeholder="请选择"
             >
              <el-option
                v-for="item in scope.item.options"
                :key="item.value"
                :label="item.label"
                :value="item.value">
              </el-option>
            </el-select>
          </div>
        </div>
      </div>
    </template>
  </base-form>
</template>

<script>
import editorMixin from '@/components/draggable/Mixin/editorMixin'

export default {
  name: 'StextareaEditor',
  mixins: [editorMixin],
  data () {
    return {
      labelWidth: 'auto',
      formItems: [
        {
          prop: 'label',
          label: '字段名称：',
          maxlength: 15
        },
        // {
        //   prop: 'name',
        //   label: '标签名称：',
        //   maxlength: 50
        // },
        {
          prop: 'placeholder',
          label: '默认提示文字：',
          maxlength: 50
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
          itemClass: 'margin-bottom25',
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
          type: 'radio',
          itemClass: 'width',
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
        max: [
          this.$rules.number(),
          this.$rules.rangeNum(1, 120)
        ],
        isRegister: [
          this.$rules.required()
        ]
      }
    }
  },
  props: {
    value: {
      type: [Array, Object, String],
      default: () => {
        return []
      }
    }
  },
  watch: {
    'value': function (val) {
      // this.setColor(newData)
    }
  },
  methods: {

    handleChange (value) {
    }
  }
}
</script>

<style lang="scss">
  .rules-wrap{
    max-width:330px;
    .rule-item{
      .rule-h{
        height: 35px;
        // margin-bottom: 25px;
        input{
          height: 35px;
        }
      }
      .rule-item-length{
        margin: 10px 0;
      }
    }
    .pt{
      padding-top: 0 !important;
    }
  }
</style>
