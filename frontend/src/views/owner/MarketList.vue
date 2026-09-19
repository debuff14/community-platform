<template>
  <div class="market">
    <el-card shadow="never">
      <div class="toolbar">
        <div class="filters">
          <el-input
            v-model="filters.keyword"
            placeholder="搜索商品名称"
            clearable
            style="width: 220px"
            @keyup.enter="onSearch"
            @clear="onSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="onSearch" />
            </template>
          </el-input>
          <el-select v-model="filters.category" placeholder="全部分类" clearable style="width: 150px" @change="onSearch">
            <el-option v-for="(name, key) in GOODS_CATEGORIES" :key="key" :label="name" :value="Number(key)" />
          </el-select>
        </div>
        <div class="actions">
          <el-button :icon="Sell" @click="router.push('/my-goods')">我的商品</el-button>
          <el-button type="primary" :icon="Plus" @click="router.push('/market/publish')">发布商品</el-button>
        </div>
      </div>

      <div v-loading="loading" class="goods-wrap">
        <el-row :gutter="16">
          <el-col v-for="item in list" :key="item.id" :xs="12" :sm="8" :md="6">
            <el-card
              shadow="hover"
              class="goods-card"
              :body-style="{ padding: '0' }"
              @click="router.push(`/market/${item.id}`)"
            >
              <el-image :src="item.image" fit="cover" class="goods-image" />
              <div class="goods-info">
                <div class="goods-name">{{ item.name }}</div>
                <div class="goods-price">¥ {{ Number(item.price).toFixed(2) }}</div>
                <div class="goods-seller">{{ item.building }}{{ item.roomNo }}</div>
              </div>
            </el-card>
          </el-col>
        </el-row>

        <el-empty v-if="!loading && list.length === 0" :description="hasFilter ? '没有找到相关商品' : '暂无在售商品'">
          <el-button v-if="hasFilter" @click="clearFilters">清空筛选</el-button>
        </el-empty>
      </div>

      <div class="pagination" v-if="total > 0">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="onPageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Plus, Search, Sell } from '@element-plus/icons-vue'
import { getMarketPage } from '@/api/goods'
import { GOODS_CATEGORIES } from '@/utils/goods'

const router = useRouter()

const list = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(12)
const loading = ref(false)
const filters = reactive({ keyword: '', category: null })

const hasFilter = computed(() => !!filters.keyword || filters.category !== null)

const load = async () => {
  loading.value = true
  try {
    const data = await getMarketPage({
      page: page.value,
      size: size.value,
      category: filters.category ?? undefined,
      keyword: filters.keyword || undefined
    })
    list.value = data.records
    total.value = data.total
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    loading.value = false
  }
}

const onSearch = () => {
  page.value = 1
  load()
}

const onPageChange = (p) => {
  page.value = p
  load()
}

const clearFilters = () => {
  filters.keyword = ''
  filters.category = null
  onSearch()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.filters {
  display: flex;
  gap: 10px;
}

.goods-wrap {
  min-height: 200px;
}

.goods-card {
  margin-bottom: 16px;
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
}

.goods-image {
  width: 100%;
  height: 160px;
  display: block;
}

.goods-info {
  padding: 12px;
}

.goods-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-price {
  margin-top: 6px;
  color: #f56c6c;
  font-size: 18px;
  font-weight: 600;
}

.goods-seller {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: 16px;
}
</style>
