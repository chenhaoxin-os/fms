<template>
  <div class="sidebar-logo-container" :class="{'collapse':collapse}" :style="logoContainerStyle">
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/system/user">
        <img v-if="logo" :src="logo" class="sidebar-logo" />
        <h1 v-else class="sidebar-title" :style="titleColorStyle">{{ title }} </h1>
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/system/user">
        <img v-if="logo" :src="logo" class="sidebar-logo" />
        <h1 class="sidebar-title" :style="titleColorStyle">{{ title }} </h1>
      </router-link>
    </transition>
  </div>
</template>

<script>
import logoImg from '@/assets/logo/logo.png'
import variables from '@/assets/styles/variables.scss'

export default {
  name: 'SidebarLogo',
  props: {
    collapse: {
      type: Boolean,
      required: true
    }
  },
  computed: {
    // 获取 variables 变量
    variables() {
      return variables
    },
    // 获取侧边栏主题
    sideTheme() {
      return this.$store.state.settings.sideTheme
    },
    // 获取导航类型
    navType() {
      return this.$store.state.settings.navType
    },
    // Logo 容器背景色样式
    logoContainerStyle() {
      if (this.sideTheme === 'theme-dark' && this.navType !== 3) {
        return {
          backgroundColor: this.variables.menuBackground
        }
      } else {
        return {
          backgroundColor: this.variables.menuLightBackground
        }
      }
    },
    // 标题文字颜色样式
    titleColorStyle() {
      if (this.sideTheme === 'theme-dark' && this.navType !== 3) {
        return {
          color: this.variables.logoTitleColor
        }
      } else {
        return {
          color: this.variables.logoLightTitleColor
        }
      }
    }
  },
  data() {
    return {
      title: '身份标识管理信息系统',
      logo: logoImg
    }
  }
}
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  height: 50px;
  line-height: 50px;
  text-align: center;
  overflow: hidden;
  transition: background-color 0.3s ease;

  & .sidebar-logo-link {
    height: 100%;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    & .sidebar-logo {
      width: 32px;
      height: 32px;
      vertical-align: middle;
      margin-right: 12px;
    }

    & .sidebar-title {
      display: inline-block;
      margin: 0;
      font-weight: 600;
      line-height: 50px;
      font-size: 14px;
      font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
      vertical-align: middle;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  &.collapse {
    .sidebar-logo {
      margin-right: 0px;
    }

    .sidebar-title {
      display: none;
    }
  }
}
</style>
