<template>
  <button 
    @click="goBack"
    class="ai-tech-back-button"
    :class="{ 'is-glass': theme === 'light' }"
    :title="title"
  >
    <span class="icon">↩️</span>
    <span class="text">返回列表</span>
  </button>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const props = defineProps({
  to: {
    type: String,
    default: '/aitech'
  },
  title: {
    type: String,
    default: '返回 AI 互动教程列表'
  },
  theme: {
    type: String,
    default: 'dark'
  }
})

const router = useRouter()

const goBack = () => {
  router.push(props.to)
}
</script>

<style scoped>
.ai-tech-back-button {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 9999;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  background: rgba(30, 41, 59, 0.7); /* Dark background default */
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 999px;
  color: white;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  outline: none;
}

.ai-tech-back-button:hover {
  background: rgba(30, 41, 59, 0.9);
  transform: translateY(-1px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
}

.ai-tech-back-button:active {
  transform: translateY(0);
}

/* Light theme variant (for when the button needs to be light-colored on a dark background? 
   No, usually 'theme' prop refers to the PAGE theme. 
   If page is dark, button should be visible (light/glass).
   If page is light, button should be visible (dark/colored).
   
   The default above is "Dark Button" (Slate 800). This works on Light backgrounds.
   It also works on Dark backgrounds if it has a border.
   
   Let's just make it look like a nice glass button that fits everywhere.
*/

/* If the user wants a "Glass" effect that blends with dark backgrounds: */
.ai-tech-back-button.is-glass {
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
}
.ai-tech-back-button.is-glass:hover {
  background: rgba(255, 255, 255, 0.2);
}
</style>
