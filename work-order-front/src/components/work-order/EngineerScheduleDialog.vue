<template>
  <Teleport to="body">
    <div class="schedule-dialog-backdrop" role="presentation" @mousedown.self="$emit('close')">
      <section
        class="schedule-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby="engineer-schedule-dialog-title"
      >
        <header class="schedule-dialog-header">
          <div>
            <h5 id="engineer-schedule-dialog-title" class="mb-1">工程師行事曆</h5>
            <div v-if="handlerName" class="small text-muted">{{ handlerName }}</div>
          </div>
          <button
            type="button"
            class="btn-close"
            aria-label="關閉行事曆"
            @click="$emit('close')"
          ></button>
        </header>

        <div class="schedule-dialog-body">
          <EngineerScheduleCalendar :handler-id="handlerId" />
        </div>

      </section>
    </div>
  </Teleport>
</template>

<script setup>
import EngineerScheduleCalendar from "./EngineerScheduleCalendar.vue";

defineProps({
  handlerId: { type: Number, required: true },
  handlerName: { type: String, default: "" },
});

defineEmits(["close"]);
</script>

<style scoped>
.schedule-dialog-backdrop {
  position: fixed;
  z-index: 1090;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: rgba(15, 23, 42, 0.62);
}

.schedule-dialog {
  width: min(920px, 100%);
  max-height: calc(100vh - 2rem);
  overflow: auto;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 24px 64px rgba(15, 23, 42, 0.3);
}

.schedule-dialog-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
}

.schedule-dialog-header {
  justify-content: space-between;
  border-bottom: 1px solid #e5e7eb;
}

.schedule-dialog-body {
  padding: 1.25rem;
}

@media (max-width: 575.98px) {
  .schedule-dialog-backdrop {
    padding: 0.5rem;
  }

  .schedule-dialog {
    max-height: calc(100vh - 1rem);
  }

  .schedule-dialog-body {
    padding: 0.75rem;
  }
}
</style>
