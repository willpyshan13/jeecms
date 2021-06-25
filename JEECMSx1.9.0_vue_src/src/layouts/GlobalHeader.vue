<template>
  <section class="header-container jee-bg-header">
    <div class="header-left">
      <div class="collapse" @click="toggleCollapse">
        <jee-icon iconClass="hanbao"></jee-icon>
      </div>
      <!-- <jee-icon iconClass="JEECMS" class="logo"></jee-icon> -->
      <img :src="$getPath(setting.attrs.sysImgUrl)" alt="" style="width:auto;height:20px;margin-left:20px;">
      <!-- <el-image :src="$getPath(setting.attrs.sysImgUrl)" fit="contain" style="width:108px;height:20px"></el-image> -->
      <span v-if="reinsuranceShow.hasShow&&reinsuranceShow.name"
        style="padding:5px 8px;border:1px solid #698490;margin-left:20px;border-radius:4px;font-size:14px">{{reinsuranceShow.name}}级系统</span>
    </div>
    <div class="header-right">
      <!-- 用户信息 -->
      <jee-icon iconClass="touxiang" class="avatar"></jee-icon>
      <div class="username">
        <el-dropdown>
          <span class="el-dropdown-link">
            {{$t('layout.welcome')}}，{{user.userName}}
            <span v-if="reinsuranceShow.hasShow&&reinsuranceShow.userSecret"
        style="padding:4px 8px;margin-left:12px;font-size:14px;background-color:#698490;color:#fff;border-radius:4px">{{reinsuranceShow.userSecret}}</span>
            <i class="el-icon-arrow-down el-icon--right"></i>
          </span>
          <el-dropdown-menu slot="dropdown" class="username-dropdown">
            <el-dropdown-item>
              <div class="logininfo">
                <div class="title">
                  <jee-icon iconClass="shijian"/>
                  <span>最后登录</span>
                </div>
                <div class="time">
                  <div>{{user.lastLoginTime}}</div>
                  <div v-if="user.lastLoginIP">IP: {{user.lastLoginIP}}</div>
                </div>
                <dir class="btn-password">
                  <el-link :underline="false" type="primary" @click="handleClickLogout">
                    <span>退出</span>
                  </el-link>
                  <el-link :underline="false" type="primary" @click="handleClickPassword">
                    <jee-icon iconClass="bianji"/>
                    <span>修改密码</span>
                  </el-link>
                </dir>
              </div>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <div class="split"></div>
      <!-- 通知 -->
      <div class="notice" @click="jumpInMail" title="消息通知">
        <el-badge :value="notice" v-if="notice">
          <jee-icon iconClass="tongzhi"></jee-icon>
        </el-badge>
        <jee-icon iconClass="tongzhi" v-else></jee-icon>
      </div>
      <div class="split hidden-sm-and-down"></div>
      <!-- 前台 -->
      <div class="screen hidden-sm-and-down" title="前台演示站">
        <jee-icon iconClass="diannao" @click="handleDomain"></jee-icon>
      </div>
      <div class="split hidden-sm-and-down"></div>
      <!-- 主题 -->
      <div class="style hidden-sm-and-down" @click="handleOpenStyles" title="主题色">
        <jee-icon iconClass="yansezhuti"></jee-icon>
        <i class="el-icon-arrow-down el-icon--right"></i>
      </div>
      <div class="split"></div>
      <!-- 站点 -->
      <div class="site" v-if="ownSiteOptions && ownSiteOptions.length">
        <el-dropdown @command="handleCommandSite" placement='bottom-start'>
          <span class="el-dropdown-link">
            {{currentSiteName}}<i class="el-icon-arrow-down el-icon--right"></i>
          </span>
          <el-dropdown-menu slot="dropdown" class="owner-site-dropdown scrollbar">
            <el-dropdown-item v-for="(item, index) in ownSiteOptions"
              :key="index" :command="item"
              :class="{selected:item.id===currentSiteId}"
            >{{item.label}}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <div class="split hidden-xs-only"></div>
      <!-- 语言 -->
      <div class="lang hidden-xs-only">
        <el-dropdown @command="handleCommandLang" placement='bottom-start'>
          <span class="el-dropdown-link">
            {{langs[langIndex].label}}<i class="el-icon-arrow-down el-icon--right"></i>
          </span>
          <el-dropdown-menu slot="dropdown" class="lang-dropdown">
            <el-dropdown-item
              v-for="(item, index) in langs"
              :key="index"
              :class="{selected:index===langIndex}"
              :command="index"
            >{{item.label}}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <!-- 修改密码 -->
      <password-dialog
        ref="passwordDialog"
      ></password-dialog>
    </div>
    <!-- 主题下拉 -->
    <div class="styles-wrap scrollbar"
      :class="{'is-opened': stylesOpened}"
      @mouseleave="stylesOpened = false"
    >
      <div class="style-item" v-for="(item, index) in themes" :key="index"
        @mouseover="currentStyle = index"
        @mouseleave="currentStyle = null"
        @click="()=>onClickStyle(item.bodyClass)"
      >
        <div class="color" :class="{'is-select': theme === item.bodyClass}" :style="{borderColor: item.color}" >
          <img :src="item.img" alt="">
          <div class='tag' :style="{borderTopColor: item.color}" v-if="theme === item.bodyClass"></div>
          <jee-icon class='tag2' iconClass='iconfontcheck' v-if="theme === item.bodyClass"></jee-icon>
        </div>
        <div class="name"
          :style="{color: currentStyle === index ? item.color : null}"
        >{{$t('layout.styles.'+item.name)}}</div>
      </div>
    </div>
    
    <!-- 一定要修改密码吗 -->
    
  </section>

