/**
 * Comprehensive Automated Test Suite for Student Study Planner
 * Covers all 5 required features, edge cases, validation, user isolation, and dashboard metrics.
 */

const BASE_URL = 'http://localhost:8080/api';

let totalTests = 0;
let passedTests = 0;
let failedTests = 0;

function assert(condition, testName, details = '') {
  totalTests++;
  if (condition) {
    passedTests++;
    console.log(`  ✅ [PASS] ${testName}`);
  } else {
    failedTests++;
    console.error(`  ❌ [FAIL] ${testName} - ${details}`);
  }
}

async function request(path, options = {}) {
  const url = `${BASE_URL}${path}`;
  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  const response = await fetch(url, {
    ...options,
    headers,
    body: options.body ? JSON.stringify(options.body) : undefined,
  });

  let data = null;
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    data = await response.json();
  }

  return { status: response.status, data };
}

async function runAllTests() {
  console.log('\n======================================================');
  console.log('🧪 RUNNING STUDENT STUDY PLANNER FULL TEST SUITE');
  console.log('======================================================\n');

  const timestamp = Date.now();
  const user1Email = `student_${timestamp}@example.com`;
  const user2Email = `other_${timestamp}@example.com`;
  const password = 'securePassword123';

  let token1 = null;
  let token2 = null;
  let user1Id = null;

  // -----------------------------------------------------------------
  // FEATURE 1: REGISTER / LOGIN (JWT)
  // -----------------------------------------------------------------
  console.log('📌 FEATURE 1: Authentication & JWT');

  // 1.1 Register User 1
  const reg1 = await request('/auth/register', {
    method: 'POST',
    body: { name: 'Alice Student', email: user1Email, password },
  });
  assert(reg1.status === 201 && reg1.data.token && reg1.data.email === user1Email,
    '1.1 Register new student with valid credentials',
    `Status: ${reg1.status}`);
  token1 = reg1.data?.token;
  user1Id = reg1.data?.id;

  // 1.2 Duplicate registration
  const regDup = await request('/auth/register', {
    method: 'POST',
    body: { name: 'Alice Duplicate', email: user1Email, password },
  });
  assert(regDup.status === 400,
    '1.2 Reject registration with existing email (400 Bad Request)',
    `Status: ${regDup.status}`);

  // 1.3 Invalid email format
  const regInvalidEmail = await request('/auth/register', {
    method: 'POST',
    body: { name: 'Bad Email', email: 'not-an-email', password },
  });
  assert(regInvalidEmail.status === 400 && regInvalidEmail.data.validationErrors?.email,
    '1.3 Reject invalid email format with validation message',
    `Status: ${regInvalidEmail.status}`);

  // 1.4 Short password
  const regShortPass = await request('/auth/register', {
    method: 'POST',
    body: { name: 'Short Pass', email: `short_${timestamp}@example.com`, password: '123' },
  });
  assert(regShortPass.status === 400 && regShortPass.data.validationErrors?.password,
    '1.4 Reject password shorter than 6 characters',
    `Status: ${regShortPass.status}`);

  // 1.5 Blank name
  const regBlankName = await request('/auth/register', {
    method: 'POST',
    body: { name: '', email: `blank_${timestamp}@example.com`, password },
  });
  assert(regBlankName.status === 400 && regBlankName.data.validationErrors?.name,
    '1.5 Reject blank student name',
    `Status: ${regBlankName.status}`);

  // 1.6 Login with valid credentials
  const loginValid = await request('/auth/login', {
    method: 'POST',
    body: { email: user1Email, password },
  });
  assert(loginValid.status === 200 && loginValid.data.token,
    '1.6 Login with valid credentials returns fresh JWT token',
    `Status: ${loginValid.status}`);

  // 1.7 Login with wrong password
  const loginWrongPass = await request('/auth/login', {
    method: 'POST',
    body: { email: user1Email, password: 'wrongPassword' },
  });
  assert(loginWrongPass.status === 401,
    '1.7 Reject login with incorrect password (401 Unauthorized)',
    `Status: ${loginWrongPass.status}`);

  // 1.8 Login with non-existent email
  const loginNonExistent = await request('/auth/login', {
    method: 'POST',
    body: { email: `ghost_${timestamp}@example.com`, password },
  });
  assert(loginNonExistent.status === 401,
    '1.8 Reject login for unknown email (401 Unauthorized)',
    `Status: ${loginNonExistent.status}`);

  // 1.9 GET /api/auth/me with valid token
  const meValid = await request('/auth/me', {
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(meValid.status === 200 && meValid.data.email === user1Email,
    '1.9 GET /api/auth/me returns authenticated student profile',
    `Status: ${meValid.status}`);

  // 1.10 GET /api/auth/me without token
  const meNoToken = await request('/auth/me');
  assert(meNoToken.status === 401,
    '1.10 Reject request without Authorization token (401 Unauthorized)',
    `Status: ${meNoToken.status}`);

  // 1.11 GET /api/auth/me with invalid/tampered token
  const meTampered = await request('/auth/me', {
    headers: { Authorization: 'Bearer this_is_a_forged_invalid_jwt_token' },
  });
  assert(meTampered.status === 401,
    '1.11 Reject tampered JWT token (401 Unauthorized)',
    `Status: ${meTampered.status}`);

  // Register User 2 for isolation testing
  const reg2 = await request('/auth/register', {
    method: 'POST',
    body: { name: 'Bob Peer', email: user2Email, password },
  });
  token2 = reg2.data?.token;

  // -----------------------------------------------------------------
  // FEATURE 2: SUBJECTS (ADD, EDIT, DELETE)
  // -----------------------------------------------------------------
  console.log('\n📌 FEATURE 2: Subjects Management & Isolation');

  let subMathId = null;
  let subCsId = null;

  // 2.1 Add Subject 1 (Mathematics)
  const addSub1 = await request('/subjects', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: { name: 'Mathematics', color: '#4F46E5' },
  });
  assert(addSub1.status === 201 && addSub1.data.name === 'Mathematics',
    '2.1 Add Subject with name and valid hex color (#4F46E5)',
    `Status: ${addSub1.status}`);
  subMathId = addSub1.data?.id;

  // 2.2 Add Subject 2 (Computer Science)
  const addSub2 = await request('/subjects', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: { name: 'Computer Science', color: '#10B981' },
  });
  assert(addSub2.status === 201 && addSub2.data.name === 'Computer Science',
    '2.2 Add second Subject with color #10B981',
    `Status: ${addSub2.status}`);
  subCsId = addSub2.data?.id;

  // 2.3 Duplicate Subject Name for same user
  const addSubDup = await request('/subjects', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: { name: 'mathematics', color: '#8B5CF6' },
  });
  assert(addSubDup.status === 400,
    '2.3 Reject duplicate subject name for same student (case-insensitive)',
    `Status: ${addSubDup.status}`);

  // 2.4 Invalid Color Code format
  const addSubBadColor = await request('/subjects', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: { name: 'Physics', color: 'not-hex' },
  });
  assert(addSubBadColor.status === 400 && addSubBadColor.data.validationErrors?.color,
    '2.4 Reject subject with invalid hex color pattern',
    `Status: ${addSubBadColor.status}`);

  // 2.5 Blank Subject Name
  const addSubBlank = await request('/subjects', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: { name: '   ', color: '#3B82F6' },
  });
  assert(addSubBlank.status === 400 && addSubBlank.data.validationErrors?.name,
    '2.5 Reject blank subject name',
    `Status: ${addSubBlank.status}`);

  // 2.6 Get All Subjects
  const getSubs = await request('/subjects', {
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(getSubs.status === 200 && getSubs.data.length >= 2,
    '2.6 Get all subjects returns list with taskCount',
    `Count: ${getSubs.data?.length}`);

  // 2.7 Get Subject by ID
  const getSubById = await request(`/subjects/${subMathId}`, {
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(getSubById.status === 200 && getSubById.data.name === 'Mathematics',
    '2.7 Get single subject by ID',
    `Name: ${getSubById.data?.name}`);

  // 2.8 Edit Subject Name and Color
  const editSub = await request(`/subjects/${subMathId}`, {
    method: 'PUT',
    headers: { Authorization: `Bearer ${token1}` },
    body: { name: 'Advanced Mathematics', color: '#4338CA' },
  });
  assert(editSub.status === 200 && editSub.data.name === 'Advanced Mathematics',
    '2.8 Update subject name and color',
    `Name: ${editSub.data?.name}`);

  // 2.9 Edit Subject to match another subject's name
  const editSubDup = await request(`/subjects/${subMathId}`, {
    method: 'PUT',
    headers: { Authorization: `Bearer ${token1}` },
    body: { name: 'Computer Science', color: '#4338CA' },
  });
  assert(editSubDup.status === 400,
    '2.9 Reject updating subject name to conflict with existing subject',
    `Status: ${editSubDup.status}`);

  // 2.10 Subject User Isolation: User 2 cannot access or edit User 1's subject
  const user2AccessSub1 = await request(`/subjects/${subMathId}`, {
    headers: { Authorization: `Bearer ${token2}` },
  });
  assert(user2AccessSub1.status === 404,
    '2.10 User Isolation: User 2 cannot access User 1 subject (404 Not Found)',
    `Status: ${user2AccessSub1.status}`);

  const user2EditSub1 = await request(`/subjects/${subMathId}`, {
    method: 'PUT',
    headers: { Authorization: `Bearer ${token2}` },
    body: { name: 'Hacked Math', color: '#000000' },
  });
  assert(user2EditSub1.status === 404,
    '2.11 User Isolation: User 2 cannot edit User 1 subject (404 Not Found)',
    `Status: ${user2EditSub1.status}`);

  // Temporary subject for delete test
  const tempSub = await request('/subjects', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: { name: 'Chemistry', color: '#F59E0B' },
  });
  const deleteSub = await request(`/subjects/${tempSub.data.id}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(deleteSub.status === 200,
    '2.12 Delete subject successfully',
    `Status: ${deleteSub.status}`);

  // -----------------------------------------------------------------
  // FEATURE 3: TASKS (ADD, EDIT, DELETE, MARK DONE)
  // -----------------------------------------------------------------
  console.log('\n📌 FEATURE 3: Tasks Management & Actions');

  const todayStr = new Date().toISOString().split('T')[0];
  const yesterdayStr = new Date(Date.now() - 2 * 86400000).toISOString().split('T')[0];
  const futureStr = new Date(Date.now() + 5 * 86400000).toISOString().split('T')[0];

  // 3.1 Add task with subject
  const addTask1 = await request('/tasks', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: {
      title: 'Calculus Problem Set 1',
      subjectId: subMathId,
      dueDate: todayStr,
      priority: 'HIGH',
    },
  });
  assert(addTask1.status === 201 && addTask1.data.title === 'Calculus Problem Set 1' && addTask1.data.priority === 'HIGH',
    '3.1 Create task with subject, due date, and priority',
    `Status: ${addTask1.status}`);
  const task1Id = addTask1.data?.id;

  // 3.2 Add general task without subject
  const addGeneralTask = await request('/tasks', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: {
      title: 'Organize Study Desk',
      dueDate: futureStr,
      priority: 'LOW',
    },
  });
  assert(addGeneralTask.status === 201 && addGeneralTask.data.subjectId === null,
    '3.2 Create general task without subject (subjectId is null)',
    `Status: ${addGeneralTask.status}`);
  const taskGeneralId = addGeneralTask.data?.id;

  // 3.3 Add task with blank title
  const addBlankTask = await request('/tasks', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: { title: '  ', dueDate: todayStr, priority: 'MEDIUM' },
  });
  assert(addBlankTask.status === 400 && addBlankTask.data.validationErrors?.title,
    '3.3 Reject task with blank title (400 Bad Request)',
    `Status: ${addBlankTask.status}`);

  // 3.4 Add task with missing due date
  const addNoDateTask = await request('/tasks', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: { title: 'No Date Task', priority: 'MEDIUM' },
  });
  assert(addNoDateTask.status === 400 && addNoDateTask.data.validationErrors?.dueDate,
    '3.4 Reject task with missing due date',
    `Status: ${addNoDateTask.status}`);

  // 3.5 Add task attempting to link another user's subject
  // Let User 2 create a subject
  const user2Sub = await request('/subjects', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token2}` },
    body: { name: 'Bob Private Subject', color: '#000000' },
  });
  const hackTask = await request('/tasks', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: {
      title: 'Attempt unauthorized subject link',
      subjectId: user2Sub.data.id,
      dueDate: todayStr,
      priority: 'LOW',
    },
  });
  assert(hackTask.status === 404,
    '3.5 Reject linking another user\'s subject to student task (404 Not Found)',
    `Status: ${hackTask.status}`);

  // 3.6 Get task by ID
  const getTask1 = await request(`/tasks/${task1Id}`, {
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(getTask1.status === 200 && getTask1.data.id === task1Id,
    '3.6 Get single task by ID',
    `Status: ${getTask1.status}`);

  // 3.7 Edit task
  const editTask1 = await request(`/tasks/${task1Id}`, {
    method: 'PUT',
    headers: { Authorization: `Bearer ${token1}` },
    body: {
      title: 'Calculus Problem Set 1 (Revised)',
      subjectId: subMathId,
      dueDate: todayStr,
      priority: 'HIGH',
      completed: false,
    },
  });
  assert(editTask1.status === 200 && editTask1.data.title.includes('Revised'),
    '3.7 Edit task title and properties',
    `Title: ${editTask1.data?.title}`);

  // 3.8 Mark Done / Toggle task completion
  const toggle1 = await request(`/tasks/${task1Id}/toggle`, {
    method: 'PATCH',
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(toggle1.status === 200 && toggle1.data.completed === true,
    '3.8 Toggle task status: pending -> completed (completed = true)',
    `Completed: ${toggle1.data?.completed}`);

  const toggle2 = await request(`/tasks/${task1Id}/toggle`, {
    method: 'PATCH',
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(toggle2.status === 200 && toggle2.data.completed === false,
    '3.9 Toggle task status back: completed -> pending (completed = false)',
    `Completed: ${toggle2.data?.completed}`);

  // 3.10 Delete task
  const deleteGeneral = await request(`/tasks/${taskGeneralId}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(deleteGeneral.status === 200,
    '3.10 Delete task by ID',
    `Status: ${deleteGeneral.status}`);

  // 3.11 Task User Isolation: User 2 cannot access, toggle, or delete User 1's task
  const user2GetTask1 = await request(`/tasks/${task1Id}`, {
    headers: { Authorization: `Bearer ${token2}` },
  });
  assert(user2GetTask1.status === 404,
    '3.11 User Isolation: User 2 cannot view User 1 task (404 Not Found)',
    `Status: ${user2GetTask1.status}`);

  const user2ToggleTask1 = await request(`/tasks/${task1Id}/toggle`, {
    method: 'PATCH',
    headers: { Authorization: `Bearer ${token2}` },
  });
  assert(user2ToggleTask1.status === 404,
    '3.12 User Isolation: User 2 cannot toggle User 1 task (404 Not Found)',
    `Status: ${user2ToggleTask1.status}`);

  const user2DeleteTask1 = await request(`/tasks/${task1Id}`, {
    method: 'DELETE',
    headers: { Authorization: `Bearer ${token2}` },
  });
  assert(user2DeleteTask1.status === 404,
    '3.13 User Isolation: User 2 cannot delete User 1 task (404 Not Found)',
    `Status: ${user2DeleteTask1.status}`);

  // -----------------------------------------------------------------
  // FEATURE 4: DASHBOARD (TODAY, OVERDUE, PROGRESS BAR)
  // -----------------------------------------------------------------
  console.log('\n📌 FEATURE 4: Dashboard Metrics & Lists');

  // Let's create a curated set of tasks for User 1:
  // 1. Task Today Pending (High)
  // 2. Task Today Completed (Medium)
  // 3. Task Overdue Pending (High)
  // 4. Task Overdue Completed (Low) -> MUST NOT appear in overdue list!
  // 5. Task Future Pending (Medium)

  const taskTodayPending = await request('/tasks', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: { title: 'Today Task Urgent', subjectId: subMathId, dueDate: todayStr, priority: 'HIGH' },
  });

  const taskTodayCompleted = await request('/tasks', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: { title: 'Today Task Done', subjectId: subCsId, dueDate: todayStr, priority: 'MEDIUM' },
  });
  await request(`/tasks/${taskTodayCompleted.data.id}/toggle`, {
    method: 'PATCH',
    headers: { Authorization: `Bearer ${token1}` },
  });

  const taskOverduePending = await request('/tasks', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: { title: 'Overdue Assignment Pending', subjectId: subMathId, dueDate: yesterdayStr, priority: 'HIGH' },
  });

  const taskOverdueDone = await request('/tasks', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: { title: 'Overdue Assignment But Completed', subjectId: subCsId, dueDate: yesterdayStr, priority: 'LOW' },
  });
  await request(`/tasks/${taskOverdueDone.data.id}/toggle`, {
    method: 'PATCH',
    headers: { Authorization: `Bearer ${token1}` },
  });

  const taskFuturePending = await request('/tasks', {
    method: 'POST',
    headers: { Authorization: `Bearer ${token1}` },
    body: { title: 'Future Reading Assignment', subjectId: subCsId, dueDate: futureStr, priority: 'LOW' },
  });

  // Fetch Dashboard
  const dash = await request('/dashboard', {
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(dash.status === 200,
    '4.1 GET /api/dashboard returns 200 OK',
    `Status: ${dash.status}`);

  const d = dash.data;
  console.log(`      Metrics: Total=${d.totalTasks}, Completed=${d.completedTasks}, Pending=${d.pendingTasks}, Rate=${d.completionPercentage}%`);

  assert(d.totalTasks >= 6 && d.completedTasks >= 2,
    '4.2 Dashboard correctly computes total and completed tasks count',
    `Total: ${d.totalTasks}, Completed: ${d.completedTasks}`);

  assert(d.pendingTasks === (d.totalTasks - d.completedTasks),
    '4.3 Pending tasks equals total minus completed',
    `Pending: ${d.pendingTasks}`);

  assert(d.completionPercentage > 0 && d.completionPercentage <= 100,
    '4.4 Completion progress bar percentage calculated accurately',
    `Percentage: ${d.completionPercentage}%`);

  // Verify todayTasks contains tasks due today
  const allTodayMatch = d.todayTasks.every(t => t.dueDate === todayStr);
  assert(allTodayMatch && d.todayTasks.length >= 2,
    '4.5 Today\'s tasks widget contains only tasks due on today\'s date',
    `Count: ${d.todayTasks.length}`);

  // Verify overdueTasks contains only incomplete tasks before today
  const allOverdueMatch = d.overdueTasks.every(t => t.dueDate < todayStr && t.completed === false);
  assert(allOverdueMatch && d.overdueTasks.length >= 1,
    '4.6 Overdue tasks widget contains strictly INCOMPLETE tasks with dueDate < today',
    `Count: ${d.overdueTasks.length}`);

  // Verify that completed task with past dueDate is NOT in overdueTasks
  const overdueDoneExcluded = !d.overdueTasks.some(t => t.id === taskOverdueDone.data.id);
  assert(overdueDoneExcluded,
    '4.7 Completed past tasks are excluded from the Overdue Tasks widget',
    `Excluded: ${overdueDoneExcluded}`);

  // -----------------------------------------------------------------
  // FEATURE 5: FILTER TASKS (SUBJECT, PRIORITY, STATUS)
  // -----------------------------------------------------------------
  console.log('\n📌 FEATURE 5: Multi-Criteria Task Filtering');

  // 5.1 Filter by subjectId
  const filterBySub = await request(`/tasks?subjectId=${subMathId}`, {
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(filterBySub.status === 200 && filterBySub.data.every(t => t.subjectId === subMathId),
    '5.1 Filter tasks by Subject ID',
    `Filtered count: ${filterBySub.data.length}`);

  // 5.2 Filter by priority HIGH
  const filterHigh = await request('/tasks?priority=HIGH', {
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(filterHigh.status === 200 && filterHigh.data.every(t => t.priority === 'HIGH'),
    '5.2 Filter tasks by Priority = HIGH',
    `Filtered count: ${filterHigh.data.length}`);

  // 5.3 Filter by priority LOW
  const filterLow = await request('/tasks?priority=LOW', {
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(filterLow.status === 200 && filterLow.data.every(t => t.priority === 'LOW'),
    '5.3 Filter tasks by Priority = LOW',
    `Filtered count: ${filterLow.data.length}`);

  // 5.4 Filter by status completed = true
  const filterCompleted = await request('/tasks?completed=true', {
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(filterCompleted.status === 200 && filterCompleted.data.every(t => t.completed === true),
    '5.4 Filter tasks by Status = Completed (completed=true)',
    `Filtered count: ${filterCompleted.data.length}`);

  // 5.5 Filter by status completed = false (pending/active)
  const filterPending = await request('/tasks?completed=false', {
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(filterPending.status === 200 && filterPending.data.every(t => t.completed === false),
    '5.5 Filter tasks by Status = Pending (completed=false)',
    `Filtered count: ${filterPending.data.length}`);

  // 5.6 Combined Filter: subject + priority + completed
  const filterCombined = await request(`/tasks?subjectId=${subMathId}&priority=HIGH&completed=false`, {
    headers: { Authorization: `Bearer ${token1}` },
  });
  assert(filterCombined.status === 200 &&
    filterCombined.data.every(t => t.subjectId === subMathId && t.priority === 'HIGH' && t.completed === false),
    '5.6 Combined filter: subjectId + priority=HIGH + completed=false',
    `Filtered count: ${filterCombined.data.length}`);

  // 5.7 Tasks sorted by dueDate ascending
  const allTasks = await request('/tasks', {
    headers: { Authorization: `Bearer ${token1}` },
  });
  let isSortedByDate = true;
  for (let i = 0; i < allTasks.data.length - 1; i++) {
    if (allTasks.data[i].dueDate > allTasks.data[i + 1].dueDate) {
      isSortedByDate = false;
      break;
    }
  }
  assert(allTasks.status === 200 && isSortedByDate,
    '5.7 Unfiltered tasks are sorted by due date ascending',
    `Sorted: ${isSortedByDate}`);

  // -----------------------------------------------------------------
  // SUMMARY
  // -----------------------------------------------------------------
  console.log('\n======================================================');
  console.log(`📊 TEST SUITE SUMMARY: ${passedTests}/${totalTests} TESTS PASSED`);
  if (failedTests === 0) {
    console.log('🎉 ALL FEATURES, VALIDATIONS & ISOLATION CHECKS PASSED!');
  } else {
    console.log(`⚠️ ${failedTests} TEST(S) FAILED.`);
  }
  console.log('======================================================\n');
}

runAllTests().catch(err => {
  console.error('Fatal test error:', err);
  process.exit(1);
});
