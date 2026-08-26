<template>
  <section class="chat-shell">
    <aside class="conversation-panel">
      <header class="panel-header">
        <div>
          <h2>Tin nhắn</h2>
          <span>{{ conversations.length }} cuộc trò chuyện</span>
        </div>
        <a-button size="small" :loading="loadingConversations" @click="refresh">Làm mới</a-button>
      </header>

      <div v-if="loadingConversations && !conversations.length" class="center-state"><a-spin /></div>
      <a-empty v-else-if="!conversations.length" description="Chưa có cuộc trò chuyện" class="empty-state" />
      <button
        v-for="conversation in conversations"
        v-else
        :key="conversation.id"
        type="button"
        class="conversation-row"
        :class="{ active: conversation.id === activeId }"
        @click="selectConversation(conversation.id)"
      >
        <a-avatar :size="44" :src="role === 'buyer' ? conversation.shopLogoUrl : undefined">
          {{ peerName(conversation).slice(0, 1).toUpperCase() }}
        </a-avatar>
        <span class="conversation-copy">
          <span class="conversation-title">
            <strong>{{ peerName(conversation) }}</strong>
            <time>{{ shortTime(conversation.lastMessageAt || conversation.createdAt) }}</time>
          </span>
          <span class="conversation-preview">{{ conversation.lastMessage || 'Bắt đầu cuộc trò chuyện' }}</span>
        </span>
        <span v-if="conversation.unreadCount" class="unread-badge">{{ conversation.unreadCount > 99 ? '99+' : conversation.unreadCount }}</span>
      </button>
    </aside>

    <main class="message-panel">
      <template v-if="activeConversation">
        <header class="message-header">
          <a-avatar :src="role === 'buyer' ? activeConversation.shopLogoUrl : undefined">
            {{ peerName(activeConversation).slice(0, 1).toUpperCase() }}
          </a-avatar>
          <div>
            <strong>{{ peerName(activeConversation) }}</strong>
            <small>{{ role === 'buyer' ? 'Shop' : 'Người mua' }}</small>
          </div>
        </header>

        <div ref="messageList" class="message-list">
          <div v-if="loadingMessages && !messages.length" class="center-state"><a-spin /></div>
          <a-empty v-else-if="!messages.length" description="Hãy gửi lời chào đầu tiên" class="empty-state" />
          <div
            v-for="item in messages"
            v-else
            :key="item.id"
            class="message-row"
            :class="{ mine: isMine(item) }"
          >
            <div class="message-bubble">
              <p>{{ item.content }}</p>
              <time>{{ fullTime(item.createdAt) }}</time>
            </div>
          </div>
        </div>

        <form class="composer" @submit.prevent="send">
          <a-textarea
            v-model:value="draft"
            :maxlength="2000"
            :auto-size="{ minRows: 1, maxRows: 4 }"
            placeholder="Nhập tin nhắn..."
            @press-enter="handleEnter"
          />
          <a-button type="primary" html-type="submit" :loading="sending" :disabled="!draft.trim()">Gửi</a-button>
        </form>
      </template>
      <div v-else class="select-state">
        <div class="select-icon">💬</div>
        <h3>Chọn một cuộc trò chuyện</h3>
        <p v-if="role === 'buyer'">Bạn cũng có thể mở chat từ trang sản phẩm hoặc trang shop.</p>
      </div>
    </main>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { message as toast } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getChatConversations,
  getChatMessages,
  markChatRead,
  sendChatMessage,
  startBuyerConversation,
  type ChatConversation,
  type ChatMessage,
  type ChatRole
} from '@/services/api/chat/chat.api'

const props = defineProps<{ role: ChatRole }>()
const route = useRoute()
const router = useRouter()
const conversations = ref<ChatConversation[]>([])
const messages = ref<ChatMessage[]>([])
const activeId = ref('')
const draft = ref('')
const loadingConversations = ref(false)
const loadingMessages = ref(false)
const sending = ref(false)
const refreshing = ref(false)
const messageList = ref<HTMLElement | null>(null)
let poller: ReturnType<typeof setInterval> | undefined

