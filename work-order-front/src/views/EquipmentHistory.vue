<template>
  <div class="eh-page">
    <header class="eh-page-header">
      <div>
        <span class="eh-eyebrow">EQUIPMENT HISTORY</span>
        <h1 class="eh-title">設備維修紀錄</h1>
        <p class="eh-subtitle">查看這台設備過去的報修與處理紀錄</p>
      </div>
      <button
        v-if="equipment"
        type="button"
        class="eh-btn eh-btn-primary eh-qr-trigger"
        @click="openQrPreview"
      >
        列印 QR Code
      </button>
    </header>

    <div v-if="loading && !equipment" class="eh-card eh-state">
      正在載入設備維修紀錄…
    </div>

    <div v-else-if="errorMessage" class="eh-card eh-state eh-state-error">
      <strong>無法載入設備維修紀錄</strong>
      <p>{{ errorMessage }}</p>
      <button type="button" class="eh-btn eh-btn-primary" @click="fetchHistory">
        重新載入
      </button>
    </div>

    <template v-else-if="equipment">
      <section class="eh-equipment-card" aria-labelledby="equipment-name">
        <div class="eh-equipment-heading">
          <div>
            <div class="eh-equipment-meta">
              <span class="eh-target-no">{{ equipment.targetNo }}</span>
              <span
                :class="[
                  'eh-equipment-status',
                  equipment.status ? 'is-active' : 'is-inactive',
                ]"
              >
                {{ equipment.status ? "啟用中" : "已停用" }}
              </span>
            </div>
            <h2 id="equipment-name">{{ equipment.name }}</h2>
          </div>
        </div>

        <dl class="eh-equipment-details">
          <div>
            <dt>設備型號</dt>
            <dd>{{ equipment.model || "未設定" }}</dd>
          </div>
          <div>
            <dt>設備編號</dt>
            <dd>{{ equipment.targetNo }}</dd>
          </div>
          <div>
            <dt>維修紀錄</dt>
            <dd>{{ totalElements }} 筆</dd>
          </div>
        </dl>
      </section>

      <section class="eh-card" aria-labelledby="history-title">
        <div class="eh-card-header">
          <div>
            <h2 id="history-title">歷史工單</h2>
            <p>點擊任一筆紀錄即可查看完整工單內容</p>
          </div>
          <label class="eh-period-filter">
            <select v-model="selectedPeriod" :disabled="loading" @change="changePeriod">
              <option v-for="option in periodOptions" :key="option.value" :value="option.value">
                {{ option.label }}
              </option>
            </select>
          </label>
        </div>

        <div v-if="workOrders.length === 0" class="eh-empty-state">
          <div class="eh-empty-icon" aria-hidden="true">—</div>
          <h3>目前沒有維修紀錄</h3>
          <p>{{ selectedPeriod === "ALL" ? "這台設備目前沒有已完成工單。" : "所選期間內沒有已完成工單。" }}</p>
        </div>

        <div v-else class="eh-table-wrap">
          <table class="eh-table">
            <thead>
              <tr>
                <th>工單編號</th>
                <th>標題</th>
                <th>類別</th>
                <th>報修人</th>
                <th>負責工程師</th>
                <th>完成時間</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="workOrder in workOrders"
                :key="workOrder.workOrderId"
                tabindex="0"
                role="link"
                @click="openTicket(workOrder)"
                @keydown.enter="openTicket(workOrder)"
                @keydown.space.prevent="openTicket(workOrder)"
              >
                <td class="eh-mono">{{ workOrder.workOrderNo }}</td>
                <td class="eh-ticket-title">{{ workOrder.title }}</td>
                <td>{{ workOrder.categoryName || "—" }}</td>
                <td>{{ workOrder.creatorName || "—" }}</td>
                <td>{{ workOrder.assignedHandlerName || "尚未指派" }}</td>
                <td class="eh-nowrap">{{ formatTime(workOrder.completedTime) }}</td>
              </tr>
            </tbody>
          </table>
        </div>

        <nav v-if="totalPages > 1" class="eh-pagination" aria-label="歷史工單分頁">
          <button
            type="button"
            class="eh-page-btn"
            :disabled="page === 0 || loading"
            @click="goToPage(page - 1)"
          >
            上一頁
          </button>
          <span class="eh-page-info">第 {{ page + 1 }} / {{ totalPages }} 頁</span>
          <button
            type="button"
            class="eh-page-btn"
            :disabled="page + 1 >= totalPages || loading"
            @click="goToPage(page + 1)"
          >
            下一頁
          </button>
        </nav>
      </section>
    </template>

    <div
      v-if="qrModalOpen"
      class="eh-modal-overlay"
      role="presentation"
      @click.self="closeQrPreview"
    >
      <section
        class="eh-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="qr-modal-title"
      >
        <header class="eh-modal-header">
          <div>
            <span class="eh-eyebrow">QR CODE</span>
            <h2 id="qr-modal-title">設備維修紀錄 QR Code</h2>
          </div>
          <button
            type="button"
            class="eh-modal-close"
            aria-label="關閉 QR Code 預覽"
            @click="closeQrPreview"
          >
            ×
          </button>
        </header>

        <div class="eh-qr-preview">
          <div v-if="qrLoading" class="eh-qr-loading">正在產生 QR Code…</div>
          <img
            v-else-if="qrDataUrl"
            :src="qrDataUrl"
            :alt="`${equipment.name} 維修紀錄 QR Code`"
            class="eh-qr-image"
          />
          <div class="eh-qr-equipment-name">{{ equipment.name }}</div>
          <div class="eh-qr-target-no">{{ equipment.targetNo }}</div>
          <p>掃描查看維修紀錄</p>
          <div class="eh-qr-url">{{ qrUrl }}</div>
        </div>

        <div v-if="qrError" class="eh-qr-error">{{ qrError }}</div>

        <footer class="eh-modal-footer">
          <button type="button" class="eh-btn eh-btn-secondary" @click="closeQrPreview">
            關閉
          </button>
          <button
            type="button"
            class="eh-btn eh-btn-secondary"
            :disabled="!qrDataUrl || qrLoading"
            @click="downloadQrCode"
          >
            下載 QR Code
          </button>
          <button
            type="button"
            class="eh-btn eh-btn-primary"
            :disabled="!qrDataUrl || qrLoading"
            @click="printQrCode"
          >
            列印
          </button>
        </footer>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from "vue";
