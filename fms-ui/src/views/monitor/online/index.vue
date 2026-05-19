<template>
  <div class="app-container tree-sidebar-manage-wrap">
    <div class="tree-sidebar-content">
      <div class="content-inner">
        <!-- 统计卡片区域 -->
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-info">
              <h4><i class="el-icon-cpu"></i> 总装置数</h4>
              <div class="stat-number">{{ formatNumber(statistics.total) }}</div>
            </div>
            <div class="stat-icon"><i class="el-icon-data-line"></i></div>
          </div>
          <div class="stat-card online">
            <div class="stat-info">
              <h4><i class="el-icon-success"></i> 在线运行</h4>
              <div class="stat-number">{{ formatNumber(statistics.online) }}</div>
            </div>
            <div class="stat-icon"><i class="el-icon-connection"></i></div>
          </div>
          <div class="stat-card lowbatt">
            <div class="stat-info">
              <h4><i class="el-icon-warning"></i> 低电量</h4>
              <div class="stat-number">{{ formatNumber(statistics.lowBattery) }}</div>
            </div>
            <div class="stat-icon"><i class="el-icon-cpu"></i></div>
          </div>
          <div class="stat-card offline">
            <div class="stat-info">
              <h4><i class="el-icon-turn-off"></i> 离线</h4>
              <div class="stat-number">{{ formatNumber(statistics.offline) }}</div>
            </div>
            <div class="stat-icon"><i class="el-icon-circle-close"></i></div>
          </div>
        </div>

        <!-- 搜索表单 -->
        <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
          <el-form-item label="装置编号" prop="deviceId">
            <el-input v-model="queryParams.deviceId" placeholder="请输入装置编号" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="装置名称" prop="name">
            <el-input v-model="queryParams.name" placeholder="请输入装置名称" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
          </el-form-item>
          <el-form-item label="状态" prop="status">
            <el-select v-model="queryParams.status" placeholder="装置状态" clearable style="width: 180px">
              <el-option label="在线" value="online" />
              <el-option label="低电预警" value="low_battery" />
              <el-option label="离线" value="offline" />
            </el-select>
          </el-form-item>
          <el-form-item label="类型" prop="type">
            <el-select v-model="queryParams.type" placeholder="装置类型" clearable style="width: 150px">
              <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
            <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
          </el-form-item>
        </el-form>

        <!-- 操作按钮栏 -->
        <el-row :gutter="10" class="mb8">
          <el-col :span="1.5">
            <el-button type="primary" plain icon="el-icon-plus" size="mini" @click="handleAdd">新增</el-button>
          </el-col>
<!--          <el-col :span="1.5">-->
<!--            <el-button type="success" plain icon="el-icon-edit" size="mini" :disabled="single" @click="handleUpdate">修改</el-button>-->
<!--          </el-col>-->
<!--          <el-col :span="1.5">-->
<!--            <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete">删除</el-button>-->
<!--          </el-col>-->
<!--          <el-col :span="1.5">-->
<!--            <el-button type="warning" plain icon="el-icon-download" size="mini" @click="handleExport">导出</el-button>-->
<!--          </el-col>-->
          <right-toolbar :showSearch.sync="showSearch" @queryTable="getList" :columns="columns"></right-toolbar>
        </el-row>

        <!-- 装置列表表格 -->
        <el-table v-loading="loading" :data="deviceList" @selection-change="handleSelectionChange">
