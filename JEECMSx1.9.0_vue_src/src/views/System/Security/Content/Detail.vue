<template>
  <section class="security-user-detail-container">
    <base-header v-bind="headers"
      v-on:handleDelete="handleHeaderDelete"
    />
    <base-form
      v-bind="form"
      :rules="rules"
      v-on:handleConfirm="handleConfirm"
    ></base-form>
    
  </section>
</template>
<script>
import baseHeader from '@/components/mixins/baseHeader'
import baseForm from '@/components/mixins/baseForm'

import { mapActions, mapGetters } from 'vuex'

export default {
  name: 'securityContentDetail',
  mixins: [baseHeader, baseForm],
  components: {
    
  },
  data () {
    return {
      titleDig: '',
      predictionTimeConsuming: 0,
      secretCode: '',
      requestStatus: 1,
      secretData: {},
      headers: {
        buttons: [
          {
            type: 'Delete',
            text: 'global.delete',
            icon: 'delete',
            disabled: !this._checkPermission('/secrets', 'DELETE')
          }
        ],
        title: '',
        showAlertIcon: false,
        paddingBottom: '10px'
        // content: 'system.security.handleDesc2'
      },
      form: {
        api: 'fetchSysSecretDetail',
        params: {
          id: ''
        },
        form: {
          id: '',
          name: ''
        },
        formItems: [
          {
            prop: 'name',
            label: 'system.security.name',
            placeholder: 'system.security.namePlaceholder',
            maxlength: 50
          }
        ],
        submitBtns: [{
          text: '保存',
          subType: 'submit',
          disabled: !this._checkPermission('/secrets', 'PUT')
        }]
      }
    }
  },
  computed: {
    ...mapGetters(['setting']),
    // 是否开启内容密级
    openContentSecurity () {
      const { openContentSecurity } = this.setting.attrs || {}
      return openContentSecurity
    },
    rules () {
      return {
        name: [
          this.$rules.required(),
          {
            validator: (rule, value, callback) => {
              if (value) {
                var params = {
                  name: value,
                  secretType: 1,
                  id: this.form.params.id
                }
                this.$request.fetchSysSecretCheck(params).then(res => {
                  if (res.code === 200 && res.data) {
                    callback()
                  } else {
                    callback(new Error('密级名称不能重复'))
                  }
                })
              }
            },
            trigger: 'blur'
          }
        ]
      }
    }
  },
  inject: ['removeTab'],
  methods: {
    ...mapActions('system', ['ReflushSecurityContent', 'FetchContentSecurityOptions']),
    // 顶部操作按钮
    handleHeaderDelete () {
      this.titleDig = '为保证数据安全,删除内容密级后建议重置所有内容的密级,是否删除后立即重置?'
      if (this.openContentSecurity === '0') {
        this._confirmDelete().then(() => {
          this.fetchSysSecretDelete([this.form.form.id], this.requestStatus)
        })
      } else {
        this.secretData = this.form.form
        this.fetchSysSecretDelete([this.form.form.id], this.requestStatus)

      }
    },
    handleConfirm (data) {
      this.form.loading = true
      this.$request.fetchSysSecretUpdate(data).then(res => {
        this.hideFormLoading()
        if (res.code === 200) {
          this._messageSuccess('save')
          this.ReflushSecurityContent(true)
          this.FetchContentSecurityOptions(true)
          this.removeTab(this.$route.fullPath)
          this.$router.replace({ name: 'securityContentIndex' }).catch(() => {})
        }
      }).catch(this.hideFormLoading)
    },
    // 发送请求事件
    fetchSysSecretDelete (ids, requestStatus) {
      this.requestStatus = requestStatus
      let that = this
      this.$request.fetchSysSecretDelete({ secretIds: ids, type: 1, requestStatus: this.requestStatus }).then(res => {
        if (res.code === 200) {
          
          this._messageSuccess('delete')
          this.ReflushSecurityContent(true)
          this.FetchContentSecurityOptions(true)
          this.removeTab(this.$route.fullPath)
          this.$router.replace({ name: 'securityContentIndex' }).catch(() => {})
        }
      })
    },
    fetchFormCallBack (res) {
      if (res.code === 200) {
        this.form.form = res.data
      }
    }
  },
  mounted () {
    const { id } = this.$route.query
    this.form.params.id = id
    this.fetchFormApi()
  },
  activated () {
    const { id } = this.$route.query
    if (id && id !== this.form.params.id) {
      this.form.params.id = id
      this.fetchFormApi()
    }
  }
}
</script>
<style lang="scss" scoped>

</style>
