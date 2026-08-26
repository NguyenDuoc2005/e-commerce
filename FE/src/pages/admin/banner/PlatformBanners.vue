<template>
  <div class="banner-admin-page">
    <div class="page-head">
      <div><h2>Banner trang chu</h2><p>Quan ly banner quang cao tren storefront marketplace.</p></div>
      <a-button type="primary" @click="openCreate">Them banner</a-button>
    </div>
    <a-table row-key="id" :columns="columns" :data-source="banners" :loading="loading" :pagination="false">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'preview'"><img :src="record.imageUrl" :alt="record.title" class="banner-preview" /></template>
        <template v-else-if="column.key === 'active'"><a-tag :color="record.active ? 'green' : 'default'">{{ record.active ? 'Dang hien thi' : 'Da an' }}</a-tag></template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button size="small" @click="openEdit(record)">Sua</a-button>
            <a-popconfirm title="Xoa banner nay?" @confirm="remove(record.id)"><a-button size="small" danger>Xoa</a-button></a-popconfirm>
          </a-space>
        </template>
        <template v-else-if="column.dataIndex">{{ record[column.dataIndex] ?? '—' }}</template>
      </template>
    </a-table>
    <a-modal v-model:open="modalOpen" :title="editingId ? 'Sua banner' : 'Them banner'" :confirm-loading="saving" ok-text="Luu" cancel-text="Huy" @ok="save">
      <a-form layout="vertical">
        <a-form-item label="Tieu de" required><a-input v-model:value="form.title" /></a-form-item>
        <a-form-item label="URL anh" required><a-input v-model:value="form.imageUrl" /></a-form-item>
        <a-form-item label="Lien ket"><a-input v-model:value="form.targetUrl" placeholder="/san-pham" /></a-form-item>
        <a-form-item label="Thu tu"><a-input-number v-model:value="form.sortOrder" :min="0" /></a-form-item>
        <a-form-item label="Hien thi"><a-switch v-model:checked="form.active" /></a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import { createBanner, deleteBanner, getAdminBanners, updateBanner, type PlatformBanner, type PlatformBannerPayload } from '@/services/api/admin/banner.api'

const banners = ref<PlatformBanner[]>([])
const loading = ref(false)
const saving = ref(false)
const modalOpen = ref(false)
const editingId = ref('')
const form = reactive<PlatformBannerPayload>({ title: '', imageUrl: '', targetUrl: '', position: 'HOME_HERO', active: true, sortOrder: 0 })
const columns = [
  { title: 'Anh', key: 'preview', width: 180 }, { title: 'Tieu de', dataIndex: 'title', key: 'title' },
  { title: 'Vi tri', dataIndex: 'position', key: 'position' }, { title: 'Thu tu', dataIndex: 'sortOrder', key: 'sortOrder', width: 90 },
  { title: 'Trang thai', key: 'active', width: 130 }, { title: 'Thao tac', key: 'action', width: 150 }
]

const fetchBanners = async () => {
  loading.value = true
  try { banners.value = (await getAdminBanners()).data ?? [] }
  catch (error: any) { message.error(error?.response?.data?.message ?? 'Khong tai duoc banner') }
  finally { loading.value = false }
}
const resetForm = () => Object.assign(form, { title: '', imageUrl: '', targetUrl: '', position: 'HOME_HERO', active: true, sortOrder: 0, startAt: undefined, endAt: undefined })
const openCreate = () => { editingId.value = ''; resetForm(); modalOpen.value = true }
const openEdit = (banner: PlatformBanner) => {
  editingId.value = banner.id
  Object.assign(form, { title: banner.title, imageUrl: banner.imageUrl, targetUrl: banner.targetUrl ?? '', position: banner.position, active: banner.active, sortOrder: banner.sortOrder, startAt: banner.startAt, endAt: banner.endAt })
  modalOpen.value = true
}
const save = async () => {
  if (!form.title.trim() || !form.imageUrl.trim()) { message.warning('Vui long nhap tieu de va URL anh'); return }
  saving.value = true
  try {
    if (editingId.value) await updateBanner(editingId.value, form); else await createBanner(form)
    message.success('Da luu banner'); modalOpen.value = false; await fetchBanners()
  } catch (error: any) { message.error(error?.response?.data?.message ?? 'Khong luu duoc banner') }
  finally { saving.value = false }
}
const remove = async (id: string) => {
  try { await deleteBanner(id); message.success('Da xoa banner'); await fetchBanners() }
  catch (error: any) { message.error(error?.response?.data?.message ?? 'Khong xoa duoc banner') }
}
onMounted(fetchBanners)
</script>

<style scoped>
.banner-admin-page { padding: 20px; }
.page-head { display: flex; justify-content: space-between; align-items: center; gap: 16px; margin-bottom: 18px; }
.page-head h2 { margin: 0 0 4px; font-size: 24px; }
.page-head p { margin: 0; color: #667085; }
.banner-preview { width: 150px; aspect-ratio: 12 / 5; object-fit: cover; border-radius: 6px; }
@media (max-width: 640px) { .page-head { align-items: flex-start; flex-direction: column; } }
</style>
