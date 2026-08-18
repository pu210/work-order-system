<template>
  <div class="tc-page">
    <div class="tc-page-header">
      <span class="tc-eyebrow">NEW TICKET</span>
      <h1 class="tc-title">建立新工單</h1>
      <p class="tc-subtitle">請盡量詳細描述問題，以利工程師更快掌握狀況並處理</p>
    </div>

    <div class="tc-grid">
      <div class="tc-card">
        <form @submit.prevent="handleSubmit">
          <div class="tc-field">
            <label class="tc-label tc-required">標題</label>
            <input v-model.trim="form.title" type="text" class="tc-input" maxlength="50" required />
            <div class="tc-hint">{{ form.title.length }} / 50</div>
          </div>

          <div class="tc-field-row">
            <div class="tc-field">
              <label class="tc-label tc-required">報修大類</label>
              <select v-model="selectedCategoryId" class="tc-input" required>
                <option value="" disabled>請選擇大類</option>
                <option v-for="c in categories" :key="c.repairCategoriesId" :value="c.repairCategoriesId">
                  {{ c.name }}
                </option>
              </select>
            </div>
            <div class="tc-field">
              <label class="tc-label tc-required">細項類別</label>
              <select v-model="form.subCategoryId" class="tc-input" required :disabled="!selectedCategoryId">
                <option value="" disabled>請選擇細項</option>
                <option v-for="s in filteredSubCategories" :key="s.subCategoriesId" :value="s.subCategoriesId">
                  {{ s.name }}
                </option>
              </select>
            </div>
          </div>

          <div class="tc-field">
            <label class="tc-label tc-required">位置</label>
            <input v-model.trim="form.locationDetail" type="text" class="tc-input" maxlength="100" required />
            <div class="tc-hint">{{ form.locationDetail.length }} / 100</div>
          </div>

          <div class="tc-field">
            <label class="tc-label">聯絡電話</label>
            <input
              v-model.trim="form.contactPhone"
              type="text"
              class="tc-input"
              maxlength="10"
              pattern="\d{10}"
              title="請輸入 10 碼數字"
            />
            <div class="tc-hint">{{ form.contactPhone.length }} / 10（選填，若填寫須為 10 碼數字）</div>
          </div>

          <div class="tc-field">
            <label class="tc-label">描述</label>
            <textarea v-model.trim="form.description" class="tc-input tc-textarea" rows="4" maxlength="300"></textarea>
            <div class="tc-hint">{{ form.description.length }} / 300</div>
          </div>

          <div class="tc-field">
            <label class="tc-label">附件（限圖片，單檔 10MB 以內）</label>
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
            <div v-if="fileError" class="tc-error-text">{{ fileError }}</div>
          </div>

          <div v-if="errorMessage" class="tc-alert-danger">{{ errorMessage }}</div>

          <div class="tc-actions">
            <button type="submit" class="tc-btn tc-btn-primary" :disabled="submitting">
              {{ submitting ? "送出中…" : "送出工單" }}
            </button>
            <button type="button" class="tc-btn tc-btn-secondary" :disabled="submitting" @click="router.back()">
              取消
            </button>
          </div>
        </form>
      </div>

      <div class="tc-card tc-tips">
        <h3 class="tc-tips-title">📋 填寫小提醒</h3>
        <ul class="tc-tips-list">
          <li>優先級將依「子類別」自動判斷，緊急案件會優先派工</li>
          <li>請提供明確地點與可聯絡電話，方便工程師到場處理</li>
          <li>送出後可於「我的工單」追蹤處理進度</li>
          <li>如為公共安全疑慮（漏水、跳電等），請同時口頭通知行政部</li>
        </ul>
      </div>
    </div>
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
.tc-page {
  max-width: 860px;
  margin: 0 auto;
}