const activeConversation = computed(() => conversations.value.find(row => row.id === activeId.value))
const peerName = (row: ChatConversation) => props.role === 'buyer' ? row.shopName : row.buyerName
const isMine = (row: ChatMessage) => row.senderType === (props.role === 'buyer' ? 'BUYER' : 'SELLER')
const date = (value?: string) => value ? new Date(value) : undefined
const shortTime = (value?: string) => {
  const parsed = date(value)
  if (!parsed || Number.isNaN(parsed.getTime())) return ''
  const today = new Date()
  return parsed.toDateString() === today.toDateString()
    ? new Intl.DateTimeFormat('vi-VN', { hour: '2-digit', minute: '2-digit' }).format(parsed)
    : new Intl.DateTimeFormat('vi-VN', { day: '2-digit', month: '2-digit' }).format(parsed)
}
const fullTime = (value?: string) => {
  const parsed = date(value)
  return parsed && !Number.isNaN(parsed.getTime())
    ? new Intl.DateTimeFormat('vi-VN', { dateStyle: 'short', timeStyle: 'short' }).format(parsed)
    : ''
}

const scrollToBottom = async () => {
  await nextTick()
  if (messageList.value) messageList.value.scrollTop = messageList.value.scrollHeight
}

const loadConversations = async (showLoading = true) => {
  if (showLoading) loadingConversations.value = true
  try {
    conversations.value = await getChatConversations(props.role)
    if (activeId.value && !conversations.value.some(row => row.id === activeId.value)) activeId.value = ''
  } finally {
    if (showLoading) loadingConversations.value = false
  }
}

const loadMessages = async (showLoading = true) => {
  if (!activeId.value) return
  if (showLoading) loadingMessages.value = true
  try {
    messages.value = await getChatMessages(props.role, activeId.value)
    const current = activeConversation.value
    if (current?.unreadCount) {
      await markChatRead(props.role, activeId.value)
      current.unreadCount = 0
    }
    await scrollToBottom()
  } finally {
    if (showLoading) loadingMessages.value = false
  }
}

const syncRoute = () => {
  void router.replace({ query: activeId.value ? { conversationId: activeId.value } : {} })
}

const selectConversation = async (id: string) => {
  if (id === activeId.value && messages.value.length) return
  activeId.value = id
  messages.value = []
  syncRoute()
  try {
    await loadMessages()
  } catch (error: any) {
    toast.error(error?.response?.data?.message || 'Không tải được tin nhắn')
  }
}

const refresh = async () => {
  if (refreshing.value) return
  refreshing.value = true
  try {
    await loadConversations(false)
    await loadMessages(false)
  } catch {
    // Polling will retry; avoid repeatedly interrupting the user with notifications.
  } finally {
    refreshing.value = false
  }
}

const send = async () => {
  const content = draft.value.trim()
  if (!activeId.value || !content || sending.value) return
  sending.value = true
  try {
    await sendChatMessage(props.role, activeId.value, content)
    draft.value = ''
    await Promise.all([loadConversations(false), loadMessages(false)])
  } catch (error: any) {
    toast.error(error?.response?.data?.message || 'Không gửi được tin nhắn')
  } finally {
    sending.value = false
  }
}

const handleEnter = (event: KeyboardEvent) => {
  if (event.shiftKey) return
  event.preventDefault()
  void send()
}

onMounted(async () => {
  try {
    if (props.role === 'buyer' && typeof route.query.sellerId === 'string' && route.query.sellerId) {
      const started = await startBuyerConversation(route.query.sellerId)
      activeId.value = started.id
    } else if (typeof route.query.conversationId === 'string') {
      activeId.value = route.query.conversationId
    }
    await loadConversations()
    if (!activeId.value && conversations.value.length) activeId.value = conversations.value[0].id
    if (activeId.value) {
      syncRoute()
      await loadMessages()
    }
    poller = setInterval(refresh, 4000)
  } catch (error: any) {
    toast.error(error?.response?.data?.message || 'Không mở được hộp thư')
  }
})

