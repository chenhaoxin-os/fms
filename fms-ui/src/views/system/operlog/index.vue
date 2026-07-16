<template>
  <div class="identity-system">

    <!-- 主体内容：左右结构 -->
    <div class="dashboard-main">
      <!-- 左侧: 随车人员实时展示区块 (调窄宽度) -->
      <div class="left-vehicles">
        <div class="card-panel" style="padding: 20px; margin-bottom: 12px;">
          <div style="font-weight: 600; font-size: 16px; margin-bottom: 12px; display: flex; justify-content: space-between; align-items: center;">
            <span>🚨 随车人员实时展示</span>
            <span style="font-size: 12px; font-weight: normal; color: #67c23a;">
              <span v-if="wsConnected" style="display: inline-flex; align-items: center; gap: 4px;">
                <span style="display: inline-block; width: 8px; height: 8px; border-radius: 50%; background: #67c23a; animation: pulse 1.5s ease-in-out infinite;"></span>
                实时
              </span>
              <span v-else style="color: #f56c6c;">● 离线</span>
            </span>
          </div>
        </div>
        <div class="vehicle-list">
          <div v-for="vehicle in vehicleList" :key="vehicle.id" class="vehicle-card">
            <div class="vehicle-header">
              <div class="vehicle-title">
                <span class="plate">{{ vehicle.plate }}</span>
                <span>{{ vehicle.name }}</span>
              </div>
              <div style="display: flex; gap: 12px; align-items: center;">
                <span class="capacity">载员：{{ vehicle.currentCrew }}/{{ vehicle.maxCapacity }}</span>
                <span class="vehicle-status" :class="vehicle.status === '进行中' ? 'status-active' : 'status-standby'">
                  {{ vehicle.status === '进行中' ? '🚀 进行中' : '⏸ 待命' }}
                </span>
              </div>
            </div>
            <!-- 人员展示区域 -->
            <div class="member-area">
              <div class="member-label">
                <i class="el-icon-user-solid"></i> 随车人员
              </div>
              <div class="member-tags">
                <template v-if="vehicle.status === '进行中' && vehicle.members && vehicle.members.length">
                  <span v-for="(member, idx) in vehicle.members" :key="idx" class="member-tag">{{ member.name }}</span>
                </template>
                <template v-else-if="vehicle.status === '进行中' && (!vehicle.members || vehicle.members.length === 0)">
                  <div class="empty-members">⚡ 探测中，尚未捕获稳定人员数据</div>
                </template>
                <template v-else>
                  <div class="empty-members">🔌 车辆未启动或设备未送电，无人员实时数据</div>
                </template>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧: 事件查询表格 -->
      <div class="right-events">
        <div class="card-panel">
          <!-- 筛选栏 -->
          <div class="filter-bar">
            <el-form :model="queryParams" ref="queryForm" size="small" inline label-width="70px">
              <el-form-item label="日期范围" prop="dateRange">
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  value-format="yyyy-MM-dd HH:mm:ss"
                  range-separator="-"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  :default-time="['00:00:00', '23:59:59']"
                  style="width: 260px">
                </el-date-picker>
              </el-form-item>
              <el-form-item label="事件类型" prop="eventType">
                <el-select v-model="queryParams.eventType" placeholder="选择事件类型" clearable style="width: 160px">
                  <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="small" icon="el-icon-search" @click="handleQuery">查询</el-button>
                <el-button size="small" icon="el-icon-refresh-right" @click="resetQuery">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
          <!-- 操作按钮栏 -->
          <div class="action-bar">
            <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAdd">新增</el-button>
            <el-button size="small" icon="el-icon-refresh" @click="manualRefresh" :loading="refreshing">刷新车辆</el-button>
          </div>

          <!-- 表格 -->
          <el-table
            v-loading="loading"
            :data="eventList"
            stripe
            border
            style="width: 100%; margin-top: 16px;"
            :header-cell-style="{ background: '#fafbfd', color: '#475569' }"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="index" width="55" align="center" label="序号" />
            <el-table-column label="事件时间" prop="eventTime" width="160" />
            <el-table-column label="事件ID" prop="eventId" width="130" />
            <el-table-column label="事件名称" prop="eventName" min-width="180" show-overflow-tooltip />
            <el-table-column label="事件类型" prop="eventType" width="110">
              <template slot-scope="scope">
                <el-tag :type="getEventTagType(scope.row.eventType)" size="small">{{ scope.row.eventType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="调度力量" prop="dispatchForce" min-width="140" show-overflow-tooltip />
            <el-table-column label="操作人" prop="operator" width="120" />
            <el-table-column label="操作" width="160" fixed="right">
              <template slot-scope="scope">
                <el-button type="text" size="small" icon="el-icon-view" @click="showJsonDetail(scope.row)">详情</el-button>
                <el-button type="text" size="small" icon="el-icon-delete" style="color: #f56c6c;" @click="handleDelete(scope.row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
            <el-pagination
              background
              @current-change="handlePageChange"
              @size-change="handleSizeChange"
              :current-page="queryParams.pageNum"
              :page-size="queryParams.pageSize"
              :total="totalEvents"
              layout="total, sizes, prev, pager, next, jumper"
              :page-sizes="[5, 10, 20, 50]">
            </el-pagination>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialogTitle" :visible.sync="dialogVisible" width="600px" @close="cancel">
      <el-form ref="eventForm" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="事件ID" prop="eventId">
          <el-input v-model="formData.eventId" placeholder="事件ID（不填则自动生成）" />
        </el-form-item>
        <el-form-item label="事件名称" prop="eventName">
          <el-input v-model="formData.eventName" placeholder="请输入事件名称" />
        </el-form-item>
        <el-form-item label="事件类型" prop="eventType">
          <el-select v-model="formData.eventType" placeholder="请选择事件类型" style="width: 100%">
            <el-option label="入场登记" value="入场登记" />
            <el-option label="分队分组" value="分队分组" />
            <el-option label="车辆日志" value="车辆日志" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件时间" prop="eventTime">
          <el-date-picker
            v-model="formData.eventTime"
            type="datetime"
            placeholder="选择时间"
            value-format="yyyy-MM-dd HH:mm:ss"
            style="width: 100%">
          </el-date-picker>
        </el-form-item>
        <el-form-item label="调度力量" prop="dispatchForce">
          <el-input v-model="formData.dispatchForce" placeholder="请输入调度力量" />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="formData.operator" placeholder="请输入操作人" />
        </el-form-item>
        <el-form-item label="车辆ID" prop="vehicleId">
          <el-input-number v-model="formData.vehicleId" :min="1" style="width: 100%" placeholder="请输入车辆ID" />
        </el-form-item>
        <el-form-item label="JSON数据" prop="jsonData">
          <el-input
            v-model="jsonDataStr"
            type="textarea"
            :rows="4"
            placeholder='请输入JSON格式数据，如：{"key": "value"}'
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="cancel">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </span>
    </el-dialog>

    <!-- 抽屉 JSON 详情 -->
    <el-drawer title="事件完整 JSON 数据" :visible.sync="drawerVisible" size="40%" direction="rtl">
      <div style="padding: 20px; height: 100%; overflow-y: auto">
        <pre class="json-pre">{{ currentJsonStr || '暂无数据' }}</pre>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import {
  getEventList,
  getEventStatistics,
  getEventDetail,
  addEvent,
  updateEvent,
  delEvent,
  exportEvent
} from '@/api/system/event'

import { getVehicleList } from '@/api/system/vehicle'
import VehicleWebSocket from '@/utils/websocket'

export default {
  name: 'IdentityManagement',
  data() {
    return {
      activeNav: 'event',
      // 车辆数据列表
      vehicleList: [],
      // 统计信息
      statistics: null,
      // 右侧事件相关
      loading: false,
      eventList: [],
      totalEvents: 0,
      selectedEventIds: [],
      dateRange: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        eventType: undefined,
        eventName: undefined,
        status: undefined,
        beginTime: undefined,
        endTime: undefined
      },
      categoryOptions: [
        { value: '入场登记', label: '入场登记' },
        { value: '分队分组', label: '分队分组' },
        { value: '车辆日志', label: '车辆日志' }
      ],
      // 对话框相关
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      formData: {
        id: undefined,
        eventId: '',
        eventName: '',
        eventType: '',
        eventTime: '',
        dispatchForce: '',
        status: '处置中',
        operator: '',
        vehicleId: undefined,
        jsonData: null,
        remark: ''
      },
      jsonDataStr: '',
      formRules: {
        eventName: [{ required: true, message: '请输入事件名称', trigger: 'blur' }],
        eventType: [{ required: true, message: '请选择事件类型', trigger: 'change' }],
        eventTime: [{ required: true, message: '请选择事件时间', trigger: 'change' }]
      },
      // 抽屉
      drawerVisible: false,
      currentJsonStr: '',
      // WebSocket
      wsInstance: null,
      wsConnected: false,
      refreshing: false
    };
  },
  created() {
    this.fetchVehicleList();
    this.getStatistics();
    this.getEventList();
    this.initWebSocket();
  },
  beforeDestroy() {
    if (this.wsInstance) {
      this.wsInstance.close();
      this.wsInstance = null;
    }
  },
  methods: {
    // ========== WebSocket 初始化 ==========
    initWebSocket() {
      this.wsInstance = new VehicleWebSocket({
        onMessage: (data) => {
          if (data.type === 'vehicleUpdate') {
            console.log('收到 WebSocket 车辆更新数据，时间戳:', data.timestamp);
            this.processVehicleData(data.data);
          }
        },
        onOpen: () => {
          this.wsConnected = true;
          console.log('WebSocket 已连接，等待实时数据推送');
        },
        onClose: () => {
          this.wsConnected = false;
          console.log('WebSocket 已断开');
        },
        onError: (error) => {
          this.wsConnected = false;
          console.error('WebSocket 错误：', error);
        }
      });
    },

    // ========== 处理车辆数据（共用逻辑） ==========
    processVehicleData(rawData) {
      const data = rawData || [];
      // 过滤：只保留 deviceId 不为空的车辆
      const filteredData = data.filter(vehicle =>
        vehicle.deviceId != null && vehicle.deviceId !== ''
      );

      this.vehicleList = filteredData.map(vehicle => {
        const isActive = vehicle.isRunning && vehicle.devicePower;
        const status = isActive ? '进行中' : '待命';

        let membersToShow = [];
        if (isActive && vehicle.stableCrew) {
          try {
            console.log('vehicle.stableCrew', vehicle.stableCrew);
            const crewData = typeof vehicle.stableCrew === 'string'
              ? JSON.parse(vehicle.stableCrew)
              : vehicle.stableCrew;
            if (Array.isArray(crewData)) {
              membersToShow = crewData;
            }
          } catch (e) {
            membersToShow = [];
          }
        }
        const currentCrew = membersToShow.length;

        return {
          id: vehicle.id,
          plate: vehicle.plate,
          name: vehicle.name,
          maxCapacity: vehicle.maxCapacity,
          status: status,
          members: membersToShow,
          currentCrew: currentCrew
        };
      });
    },

    // ========== 从 API 获取车辆列表（初始加载 + 降级方案） ==========
    async fetchVehicleList() {
      try {
        const res = await getVehicleList({ pageNum: 1, pageSize: 1000 });
        if (res.code === 200) {
          this.processVehicleData(res.rows || []);
        }
      } catch (error) {
        console.error('获取车辆数据失败:', error);
        this.vehicleList = [];
      }
    },

    // ========== 手动刷新 ==========
    manualRefresh() {
      this.refreshing = true;
      // 优先尝试 WebSocket 请求
      if (this.wsInstance && this.wsConnected) {
        this.wsInstance.send('REFRESH');
        this.$message.success('已发送刷新请求');
        setTimeout(() => {
          this.refreshing = false;
        }, 1000);
      } else {
        // 降级方案：HTTP 请求
        this.fetchVehicleList().then(() => {
          this.refreshing = false;
          this.$message.success('刷新成功');
        }).catch(() => {
          this.refreshing = false;
          this.$message.error('刷新失败');
        });
      }
    },

    // ========== 获取事件统计 ==========
    async getStatistics() {
      try {
        const res = await getEventStatistics();
        if (res.code === 200) {
          this.statistics = res.data;
        }
      } catch (error) {
        console.error('获取统计信息失败:', error);
      }
    },

    // ========== 获取事件列表 ==========
    async getEventList() {
      this.loading = true;
      try {
        const params = { ...this.queryParams };

        if (this.dateRange && this.dateRange.length === 2) {
          params.beginTime = this.dateRange[0];
          params.endTime = this.dateRange[1];
        } else {
          delete params.beginTime;
          delete params.endTime;
        }

        Object.keys(params).forEach(key => {
          if (params[key] === undefined || params[key] === '' || params[key] === null) {
            delete params[key];
          }
        });

        const res = await getEventList(params);
        if (res.code === 200) {
          if (res.rows) {
            this.eventList = res.rows;
            this.totalEvents = res.total;
          } else if (res.data && res.data.list) {
            this.eventList = res.data.list;
            this.totalEvents = res.data.total;
          } else if (Array.isArray(res.data)) {
            this.eventList = res.data;
            this.totalEvents = res.data.length;
          } else {
            this.eventList = [];
            this.totalEvents = 0;
          }
        } else {
          this.$message.error(res.msg || '查询失败');
        }
      } catch (error) {
        console.error('获取事件列表失败:', error);
        this.$message.error('获取事件列表失败');
      } finally {
        this.loading = false;
      }
    },

    // ========== 表格多选 ==========
    handleSelectionChange(selection) {
      this.selectedEventIds = selection.map(item => item.id);
    },

    // ========== 新增 ==========
    handleAdd() {
      this.dialogTitle = '新增事件';
      this.isEdit = false;
      this.formData = {
        id: undefined,
        eventId: '',
        eventName: '',
        eventType: '',
        eventTime: new Date().toISOString().slice(0, 19).replace('T', ' '),
        dispatchForce: '',
        status: '处置中',
        operator: this.$store.state.user?.name || 'admin',
        vehicleId: undefined,
        jsonData: null,
        remark: ''
      };
      this.jsonDataStr = '';
      this.dialogVisible = true;
      this.$nextTick(() => {
        this.$refs.eventForm && this.$refs.eventForm.clearValidate();
      });
    },

    // ========== 编辑 ==========
    async handleEdit(row) {
      const eventId = row?.id || (this.selectedEventIds.length === 1 ? this.selectedEventIds[0] : null);
      if (!eventId) {
        if (!row) this.$message.warning('请选择一条数据');
        return;
      }

      try {
        const res = await getEventDetail(eventId);
        if (res.code === 200) {
          const data = res.data;
          this.formData = {
            id: data.id,
            eventId: data.eventId || '',
            eventName: data.eventName || '',
            eventType: data.eventType || '',
            eventTime: data.eventTime || '',
            dispatchForce: data.dispatchForce || '',
            status: data.status || '处置中',
            operator: data.operator || '',
            vehicleId: data.vehicleId,
            jsonData: data.jsonData,
            remark: data.remark || ''
          };
          if (data.jsonData) {
            this.jsonDataStr = typeof data.jsonData === 'string'
              ? data.jsonData
              : JSON.stringify(data.jsonData, null, 2);
          } else {
            this.jsonDataStr = '';
          }
          this.dialogTitle = '编辑事件';
          this.isEdit = true;
          this.dialogVisible = true;
          this.$nextTick(() => {
            this.$refs.eventForm && this.$refs.eventForm.clearValidate();
          });
        } else {
          this.$message.error(res.msg || '获取事件详情失败');
        }
      } catch (error) {
        console.error('获取事件详情失败:', error);
        this.$message.error('获取事件详情失败');
      }
    },

    // ========== 删除 ==========
    async handleDelete(row) {
      let ids = [];
      if (row) {
        ids = [row.id];
      } else if (this.selectedEventIds.length) {
        ids = this.selectedEventIds;
      } else {
        this.$message.warning('请选择要删除的数据');
        return;
      }

      const confirmMsg = `确定要删除选中的 ${ids.length} 条事件吗？`;
      this.$confirm(confirmMsg, '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const res = await delEvent(ids.join(','));
          if (res.code === 200) {
            this.$message.success('删除成功');
            this.getEventList();
            this.getStatistics();
          } else {
            this.$message.error(res.msg || '删除失败');
          }
        } catch (error) {
          console.error('删除失败:', error);
          this.$message.error('删除失败');
        }
      }).catch(() => {});
    },

    // ========== 导出 ==========
    async handleExport() {
      try {
        const params = { ...this.queryParams };
        if (this.dateRange && this.dateRange.length === 2) {
          params.beginTime = this.dateRange[0];
          params.endTime = this.dateRange[1];
        }
        Object.keys(params).forEach(key => {
          if (params[key] === undefined || params[key] === '' || params[key] === null) {
            delete params[key];
          }
        });

        const res = await exportEvent(params);
        const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
        const link = document.createElement('a');
        const url = window.URL.createObjectURL(blob);
        link.href = url;
        link.download = `事件数据_${new Date().toISOString().slice(0, 19).replace(/:/g, '-')}.xlsx`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        window.URL.revokeObjectURL(url);
        this.$message.success('导出成功');
      } catch (error) {
        console.error('导出失败:', error);
        this.$message.error('导出失败');
      }
    },

    // ========== 提交表单 ==========
    submitForm() {
      this.$refs.eventForm.validate(async (valid) => {
        if (valid) {
          let jsonData = null;
          if (this.jsonDataStr && this.jsonDataStr.trim()) {
            try {
              jsonData = JSON.parse(this.jsonDataStr);
            } catch (e) {
              this.$message.error('JSON 格式不正确');
              return;
            }
          }

          const submitData = {
            ...this.formData,
            jsonData: jsonData
          };

          try {
            let res;
            if (this.isEdit) {
              res = await updateEvent(submitData);
            } else {
              res = await addEvent(submitData);
            }

            if (res.code === 200) {
              this.$message.success(this.isEdit ? '修改成功' : '新增成功');
              this.dialogVisible = false;
              this.getEventList();
              this.getStatistics();
            } else {
              this.$message.error(res.msg || (this.isEdit ? '修改失败' : '新增失败'));
            }
          } catch (error) {
            console.error('提交失败:', error);
            this.$message.error(error.response?.data?.msg || '提交失败');
          }
        }
      });
    },

    // ========== 取消对话框 ==========
    cancel() {
      this.dialogVisible = false;
      this.formData = {};
      this.jsonDataStr = '';
    },

    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getEventList();
    },

    resetQuery() {
      this.dateRange = [];
      this.queryParams = {
        pageNum: 1,
        pageSize: 10,
        eventType: undefined,
        eventName: undefined,
        status: undefined,
        beginTime: undefined,
        endTime: undefined
      };
      this.getEventList();
    },

    handlePageChange(page) {
      this.queryParams.pageNum = page;
      this.getEventList();
    },

    handleSizeChange(size) {
      this.queryParams.pageSize = size;
      this.queryParams.pageNum = 1;
      this.getEventList();
    },

    async showJsonDetail(row) {
      try {
        const res = await getEventDetail(row.id);
        if (res.code === 200) {
          const event = res.data;
          const displayData = {
            id: event.id,
            eventId: event.eventId,
            eventName: event.eventName,
            eventType: event.eventType,
            eventTime: event.eventTime,
            dispatchForce: event.dispatchForce,
            status: event.status,
            operator: event.operator,
            vehicleId: event.vehicleId,
            remark: event.remark,
            createBy: event.createBy,
            createTime: event.createTime,
            updateBy: event.updateBy,
            updateTime: event.updateTime
          };
          this.currentJsonStr = JSON.stringify(displayData, null, 2);
        } else {
          this.currentJsonStr = JSON.stringify(row, null, 2);
        }
      } catch (error) {
        console.error('获取事件详情失败:', error);
        this.currentJsonStr = JSON.stringify(row, null, 2);
      }
      this.drawerVisible = true;
    },

    getEventTagType(type) {
      const map = {
        '入场登记': 'primary',
        '分队分组': 'success',
        '车辆日志': 'warning'
      };
      return map[type] || 'info';
    }
  },
  watch: {
    dateRange() {
      this.queryParams.pageNum = 1;
      this.getEventList();
    }
  }
};
</script>

