<template>
  <section class="config-template-detail-container">

    <search-header
      class="search-header"
    v-bind="searchHeader"
    :params="form.form"
    @handleBtnSearch='handleEventBtnSearch'
    ></search-header>
    <p class="html-row">html:</p>
    <el-input class="right-form" type="textarea" v-model="form.form.source" placeholder="请输入模板html" ></el-input>
    <div>
      <el-button class='save-btn' size="small" type='primary' @click="handleConfirm"
        :disabled="!_checkPermission('/template', news ? 'POST' : 'PUT')"
      >保存</el-button>
    </div>
  </section>
</template>

<script>
import baseHeader from '@/components/mixins/baseHeader'
import searchHeader from '@/components/mixins/searchHeader'
import baseForm from '@/components/mixins/baseForm'
import { deepClone } from '../../../utils'

export default {
  name: 'configTemplatesDetail',
  mixins: [baseHeader, searchHeader, baseForm],
  data () {
    return {
      root: '',
      tplType: false,
      headers: {
        buttons: [
          {
            type: 'Create',
            text: '返回',
            icon: 'fanhui'
          }
        ],
        title: '',
        showAlertIcon: true,
        content: '操作说明'
      },
      searchHeader: {
        searchItems: [
          {
            type: 'input',
            value: 'filename',
            placeholder: '请输入模板名称',
            suffix: '.html',
            label: '模板名称：',
            maxlength: 50
          },
          {
            type: 'checkboxInput',
            value: 'models',
            placeholder: '请选择适用模型',
            label: '适用模型：',
            options: [],
            optionLabel: 'modelName',
            optionValue: 'id',
            hiddenKey: true,
            hiddenValue: true
          },
          {
            btnType: 'Search',
            type: 'henderBorderBtn',
            text: '可视化模板编辑'
            // icon: 'liulan'
          }
        ]
      },
      form: {
        api: 'fetchTemplateDetail',
        params: {
        },
        form: {
          filename: '',
          models: []
        },
        formItems: [
          {
            prop: 'source',
            type: 'textarea',
            height: '900px',
            placeholder: '请输入模板内容',
            autosize: { minRows: 20 }
          }
        ],
        rules: {
          filename: [this.$rules.required()],
          source: [this.$rules.required()]
        }
      },
      news: false
    }
  },
  inject: ['removeTab'],
  methods: {
    handleEventBtnSearch () {
      /* system-cms-prefix start */
      let prefix = 'cmsmanager'
      /* system-cms-prefix change let prefix = 'cmsadmin' system-cms-prefix change */
      /* system-cms-prefix end */

      const tokenKey = 'JEECMS-Auth-Token'
      const token = window.localStorage.getItem(tokenKey)
      const siteId = window.localStorage.getItem('siteId')
      const siteName = window.localStorage.getItem('siteName')
      const pUrl = this.$path || window.location.host

      /**
         * 参数：
         * siteId 站点id
         * siteName 站点名称
         * type: 服务名称
         * ShowHide: 判断是否有返回按钮
         * token 用户登录信息
         * url 用户登录地址api
         * code appcode
         * id appid
         * prefix api后缀
        */
      let baseUrl = 'http://ide.jeecms.com'
      // let baseUrl = 'http://ide.yun.jeecms.com'
      let link = `${baseUrl}/edit/?token=${token}&url=${pUrl}&siteId=${siteId}&siteName=${siteName}&prefix=${prefix}&tokenKey=${tokenKey}`
      window.open(link)
      // window.location.href = link
    },
    // 添加模板
    handleConfirm () {
      if (this.root !== 'undefined') {
        this.form.form.root = this.root
      }
      if (!this.form.form.filename) {
        this.$message.error('请填写模板名称！')
        return false
      }

      var reg = /^[\u4e00-\u9fa5|A-Za-z0-9_]+$/
      if (!reg.test(this.form.form.filename)) {
        this.$message.error('模板名称只能输入中文、大小写英文、数字、下划线')
        return false
      }
      if (!this.form.form.source) {
        this.$message.error('请填写模板内容！')
        return false
      }
      this.fetchTemplateUniqueDirName(this.form.form)
    },
    // 检测同名
    fetchTemplateUniqueDirName (data) {
      var obj = {}
      obj.dirName = data.filename + '.html'
      obj.root = this.$encode(data.root)
      this.$request.fetchTemplateUniqueDirName(obj).then(res => {
        if (res.code === 200) {
          if (res.data) {
            this.upTemplate()
          } else {
            this.$message.error('模板名称重名')
            return false
          }
        }
      }).catch(this.hideFormLoading)
    },
    // 新建或修改
    upTemplate () {
      let params = deepClone(this.form.form)
      params.root = this.$encode(params.root)
      params.source = this.$encode(params.source)
      if (this.news) {
        this.$request.fetchTemplateCreate(params).then(res => {
          if (res.code === 200) {
            this.hideFormLoading()
            this.$message({
              message: '新建成功',
              type: 'success'
            })
            this.removeTab(this.$route.fullPath)
            this.$routerLink('/config/templates/index', 'list')
          }
        }).catch(this.hideFormLoading)
      } else {
        this.$request.fetchTemplateUpdate(params).then(res => {
          if (res.code === 200) {
            this.hideFormLoading()
            this.$message({
              message: '保存成功',
              type: 'success'
            })
            this.removeTab(this.$route.fullPath)
            this.$routerLink('/config/templates/index', 'list')
          }
        }).catch(this.hideFormLoading)
      }
    },
    // 获取适用模型列表
    fetchTemplateModelList (tplType) {
      this.$request.fetchTemplateModelList({ tplType }).then(res => {
        if (res.code === 200) {
          this.searchHeader.searchItems[1].options = res.data
        }
      }).then(this.hideTableLoading)
        .catch(this.hideTableLoading)
    },
    // 获取模板内容
    fetchFormCallBack (res) {
      if (res.code === 200) {
        this.form.form = res.data
        this.form.form.filename = this.form.form.filename.substring(0, this.form.form.filename.length - 5)
      }
    }
  },
  activated () {
    this.form.form = {}
    const { root, html, news } = this.$route.query
    this.root = root
    if (root.indexOf('content') > -1) {
      this.tplType = 2
      this.fetchTemplateModelList(this.tplType)
      this.searchHeader.searchItems[1].hiddenKey = false
    } else if (root.indexOf('channel') > -1) {
      this.tplType = 1
      this.fetchTemplateModelList(this.tplType)
      this.searchHeader.searchItems[1].hiddenKey = false
    } else {
      this.tplType = true
      this.searchHeader.searchItems[1].hiddenKey = true
    }
    if (html) {
      this.form.params.name = this.$encode(root)
      this.form.params.id += 1
      this.fetchFormApi()
    }
    if (news) {
      this.news = news
    } else {
      this.news = false
    }
  }
}
</script>

<style lang="scss">
.config-template-detail-container{
  background-color: #ffffff;
  height: 100%;
  display: flex;
  flex-direction: column;
  .left-btn{
    height: 50px;
  }
  .html-row{
    color:rgba(51,51,51,1);
    font-size:14px;
    padding: 20px 0 10px;
  }
  .save-btn{
    margin-top: 30px;
  }
  .right-form{
    flex: 1;
    overflow-y: scroll;
    textarea{
      height: 100%;
      overflow-y: scroll;
    }
  }
}
</style>
