<template>
  <div class="app-container">
    <!-- 查询表单：使用 flex 布局，自动折行 -->
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
          <el-option label="副卡" value="副卡" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <!-- 表格：宽度100%，列宽按比例分配，超出部分横向滚动 -->
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
        <el-table-column label="人员编号" align="center" prop="personCode" min-width="150" show-overflow-tooltip />
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
              type="primary"
              icon="el-icon-document"
              @click="handleIssue(scope.row)"
            >发卡</el-button>
            <el-button
              v-if="scope.row.cardStatus === '1'"
              size="mini"
              type="danger"
              icon="el-icon-delete"
              @click="handleCancel(scope.row)"
            >注销</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 发卡弹窗（保持不变） -->
    <el-dialog title="发卡操作" :visible.sync="issueDialogVisible" width="500px" append-to-body>
      <el-form ref="issueForm" :model="issueForm" label-width="100px">
        <el-form-item label="姓名：">
          <span>{{ issueForm.holderName }}</span>
        </el-form-item>
        <el-form-item label="人员编号：">
          <span>{{ issueForm.personCode }}</span>
        </el-form-item>
        <el-form-item label="卡类别：" prop="cardType" required>
          <el-select v-model="issueForm.cardType" placeholder="请选择卡类别" clearable style="width: 100%">
            <el-option label="主卡" value="主卡" />
            <el-option label="副卡" value="副卡" />
          </el-select>
        </el-form-item>
        <el-form-item label="十进制编码：">
          <el-input v-model="issueForm.decimalCode" placeholder="请将标识牌放在读卡器上，点击【读取卡片】" />
        </el-form-item>
        <el-form-item label="EPC编码：">
          <el-input v-model="issueForm.epcCode" placeholder="读取后自动填充" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="readCardFromReader">读取卡片</el-button>
        <el-button type="success" @click="submitWriteCard">写卡</el-button>
        <el-button @click="issueDialogVisible = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCard, issueCard, cancelCard } from "@/api/system/cardIssue";
import RightToolbar from "@/components/RightToolbar";

export default {
  name: "CardIssue",
  components: { RightToolbar },
  data() {
    return {
      loading: false,
      showSearch: true,
      cardList: [],
      total: 0,
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        holderName: undefined,
        cardStatus: undefined,
        cardType: undefined,
      },
      issueDialogVisible: false,
      issueForm: {
        id: null,
        holderName: '',
        personCode: '',
        cardType: '',
        decimalCode: '',
        epcCode: '',
      }
    };
  },
  created() {
    this.getList();
  },
  methods: {
    getList() {
      this.loading = true;
      listCard(this.queryParams).then(response => {
        this.cardList = response.rows;
        this.total = response.total;
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
    handleSelectionChange(selection) {
      // 可扩展批量操作
    },
    handleIssue(row) {
      this.issueForm.id = row.id;
      this.issueForm.holderName = row.holderName;
      this.issueForm.personCode = row.personCode;
      this.issueForm.cardType = row.cardType;
      this.issueForm.decimalCode = '';
      this.issueForm.epcCode = '';
      this.issueDialogVisible = true;
    },
    readCardFromReader() {
      this.$prompt('请将标识牌放在读卡器上，点击确定后系统将读取卡片信息', '读取卡片', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^[0-9A-Fa-f]+$/,
        inputErrorMessage: '请输入有效的十六进制编码'
      }).then(({ value }) => {
        this.issueForm.decimalCode = value;
        this.issueForm.epcCode = value + "FFFFFFFF";
        this.$message.success('读取成功');
      }).catch(() => {});
    },
    submitWriteCard() {
      if (!this.issueForm.decimalCode || !this.issueForm.epcCode) {
        this.$message.error('请先读取卡片信息');
        return;
      }
      const data = {
        id: this.issueForm.id,
        cardType: this.issueForm.cardType,
        decimalCode: this.issueForm.decimalCode,
        epcCode: this.issueForm.epcCode
      };
      issueCard(data).then(() => {
        this.$message.success('写卡并绑定成功');
        this.issueDialogVisible = false;
        this.getList();
      }).catch(() => {
        this.$message.error('发卡失败');
      });
    },
    handleCancel(row) {
      this.$confirm('确认注销该卡片吗？注销后该人员可重新发卡。', '警告', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        cancelCard({ id: row.id }).then(() => {
          this.$message.success('注销成功');
          this.getList();
        });
      });
    }
  }
};
</script>

<style scoped>
/* 查询表单自动折行 */
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

/* 表格容器：确保宽度100%并支持横向滚动 */
.table-container {
  width: 100%;
  overflow-x: auto;
}
.el-table {
  width: 100%;
  min-width: 1000px; /* 保证在小屏幕下可滚动，避免挤压 */
}
</style>