onBeforeUnmount(() => {
  if (poller) clearInterval(poller)
})
</script>

<style scoped>
.chat-shell { display: grid; grid-template-columns: minmax(260px, 340px) 1fr; min-height: 620px; height: calc(100vh - 210px); border: 1px solid #e5e7eb; border-radius: 14px; overflow: hidden; background: #fff; box-shadow: 0 8px 26px rgb(15 23 42 / 6%); }
.conversation-panel { border-right: 1px solid #e5e7eb; overflow-y: auto; background: #f8fafc; }
.panel-header, .message-header { min-height: 72px; padding: 14px 16px; border-bottom: 1px solid #e5e7eb; background: #fff; }
.panel-header { display: flex; align-items: center; justify-content: space-between; position: sticky; top: 0; z-index: 2; }
.panel-header h2 { margin: 0; font-size: 20px; }
.panel-header span, .message-header small { display: block; color: #64748b; font-size: 12px; }
.conversation-row { position: relative; display: flex; width: 100%; align-items: center; gap: 11px; padding: 13px 14px; border: 0; border-bottom: 1px solid #eef2f7; background: transparent; text-align: left; cursor: pointer; }
.conversation-row:hover, .conversation-row.active { background: #fff1f2; }
.conversation-row.active { box-shadow: inset 3px 0 #e11d48; }
.conversation-copy { min-width: 0; flex: 1; }
.conversation-title { display: flex; justify-content: space-between; gap: 8px; }
.conversation-title strong, .conversation-preview { display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.conversation-title time, .message-bubble time { color: #94a3b8; font-size: 11px; white-space: nowrap; }
.conversation-preview { color: #64748b; font-size: 13px; margin-top: 3px; }
.unread-badge { min-width: 20px; padding: 1px 5px; border-radius: 12px; background: #e11d48; color: #fff; font-size: 11px; text-align: center; }
.message-panel { min-width: 0; display: flex; flex-direction: column; }
.message-header { display: flex; align-items: center; gap: 11px; }
.message-list { flex: 1; overflow-y: auto; padding: 18px; background: #f8fafc; }
.message-row { display: flex; margin: 7px 0; justify-content: flex-start; }
.message-row.mine { justify-content: flex-end; }
.message-bubble { max-width: min(72%, 580px); padding: 9px 12px 6px; border-radius: 14px 14px 14px 4px; background: #fff; border: 1px solid #e5e7eb; box-shadow: 0 1px 2px rgb(15 23 42 / 5%); }
.message-row.mine .message-bubble { border: 0; border-radius: 14px 14px 4px; background: #e11d48; color: #fff; }
.message-bubble p { margin: 0; white-space: pre-wrap; overflow-wrap: anywhere; }
.message-bubble time { display: block; margin-top: 3px; text-align: right; }
.message-row.mine time { color: #ffe4e6; }
.composer { display: flex; align-items: flex-end; gap: 10px; padding: 13px 16px; border-top: 1px solid #e5e7eb; background: #fff; }
.center-state, .select-state { display: flex; align-items: center; justify-content: center; min-height: 180px; }
.select-state { flex: 1; flex-direction: column; color: #64748b; text-align: center; }
.select-state h3 { margin: 8px 0 2px; color: #334155; }
.select-state p { margin: 0; }
.select-icon { font-size: 46px; }
.empty-state { margin-top: 52px; }
@media (max-width: 760px) {
  .chat-shell { grid-template-columns: 110px 1fr; height: calc(100vh - 150px); min-height: 520px; }
  .panel-header { display: block; padding: 10px; }
  .panel-header h2 { font-size: 16px; }
  .panel-header button { margin-top: 6px; }
  .conversation-row { padding: 10px 7px; justify-content: center; }
  .conversation-row :deep(.ant-avatar) { display: none; }
  .conversation-title { display: block; }
  .conversation-title time { display: none; }
  .conversation-preview { font-size: 11px; }
  .unread-badge { position: absolute; right: 3px; top: 4px; }
  .message-list { padding: 10px; }
  .message-bubble { max-width: 88%; }
}
</style>
