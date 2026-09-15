<template>
  <div class="layout">
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>{{ sidebarTitle }}</h2>
      </div>
      <div class="sidebar-user">
        <span>欢迎, {{ currentUser?.name }}</span>
        <el-button size="small" @click="handleLogout">退出登录</el-button>
      </div>
      <ul class="sidebar-menu">
        <li v-for="item in menuItems" :key="item.key">
          <a :class="{active: currentPage===item.key}" @click="currentPage=item.key">{{ item.label }}</a>
        </li>
      </ul>
    </div>

    <div class="main">
      <div class="main-header">
        <h3>{{ pageTitle }}</h3>
      </div>
      <div class="main-content">
        <!-- 主页 -->
        <div v-show="currentPage==='dashboard'" class="dashboard">
          <div class="dashboard-welcome">
            <h2>欢迎回来，{{ currentUser?.name }}！</h2>
            <p>今天是{{ currentDate }}，{{ isAdmin ? '您是系统管理员，拥有全部管理权限' : isHr ? '您是人事专员，负责人事与薪资管理' : '欢迎使用员工自助服务' }}</p>
          </div>

          <div class="dashboard-stats">
            <template v-if="!isUser">
              <div class="stat-card">
                <div class="stat-icon blue">👥</div>
                <div class="stat-info">
                  <h4>{{ empCount }}</h4>
                  <p>员工总数</p>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon green">📅</div>
                <div class="stat-info">
                  <h4>{{ currentMonthSalaryCount }}</h4>
                  <p>本月工资记录</p>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon orange">🏢</div>
                <div class="stat-info">
                  <h4>{{ deptCount }}</h4>
                  <p>部门数量</p>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon purple">💰</div>
                <div class="stat-info">
                  <h4>¥{{ totalSalaryAmount }}</h4>
                  <p>本月工资总额</p>
                </div>
              </div>
            </template>
            <template v-else>
              <div class="stat-card">
                <div class="stat-icon blue">💵</div>
                <div class="stat-info">
                  <h4>¥{{ Number(myMonthSalary).toFixed(2) }}</h4>
                  <p>本月实发工资</p>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon orange">⚠️</div>
                <div class="stat-info">
                  <h4>¥{{ Number(myMonthFine).toFixed(2) }}</h4>
                  <p>本月考勤罚金</p>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon green">📈</div>
                <div class="stat-info">
                  <h4>¥{{ Number(myYearSalary).toFixed(2) }}</h4>
                  <p>本年度工资总额</p>
                </div>
              </div>
              <div class="stat-card">
                <div class="stat-icon purple">🎁</div>
                <div class="stat-info">
                  <h4>¥{{ Number(myYearBonus).toFixed(2) }}</h4>
                  <p>本年度年终奖</p>
                </div>
              </div>
            </template>
          </div>

          <div class="dashboard-cards">
            <div class="dashboard-card">
              <h3>⚡ 快捷操作</h3>
              <div class="quick-actions">
                <template v-if="!isUser">
                  <div class="quick-action" @click="currentPage='employee'">
                    <div class="icon">👥</div>
                    <div>员工管理</div>
                  </div>
                  <div class="quick-action" @click="currentPage='salary'">
                    <div class="icon">💵</div>
                    <div>工资录入</div>
                  </div>
                  <div class="quick-action" @click="currentPage='salary'">
                    <div class="icon">📋</div>
                    <div>工资管理</div>
                  </div>
                  <div class="quick-action" @click="currentPage='attendance'">
                    <div class="icon">📅</div>
                    <div>考勤管理</div>
                  </div>
                </template>
                <template v-else>
                  <div class="quick-action" @click="currentPage='salary'">
                    <div class="icon">💵</div>
                    <div>工资查询</div>
                  </div>
                  <div class="quick-action" @click="currentPage='attendance'">
                    <div class="icon">📅</div>
                    <div>考勤查询</div>
                  </div>
                  <div class="quick-action" @click="currentPage='annualSalary'">
                    <div class="icon">📈</div>
                    <div>年薪查询</div>
                  </div>
                  <div class="quick-action" @click="currentPage='profile'">
                    <div class="icon">👤</div>
                    <div>个人信息</div>
                  </div>
                </template>
              </div>
            </div>

            <div class="dashboard-card">
              <h3>👤 个人信息</h3>
              <ul class="info-list">
                <li><span class="label">工号</span><span class="value">{{ currentUser?.id }}</span></li>
                <li><span class="label">姓名</span><span class="value">{{ currentUser?.name }}</span></li>
                <li><span class="label">部门</span><span class="value">{{ currentUser?.deptName || '未分配' }}</span>
                </li>
                <li><span class="label">职位</span><span class="value">{{ currentUser?.postName || '未分配' }}</span>
                </li>
                <li><span class="label">角色</span><span class="value">{{ currentUser?.roleName }}</span></li>
              </ul>
            </div>
          </div>

          <div v-if="!isUser" class="dashboard-charts">
            <div class="chart-card">
              <h3>📊 部门人数分布</h3>
              <div ref="deptChartRef" class="chart" style="height:300px;"></div>
            </div>
            <div class="chart-card">
              <h3>📈 月度工资趋势</h3>
              <div ref="salaryChartRef" class="chart" style="height:300px;"></div>
            </div>
          </div>
        </div>

        <!-- 部门管理 -->
        <div v-show="currentPage==='dept'" class="page-container">
          <div class="page-header">
            <h4 class="page-title">部门列表</h4>
            <el-button type="primary" @click="openAddDept">新增部门</el-button>
          </div>

          <div class="table-wrapper">
            <el-table :data="deptList" border stripe>
              <el-table-column prop="deptName" label="部门名称" min-width="200"></el-table-column>
              <el-table-column prop="remark" label="备注"></el-table-column>
              <el-table-column label="操作" min-width="160" align="center">
                <template #default="scope">
                  <el-button size="small" @click="openEditDept(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="deleteDept(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-dialog v-model="deptDialogVisible" title="部门信息" width="450px">
            <el-form label-width="100px">
              <el-form-item label="部门名称">
                <el-input v-model="deptForm.deptName" placeholder="请输入部门名称"></el-input>
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="deptForm.remark" placeholder="请输入备注信息"></el-input>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="deptDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="saveDept">保存</el-button>
            </template>
          </el-dialog>
        </div>

        <!-- 员工管理 -->
        <div v-show="currentPage==='employee'" class="page-container">
          <div class="page-header">
            <h4 class="page-title">{{ isHr ? '员工查询' : '员工管理' }}</h4>
            <el-button v-if="isAdmin" type="primary" @click="openAdd">新增员工</el-button>
          </div>

          <div class="search-bar">
            <el-select v-model="formQuery.deptId" placeholder="选择部门" style="width:200px"
                       @change="onDeptQueryChange">
              <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id"></el-option>
            </el-select>
            <el-select v-model="formQuery.postId" placeholder="选择职位" style="width:200px">
              <el-option v-for="p in filteredQueryPostList" :key="p.id" :label="p.postName" :value="p.id"></el-option>
            </el-select>
            <el-select v-model="formQuery.name" placeholder="选择员工" style="width:200px" filterable clearable>
              <el-option v-for="emp in empList" :key="emp.id" :label="emp.id + ' - ' + emp.name"
                         :value="emp.name"></el-option>
            </el-select>
            <div class="btn-group">
              <el-button type="primary" @click="getEmpList">查询</el-button>
              <el-button @click="resetFormQuery">重置</el-button>
            </div>
          </div>

          <div class="table-wrapper">
            <el-table :data="empList" border stripe fit>
              <el-table-column prop="id" label="工号" min-width="80"></el-table-column>
              <el-table-column prop="name" label="姓名"></el-table-column>
              <el-table-column prop="deptName" label="部门"></el-table-column>
              <el-table-column prop="postName" label="职位"></el-table-column>
              <el-table-column prop="levelName" label="职级"></el-table-column>
              <el-table-column prop="roleName" label="角色"></el-table-column>
              <el-table-column label="个人基本工资" min-width="120">
                <template #default="scope">{{ scope.row.personalSalary || scope.row.postBaseSalary || '-' }}</template>
              </el-table-column>
              <el-table-column label="操作" min-width="160" align="center">
                <template #default="scope">
                  <el-button v-if="isAdmin" size="small" @click="openEdit(scope.row)">编辑</el-button>
                  <el-button v-if="isAdmin" size="small" type="danger" @click="deleteEmp(scope.row.id)">删除</el-button>
                  <el-button v-if="isHr" size="small" @click="openEdit(scope.row)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-dialog v-model="dialogVisible" title="员工信息" width="450px">
            <el-form label-width="80px">
              <el-form-item label="姓名">
                <el-input v-model="form.name" placeholder="请输入姓名"></el-input>
              </el-form-item>
              <el-form-item label="部门">
                <el-select v-model="form.deptId" placeholder="请选择部门" style="width:100%" @change="onDeptChange">
                  <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="职位">
                <el-select v-model="form.postId" placeholder="请选择职位" style="width:100%" @change="onPostChange">
                  <el-option v-for="p in filteredPostList" :key="p.id" :label="p.postName" :value="p.id"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="职级">
                <el-select v-model="form.levelId" placeholder="请选择职级" style="width:100%">
                  <el-option v-for="l in levelList" :key="l.id" :label="l.levelName" :value="l.id"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="角色">
                <el-select v-model="form.roleId" placeholder="请选择角色" style="width:100%">
                  <el-option v-for="r in roleList" :key="r.id" :label="r.roleName" :value="r.id"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="登录账号">
                <el-input v-model="form.username" placeholder="请输入登录账号"></el-input>
              </el-form-item>
              <el-form-item label="登录密码">
                <el-input v-model="form.password" type="text" placeholder="请输入登录密码"></el-input>
              </el-form-item>
              <el-form-item label="个人工资">
                <el-input v-model.number="form.personalSalary" type="number"
                          placeholder="个人基本工资（默认继承职位基本工资）"></el-input>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="dialogVisible = false">取消</el-button>
              <el-button type="primary" @click="saveEmp">保存</el-button>
            </template>
          </el-dialog>
        </div>

        <!-- 岗位管理 -->
        <div v-show="currentPage==='post'" class="page-container">
          <div class="page-header">
            <h4 class="page-title">岗位管理</h4>
            <el-button type="primary" @click="openAddPost">新增岗位</el-button>
          </div>

          <div class="search-bar">
            <el-select v-model="postQuery.deptId" placeholder="选择部门" style="width:200px">
              <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id"></el-option>
            </el-select>
            <el-input v-model="postQuery.postName" placeholder="输入岗位名称搜索" style="width:200px"></el-input>
            <div class="btn-group">
              <el-button type="primary" @click="getPostList">查询</el-button>
              <el-button @click="resetPostQuery">重置</el-button>
            </div>
          </div>

          <div class="table-wrapper">
            <el-table :data="postList" border stripe>
              <el-table-column prop="postName" label="岗位名称" min-width="150"></el-table-column>
              <el-table-column prop="deptName" label="所属部门" min-width="150"></el-table-column>
              <el-table-column prop="baseSalary" label="基本工资" min-width="120"></el-table-column>
              <el-table-column label="操作" min-width="160" align="center">
                <template #default="scope">
                  <el-button size="small" @click="openEditPost(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="deletePost(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-dialog v-model="postDialogVisible" title="岗位信息" width="450px">
            <el-form label-width="100px">
              <el-form-item label="岗位名称">
                <el-input v-model="postForm.postName" placeholder="请输入岗位名称"></el-input>
              </el-form-item>
              <el-form-item label="所属部门">
                <el-select v-model="postForm.deptId" placeholder="请选择部门" style="width:100%">
                  <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="基本工资">
                <el-input v-model="postForm.baseSalary" type="number" placeholder="请输入基本工资"></el-input>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="postDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="savePost">保存</el-button>
            </template>
          </el-dialog>
        </div>

        <!-- 职级管理 -->
        <div v-show="currentPage==='level'" class="page-container">
          <div class="page-header">
            <h4 class="page-title">职级管理</h4>
            <el-button type="primary" @click="openAddLevel">新增职级</el-button>
          </div>

          <div class="table-wrapper">
            <el-table :data="levelList" border stripe>
              <el-table-column prop="levelName" label="职级名称" min-width="150"></el-table-column>
              <el-table-column prop="levelSubsidy" label="职级补贴" min-width="150"></el-table-column>
              <el-table-column label="操作" min-width="160" align="center">
                <template #default="scope">
                  <el-button size="small" @click="openEditLevel(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="deleteLevel(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-dialog v-model="levelDialogVisible" title="职级信息" width="450px">
            <el-form label-width="100px">
              <el-form-item label="职级名称">
                <el-input v-model="levelForm.levelName" placeholder="请输入职级名称"></el-input>
              </el-form-item>
              <el-form-item label="职级补贴">
                <el-input v-model="levelForm.levelSubsidy" type="number" placeholder="请输入职级补贴"></el-input>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="levelDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="saveLevel">保存</el-button>
            </template>
          </el-dialog>
        </div>

        <!-- 角色管理 -->
        <div v-show="currentPage==='role'" class="page-container">
          <div class="page-header">
            <h4 class="page-title">角色管理</h4>
          </div>

          <div class="table-wrapper">
            <el-table :data="roleList" border stripe>
              <el-table-column prop="roleName" label="角色名称" min-width="150"></el-table-column>
              <el-table-column prop="roleCode" label="角色编码" min-width="150"></el-table-column>
              <el-table-column prop="remark" label="备注"></el-table-column>
              <el-table-column label="操作" min-width="120" align="center">
                <template #default="scope">
                  <el-button size="small" @click="openEditRole(scope.row)">编辑</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-dialog v-model="roleDialogVisible" title="角色信息" width="450px">
            <el-form label-width="100px">
              <el-form-item label="角色名称">
                <el-input v-model="roleForm.roleName" placeholder="请输入角色名称"></el-input>
              </el-form-item>
              <el-form-item label="角色编码">
                <el-input v-model="roleForm.roleCode" placeholder="请输入角色编码（小写字母）" disabled></el-input>
              </el-form-item>
              <el-form-item label="备注">
                <el-input v-model="roleForm.remark" placeholder="请输入备注信息"></el-input>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="roleDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="saveRole">保存</el-button>
            </template>
          </el-dialog>
        </div>

        <!-- 补贴管理 -->
        <div v-show="currentPage==='subsidy'" class="page-container">
          <div class="page-header">
            <h4 class="page-title">补贴管理</h4>
            <el-button type="primary" @click="openAddSubsidy">新增补贴</el-button>
          </div>

          <div class="table-wrapper">
            <el-table :data="subsidyList" border stripe>
              <el-table-column prop="deptName" label="部门" min-width="150"></el-table-column>
              <el-table-column prop="foodSubsidy" label="餐补" min-width="100"></el-table-column>
              <el-table-column prop="trafficSubsidy" label="交通补贴" min-width="120"></el-table-column>
              <el-table-column prop="housingSubsidy" label="住房补贴" min-width="120"></el-table-column>
              <el-table-column label="操作" min-width="160" align="center">
                <template #default="scope">
                  <el-button size="small" @click="openEditSubsidy(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="deleteSubsidy(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-dialog v-model="subsidyDialog" title="补贴信息" width="450px">
            <el-form label-width="100px">
              <el-form-item label="部门">
                <el-select v-model="subsidyForm.deptId" style="width:100%">
                  <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.id"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="餐补">
                <el-input v-model="subsidyForm.foodSubsidy" type="number" placeholder="请输入餐补金额"></el-input>
              </el-form-item>
              <el-form-item label="交通补贴">
                <el-input v-model="subsidyForm.trafficSubsidy" type="number" placeholder="请输入交通补贴"></el-input>
              </el-form-item>
              <el-form-item label="住房补贴">
                <el-input v-model="subsidyForm.housingSubsidy" type="number" placeholder="请输入住房补贴"></el-input>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="subsidyDialog=false">取消</el-button>
              <el-button type="primary" @click="saveSubsidy">保存</el-button>
            </template>
          </el-dialog>
        </div>

        <!-- 考勤管理 -->
        <div v-show="currentPage==='attendance'" class="page-container">
          <div class="page-header">
            <h4 class="page-title">{{ isUser ? '考勤查询' : '考勤管理' }}</h4>
            <el-button v-if="!isUser" type="success" @click="openAddAttendance">新增考勤</el-button>
          </div>

          <div v-if="!isUser" class="search-bar">
            <div style="display:flex;gap:15px;align-items:center;">
              <div style="display:flex;align-items:center;gap:8px;">
                <label>迟到每次扣款：</label>
                <el-input v-model="lateFee" type="number" style="width:100px"></el-input>
              </div>
              <div style="display:flex;align-items:center;gap:8px;">
                <label>缺勤每天扣款：</label>
                <el-input v-model="absentFee" type="number" style="width:100px"></el-input>
              </div>
              <el-button type="primary" size="small" @click="saveAttendanceFee">保存设置</el-button>
            </div>
          </div>

          <div class="search-bar">
            <el-date-picker v-model="attendanceSearchMonth" type="month" placeholder="选择月份" format="YYYY-MM"
                            value-format="YYYY-MM" style="width:200px"></el-date-picker>
            <el-select v-if="!isUser" v-model="attendanceSearchDept" placeholder="选择部门" style="width:200px">
              <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.deptName"></el-option>
            </el-select>
            <el-select v-if="!isUser" v-model="attendanceSearchName" placeholder="选择员工" style="width:200px" filterable clearable>
              <el-option v-for="emp in empList" :key="emp.id" :label="emp.id + ' - ' + emp.name"
                         :value="emp.name"></el-option>
            </el-select>
            <div class="btn-group">
              <el-button type="primary" @click="getAttendanceList">查询</el-button>
              <el-button
                  @click="attendanceSearchMonth='';attendanceSearchDept='';attendanceSearchName='';getAttendanceList()">
                重置
              </el-button>
              <el-button v-if="!isUser" @click="toggleAttendanceSort">{{
                  attendanceSortOrder === 'DESC' ? '↓ 降序' : '↑ 升序'
                }}
              </el-button>
            </div>
          </div>

          <div class="table-wrapper">
            <el-table :data="attendanceList" border stripe empty-text="信息未录入或全勤">
              <el-table-column prop="month" label="月份" min-width="100">
                <template #default="scope">{{ scope.row.month?.substring(0, 7) }}</template>
              </el-table-column>
              <el-table-column prop="name" label="姓名" min-width="100"></el-table-column>
              <el-table-column prop="deptName" label="部门" min-width="120"></el-table-column>
              <el-table-column prop="lateTimes" label="迟到次数" min-width="100"></el-table-column>
              <el-table-column prop="absentDays" label="缺勤天数" min-width="100"></el-table-column>
              <el-table-column prop="fine" label="考勤罚金" min-width="100"></el-table-column>
              <el-table-column v-if="!isUser" label="操作" min-width="160" align="center">
                <template #default="scope">
                  <el-button size="small" @click="openEditAttendance(scope.row)">编辑</el-button>
                  <el-button size="small" type="danger" @click="deleteAttendance(scope.row.id)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-dialog v-model="attendanceDialog" title="考勤信息" width="450px">
            <el-form label-width="100px">
              <el-form-item label="月份">
                <el-date-picker v-model="attendanceForm.month" type="month" format="YYYY-MM" value-format="YYYY-MM-01"
                                style="width:100%" @change="calcAttendanceFine"></el-date-picker>
              </el-form-item>
              <el-form-item label="员工">
                <el-select v-model="attendanceForm.empId" style="width:100%" filterable @change="calcAttendanceFine">
                  <el-option v-for="emp in empList" :key="emp.id" :label="emp.id + ' - ' + emp.name"
                             :value="emp.id"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="迟到次数">
                <el-input v-model="attendanceForm.lateTimes" type="number" @input="calcAttendanceFine"
                          placeholder="请输入迟到次数"></el-input>
              </el-form-item>
              <el-form-item label="缺勤天数">
                <el-input v-model="attendanceForm.absentDays" type="number" @input="calcAttendanceFine"
                          placeholder="请输入缺勤天数"></el-input>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="attendanceDialog=false">取消</el-button>
              <el-button type="primary" @click="saveAttendance">保存</el-button>
            </template>
          </el-dialog>
        </div>

        <!-- 工资管理 -->
        <div v-show="currentPage==='salary'" class="page-container">
          <div class="page-header">
            <h4 class="page-title">{{ isUser ? '工资查询' : '工资管理' }}</h4>
            <div v-if="!isUser" style="display:flex;gap:10px;">
              <el-button type="success" @click="openAddSalary">录入工资</el-button>
              <el-button type="warning" @click="openBatchAddSalary">批量录入</el-button>
              <el-button type="info" @click="openAiPlanning">AI智能薪资筹划</el-button>
            </div>
          </div>

          <div class="search-bar">
            <el-date-picker v-model="searchMonth" type="month" placeholder="选择月份" format="YYYY-MM"
                            value-format="YYYY-MM" style="width:200px"></el-date-picker>
            <el-select v-if="!isUser" v-model="searchDept" placeholder="请选择部门" style="width:200px">
              <el-option v-for="d in deptList" :key="d.id" :label="d.deptName" :value="d.deptName"></el-option>
            </el-select>
            <el-select v-if="!isUser" v-model="searchName" placeholder="选择员工" style="width:200px" filterable clearable>
              <el-option v-for="emp in empList" :key="emp.id" :label="emp.id + ' - ' + emp.name"
                         :value="emp.name"></el-option>
            </el-select>
            <div class="btn-group">
              <el-button type="primary" @click="getSalaryList(1)">查询</el-button>
              <el-button @click="searchMonth='';searchDept='';searchName='';getSalaryList(1)">重置</el-button>
              <el-button v-if="!isUser" @click="toggleSalarySort">{{ salarySortOrder === 'DESC' ? '↓ 降序' : '↑ 升序' }}</el-button>
              <el-button type="success" @click="exportSalaryList">导出</el-button>
            </div>
          </div>

          <div class="table-wrapper">
            <el-table :data="salaryList" border stripe size="small">
              <el-table-column prop="month" label="月份" min-width="80">
                <template #default="scope">{{ scope.row.month?.substring(0, 7) }}</template>
              </el-table-column>
              <el-table-column prop="name" label="姓名" min-width="80"></el-table-column>
              <el-table-column prop="deptName" label="部门" min-width="100"></el-table-column>
              <el-table-column prop="basicSalary" label="基本工资" min-width="100"></el-table-column>
              <el-table-column prop="foodSubsidy" label="餐补" min-width="80"></el-table-column>
              <el-table-column prop="trafficSubsidy" label="交通补" min-width="80"></el-table-column>
              <el-table-column prop="housingSubsidy" label="住房补" min-width="80"></el-table-column>
              <el-table-column prop="levelSubsidy" label="职级补" min-width="80"></el-table-column>
              <el-table-column prop="lateFine" label="考勤罚金" min-width="100"></el-table-column>
              <el-table-column prop="tax" label="个人所得税" min-width="90"></el-table-column>
              <el-table-column prop="actualSalary" label="实发工资" min-width="110">
                <template #default="scope"><span style="font-weight:600;color:#3b82f6;">{{
                    scope.row.actualSalary
                  }}</span></template>
              </el-table-column>
              <el-table-column v-if="!isUser" label="操作" min-width="140" align="center">
                <template #default="scope">
                  <div style="display:flex;flex-direction:column;gap:6px;align-items:center;width:100%;">
                    <el-button size="small" style="width:60px;margin:0 auto;" @click="openEditSalary(scope.row)">编辑
                    </el-button>
                    <el-button size="small" type="danger" style="width:60px;margin:0 auto;"
                               @click="deleteSalary(scope.row.id)">删除
                    </el-button>
                  </div>
                </template>
              </el-table-column>
            </el-table>
          </div>
          <div style="margin-top: 20px; display: flex; justify-content: flex-end;">
            <el-pagination
                :current-page="salaryPageData.pageNum"
                :page-size="salaryPageData.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="salaryPageData.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange">
            </el-pagination>
          </div>

          <el-dialog v-model="salaryDialog" title="录入工资" width="500px">
            <el-form label-width="100px">
              <el-form-item label="月份">
                <el-date-picker v-model="salaryForm.month" type="month" placeholder="选择月份" format="YYYY-MM"
                                value-format="YYYY-MM" style="width:100%" @change="onMonthChange"></el-date-picker>
              </el-form-item>
              <el-form-item label="员工">
                <el-select v-model="salaryForm.empId" placeholder="选择员工" style="width:100%" filterable
                           @change="onEmpChange">
                  <el-option v-for="emp in empList" :key="emp.id" :label="emp.id + ' - ' + emp.name"
                             :value="emp.id"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="基本工资">
                <el-input v-model="salaryForm.basicSalary"></el-input>
              </el-form-item>
              <el-form-item label="职级补贴">
                <el-input v-model="salaryForm.levelSubsidy" disabled></el-input>
              </el-form-item>
              <el-form-item label="餐补">
                <el-input v-model="salaryForm.foodSubsidy" disabled></el-input>
              </el-form-item>
              <el-form-item label="交通补贴">
                <el-input v-model="salaryForm.trafficSubsidy" disabled></el-input>
              </el-form-item>
              <el-form-item label="住房补贴">
                <el-input v-model="salaryForm.housingSubsidy" disabled></el-input>
              </el-form-item>
              <el-form-item label="考勤罚金">
                <el-input v-model="salaryForm.lateFine" disabled></el-input>
              </el-form-item>
              <el-form-item label="个人所得税">
                <el-input v-model="salaryForm.tax" disabled></el-input>
              </el-form-item>
              <el-form-item label="实发工资">
                <el-input v-model="salaryForm.actualSalary" disabled></el-input>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="salaryDialog=false">关闭</el-button>
              <el-button type="primary" @click="saveSalary">保存</el-button>
            </template>
          </el-dialog>

          <el-dialog v-model="batchSalaryDialog" title="批量录入工资" width="600px">
            <el-form label-width="100px">
              <el-form-item label="录入方式">
                <el-radio-group v-model="batchSalaryForm.mode">
                  <el-radio value="single">单月录入</el-radio>
                  <el-radio value="year">全年12个月</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item v-if="batchSalaryForm.mode === 'single'" label="月份">
                <el-date-picker v-model="batchSalaryForm.month" type="month" placeholder="选择月份" format="YYYY-MM"
                                value-format="YYYY-MM" style="width:100%"></el-date-picker>
              </el-form-item>
              <el-form-item v-if="batchSalaryForm.mode === 'year'" label="年份">
                <el-select v-model="batchSalaryForm.year" placeholder="选择年份" style="width:100%">
                  <el-option v-for="y in [2024,2025,2026,2027]" :key="y" :label="y+'年'" :value="y"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="员工">
                <div style="width:100%">
                  <div style="margin-bottom:8px;display:flex;gap:8px;align-items:center;">
                    <el-button size="small" @click="selectAllEmps">全选</el-button>
                    <el-button size="small" @click="clearEmpSelection">取消全选</el-button>
                    <span style="color:#909399;font-size:12px;">已选 {{ batchSalaryForm.empIds.length }} / {{ empList.length }} 人</span>
                  </div>
                  <el-select v-model="batchSalaryForm.empIds" multiple placeholder="选择员工" style="width:100%"
                             filterable collapse-tags collapse-tags-tooltip>
                    <el-option v-for="emp in empList" :key="emp.id" :label="emp.id + ' - ' + emp.name"
                               :value="emp.id"></el-option>
                  </el-select>
                </div>
              </el-form-item>
              <el-form-item label="说明">
                <div style="color:#909399;font-size:13px;">
                  系统将自动根据员工的个人工资、职级、部门补贴生成工资数据<br>
                  考勤罚金将自动关联对应月份的考勤数据
                </div>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="batchSalaryDialog=false">取消</el-button>
              <el-button type="primary" @click="batchSaveSalary" :loading="batchSaving">批量保存</el-button>
            </template>
          </el-dialog>

          <el-dialog v-model="aiPlanningDialog" title="AI薪资分配分析" width="850px">
            <div style="padding:10px;">
              <div class="ai-planning-content">
                <div class="ai-input-section">
                  <el-form label-width="100px">
                    <el-form-item label="选择员工">
                      <el-select v-model="aiPlanningForm.empId" placeholder="请选择员工" filterable
                                 @change="onEmpSelect">
                        <el-option v-for="emp in empList" :key="emp.id" :label="emp.id + ' - ' + emp.name"
                                   :value="emp.id"></el-option>
                      </el-select>
                    </el-form-item>
                    <el-form-item label="年终奖">
                      <el-input v-model="aiPlanningForm.yearEndBonus" type="number" placeholder="输入年终奖金额"
                                style="width:200px;"></el-input>
                    </el-form-item>
                  </el-form>
                </div>

                <div class="ai-divider"></div>

                <div class="ai-input-section" v-if="showSalaryForm && aiPlanningTaxData">
                  <h5>📝 AI建议方案</h5>
                  <el-form label-width="120px">
                    <el-form-item label="平均个人月工资">
                      <el-input v-model.number="aiPlanningForm.basicSalary" type="number"
                                style="width:200px;"></el-input>
                      <span style="color:#10b981;font-size:12px;margin-left:10px;">(基于今年平均)</span>
                    </el-form-item>
                    <el-form-item label="年终奖">
                      <el-input v-model.number="aiPlanningForm.yearEndBonus" type="number"
                                style="width:200px;"></el-input>
                      <span style="color:#10b981;font-size:12px;margin-left:10px;">(AI建议值)</span>
                    </el-form-item>
                  </el-form>
                </div>

                <div class="ai-divider"></div>

                <div class="ai-output-section">
                  <div class="ai-loading" v-if="aiPlanningLoading">
                    <div class="loading-dots"><span></span><span></span><span></span></div>
                    <p>AI正在分析工资分配...</p>
                  </div>
                  <template v-else>
                    <div v-if="aiPlanningWarnings.length > 0" class="ai-warning">
                      <el-alert type="warning" :closable="false" show-icon>
                        <template #title><strong>⚠️ AI分析结果</strong></template>
                        <ul style="margin: 5px 0 0 0; padding-left: 20px;">
                          <li v-for="(warning, index) in aiPlanningWarnings" :key="index">{{ warning }}</li>
                        </ul>
                      </el-alert>
                    </div>

                    <div v-if="aiPlanningTaxData" class="ai-tax-comparison" style="margin-top:20px;">
                      <h4>⚡ 当前方案 - 税负实时计算</h4>
                      <table class="tax-table">
                        <thead>
                        <tr>
                          <th>项目</th>
                          <th>当前分配</th>
                          <th>调整后分配</th>
                          <th>变化</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr>
                          <td>平均个人月工资</td>
                          <td>¥{{ formatNumber(originalBasic) }}</td>
                          <td>¥{{ formatNumber(aiPlanningForm.basicSalary) }}</td>
                          <td :class="getChangeClass((aiPlanningForm.basicSalary || 0) - originalBasic)">
                            {{ formatChange((aiPlanningForm.basicSalary || 0) - originalBasic) }}
                          </td>
                        </tr>
                        <tr>
                          <td>年终奖</td>
                          <td>¥{{ formatNumber(originalBonus) }}</td>
                          <td>¥{{ formatNumber(aiPlanningForm.yearEndBonus) }}</td>
                          <td :class="getChangeClass((aiPlanningForm.yearEndBonus || 0) - originalBonus)">
                            {{ formatChange((aiPlanningForm.yearEndBonus || 0) - originalBonus) }}
                          </td>
                        </tr>
                        <tr>
                          <td>年度总税负</td>
                          <td>¥{{ formatNumber(aiPlanningTaxData.currentTotalTax) }}</td>
                          <td>¥{{ formatNumber(aiPlanningTaxData.suggestedTotalTax) }}</td>
                          <td :class="getChangeClass(aiPlanningTaxData.suggestedTotalTax - aiPlanningTaxData.currentTotalTax)">
                            {{ formatChange(aiPlanningTaxData.suggestedTotalTax - aiPlanningTaxData.currentTotalTax) }}
                          </td>
                        </tr>
                        </tbody>
                      </table>
                    </div>

                    <div class="ai-result" style="margin-top:20px;">
                      <h4>📝 AI分析报告</h4>
                      <div class="result-content" v-html="aiPlanningResult"></div>
                    </div>
                  </template>
                </div>
              </div>
            </div>
            <template #footer>
              <el-button @click="aiPlanningDialog=false">关闭</el-button>
              <el-button type="primary" @click="executeAiPlanning" :loading="aiPlanningLoading">开始分析</el-button>
              <el-button v-if="showSalaryForm && aiPlanningTaxData" type="success" @click="applySuggestions">应用建议
              </el-button>
            </template>
          </el-dialog>
        </div>

        <!-- 年薪管理 -->
        <div v-show="currentPage==='annualSalary'" class="page-container">
          <div class="page-header">
            <h4 class="page-title">{{ isUser ? '年薪查询' : '年薪管理' }}</h4>
            <el-button v-if="!isUser" type="primary" @click="openAddYearEndBonus">年终奖录入</el-button>
          </div>

          <div class="search-bar">
            <el-date-picker v-model="bonusQuery.year" type="year" placeholder="选择年份" format="YYYY"
                            value-format="YYYY" style="width:150px"></el-date-picker>
            <el-select v-if="!isUser" v-model="bonusQuery.name" placeholder="选择员工" style="width:200px" filterable clearable>
              <el-option v-for="emp in empList" :key="emp.id" :label="emp.id + ' - ' + emp.name"
                         :value="emp.name"></el-option>
            </el-select>
            <el-button type="primary" @click="getYearEndBonusList">查询</el-button>
            <el-button @click="resetBonusQuery">重置</el-button>
            <el-button v-if="!isUser" @click="toggleYearEndBonusSort">{{
                yearEndBonusSortOrder === 'DESC' ? '↓ 降序' : '↑ 升序'
              }}
            </el-button>
            <el-button type="success" @click="exportYearEndBonusList">导出</el-button>
          </div>

          <div class="table-wrapper">
            <el-table :data="yearEndBonusList" border stripe>
              <el-table-column prop="year" label="年份" min-width="80"></el-table-column>
              <el-table-column prop="name" label="姓名" min-width="100"></el-table-column>
              <el-table-column prop="deptName" label="部门" min-width="120"></el-table-column>
              <el-table-column prop="yearSalary" label="年度工资" min-width="120">
                <template #default="scope">{{ scope.row.yearSalary?.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column prop="yearBonus" label="年终奖" min-width="120">
                <template #default="scope">{{ scope.row.yearBonus?.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column prop="yearTax" label="年度个税" min-width="120">
                <template #default="scope">{{ scope.row.yearTax?.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column prop="yearActual" label="年度税后收入" min-width="140">
                <template #default="scope">{{ scope.row.yearActual?.toFixed(2) }}</template>
              </el-table-column>
              <el-table-column label="操作" min-width="160" align="center">
                <template #default="scope">
                  <el-button v-if="!isUser" size="small" type="primary" @click="openViewYearEndBonus(scope.row)">编辑</el-button>
                  <el-button v-if="!isUser" size="small" type="danger" @click="deleteYearEndBonus(scope.row.id)">删除</el-button>
                  <el-button v-if="isUser" size="small" @click="openViewYearEndBonus(scope.row)">查看</el-button>
                </template>
              </el-table-column>
            </el-table>
          </div>

          <el-dialog v-model="yearEndBonusDialog" title="录入年终奖" width="450px">
            <el-form label-width="100px">
              <el-form-item label="年份">
                <el-select v-model="yearEndBonusForm.year" style="width:100%">
                  <el-option v-for="y in [2024,2025,2026]" :key="y" :label="y+'年'" :value="y"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="员工">
                <el-select v-model="yearEndBonusForm.empId" style="width:100%" filterable>
                  <el-option v-for="emp in empList" :key="emp.id" :label="emp.id + ' - ' + emp.name"
                             :value="emp.id"></el-option>
                </el-select>
              </el-form-item>
              <el-form-item label="年终奖金额">
                <el-input v-model="yearEndBonusForm.bonusAmount" type="number" @input="calcBonusTax"
                          placeholder="请输入年终奖金额"></el-input>
              </el-form-item>
              <el-form-item label="个税">
                <el-input v-model="yearEndBonusForm.taxAmount" disabled></el-input>
              </el-form-item>
              <el-form-item label="税后金额">
                <el-input v-model="yearEndBonusForm.netAmount" disabled></el-input>
              </el-form-item>
            </el-form>
            <template #footer>
              <el-button @click="yearEndBonusDialog=false">关闭</el-button>
              <el-button type="primary" @click="saveYearEndBonus">保存</el-button>
            </template>
          </el-dialog>
        </div>

        <!-- 税务明细 -->
        <div v-show="currentPage==='taxDetail'" class="page-container">
          <div class="page-header">
            <h4 class="page-title">税务明细</h4>
          </div>

          <div class="tax-info-card">
            <h4>📋 中国税务标准说明</h4>

            <div v-if="monthlyTaxRates.length > 0" class="tax-section">
              <h5>一、个人所得税税率标准（2019年后）</h5>
              <p>个人所得税税率采用7级超额累进税率，按月计算：</p>
              <table class="tax-table">
                <thead>
                <tr>
                  <th>级数</th>
                  <th>月应纳税所得额</th>
                  <th>税率</th>
                  <th>速算扣除数</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="(rate, index) in monthlyTaxRates" :key="index">
                  <td>{{ index + 1 }}</td>
                  <td>{{ formatTaxRange(rate.minThreshold, rate.maxLimit) }}</td>
                  <td>{{ (rate.rate * 100).toFixed(2) }}%</td>
                  <td>{{ formatNumber(rate.quickD) }}</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <!-- 个人信息 -->
        <div v-show="currentPage==='profile'" class="page-container">
          <div class="page-header">
            <h4 class="page-title">个人信息</h4>
          </div>
          <el-card>
            <el-form label-width="100px">
              <el-form-item label="工号">
                <el-input v-model="currentUser.id" disabled></el-input>
              </el-form-item>
              <el-form-item label="姓名">
                <el-input v-model="currentUser.name" disabled></el-input>
              </el-form-item>
              <el-form-item label="部门">
                <el-input v-model="currentUser.deptName" disabled></el-input>
              </el-form-item>
              <el-form-item label="职位">
                <el-input v-model="currentUser.postName" disabled></el-input>
              </el-form-item>
              <el-form-item label="角色">
                <el-input v-model="currentUser.roleName" disabled></el-input>
              </el-form-item>
            </el-form>
          </el-card>
        </div>

        <!-- 修改密码 -->
        <div v-show="currentPage==='changePwd'" class="page-container">
          <div class="page-header">
            <h4 class="page-title">修改密码</h4>
          </div>
          <el-card>
            <el-form label-width="100px">
              <el-form-item label="旧密码">
                <el-input v-model="oldPwd" type="password"></el-input>
              </el-form-item>
              <el-form-item label="新密码">
                <el-input v-model="newPwd" type="password"></el-input>
              </el-form-item>
              <el-form-item label="确认密码">
                <el-input v-model="confirmPwd" type="password"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleChangePwd">提交修改</el-button>
              </el-form-item>
            </el-form>
          </el-card>
        </div>

      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import * as api from '../api'

const router = useRouter()

// ========== 基础数据 ==========
const currentPage = ref('dashboard')
const currentUser = ref({})
const currentDate = ref(new Date().toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' }))

// 角色判断
const isAdmin = computed(() => currentUser.value.roleCode === 'admin')
const isHr = computed(() => currentUser.value.roleCode === 'hr')
const isUser = computed(() => currentUser.value.roleCode !== 'admin' && currentUser.value.roleCode !== 'hr')

// 侧边栏动态菜单（按角色）
const menuItems = computed(() => {
  if (isAdmin.value) {
    return [
      { key: 'dashboard', label: '🏠 首页' },
      { key: 'employee', label: '👥 员工管理' },
      { key: 'dept', label: '🏢 部门管理' },
      { key: 'post', label: '💼 岗位管理' },
      { key: 'level', label: '📊 职级管理' },
      { key: 'role', label: '🔐 角色管理' },
      { key: 'subsidy', label: '💰 补贴管理' },
      { key: 'attendance', label: '📅 考勤管理' },
      { key: 'salary', label: '💵 工资管理' },
      { key: 'annualSalary', label: '📈 年薪管理' },
      { key: 'taxDetail', label: '📜 税务明细' },
      { key: 'profile', label: '👤 个人信息' },
      { key: 'changePwd', label: '🔒 修改密码' }
    ]
  } else if (isHr.value) {
    return [
      { key: 'dashboard', label: '🏠 首页' },
      { key: 'employee', label: '👥 员工查询' },
      { key: 'attendance', label: '📅 考勤管理' },
      { key: 'salary', label: '💵 工资查询' },
      { key: 'annualSalary', label: '📈 年薪查询' },
      { key: 'taxDetail', label: '📜 税务明细' },
      { key: 'profile', label: '👤 个人信息' },
      { key: 'changePwd', label: '🔒 修改密码' }
    ]
  } else {
    return [
      { key: 'dashboard', label: '🏠 首页' },
      { key: 'salary', label: '💵 工资查询' },
      { key: 'attendance', label: '📅 考勤查询' },
      { key: 'annualSalary', label: '📈 年薪查询' },
      { key: 'taxDetail', label: '📜 税务明细' },
      { key: 'profile', label: '👤 个人信息' },
      { key: 'changePwd', label: '🔒 修改密码' }
    ]
  }
})

// 侧边栏标题
const sidebarTitle = computed(() => {
  if (isAdmin.value) return '管理员后台'
  if (isHr.value) return '人事管理后台'
  return '员工服务中心'
})
const empCount = ref(0)
const deptCount = ref(0)
const currentMonthSalaryCount = ref(0)
const totalSalaryAmount = ref(0)
const lateFee = ref(Number(localStorage.getItem('lateFee')) || 20)
const absentFee = ref(Number(localStorage.getItem('absentFee')) || 100)

// 员工端个人统计
const myMonthSalary = ref(0)
const myMonthFine = ref(0)
const myYearSalary = ref(0)
const myYearBonus = ref(0)

// 搜索条件
const searchMonth = ref('')
const searchDept = ref('')
const searchName = ref('')
const attendanceSearchMonth = ref('')
const attendanceSearchDept = ref('')
const attendanceSearchName = ref('')

// 列表数据
const deptList = ref([])
const empList = ref([])
const postList = ref([])
const levelList = ref([])
const roleList = ref([])
const salaryList = ref([])
const subsidyList = ref([])
const attendanceList = ref([])
const yearEndBonusList = ref([])
const monthlyTaxRates = ref([])
const bonusTaxRates = ref([])

// 分页
const salaryPageData = reactive({ list: [], total: 0, pageNum: 1, pageSize: 10 })

// 排序
const attendanceSortOrder = ref('DESC')
const salarySortOrder = ref('DESC')
const yearEndBonusSortOrder = ref('DESC')

// 图表 ref
const deptChartRef = ref(null)
const salaryChartRef = ref(null)

// 对话框
const dialogVisible = ref(false)
const form = reactive({})
const postDialogVisible = ref(false)
const postForm = reactive({})
const levelDialogVisible = ref(false)
const levelForm = reactive({})
const roleDialogVisible = ref(false)
const roleForm = reactive({})
const deptDialogVisible = ref(false)
const deptForm = reactive({})
const subsidyDialog = ref(false)
const subsidyForm = reactive({})
const salaryDialog = ref(false)
const salaryForm = reactive({})
const yearEndBonusDialog = ref(false)
const yearEndBonusForm = reactive({})
const attendanceDialog = ref(false)
const attendanceForm = reactive({})
const formQuery = reactive({})
const postQuery = reactive({})
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

// AI 筹划
const aiPlanningDialog = ref(false)
const aiPlanningForm = reactive({ empId: '', basicSalary: 0, yearEndBonus: 0 })
const aiPlanningLoading = ref(false)
const aiPlanningResult = ref('')
const aiPlanningTaxData = ref(null)
const originalBasic = ref(0)
const originalBonus = ref(0)
const showSalaryForm = ref(false)
const aiPlanningWarnings = ref([])

// 年终奖
const bonusQuery = reactive({ year: '', name: '' })

// 批量工资
const batchSalaryDialog = ref(false)
const batchSalaryForm = reactive({ mode: 'single', month: '', year: new Date().getFullYear(), empIds: [] })
const batchSaving = ref(false)

// ========== 计算属性 ==========
const pageTitle = computed(() => {
  const map = {
    dashboard: '首页', employee: '员工管理', dept: '部门管理', post: '岗位管理',
    level: '职级管理', role: '角色管理', subsidy: '补贴管理', attendance: '考勤管理',
    salary: '工资管理', annualSalary: '年薪管理', taxDetail: '税务明细',
    profile: '个人信息', changePwd: '修改密码'
  }
  return map[currentPage.value] || '后台管理'
})

const filteredPostList = computed(() => {
  if (!form.deptId) return postList.value
  return postList.value.filter(item => Number(item.deptId) === Number(form.deptId))
})

const filteredQueryPostList = computed(() => {
  if (!formQuery.deptId) return postList.value
  return postList.value.filter(item => Number(item.deptId) === Number(formQuery.deptId))
})

// ========== 工具函数 ==========
const formatNumber = (num) => {
  if (num === null || num === undefined) return '0'
  return Number(num).toLocaleString()
}

const formatTaxRange = (min, max) => {
  const minVal = min !== null ? Number(min) : 0
  const maxVal = max !== null ? Number(max) : null
  if (minVal === 0 && maxVal !== null) return `≤${maxVal.toLocaleString()}元`
  else if (maxVal !== null) return `${minVal.toLocaleString()}~${maxVal.toLocaleString()}元`
  else return `＞${minVal.toLocaleString()}元`
}

const formatAiResult = (text) => {
  if (!text) return ''
  text = text.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  let lines = text.split('\n')
  let html = ''
  for (let line of lines) {
    line = line.trim()
    if (!line) html += '<br/>'
    else if (line.startsWith('一、') || line.startsWith('二、') || line.startsWith('三、')) html += `<h5>${line}</h5>`
    else if (line.startsWith('- ')) html += `<p style="margin-left: 20px;">${line}</p>`
    else html += `<p>${line}</p>`
  }
  return html
}

const downloadExcel = (filename, headers, rows) => {
  const csvContent = [headers.join('\t'), ...rows.map(row => row.join('\t'))].join('\n')
  const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  link.setAttribute('href', url)
  link.setAttribute('download', `${filename}_${new Date().toLocaleDateString().replace(/\//g, '-')}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  ElMessage.success('导出成功')
}

const checkLogin = () => {
  const userStr = sessionStorage.getItem('user')
  if (userStr) {
    currentUser.value = JSON.parse(userStr)
  } else {
    router.push('/login')
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' }).then(() => {
    sessionStorage.removeItem('user')
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

// ========== 部门 ==========
const getDeptList = async () => {
  const res = await api.getDeptList()
  deptList.value = res.data || []
}
const openAddDept = () => { Object.assign(deptForm, { id: '', deptName: '', remark: '' }); deptDialogVisible.value = true }
const openEditDept = (row) => { Object.assign(deptForm, row); deptDialogVisible.value = true }
const saveDept = async () => {
  if (!deptForm.deptName) { ElMessage.warning('请填写部门名称'); return }
  const fn = deptForm.id ? api.updateDept : api.addDept
  await fn(deptForm)
  ElMessage.success('保存成功'); deptDialogVisible.value = false; getDeptList()
}
const deleteDept = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    const res = await api.deleteDept(id)
    if (res.code === 200) { ElMessage.success('删除成功'); getDeptList() }
    else ElMessage.error(res.message || '删除失败')
  } catch {}
}

// ========== 员工 ==========
const getEmpList = async () => {
  const params = {}
  if (formQuery.deptId) params.deptId = formQuery.deptId
  if (formQuery.postId) params.postId = formQuery.postId
  if (formQuery.name) params.name = formQuery.name
  const res = await api.getEmployeeList(params)
  empList.value = res.data?.data || res.data?.list || []
}
const resetFormQuery = () => { Object.assign(formQuery, { deptId: '', postId: '', name: '' }); getEmpList() }
const onDeptChange = () => { form.postId = '' }
const onPostChange = () => {
  if (!form.postId) return
  const selectedPost = postList.value.find(p => Number(p.id) === Number(form.postId))
  if (selectedPost && selectedPost.baseSalary) form.personalSalary = selectedPost.baseSalary
}
const onDeptQueryChange = () => {}
const openAdd = () => {
  Object.assign(form, { id: '', name: '', deptId: '', postId: '', levelId: '', roleId: '', username: '', password: '', personalSalary: 0 })
  dialogVisible.value = true
}
const openEdit = (row) => {
  Object.assign(form, { ...row, deptId: Number(row.deptId), postId: Number(row.postId), levelId: Number(row.levelId), roleId: Number(row.roleId) })
  if (!form.personalSalary && row.postBaseSalary) form.personalSalary = row.postBaseSalary
  dialogVisible.value = true
}
const saveEmp = async () => {
  if (!form.name || !form.deptId || !form.postId || !form.levelId || !form.roleId || !form.username || !form.password) {
    ElMessage.warning('信息不全'); return
  }
  const fn = form.id ? api.updateEmployee : api.addEmployee
  await fn(form)
  ElMessage.success('保存成功'); dialogVisible.value = false; getEmpList()
}
const deleteEmp = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    const res = await api.deleteEmployee(id)
    if (res.code === 200) { ElMessage.success(res.message || '删除成功'); getEmpList() }
    else ElMessage.error(res.message || '删除失败')
  } catch {}
}

// ========== 职位 ==========
const getPostList = async () => {
  const params = {}
  if (postQuery.deptId) params.deptId = postQuery.deptId
  if (postQuery.postName) params.postName = postQuery.postName
  const res = await api.getPostList(params)
  postList.value = res.data || []
}
const resetPostQuery = () => { Object.assign(postQuery, { deptId: '', postName: '' }); getPostList() }
const openAddPost = () => { Object.assign(postForm, { id: '', postName: '', deptId: '', baseSalary: '' }); postDialogVisible.value = true }
const openEditPost = (row) => { Object.assign(postForm, row); postDialogVisible.value = true }
const savePost = async () => {
  if (!postForm.postName || !postForm.deptId) { ElMessage.warning('信息不全'); return }
  const fn = postForm.id ? api.updatePost : api.addPost
  await fn(postForm)
  ElMessage.success('保存成功'); postDialogVisible.value = false; getPostList()
}
const deletePost = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await api.deletePost(id); ElMessage.success('删除成功'); getPostList()
  } catch {}
}

// ========== 职级 ==========
const getLevelList = async () => { const res = await api.getLevelList(); levelList.value = res.data || [] }
const openAddLevel = () => { Object.assign(levelForm, { id: '', levelName: '', levelSubsidy: '' }); levelDialogVisible.value = true }
const openEditLevel = (row) => { Object.assign(levelForm, row); levelDialogVisible.value = true }
const saveLevel = async () => {
  if (!levelForm.levelName) { ElMessage.warning('请填写职级名称'); return }
  const fn = levelForm.id ? api.updateLevel : api.addLevel
  await fn(levelForm)
  ElMessage.success('保存成功'); levelDialogVisible.value = false; getLevelList()
}
const deleteLevel = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await api.deleteLevel(id); ElMessage.success('删除成功'); getLevelList()
  } catch {}
}

// ========== 角色 ==========
const getRoleList = async () => { const res = await api.getRoleList(); roleList.value = res.data || [] }
const openEditRole = (row) => { Object.assign(roleForm, row); roleDialogVisible.value = true }
const saveRole = async () => {
  if (!roleForm.roleName) { ElMessage.warning('请填写角色名称'); return }
  await api.saveRole(roleForm)
  ElMessage.success('保存成功'); roleDialogVisible.value = false; getRoleList()
}

// ========== 补贴 ==========
const getSubsidyList = async () => { const res = await api.getSubsidyList(); subsidyList.value = res.data || [] }
const openAddSubsidy = () => { Object.assign(subsidyForm, { id: '', deptId: '', foodSubsidy: 0, trafficSubsidy: 0, housingSubsidy: 0 }); subsidyDialog.value = true }
const openEditSubsidy = (row) => { Object.assign(subsidyForm, row); subsidyDialog.value = true }
const saveSubsidy = async () => {
  if (!subsidyForm.deptId) { ElMessage.warning('请选择部门'); return }
  const fn = subsidyForm.id ? api.updateSubsidy : api.addSubsidy
  await fn(subsidyForm)
  ElMessage.success('保存成功'); subsidyDialog.value = false; getSubsidyList()
}
const deleteSubsidy = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await api.deleteSubsidy(id); ElMessage.success('删除成功'); getSubsidyList()
  } catch {}
}

// ========== 税务 ==========
const getTaxRates = async () => {
  const res1 = await api.getTaxDetailList('个人所得税')
  monthlyTaxRates.value = res1.list || res1.data || []
  const res2 = await api.getTaxDetailList('年终奖单独计税税率')
  bonusTaxRates.value = res2.list || res2.data || []
}

// ========== 考勤 ==========
const getAttendanceList = async () => {
  const params = { sortOrder: attendanceSortOrder.value }
  if (attendanceSearchMonth.value) { params.month = attendanceSearchMonth.value; params.year = attendanceSearchMonth.value.substring(0, 4) }
  if (attendanceSearchDept.value) params.dept = attendanceSearchDept.value
  if (attendanceSearchName.value) params.name = attendanceSearchName.value
  if (isUser.value) params.name = currentUser.value.name
  const res = await api.getAttendanceList(params)
  attendanceList.value = res.data?.data || res.data?.list || []
}
const toggleAttendanceSort = () => { attendanceSortOrder.value = attendanceSortOrder.value === 'DESC' ? 'ASC' : 'DESC'; getAttendanceList() }
const openAddAttendance = () => {
  const now = new Date()
  const month = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`
  Object.assign(attendanceForm, { id: null, month, empId: null, lateTimes: 0, absentDays: 0, fine: 0 })
  attendanceDialog.value = true
}
const openEditAttendance = (row) => { Object.assign(attendanceForm, { ...row, empId: Number(row.empId) }); attendanceDialog.value = true }
const calcAttendanceFine = () => {
  const late = Number(attendanceForm.lateTimes) || 0
  const absent = Number(attendanceForm.absentDays) || 0
  attendanceForm.fine = late * lateFee.value + absent * absentFee.value
}
const saveAttendanceFee = () => {
  localStorage.setItem('lateFee', String(lateFee.value))
  localStorage.setItem('absentFee', String(absentFee.value))
  ElMessage.success('扣款标准已保存')
}
const saveAttendance = async () => {
  calcAttendanceFine()
  if (!attendanceForm.empId) { ElMessage.warning('请选择员工'); return }
  const monthStr = attendanceForm.month?.substring(0, 7) || ''
  const data = { ...attendanceForm, month: monthStr, empId: Number(attendanceForm.empId), lateTimes: Number(attendanceForm.lateTimes) || 0, absentDays: Number(attendanceForm.absentDays) || 0, fine: Number(attendanceForm.fine) || 0 }
  const res = await api.saveAttendance(data)
  if (res.code === 200) { ElMessage.success('保存成功'); attendanceDialog.value = false; getAttendanceList() }
  else ElMessage.error(res.message || '保存失败')
}
const deleteAttendance = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await api.deleteAttendance(id); ElMessage.success('删除成功'); getAttendanceList()
  } catch {}
}

// ========== 工资 ==========
const getSalaryList = async (pageNum = 1) => {
  const params = { pageNum, pageSize: salaryPageData.pageSize, sortOrder: salarySortOrder.value }
  if (searchMonth.value) { params.month = searchMonth.value; params.year = searchMonth.value.substring(0, 4) }
  if (searchDept.value) params.dept = searchDept.value
  if (searchName.value) params.name = searchName.value
  if (isUser.value) params.name = currentUser.value.name
  const res = await api.getSalaryList(params)
  if (res.code === 200) {
    salaryList.value = res.data?.data || res.data?.list || []
    Object.assign(salaryPageData, { ...res.data, pageNum })
  }
}
const toggleSalarySort = () => { salarySortOrder.value = salarySortOrder.value === 'DESC' ? 'ASC' : 'DESC'; getSalaryList(1) }
const handleSizeChange = (val) => { salaryPageData.pageSize = val; getSalaryList(1) }
const handleCurrentChange = (val) => { getSalaryList(val) }

const exportSalaryList = async () => {
  const params = { pageSize: 9999 }
  if (searchMonth.value) { params.month = searchMonth.value; params.year = searchMonth.value.substring(0, 4) }
  if (searchDept.value) params.dept = searchDept.value
  if (searchName.value) params.name = searchName.value
  const res = await api.getSalaryList(params)
  const allData = res.data?.data || res.data?.list || []
  if (allData.length === 0) { ElMessage.warning('暂无数据可导出'); return }
  const headers = ['月份', '姓名', '部门', '基本工资', '餐补', '交通补贴', '住房补贴', '职级补贴', '考勤罚金', '个税', '实发工资']
  const rows = allData.map(item => [
    item.month?.substring(0, 7) || '', item.name || '', item.deptName || '',
    (item.basicSalary || 0).toFixed(2), (item.foodSubsidy || 0).toFixed(2),
    (item.trafficSubsidy || 0).toFixed(2), (item.housingSubsidy || 0).toFixed(2),
    (item.levelSubsidy || 0).toFixed(2), (item.lateFine || 0).toFixed(2),
    (item.tax || 0).toFixed(2), (item.actualSalary || 0).toFixed(2)
  ])
  downloadExcel('工资明细', headers, rows)
}

const openAddSalary = () => {
  Object.assign(salaryForm, { id: null, month: '', empId: null, basicSalary: 0, levelSubsidy: 0, foodSubsidy: 0, trafficSubsidy: 0, housingSubsidy: 0, lateFine: 0, tax: 0, actualSalary: 0 })
  salaryDialog.value = true
}
const openEditSalary = (row) => { Object.assign(salaryForm, { ...row, empId: Number(row.empId) }); salaryDialog.value = true }
const onMonthChange = async () => { await onEmpChange() }
const onEmpChange = async () => {
  if (!salaryForm.empId || !salaryForm.month) return
  const res = await api.calculateSalary(salaryForm.empId, salaryForm.month)
  if (res.code === 200 && res.data) {
    salaryForm.levelSubsidy = res.data.levelSubsidy || 0
    salaryForm.foodSubsidy = res.data.foodSubsidy || 0
    salaryForm.trafficSubsidy = res.data.trafficSubsidy || 0
    salaryForm.housingSubsidy = res.data.housingSubsidy || 0
    salaryForm.lateFine = res.data.lateFine || 0
    salaryForm.tax = res.data.tax || 0
    salaryForm.actualSalary = res.data.actualSalary || 0
    if (!salaryForm.basicSalary) salaryForm.basicSalary = res.data.basicSalary || 0
  }
}
const saveSalary = async () => {
  if (!salaryForm.empId) { ElMessage.warning('请选择员工'); return }
  if (!salaryForm.month) { ElMessage.warning('请选择月份'); return }
  const monthStr = salaryForm.month?.substring(0, 7) || ''
  const data = { ...salaryForm, month: monthStr, empId: Number(salaryForm.empId), basicSalary: Number(salaryForm.basicSalary) || 0 }
  const res = await api.saveSalary(data)
  if (res.code === 200) { ElMessage.success('保存成功'); salaryDialog.value = false; getSalaryList(salaryPageData.pageNum) }
  else ElMessage.warning(res.message || '保存失败')
}
const deleteSalary = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await api.deleteSalary(id); ElMessage.success('删除成功'); getSalaryList(salaryPageData.pageNum)
  } catch {}
}

