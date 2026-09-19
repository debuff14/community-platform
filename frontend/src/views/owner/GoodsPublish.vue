<template>
  <el-card shadow="never" class="publish-card">
    <template #header>
      <div class="card-header">
        <span>{{ isEdit ? '编辑商品' : '发布商品' }}</span>
        <el-button link @click="router.push('/my-goods')">我的商品</el-button>
      </div>
    </template>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="form.name" maxlength="100" show-word-limit placeholder="请输入商品名称" style="max-width: 420px" />
      </el-form-item>

      <el-form-item label="商品描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          maxlength="1000"
          show-word-limit
          placeholder="介绍一下商品的成色、使用情况等"
          style="max-width: 520px"
        />
      </el-form-item>

      <el-form-item label="价格" prop="price">
        <el-input-number v-model="form.price" :precision="2" :min="0.01" :step="1" style="width: 200px" />
        <span class="tip">元</span>
      </el-form-item>

      <el-form-item label="分类" prop="category">
        <el-select v-model="form.category" placeholder="请选择分类" style="width: 200px">
          <el-option v-for="(name, key) in GOODS_CATEGORIES" :key="key" :label="name" :value="Number(key)" />
        </el-select>
      </el-form-item>

      <el-form-item label="商品图片" prop="image">
        <div>
          <el-upload
            v-model:file-list="fileList"
            list-type="picture-card"
            :limit="1"
            :http-request="doUpload"
            :before-upload="beforeUpload"
            :on-success="onSuccess"
            :on-remove="onRemove"
            :on-exceed="onExceed"
            :on-preview="onPreview"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">必填，仅支持 jpg/png/gif/webp，单张不超过 5MB</div>
        </div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitting" :disabled="!form.image" @click="submit">
          {{ isEdit ? '保存并重新提交审核' : '提交审核' }}
        </el-button>
      </el-form-item>
    </el-form>

    <el-dialog v-model="previewVisible" width="480px" title="图片预览">
      <img :src="previewUrl" alt="预览" style="width: 100%" />
    </el-dialog>
  </el-card>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getMarketDetail, publishGoods, updateGoods } from '@/api/goods'
import { uploadImage } from '@/api/upload'
import { GOODS_CATEGORIES } from '@/utils/goods'

const route = useRoute()
const router = useRouter()

const isEdit = computed(() => !!route.query.id)

const formRef = ref()
const submitting = ref(false)
const fileList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

const form = reactive({
  name: '',
  description: '',
  price: null,
  category: null,
  image: ''
})

const rules = {
  name: [{ required: true, message: '请填写商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请填写价格', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  image: [{ required: true, message: '请上传商品图片', trigger: 'change' }]
}

const beforeUpload = (file) => {
  const allowed = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!allowed.includes(file.type)) {
    ElMessage.error('只支持 jpg/png/gif/webp 格式的图片')
    return false
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const doUpload = async (options) => {
  try {
    const url = await uploadImage(options.file)
    options.onSuccess(url)
  } catch (e) {
    options.onError(e)
  }
}

const onSuccess = (response, uploadFile) => {
  uploadFile.url = response
  form.image = response
}

const onRemove = () => {
  form.image = ''
}

const onExceed = () => {
  ElMessage.warning('只能上传 1 张商品图片')
}

const onPreview = (file) => {
  previewUrl.value = file.url || file.response
  previewVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateGoods(route.query.id, form)
      ElMessage.success('修改成功，已重新提交审核')
    } else {
      await publishGoods(form)
      ElMessage.success('发布成功，等待管理员审核')
    }
    router.push('/my-goods')
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  if (!isEdit.value) return
  try {
    const goods = await getMarketDetail(route.query.id)
    form.name = goods.name
    form.description = goods.description
    form.price = Number(goods.price)
    form.category = goods.category
    form.image = goods.image
    fileList.value = [{ name: '商品图片', url: goods.image }]
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  }
})
</script>

<style scoped>
.publish-card {
  max-width: 780px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.tip {
  margin-left: 8px;
  color: #909399;
}

.upload-tip {
  margin-top: 4px;
  font-size: 13px;
  color: #909399;
}
</style>
