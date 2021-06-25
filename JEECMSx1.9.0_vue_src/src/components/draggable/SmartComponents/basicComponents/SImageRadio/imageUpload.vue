<template>
  <section class="s-image-checkimg-upload-ques">
    <div class="upload-img" v-if="picUrl||value.picUrl"  @click="handlePreview">
        <el-image class="t-img"
          :src="$getPath(picUrl||value.picUrl)"
          fit="contain"></el-image>
          <div class="upload-mode">
            <jee-icon iconClass="guanbi" @click.stop="delPic"
              :style="{fill:'#fff',fontSize:'11px'}"></jee-icon>
          </div>
      </div>
      <div class="upload-btn" v-else  @click="showUpload">
        <jee-icon iconClass="shangchuantupian" class="jee-hover-fill"></jee-icon>
      </div>
      <!-- 放大 -->
      <el-dialog :visible.sync="dialogVisible"
        class="image-preview-dialog"
        append-to-body custom-class="transparent">
        <img class="t-img" :src="picUrl||value.picUrl" alt="">
      </el-dialog>
      <upload
        ref="imageUpload"
        resourceType="image"
        @handleConfirm="handleConfirmImg"
      ></upload>
  </section>
</template>

<script>
import Upload from '@/components/editor/Upload'
export default {
  name: 'iamge-upload',
  props: {
    value: Object
  },
  components: {
    Upload
  },
  computed: {
    id: {
      get () {
        return this.value.pic
      },
      set (val) {
        // this.value.pic = val
        this.$emit('input', {
          ...this.value,
          pic: val,
          picUrl: this.picUrl
        })
      }
    }
  },
  data () {
    return {
      dialogVisible: false,
      file: {},
      picUrl: ''
    }
  },
  methods: {
    // 点击放大查看
    handlePreview () {
      this.dialogVisible = true
    },
    showUpload () {
      this.$refs.imageUpload.showDialog()
    },
    handleConfirmImg (html, data, file) {
      if (data) {
        this.picUrl = data.url
        this.id = data.id
      } else {
        this.picUrl = ''
        this.id = ''
      }
    },
    delPic () {
      this.picUrl = ''
      this.id = ''
    }
  }
}
</script>

<style lang="scss">
.s-image-checkimg-upload-ques{
  position: relative;
  // z-index: 1002;
    .btn-88{
      width: 88px;
      padding: 10px 20px !important;
      font-size: 12px !important;
    }
    .upload-btn{
      width: 38px;
      height: 26px;
      border: 1px dashed #e2e2e2;
      border-radius: 4px;
      padding-top: 12px;
      z-index: 1;
    }
    .upload-img{
      width: 40px;
      height: 40px;
      border-radius: 4px;
      overflow: hidden;
      position: relative;
      margin-top: -27px;
      top: 13px;
      .el-image{
        background: #f0f0f0;
      }
      .upload-mode{
        position: absolute;
        left: 0;
        top: 0;
        bottom: 0;
        right: 0;
        text-align: right;
        background-color: rgba(0, 0, 0, 0.7);
        display: none;
        .jee-svg-icon{
          vertical-align: top;
          cursor: pointer;
          margin: 0;
        }
      }
      &:hover{
        .upload-mode{
          display: block;
        }
      }
    }
}

</style>
