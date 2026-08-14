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
        <input v-model.trim="form.contactPhone" type="text" class="form-control" maxlength="10" />
        <div class="form-text">{{ form.contactPhone.length }} / 10</div>
      </div>

      <div class="mb-3">
        <label class="form-label">描述</label>
        <textarea v-model.trim="form.description" class="form-control" rows="4" maxlength="300"></textarea>
        <div class="form-text">{{ form.description.length }} / 300</div>
      </div>

      <div v-if="errorMessage" class="alert alert-danger py-2">{{ errorMessage }}</div>
      <div v-if="successMessage" class="alert alert-success py-2">{{ successMessage }}</div>

      <button type="submit" class="btn btn-primary" :disabled="submitting">
        {{ submitting ? "送出中…" : "送出工單" }}
      </button>
    </form>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { createWorkOrder } from '@/api/workOrder.js'
import { getRepairCategories, getSubCategories } from '@/api/category.js'

const router = useRouter()

const categories = ref([])
const subCategories = ref([])
const selectedCategoryId = ref('')
const submitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

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

async function handleSubmit() {
  errorMessage.value = ''
  successMessage.value = ''
  submitting.value = true
  try {
    const created = await createWorkOrder({
      title: form.value.title,
      subCategoryId: form.value.subCategoryId,
      locationDetail: form.value.locationDetail,
      contactPhone: form.value.contactPhone || undefined,
      description: form.value.description || undefined,
    })
    successMessage.value = `工單建立成功（${created.workOrderNo}）`
    router.push({ name: 'my-tickets' })
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '建立工單失敗，請稍後再試'
  } finally {
    submitting.value = false
  }
}
</script>
