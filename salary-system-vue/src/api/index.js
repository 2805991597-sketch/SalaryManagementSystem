import request from './request'

// 登录
export const login = (data) => request.post('/login', data)
export const logout = () => request.post('/logout')

// 部门
export const getDeptList = (deptName) => request.get('/dept/list', { params: { deptName } })
export const addDept = (data) => request.post('/dept/add', data)
export const updateDept = (data) => request.post('/dept/update', data)
export const deleteDept = (id) => request.get('/dept/delete', { params: { id } })

// 员工
export const getEmployeeList = (params) => request.get('/employee/list', { params })
export const addEmployee = (data) => request.post('/employee/add', data)
export const updateEmployee = (data) => request.post('/employee/update', data)
export const deleteEmployee = (id) => request.get('/employee/delete', { params: { id } })
export const getEmployee = (id) => request.get('/employee/get', { params: { id } })
export const changePassword = (data) => request.post('/employee/changePassword', data)
export const updatePersonalSalary = (data) => request.post('/employee/updatePersonalSalary', data)

// 职位
export const getPostList = (params) => request.get('/post/list', { params })
export const addPost = (data) => request.post('/post/add', data)
export const updatePost = (data) => request.post('/post/update', data)
export const deletePost = (id) => request.get('/post/delete', { params: { id } })

// 职级
export const getLevelList = () => request.get('/level/list')
export const addLevel = (data) => request.post('/level/add', data)
export const updateLevel = (data) => request.post('/level/update', data)
export const deleteLevel = (id) => request.post('/level/delete', null, { params: { id } })

// 角色
export const getRoleList = () => request.get('/role/list')
export const saveRole = (data) => request.post('/role/save', data)

// 补贴
export const getSubsidyList = () => request.get('/subsidy/list')
export const addSubsidy = (data) => request.post('/subsidy/add', data)
export const updateSubsidy = (data) => request.post('/subsidy/update', data)
export const deleteSubsidy = (id) => request.get('/subsidy/delete', { params: { id } })

// 考勤
export const getAttendanceList = (params) => request.get('/attendance/list', { params })
export const saveAttendance = (data) => request.post('/attendance/save', data)
export const deleteAttendance = (id) => request.get('/attendance/delete', { params: { id } })

// 薪资
export const getSalaryList = (params) => request.get('/salary/list', { params })
export const saveSalary = (data) => request.post('/salary/save', data)
export const batchSaveSalary = (data) => request.post('/salary/batchSave', data)
export const deleteSalary = (id) => request.delete('/salary/delete', { params: { id } })
export const calculateSalary = (empId, month) => request.get('/salary/calculate', { params: { empId, month } })
export const checkAllSalary = () => request.get('/salary/checkAll')

// AI 筹划
export const aiPlanning = (data) => request.post('/salary/aiPlanning', data)

// 年终奖
export const getYearEndBonusList = (params) => request.get('/year-end-bonus/list', { params })
export const saveYearEndBonus = (data) => request.post('/year-end-bonus/save', data)
export const deleteYearEndBonus = (id) => request.delete('/year-end-bonus/delete', { params: { id } })

// 税务
export const getTaxDetailList = (taxType) => request.get('/tax-detail/list', { params: { taxType } })
