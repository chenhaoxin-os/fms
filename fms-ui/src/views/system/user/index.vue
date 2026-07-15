<template>
  <div class="app-container tree-sidebar-manage-wrap">
    <tree-panel title="组织机构" :tree-data="deptOptions" search-placeholder="请输入部门名称" storage-key="dept-sidebar-width" :defaultExpandAll="true" @node-click="handleNodeClick" @refresh="getDeptTree" ref="deptTreeRef" />
    <div class="tree-sidebar-content">
      <div class="content-inner">
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
          <el-form-item label="姓名" prop="nickName">
            <el-input v-model="queryParams.nickName" placeholder="请输入姓名" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="人员编号" prop="userNumber">
            <el-input v-model="queryParams.userNumber" placeholder="请输入人员编号" clearable style="width: 240px" @keyup.enter.native="handleQuery" />
          </el-form-item>
          <!-- 人员类别（新增） -->
          <el-form-item label="人员类别" prop="personCategory">
            <el-select v-model="queryParams.personCategory" placeholder="请选择人员类别" clearable style="width: 240px">
              <el-option v-for="dict in dict.type.person_category" :key="dict.value" :label="dict.label" :value="dict.value" />
            </el-select>
          </el-form-item>
<!--          <el-form-item label="状态" prop="status">-->
<!--            <el-select v-model="queryParams.status" placeholder="用户状态" clearable style="width: 240px">-->
<!--              <el-option v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.label" :value="dict.value" />-->
<!--            </el-select>-->
<!--          </el-form-item>-->
<!--          <el-form-item label="创建时间">-->
<!--            <el-date-picker v-model="dateRange" style="width: 240px" value-format="yyyy-MM-dd" type="daterange" range-separator="-" start-placeholder="开始日期" end-placeholder="结束日期"></el-date-picker>-->
<!--          </el-form-item>-->
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd" v-hasPermi="['system:user:add']">新增</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate" v-hasPermi="['system:user:edit']">修改</el-button>
          </el-col>
          <el-col :span="1.5">
            <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['system:user:remove']">删除</el-button>
          </el-col>
<!--          <el-col :span="1.5">-->
<!--            <el-button type="info" plain icon="el-icon-upload2" size="mini" @click="handleImport" v-hasPermi="['system:user:import']">导入</el-button>-->
<!--          </el-col>-->
<!--          <el-col :span="1.5">-->
<!--            <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport" v-hasPermi="['system:user:export']">导出</el-button>-->
<!--          </el-col>-->
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
        </el-row>

        <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange" border>
          <el-table-column type="selection" width="50" align="center" />

          <!-- 用户编号（保留但默认隐藏） -->
          <el-table-column label="用户编号" align="center" key="userId" prop="userId" v-if="columns.userId && columns.userId.visible" />

          <!-- 姓名（原用户昵称） -->
          <el-table-column label="姓名" align="center" key="nickName" prop="nickName" v-if="columns.nickName.visible" :show-overflow-tooltip="true" />

          <!-- 流水号 -->
          <el-table-column label="流水号" align="center" key="serialNo" prop="serialNo" v-if="columns.serialNo.visible" :show-overflow-tooltip="true" />

          <!-- 人员类别 -->
          <el-table-column label="人员类别" align="center" key="personCategory" prop="personCategory" v-if="columns.personCategory.visible" :show-overflow-tooltip="true">
            <template slot-scope="scope">
              {{ selectDictLabel(dict.type.person_category, scope.row.personCategory) }}
            </template>
          </el-table-column>

          <!-- 入伍年份 -->
          <el-table-column label="入伍年份" align="center" key="yearOfEnlistment" prop="yearOfEnlistment" v-if="columns.yearOfEnlistment.visible" :show-overflow-tooltip="true" />

          <!-- 性别 -->
          <el-table-column label="性别" align="center" key="sex" v-if="columns.sex.visible">
            <template slot-scope="scope">
              {{ selectDictLabel(dict.type.sys_user_sex, scope.row.sex) }}
            </template>
          </el-table-column>

          <!-- 血型 -->
          <el-table-column label="血型" align="center" key="bloodType" prop="bloodType" v-if="columns.bloodType.visible">
            <template slot-scope="scope">
              {{ selectDictLabel(dict.type.sys_blood_type, scope.row.bloodType) }}
            </template>
          </el-table-column>

          <!-- 过敏史 -->
          <el-table-column label="过敏史" align="center" key="historyOfDrugAllergy" prop="historyOfDrugAllergy" v-if="columns.historyOfDrugAllergy.visible">
            <template slot-scope="scope">
              {{ selectDictLabel(dict.type.sys_history_of_drug_allergy, scope.row.historyOfDrugAllergy) }}
            </template>
          </el-table-column>

          <!-- 人员编号 -->
          <el-table-column label="人员编号" align="center" key="userNumber" prop="userNumber" v-if="columns.userNumber.visible" :show-overflow-tooltip="true" />

          <!-- 状态（保留原有功能，默认显示） -->
          <el-table-column label="状态" align="center" key="status" v-if="columns.status && columns.status.visible">
            <template slot-scope="scope">
              <el-switch v-model="scope.row.status" active-value="0" inactive-value="1" @change="handleStatusChange(scope.row)"></el-switch>
            </template>
          </el-table-column>

          <!-- 创建时间（保留原有功能，默认显示） -->
          <el-table-column label="创建时间" align="center" prop="createTime" v-if="columns.createTime && columns.createTime.visible" width="160">
            <template slot-scope="scope">
              <span>{{ parseTime(scope.row.createTime) }}</span>
            </template>
          </el-table-column>

          <!-- 操作列 -->
          <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
            <template slot-scope="scope" v-if="scope.row.userId !== 1">
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['system:user:edit']">修改</el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['system:user:remove']">删除</el-button>
              <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)" v-hasPermi="['system:user:resetPwd', 'system:user:edit']">
                <el-button size="mini" type="text" icon="el-icon-d-arrow-right">更多</el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="handleResetPwd" icon="el-icon-key" v-hasPermi="['system:user:resetPwd']">重置密码</el-dropdown-item>
