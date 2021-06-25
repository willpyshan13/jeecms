<template>
  <section class="d-radio-preview z-draggable-preview new-content payread-content">
    <el-form-item
      :label="option.label"
      :prop="option.name"
      :rules="isValidator? getMyRules : []"
      :required="option.isRequired"
      :class="{'sm-height': !option.isOtherOption}"
      class="payreadcss"
    >
     
      <el-switch
        v-model="form[option.name].payRead"
        active-color="#1ec6df"
        inactive-color="#ff4949"
        class="el-switch topswitch"
        :active-text="form[option.name].payRead?'开启':'关闭'"
        :active-value='1'
        :inactive-value='0'
       >
        </el-switch>
        <div v-if="form[option.name]&&form[option.name].payRead" class="inpParent">
            <el-input class="inp" @input="changeInput" v-model="form[option.name].payPrice" maxlength="8" placeholder="请输入售价(0.01-10000)"></el-input>
            <el-input class="inp laoer" v-model="form[option.name].trialReading" maxlength="5" placeholder="请输入试读字数(0-99999)"></el-input>
        </div>
        
      <div class="z-tip-form-item" v-if="option.tip">{{option.tip}}</div>
    </el-form-item>
  </section>
</template>

<script>
import previewMixin from '@/components/draggable/Mixin/previewMixin'

export default {
  name: 'NewDPayreadPreview',
  mixins: [previewMixin],
  props: {
    value: {
      type: Object,
      default: () => ({
        trialReading:'',
        payRead:1,
        payPrice:''
      })
    }
  },
  computed: {
    getMyRules () {
      const requiredRule = {
        validator: (rule, value, callback) => {
          var reg = /^([1-9]\d*|0)(\.\d{1,2})?$/
          var regRead = /^(0|\+?[1-9][0-9]{0,4})$/
          // if (!value.payRead) {
          //   // value.payRead = 0
            
          // }
          if (value.payRead==0) {
            callback()
          }else if (!value.payPrice||!value.trialReading) {
            callback(new Error('此项必填'))
          }else if(value.payPrice<0.01||value.payPrice>10000){
            callback(new Error('请输入售价(0.01-10000)'))
          }else if (!reg.test(value.payPrice)) {
            callback(new Error('请输入售价(0.01-10000)'))
          }else if (!regRead.test(value.trialReading)) {
            callback(new Error('请输入试读字数(0-99999)'))
          }else{
            callback()
          }
          // if (value && value.value) {
          //   callback()
          // } else {
          //   callback(new Error('此项必填'))
          // }
        },
        trigger: ['blur', 'change']
      }
      let rules = this.getRules
      // if (this.option.isRequired) 
      rules.push(requiredRule)
      return rules
    }
  },
  methods:{
    changeInput(){
      this.$forceUpdate();
    },
    payreadDetail(){
      this.$request.fetchPayConfig().then(res => {
        if (res.code==200) {
          // this.form.form = res.data;
          // this.tags = res.data.money
          if (!this.form[this.option.name].trialReading) {
            this.form[this.option.name].trialReading = res.data.payRead?res.data.payRead:''
          }
          if (!this.form[this.option.name].payPrice) {
            this.form[this.option.name].payPrice = res.data.payPrice?String(res.data.payPrice):''
          }
          if (!this.form[this.option.name].payRead) {
            this.form[this.option.name].payRead = 0
          }
        }
      })
    }
  },
  mounted(){
    this.payreadDetail()
  },
  activated(){
    this.payreadDetail()
  }
}
</script>
<style lang="scss">
.column-model-main-container{
  .inp{
    // margin-left: 15px;
    input{
      margin-top: -3px;
    }
  }
  .topswitch{
    margin-top: 7px !important;
    margin-right: 20px;
    .el-switch__core::after{
      width:12px;
      height:12px;
      margin-top:0px;
      margin-bottom: 2px;
      margin-left: -13px !important;
    }
    .el-switch__core{
      width:28px!important;
      height:16px;
    }
  }
}
.content-block-right{
  .inp{
    width: 100% !important;
  }
  .payread-content{
    .el-form-item{
      display: block !important;
      &__label{
        float: none;
      }
      &__content{
        .topswitch{
          float: none !important;
        }
        .inpParent{
          display: block !important;
          margin-top: 2px;
          .laoer{
            margin-left: 0px !important;
            margin-top: 4px;
          }
          .inp{
            input{
              margin-top: 5px;
            }
          }
        }
      }
    }
  }
}
.widgetBr{
  .payread-content{
    .el-form-item{
      display: block !important;
      &__label{
        float: none;
      }
      &__content{
        .topswitch{
          float: none !important;
        }
        .inpParent{
          display: block !important;
          .laoer{
            margin-left: 0px !important;
          }
          .inp{
            input{
              margin-top: 5px;
            }
          }
        }
      }
    }
  }
}
.d-radio-preview.new-content{
  .payreadcss{
    .el-form-item__content{
      .el-switch{
        float: left;
      }
      .inpParent{
        display: flex;
        margin-bottom: 10px;
        .laoer{
          margin-left: 10px;
        }
      }
    }
  }
  .inp{
    // padding-left: 20px;
    width: 245px;
    height: 35px;
    box-sizing: border-box;
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
