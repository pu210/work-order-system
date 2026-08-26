<template>
  <Teleport to="body">
    <div class="workflow-modal-backdrop" role="presentation" @mousedown.self="requestClose">
      <section
        class="workflow-modal"
        role="dialog"
        aria-modal="true"
        :aria-labelledby="titleId"
      >
        <header class="workflow-modal-header">
          <h5 :id="titleId" class="mb-0">{{ title }}</h5>
          <button
            type="button"
            class="btn-close"
            aria-label="關閉"
            :disabled="busy"
            @click="requestClose"
          ></button>
        </header>

        <div class="workflow-modal-body">
          <slot />
        </div>

        <footer class="workflow-modal-footer">
          <slot name="footer" />
        </footer>
      </section>
    </div>
  </Teleport>
</template>

<script setup>
import { onBeforeUnmount, onMounted } from "vue";

const props = defineProps({
  title: { type: String, required: true },
  busy: { type: Boolean, default: false },
  titleId: { type: String, default: "work-order-action-dialog-title" },
});

const emit = defineEmits(["close"]);

function requestClose() {
  if (!props.busy) emit("close");
}

function handleKeydown(event) {
  if (event.key === "Escape") requestClose();
}

onMounted(() => {
  document.addEventListener("keydown", handleKeydown);
  document.body.classList.add("workflow-modal-open");
});

onBeforeUnmount(() => {
  document.removeEventListener("keydown", handleKeydown);
  document.body.classList.remove("workflow-modal-open");
});
</script>

<style>
.workflow-modal-open { overflow: hidden; }
.workflow-modal-backdrop {
  position: fixed;
  z-index: 1080;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 1rem;
  background: rgba(15, 23, 42, 0.58);
}
.workflow-modal {
  width: min(620px, 100%);
  max-height: calc(100vh - 2rem);
  overflow: auto;
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 24px 64px rgba(15, 23, 42, 0.28);
}
.workflow-modal-header,
.workflow-modal-footer {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
}
.workflow-modal-header {
  justify-content: space-between;
  border-bottom: 1px solid #e5e7eb;
}
.workflow-modal-footer {
  justify-content: flex-end;
  flex-wrap: wrap;
  border-top: 1px solid #e5e7eb;
}
.workflow-modal-body { padding: 1.25rem; }
</style>
