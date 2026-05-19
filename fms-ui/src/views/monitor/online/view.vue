<template>
  <el-drawer
    :title="title"
    :visible.sync="visible"
    direction="rtl"
    size="40%"
    :before-close="handleClose"
  >
    <div class="device-detail-container">
      <el-form :model="form" label-width="100px" size="medium">
        <el-form-item label="装置ID">
          <el-input v-model="form.deviceId" readonly disabled />
        </el-form-item>
        <el-form-item label="装置名称">
          <el-input v-model="form.name" readonly disabled />
        </el-form-item>
        <el-form-item label="类型">
          <el-input v-model="form.type" readonly disabled />
        </el-form-item>
        <el-form-item label="最后通讯时间">
          <el-input v-model="form.lastComTime" readonly disabled />
        </el-form-item>
        <el-form-item label="电量(%)">
          <el-input v-model="form.batteryLevel" readonly disabled>
            <template slot="append">
              <el-progress
                :percentage="form.batteryLevel"
                :color="getBatteryColor(form.batteryLevel)"
                :stroke-width="8"
                style="width: 100px; margin-left: 10px;"
              />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="压力(MPa)">
          <el-input v-model="form.pressure" readonly disabled />
        </el-form-item>
        <el-form-item label="状态">
          <el-tag :type="getStatusType(form.status)">
            {{ getStatusText(form.status) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-input v-model="form.createTime" readonly disabled />
        </el-form-item>
        <el-form-item label="更新时间">
          <el-input v-model="form.updateTime" readonly disabled />
        </el-form-item>
      </el-form>
      <div class="drawer-footer">
        <el-button @click="handleClose">关 闭</el-button>
        <el-button type="primary" @click="handleEdit" v-if="allowEdit">编 辑</el-button>
      </div>
    </div>
  </el-drawer>
</template>

<script>
import { getDevice } from "@/api/monitor/device";

export default {
  name: "DeviceViewDrawer",
  props: {
    allowEdit: {
      type: Boolean,
      default: false  // 是否显示编辑按钮（预留扩展）
    }
  },
  data() {
    return {
      visible: false,
      title: "监测详情",
      form: {
        deviceId: "",
        name: "",
        type: "",
        lastComTime: "",
        batteryLevel: 0,
        pressure: null,
        status: "",
        createTime: "",
        updateTime: "",
        remark: ""
      }
    };
  },
  methods: {
    // 打开抽屉并加载数据
    open(id) {
      this.visible = true;
      this.loadDetail(id);
    },
    // 加载装置详情
    async loadDetail(id) {
      try {
        const res = await getDevice(id);
        if (res.code === 200) {
          const data = res.data;
          // 格式化日期
          this.form = {
            ...data,
            lastComTime: this.formatDateTime(data.lastComTime),
            createTime: this.formatDateTime(data.createTime),
            updateTime: this.formatDateTime(data.updateTime)
          };
        } else {
          this.$message.error(res.msg || "加载失败");
        }
      } catch (error) {
        console.error("加载装置详情失败", error);
        this.$message.error("加载失败");
      }
    },
    // 关闭抽屉
    handleClose() {
      this.visible = false;
      this.resetForm();
    },
    // 重置表单
    resetForm() {
      this.form = {
        deviceId: "",
        name: "",
        type: "",
        lastComTime: "",
        batteryLevel: 0,
        pressure: null,
        status: "",
        createTime: "",
        updateTime: "",
        remark: ""
      };
    },
    // 编辑（预留，可根据需要实现跳转或打开编辑弹窗）
    handleEdit() {
      this.$emit("edit", this.form);
      this.handleClose();
    },
    // 工具：格式化日期时间
    formatDateTime(value) {
      if (!value) return "-";
      try {
        const date = new Date(value);
        if (isNaN(date.getTime())) return value;
        const year = date.getFullYear();
        const month = String(date.getMonth() + 1).padStart(2, "0");
        const day = String(date.getDate()).padStart(2, "0");
        const hours = String(date.getHours()).padStart(2, "0");
        const minutes = String(date.getMinutes()).padStart(2, "0");
        const seconds = String(date.getSeconds()).padStart(2, "0");
        return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
      } catch (e) {
        return value;
      }
    },
    // 状态文本映射
    getStatusText(status) {
      const map = {
        online: "在线",
        low_battery: "低电量预警",
        offline: "离线"
      };
      return map[status] || status;
    },
    // 状态标签类型
    getStatusType(status) {
      const map = {
        online: "success",
        low_battery: "warning",
        offline: "info"
      };
      return map[status] || "info";
    },
    // 电量进度条颜色
    getBatteryColor(level) {
      if (level >= 80) return "#67C23A";
      if (level >= 30) return "#E6A23C";
      return "#F56C6C";
    }
  }
};
</script>

<style scoped>
.device-detail-container {
  padding: 20px;
}
.drawer-footer {
  margin-top: 30px;
  text-align: center;
}
</style>
