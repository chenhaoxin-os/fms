<template>
  <div class="app-container">
    <!-- 查询表单 -->
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px" class="search-form">
      <el-form-item label="姓名" prop="holderName">
        <el-input v-model="queryParams.holderName" placeholder="请输入姓名" clearable style="width: 180px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="卡状态" prop="cardStatus">
        <el-select v-model="queryParams.cardStatus" placeholder="卡状态" clearable style="width: 140px">
          <el-option label="待发卡" value="0" />
          <el-option label="已发卡" value="1" />
          <el-option label="已注销" value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="卡类型" prop="cardType">
        <el-select v-model="queryParams.cardType" placeholder="卡类型" clearable style="width: 140px">
          <el-option label="主卡" value="主卡" />
          <el-option label="副卡1" value="副卡1" />
          <el-option label="副卡2" value="副卡2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" icon="el-icon-plus" size="mini" @click="handleAddCard">新增</el-button>
      </el-col>
      <el-col :span="1.5">
        <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
      </el-col>
    </el-row>

    <!-- 表格 -->
    <div class="table-container">
      <el-table
        v-loading="loading"
        :data="cardList"
        @selection-change="handleSelectionChange"
        border
        stripe
        style="width: 100%">
        <el-table-column type="selection" width="55" align="center" fixed="left" />
        <el-table-column label="姓名" align="center" prop="holderName" min-width="120" show-overflow-tooltip />
        <el-table-column label="人员编号" align="center" prop="userNumber" min-width="150" show-overflow-tooltip />
        <el-table-column label="卡类别" align="center" prop="cardType" min-width="100" />
        <el-table-column label="标识牌十进制编码" align="center" prop="decimalCode" min-width="180" show-overflow-tooltip />
        <el-table-column label="EPC编码" align="center" prop="epcCode" min-width="200" show-overflow-tooltip />
        <el-table-column label="发卡时间" align="center" prop="issueTime" min-width="160" />
        <el-table-column label="卡状态" align="center" prop="cardStatus" min-width="100">
          <template slot-scope="scope">
            <el-tag v-if="scope.row.cardStatus === '0'" type="warning">待发卡</el-tag>
            <el-tag v-else-if="scope.row.cardStatus === '1'" type="success">已发卡</el-tag>
            <el-tag v-else-if="scope.row.cardStatus === '2'" type="info">已注销</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" align="center" min-width="180" fixed="right">
          <template slot-scope="scope">
            <el-button
              v-if="scope.row.cardStatus === '0' || scope.row.cardStatus === '2'"
              size="mini"
              type="text"
              icon="el-icon-document"
              @click="handleIssue(scope.row)"
            >编辑</el-button>

            <el-button
              v-if="scope.row.cardStatus === '1'"
              size="mini"
              type="text"
              icon="el-icon-delete"
              @click="handleCancel(scope.row)"
            >注销</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 统一的发卡/新增弹窗 -->
    <el-dialog
      :title="isAddMode ? '新增卡片并发卡' : '发卡操作'"
      :visible.sync="issueDialogVisible"
      width="580px"
      append-to-body
      :close-on-click-modal="false"
      @close="handleDialogClose">
      <el-form ref="issueForm" :model="issueForm" :rules="issueRules" label-width="110px">
        <!-- 新增模式：人员下拉选择（远程搜索） -->
        <el-form-item v-if="isAddMode" label="人员" prop="personCode">
          <el-select
            v-model="issueForm.personCode"
            filterable
            remote
            reserve-keyword
            placeholder="请输入姓名或编号搜索"
            :remote-method="remoteSearchPerson"
            :loading="personLoading"
            style="width: 100%"
            @change="handlePersonSelect">
            <el-option
              v-for="item in personOptions"
              :key="item.userName"
              :label="item.nickName + '（' + item.userName + '）'"
              :value="item.userName">
            </el-option>
          </el-select>
        </el-form-item>
        <!-- 发卡模式：只读显示 -->
        <el-form-item v-else label="人员编号">
          <span>{{ issueForm.userNumber }}</span>
        </el-form-item>


        <el-form-item label="姓名">
          <el-input v-model="issueForm.holderName" disabled placeholder="选择人员后自动填充" />
        </el-form-item>

        <el-form-item label="卡类别" prop="cardType" required>
          <el-select v-model="issueForm.cardType" placeholder="请选择卡类别" clearable style="width: 100%" @change="autoGenerateCode">
            <el-option label="主卡" value="主卡" />
            <el-option label="副卡1" value="副卡1" />
            <el-option label="副卡2" value="副卡2" />
          </el-select>
        </el-form-item>

        <el-form-item label="十进制编码">
          <el-input
            v-model="issueForm.decimalCode"
            placeholder="点击【读取卡片】从读卡器获取"
            readonly
            :class="{ 'has-value': issueForm.decimalCode }"
          />
        </el-form-item>
        <el-form-item label="EPC编码">
          <el-input
            v-model="issueForm.epcCode"
            placeholder="读取后自动填充"
            readonly
            :class="{ 'has-value': issueForm.epcCode }"
          />
        </el-form-item>
      </el-form>

      <!-- 读卡状态提示 -->
      <div v-if="readingCard" class="reading-status">
        <i class="el-icon-loading"></i> 正在读取卡片，请将卡片靠近读卡器...
        <el-button type="text" @click="stopReading" style="margin-left: 10px;">取消</el-button>
      </div>
      <div v-if="readSuccess && !readingCard" class="read-success">
        <i class="el-icon-success"></i> 卡片读取成功！EPC: {{ issueForm.epcCode.substring(0, 16) }}...
      </div>

      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="readCardFromReader" :loading="readingCard" :disabled="readingCard">
          <i class="el-icon-reading"></i> 读取卡片/用于测试
        </el-button>
        <el-button type="success" @click="submitWriteCard" :disabled="!issueForm.epcCode || !issueForm.cardType || !issueForm.personCode">
          写卡
        </el-button>
        <el-button @click="issueDialogVisible = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCard, issueCard, cancelCard, generateCardCode, addCard } from "@/api/system/cardIssue";