<style scoped>
/* ========== 原有样式 ========== */
.identity-system {
  max-width: 1600px;
  margin: 0 auto;
  padding: 20px;
}

.card-panel {
  background: #ffffff;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.dashboard-main {
  display: flex;
  gap: 24px;
  flex-wrap: wrap;
}

.left-vehicles {
  flex: 0 0 320px;
  min-width: 280px;
  max-width: 320px;
}

.right-events {
  flex: 1;
  min-width: 580px;
}

.vehicle-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.vehicle-card {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid #edf0f5;
}

.vehicle-header {
  padding: 12px 16px;
  background: #fafcff;
  border-bottom: 1px solid #eef2f7;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
}

.vehicle-title {
  font-weight: 700;
  font-size: 14px;
  color: #1f2d3d;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.plate {
  background: #eef2fa;
  padding: 2px 8px;
  border-radius: 16px;
  font-size: 11px;
  font-weight: 500;
  font-family: monospace;
  color: #1e6f3f;
}

.vehicle-status {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 20px;
  background: #f0f9f0;
  color: #2c7a4a;
  white-space: nowrap;
}

.status-active {
  background: #fee9e6;
  color: #d9534f;
}

.status-standby {
  background: #f0f2f5;
  color: #7f8c8d;
}

.capacity {
  font-size: 11px;
  color: #5f7d9c;
  white-space: nowrap;
}