</template>

<script>
import PasswordDialog from '@/components/dialog/PasswordDialog'

import {
  mapState,
  mapActions,
  mapGetters
} from 'vuex'

export default {
  name: 'GlobalHeader',
  props: ['collapsed'],
  components: {
    PasswordDialog,
    
  },
  data () {
    return {
      stylesOpened: false,
      currentStyle: '',
      showClose: true,
      
    }
  },
  computed: {
    ...mapState('app', ['user', 'theme', 'themes', 'langs', 'notice', 'setting']),
    ...mapGetters('app', ['langIndex']),
    ...mapGetters(['ownSiteOptions', 'currentSiteName', 'currentSiteId', 'siteBaseConfig']),
    ...mapGetters('system', ['reinsuranceShow'])
  },
  methods: {
    ...mapActions('app', ['changeTheme', 'changeLang', 'fetchNotice', 'fetchLogout']),
    ...mapActions('config', ['FetchSitesOwnsite']),
    
    toggleCollapse () {
      this.$emit('toggleCollapse')
    },
    handleDomain () {
      const { url } = this.siteBaseConfig
      if (url) window.open(url)
    },
    onClickStyle (className) {
      this.changeTheme(className)
      this.stylesOpened = false
    },
    handleOpenStyles (event) {
      event.stopPropagation()
      this.stylesOpened = true
    },
    handleClickLogout () {
      this.fetchLogout()
      this.$routerLink(`/login?redirect=${escape(this.$route.fullPath)}`)
      // location.reload()
    },
    handleClickPassword () {
      this.$refs.passwordDialog.showDialog()
    },
    handleCommandLang (index) {
      if (index === this.langIndex) return
      const lang = this.langs[index].name
      this.$i18n.locale = lang
      this.changeLang(lang)
    },
    handleCommandSite (item) {
      if (item.id === this.currentSiteId) return
      window.localStorage.setItem('siteId', item.id)
      window.localStorage.setItem('siteName', item.name)
      window.location.reload()
    },
    // 跳站内信
    jumpInMail () {
      this.$routerLink('/inMail')
    },
    
  },
  mounted () {
    
    this.fetchNotice()
    
    
    // this.FetchSitesOwnsite()
  }
}
</script>
<style lang="scss">
.owner-site-dropdown,
.username-dropdown,
.lang-dropdown{
  margin-top: 0!important;
}
.dialog-item{
  .el-dialog{
    .el-dialog__header{
      padding: 30px 30px 18px !important;
    }

  }
}
.owner-site-dropdown{
  height: 200px;
  overflow: hidden auto;
}
.username-dropdown{
  width: 176px;
  .el-dropdown-menu__item{
    padding: 0 10px;
    &:hover{
      background-color: #ffffff!important;
    }
  }
  .logininfo{
    .title{
      color: #999999;
      font-size: 12px;
      line-height: 12px;
      .jee-svg-icon{
        fill: #999999;
        font-size: 12px;
        padding-right: 10px;
      }
    }
    .time{
      color: #333333;
      font-size: 14px;
      line-height: 22px;
      padding-left: 22px;
      padding-top: 16px;
    }
    .btn-password{
      display: flex;
      justify-content: space-between;
      margin: 0;
      font-size: 12px;
      line-height: 12px;
      padding: 14px 6px 5px 0;
      .jee-svg-icon{
        font-size: 14px;
        padding-right: 10px;
      }
    }
  }
}
</style>