<!--                  <el-dropdown-item command="handleAuthRole" icon="el-icon-circle-check" v-hasPermi="['system:user:edit']">分配角色</el-dropdown-item>-->
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
        <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 添加或修改用户配置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="80px">
        <el-row>
          <el-col :span="12">
            <el-form-item label="归属部门" prop="deptId">
              <treeselect v-model="form.deptId" :options="enabledDeptOptions" :show-count="true" placeholder="请选择归属部门" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="nickName">
              <el-input v-model="form.nickName" placeholder="请输入姓名" maxlength="30" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>

          <el-col :span="12">
            <el-form-item label="人员类别"prop="personCategory">
              <el-select v-model="form.personCategory" placeholder="请选择人员类别" @change="fetchSerialNumber">
                <el-option v-for="dict in dict.type.person_category" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入伍年份" prop="yearOfEnlistment">
              <el-date-picker
                v-model="form.yearOfEnlistment"
                type="year"
                placeholder="请选择入伍年份"
                format="yyyy"
                value-format="yyyy"
                clearable
                style="width: auto;"
                @change="fetchSerialNumber"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="流水号" prop="serialNo">
              <el-input v-model="form.serialNo" placeholder="自动生成" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="人员编号" prop="userNumber">
              <el-input v-model="form.userNumber" placeholder="自动生成" disabled />
            </el-form-item>
          </el-col>


        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="用户名" prop="userName">
              <el-input v-model="form.userName" placeholder="请输入用户名" maxlength="30" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item v-if="form.userId == undefined" label="用户密码" prop="password" :rules="pwdValidator">
              <el-input v-model="form.password" placeholder="请输入用户密码" type="password" maxlength="20" show-password />
            </el-form-item>
          </el-col>

        </el-row>
        <el-row>
          <el-col :span="12">
            <el-form-item label="血型" prop="bloodType">
              <el-select v-model="form.bloodType" placeholder="请选择血型">
                <el-option v-for="dict in dict.type.sys_blood_type" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="过敏史" prop="historyOfDrugAllergy">
              <el-select v-model="form.historyOfDrugAllergy" placeholder="请选择过敏史">
                <el-option v-for="dict in dict.type.sys_history_of_drug_allergy" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="12" >
            <el-form-item label="角色" prop="roleIds">
              <el-select v-model="form.roleIds" multiple placeholder="请选择角色">
                <el-option v-for="item in roleOptions" :key="item.roleId" :label="item.roleName" :value="item.roleId" :disabled="item.status == 1"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别"  prop="sex">
              <el-select v-model="form.sex" placeholder="请选择性别">
                <el-option v-for="dict in dict.type.sys_user_sex" :key="dict.value" :label="dict.label" :value="dict.value"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio v-for="dict in dict.type.sys_normal_disable" :key="dict.value" :label="dict.value">{{ dict.label }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 用户详情抽屉 -->
    <user-view-drawer ref="userViewRef" />
    <!-- 用户导入对话框 -->
    <excel-import-dialog ref="importUserRef" title="用户导入" action="/system/user/importData" template-action="/system/user/importTemplate" template-file-name="user_template" update-support-label="是否更新已经存在的用户数据" @success="getList" />
  </div>
</template>

<script>
import { listUser, getUser, delUser, addUser, updateUser, resetUserPwd, changeUserStatus, deptTreeSelect, generateUserNumber } from "@/api/system/user"
import Treeselect from "@riophae/vue-treeselect"
import "@riophae/vue-treeselect/dist/vue-treeselect.css"
import TreePanel from "@/components/TreePanel"
import ExcelImportDialog from "@/components/ExcelImportDialog"
import UserViewDrawer from "./view"
import passwordRule from "@/utils/passwordRule"

export default {
  name: "User",
  mixins: [passwordRule],
  dicts: ['sys_normal_disable', 'sys_user_sex','sys_history_of_drug_allergy', 'sys_blood_type', 'person_category'],
  components: { Treeselect, TreePanel, ExcelImportDialog, UserViewDrawer },
  data() {
    return {
      // 遮罩层
      loading: true,
      // 选中数组
      ids: [],
      // 非单个禁用
      single: true,
      // 非多个禁用
      multiple: true,
      // 显示搜索条件
      showSearch: true,
      // 总条数
      total: 0,
      // 用户表格数据
      userList: null,
      // 弹出层标题
      title: "",
      // 所有部门树选项
      deptOptions: undefined,
      // 过滤掉已禁用部门树选项
      enabledDeptOptions: undefined,
      // 是否显示弹出层
      open: false,
      // 默认密码
      initPassword: undefined,
      // 日期范围
      dateRange: [],
      // 岗位选项
      postOptions: [],
      // 角色选项
      roleOptions: [],
      // 表单参数
      form: {},
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        nickName: undefined,
        userNumber: undefined,
        personCategory: undefined,
        status: undefined,
        deptId: undefined
      },
      // 列信息
      columns: {
        nickName: { label: '姓名', visible: true },
        serialNo: { label: '流水号', visible: true },
        personCategory: { label: '人员类别', visible: true },
        yearOfEnlistment: { label: '入伍年份', visible: true },
        sex: { label: '性别', visible: true },
        bloodType: { label: '血型', visible: true },
        historyOfDrugAllergy: { label: '过敏史', visible: true },
        userNumber: { label: '人员编号', visible: true }
      },
      // 表单校验
      rules: {
        userName: [
          { required: true, message: "用户名称不能为空", trigger: "blur" },
          { min: 2, max: 20, message: '用户名称长度必须介于 2 和 20 之间', trigger: 'blur' }
        ],
        nickName: [
          { required: true, message: "用户昵称不能为空", trigger: "blur" }
        ],
        // 新增角色必填验证
        roleIds: [
          { required: true, message: "请至少选择一个角色", trigger: "blur" }
        ],
        personCategory: [
          { required: true, message: "人员类别不能为空", trigger: "blur" }
        ],
        yearOfEnlistment: [
          { required: true, message: "入伍年份不能为空", trigger: "blur" }
        ],
        sex: [
          { required: true, message: "性别不能为空", trigger: "blur" }
        ],
        deptId: [
          { required: true, message: "请至少选择一个部门", trigger: "blur" }
        ],
        bloodType: [
          { required: true, message: "血型不能为空", trigger: "blur" }
        ],
        historyOfDrugAllergy: [
          { required: true, message: "过敏史不能为空", trigger: "blur" }
        ],

      }
    }
  },
  created() {
    this.getList()
    this.getDeptTree()
    this.getConfigKey("sys.user.initPassword").then(response => {
      this.initPassword = response.msg
    })
  },
  methods: {
    /** 查询用户列表 */
    getList() {
      this.loading = true
      listUser(this.addDateRange(this.queryParams, this.dateRange)).then(response => {
        this.userList = response.rows.filter(item => item.userName !== 'admin')
        this.total = response.total - 1
        this.loading = false
      })
    },
    /** 查询部门下拉树结构 */
    getDeptTree() {
      deptTreeSelect().then(response => {
        this.deptOptions = response.data
        this.enabledDeptOptions = this.filterDisabledDept(JSON.parse(JSON.stringify(response.data)))
      })
    },
    // 过滤禁用的部门
    filterDisabledDept(deptList) {
      return deptList.filter(dept => {
        if (dept.disabled) {
          return false
        }
        if (dept.children && dept.children.length) {
          dept.children = this.filterDisabledDept(dept.children)
        }
        return true
      })
    },
    // 节点单击事件
    handleNodeClick(data) {
      this.queryParams.deptId = data.id
      this.handleQuery()
    },
    // 用户状态修改
    handleStatusChange(row) {
      let text = row.status === "0" ? "启用" : "停用"
      this.$modal.confirm('确认要"' + text + '""' + row.userName + '"用户吗？').then(function() {
        return changeUserStatus(row.userId, row.status)
      }).then(() => {
        this.$modal.msgSuccess(text + "成功")
      }).catch(function() {
        row.status = row.status === "0" ? "1" : "0"
      })
    },
    // 取消按钮
    cancel() {
      this.open = false
      this.reset()
    },
    // 表单重置
    reset() {
      this.form = {
        serialNo: undefined,
        personCategory: undefined,
        yearOfEnlistment: undefined,
        bloodType: undefined,
        historyOfDrugAllergy: undefined,
        userNumber: undefined,
        userId: undefined,
        deptId: undefined,
        userName: undefined,
        nickName: undefined,
        password: undefined,
        sex: undefined,
        status: "0",
        remark: undefined,
        roleIds: []
      }
      this.resetForm("form")
    },
    /** 搜索按钮操作 */
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    /** 重置按钮操作 */
    resetQuery() {
      this.dateRange = []
      this.resetForm("queryForm")
      this.queryParams.deptId = undefined
      this.$refs.deptTreeRef.setCurrentKey(null)
      this.handleQuery()
    },
    // 多选框选中数据
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.userId)
      this.single = selection.length != 1
      this.multiple = !selection.length
    },
    // 更多操作触发
    handleCommand(command, row) {
      switch (command) {
        case "handleResetPwd":
          this.handleResetPwd(row)
          break
        case "handleAuthRole":
          this.handleAuthRole(row)
          break
        default:
          break
      }
    },
    /** 新增按钮操作 */
    handleAdd() {
      this.reset();
      getUser().then(response => {
        this.postOptions = response.posts;
        this.roleOptions = response.roles;
        this.open = true;
        this.title = "添加用户";
        this.form.password = this.initPassword;

        // 设置默认入队年份（当前年份）
        const currentYear = new Date().getFullYear().toString();
        this.form.yearOfEnlistment = currentYear;
        // 如果有默认人员类别，也可设置，否则用户自己选
         this.form.personCategory = '1'; // 示例

        // 调用生成接口获取流水号和人员编号
        this.fetchSerialNumber();
      });
    },