const openBatchAddSalary = () => {
  Object.assign(batchSalaryForm, { mode: 'single', month: '', year: new Date().getFullYear(), empIds: [] })
  batchSalaryDialog.value = true
}
const selectAllEmps = () => {
  batchSalaryForm.empIds = empList.value.map(e => e.id)
}
const clearEmpSelection = () => {
  batchSalaryForm.empIds = []
}
const batchSaveSalary = async () => {
  if (!batchSalaryForm.month && batchSalaryForm.mode === 'single') { ElMessage.warning('请选择月份'); return }
  if (!batchSalaryForm.empIds || batchSalaryForm.empIds.length === 0) { ElMessage.warning('请选择员工'); return }
  batchSaving.value = true
  try {
    const res = await api.batchSaveSalary(batchSalaryForm)
    if (res.code === 200) {
      ElMessage.success(res.message || '批量保存成功')
      batchSalaryDialog.value = false; getSalaryList(1)
    } else ElMessage.warning(res.message || '批量保存失败')
  } catch (e) { ElMessage.error('批量保存失败') } finally { batchSaving.value = false }
}

// ========== 年终奖 ==========
const getYearEndBonusList = async () => {
  const params = {}
  if (bonusQuery.year) params.year = bonusQuery.year
  if (bonusQuery.name) params.name = bonusQuery.name
  if (isUser.value) params.name = currentUser.value.name
  const res = await api.getYearEndBonusList(params)
  yearEndBonusList.value = res.bonusList || []
  sortYearEndBonus()
}
const toggleYearEndBonusSort = () => { yearEndBonusSortOrder.value = yearEndBonusSortOrder.value === 'DESC' ? 'ASC' : 'DESC'; sortYearEndBonus() }
const sortYearEndBonus = () => {
  yearEndBonusList.value.sort((a, b) => {
    const ya = Number(a.year), yb = Number(b.year)
    return yearEndBonusSortOrder.value === 'DESC' ? yb - ya : ya - yb
  })
}
const resetBonusQuery = () => { bonusQuery.year = ''; bonusQuery.name = ''; getYearEndBonusList() }
const exportYearEndBonusList = async () => {
  const params = {}
  if (bonusQuery.year) params.year = bonusQuery.year
  if (bonusQuery.name) params.name = bonusQuery.name
  const res = await api.getYearEndBonusList(params)
  const allData = res.bonusList || []
  if (allData.length === 0) { ElMessage.warning('暂无数据可导出'); return }
  const headers = ['年份', '姓名', '部门', '年终奖', '个税', '税后金额']
  const rows = allData.map(item => [item.year || '', item.name || '', item.deptName || '', (item.bonusAmount || 0).toFixed(2), (item.taxAmount || 0).toFixed(2), (item.netAmount || 0).toFixed(2)])
  downloadExcel('年终奖明细', headers, rows)
}
const openAddYearEndBonus = () => {
  Object.assign(yearEndBonusForm, { id: null, empId: null, year: new Date().getFullYear(), bonusAmount: 0, taxAmount: 0, netAmount: 0 })
  yearEndBonusDialog.value = true
}
const openViewYearEndBonus = (row) => {
  Object.assign(yearEndBonusForm, { id: row.id, empId: Number(row.empId), year: row.year, bonusAmount: row.bonusAmount, taxAmount: row.taxAmount, netAmount: row.netAmount })
  yearEndBonusDialog.value = true
}
const calcBonusTax = () => {
  const bonus = Number(yearEndBonusForm.bonusAmount) || 0
  if (bonus <= 0) { yearEndBonusForm.taxAmount = 0; yearEndBonusForm.netAmount = 0; return }
  const monthly = bonus / 12
  let rate, deduction
  if (monthly <= 3000) { rate = 0.03; deduction = 0 }
  else if (monthly <= 12000) { rate = 0.10; deduction = 210 }
  else if (monthly <= 25000) { rate = 0.20; deduction = 1410 }
  else if (monthly <= 35000) { rate = 0.25; deduction = 2660 }
  else if (monthly <= 55000) { rate = 0.30; deduction = 4410 }
  else if (monthly <= 80000) { rate = 0.35; deduction = 7160 }
  else { rate = 0.45; deduction = 15160 }
  const tax = bonus * rate - deduction
  yearEndBonusForm.taxAmount = Math.round(tax * 100) / 100
  yearEndBonusForm.netAmount = bonus - tax
}
const saveYearEndBonus = async () => {
  if (!yearEndBonusForm.empId || !yearEndBonusForm.year) { ElMessage.warning('请选择员工和年份'); return }
  const res = await api.saveYearEndBonus(yearEndBonusForm)
  if (res.code === 200) { ElMessage.success('保存成功'); yearEndBonusDialog.value = false; getYearEndBonusList() }
  else ElMessage.error('保存失败: ' + (res.msg || '未知错误'))
}
const deleteYearEndBonus = async (id) => {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    const res = await api.deleteYearEndBonus(id)
    if (res.code === 200) { ElMessage.success('删除成功'); getYearEndBonusList() }
    else ElMessage.error('删除失败')
  } catch {}
}

