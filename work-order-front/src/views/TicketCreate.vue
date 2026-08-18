<template>
  <div class="ticket-create-view">
    <h3 class="mb-4">建立工單</h3>

    <form @submit.prevent="handleSubmit" class="col-12 col-md-8 col-lg-6">
      <div class="mb-3">
        <label class="form-label">標題 <span class="text-danger">*</span></label>
        <input v-model.trim="form.title" type="text" class="form-control" maxlength="50" required />
        <div class="form-text">{{ form.title.length }} / 50</div>
      </div>

      <div class="row">
        <div class="col-6 mb-3">
          <label class="form-label">報修大類 <span class="text-danger">*</span></label>
          <select v-model="selectedCategoryId" class="form-select" required>
            <option value="" disabled>請選擇大類</option>
            <option v-for="c in categories" :key="c.repairCategoriesId" :value="c.repairCategoriesId">
              {{ c.name }}
            </option>
          </select>
        </div>
        <div class="col-6 mb-3">
          <label class="form-label">細項類別 <span class="text-danger">*</span></label>
          <select v-model="form.subCategoryId" class="form-select" required :disabled="!selectedCategoryId">
            <option value="" disabled>請選擇細項</option>
            <option v-for="s in filteredSubCategories" :key="s.subCategoriesId" :value="s.subCategoriesId">
              {{ s.name }}
            </option>
          </select>
        </div>
      </div>

      <div class="mb-3">
        <label class="form-label">位置 <span class="text-danger">*</span></label>
        <input v-model.trim="form.locationDetail" type="text" class="form-control" maxlength="100" required />
        <div class="form-text">{{ form.locationDetail.length }} / 100</div>
      </div>

      <div class="mb-3">
        <label class="form-label">聯絡電話</label>
        <input
          v-model.trim="form.contactPhone"
          type="text"
          class="form-control"
          maxlength="10"
          pattern="\d{10}"
          title="請輸入 10 碼數字"
        />
        <div class="form-text">{{ form.contactPhone.length }} / 10（選填，若填寫須為 10 碼數字）</div>
      </div>

      <div class="mb-3">
        <label class="form-label">描述</label>
        <textarea v-model.trim="form.description" class="form-control" rows="4" maxlength="300"></textarea>
        <div class="form-text">{{ form.description.length }} / 300</div>
      </div>

      <div class="mb-3">
        <label class="form-label">附件（限圖片，單檔 10MB 以內）</label>
        <input
          ref="fileInputRef"
          type="file"
          accept="image/*"
          multiple
          class="d-none"
          @change="handleFilesSelected"
        />
        <div class="attachment-grid">
          <div v-for="(item, index) in selectedFiles" :key="item.id" class="attachment-tile">
            <img :src="item.previewUrl" :alt="item.file.name" class="attachment-thumb" />
            <button
              type="button"
              class="attachment-remove"
              :title="`移除 ${item.file.name}`"
              @click="removeFile(index)"
            >
              ✕
            </button>
          </div>
          <button type="button" class="attachment-add-tile" @click="fileInputRef.click()">
            <span class="fs-3 d-block">＋</span>
            <span class="small">添加圖片</span>
          </button>
        </div>
        <div v-if="fileError" class="text-danger small mt-1">{{ fileError }}</div>
      </div>

      <div v-if="errorMessage" class="alert alert-danger py-2">{{ errorMessage }}</div>

      <button type="submit" class="btn btn-primary" :disabled="submitting">
        {{ submitting ? "送出中…" : "送出工單" }}
      </button>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import Swal from 'sweetalert2'
import { createWorkOrder, uploadAttachments } from '@/api/workOrder.js'
import { getRepairCategories, getSubCategories } from '@/api/category.js'

const router = useRouter()

const MAX_FILE_SIZE = 10 * 1024 * 1024