import { useRoute, useRouter } from "vue-router";
import QRCode from "qrcode";

import { getEquipmentHistory } from "@/api/equipmentHistory.js";

const PAGE_SIZE = 20;
const periodOptions = [
  { value: "ALL", label: "全部" },
  { value: "7D", label: "最近 7 天" },
  { value: "1M", label: "最近 1 個月" },
  { value: "3M", label: "最近 3 個月" },
  { value: "6M", label: "最近 6 個月" },
  { value: "1Y", label: "最近 1 年" },
];

const route = useRoute();
const router = useRouter();

const equipment = ref(null);
const workOrders = ref([]);
const page = ref(0);
const totalPages = ref(0);
const totalElements = ref(0);
const selectedPeriod = ref("ALL");
const loading = ref(false);
const errorMessage = ref("");
const qrModalOpen = ref(false);
const qrLoading = ref(false);
const qrDataUrl = ref("");
const qrUrl = ref("");
const qrError = ref("");

const targetNo = computed(() => String(route.params.targetNo || ""));

function formatTime(value) {
  if (!value) return "—";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value.replace("T", " ").slice(0, 16);
  }

  return new Intl.DateTimeFormat("zh-TW", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  }).format(date);
}

function getErrorMessage(error) {
  if (error?.response?.status === 404) {
    return `找不到設備「${targetNo.value}」，請確認 QR Code 是否正確。`;
  }

  return error?.response?.data?.message || "系統暫時無法取得資料，請稍後再試。";
}