// ========== AI 筹划 ==========
const openAiPlanning = () => {
  aiPlanningDialog.value = true
  Object.assign(aiPlanningForm, { empId: '', basicSalary: 0, yearEndBonus: 0 })
  aiPlanningResult.value = ''
  aiPlanningTaxData.value = null
  showSalaryForm.value = false
  aiPlanningWarnings.value = []
}
const onEmpSelect = async () => {
  if (!aiPlanningForm.empId) return
  aiPlanningTaxData.value = null
  aiPlanningResult.value = ''
  aiPlanningWarnings.value = []
  aiPlanningForm.yearEndBonus = 0
  const currentYear = new Date().getFullYear()
  try {
    const [salaryRes, bonusRes] = await Promise.all([
      api.getSalaryList({ empId: aiPlanningForm.empId, pageSize: 100 }),
      api.getYearEndBonusList({ year: currentYear, empId: aiPlanningForm.empId })
    ])
    let avgSalary = 0
    const salaries = salaryRes.data?.data || salaryRes.data?.list || []
    const yearSalaries = salaries.filter(s => s.month && String(s.month).startsWith(String(currentYear)))
    if (yearSalaries.length > 0) {
      const total = yearSalaries.reduce((sum, s) => sum + Number(s.basicSalary || 0), 0)
      avgSalary = Math.round((total / yearSalaries.length) * 100) / 100
    }
    const bonusList = bonusRes.bonusList || []
    const currentYearBonus = bonusList.find(b => Number(b.year) === currentYear)
    if (currentYearBonus) aiPlanningForm.yearEndBonus = Number(currentYearBonus.bonusAmount) || 0
    if (avgSalary === 0) {
      const empRes = await api.getEmployee(aiPlanningForm.empId)
      if (empRes.data) avgSalary = empRes.data.personalSalary || empRes.data.postBaseSalary || 0
    }
    aiPlanningForm.basicSalary = avgSalary
    showSalaryForm.value = true
  } catch { showSalaryForm.value = false }
}
const executeAiPlanning = async () => {
  if (!aiPlanningForm.empId) { ElMessage.warning('请选择员工'); return }
  aiPlanningLoading.value = true
  aiPlanningResult.value = ''
  aiPlanningWarnings.value = []
  const currentYear = new Date().getFullYear()
  try {
    const res = await api.aiPlanning({
      empId: Number(aiPlanningForm.empId), year: currentYear, targetSalary: 0, objective: 'tax_opt',
      yearEndBonus: Number(aiPlanningForm.yearEndBonus) || 0, basicSalary: aiPlanningForm.basicSalary || 0
    })
    if (res.code === 200) {
      if (res.salaryData) { aiPlanningForm.basicSalary = Number(res.salaryData.basicSalary) || 0; originalBasic.value = aiPlanningForm.basicSalary; showSalaryForm.value = true }
      originalBonus.value = Number(aiPlanningForm.yearEndBonus) || 0
      if (res.suggestedData) {
        if (res.suggestedData.suggestedBasicSalary !== undefined) aiPlanningForm.basicSalary = res.suggestedData.suggestedBasicSalary
        if (res.suggestedData.suggestedBonus !== undefined) aiPlanningForm.yearEndBonus = res.suggestedData.suggestedBonus
      }
      if (res.suggestions && res.suggestions.length > 0) aiPlanningWarnings.value = res.suggestions
      if (res.analysis) aiPlanningResult.value = formatAiResult(res.analysis)
      if (res.taxData) aiPlanningTaxData.value = res.taxData
    } else ElMessage.error(res.msg || '分析失败')
  } catch { aiPlanningResult.value = '<p>网络错误，AI服务暂时不可用</p>' } finally { aiPlanningLoading.value = false }
}
const getChangeClass = (val) => val > 0 ? 'up' : (val < 0 ? 'down' : '')
const formatChange = (val) => {
  if (val > 0) return '+' + formatNumber(val)
  if (val < 0) return formatNumber(val)
  return '0'
}
const applySuggestions = () => {
  if (!aiPlanningForm.empId || (!aiPlanningForm.basicSalary && !aiPlanningForm.yearEndBonus)) {
    ElMessage.warning('没有可应用的建议值'); return
  }
  applyAiSuggestions()
}
const applyAiSuggestions = async () => {
  const empId = Number(aiPlanningForm.empId)
  if (aiPlanningForm.basicSalary) {
    await api.updatePersonalSalary({ id: empId, personalSalary: aiPlanningForm.basicSalary })
  }
  ElMessage.success('建议已应用到员工个人基本工资')
  getEmpList()
}

