<template>
  <section class="d-radio-preview z-draggable-preview new-content reward-content">
    <el-form-item
      :label="option.label"
      :prop="option.name"
      :rules="isValidator? getMyRules : []"
      :required="option.isRequired"
      :class="{'sm-height': !option.isOtherOption}"
    >
     
      <el-switch
        v-model="form[option.name]"
        active-color="#1ec6df"
        inactive-color="#ff4949"
        class="el-switch topswitch"
        :active-text="form[option.name]?'开启':'关闭'"
        :active-value='1'
        :inactive-value='0'
        size="small">
        </el-switch>
      <div class="z-tip-form-item" v-if="option.tip">{{option.tip}}</div>
    </el-form-item>
  </section>
</template>

<script>
import previewMixin from '@/components/draggable/Mixin/previewMixin'

export default {
  name: 'NewDRewardPreview',
  mixins: [previewMixin],
  props: {
    value: {
      type: [Boolean,String,Number],
      default: 1
    }
  },
  watch:{
    'form.reward':function (num){
      this.rewardchange()
    }
     
  },
  computed: {
    getMyRules () {
      const requiredRule = {
        validator: (rule, value, callback) => {
          // if (value) {
            callback()
          // } else {
          //   callback(new Error('此项必填'))
          // }
        },
        trigger: ['blur', 'change']
      }
      let rules = this.getRules
      if (this.option.isRequired) rules.push(requiredRule)
      return rules
    }
  },
  methods:{
    rewardchange(){
      if (!this.form[this.option.name]) {
        this.form[this.option.name] = 0
      }
    }
  }
}
</script>
<style lang="scss">
.content-block-right{
  .reward-content{
    .el-form-item{
      display: block !important;
      &__label{
        float: none;
      }
    }
  }
}
.inp{
  // margin-left: 15px;
    input{
      margin-top: -3px;
    }
  }
.topswitch{
    // margin-top: 7px !important;
    margin-right: 15px;
    .el-switch__core::after{
      width:12px;
      height:12px;
      margin-top:0px;
      margin-bottom: 2px;
      // margin-left: -15px !important;
    }
    .el-switch__core{
      width:28px!important;
      height:16px;
    }
  }
  .ghost-box .is-checked{
    .el-switch__core::after{
      margin-left: -13px !important;
    }
  }
.widgetBr{
  .reward-content{
    .el-form-item{
      display: block !important;
      &__label{
        float: none;
      }
    }
  }
}
.d-radio-preview.new-content{
    .el-switch{
        // margin-top: 7px;
        // width:128px;
        // height: 16px;
    }
  .el-radio-group{
    width: 100%;
  }
  /deep/ &.z-draggable-preview > .el-form-item{
    padding-bottom:0;
    &__label{
      margin-bottom: 18px;
    }
    .el-radio-group{
      height: auto;
      .el-radio{
        margin-right: 40px;
        margin-bottom: 8px;
        &:not(.is-checked){
          .el-radio__inner{
            border-color: 1px solid #ccc;
          }
        }
      }
    }
    .el-checkbox-group{
      width:100%;
      .el-checkbox{
        line-height:1;
        padding-bottom:10px;
      }
    }
    .el-radio-group .el-radio{
      padding-bottom: 10px;
    }
  }
}
.content-block-right .d-radio-preview.new-content{
  &.z-draggable-preview > .el-form-item{
    padding-bottom: 10px !important;
    
  }
}
</style>