async function fetchHistory() {
  if (!targetNo.value) {
    errorMessage.value = "網址中缺少設備編號。";
    return;
  }

  loading.value = true;
  errorMessage.value = "";

  try {
    const result = await getEquipmentHistory(targetNo.value, {
      page: page.value,
      size: PAGE_SIZE,
      period: selectedPeriod.value,
    });

    equipment.value = result.equipment;
    workOrders.value = result.workOrders?.content || [];
    page.value = result.workOrders?.page ?? page.value;
    totalPages.value = result.workOrders?.totalPages ?? 0;
    totalElements.value = result.workOrders?.totalElements ?? 0;
  } catch (error) {
    errorMessage.value = getErrorMessage(error);
  } finally {
    loading.value = false;
  }
}

function changePeriod() {
  page.value = 0;
  fetchHistory();
}

function goToPage(targetPage) {
  if (targetPage < 0 || targetPage >= totalPages.value) return;
  page.value = targetPage;
  fetchHistory();
}

function openTicket(workOrder) {
  router.push({
    name: "ticket-detail",
    params: { id: workOrder.workOrderId },
    query: {
      from: "equipment-history",
      targetNo: targetNo.value,
    },
  });
}

async function openQrPreview() {
  qrModalOpen.value = true;
  qrLoading.value = true;
  qrError.value = "";

  const resolvedRoute = router.resolve({
    name: "equipment-history",
    params: { targetNo: targetNo.value },
  });
  qrUrl.value = new URL(resolvedRoute.href, window.location.origin).toString();

  try {
    qrDataUrl.value = await QRCode.toDataURL(qrUrl.value, {
      width: 360,
      margin: 2,
      errorCorrectionLevel: "M",
      color: {
        dark: "#14213D",
        light: "#FFFFFF",
      },
    });
  } catch {
    qrDataUrl.value = "";
    qrError.value = "QR Code 產生失敗，請稍後再試。";
  } finally {
    qrLoading.value = false;
  }
}

function closeQrPreview() {
  qrModalOpen.value = false;
  qrError.value = "";
}

function downloadQrCode() {
  if (!qrDataUrl.value || !equipment.value) return;

  const safeTargetNo = equipment.value.targetNo.replace(/[^A-Za-z0-9_-]/g, "_");
  const link = document.createElement("a");
  link.href = qrDataUrl.value;
  link.download = `equipment-${safeTargetNo}-qrcode.png`;
  link.click();
}

function printQrCode() {
  if (!qrDataUrl.value || !equipment.value) return;

  const printWindow = window.open("", "_blank", "width=520,height=720");
  if (!printWindow) {
    qrError.value = "瀏覽器阻擋了列印視窗，請允許彈出式視窗後再試。";
    return;
  }

  const printDocument = printWindow.document;
  printDocument.open();
  printDocument.write(`
    <!doctype html>
    <html lang="zh-Hant">
      <head>
        <meta charset="UTF-8" />
        <title>設備 QR Code</title>
        <style>
          @page { margin: 12mm; }
          * { box-sizing: border-box; }
          body {
            margin: 0;
            color: #14213d;
            font-family: Arial, "Noto Sans TC", sans-serif;
          }
          .label {
            width: 90mm;
            margin: 0 auto;
            padding: 8mm;
            border: 1px solid #cfd5df;
            border-radius: 4mm;
            text-align: center;
          }
          img { display: block; width: 58mm; height: 58mm; margin: 0 auto 4mm; }
          h1 { margin: 0 0 2mm; font-size: 18px; }
          .target-no { margin-bottom: 3mm; font-family: monospace; font-size: 15px; font-weight: 700; }
          p { margin: 0; color: #4b5563; font-size: 13px; }
        </style>
      </head>
      <body>
        <main class="label">
          <img id="qr-image" alt="設備維修紀錄 QR Code" />
          <h1 id="equipment-name"></h1>
          <div id="target-no" class="target-no"></div>
          <p>掃描查看維修紀錄</p>
        </main>
      </body>
    </html>
  `);
  printDocument.close();

  printDocument.getElementById("equipment-name").textContent = equipment.value.name;
  printDocument.getElementById("target-no").textContent = equipment.value.targetNo;

  const image = printDocument.getElementById("qr-image");
  image.addEventListener("load", () => {
    printWindow.focus();
    printWindow.print();
  });
  image.src = qrDataUrl.value;
}

onMounted(fetchHistory);
</script>