// ========== 修改密码 ==========
const handleChangePwd = async () => {
  if (!pwdForm.oldPassword || !pwdForm.newPassword || !pwdForm.confirmPassword) {
    ElMessage.warning('请填写完整信息'); return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) { ElMessage.warning('两次密码不一致'); return }
  try {
    await api.changePassword({ id: currentUser.value.id, oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功'); Object.assign(pwdForm, { oldPassword: '', newPassword: '', confirmPassword: '' })
  } catch { ElMessage.error('密码修改失败') }
}

// ========== 仪表盘 ==========
const loadDashboardData = () => {
  empCount.value = empList.value.length
  deptCount.value = deptList.value.length
  loadSalaryDataForDashboard()
  if (isUser.value) loadMyDashboardData()
  nextTick(() => { loadDeptChartData(); loadSalaryChartData() })
}
const loadMyDashboardData = async () => {
  const now = new Date()
  const curMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  const curYear = String(now.getFullYear())
  // 本月工资
  const sRes = await api.getSalaryList({ pageSize: 9999, name: currentUser.value.name })
  const sData = sRes.data?.data || sRes.data?.list || []
  const monthSalary = sData.find(s => s.month?.substring(0, 7) === curMonth)
  myMonthSalary.value = monthSalary ? Number(monthSalary.actualSalary || 0) : 0
  myMonthFine.value = monthSalary ? Number(monthSalary.lateFine || 0) : 0
  // 年度工资
  myYearSalary.value = sData
    .filter(s => s.month?.startsWith(curYear))
    .reduce((sum, s) => sum + Number(s.actualSalary || 0), 0)
  // 年终奖
  const bRes = await api.getYearEndBonusList({ name: currentUser.value.name, year: curYear })
  const bData = bRes.bonusList || []
  myYearBonus.value = bData.reduce((sum, b) => sum + Number(b.netAmount || b.yearBonus || 0), 0)
}
const loadSalaryDataForDashboard = async () => {
  const now = new Date()
  const curMonth = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  const res = await api.getSalaryList({ pageSize: 9999, month: curMonth, year: String(now.getFullYear()) })
  const data = res.data?.data || res.data?.list || []
  currentMonthSalaryCount.value = data.length
  totalSalaryAmount.value = data.reduce((sum, s) => sum + Number(s.actualSalary || 0), 0).toFixed(2)
}
const loadDeptChartData = () => {
  const deptCounts = {}
  empList.value.forEach(emp => { const d = emp.deptName || '未分配'; deptCounts[d] = (deptCounts[d] || 0) + 1 })
  if (!deptChartRef.value) return
  const chart = echarts.init(deptChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { orient: 'vertical', right: '5%', top: 'center' },
    series: [{ name: '部门人数', type: 'pie', radius: ['40%', '70%'], data: Object.entries(deptCounts).map(([name, value]) => ({ value, name })) }]
  })
}
const loadSalaryChartData = async () => {
  const res = await api.getSalaryList({ pageSize: 9999 })
  const data = res.data?.data || res.data?.list || []
  const monthlyMap = {}
  data.forEach(s => { const m = s.month?.substring(0, 7) || ''; monthlyMap[m] = (monthlyMap[m] || 0) + Number(s.actualSalary || 0) })
  const months = Object.keys(monthlyMap).sort()
  if (!salaryChartRef.value) return
  const chart = echarts.init(salaryChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: months },
    yAxis: { type: 'value' },
    series: [{ name: '实发工资', type: 'line', smooth: true, data: months.map(m => monthlyMap[m].toFixed(2)) }]
  })
}

