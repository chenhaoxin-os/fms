<template>
  <div class="app-container">
    <!-- 查询表单 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px" class="search-form">
      <el-form-item label="车牌号" prop="plate">
        <el-input v-model="queryParams.plate" placeholder="请输入车牌号" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="车辆名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入车辆名称" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleMuitDelete">删除</el-button>
      </el-col>
      <el-col :span="1.5">
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="vehicleList"
        @selection-change="handleSelectionChange"
        border
        stripe
        style="width: 100%">
        <el-table-column type="selection" width="55" align="center" fixed="left" />
        <el-table-column label="ID" align="center" prop="id" width="80" />
        <el-table-column label="车牌号" align="center" prop="plate" min-width="130" />
        <el-table-column label="车辆名称" align="center" prop="name" min-width="120" />
        <el-table-column label="核载人数" align="center" prop="maxCapacity" width="100" />
        <el-table-column label="设备电源" align="center" prop="devicePower" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.devicePower ? 'success' : 'danger'">
              {{ scope.row.devicePower ? '已送电' : '未送电' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="运行状态" align="center" prop="isRunning" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.isRunning ? 'primary' : 'info'">
              {{ scope.row.isRunning ? '运行中' : '待命' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="固定人员" align="center" prop="stableCrew" min-width="200">
          <template slot-scope="scope">
            <el-tag v-for="(item, index) in parseCrew(scope.row.stableCrew)" :key="index" size="small" style="margin-right: 4px; margin-bottom: 2px;">
              {{ item.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" width="180" fixed="right">
          <template slot-scope="scope">
            <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
            <!-- 新增：解绑按钮（仅当 deviceId 不为空时显示） -->
            <el-button
              v-if="scope.row.deviceId"
              size="mini"
              type="text"
              icon="el-icon-connection"
              style="color: #e6a23c;"
              @click="handleUnbind(scope.row)"
            >解绑</el-button>
            <el-button size="mini" type="text" icon="el-icon-delete" style="color: #f56c6c;" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="车牌号" prop="plate">
          <el-input v-model="form.plate" placeholder="请输入车牌号" />
        </el-form-item>
        <el-form-item label="车辆名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入车辆名称" />
        </el-form-item>
        <el-form-item label="核载人数" prop="maxCapacity">
          <el-input-number v-model="form.maxCapacity" :min="1" :max="50" />
        </el-form-item>
        <el-form-item label="关联装置" prop="deviceId">
          <el-select
            v-model="form.deviceId"
            placeholder="请选择关联装置"
            clearable
            filterable
            :loading="deviceLoading"
            style="width: 100%"
          >
            <el-option
              v-for="item in deviceOptions"
              :key="item.id"
              :label="item.name + '（' + item.deviceId + '）'"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="设备电源" prop="devicePower">
          <el-radio-group v-model="form.devicePower">
            <el-radio :label="true">已送电</el-radio>
            <el-radio :label="false">未送电</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="运行状态" prop="isRunning">
          <el-radio-group v-model="form.isRunning">
            <el-radio :label="true">运行中</el-radio>
            <el-radio :label="false">待命</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="固定人员" prop="stableCrew">
          <el-input
            v-model="crewNames"
            type="textarea"
            :rows="3"
            placeholder="请输入人员姓名，多个请用英文逗号分隔，如：张三,李四,王五"
          />
          <span style="font-size: 12px; color: #909399;">提示：输入后请用英文逗号隔开</span>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getVehicleList, addVehicle, updateVehicle, delVehicle, getVehicleDetail, unbindDevice } from "@/api/system/vehicle";
import { listDevice } from "@/api/monitor/device";   // 根据您实际路径
import Pagination from "@/components/Pagination";
import RightToolbar from "@/components/RightToolbar";

export default {
  name: "VehicleManagement",
  components: { Pagination, RightToolbar },
  data() {
    return {
      deviceOptions: [],      // 装置下拉选项
      deviceLoading: false,   // 加载装置列表时的 loading
      loading: false,
      showSearch: true,
      vehicleList: [],
      total: 0,
      multiple: true,
      ids: [],  // ⬅️ 新增这一行
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        plate: undefined,
        name: undefined
      },
      open: false,
      title: '',
      form: {
        id: undefined,
        plate: '',
        name: '',
        maxCapacity: 8,
        devicePower: true,
        isRunning: true,
        stableCrew: [],
        remark: '',
        deviceId: undefined,   // 新增
      },
      crewNames: '', // 用于界面输入的人员字符串
      rules: {
        plate: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
        name: [{ required: true, message: '请输入车辆名称', trigger: 'blur' }],
        maxCapacity: [{ required: true, message: '请输入核载人数', trigger: 'blur' }]
      }
    };
  },
  created() {
    this.getList();
    this.loadDeviceList();
  },
  methods: {
    /** 查询车辆列表 */
    getList() {
      this.loading = true;
      getVehicleList(this.queryParams).then(response => {
        this.vehicleList = response.rows || [];
        this.total = response.total || 0;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    /** 搜索 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置 */
    resetQuery() {
      this.queryParams = { pageNum: 1, pageSize: 10, plate: undefined, name: undefined };
      this.getList();
    },
    /** 加载装置列表（用于下拉选项） */
    loadDeviceList() {
      this.deviceLoading = true;
      listDevice({ pageNum: 1, pageSize: 1000, type: '车载式'  })   // 取所有装置（可酌情增加分页）
        .then(res => {
          this.deviceOptions = res.rows || [];
          this.deviceLoading = false;
        })
        .catch(() => {
          this.deviceOptions = [];
          this.deviceLoading = false;
        });
    },
    /** 多选 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.multiple = !selection.length;
    },
    /** 取消弹窗 */
    cancel() {
      this.open = false;
      this.resetForm();
    },
    resetForm() {
      this.form = {
        id: undefined,
        plate: '',
        name: '',
        maxCapacity: 8,
        devicePower: true,
        isRunning: true,
        stableCrew: [],  // 初始化为空数组
        remark: '',
        deviceId: undefined,   // 重置
      };
      this.crewNames = '';
      this.$refs.form && this.$refs.form.resetFields();
    },
    /** 解绑装置 */
    handleUnbind(row) {
      this.$confirm(
        `确认解除车辆「${row.name}」（${row.plate}）与当前装置的关联吗？解绑后该装置可被其他车辆使用。`,
        '解绑确认',
        {
          confirmButtonText: '确定解绑',
          cancelButtonText: '取消',
          type: 'warning'
        }
      ).then(() => {
        return unbindDevice(row.id);
      }).then(() => {
        this.$message.success('解绑成功');
        this.getList();
      }).catch((err) => {
        if (err !== 'cancel') {
          this.$message.error(err.response?.data?.msg || '解绑失败');
        }
      });
    },
    /** 新增 */
    handleAdd() {
      this.resetForm();
      this.title = '新增车辆';
      this.open = true;
    },
    handleUpdate(row) {
      this.resetForm();
      const vehicleId = row.id;
      // 确保装置列表已加载
      const loadPromise = this.deviceOptions.length > 0
        ? Promise.resolve()
        : this.loadDeviceList();

      loadPromise.then(() => {
        getVehicleDetail(vehicleId).then(response => {
          this.form = response.data;
          // 回显人员（兼容字符串和数组）
          if (this.form.stableCrew) {
            try {
              const crewData = typeof this.form.stableCrew === 'string'
                ? JSON.parse(this.form.stableCrew)
                : this.form.stableCrew;
              if (Array.isArray(crewData)) {
                this.crewNames = crewData.map(item => item.name).join(',');
              } else {
                this.crewNames = '';
              }
            } catch (e) {
              this.crewNames = '';
            }
          } else {
            this.crewNames = '';
          }
          this.title = '修改车辆';
          this.open = true;
        });
      });
    },
    /** 提交 */
    submitForm() {
      this.$refs.form.validate(valid => {
        if (valid) {
          // 构造人员数组
          let crewArray = [];
          if (this.crewNames.trim()) {
            const names = this.crewNames.split(',').filter(s => s.trim() !== '');
            crewArray = names.map(name => ({ name: name.trim() }));
          }
          // 直接赋值给 form.stableCrew（数组）
          this.form.stableCrew = crewArray;

          // 提交数据（axios 会自动将数组序列化为 JSON）
          const action = this.form.id ? updateVehicle : addVehicle;
          action(this.form).then(() => {
            this.$message.success(this.form.id ? '修改成功' : '新增成功');
            this.open = false;
            this.getList();
          }).catch(error => {
            this.$message.error(error.response?.data?.msg || '操作失败');
          });
        }
      });
    },
    handleMuitDelete() {
      if (!this.ids.length) {
        this.$message.warning('请选择要删除的数据');
        return;
      }
      this.$confirm('是否确认删除编号为"' + this.ids.join(',') + '"的数据项？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
         delVehicle(this.ids.join(','), { _silentError: true }).then((res) => {
          // ========== 新增：根据后端返回的 code 判断 ==========
          if (res.code === 200) {
            this.getList();
            this.$message.success('删除成功');
          } else {
            // 业务失败，直接展示后端返回的 msg
            const msg = res.msg || '删除失败';
            this.$alert(msg, '删除失败', {
              confirmButtonText: '确定',
              dangerouslyUseHTMLString: true, // 支持 <br> 等 HTML 标签
              type: 'error'
            });
          }
        }).catch((error) => {
          // 处理网络异常或其他非业务错误（如请求超时、取消等）
          if (error === 'cancel') return; // 用户取消确认框

          // 提取错误信息（兼容多种错误结构）
          let msg =
            error.response?.data?.msg ||
            error.response?.data?.message ||
            error.response?.data ||
            error.message ||
            error.toString();

          if (typeof msg === 'string' && msg.startsWith('Error: ')) {
            msg = msg.substring(7);
          }

          if (!msg || msg === '[object Object]') {
            msg = '删除失败，请稍后重试';
          }

          this.$alert(msg, '删除失败', {
            confirmButtonText: '确定',
            dangerouslyUseHTMLString: true,
            type: 'error'
          })
        })
      })
    },
    /** 删除 */
    handleDelete(row) {
      const ids = row ? [row.id] : this.ids;
      if (!this.ids.length) {
        this.$message.warning('请选择要删除的数据');
        return;
      }
      this.$confirm('是否确认删除编号为"' + ids.join(',') + '"的数据项？', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        return delVehicle(ids.join(','));
      }).then(() => {
        this.getList();
        this.$message.success('删除成功');
      }).catch(() => {});
    },
    /** 解析人员数据（兼容字符串和数组） */
    parseCrew(crew) {
      if (!crew) return [];
      // 如果是数组，直接返回
      if (Array.isArray(crew)) {
        return crew;
      }
      // 如果是字符串，尝试解析
      if (typeof crew === 'string') {
        try {
          const parsed = JSON.parse(crew);
          return Array.isArray(parsed) ? parsed : [];
        } catch (e) {
          return [];
        }
      }
      return [];
    }
  }
};
</script>

<style scoped>
/* 参照发卡管理样式，可复用 */
.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-end;
  margin-bottom: 20px;
}
.search-form .el-form-item {
  margin-right: 20px;
  margin-bottom: 10px;
}
.table-container {
  width: 100%;
  overflow-x: auto;
}
.el-table {
  width: 100%;
  min-width: 900px;
}
</style>