<style scoped>
.eh-page {
  max-width: 1240px;
  margin: 0 auto;
}

.eh-page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
}

.eh-qr-trigger {
  flex: 0 0 auto;
}

.eh-eyebrow {
  display: block;
  margin-bottom: 6px;
  color: var(--color-primary);
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.08em;
}

.eh-title {
  margin: 0;
  color: var(--color-ink);
  font-family: var(--font-display);
  font-size: 24px;
  font-weight: 700;
}

.eh-subtitle {
  margin: 6px 0 0;
  color: var(--color-text-muted);
  font-size: 13.5px;
}

.eh-card,
.eh-equipment-card {
  background: var(--color-surface);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  box-shadow: 0 1px 2px rgba(20, 33, 61, 0.05), 0 2px 8px rgba(20, 33, 61, 0.06);
}

.eh-equipment-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-bottom: 18px;
  padding: 20px 22px;
}

.eh-equipment-heading {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 240px;
}

.eh-equipment-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.eh-target-no {
  color: var(--color-primary);
  font-family: var(--font-mono, monospace);
  font-size: 12px;
  font-weight: 700;
}

.eh-equipment-status {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 700;
}

.eh-equipment-status.is-active {
  background: var(--color-success-soft);
  color: var(--color-success);
}

.eh-equipment-status.is-inactive {
  background: #eef0f4;
  color: var(--color-text-muted);
}

.eh-equipment-heading h2 {
  margin: 0;
  color: var(--color-ink);
  font-family: var(--font-display);
  font-size: 20px;
}

.eh-equipment-details {
  display: grid;
  grid-template-columns: repeat(3, minmax(110px, 1fr));
  gap: 12px 28px;
  margin: 0;
}

.eh-equipment-details div {
  min-width: 0;
}

.eh-equipment-details dt {
  margin-bottom: 4px;
  color: var(--color-text-faint);
  font-size: 11.5px;
  font-weight: 600;
}

.eh-equipment-details dd {
  margin: 0;
  overflow: hidden;
  color: var(--color-text);
  font-size: 13.5px;
  font-weight: 600;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.eh-card {
  padding: 20px 22px;
}

.eh-card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.eh-card-header h2 {
  margin: 0;
  color: var(--color-ink);
  font-family: var(--font-display);
  font-size: 17px;
}

.eh-card-header p {
  margin: 4px 0 0;
  color: var(--color-text-muted);
  font-size: 12.5px;
}

.eh-period-filter {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--color-text-muted);
  font-size: 12.5px;
  font-weight: 600;
  white-space: nowrap;
}

.eh-period-filter select {
  min-width: 132px;
  padding: 7px 30px 7px 10px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: #fff;
  color: var(--color-text);
  font: inherit;
}

.eh-period-filter select:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.eh-btn {
  padding: 8px 15px;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  cursor: pointer;
  font-family: var(--font-body);
  font-size: 13px;
  font-weight: 600;
}

.eh-btn:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.eh-btn-primary {
  background: var(--color-primary);
  color: #fff;
}

.eh-btn-secondary {
  background: #fff;
  border-color: var(--color-border);
  color: var(--color-text);
}

.eh-table-wrap {
  overflow-x: auto;
}

.eh-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}

.eh-table th {
  padding: 10px 14px;
  border-bottom: 1px solid var(--color-border);
  color: var(--color-text-faint);
  font-size: 11.5px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-align: left;
  white-space: nowrap;
}

.eh-table td {
  padding: 13px 14px;
  border-bottom: 1px solid var(--color-border);
  vertical-align: middle;
}

.eh-table tbody tr {
  cursor: pointer;
  transition: background 0.12s;
}

.eh-table tbody tr:hover,
.eh-table tbody tr:focus {
  background: var(--color-bg);
  outline: none;
}

.eh-table tbody tr:last-child td {
  border-bottom: 0;
}

.eh-mono {
  color: var(--color-ink);
  font-family: var(--font-mono, monospace);
  font-weight: 700;
  white-space: nowrap;
}

.eh-ticket-title {
  min-width: 160px;
  font-weight: 600;
}

.eh-nowrap {
  white-space: nowrap;
}

