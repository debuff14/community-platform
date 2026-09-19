<template>
  <el-card shadow="never" class="create-card">
    <template #header>
      <div class="card-header">
        <span>我要报修</span>
        <el-button link @click="router.push('/repair')">返回我的报修</el-button>
      </div>
    </template>

    <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
      <el-form-item label="报修类型" prop="type">
        <el-select v-model="form.type" placeholder="请选择报修类型" style="width: 260px">
          <el-option label="水电气" :value="1" />
          <el-option label="门窗" :value="2" />
          <el-option label="电梯" :value="3" />
          <el-option label="其他" :value="0" />
        </el-select>
      </el-form-item>

      <el-form-item label="紧急程度" prop="urgency">
        <el-radio-group v-model="form.urgency">
          <el-radio :value="0">普通</el-radio>
          <el-radio :value="1">紧急</el-radio>
        </el-radio-group>
        <span v-if="form.urgency === 1" class="urgency-tip">紧急报修 2 小时内响应</span>
      </el-form-item>

      <el-form-item label="问题描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="5"
          maxlength="500"
          show-word-limit
          placeholder="请描述故障情况，例如：厨房水管漏水"
          style="max-width: 520px"
        />
      </el-form-item>

      <el-form-item label="现场照片">
        <div class="upload-block">
          <el-upload
            v-model:file-list="fileList"
            list-type="picture-card"
            :limit="3"
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
          <div class="upload-tip">选填，最多上传 3 张现场照片（jpg/png/gif/webp，单张不超过 5MB）</div>
        </div>
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="submitting" @click="submit">提交报修</el-button>
      </el-form-item>
    </el-form>

    <el-dialog v-model="previewVisible" width="520px" title="图片预览">
      <img :src="previewUrl" alt="预览" style="width: 100%" />
    </el-dialog>
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { createRepair } from '@/api/repair'
import { uploadImage } from '@/api/upload'

const router = useRouter()

const formRef = ref()
const submitting = ref(false)
const fileList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

const form = reactive({
  type: null,
  urgency: 0,
  description: '',
  images: []
})

const rules = {
  type: [{ required: true, message: '请选择报修类型', trigger: 'change' }],
  urgency: [{ required: true, message: '请选择紧急程度', trigger: 'change' }],
  description: [{ required: true, message: '请填写问题描述', trigger: 'blur' }]
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

const syncImages = () => {
  form.images = fileList.value.map((file) => file.url || file.response).filter(Boolean)
}

const onSuccess = (response, uploadFile) => {
  uploadFile.url = response
  syncImages()
}

const onRemove = () => {
  syncImages()
}

const onExceed = () => {
  ElMessage.warning('最多上传 3 张现场照片')
}

const onPreview = (file) => {
  previewUrl.value = file.url || file.response
  previewVisible.value = true
}

const submit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  syncImages()
  submitting.value = true
  try {
    await createRepair(form)
    ElMessage.success('报修提交成功')
    router.push('/repair')
  } catch (e) {
    /* 错误提示由拦截器统一处理 */
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.create-card {
  max-width: 760px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.urgency-tip {
  margin-left: 12px;
  font-size: 13px;
  color: #f56c6c;
}

.upload-tip {
  margin-top: 2px;
  font-size: 13px;
  color: #909399;
}
</style>