.member-area {
  padding: 10px 12px;
  background: #ffffff;
  border-bottom: 1px solid #f0f3f8;
}

.member-label {
  font-size: 11px;
  font-weight: 500;
  color: #5e7a93;
  margin-bottom: 6px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.member-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.member-tag {
  background: #eef5ff;
  padding: 2px 8px;
  border-radius: 20px;
  font-size: 11px;
  color: #1e466e;
  font-weight: 500;
  white-space: nowrap;
}

.empty-members {
  color: #bcc9d6;
  font-size: 11px;
  padding: 4px 0;
}

.right-events .card-panel {
  padding: 0 20px 20px 20px;
}

.action-bar {
  padding: 16px 0 8px 0;
  border-bottom: 1px solid #ecf0f5;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.filter-bar {
  padding: 16px 0 8px 0;
  border-bottom: 1px solid #ecf0f5;
}

.stats-row {
  padding: 16px 0 8px 0;
  border-bottom: 1px solid #ecf0f5;
}

.stat-card {
  background: #f8fafc;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  transition: all 0.3s;
}

.stat-card:hover {
  background: #f0f4f9;
  transform: translateY(-2px);
}

.stat-num {
  font-size: 24px;
  font-weight: 700;
  color: #1e6f3f;
}

.stat-num.processing {
  color: #e6a23c;
}

.stat-num.finished {
  color: #67c23a;
}

.stat-label {
  font-size: 12px;
  color: #8c9aaf;
  margin-top: 6px;
}

.json-pre {
  background: #f8fafc;
  border-radius: 12px;
  padding: 16px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
}

/* ========== WebSocket 状态动画 ========== */
@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.8);
  }
}

@media (max-width: 1000px) {
  .left-vehicles {
    flex: 0 0 280px;
    max-width: 280px;
  }
  .right-events {
    min-width: 480px;
  }
}
</style>