<!--          <el-table-column type="selection" width="50" align="center" />-->
          <el-table-column label="装置编号" align="center" key="deviceId" prop="deviceId" v-if="columns.deviceId.visible" />
          <el-table-column label="装置名称" align="center" key="name" prop="name" v-if="columns.name.visible" />
          <el-table-column label="类型" align="center" key="type" prop="type" v-if="columns.type.visible">
            <template slot-scope="scope">
              {{ typeMap[scope.row.type] || scope.row.type }}
            </template>
          </el-table-column>
          <el-table-column label="最后通讯时间" align="center" key="lastComTime" prop="lastComTime" :formatter="formatDateTime" v-if="columns.lastComTime.visible" />
          <!-- 合并后的电量/指标列 -->
          <el-table-column label="电量/指标" align="center" key="combinedMetrics" v-if="columns.combinedMetrics.visible">
            <template slot-scope="scope">
              <div style="display: flex; flex-direction: column; align-items: center; gap: 6px;">
                <!-- 电量进度条 -->
                <el-progress
                  :percentage="scope.row.batteryLevel || 0"
                  :color="getBatteryColor(scope.row.batteryLevel)"
                  :stroke-width="10"
                  :show-text="false"
                  style="width: 100%;"
                />
                <!-- 下方文本：百分比 / 压力值 -->
                <div style="font-size: 12px; color: #606266;">
                  <span>{{ (scope.row.batteryLevel !== undefined && scope.row.batteryLevel !== null) ? scope.row.batteryLevel + '%' : '-' }}</span>
                  <span> / </span>
                  <span>{{ (scope.row.pressure !== undefined && scope.row.pressure !== null) ? scope.row.pressure + ' MPa' : '-' }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
<!--          <el-table-column label="电量/指标" align="center" key="batteryLevel" prop="batteryLevel" v-if="columns.batteryLevel.visible" width="100">-->
<!--            <template slot-scope="scope">-->
<!--              <el-progress :percentage="scope.row.batteryLevel" :color="getBatteryColor(scope.row.batteryLevel)" :stroke-width="10" :show-text="true" />-->
<!--            </template>-->
<!--          </el-table-column>-->
<!--          <el-table-column label="压力(MPa)" align="center" key="pressure" prop="pressure" v-if="columns.pressure.visible" width="100" />-->
          <el-table-column label="状态" align="center" key="status" v-if="columns.status.visible">
            <template slot-scope="scope">
              <div style="display: flex; align-items: center; justify-content: center; gap: 6px;">
                <span :class="['status-dot', getStatusDotClass(scope.row.status)]"></span>
                <span>{{ getStatusText(scope.row.status) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="160" class-name="small-padding fixed-width">
            <template slot-scope="scope">
              <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)">修改</el-button>
              <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)">删除</el-button>
              <el-dropdown size="mini" @command="(command) => handleCommand(command, scope.row)">
                <el-button size="mini" type="text" icon="el-icon-d-arrow-right">更多</el-button>
                <el-dropdown-menu slot="dropdown">
                  <el-dropdown-item command="handleViewDetail" icon="el-icon-view">监测详情</el-dropdown-item>
                  <el-dropdown-item command="handleResetBattery" icon="el-icon-refresh-right">重置电量</el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页组件 -->
        <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />
      </div>
    </div>

    <!-- 添加或修改装置对话框 -->
    <el-dialog :title="title" :visible.sync="open" width="600px" append-to-body>
      <el-form ref="form" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="装置编号" prop="deviceId">
          <el-input v-model="form.deviceId" placeholder="请输入装置编号" maxlength="50" />
        </el-form-item>
        <el-form-item label="装置名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入装置名称" maxlength="50" />
        </el-form-item>
        <el-form-item label="装置类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择装置类型">
            <el-option v-for="item in typeOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="电量(%)" prop="batteryLevel">
          <el-input-number v-model="form.batteryLevel" :min="0" :max="100" label="电量百分比" style="width: 200px" />
        </el-form-item>
        <el-form-item label="压力(MPa)" prop="pressure">
          <el-input-number v-model="form.pressure" :min="0" :max="50" :precision="1" step="0.5" style="width: 200px" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="online">在线</el-radio>
            <el-radio label="offline">离线</el-radio>
            <el-radio label="low_battery">低电预警</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="最后通讯时间" v-if="form.lastComTime">
          <span>{{ form.lastComTime }}</span>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitForm">确 定</el-button>
        <el-button @click="cancel">取 消</el-button>
      </div>
    </el-dialog>

    <!-- 监测详情抽屉（可选） -->
    <device-view-drawer ref="deviceViewRef" />
  </div>
</template>

<script>
// 引入 API（请根据您的后端实际路径修改）
import {
  listDevice,
  getDevice,
  addDevice,
  updateDevice,
  delDevice,
  changeDeviceStatus,
  exportDevice,
  getDeviceStatistics   // 新增统计接口
} from "@/api/monitor/device"
import TreePanel from "@/components/TreePanel"
import RightToolbar from "@/components/RightToolbar"
import Pagination from "@/components/Pagination"
import DeviceViewDrawer from "./view";
export default {
  name: "DeviceManagement",
  components: { TreePanel, RightToolbar, Pagination,DeviceViewDrawer },
  data() {
    return {
      // 加载状态
      loading: false,
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
      // 装置列表数据
      deviceList: [],
      // 统计信息
      statistics: {
        total: 0,
        online: 0,
        lowBattery: 0,
        offline: 0
      },
      // 弹出层标题
      title: "",
      // 左侧分类树数据
      categoryTree: [],
      // 类型下拉选项
      typeOptions: [
        { value: "手持式", label: "手持式" },
        { value: "单兵式", label: "单兵式" },
        { value: "车载式", label: "车载式" }
      ],
      typeMap: { "手持式": "手持式", "单兵式": "单兵式","车载式": "车载式" },
      // 是否显示弹出层
      open: false,
      // 表单参数
      form: {},
      // 查询参数
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        deviceId: undefined,
        name: undefined,
        status: undefined,
        type: undefined,
        categoryId: undefined
      },
      // 列显示控制
      columns: {
        deviceId: { label: "装置编号", visible: true },
        name: { label: "装置名称", visible: true },
        type: { label: "类型", visible: true },
        lastComTime: { label: "最后通讯时间", visible: true },
        combinedMetrics: { label: "电量/指标", visible: true },  // 新增合并列
        status: { label: "状态", visible: true }
      },
      // 表单校验规则
      rules: {
        name: [
          { required: true, message: "装置名称不能为空", trigger: "blur" },
          { min: 1, max: 50, message: "长度在 1 到 50 个字符", trigger: "blur" }
        ],
        type: [
          { required: true, message: "请选择装置类型", trigger: "change" }
        ],
        batteryLevel: [
          { required: true, message: "电量不能为空", trigger: "blur" },
          { type: "number", min: 0, max: 100, message: "电量必须在 0~100 之间", trigger: "blur" }
        ],
        pressure: [
          { required: true, message: "压力不能为空", trigger: "blur" },
          { type: "number", min: 0, message: "压力必须大于等于0", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.getList();
    this.getStatistics();    // 获取统计数据
  },
  methods: {
    /** 查询装置列表 */
    getList() {
      this.loading = true;
      listDevice(this.queryParams).then(response => {
        this.deviceList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    // 获取状态文字
    getStatusText(status) {
      switch (status) {
        case 'online': return '在线';
        case 'offline': return '离线';
        case 'low_battery': return '低电预警';
        default: return status;
      }
    },
// 获取状态圆点样式类
    getStatusDotClass(status) {
      switch (status) {
        case 'online': return 'dot-online';
        case 'offline': return 'dot-offline';
        case 'low_battery': return 'dot-lowbatt';
        default: return '';
      }
    },
    /** 获取统计数据（总装置数、在线等） */
    getStatistics() {
      getDeviceStatistics().then(response => {
        this.statistics = response.data;
      }).catch(() => {
        // 如果统计接口暂未提供，可以前端从全量列表计算（不推荐，但可作为降级）
        // 此处仅仅提示，实际可调用全量列表接口（分页为1，size=1e5）后计算
        console.warn("统计接口调用失败，请检查后端");
      });
    },
    formatNumber(value) {
      if (value === undefined || value === null) return '0';
      return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    },
    // 格式化日期时间：将 ISO 字符串转为 YYYY-MM-DD HH:mm:ss
    formatDateTime(row, column, cellValue) {
      if (!cellValue) return '-';
      try {
        // 兼容 "2025-06-06T12:00:00" 格式
        const date = new Date(cellValue);
        if (isNaN(date.getTime())) return cellValue;
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const day = String(date.getDate()).padStart(2, '0');
        const hours = String(date.getHours()).padStart(2, '0');
        const minutes = String(date.getMinutes()).padStart(2, '0');
        const seconds = String(date.getSeconds()).padStart(2, '0');
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
      } catch (e) {
        return cellValue;
      }
    },
    /** 搜索按钮 */
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    /** 重置按钮 */
    resetQuery() {
      this.resetForm("queryForm");
      this.queryParams.categoryId = undefined;
      if (this.$refs.categoryTreeRef) {
        this.$refs.categoryTreeRef.setCurrentKey(null);
      }
      this.handleQuery();
    },
    /** 多选框选中 */
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id);
      this.single = selection.length !== 1;
      this.multiple = !selection.length;
    },
    /** 状态修改 */
    handleStatusChange(row) {
      let text = row.status === "online" ? "启用" : "停用";
      this.$confirm('确认要"' + text + '"装置 "' + row.name + '" 吗？', "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        return changeDeviceStatus(row.id, row.status);
      }).then(() => {
        this.$message.success(text + "成功");
        this.getList();
        this.getStatistics();  // 刷新统计数据
      }).catch(() => {
        row.status = row.status === "online" ? "offline" : "online";
      });
    },
    /** 取消 */
    cancel() {
      this.open = false;
      this.reset();
    },
    /** 表单重置 */
    reset() {
      this.form = {
        id: undefined,
        name: undefined,
        type: undefined,
        batteryLevel: 100,
        pressure: 0,
        status: "online",
        lastComTime: undefined
      };
      this.resetForm("form");
    },
    /** 新增 */
    handleAdd() {
      this.reset();
      this.open = true;
      this.title = "添加装置";
    },
    /** 修改 */
    handleUpdate(row) {
      this.reset();
      const id = row.id || this.ids[0];
      getDevice(id).then(response => {
        this.form = response.data;
        this.open = true;
        this.title = "修改装置";
      });
    },
    /** 提交 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          if (this.form.id !== undefined) {
            updateDevice(this.form).then(() => {
              this.$message.success("修改成功");
              this.open = false;
              this.getList();
              this.getStatistics();
            });
          } else {
            addDevice(this.form).then(() => {
              this.$message.success("新增成功");
              this.open = false;
              this.getList();
              this.getStatistics();
            });
          }
        }
      });
    },
    /** 删除 */
    handleDelete(row) {
      const ids = row.id ? [row.id] : this.ids;
      this.$confirm('是否确认删除装置编号为"' + ids.join(",") + '"的数据项？', "警告", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning"
      }).then(() => {
        return delDevice(ids);
      }).then(() => {
        this.getList();
        this.getStatistics();
        this.$message.success("删除成功");
      }).catch(() => {});
    },
    /** 导出 */
    handleExport() {
      this.download("/device/export", this.queryParams, `device_${new Date().getTime()}.xlsx`);
    },
    /** 更多操作 */
    handleCommand(command, row) {
      switch (command) {
        case "handleViewDetail":
          this.handleViewDetail(row);
          break;
        case "handleResetBattery":
          this.handleResetBattery(row);
          break;
        default:
          break;
      }
    },
    /** 查看监测详情 */
    handleViewDetail(row) {
      if (this.$refs.deviceViewRef) {
        this.$refs.deviceViewRef.open(row.id);
      } else {
        this.$alert("监测详情功能开发中", "提示");
      }
    },
    /** 重置电量 */
    handleResetBattery(row) {
      this.$confirm("确认将装置「" + row.name + "」的电量重置为100%吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "info"
      }).then(() => {
        return updateDevice({ id: row.id, batteryLevel: 100 });
      }).then(() => {
        this.$message.success("电量已重置");
        this.getList();
        this.getStatistics();
      });
    },
    /** 电量进度条颜色 */
    getBatteryColor(level) {
      if (level >= 80) return "#67C23A";
      if (level >= 30) return "#E6A23C";
      return "#F56C6C";
    }
  }
};
</script>

<style scoped>
/* 统计卡片样式 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(210px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}
.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 18px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  border: 1px solid #ebeef5;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 18px 0 rgba(0, 0, 0, 0.08);
}
.stat-info h4 {
  font-size: 14px;
  font-weight: 400;
  color: #909399;
  margin-bottom: 8px;
}
.stat-number {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}
.stat-icon {
  font-size: 36px;
  color: #409eff;
}
.stat-card.online .stat-icon { color: #67c23a; }
.stat-card.lowbatt .stat-icon { color: #e6a23c; }
.stat-card.offline .stat-icon { color: #909399; }

.mb8 {
  margin-bottom: 8px;
}
/* 统计卡片样式 - 图片风格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  transition: all 0.3s;
  border: 1px solid #ebeef5;
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 18px 0 rgba(0, 0, 0, 0.1);
}
.stat-info h4 {
  font-size: 14px;
  font-weight: 400;
  color: #909399;
  margin-bottom: 12px;
}
.stat-number {
  font-size: 32px;
  font-weight: 600;
  color: #303133;
  letter-spacing: 1px;
}
.stat-icon {
  font-size: 42px;
  color: #409eff;
  opacity: 0.8;
}
/* 状态小圆点 */
.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 4px; /* 可在 flex gap 基础上微调 */
}
.dot-online {
  background-color: #67C23A; /* 在线绿色 */
  box-shadow: 0 0 0 2px rgba(103, 194, 58, 0.2);
}
.dot-offline {
  background-color: #909399; /* 离线灰色 */
}
.dot-lowbatt {
  background-color: #E6A23C; /* 低电量橙色 */
}
/* 不同卡片图标颜色 */
.stat-card.online .stat-icon { color: #67c23a; }
.stat-card.lowbatt .stat-icon { color: #e6a23c; }
.stat-card.offline .stat-icon { color: #909399; }

/* 统计卡片通用左边框基础 */
.stat-card {
  border-left: 4px solid #303133; /* 默认黑色左边框 */
}
.stat-card .stat-number {
  font-weight: 700; /* 加粗更明显 */
  color: #303133;   /* 默认黑色 */
}

/* 在线运行卡片：绿色边框 + 绿色数字 */
.stat-card.online {
  border-left-color: #67C23A;
}
.stat-card.online .stat-number {
  color: #67C23A;
}

/* 低电量卡片：橙色边框 + 橙色数字 */
.stat-card.lowbatt {
  border-left-color: #E6A23C;
}
.stat-card.lowbatt .stat-number {
  color: #E6A23C;
}

/* 离线卡片：红色边框 + 红色数字 */
.stat-card.offline {
  border-left-color: #F56C6C;
}
.stat-card.offline .stat-number {
  color: #F56C6C;
}
</style>
