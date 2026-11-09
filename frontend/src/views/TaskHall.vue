<template>
  <div class="hall">
    <div class="head">
      <div>
        <h2 class="wk-page-title">任务大厅</h2>
        <p class="wk-page-desc">共 {{ total }} 个任务正在招募，赏金已由平台托管</p>
      </div>
      <el-button type="primary" @click="router.push('/tasks/publish')">
        <el-icon><Plus /></el-icon> 发布任务
      </el-button>
    </div>

    <div class="layout">
      <!-- 左侧筛选 -->
      <aside class="filters wk-card">
        <div class="filter-block">
          <div class="filter-title">任务分类</div>
          <div
            class="cat-item"
            :class="{ active: !query.categoryId }"
            @click="selectCategory(null)"
          >
            全部分类
          </div>
          <div
            v-for="c in categories"
            :key="c.id"
            class="cat-item"
            :class="{ active: query.categoryId === c.id }"
            @click="selectCategory(c.id)"
          >
            {{ c.name }}
          </div>
        </div>

        <div class="filter-block">
          <div class="filter-title">赏金区间（元）</div>
          <div class="budget-row">
            <el-input v-model="query.minBudget" placeholder="最低" size="small" />
            <span class="dash">—</span>
            <el-input v-model="query.maxBudget" placeholder="最高" size="small" />
          </div>
          <el-button class="apply" size="small" @click="search">应用</el-button>
        </div>

        <div class="filter-block">
          <div class="filter-title">排序</div>
          <el-radio-group v-model="query.sort" size="small" @change="search">
            <el-radio-button value="latest">最新</el-radio-button>
            <el-radio-button value="budget">赏金</el-radio-button>
            <el-radio-button value="deadline">截止</el-radio-button>
          </el-radio-group>
        </div>
      </aside>

      <!-- 右侧列表 -->
      <section class="list">
        <div class="search-bar wk-card">
          <el-input
            v-model="query.keyword"
            placeholder="搜索任务标题或描述，如：LOGO、小程序、文案"
            clearable
            @keyup.enter="search"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" @click="search">搜索</el-button>
        </div>

        <div v-loading="loading" class="cards">
          <el-empty v-if="!loading && tasks.length === 0" description="没有符合条件的任务，换个筛选试试" />

          <article
            v-for="task in tasks"
            :key="task.id"
            class="task-card wk-card"
            @click="router.push(`/tasks/${task.id}`)"
          >
            <div class="card-top">
              <el-tag size="small" effect="plain">{{ task.categoryName }}</el-tag>
              <el-tag size="small" :type="taskStatus(task.status).type" effect="light">
                {{ taskStatus(task.status).text }}
              </el-tag>
              <span class="fav" @click.stop="onFavorite(task)">
                <el-icon :class="{ on: task.favorite }"><StarFilled v-if="task.favorite" /><Star v-else /></el-icon>
              </span>
            </div>

            <h3 class="title">{{ task.title }}</h3>
            <p class="desc">{{ task.description }}</p>

            <div class="budget">¥ {{ formatMoney(task.budget) }}</div>

            <div class="meta">
              <span><el-icon><Clock /></el-icon> {{ remainText(task.deadline) }}</span>
              <span><el-icon><User /></el-icon> {{ task.employerName }}</span>
              <span><el-icon><ChatDotRound /></el-icon> {{ task.bidCount }} 人投标</span>
            </div>
          </article>
        </div>

        <el-pagination
          v-if="total > query.size"
          class="pager"
          layout="prev, pager, next, total"
          :current-page="query.page"
          :page-size="query.size"
          :total="total"
          @current-change="onPage"
        />
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getCategories, getTaskHall, toggleFavorite } from '@/api/task'
import { getToken } from '@/api/request'
import { formatMoney, remainText, taskStatus } from '@/utils/format'

const router = useRouter()
const categories = ref([])
const tasks = ref([])
const total = ref(0)
const loading = ref(false)

const query = reactive({
  keyword: '',
  categoryId: null,
  minBudget: '',
  maxBudget: '',
  sort: 'latest',
  page: 1,
  size: 9
})

async function load() {
  loading.value = true
  try {
    const params = {
      page: query.page,
      size: query.size,
      sort: query.sort
    }
    if (query.keyword) params.keyword = query.keyword
    if (query.categoryId) params.categoryId = query.categoryId
    if (query.minBudget) params.minBudget = query.minBudget
    if (query.maxBudget) params.maxBudget = query.maxBudget

    const data = await getTaskHall(params)
    tasks.value = data.list || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function search() {
  query.page = 1
  load()
}

function selectCategory(id) {
  query.categoryId = id
  search()
}

function onPage(p) {
  query.page = p
  load()
}

async function onFavorite(task) {
  if (!getToken()) {
    router.push({ path: '/login', query: { redirect: '/tasks' } })
    return
  }
  const data = await toggleFavorite(task.id)
  task.favorite = data.favorite
  ElMessage.success(data.favorite ? '已收藏' : '已取消收藏')
}

onMounted(async () => {
  categories.value = await getCategories().catch(() => [])
  await load()
})
</script>

<style scoped>
.head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 20px;
}

.layout {
  display: grid;
  grid-template-columns: 220px 1fr;
  gap: 20px;
  align-items: start;
}

.filters {
  padding: 18px;
  position: sticky;
  top: 88px;
}

.filter-block + .filter-block {
  margin-top: 22px;
  padding-top: 18px;
  border-top: 1px solid var(--wk-border);
}

.filter-title {
  font-size: 13px;
  color: var(--wk-text-3);
  margin-bottom: 10px;
}

.cat-item {
  padding: 8px 10px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--wk-text-2);
  font-size: 14px;
}

.cat-item:hover {
  background: #f7f8fa;
}

.cat-item.active {
  background: var(--wk-primary-light);
  color: var(--wk-primary);
  font-weight: 600;
}

.budget-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.dash {
  color: var(--wk-text-3);
}

.apply {
  width: 100%;
  margin-top: 10px;
}

.search-bar {
  display: flex;
  gap: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
}

.cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  min-height: 200px;
}

.task-card {
  padding: 18px;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
  display: flex;
  flex-direction: column;
}

.task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.09);
}

.card-top {
  display: flex;
  align-items: center;
  gap: 8px;
}

.fav {
  margin-left: auto;
  color: #c9cdd4;
  font-size: 16px;
  display: inline-flex;
}

.fav .on {
  color: #ffb020;
}

.title {
  font-size: 15px;
  margin: 12px 0 8px;
  line-height: 1.5;
  /* 固定两行高度，卡片内赏金/元信息才能对齐 */
  min-height: 45px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.desc {
  margin: 0;
  font-size: 13px;
  color: var(--wk-text-3);
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
}

.budget {
  margin-top: 14px;
  color: var(--wk-primary);
  font-size: 20px;
  font-weight: 700;
}

.meta {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  font-size: 12px;
  color: var(--wk-text-3);
}

.meta span {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.pager {
  justify-content: center;
  margin-top: 24px;
}

@media (max-width: 1100px) {
  .cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
