<script>
import compoment from '@/components/draggable/CustomComponents/Input/DInputPreview'
export default {
  name: 'DUsernamePreview',
  extends: compoment,
  computed: {
    getRules () {
      const { isRequired, inputLimit, rules, disabled } = this.option
      let formItemRules = []
      if (!disabled) {
        formItemRules.push(
          {
            validator: (rule, value, callback) => {
              console.log(value);
              let reg = /^[\w\@\-\u4e00-\u9fa5]{6,18}$/
              if (reg.test(value)) {
                callback()
              }else if(!value){
                callback(new Error('此项必填'))
              }else{
                callback(new Error('请输入6-18位字符，可包含数字，字母，汉字和"_","-","@"'))
              }
              
            },
            trigger: ['blur', 'change']
          }
        )
      }
      if (isRequired) {
        formItemRules.push(this.$rules.required())
      }
      if (inputLimit) {
        formItemRules.push(this.$rules[inputLimit]())
      }
      if (rules instanceof Array && rules.length) {
        rules.forEach(r => {
          const { rule, params = [] } = r
          if (rule) formItemRules.push(this.$rules[rule](...params))
        })
      }
      return formItemRules
    }
  },
  methods: {
    validatorName (rule, value, callback) {
      var params = {
        username: value
      }
      this.$request.fetchSysUsernameUnique(params).then(res => {
        if (res.code === 200) {
          if (res.data) {
            callback()
          } else {
            callback(new Error('用户名已存在'))
          }
        } else {
          callback(new Error(res.message))
        }
      })
    }
  }
}
</script>