import { readCard, stopRead, getReaderStatus, writeEpc } from "@/api/rfid";
import { listUser } from "@/api/system/user";
import RightToolbar from "@/components/RightToolbar";

export default {
  name: "CardIssue",
  components: { RightToolbar },
  data() {
    return {
      isGenerating: false,
      loading: false,
      showSearch: true,
      cardList: [],
      total: 0,
      readingCard: false,
      readSuccess: false,
      readerConnected: false,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        holderName: undefined,
        cardStatus: undefined,
        cardType: undefined,
      },
      // 统一弹窗数据
      issueDialogVisible: false,
      isAddMode: false,
      issueForm: {
        id: null,
        personCode: '',
        holderName: '',
        cardType: '',
        decimalCode: '',
        epcCode: '',
      },
      issueRules: {
        personCode: [
          { required: true, message: '请选择人员', trigger: 'change' }
        ],
        cardType: [
          { required: true, message: '请选择卡类别', trigger: 'change' }
        ]
      },
      // 人员下拉相关
      personOptions: [],
      personLoading: false
    };
  },
  created() {
    this.getList();
    this.checkReaderStatus();
  },
  beforeDestroy() {
    this.stopReading();
  },
  methods: {
    // ========== 卡片列表 ==========
    getList() {
      this.loading = true;
      listCard(this.queryParams).then(response => {
        this.cardList = response.rows;
        this.total = response.total;
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    handleQuery() {
      this.queryParams.pageNum = 1;
      this.getList();
    },
    resetQuery() {
      this.resetForm("queryForm");
      this.handleQuery();
    },
    handleSelectionChange(selection) {},

    // ========== 新增 ==========
    handleAddCard() {
      this.isAddMode = true;
      this.issueForm = {
        id: null,
        personCode: '',
        holderName: '',
        cardType: '',
        decimalCode: '',
        epcCode: '',
      };
      this.readSuccess = false;
      this.issueDialogVisible = true;
      // 初始加载人员列表（第一页，20条）
      this.personLoading = true;
      listUser({ pageNum: 1, pageSize: 20 }).then(res => {
        this.personOptions = (res.rows || []).filter(item => item.userName !== 'admin');
        this.personLoading = false;
      }).catch(() => {
        this.personOptions = [];
        this.personLoading = false;
      });
      this.$nextTick(() => {
        this.$refs.issueForm && this.$refs.issueForm.clearValidate();
      });
    },

    // ========== 发卡（对已有记录） ==========
    handleIssue(row) {
      this.isAddMode = false;
      this.issueForm = {
        id: row.id,
        userNumber: row.userNumber,
        personCode: row.personCode,
        holderName: row.holderName,
        cardType: row.cardType || '',
        decimalCode: '',
        epcCode: '',
      };
      this.readSuccess = false;
      this.issueDialogVisible = true;
      if (this.issueForm.cardType) {
        this.autoGenerateCode();
      }
      this.$nextTick(() => {
        this.$refs.issueForm && this.$refs.issueForm.clearValidate();
      });
    },

    // ========== 人员远程搜索 ==========
    remoteSearchPerson(query) {
      if (!query || query.trim() === '') {
        // 清空搜索时恢复初始数据
        this.personLoading = true;
        listUser({ pageNum: 1, pageSize: 20 }).then(res => {
          this.personOptions = (res.rows || []).filter(item => item.userName !== 'admin');
          this.personLoading = false;
        }).catch(() => {
          this.personOptions = [];
          this.personLoading = false;
        });
        return;
      }
      this.personLoading = true;
      listUser({
        pageNum: 1,
        pageSize: 20,
        nickName: query.trim()
      }).then(res => {
        this.personOptions = (res.rows || []).filter(item => item.userName !== 'admin');
        this.personLoading = false;
      }).catch(() => {
        this.personOptions = [];
        this.personLoading = false;
      });
    },

    // 人员选择后，填充姓名
    handlePersonSelect(val) {
      if (!val) {
        this.issueForm.holderName = '';
        return;
      }
      const selected = this.personOptions.find(item => item.userName === val);
      if (selected) {
        this.issueForm.holderName = selected.nickName || '';
      } else {
        // 如果选中值不在当前列表中（如直接输入编号后选中），重新查询
        listUser({ userName: val, pageNum: 1, pageSize: 1 }).then(res => {
          if (res.rows && res.rows.length > 0) {
            const user = res.rows[0];
            this.issueForm.holderName = user.nickName || '';
            if (!this.personOptions.find(item => item.userName === val)) {
              this.personOptions.push(user);
            }
          } else {
            this.issueForm.holderName = '';
            this.$message.warning('未找到该人员');
          }
        }).catch(() => {
          this.issueForm.holderName = '';
        });
      }
    },

    // ========== 编码生成 ==========
    autoGenerateCode() {
      if (!this.issueForm.personCode || !this.issueForm.cardType) return;
      if (this.isGenerating) return;
      this.isGenerating = true;
      generateCardCode(this.issueForm.personCode, this.issueForm.cardType).then(response => {
        if (response.code === 200) {
          this.issueForm.decimalCode = response.data.decimalCode;
          this.issueForm.epcCode = response.data.epcCode;
          this.readSuccess = true;
        } else {
          this.$message.error(response.msg || '生成编码失败');
        }
      }).catch(error => {
        this.$message.error('生成编码失败：' + (error.message || '网络错误'));
      }).finally(() => {
        this.isGenerating = false;
      });
    },

    // ========== 读卡器相关 ==========
    checkReaderStatus() {
      getReaderStatus().then(response => {
        this.readerConnected = response?.connected || false;
      }).catch(() => {
        this.readerConnected = false;
      });
    },
    readCardFromReader() {
      this.readingCard = true;
      this.readSuccess = false;
      readCard().then(response => {
        this.readingCard = false;
        if (response.success) {
          this.issueForm.epcCode = response.epc;
          this.issueForm.decimalCode = response.decimalCode || this.convertEpcToDecimal(response.epc);
          this.readSuccess = true;
          this.$message.success(`读取成功！EPC: ${response.epc.substring(0, 16)}...`);
        } else {
          this.$message.error(response.message || '读取失败，请确保卡片已靠近读卡器');
        }
      }).catch(error => {
        this.readingCard = false;
        let errorMsg = '读取失败';
        if (error.message && error.message.includes('timeout')) {
          errorMsg = '读取超时，请确认读写器连接正常';
        } else if (error.message && error.message.includes('Network Error')) {
          errorMsg = '网络错误，请确认后端服务已启动';
        } else {
          errorMsg = error.response?.data?.message || error.message || '读取失败，请重试';
        }
        this.$message.error(errorMsg);
      });
    },
    convertEpcToDecimal(epc) {
      if (!epc || epc.length < 8) return epc;
      try {
        const hexPart = epc.length > 12 ? epc.substring(epc.length - 12) : epc;
        return parseInt(hexPart, 16).toString();
      } catch (e) {
        return epc;
      }
    },
    stopReading() {
      if (this.readingCard) {
        this.readingCard = false;
        stopRead().catch(() => {});
      }
    },
    handleDialogClose() {
      this.stopReading();
      this.readSuccess = false;
    },

    // ========== 提交写卡（统一处理新增和发卡） ==========
    submitWriteCard() {
      if (this.isAddMode) {
        this.$refs.issueForm.validate(valid => {
          if (!valid) return;
          this._doWriteCard();
        });
      } else {
        this._doWriteCard();
      }
    },
    editCard() {
      if (this.isAddMode) {
        this.$refs.issueForm.validate(valid => {
          if (!valid) return;
          this._doWriteCard();
        });
      } else {
        this._doWriteCard();
      }
    },
    _doWriteCard() {
      if (!this.issueForm.cardType) {
        this.$message.error('请选择卡类别');
        return;
      }
      if (!this.issueForm.epcCode) {
        this.$message.error('请先读取卡片信息');
        return;
      }
      if (!this.issueForm.personCode) {
        this.$message.error('缺少人员编号');
        return;
      }

      const loading = this.$loading({
        lock: true,
        text: '正在处理...',
        spinner: 'el-icon-loading',
        background: 'rgba(0, 0, 0, 0.7)'
      });
       writeEpc(this.issueForm).then(writeRes => {
          if (writeRes.success) {
            // 不再调用 issueCard，直接返回成功并刷新列表
            loading.close();
            this.$message.success('EPC写入成功');
            this.getList(); // 刷新列表
            return; // 终止链
          } else {
            loading.close();
            this.$message.error(writeRes.message || '写入EPC失败');
            return Promise.reject(new Error('写入EPC失败'));
          }
        }).then(() => {
        loading.close();
        this.$message.success('写卡并绑定成功');
        this.issueDialogVisible = false;
        this.getList();
      }).catch(error => {
        loading.close();
        const errorMsg = error.response?.data?.msg || error.message || '操作失败';
        this.$message.error(errorMsg);
      });
    },

    // ========== 注销 ==========
    handleCancel(row) {
      this.$confirm('确认注销该卡片吗？注销后该人员可重新发卡。', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        cancelCard({ id: row.id }).then(() => {
          this.$message.success('注销成功');
          this.getList();
        }).catch(error => {
          this.$message.error(error.response?.data?.msg || '注销失败');
        });
      }).catch(() => {});
    }
  }
};
</script>

<style scoped>
/* 样式保持不变 */
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
  min-width: 1000px;
}
.reading-status {
  text-align: center;
  padding: 12px;
  background: #e6f7ff;
  border-radius: 4px;
  margin-bottom: 15px;
  color: #1890ff;
}
.reading-status i {
  margin-right: 8px;
}
.read-success {
  text-align: center;
  padding: 12px;
  background: #f6ffed;
  border-radius: 4px;
  margin-bottom: 15px;
  color: #52c41a;
}
.read-success i {
  margin-right: 8px;
}
.has-value {
  background-color: #f0f9ff;
}
.dialog-footer .el-button--primary {
  background-color: #1890ff;
  border-color: #1890ff;
}
.dialog-footer .el-button--success {
  background-color: #52c41a;
  border-color: #52c41a;
}
@media (max-width: 768px) {
  .search-form .el-form-item {
    width: 100%;
    margin-right: 0;
  }
  .search-form .el-form-item .el-input,
  .search-form .el-form-item .el-select {
    width: 100% !important;
  }
}
</style>
