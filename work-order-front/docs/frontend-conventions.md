# 前端開發規範

參考：檔案要放在哪裡、串接後端 API 的流程。

---

## 一、目錄結構與檔案該放哪裡

```
src/
├── api/          # 後端 API 呼叫的封裝函式（每個後端模組一支檔案）
├── components/   # 可重複使用的元件
│   └── layout/   # 版面用元件，如頂端列 AppNav.vue
├── layouts/      # 頁面外框（登入頁外框、登入後版面外框）
├── views/        # 頁面級元件，一個路由對應一個檔案
├── router/       # 路由設定與導覽選單設定
├── stores/       # Pinia 狀態管理
├── utils/        # 不依賴 Vue 的純工具函式
└── plugins/      # 第三方套件的初始化設定（如 axios）
```

### `api/`：後端 API 呼叫，依「後端模組」拆檔案，不要寫在元件裡

**規則：元件裡永遠不要直接寫 `axios.post(...)` 或 `api.get(...)`，一定要包成 `api/` 資料夾裡的具名函式再呼叫。**

現有檔案：
- [`api/workOrder.js`](../src/api/workOrder.js) — 對應後端 `modules/b`、`modules/c`（工單 CRUD + 狀態機操作）
- [`api/category.js`](../src/api/category.js) — 對應後端 `modules/f`（報修分類）

之後要串新的後端模組，就新增對應的 `api/xxx.js`，函式命名用「動詞 + 名詞」（`getXxx`、`createXxx`、`updateXxx`），例如之後串 `modules/e` 公告可以建立 `api/announcement.js`。

**為什麼要這樣做**：API 路徑字串、`.then(res => res.data.data)` 這種解包邏輯只要寫一次，元件不用管 HTTP 細節，之後路徑改了也只要改一個地方。

### `router/navItems.js`：頂端列選單與角色權限的單一資料源

三種身份（`ADMIN`/`HANDLER`/`EMPLOYEE`）能看到哪些選單，**只在這一個檔案定義**，`router/router.js` 的路由守衛和 `components/layout/AppNav.vue` 的頂端列都是讀這份清單，不要在別的地方另外寫角色清單，否則兩邊會不同步。

新增頁面的標準流程：
1. 在 `views/` 建立頁面元件
2. 在 `navItems.js` 的 `NAV_ITEMS` 加一筆（`key`, `label`, `path`, `roles`, `enabled: true`）
3. 在 `router.js` 註冊路由，`meta.roles` 直接引用 `rolesFor(key)`，不要手動重複打角色清單

### `stores/auth.js`：登入狀態的唯一來源

拿目前登入者的 `userId`、`name`、`roleCodes` 一律用 `useAuthStore()`，不要自己再去讀 `localStorage`。判斷角色用 `authStore.hasRole('ADMIN')`。

---

## 二、串接後端 API 的標準流程

每次要串一支新的後端 API，照這個順序做，不要跳步：

### 1. 找到對應的 Controller / Service / DTO，確認欄位
不要憑猜測寫請求欄位。去後端讀對應的 `@RequestMapping`、`@RequestBody` 的 DTO class，確認：
- 欄位名稱、必填/選填、長度限制
- 回傳的 DTO 有哪些欄位

### 2. 確認回應的「外殼」格式
這個專案兩種外殼混用，串之前一定要確認是哪一種：
- **包 `ApiResponse`**：`modules/a`、`modules/b`、`modules/c` 用這種，回應是 `{ code, message, data }`，要拿 `response.data.data`
- **直接回傳資料本身**：`modules/e`、`modules/f` 是這種，`response.data` 就是資料，沒有再包一層

拿錯層會直接變 `undefined`，這個錯誤不會報錯、只會畫面空白，很難排查，串接前務必先確認。

### 3. 檢查前端需要的欄位，後端 DTO 裡有沒有
如果發現後端 DTO 缺欄位（例如畫面需要顯示使用者名稱，但 DTO 沒回傳），**不要自己在前端硬湊或用別的方式繞過**，去跟負責那支後端的組員確認要不要補欄位。範例：
- `LoginResponseDTO` 原本沒有 `userId`/`name`，是前端要顯示使用者姓名時發現缺的，補上後才能用
- `WorkOrderResponse` 原本沒有 `creatorUserId`，是要判斷「目前登入者是不是報修人本人」時發現缺的

**動到別人負責的後端檔案時，一定要在異動的地方加註解**，寫清楚為什麼加這個欄位，方便對方之後合併/review 時知道發生什麼事。

### 4. 在 `api/` 資料夾建立對應函式
```js
// api/workOrder.js 範例
export function createWorkOrder(payload) {
  return api.post('/api/work-orders', payload).then((res) => res.data.data)
}
```

### 5. 元件裡處理三種狀態：loading / 成功 / 失敗
```js
const loading = ref(false)
const errorMessage = ref('')

async function fetchData() {
  loading.value = true
  errorMessage.value = ''
  try {
    const result = await someApiFunction()
    // 成功處理
  } catch (error) {
    errorMessage.value = error.response?.data?.message || '操作失敗，請稍後再試'
  } finally {
    loading.value = false
  }
}
```
畫面上要有 `v-if="loading"`、`v-else-if="errorMessage"` 對應的顯示，不讓使用者看到空白畫面或英文技術錯誤訊息。