.eh-state,
.eh-empty-state {
  padding: 48px 20px;
  color: var(--color-text-muted);
  text-align: center;
}

.eh-state-error {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}

.eh-state-error p {
  margin: 6px 0 18px;
}

.eh-empty-icon {
  display: grid;
  width: 40px;
  height: 40px;
  margin: 0 auto 10px;
  place-items: center;
  border-radius: 50%;
  background: var(--color-bg);
  color: var(--color-text-faint);
}

.eh-empty-state h3 {
  margin: 0 0 4px;
  color: var(--color-text);
  font-family: var(--font-display);
  font-size: 15px;
}

.eh-empty-state p {
  margin: 0;
  font-size: 12.5px;
}

.eh-pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-top: 18px;
}

.eh-page-btn {
  padding: 6px 14px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  background: #fff;
  color: var(--color-text-muted);
  cursor: pointer;
  font-family: var(--font-body);
  font-size: 12.5px;
}

.eh-page-btn:hover:not(:disabled) {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.eh-page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.eh-page-info {
  color: var(--color-text-muted);
  font-size: 12.5px;
}

.eh-modal-overlay {
  position: fixed;
  z-index: 1200;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 20px;
  background: rgba(15, 23, 42, 0.58);
}

.eh-modal {
  width: min(100%, 480px);
  max-height: calc(100vh - 40px);
  overflow-y: auto;
  border-radius: var(--radius-lg);
  background: var(--color-surface);
  box-shadow: var(--shadow-pop);
}

.eh-modal-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 20px;
  border-bottom: 1px solid var(--color-border);
}

.eh-modal-header h2 {
  margin: 0;
  color: var(--color-ink);
  font-family: var(--font-display);
  font-size: 18px;
}

.eh-modal-close {
  width: 34px;
  height: 34px;
  padding: 0;
  border: 0;
  border-radius: 50%;
  background: var(--color-bg);
  color: var(--color-text-muted);
  cursor: pointer;
  font-size: 24px;
  line-height: 1;
}

.eh-qr-preview {
  padding: 22px 24px 14px;
  text-align: center;
}

.eh-qr-image {
  display: block;
  width: min(100%, 300px);
  height: auto;
  margin: 0 auto 12px;
}

.eh-qr-loading {
  display: grid;
  min-height: 280px;
  place-items: center;
  color: var(--color-text-muted);
  font-size: 13.5px;
}

.eh-qr-equipment-name {
  color: var(--color-ink);
  font-family: var(--font-display);
  font-size: 18px;
  font-weight: 700;
}

.eh-qr-target-no {
  margin-top: 4px;
  color: var(--color-primary);
  font-family: var(--font-mono, monospace);
  font-size: 14px;
  font-weight: 700;
}

.eh-qr-preview p {
  margin: 8px 0;
  color: var(--color-text-muted);
  font-size: 13px;
}

.eh-qr-url {
  overflow-wrap: anywhere;
  color: var(--color-text-faint);
  font-size: 11px;
}

.eh-qr-error {
  margin: 0 20px 14px;
  padding: 9px 12px;
  border-radius: var(--radius-sm);
  background: var(--color-danger-soft);
  color: var(--color-danger);
  font-size: 12.5px;
}

.eh-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding: 16px 20px;
  border-top: 1px solid var(--color-border);
}

@media (max-width: 800px) {
  .eh-equipment-card {
    align-items: stretch;
    flex-direction: column;
  }

  .eh-equipment-details {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 560px) {
  .eh-page-header {
    align-items: stretch;
    flex-direction: column;
    margin-bottom: 16px;
  }

  .eh-qr-trigger {
    align-self: flex-start;
  }

  .eh-equipment-card,
  .eh-card {
    padding: 16px;
  }

  .eh-equipment-details {
    grid-template-columns: 1fr;
  }

  .eh-card-header {
    align-items: stretch;
    flex-direction: column;
  }

  .eh-period-filter {
    justify-content: space-between;
  }

  .eh-card-header .eh-btn {
    align-self: flex-start;
  }

  .eh-modal-footer {
    align-items: stretch;
    flex-direction: column-reverse;
  }
}
</style>
