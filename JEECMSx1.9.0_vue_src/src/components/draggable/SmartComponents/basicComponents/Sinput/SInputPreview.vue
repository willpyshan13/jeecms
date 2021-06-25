<template>
  <section class="s-input-preview s-draggable-preview">
    <div v-if="deviceTab === 2">
      <el-form-item
        :label="option.label"
        :prop="option.name"
        :rules="getRules"
      >
        <el-input
          :style="{width:option.width+'%'}"
          autocomplete='new-password'
          class="value"
          v-model="val"
          :label="option.label"
          :placeholder="option.placeholder"
          :maxlength="option.isLengthLimit ? option.max : 120"
          :disabled="option.disabled"
          :readonly="true"
          :type="inputType"
        ></el-input>
        <div class="z-tip-form-item" v-if="option.tip">{{option.tip}}</div>
      </el-form-item>
    </div>
    <div v-else class="smart-mobile-box">
      <el-form-item
        :label="option.label"
        :prop="option.name"
        :rules="getRules"
      >
        <el-input
          autocomplete='new-password'
          v-model="val"
          class="value"
          :label="option.label"
          :placeholder="option.placeholder"
          :maxlength="option.isLengthLimit ? option.max : 120"
          :disabled="option.disabled"
          :type="inputType"
        ></el-input>
        <div class="z-tip-form-item" v-if="option.tip">{{option.tip}}</div>
      </el-form-item>
    </div>
  </section>
</template>

<script>
import previewMixin from '@/components/draggable/Mixin/previewMixin'

export default {
  name: 'SInputPreview',
  mixins: [previewMixin],
  data () {
    return {
      inputType: 'text'
    }
  }
}
</script>
<style lang="scss">
  .smart-mobile-box{
    .el-form-item{
      &__label{
        color: #333333 ;
        font-size: 16px;
      }
    }
  }
</style>