/* ---------------------------------------------------------------------- */
/* 頁首 */
/* ---------------------------------------------------------------------- */
.tc-page-header {
  margin-bottom: 22px;
}
.tc-eyebrow {
  display: block;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: var(--color-primary);
  text-transform: uppercase;
  margin-bottom: 6px;
}
.tc-title {
  font-family: var(--font-display);
  font-weight: 700;
  font-size: 24px;
  color: var(--color-ink);
  margin: 0;
}
.tc-subtitle {
  margin: 6px 0 0;
  color: var(--color-text-muted);
  font-size: 13.5px;
}

/* ---------------------------------------------------------------------- */
/* 版面：左側主表單、右側提醒卡片 */
/* ---------------------------------------------------------------------- */
.tc-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 16px;
  align-items: flex-start;
}
@media (max-width: 900px) {
  .tc-grid {
    grid-template-columns: 1fr;
  }
}

.tc-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius);
  box-shadow: 0 1px 2px rgba(20, 33, 61, 0.05);
  padding: 20px 22px;
}

/* ---------------------------------------------------------------------- */
/* 表單欄位 */
/* ---------------------------------------------------------------------- */
.tc-field {
  margin-bottom: 16px;
}
.tc-field-row {
  display: flex;
  gap: 14px;
}
.tc-field-row .tc-field {
  flex: 1;
}
.tc-label {
  display: block;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--color-text);
  margin-bottom: 6px;
}
.tc-required::after {
  content: ' *';
  color: var(--color-danger);
}
.tc-hint {
  font-size: 11.5px;
  color: var(--color-text-faint);
  margin-top: 4px;
}

.tc-input {
  width: 100%;
  padding: 10px 12px;
  border-radius: var(--radius-sm);
  border: 1px solid var(--color-border);
  font-size: 13.5px;
  font-family: var(--font-body);
  background: #fff;
  color: var(--color-text);
}
.tc-input:focus {
  border-color: var(--color-primary);
  outline: none;
  box-shadow: 0 0 0 3px var(--color-primary-soft);
}
.tc-textarea {
  resize: vertical;
  min-height: 90px;
}

.tc-error-text {
  font-size: 0.8rem;
  color: var(--color-danger);
  margin-top: 4px;
}
.tc-alert-danger {
  background: var(--color-danger-soft);
  color: var(--color-danger);
  border-radius: var(--radius-sm);
  padding: 10px 14px;
  font-size: 13.5px;
  margin-bottom: 16px;
}

.tc-actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}
.tc-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13.5px;
  font-weight: 600;
  padding: 9px 18px;
  border-radius: var(--radius-sm);
  border: 1px solid transparent;
  cursor: pointer;
  transition: all 0.15s;
  font-family: var(--font-body);
}
.tc-btn-primary {
  background: var(--color-primary);
  color: #fff;
}
.tc-btn-primary:hover:not(:disabled) {
  background: var(--color-primary-dark);
}
.tc-btn-secondary {
  background: #fff;
  color: var(--color-text);
  border-color: var(--color-border);
}
.tc-btn-secondary:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.tc-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* ---------------------------------------------------------------------- */
/* 附件（維持既有結構與邏輯，僅將顏色來源換成專案設計變數） */
/* ---------------------------------------------------------------------- */
.attachment-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.attachment-tile,
.attachment-add-tile {
  width: 96px;
  height: 96px;
  border-radius: var(--radius-sm);
  position: relative;
}

.attachment-tile {
  overflow: hidden;
  border: 1px solid var(--color-border);
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
  border: 1px dashed var(--color-border);
  background: transparent;
  color: var(--color-text-muted);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.attachment-add-tile:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

/* ---------------------------------------------------------------------- */
/* 填寫小提醒 */
/* ---------------------------------------------------------------------- */
.tc-tips-title {
  font-size: 15px;
  font-weight: 700;
  color: var(--color-ink);
  margin: 0 0 12px;
  font-family: var(--font-display);
}
.tc-tips-list {
  padding-left: 18px;
  margin: 0;
  line-height: 1.9;
  font-size: 12.5px;
  color: var(--color-text-muted);
}
</style>