// 新增方法：根据当前表单的人员类别和入队年份生成编号
    fetchSerialNumber() {
      const { personCategory, yearOfEnlistment } = this.form;
      if (!personCategory || !yearOfEnlistment) {
        // 可以提示用户先选择类别和年份
        this.$message.warning('请先选择人员类别和入队年份');
        return;
      }
      generateUserNumber(personCategory, yearOfEnlistment).then(response => {
        this.form.serialNo = response.data.serialNo;
        this.form.userNumber = response.data.userNumber;
        // 将这两个字段设置为只读（通过表单控件的 disabled 或 readonly）
      }).catch(() => {
        // 失败处理
      });
    },
    /** 修改按钮操作 */
    handleUpdate(row) {
      this.reset()
      const userId = row.userId || this.ids
      getUser(userId).then(response => {
        this.form = response.data
        this.postOptions = response.posts
        this.roleOptions = response.roles
        this.$set(this.form, "postIds", response.postIds)
        this.$set(this.form, "roleIds", response.roleIds)
        this.open = true
        this.title = "修改用户"
        this.form.password = ""
      })
    },
    /** 重置密码按钮操作 */
    handleResetPwd(row) {
      this.$prompt(`请输入「${row.userName}」的新密码`, "重置密码", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        closeOnClickModal: false,
        inputValidator: this.pwdPromptValidator
      }).then(({ value }) => {
        resetUserPwd(row.userId, value).then(() => {
          this.$modal.msgSuccess("修改成功，新密码是：" + value)
        })
      }).catch(() => {})
    },
    /** 分配角色操作 */
    handleAuthRole(row) {
      const userId = row.userId
      this.$router.push("/system/user-auth/role/" + userId)
    },
    /** 提交按钮 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.userId != undefined) {
            updateUser(this.form).then(() => {
              this.$modal.msgSuccess("修改成功")
              this.open = false
              this.getList()
            })
          } else {
            addUser(this.form).then(() => {
              this.$modal.msgSuccess("新增成功")
              this.open = false
              this.getList()
            })
          }
        }
      })
    },
    /** 删除按钮操作 */
    handleDelete(row) {
      const userIds = row.userId || this.ids
      this.$modal.confirm('是否确认删除用户编号为"' + userIds + '"的数据项？').then(function() {
        return delUser(userIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    /** 导出按钮操作 */
    handleExport() {
      this.download('system/user/export', {
        ...this.queryParams
      }, `user_${new Date().getTime()}.xlsx`)
    },
    /** 详情按钮操作 */
    handleViewData(row) {
      this.$refs.userViewRef.open(row.userId)
    },
    /** 导入按钮操作 */
    handleImport() {
      this.$refs.importUserRef.open()
    }
  }
}
</script>
<style scoped>
/* 深度选择器覆盖按钮圆角 */
::v-deep .el-button {
  border-radius: 16px;
}
</style>