const categories = ref([])
const subCategories = ref([])
const selectedCategoryId = ref('')
const submitting = ref(false)
const errorMessage = ref('')
// { id, file, previewUrl } — previewUrl 是 URL.createObjectURL 產生的本機預覽，跟真正上傳無關
const selectedFiles = ref([])
const fileError = ref('')
const fileInputRef = ref(null)
let nextFileId = 0

const form = ref({
  title: '',
  subCategoryId: '',
  locationDetail: '',
  contactPhone: '',
  description: '',
})

const filteredSubCategories = computed(() =>
  subCategories.value.filter((s) => s.categoryId === selectedCategoryId.value)
)

watch(selectedCategoryId, () => {
  form.value.subCategoryId = ''
})

onMounted(async () => {
  try {
    const [categoryList, subCategoryList] = await Promise.all([
      getRepairCategories(),
      getSubCategories(),
    ])
    categories.value = categoryList
    subCategories.value = subCategoryList
  } catch (error) {
    errorMessage.value = '無法載入報修分類，請確認後端已啟動'
  }
})

function handleFilesSelected(event) {
  fileError.value = ''
  const files = Array.from(event.target.files || [])
  const invalid = files.find((f) => !f.type.startsWith('image/') || f.size > MAX_FILE_SIZE)
  if (invalid) {
    fileError.value = `「${invalid.name}」不是圖片或超過 10MB，請重新選擇`
  } else {
    selectedFiles.value.push(
      ...files.map((file) => ({
        id: nextFileId++,
        file,
        previewUrl: URL.createObjectURL(file),
      }))
    )
  }
  // 清空原生 input，讓下一次選檔（含選到同一個檔案）都會觸發 change，且不留原生「已選擇 N 個檔案」殘留字樣
  event.target.value = ''
}

function removeFile(index) {
  URL.revokeObjectURL(selectedFiles.value[index].previewUrl)
  selectedFiles.value.splice(index, 1)
}

onUnmounted(() => {
  selectedFiles.value.forEach((item) => URL.revokeObjectURL(item.previewUrl))
})

async function handleSubmit() {
  errorMessage.value = ''
  submitting.value = true
  try {
    const created = await createWorkOrder({
      title: form.value.title,
      subCategoryId: form.value.subCategoryId,
      locationDetail: form.value.locationDetail,
      contactPhone: form.value.contactPhone || undefined,
      description: form.value.description || undefined,
    })

    // 建單與附件上傳非同一交易：單已建立成功，附件失敗只提示不擋流程
    if (selectedFiles.value.length) {
      try {
        await uploadAttachments(created.workOrderId, selectedFiles.value.map((item) => item.file))
      } catch (uploadError) {
        await Swal.fire({
          icon: 'warning',
          title: `工單建立成功（${created.workOrderNo}）`,
          text: `但附件上傳失敗：${uploadError.response?.data?.message || '請稍後至工單詳情頁重新上傳'}`,
        })
        router.push({ name: 'my-tickets' })
        return
      }
    }

    await Swal.fire({
      icon: 'success',
      title: '工單建立成功',
      text: created.workOrderNo,
    })
    router.push({ name: 'my-tickets' })
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '建立工單失敗，請稍後再試'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.attachment-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.attachment-tile,
.attachment-add-tile {
  width: 96px;
  height: 96px;
  border-radius: 0.375rem;
  position: relative;
}

.attachment-tile {
  overflow: hidden;
  border: 1px solid var(--bs-border-color);
}

.attachment-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}

.attachment-remove {
  position: absolute;
  top: 2px;
  right: 2px;
  width: 20px;
  height: 20px;
  line-height: 1;
  border: none;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  color: #fff;
  font-size: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.attachment-remove:hover {
  background: rgba(220, 53, 69, 0.9);
}

.attachment-add-tile {
  border: 1px dashed var(--bs-border-color);
  background: transparent;
  color: var(--bs-secondary-color);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.attachment-add-tile:hover {
  border-color: var(--bs-primary);
  color: var(--bs-primary);
}
</style>
