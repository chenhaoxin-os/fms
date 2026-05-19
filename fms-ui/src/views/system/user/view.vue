<template>
  <el-drawer title="用户信息详情" :visible.sync="visible" direction="rtl" size="68%" append-to-body :before-close="handleClose" custom-class="detail-drawer">
    <div v-loading="loading" class="drawer-content">
      <!-- 基本信息 -->
      <h4 class="section-header">基本信息</h4>
      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">归属部门：</label>
            <span class="info-value plaintext">{{ info.dept && info.dept.deptName }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">姓名：</label>
            <span class="info-value plaintext">{{ info.nickName }}</span>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">流水号：</label>
            <span class="info-value plaintext">{{ info.serialNo || '-' }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">人员类别：</label>
            <!-- 使用计算属性 -->
            <span class="info-value plaintext">{{ personCategoryLabel }}</span>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">入伍年份：</label>
            <span class="info-value plaintext">{{ info.yearOfEnlistment || '-' }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">用户性别：</label>
            <span class="info-value plaintext">{{ sexLabel }}</span>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">登录账号：</label>
            <span class="info-value plaintext">{{ info.userName }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">用户状态：</label>
            <span class="info-value plaintext">
            <el-tag size="small" :type="info.status === '0' ? 'success' : 'danger'">{{ info.status === '0' ? '正常' : '停用' }}</el-tag>
          </span>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">血型：</label>
            <!-- 使用计算属性 -->
            <span class="info-value plaintext">{{ bloodTypeLabel }}</span>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">过敏史：</label>
            <!-- 使用计算属性 -->
            <span class="info-value plaintext">{{ historyOfDrugAllergyLabel }}</span>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="mb8">
        <el-col :span="12">
          <div class="info-item">
            <label class="info-label">人员编号：</label>
            <span class="info-value plaintext">{{ info.userNumber || '-' }}</span>
          </div>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="mb8">
        <el-col :span="24">
          <div class="info-item full-width">
            <label class="info-label">角色：</label>
            <span class="info-value plaintext">{{ roleNames || '无角色' }}</span>
          </div>
        </el-col>
      </el-row>
    </div>
  </el-drawer>
</template>

<script>
import { getUser } from '@/api/system/user'

export default {
  name: 'UserViewDrawer',
  dicts: ['sys_user_sex','person_category','sys_blood_type','sys_history_of_drug_allergy'],
  data() {
    return {
      visible: false,
      loading: false,
      info: {},
      postOptions: [],
      roleOptions: []
    }
  },
  computed: {
    sexLabel() {
      return this.selectDictLabel(this.dict.type.sys_user_sex, this.info.sex) || '-'
    },
    // 人员类别
    personCategoryLabel() {
      return this.selectDictLabel(this.dict.type.person_category, this.info.personCategory) || '-'
    },
    // 血型
    bloodTypeLabel() {
      return this.selectDictLabel(this.dict.type.sys_blood_type, this.info.bloodType) || '-'
    },
    // 过敏史
    historyOfDrugAllergyLabel() {
      return this.selectDictLabel(this.dict.type.sys_history_of_drug_allergy, this.info.historyOfDrugAllergy) || '-'
    },
    postNames() {
      if (!this.postOptions.length) return ''
      const ids = this.info.postIds || []
      return this.postOptions.filter(p => ids.includes(p.postId)).map(p => p.postName).join('、') || ''
    },
    roleNames() {
      if (!this.roleOptions.length) return ''
      const ids = this.info.roleIds || []
      return this.roleOptions.filter(r => ids.includes(r.roleId)).map(r => r.roleName).join('、') || ''
    }
  },
  methods: {
    open(userId) {
      this.visible = true
      this.loading = true
      getUser(userId).then(res => {
        this.info = res.data || {}
        this.postOptions = res.posts || []
        this.roleOptions = res.roles || []
        this.info.postIds = res.postIds || []
        this.info.roleIds = res.roleIds || []
      }).finally(() => {
        this.loading = false
      })
    },
    handleClose() {
      this.visible = false
    }
  }
}
</script>