// ========== 监听页面切换 ==========
watch(currentPage, (newPage) => {
  if (newPage === 'dashboard') loadDashboardData()
  if (newPage === 'dept') getDeptList()
  if (newPage === 'employee') getEmpList()
  if (newPage === 'post') getPostList()
  if (newPage === 'level') getLevelList()
  if (newPage === 'role') getRoleList()
  if (newPage === 'subsidy') getSubsidyList()
  if (newPage === 'attendance') getAttendanceList()
  if (newPage === 'salary') getSalaryList()
  if (newPage === 'annualSalary') getYearEndBonusList()
  if (newPage === 'taxDetail') getTaxRates()
})

// ========== 初始化 ==========
onMounted(async () => {
  checkLogin()
  await getDeptList()
  await getPostList()
  await getLevelList()
  await getRoleList()
  await getEmpList()
  loadDashboardData()
})
</script>

<style>
/* 全局布局样式 */
.layout { display: flex; min-height: 100vh; }
.sidebar { width: 220px; background: #1e293b; color: #fff; padding: 20px 0; }
.sidebar-header { padding: 0 20px 20px; border-bottom: 1px solid #334155; }
.sidebar-header h2 { color: #fff; font-size: 18px; }
.sidebar-user { padding: 12px 20px; border-bottom: 1px solid #334155; font-size: 13px; display: flex; justify-content: space-between; align-items: center; }
.sidebar-menu { list-style: none; padding: 0; margin: 0; }
.sidebar-menu li { padding: 0; }
.sidebar-menu a { display: block; padding: 12px 20px; color: #cbd5e1; cursor: pointer; transition: all 0.2s; }
.sidebar-menu a:hover { background: #334155; color: #fff; }
.sidebar-menu a.active { background: #3b82f6; color: #fff; }
.main { flex: 1; background: #f1f5f9; }
.main-header { background: #fff; padding: 16px 24px; border-bottom: 1px solid #e2e8f0; }
.main-header h3 { margin: 0; color: #1e293b; }
.main-content { padding: 24px; }
.page-container { background: #fff; border-radius: 8px; padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-title { margin: 0; color: #1e293b; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; align-items: center; }
.btn-group { display: flex; gap: 8px; }
.table-wrapper { overflow-x: auto; }
.table-wrapper :deep(.el-table) {
  width: 100% !important;
}
.dashboard-welcome { background: linear-gradient(135deg, #3b82f6, #2563eb); color: #fff; padding: 30px; border-radius: 12px; margin-bottom: 20px; }
.dashboard-stats { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card { background: #fff; border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 16px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.stat-icon { width: 56px; height: 56px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 28px; }
.stat-icon.blue { background: #dbeafe; }
.stat-icon.green { background: #dcfce7; }
.stat-icon.orange { background: #ffedd5; }
.stat-icon.purple { background: #f3e8ff; }
.stat-info h4 { margin: 0; font-size: 24px; color: #1e293b; }
.stat-info p { margin: 4px 0 0; color: #64748b; font-size: 13px; }
.dashboard-cards { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; margin-bottom: 20px; }
.dashboard-card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.dashboard-card h3 { margin: 0 0 16px; color: #1e293b; }
.quick-actions { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.quick-action { display: flex; align-items: center; gap: 10px; padding: 12px; border: 1px solid #e2e8f0; border-radius: 8px; cursor: pointer; transition: all 0.2s; }
.quick-action:hover { border-color: #3b82f6; background: #eff6ff; }
.quick-action .icon { font-size: 24px; }
.info-list { list-style: none; padding: 0; margin: 0; }
.info-list li { display: flex; justify-content: space-between; padding: 10px 0; border-bottom: 1px solid #f1f5f9; }
.info-list .label { color: #64748b; }
.info-list .value { color: #1e293b; font-weight: 500; }
.dashboard-charts { display: grid; grid-template-columns: 1fr 1fr; gap: 16px; }
.chart-card { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }
.chart-card h3 { margin: 0 0 16px; color: #1e293b; }
.tax-table { width: 100%; border-collapse: collapse; margin-top: 12px; }
.tax-table th, .tax-table td { border: 1px solid #e2e8f0; padding: 10px; text-align: center; }
.tax-table th { background: #f8fafc; color: #475569; }
.tax-info-card { background: #fff; border-radius: 12px; padding: 20px; }
.tax-section { margin-bottom: 24px; }
.ai-planning-content { display: flex; flex-direction: column; gap: 16px; }
.ai-divider { height: 1px; background: #e2e8f0; }
.tax-table .up { color: #ef4444; }
.tax-table .down { color: #10b981; }
.ai-loading { text-align: center; padding: 40px; }
.loading-dots { display: flex; justify-content: center; gap: 8px; margin-bottom: 12px; }
.loading-dots span { width: 12px; height: 12px; border-radius: 50%; background: #3b82f6; animation: bounce 1.4s infinite ease-in-out both; }
.loading-dots span:nth-child(1) { animation-delay: -0.32s; }
.loading-dots span:nth-child(2) { animation-delay: -0.16s; }
@keyframes bounce { 0%, 80%, 100% { transform: scale(0); } 40% { transform: scale(1); } }
.result-content { line-height: 1.8; color: #334155; }
.result-content h5 { color: #1e293b; margin: 12px 0 6px; }
</style>