<template>
  <div class="profile">
    <Sidebar />

    <main class="profile__content">
      <!-- ==================== 用户资料头部（紧凑浅色） ==================== -->
      <header class="profile__hero">
        <div class="profile__avatar" aria-hidden="true">{{ avatarText }}</div>

        <div class="profile__identity">
          <h1 class="profile__name">{{ authStore.userNickname }}</h1>
          <div class="profile__meta">
            <span class="profile__email">{{ authStore.user?.email || '—' }}</span>
            <span class="profile__sep" aria-hidden="true">·</span>
            <span class="profile__role">{{ authStore.isGuest ? '游客账号' : '注册用户' }}</span>
            <span class="profile__sep" aria-hidden="true">·</span>
            <span class="profile__verify" :class="{ 'profile__verify--ok': isEmailVerified }">
              {{ isEmailVerified ? '邮箱已验证' : '邮箱未验证' }}
            </span>
          </div>
        </div>

        <button class="profile__hero-btn" @click="openEditProfile">编辑昵称</button>
      </header>

      <div class="profile__body">
        <!-- ==================== 左侧锚点导航 ==================== -->
        <aside class="profile__nav" aria-label="个人中心导航">
          <button
            v-for="s in SECTIONS"
            :key="s.id"
            class="profile__nav-item"
            :class="{ 'profile__nav-item--active': activeSection === s.id }"
            @click="goSection(s.id)"
          >
            {{ s.label }}
          </button>
        </aside>

        <!-- ==================== 右侧内容区 ==================== -->
        <div class="profile__panels">
          <!-- ---------- 账号安全 ---------- -->
          <section id="security" class="panel">
            <h2 class="panel__title">账号安全</h2>

            <button class="row" @click="openPasswordModal">
              <span class="row__icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="4" y="11" width="16" height="9" rx="2" />
                  <path d="M8 11V7a4 4 0 0 1 8 0v4" />
                </svg>
              </span>
              <span class="row__text">
                <span class="row__label">修改密码</span>
                <span class="row__desc">定期更新登录密码，保护账号安全</span>
              </span>
              <span class="row__arrow" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 6 6 6-6 6" /></svg>
              </span>
            </button>

            <button class="row" @click="openEmailModal">
              <span class="row__icon" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="5" width="18" height="14" rx="2" />
                  <path d="m3 7 9 6 9-6" />
                </svg>
              </span>
              <span class="row__text">
                <span class="row__label">{{ isEmailVerified ? '换绑邮箱' : '绑定邮箱' }}</span>
                <span class="row__desc">{{ isEmailVerified ? emailDisplay : '绑定邮箱用于接收验证码与找回密码' }}</span>
              </span>
              <span class="row__value">{{ isEmailVerified ? emailDisplay : '未绑定' }}</span>
              <span class="row__arrow" aria-hidden="true">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m9 6 6 6-6 6" /></svg>
              </span>
            </button>
          </section>

          <!-- ---------- 使用统计 ---------- -->
          <section id="stats" class="panel">
            <div class="panel__head">
              <h2 class="panel__title">使用统计</h2>
              <button class="btn btn--ghost" :disabled="statsLoading" @click="loadStats">
                {{ statsLoading ? '加载中…' : '刷新' }}
              </button>
            </div>

            <p v-if="statsError" class="panel__error">{{ statsError }}</p>

            <div class="stats-grid">
              <div class="stat">
                <span class="stat__icon stat__icon--brand" aria-hidden="true">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2" />
                    <rect x="9" y="3" width="6" height="4" rx="1" />
                    <path d="M9 12h6M9 16h4" />
                  </svg>
                </span>
                <div class="stat__text">
                  <span class="stat__value">{{ statsLoading ? '—' : stats.totalTasks }}</span>
                  <span class="stat__label">总任务数</span>
                </div>
              </div>

              <div class="stat">
                <span class="stat__icon stat__icon--success" aria-hidden="true">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <circle cx="12" cy="12" r="9" />
                    <path d="m9 12 2 2 4-4" />
                  </svg>
                </span>
                <div class="stat__text">
                  <span class="stat__value">{{ statsLoading ? '—' : stats.completedTasks }}</span>
                  <span class="stat__label">已完成任务</span>
                </div>
              </div>

              <div class="stat">
                <span class="stat__icon stat__icon--info" aria-hidden="true">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M5 20V10" />
                    <path d="M12 20V4" />
                    <path d="M19 20v-6" />
                  </svg>
                </span>
                <div class="stat__text">
                  <span class="stat__value">{{ statsLoading ? '—' : stats.totalResults }}</span>
                  <span class="stat__label">预测结果</span>
                </div>
              </div>

              <div class="stat">
                <span class="stat__icon stat__icon--warning" aria-hidden="true">
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round">
                    <path d="M12 3l2.7 5.6 6.1.9-4.4 4.3 1 6.1-5.4-2.9-5.4 2.9 1-6.1L3.2 9.5l6.1-.9L12 3z" />
                  </svg>
                </span>
                <div class="stat__text">
                  <span class="stat__value">{{ statsLoading ? '—' : stats.totalFavorites }}</span>
                  <span class="stat__label">我的收藏</span>
                </div>
              </div>
            </div>
          </section>

          <!-- ---------- 科研档案 ---------- -->
          <section id="research" class="panel">
            <h2 class="panel__title">科研档案</h2>
            <p class="panel__hint">
              用于结果报告署名与课题组统计，保存后立即生效，留空即清除。
            </p>

            <div class="form-grid">
              <div class="field">
                <label class="field__label" for="research-institution">所属机构</label>
                <input
                  id="research-institution"
                  v-model="research.institution"
                  class="field__input"
                  type="text"
                  placeholder="如：中国科学院上海药物研究所"
                />
              </div>

              <div class="field">
                <label class="field__label" for="research-lab">实验室 / 课题组</label>
                <input
                  id="research-lab"
                  v-model="research.lab"
                  class="field__input"
                  type="text"
                  placeholder="如：药物设计实验室"
                />
              </div>

              <div class="field">
                <label class="field__label" for="research-orcid">ORCID</label>
                <input
                  id="research-orcid"
                  v-model="research.orcid"
                  class="field__input"
                  type="text"
                  placeholder="0000-0002-1825-0097"
                />
                <span class="field__tip">格式 0000-0002-1825-0097，服务端会校验最后一位校验码</span>
              </div>

              <div class="field">
                <label class="field__label" for="research-area">研究方向</label>
                <input
                  id="research-area"
                  v-model="research.researchArea"
                  class="field__input"
                  type="text"
                  placeholder="如：药物-靶点相互作用预测"
                />
              </div>
            </div>

            <p v-if="researchError" class="panel__error">{{ researchError }}</p>

            <div class="panel__actions">
              <button class="btn btn--primary" :disabled="researchLoading" @click="saveResearch">
                {{ researchLoading ? '保存中…' : '保存科研档案' }}
              </button>
            </div>
          </section>

          <!-- ---------- 登录记录 ---------- -->
          <section id="logs" class="panel">
            <div class="panel__head">
              <h2 class="panel__title">登录记录</h2>
              <button class="btn btn--ghost" :disabled="logsLoading" @click="loadLoginLogs">
                {{ logsLoading ? '加载中…' : '刷新' }}
              </button>
            </div>
            <p class="panel__hint">最多展示最近 {{ LOG_LIMIT }} 条，仅记录不提供"下线其他设备"。</p>

            <p v-if="logsError" class="panel__error">{{ logsError }}</p>
            <p v-else-if="logsLoading && !loginLogs.length" class="panel__hint">加载中…</p>
            <p v-else-if="!loginLogs.length" class="panel__hint">暂无登录记录。</p>

            <ul v-else class="log-list">
              <li v-for="(log, index) in loginLogs" :key="index" class="log">
                <div class="log__main">
                  <StatusTag :value="log.success ? 'success' : 'failed'" />
                  <span class="log__ip">{{ log.loginIp || '—' }}</span>
                  <span class="log__loc">{{ log.loginLocation || '位置未知' }}</span>
                </div>
                <div class="log__meta">
                  <span>{{ log.loginType || '—' }}</span>
                  <span>{{ describeAgent(log.userAgent) }}</span>
                  <time>{{ formatTime(log.createdAt) }}</time>
                </div>
                <p v-if="!log.success && log.failReason" class="log__fail">
                  失败原因：{{ log.failReason }}
                </p>
              </li>
            </ul>
          </section>

          <!-- ---------- 危险操作 ---------- -->
          <section id="danger" class="panel panel--danger">
            <h2 class="panel__title panel__title--danger">危险操作</h2>
            <p class="panel__hint">删除账户会永久清除全部任务、预测结果与收藏，且无法恢复。</p>
            <div class="panel__actions">
              <button class="btn btn--danger" @click="openDeleteModal">删除账户</button>
            </div>
          </section>
        </div>
      </div>
    </main>

    <!-- ==================== 成功提示 ==================== -->
    <div v-if="successMessage" class="profile__toast">{{ successMessage }}</div>

    <!-- ==================== 编辑昵称 ==================== -->
    <div v-if="showEditModal" class="pmodal" @click.self="closeEditModal">
      <div class="pmodal__dialog">
        <h3 class="pmodal__title">编辑昵称</h3>
        <div class="pmodal__field">
          <label class="pmodal__label">昵称</label>
          <input
            v-model="editNickname"
            class="pmodal__input"
            type="text"
            placeholder="请输入昵称"
            @keyup.enter="saveProfile"
          />
        </div>
        <div v-if="editError" class="pmodal__error">{{ editError }}</div>
        <div class="pmodal__actions">
          <button class="pmodal__btn pmodal__btn--ghost" @click="closeEditModal">取消</button>
          <button
            class="pmodal__btn pmodal__btn--primary"
            :disabled="editLoading"
            @click="saveProfile"
          >
            {{ editLoading ? '保存中…' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ==================== 修改密码 ==================== -->
    <div v-if="showPasswordModal" class="pmodal" @click.self="closePasswordModal">
      <div class="pmodal__dialog">
        <h3 class="pmodal__title">修改密码</h3>
        <div class="pmodal__field">
          <label class="pmodal__label">原密码</label>
          <input v-model="oldPassword" class="pmodal__input" type="password" placeholder="请输入原密码" />
        </div>
        <div class="pmodal__field">
          <label class="pmodal__label">新密码</label>
          <input v-model="newPassword" class="pmodal__input" type="password" placeholder="请输入新密码" />
        </div>
        <div class="pmodal__field">
          <label class="pmodal__label">确认新密码</label>
          <input
            v-model="confirmPassword"
            class="pmodal__input"
            type="password"
            placeholder="请再次输入新密码"
            @keyup.enter="submitPassword"
          />
        </div>
        <div v-if="passwordError" class="pmodal__error">{{ passwordError }}</div>
        <div class="pmodal__actions">
          <button class="pmodal__btn pmodal__btn--ghost" @click="closePasswordModal">取消</button>
          <button
            class="pmodal__btn pmodal__btn--primary"
            :disabled="passwordLoading"
            @click="submitPassword"
          >
            {{ passwordLoading ? '提交中…' : '确认修改' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ==================== 绑定 / 换绑邮箱 ==================== -->
    <div v-if="showEmailModal" class="pmodal" @click.self="closeEmailModal">
      <div class="pmodal__dialog">
        <h3 class="pmodal__title">{{ isEmailVerified ? '换绑邮箱' : '绑定邮箱' }}</h3>

        <div class="pmodal__field">
          <label class="pmodal__label">新邮箱</label>
          <input
            v-model="emailForm.email"
            class="pmodal__input"
            type="email"
            placeholder="12345678@qq.com"
          />
          <span class="pmodal__tip">服务端当前只放行 QQ 邮箱，其他域名会被拒绝</span>
        </div>

        <div v-if="isEmailVerified" class="pmodal__field">
          <label class="pmodal__label">当前密码</label>
          <input
            v-model="emailForm.password"
            class="pmodal__input"
            type="password"
            placeholder="换绑需验证当前密码"
          />
        </div>

        <div class="pmodal__field">
          <label class="pmodal__label">邮箱验证码</label>
          <div class="pmodal__code-row">
            <input
              v-model="emailForm.code"
              class="pmodal__input"
              type="text"
              placeholder="6 位数字"
              maxlength="6"
            />
            <button
              class="pmodal__btn pmodal__btn--ghost"
              :disabled="sendingCode || countdown > 0"
              @click="sendEmailCode"
            >
              {{ countdown > 0 ? `${countdown} 秒` : sendingCode ? '发送中…' : '发送验证码' }}
            </button>
          </div>
        </div>

        <p v-if="emailDevCode" class="pmodal__tip pmodal__tip--dev">
          开发模式：验证码 {{ emailDevCode }} 已自动填入
        </p>
        <div v-if="emailError" class="pmodal__error">{{ emailError }}</div>

        <div class="pmodal__actions">
          <button class="pmodal__btn pmodal__btn--ghost" @click="closeEmailModal">取消</button>
          <button
            class="pmodal__btn pmodal__btn--primary"
            :disabled="emailLoading"
            @click="submitEmail"
          >
            {{ emailLoading ? '提交中…' : isEmailVerified ? '确认换绑' : '确认绑定' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ==================== 删除账户 ==================== -->
    <div v-if="showDeleteModal" class="pmodal" @click.self="closeDeleteModal">
      <div class="pmodal__dialog">
        <h3 class="pmodal__title pmodal__title--danger">删除账户</h3>
        <p class="pmodal__warning">
          删除账户后所有数据将被永久清除且无法恢复，请谨慎操作。
        </p>
        <div class="pmodal__field">
          <label class="pmodal__label">当前密码</label>
          <input
            v-model="deletePassword"
            class="pmodal__input"
            type="password"
            placeholder="请输入当前密码以确认删除"
            @keyup.enter="confirmDeleteAccount"
          />
        </div>
        <div v-if="deleteError" class="pmodal__error">{{ deleteError }}</div>
        <div class="pmodal__actions">
          <button class="pmodal__btn pmodal__btn--ghost" @click="closeDeleteModal">取消</button>
          <button
            class="pmodal__btn pmodal__btn--danger"
            :disabled="deleteLoading"
            @click="confirmDeleteAccount"
          >
            {{ deleteLoading ? '删除中…' : '确认删除' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { taskApi, resultApi } from '@/api/predict'
import { favoriteApi } from '@/api/favorite'
import { authApi, type LoginLogItem, type UserDTO } from '@/api/auth'
import Sidebar from '@/components/Sidebar.vue'
import StatusTag from '@/components/ui/StatusTag.vue'
import type { Task } from '@/types'

const authStore = useAuthStore()
const router = useRouter()

/** 左侧锚点导航。id 必须与 template 中 section 的 id 一致 */
const SECTIONS = [
  { id: 'security', label: '账号安全' },
  { id: 'stats', label: '使用统计' },
  { id: 'research', label: '科研档案' },
  { id: 'logs', label: '登录记录' },
  { id: 'danger', label: '危险操作' }
] as const

/** 登录记录拉取条数上限，需与后端 MAX_LOGIN_LOG_LIMIT 保持一致 */
const LOG_LIMIT = 20

/** 锚点滚动时的顶部留白，需略大于固定头部高度 */
const SCROLL_OFFSET = 80

// ==================== 完整档案 ====================
// store.user 经 normalizeUser 裁剪过（只剩 id/email/nickname/avatar/createdAt），
// emailVerified 与科研档案字段只能从完整档案取。
const fullProfile = ref<UserDTO | null>(null)

const isEmailVerified = computed(() => fullProfile.value?.emailVerified === 1)

const emailDisplay = computed(() =>
  isEmailVerified.value ? fullProfile.value?.email ?? '—' : '未绑定'
)

const avatarText = computed(() => {
  if (!authStore.userNickname) return 'S'
  return authStore.userNickname.charAt(0).toUpperCase()
})

const loadFullProfile = async () => {
  try {
    fullProfile.value = await authApi.getFullProfile()
    syncResearchForm()
  } catch (error) {
    // 不打断页面：横幅仍可回退到 store 中的缓存昵称
    console.error('加载完整档案失败', error)
  }
}

// ==================== 使用统计 ====================
const tasks = ref<Task[]>([])
const resultsTotal = ref(0)
const favoritesTotal = ref(0)
const statsLoading = ref(false)
const statsError = ref('')

const stats = computed(() => ({
  totalTasks: tasks.value.length,
  completedTasks: tasks.value.filter((t) => t.status === 'completed').length,
  totalResults: resultsTotal.value,
  totalFavorites: favoritesTotal.value
}))

const loadStats = async () => {
  statsLoading.value = true
  statsError.value = ''

  // 用 allSettled 而非 all：任一项接口失败（如收藏为空/无权限）不应让整块统计变成报错
  const [taskRes, resultRes, favoriteRes] = await Promise.allSettled([
    taskApi.getTaskList(),
    resultApi.getResultList(1, 1),
    favoriteApi.getFavoriteList(1, 1)
  ])

  if (taskRes.status === 'fulfilled') {
    tasks.value = taskRes.value as unknown as Task[]
  }
  if (resultRes.status === 'fulfilled') {
    resultsTotal.value = resultRes.value.total
  }
  if (favoriteRes.status === 'fulfilled') {
    const page = favoriteRes.value
    favoritesTotal.value = page.total ?? page.list?.length ?? 0
  }

  // 部分失败时明确告知，避免"某个接口挂了却显示 0"被误读为真实数据
  const failed: string[] = []
  if (taskRes.status === 'rejected') failed.push('任务')
  if (resultRes.status === 'rejected') failed.push('结果')
  if (favoriteRes.status === 'rejected') failed.push('收藏')
  if (failed.length) {
    statsError.value = `${failed.join('、')}数据加载失败，当前数字可能不完整`
  }

  statsLoading.value = false
}

// ==================== 科研档案 ====================
const research = ref({
  institution: '',
  lab: '',
  orcid: '',
  researchArea: ''
})
const researchLoading = ref(false)
const researchError = ref('')

const syncResearchForm = () => {
  research.value = {
    institution: fullProfile.value?.institution ?? '',
    lab: fullProfile.value?.lab ?? '',
    orcid: fullProfile.value?.orcid ?? '',
    researchArea: fullProfile.value?.researchArea ?? ''
  }
}

const saveResearch = async () => {
  researchLoading.value = true
  researchError.value = ''
  try {
    // 后端按"一组整体覆盖"处理，空串即清除，因此这里原样提交不做过滤
    const updated = await authApi.updateResearchProfile({ ...research.value })
    fullProfile.value = updated
    syncResearchForm()
    showSuccess('科研档案已保存')
  } catch (error) {
    researchError.value = error instanceof Error ? error.message : '保存科研档案失败'
  } finally {
    researchLoading.value = false
  }
}

// ==================== 登录记录 ====================
const loginLogs = ref<LoginLogItem[]>([])
const logsLoading = ref(false)
const logsError = ref('')

const loadLoginLogs = async () => {
  logsLoading.value = true
  logsError.value = ''
  try {
    loginLogs.value = await authApi.getLoginLogs(LOG_LIMIT)
  } catch (error) {
    logsError.value = error instanceof Error ? error.message : '加载登录记录失败'
  } finally {
    logsLoading.value = false
  }
}

const formatTime = (iso?: string) => {
  if (!iso) return '—'
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return iso
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}`
}

/**
 * 把 User-Agent 串压成可读的客户端名。
 * 只做已知特征匹配，识别不出来时原样返回（截断），不臆造"设备型号"之类信息。
 */
const describeAgent = (ua?: string) => {
  if (!ua) return '—'
  const lower = ua.toLowerCase()
  if (lower.includes('edg/')) return 'Edge'
  if (lower.includes('chrome/')) return 'Chrome'
  if (lower.includes('firefox/')) return 'Firefox'
  if (lower.includes('safari/')) return 'Safari'
  if (lower.includes('curl/')) return 'curl'
  if (lower.includes('postman')) return 'Postman'
  return ua.length > 48 ? `${ua.slice(0, 48)}…` : ua
}

// ==================== 锚点导航 ====================
const activeSection = ref<string>(SECTIONS[0].id)
let observer: IntersectionObserver | null = null

const goSection = (id: string) => {
  activeSection.value = id
  const el = document.getElementById(id)
  if (!el) return
  window.scrollTo({
    top: el.getBoundingClientRect().top + window.scrollY - SCROLL_OFFSET,
    behavior: 'smooth'
  })
}

const setupScrollSpy = () => {
  const elements = SECTIONS.map((s) => document.getElementById(s.id)).filter(
    (el): el is HTMLElement => el !== null
  )
  if (!elements.length || typeof IntersectionObserver === 'undefined') return

  observer = new IntersectionObserver(
    (entries) => {
      const visible = entries
        .filter((entry) => entry.isIntersecting)
        .sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top)
      if (visible.length) {
        activeSection.value = visible[0].target.id
      }
    },
    { rootMargin: `-${SCROLL_OFFSET}px 0px -60% 0px`, threshold: 0 }
  )

  elements.forEach((el) => observer?.observe(el))
}

// ==================== 成功提示 ====================
const successMessage = ref('')
let successTimer: number | null = null

const showSuccess = (message: string) => {
  successMessage.value = message
  if (successTimer) clearTimeout(successTimer)
  successTimer = window.setTimeout(() => {
    successMessage.value = ''
  }, 3000)
}

// ==================== 编辑昵称 ====================
const showEditModal = ref(false)
const editNickname = ref('')
const editLoading = ref(false)
const editError = ref('')

const openEditProfile = () => {
  editNickname.value = authStore.user?.nickname || ''
  editError.value = ''
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
}

const saveProfile = async () => {
  const nickname = editNickname.value.trim()
  if (!nickname) {
    editError.value = '昵称不能为空'
    return
  }
  editLoading.value = true
  editError.value = ''
  try {
    await authApi.updateProfile({ nickname })
    authStore.updateNickname(nickname)
    await loadFullProfile()
    showEditModal.value = false
    showSuccess('昵称已更新')
  } catch (error) {
    editError.value = error instanceof Error ? error.message : '保存失败'
  } finally {
    editLoading.value = false
  }
}

// ==================== 修改密码 ====================
const showPasswordModal = ref(false)
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const passwordLoading = ref(false)
const passwordError = ref('')

const openPasswordModal = () => {
  oldPassword.value = ''
  newPassword.value = ''
  confirmPassword.value = ''
  passwordError.value = ''
  showPasswordModal.value = true
}

const closePasswordModal = () => {
  showPasswordModal.value = false
}

const submitPassword = async () => {
  if (!oldPassword.value) {
    passwordError.value = '请输入原密码'
    return
  }
  if (!newPassword.value) {
    passwordError.value = '请输入新密码'
    return
  }
  if (newPassword.value !== confirmPassword.value) {
    passwordError.value = '两次输入的新密码不一致'
    return
  }
  passwordLoading.value = true
  passwordError.value = ''
  try {
    await authApi.changePassword(oldPassword.value, newPassword.value)
    showPasswordModal.value = false
    showSuccess('密码修改成功')
  } catch (error) {
    passwordError.value = error instanceof Error ? error.message : '修改密码失败'
  } finally {
    passwordLoading.value = false
  }
}

// ==================== 绑定 / 换绑邮箱 ====================
const showEmailModal = ref(false)
const emailForm = ref({ email: '', code: '', password: '' })
const emailLoading = ref(false)
const emailError = ref('')
const emailDevCode = ref('')
const sendingCode = ref(false)
const countdown = ref(0)
let countdownTimer: number | null = null

const startCountdown = () => {
  countdown.value = 60
  if (countdownTimer) clearInterval(countdownTimer)
  countdownTimer = window.setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0) {
      if (countdownTimer) clearInterval(countdownTimer)
      countdownTimer = null
    }
  }, 1000)
}

const openEmailModal = () => {
  emailForm.value = { email: '', code: '', password: '' }
  emailError.value = ''
  emailDevCode.value = ''
  showEmailModal.value = true
}

const closeEmailModal = () => {
  showEmailModal.value = false
}

const sendEmailCode = async () => {
  const email = emailForm.value.email.trim()
  if (!email) {
    emailError.value = '请先填写邮箱'
    return
  }
  sendingCode.value = true
  emailError.value = ''
  emailDevCode.value = ''
  try {
    const res = await authApi.sendCaptcha(
      email,
      isEmailVerified.value ? 'change_email' : 'bind'
    )
    // 后端未配置发件邮箱时走开发模式，直接回显验证码
    if (res.devMode && res.code) {
      emailDevCode.value = res.code
      emailForm.value.code = res.code
    }
    startCountdown()
  } catch (error) {
    emailError.value = error instanceof Error ? error.message : '验证码发送失败'
  } finally {
    sendingCode.value = false
  }
}

const submitEmail = async () => {
  const email = emailForm.value.email.trim()
  const code = emailForm.value.code.trim()
  const password = emailForm.value.password
  const wasVerified = isEmailVerified.value

  if (!email) {
    emailError.value = '请填写邮箱'
    return
  }
  if (!code) {
    emailError.value = '请填写验证码'
    return
  }
  if (wasVerified && !password) {
    emailError.value = '换绑邮箱需输入当前密码'
    return
  }

  emailLoading.value = true
  emailError.value = ''
  try {
    const updated = wasVerified
      ? await authApi.changeEmail(email, code, password)
      : await authApi.bindEmail(email, code)
    fullProfile.value = updated
    showEmailModal.value = false
    // 提示语用提交前的状态判断，提交后 isEmailVerified 已变为 true
    showSuccess(wasVerified ? '邮箱换绑成功' : '邮箱绑定成功')
  } catch (error) {
    emailError.value = error instanceof Error ? error.message : '提交失败'
  } finally {
    emailLoading.value = false
  }
}

// ==================== 删除账户 ====================
const showDeleteModal = ref(false)
const deletePassword = ref('')
const deleteLoading = ref(false)
const deleteError = ref('')

const openDeleteModal = () => {
  deletePassword.value = ''
  deleteError.value = ''
  showDeleteModal.value = true
}

const closeDeleteModal = () => {
  showDeleteModal.value = false
}

const confirmDeleteAccount = async () => {
  if (!deletePassword.value) {
    deleteError.value = '请输入当前密码'
    return
  }
  deleteLoading.value = true
  deleteError.value = ''
  try {
    await authApi.deleteAccount(deletePassword.value)
    await authStore.logout()
    showDeleteModal.value = false
    router.push('/login')
  } catch (error) {
    deleteError.value = error instanceof Error ? error.message : '删除失败，请重试'
  } finally {
    deleteLoading.value = false
  }
}

// ==================== 生命周期 ====================
onMounted(async () => {
  await Promise.allSettled([authStore.refreshUser(), loadFullProfile()])
  await loadStats()
  await loadLoginLogs()
  await nextTick()
  setupScrollSpy()
})

onUnmounted(() => {
  if (successTimer) clearTimeout(successTimer)
  if (countdownTimer) clearInterval(countdownTimer)
  observer?.disconnect()
  observer = null
})
</script>

<style lang="scss" scoped>
.profile {
  display: flex;
  min-height: 100vh;
  background: $color-canvas;
  padding-top: $header-height;
}

// 居中与 1200px 宽度由 styles/base.scss 统一提供，这里不再重复限宽
.profile__content {
  flex: 1;
  min-height: calc(100vh - #{$header-height});
  padding: $spacing-lg $spacing-xl $spacing-2xl;
}

// ==================== 用户资料头部（紧凑浅色） ====================
.profile__hero {
  display: flex;
  align-items: center;
  gap: $spacing-lg;
  padding: $spacing-lg $spacing-xl;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  margin-bottom: $spacing-lg;
}

.profile__avatar {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: $app-accent-soft;
  color: $app-accent-strong;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
}

.profile__identity {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profile__name {
  margin: 0;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $color-text;
  line-height: 1.3;
}

.profile__meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: $spacing-sm;
  font-size: $font-size-sm;
  color: $color-text-faint;
}

.profile__email {
  overflow-wrap: anywhere;
  color: $color-text-soft;
}

.profile__sep {
  color: $color-border;
}

.profile__verify {
  font-weight: $font-weight-medium;
  color: $warning-color;

  &--ok {
    color: $app-success;
  }
}

.profile__hero-btn {
  flex-shrink: 0;
  padding: $spacing-sm $spacing-lg;
  border-radius: $radius-control;
  border: 1px solid $color-border;
  background: $color-surface;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $app-accent-strong;
  cursor: pointer;
  transition: all $transition-fast;

  &:hover {
    border-color: $app-accent;
    background: $app-accent-softer;
  }
}

// ==================== 主体：左导航 + 右内容 ====================
.profile__body {
  display: grid;
  grid-template-columns: 180px minmax(0, 1fr);
  align-items: start;
  gap: $spacing-lg;
}

.profile__nav {
  position: sticky;
  top: calc(#{$header-height} + #{$spacing-lg});
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
}

.profile__nav-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  width: 100%;
  padding: 10px $spacing-md;
  border: 0;
  border-radius: $radius-control;
  background: transparent;
  font-size: $font-size-sm;
  color: $color-text-soft;
  text-align: left;
  cursor: pointer;
  transition:
    background $transition-fast,
    color $transition-fast;

  &:hover {
    background: $color-surface-sunken;
    color: $color-text;
  }

  &--active {
    background: $app-brand-soft;
    color: $app-brand;
    font-weight: $font-weight-medium;
  }
}

.profile__panels {
  display: flex;
  flex-direction: column;
  gap: $spacing-lg;
  min-width: 0;
}

// ==================== 面板 ====================
.panel {
  padding: $spacing-xl;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-card;
  box-shadow: $shadow-card;
  // 锚点跳转时给固定头部留位置
  scroll-margin-top: calc(#{$header-height} + #{$spacing-md});

  &--danger {
    border-color: rgba(239, 68, 68, 0.25);
    background: rgba(239, 68, 68, 0.02);
  }
}

.panel__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: $spacing-md;
}

.panel__title {
  margin: 0 0 $spacing-md;
  font-size: $font-size-lg;
  font-weight: $font-weight-semibold;
  color: $color-text;

  &--danger {
    color: $error-color;
  }
}

.panel__head .panel__title {
  margin-bottom: 0;
}

.panel__head + .panel__hint,
.panel__head + .panel__error,
.panel__head + .stats-grid,
.panel__head + .log-list {
  margin-top: $spacing-md;
}

.panel__hint {
  margin: 0 0 $spacing-md;
  font-size: $font-size-sm;
  color: $color-text-faint;
  line-height: 1.6;
}

.panel__error {
  margin: $spacing-md 0 0;
  font-size: $font-size-sm;
  color: $error-color;
}

.panel__actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
  margin-top: $spacing-lg;
}

// ==================== 行式操作（账号安全） ====================
.row {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  width: 100%;
  padding: $spacing-md;
  margin-bottom: $spacing-xs;
  border: 1px solid transparent;
  border-radius: $radius-control;
  background: transparent;
  font-size: $font-size-base;
  text-align: left;
  cursor: pointer;
  transition:
    background $transition-fast,
    border-color $transition-fast;

  &:last-child { margin-bottom: 0; }

  &:hover {
    background: $color-surface-sunken;
    border-color: $color-border;
  }
}

.row__icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: $radius-control;
  background: $app-accent-softer;
  color: $app-accent-strong;

  svg { width: 18px; height: 18px; }
}

.row__text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.row__label {
  color: $color-text;
  font-weight: $font-weight-medium;
}

.row__desc {
  font-size: $font-size-xs;
  color: $color-text-faint;
}

.row__value {
  font-size: $font-size-sm;
  color: $color-text-faint;
  overflow-wrap: anywhere;
}

.row__arrow {
  flex-shrink: 0;
  display: flex;
  color: $color-text-faint;

  svg { width: 16px; height: 16px; }
}

// ==================== 统计 ====================
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: $spacing-md;
}

.stat {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  padding: $spacing-lg;
  background: $color-surface;
  border: 1px solid $color-border;
  border-radius: $radius-card;
  transition: border-color $transition-fast;
}

.stat__icon {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: $radius-control;

  svg { width: 20px; height: 20px; }

  &--brand   { color: #0890b0; background: rgba(8, 144, 176, 0.12); }
  &--success { color: #10b880; background: rgba(16, 184, 128, 0.12); }
  &--info    { color: #3880f0; background: rgba(56, 128, 240, 0.12); }
  &--warning { color: #f59e0b; background: rgba(245, 158, 11, 0.14); }
}

.stat__text {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.stat__value {
  font-size: $font-size-2xl;
  font-weight: $font-weight-semibold;
  line-height: 1.15;
  color: $color-text;
  font-variant-numeric: tabular-nums;
}

.stat__label {
  font-size: $font-size-sm;
  color: $color-text-faint;
}

// ==================== 表单 ====================
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: $spacing-md $spacing-lg;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.field__label {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text;
}

.field__input {
  width: 100%;
  padding: 9px $spacing-md;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  font-size: $font-size-base;
  color: $color-text;
  transition:
    border-color $transition-fast,
    box-shadow $transition-fast;

  &::placeholder {
    color: $color-text-faint;
  }

  &:focus {
    outline: none;
    border-color: $app-accent;
    box-shadow: 0 0 0 3px rgba(8, 145, 178, 0.16);
  }
}

.field__tip {
  font-size: $font-size-xs;
  color: $color-text-faint;
}

// ==================== 登录记录 ====================
.log-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
  margin: 0;
  padding: 0;
  list-style: none;
}

.log {
  padding: $spacing-md;
  border: 1px solid $color-border-soft;
  border-radius: $radius-control;
  background: $color-surface-alt;
}

.log__main {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.log__ip {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text;
}

.log__loc {
  font-size: $font-size-sm;
  color: $color-text-soft;
}

.log__meta {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-md;
  margin-top: 6px;
  font-size: $font-size-xs;
  color: $color-text-faint;
}

.log__fail {
  margin: 6px 0 0;
  font-size: $font-size-xs;
  color: $error-color;
}

// ==================== 按钮 ====================
.btn {
  padding: 9px $spacing-lg;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text;
  cursor: pointer;
  transition:
    background $transition-fast,
    border-color $transition-fast,
    opacity $transition-fast;

  &:disabled {
    opacity: 0.55;
    cursor: not-allowed;
  }

  &--primary {
    background: $app-accent;
    border-color: $app-accent;
    color: #ffffff;

    &:hover:not(:disabled) {
      background: $app-accent-strong;
      border-color: $app-accent-strong;
    }
  }

  &--ghost {
    background: transparent;
    color: $color-text-soft;

    &:hover:not(:disabled) {
      background: $color-surface-sunken;
      color: $color-text;
    }
  }

  &--danger {
    background: $error-color;
    border-color: $error-color;
    color: #ffffff;

    &:hover:not(:disabled) {
      background: #dc2626;
      border-color: #dc2626;
    }
  }
}

// ==================== 提示条 ====================
.profile__toast {
  position: fixed;
  top: calc(#{$header-height} + #{$spacing-md});
  left: 50%;
  transform: translateX(-50%);
  z-index: $z-toast;
  padding: 10px $spacing-lg;
  border-radius: $radius-pill;
  background: $success-color;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: #ffffff;
  box-shadow: $shadow-lg;
}

// ==================== 弹窗 ====================
.pmodal {
  position: fixed;
  inset: 0;
  z-index: $z-modal;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: $spacing-lg;
  background: rgba(15, 23, 42, 0.5);
}

.pmodal__dialog {
  width: 100%;
  max-width: 440px;
  max-height: 88vh;
  overflow-y: auto;
  padding: $spacing-xl;
  border-radius: $radius-card;
  background: $color-surface;
  box-shadow: $shadow-xl;
}

.pmodal__title {
  margin: 0 0 $spacing-lg;
  font-size: $font-size-xl;
  font-weight: $font-weight-semibold;
  color: $color-text;

  &--danger {
    color: $error-color;
  }
}

.pmodal__warning {
  margin: 0 0 $spacing-md;
  font-size: $font-size-sm;
  color: $error-color;
  line-height: 1.6;
}

.pmodal__field {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: $spacing-md;
}

.pmodal__label {
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text;
}

.pmodal__input {
  width: 100%;
  padding: 9px $spacing-md;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  font-size: $font-size-base;
  color: $color-text;
  transition:
    border-color $transition-fast,
    box-shadow $transition-fast;

  &::placeholder {
    color: $color-text-faint;
  }

  &:focus {
    outline: none;
    border-color: $app-accent;
    box-shadow: 0 0 0 3px rgba(8, 145, 178, 0.16);
  }
}

.pmodal__tip {
  font-size: $font-size-xs;
  color: $color-text-faint;

  &--dev {
    margin: 0 0 $spacing-md;
    color: $app-accent-strong;
  }
}

.pmodal__code-row {
  display: flex;
  gap: $spacing-sm;

  .pmodal__input {
    flex: 1;
    min-width: 0;
  }

  .pmodal__btn {
    flex-shrink: 0;
  }
}

.pmodal__error {
  margin-bottom: $spacing-md;
  font-size: $font-size-sm;
  color: $error-color;
}

.pmodal__actions {
  display: flex;
  justify-content: flex-end;
  gap: $spacing-sm;
  margin-top: $spacing-lg;
}

.pmodal__btn {
  padding: 9px $spacing-lg;
  border: 1px solid $color-border;
  border-radius: $radius-control;
  background: $color-surface;
  font-size: $font-size-sm;
  font-weight: $font-weight-medium;
  color: $color-text;
  cursor: pointer;
  transition:
    background $transition-fast,
    border-color $transition-fast,
    opacity $transition-fast;

  &:disabled {
    opacity: 0.55;
    cursor: not-allowed;
  }

  &--primary {
    background: $app-accent;
    border-color: $app-accent;
    color: #ffffff;

    &:hover:not(:disabled) {
      background: $app-accent-strong;
      border-color: $app-accent-strong;
    }
  }

  &--ghost {
    background: transparent;
    color: $color-text-soft;

    &:hover:not(:disabled) {
      background: $color-surface-sunken;
      color: $color-text;
    }
  }

  &--danger {
    background: $error-color;
    border-color: $error-color;
    color: #ffffff;

    &:hover:not(:disabled) {
      background: #dc2626;
      border-color: #dc2626;
    }
  }
}

// ==================== 窄屏 ====================
@media (max-width: 900px) {
  .profile__content {
    padding: $spacing-md;
  }

  .profile__body {
    grid-template-columns: minmax(0, 1fr);
  }

  .profile__nav {
    position: static;
    flex-direction: row;
    flex-wrap: nowrap;
    overflow-x: auto;
    gap: $spacing-sm;
    scrollbar-width: none;

    &::-webkit-scrollbar { display: none; }
  }

  .profile__nav-item {
    width: auto;
    flex-shrink: 0;
    white-space: nowrap;
  }

  .profile__hero {
    flex-wrap: wrap;
  }

  .profile__hero-btn {
    margin-left: auto;
  }
}
</style>