<style lang="scss" scoped>
.header-container{
  height: 50px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #698490;
  position: relative;
  .collapse,
  .notice,
  .style,
  .screen {
    color: #698490;
    .jee-svg-icon{
      font-size: 20px;
    }
    &:hover{
      color: #ffffff;
    }
  }
  .header-left,
  .header-right{
    display: flex;
    justify-content: flex-start;
    align-items: center;

  }
  .header-left{
    .collapse{
      cursor: pointer;
      width: 75px;
      height: 18px;
      border-right: 1px solid rgba(96,125,139,0.2);
      text-align: center;
      .jee-svg-icon{
        font-size: 18px
      }
    }
    .logo{
      padding-left: 24px;
      font-size: 109px;
    }
  }
  .header-right{
    justify-content: flex-end;
    margin-left: auto;
    .username,
    .site,
    .style,
    .notice,
    .screen,
    .lang {
      color: #698490;
      cursor: pointer;
      .jee-svg-icon{
        fill:#698490
      }
      .el-dropdown {
        color: #698490;
        padding: 15px 0;
        line-height: 20px;
        .el-icon-arrow-down.el-icon--right{
          color: #698490;
          margin-left: 14px;
        }
      }
      &:hover{
        color: #ffffff;
        .jee-svg-icon{
          fill:#ffffff
        }
        .el-dropdown{
          color: #ffffff;
          .el-icon-arrow-down{
            color: #ffffff;
          }
        }
      }
    }
    .avatar{
      font-size: 31px;
      padding-right: 10px;
      fill:#698490;
      color:#486573;
      &:hover{
        fill:#ffffff;
        color:#cccccc;
      }
    }
    .username .el-dropdown{
      padding-right: 20px;
    }
    .notice{
      padding: 0 25px;
      .jee-svg-icon{
        font-size: 16px;
      }
    }
    .screen{
      padding: 0 22px;
    }
    .style{
      padding: 0 20px;
      .jee-svg-icon{
        font-size: 20px;
      }
      .el-icon-arrow-down{
        margin-left: 10px;
      }
    }
    .site {
      .el-dropdown{
        padding: 15px 20px;
      }
    }
    .lang .el-dropdown{
      padding-left: 20px;
      padding-right: 50px;
    }
    .split{
      width: 1px;
      height: 24px;
      background: rgba(105,132,144,0.3);
    }
  }
  .styles-wrap{
    position: absolute;
    top: 50px;
    left: 0px;
    width: 100%;
    height: 245px;
    overflow-x: auto;
    overflow-y: hidden;
    box-sizing: border-box;
    background-color: #ffffff;
    transform: scaleY(0);
    transition: all ease-out 200ms;
    transform-origin: top;
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 37px;
    z-index: 3;
    box-shadow:0px -2px 40px 0px rgba(0, 0, 0, 0.18);
    .style-item{
      cursor: pointer;
      .color{
        border-radius: 4px;
        border: 1px solid #ffffff;
        width: 216px;
        height: 120px;
        box-sizing: border-box;
        transform: scale(1);
        transition: transform 100ms;
        position: relative;
        .is-select{
          border-width: 3px;
        }
        img{
          width: 100%;
          height: 100%;
        }
        .tag{
          position: absolute;
          top: 0;
          right: 0;
          width: 0;
          height: 0;
          border-top: 30px solid transparent;
          border-left: 30px solid transparent;
        }
        .tag2{
          z-index: 1;
          position: absolute;
          top: 4px;
          right: 4px;
          font-size: 12px;
        }
        &:hover{
          transform: scale(1.2);
        }
      }
      .name{
        font-size: 16px;
        padding-top: 30px;
        color: #333333;
        text-align: center;
      }
      &:nth-of-type(n+2){
        margin-left: 50px;
      }
      &:last-of-type{
        margin-right: 57px;
      }
    }
    &.is-opened{
      transform: scaleY(1);
    }
  }
  .dialog-item{
    .dialog-text{
      color: #999999;
      font-size: 14px;
      // margin: -2px 0;
      z-index: 9999;
      p:nth-child(2){
        margin-top: 5px;
      }
    }
    .dialog-form{
      margin-right: 80px;
      text-align: right;
      .form-phone{
        margin-top: 18px;
        .phone-item{
          display: flex;
          align-items: center;
          justify-content: flex-end;
          .phone-lable{
            margin-right: 20px;
          }
          .phone-input{
            width: 300px;
            height: 32px;
            .el-input{
              width: 100%;
              height: 100%;
            }
          }
        }

      }
      .form-code{
        margin-top: 20px;
        .code-item{
          display: flex;
          align-items: center;
          justify-content: flex-end;
          .code-lable{
            margin-right: 20px;
          }
          .code-input{
            width: 205px;
            height: 32px;
            .el-input{
              width: 100%;
              height: 100%;
            }
          }
          .code-but,.code-hide{
            margin-left: 5px;
            color: #fff;
            width: 90px;
            text-align: center;
            height: 30px;
            line-height: 30px;
            border-radius: 4px;
            cursor: pointer;
          }
          .code-but{
            background: #1EC6DF;
            cursor: pointer;

          }
          .code-hide{
            background: #cccccc;
          }
        }
      }
      .form-tip{
        width: 200px;
        margin-left: 170px;
        color: #FF0000;
        font-size: 12px;
        margin-top: 6px;
        text-align: left;
      }
      .item-red{
        color: #FF0000;
        margin-right: 5px;
      }
      .form-add,.form-show{
        width: 300px;
        height: 32px;
        line-height: 32px;
        border-radius: 4px;
        color: #fff;
        text-align: center;
        cursor: pointer;
        float: right;
        margin-top: 30px;
      }
      .form-show{
        background: #ccc;
      }
    }
  }
}
</style>
