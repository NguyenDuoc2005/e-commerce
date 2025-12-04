<template>
  <div class="jp-top-banner w-100">
    <img src="https://bizweb.dktcdn.net/100/347/092/themes/708609/assets/banner.jpg?1753792346461"
      alt="Banner quảng cáo" class="jp-top-banner-img" />
  </div>

  <header class="jp-header">
    <!-- TOP BAR -->
    <div class="jp-top">
      <!-- Hamburger (mobile) -->
      <button class="jp-hamburger d-lg-none" @click="toggleSideMenu">
        <span></span><span></span><span></span>
      </button>
      <!-- Logo -->
      <a href="/" class="d-flex align-items-center jp-logo-area ps-4" style="min-width:100px;">
        <img src="/images/logo.jpg" alt="Logo" class="jp-logo-img me-3"
          style="height:48px;width:48px;object-fit:cover;">
        <div class="position-relative d-none d-md-inline-block" style="display: inline-block;">
          <span class="fw-bold text-uppercase"
            style="font-size:1.7rem; letter-spacing:0.04em; color:#D32F2F; text-shadow: 1px 1px 6px #ccc; display:inline-block; padding-bottom:10px;">
            GLAMESOLE
            <span
              style="position:absolute; left:0; right:0; bottom:0; height:5px; background:#D32F2F; border-radius:3px; width:100%; display:block; content:'';"></span>
          </span>
        </div>
      </a>

      <!-- Nút tìm kiếm chỉ hiển thị trên mobile -->
      <button class="jp-icon-round d-lg-none" @click="showMobileSearch = !showMobileSearch">
        <SearchOutlined />
      </button>
      <!-- Search Desktop với màu #EA2F38 -->
      <form class="d-none d-lg-flex align-items-center flex-grow-1" @submit.prevent="onSearch"
        style="max-width:600px; width:100%;">
        <div class="input-group w-100">
          <!-- <input type="text" class="form-control" placeholder="Tìm kiếm..." v-model="keyword"
            style="height:44px; font-size:1.08rem; border-radius:24px 0 0 24px; outline:none; box-shadow:none; border:1.5px solid #EA2F38;"
            @focus="e => { e.target.style.outline = 'none'; e.target.style.boxShadow = 'none'; }" /> -->
          <!-- <button class="d-flex align-items-center justify-content-center" type="submit"
            style="background:#EA2F38; border:none; border-radius:0 24px 24px 0; width:48px; height:44px; outline:none; box-shadow:none;"
            @focus="e => { e.target.style.outline = 'none'; e.target.style.boxShadow = 'none'; }">
            <SearchOutlined style="font-size:1.27rem; color:#fff;" />
          </button> -->
        </div>
      </form>

      <!-- Hotline + ICONS -->
      <div class="jp-top-right">
        <div class="d-none d-lg-flex align-items-center bg-white border rounded-pill ps-3"
          style="border-color:#EA2F38; gap:12px; min-width:0;">
          <div class="d-flex flex-column justify-content-center align-items-center" style="line-height:1.18;">
            <span style="color:#EA2F38; font-size:1rem; font-weight:500; letter-spacing:0.06em;">Tư vấn bán hàng</span>
            <span style="font-size:1.11rem; color:#333; margin-top:2px; letter-spacing:0.03em; font-weight:600;">Gọi
              ngay 0366994511</span>
          </div>
          <button class="rounded-circle d-flex align-items-center justify-content-center p-0 ms-2"
            style="width:44px; height:44px; background:#EA2F38; border:none;" @click="callHotline"
            aria-label="Gọi điện">
            <PhoneOutlined style="font-size:1.22rem; color:#fff;" />
          </button>
          <div v-if="isLogin" class="jp-account-status text-nowrap me-2"
            style="color: #ea2f38; font-size: 1rem; font-weight: 500">
            Xin chào, {{ userLogin.fullName || "Người dùng" }}
            <span style="color: #000">{{
              JSON.stringify(userLogin.value)
              }}</span>
            <!-- Debug -->
          </div>
        </div>

        <!-- Account dropdown -->
        <div class="jp-account-wrap" ref="accountRef">
          <button class="border rounded-circle d-flex align-items-center justify-content-center p-0 ms-2"
            style="width:44px; height:44px; background:#EA2F38; border-color:#EA2F38;" @click="toggleDropdown"
            :aria-expanded="showDropdown">
            <UserOutlined style="font-size:1.5rem; color:#fff;" />
          </button>
          <transition name="fade">
            <div v-if="showDropdown" class="jp-account-dropdown">
              <template v-if="isLogin">
                <div class="jp-account-info">
                  <img v-if="userLogin.pictureUrl" :src="userLogin.pictureUrl" alt="avatar" class="jp-avatar" />
                  <UserOutlined v-else class="jp-avatar-fallback" />
                  <div>
                    <div class="jp-account-name">{{ userLogin.fullName }}</div>
                    <div class="jp-account-email">{{ userLogin.email }}</div>
                  </div>
                </div>
                <a href="/thong-tin-ca-nhan" class="jp-dropdown-link">Trang cá nhân</a>
                <a href="/don-mua" class="jp-dropdown-link">Đơn mua</a>
                <a href="/tra-cuu" class="jp-dropdown-link">Tra cứu đơn hàng</a>
                <button class="jp-dropdown-link logout" @click="logout">Đăng xuất</button>
              </template>
              <template v-else>
                <a href="/tra-cuu" class="jp-dropdown-link">Tra cứu đơn hàng</a>
                <a href="/login" class="jp-dropdown-link">Đăng nhập</a>
                <a href="/register" class="jp-dropdown-link">Đăng ký</a>
              </template>
            </div>
          </transition>
        </div>
        <button class="rounded-circle d-flex align-items-center justify-content-center position-relative p-0 ms-2"
          style="width:44px; height:44px; background:#EA2F38; border:none;" @click="goToCart" aria-label="Giỏ hàng">
          <ShoppingCartOutlined style="font-size:1.28rem; color:#fff;" />
          <span v-if="cartItemCount > 0" class="position-absolute badge rounded-pill shadow"
            style="display: flex; align-items: center; justify-content: center; font-size:0.78rem; font-weight:600; min-width:20px; height:20px; padding:0 5px; background:#e53935; color:#fff; border:2px solid #e53935; right: -9px; top: -8px; box-shadow:0 2px 8px #e539354d; z-index:1;">
            {{ cartItemCount }}
          </span>
        </button>
      </div>
    </div>

    <!-- Search mobile -->
    <div v-if="showMobileSearch" class="jp-mobile-search d-lg-none">
      <form @submit.prevent="onSearch" class="w-100 d-flex align-items-center">
        <input v-model="keyword" class="jp-mobile-search-input" type="text" placeholder="Tìm kiếm..." />
        <button type="submit" class="jp-icon-round ms-2">
          <SearchOutlined />
        </button>
        <button class="jp-icon-round ms-2" type="button" @click="showMobileSearch = false">✕</button>
      </form>
    </div>

    <!-- MENU BAR -->
    <nav class="jp-menu" :class="{ sticky: isMenuSticky }" ref="menuBar">
      <ul class="jp-menu-list">
        <!-- Trang chủ fix cứng, luôn luôn ở đầu -->
        <li class="jp-menu-item" key="menu-trang-chu">
          <a href="/" class="jp-menu-link">TRANG CHỦ</a>
        </li>

        <!-- Các mục thương hiệu duyệt bình thường -->
        <li class="jp-menu-item">
          <a href="/san-pham" class="jp-menu-link">Sản phẩm</a>
        </li>

        <li class="jp-menu-item" key="menu-trang-chu">
          <a href="/gioi-thieu" class="jp-menu-link">GIỚI THIỆU</a>
        </li>

        <li class="jp-menu-item" key="menu-trang-chu">
          <a href="/lien-he" class="jp-menu-link">LIÊN HỆ</a>
        </li>

        <li class="jp-menu-item" key="menu-trang-chu">
          <a href="/tra-cuu" class="jp-menu-link">TRA CỨU ĐƠN HÀNG</a>
        </li>
      </ul>
    </nav>

    <!-- SIDEMENU MOBILE -->
    <div v-if="showSideMenu" class="jp-side-menu-overlay" @click="closeSideMenu"></div>
    <transition name="slide-left">
      <aside v-if="showSideMenu" class="jp-side-menu">
        <div class="jp-side-menu-header">
          <template v-if="!isLogin">
            <button class="jp-side-auth" @click="goToLogin">Đăng nhập</button>
            <button class="jp-side-auth ms-2" @click="goToRegister">Đăng ký</button>
          </template>
          <template v-else>
            <div class="jp-side-user">
              <img v-if="userLogin.pictureUrl" :src="userLogin.pictureUrl" alt="avatar" class="jp-avatar" />
              <UserOutlined v-else class="jp-avatar-fallback" />
              <div>
                <div class="jp-account-name">{{ userLogin.fullName }}</div>
                <div class="jp-account-email">{{ userLogin.email }}</div>
              </div>
            </div>
          </template>
          <button class="jp-close-btn" @click="closeSideMenu">✕</button>
        </div>
        <ul class="jp-side-menu-list">
          <!-- Trang chủ fix cứng trên đầu -->
          <li class="jp-side-menu-item" key="side-menu-trang-chu">
            <a href="/" class="jp-side-menu-link">TRANG CHỦ</a>
          </li>
          <!-- Duyệt các thương hiệu khác -->
          <li v-for="item in menuItems" :key="item.id" class="jp-side-menu-item">
            <a href="#" class="jp-side-menu-link" @click.prevent="() => { onMenuClick(item.id); closeSideMenu(); }">{{
              item.ten }}</a>
          </li>
          <li class="jp-menu-item" key="menu-trang-chu">
            <a href="/gioi-thieu" class="jp-menu-link">GIỚI THIỆU</a>
          </li>
          <li class="jp-menu-item" key="menu-trang-chu">
            <a href="/lien-he" class="jp-menu-link">LIÊN HỆ</a>
          </li>
        </ul>
      </aside>
    </transition>
  </header>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'
import { SearchOutlined, ShoppingCartOutlined, UserOutlined, PhoneOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { router } from '@/routes/router'
import { useRoute } from 'vue-router'
import { GetThuongHieus } from '@/services/api/admin/thuonghieu.api'
import { localStorageAction } from '@/utils/storage'
import { USER_INFO_STORAGE_KEY, CART_STORAGE_KEY } from '@/constants/storageKey'
import { getAllCart, type requestCart } from '@/services/api/permitall/cart/cart'
import { toast } from 'vue3-toastify'
import { GetAllThuongHieusTrangChu } from '@/services/api/permitall/thuonghieu/pmthuonghieu.api'

// Hàm gửi sự kiện tùy chỉnh khi giỏ hàng thay đổi
const dispatchCartUpdate = () => {
  window.dispatchEvent(new Event('cartUpdated'))
}

// Lấy giỏ hàng từ server khi đã đăng nhập
const fetchCartFromServer = async () => {
  if (!idUser?.userId) return
  const param: requestCart = { idUser: idUser.userId }
  try {
    const res = await getAllCart(param)
    // Đếm số lượng sản phẩm duy nhất dựa trên idSPCT
    console.log(res)
    const uniqueItems = new Set(res.data.map((item) => item.idSPCT))
    cartItemCount.value = res.data.length
    console.log('Unique items from server:', uniqueItems.size) // Debug
  } catch (error) {
    console.error('Lỗi khi lấy giỏ hàng từ server:', error)
    // toast.error('Không thể tải số lượng giỏ hàng.')
    cartItemCount.value = 0
  }
}

// Lấy giỏ hàng tạm từ localStorage khi chưa đăng nhập
const fetchCartFromLocalStorage = () => {
  const tempCart = localStorageAction.get(CART_STORAGE_KEY) || []
  if (!Array.isArray(tempCart)) return
  // Đếm số lượng sản phẩm duy nhất dựa trên idChiTietSanPham
  const uniqueItems = new Set(tempCart.map((item: any) => item.idChiTietSanPham))
  console.log(uniqueItems)
  cartItemCount.value = uniqueItems.size
  console.log('Unique items from local:', uniqueItems.size) // Debug
}
const keyword = ref('')
const cartItemCount = ref(0) // Khởi tạo số lượng là 0
const showMobileSearch = ref(false)
const showSideMenu = ref(false)
const isMenuSticky = ref(false)
const menuBar = ref<HTMLElement | null>(null)
const accountRef = ref<HTMLElement | null>(null)
const showDropdown = ref(false)
const authStore = useAuthStore()
const userLogin = computed(() => authStore.user || {})

const user = localStorageAction.get(USER_INFO_STORAGE_KEY)
const isLogin = computed(() =>
  !!userLogin.value &&
  !!userLogin.value.fullName &&
  user.role === 'USERS'
)

const menuItems = ref([])
const idUser = localStorageAction.get(USER_INFO_STORAGE_KEY)

// Cập nhật số lượng giỏ hàng khi trạng thái đăng nhập thay đổi
watch(isLogin, (newValue) => {
  if (newValue) {
    fetchCartFromServer()
  } else {
    fetchCartFromLocalStorage()
  }
})

// Theo dõi sự kiện thay đổi giỏ hàng
const handleCartUpdate = () => {
  if (isLogin.value) {
    fetchCartFromServer()
  } else {
    fetchCartFromLocalStorage()
  }
}

// Lấy danh sách thương hiệu
const fetchCategory = async () => {
  try {
    const payload = { page: 1, size: 100000 }
    const res = await GetAllThuongHieusTrangChu(payload)
    menuItems.value = res.data.content
  } catch (error) {
    console.error('Lỗi lấy thương hiệu:', error)
    menuItems.value = []
  }
}

onMounted(() => {
  fetchCategory()
  // Khởi tạo số lượng giỏ hàng dựa trên trạng thái đăng nhập
  if (isLogin.value) {
    fetchCartFromServer()
  } else {
    fetchCartFromLocalStorage()
  }
  // Lắng nghe sự kiện thay đổi giỏ hàng
  window.addEventListener('cartUpdated', handleCartUpdate)
  window.addEventListener('storage', handleCartUpdate) // Theo dõi thay đổi localStorage
  window.addEventListener('scroll', handleScroll)
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  window.removeEventListener('cartUpdated', handleCartUpdate)
  window.removeEventListener('storage', handleCartUpdate)
  window.removeEventListener('scroll', handleScroll)
  document.removeEventListener('click', handleClickOutside)
})

const route = useRoute()
const onMenuClick = (idThuongHieu: string) => {
  router.push({
    path: '/san-pham',
  })
}

const onSearch = () => {
  router.push({
    path: '/san-pham',
    query: { ...route.query, keyword: keyword.value }
  })
}

const goToLogin = () => { showDropdown.value = false; showSideMenu.value = false; window.location.href = '/login' }
const goToRegister = () => { showDropdown.value = false; showSideMenu.value = false; window.location.href = '/register' }
const goToCart = () => { showDropdown.value = false; showSideMenu.value = false; window.location.href = '/gio-hang' }
const callHotline = () => window.open('tel:0984843218')
const logout = () => {
  authStore.logout()
  showDropdown.value = false
  showSideMenu.value = false
  // Sau khi đăng xuất, cập nhật lại số lượng từ localStorage
  setTimeout(() => {
    fetchCartFromLocalStorage()
    window.location.reload()
  }, 300)
}

// Sticky menu khi scroll
const handleScroll = () => {
  if (!menuBar.value) return
  isMenuSticky.value = window.scrollY > 110
}

const toggleDropdown = (e?: Event) => {
  if (e) e.preventDefault()
  showDropdown.value = !showDropdown.value
}

const handleClickOutside = (event: MouseEvent) => {
  if (accountRef.value && !accountRef.value.contains(event.target as Node)) {
    showDropdown.value = false
  }
}

const toggleSideMenu = () => { showSideMenu.value = !showSideMenu.value }
const closeSideMenu = () => { showSideMenu.value = false }
</script>
<style scoped>
/* Giữ nguyên CSS hiện tại */
.jp-header {
  width: 100%;
  background: #fff;
  box-shadow: 0 1px 7px #f4433620;
  position: relative;
  z-index: 100;
}

.jp-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 18px;
  min-height: 86px;
  background: #fff;
  border-bottom: none;
  gap: 10px;
}

.jp-hamburger {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border: none;
  background: none;
  padding: 0 6px;
  margin-right: 7px;
  height: 46px;
}

.jp-hamburger span {
  width: 32px;
  height: 4px;
  background: #374151;
  margin: 4px 0;
  border-radius: 2px;
}

.jp-logo-area {
  display: flex;
  align-items: center;
  text-decoration: none;
}

.jp-logo-img {
  height: 60px;
  width: 60px;
  margin-right: 13px;
  object-fit: contain;
}

.jp-logo-text {
  display: flex;
  flex-direction: column;
}

.jp-logo-title {
  font-size: 1.32rem;
  font-weight: 600;
  color: #23282d;
  letter-spacing: 0.04em;
  line-height: 1.17;
  text-transform: uppercase;
  /* Nếu muốn logo chữ in hoa gọn */
}


.jp-logo-desc {
  font-size: 1.1rem;
  font-weight: 400;
  color: #e41b25;
  margin-left: 2px;
  margin-top: -1px;
}

.jp-search-wrap {
  flex: 1 1 490px;
  max-width: 600px;
  margin: 0 32px;
  background: #fff;
  border-radius: 32px;
  border: 1.6px solid #eee;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 10px #e5e7eb16;
}

.jp-search-input {
  border: none;
  height: 50px;
  font-size: 1.15rem;
  background: #fff;
  color: #222;
  flex: 1;
  padding-left: 24px;
  border-radius: 32px 0 0 32px;
}

.jp-search-input:focus {
  outline: none;
  background: #f6fcff;
}

.jp-search-btn {
  background: #fff;
  color: #EA2F38;
  border: none;
  border-radius: 0 32px 32px 0;
  height: 46px;
  width: 54px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  margin-right: 6px;
  transition: background 0.19s, color 0.13s;
}

.jp-search-btn:hover {
  background: #EA2F38;
  color: #fff;
}

.jp-top-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.jp-hotline {
  border: 1.7px solid #EA2F38;
  border-radius: 26px;
  background: #fff;
  padding: 6px 26px 6px 14px;
  margin-right: 5px;
  min-width: 172px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 48px;
  line-height: 1.13;
}

.jp-hotline-label {
  color: #EA2F38;
  font-size: 1.04rem;
  font-weight: 500;
}

.jp-hotline-phone {
  color: #EA2F38;
  font-size: 1.04rem;
  font-weight: bold;
  margin-top: 2px;
}

.jp-icon-round {
  background: #EA2F38;
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.45rem;
  position: relative;
  margin-left: 7px;
  transition: background 0.18s, color 0.15s, box-shadow .16s;
}

.jp-icon-round:hover {
  background: #fff;
  color: #EA2F38;
  box-shadow: 0 1px 8px #EA2F3833;
}

.jp-cart-badge {
  position: absolute;
  top: 6px;
  right: 8px;
  background: #fff;
  color: #EA2F38;
  border: 2px solid #EA2F38;
  border-radius: 999px;
  font-size: 0.95rem;
  font-weight: bold;
  padding: 1px 7px;
  min-width: 21px;
  text-align: center;
  z-index: 3;
}

/* Account dropdown desktop */
.jp-account-wrap {
  position: relative;
}

.jp-account-btn {
  background: #EA2F38;
  color: #fff;
  border: none;
  border-radius: 50%;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.45rem;
  margin-left: 7px;
  transition: background 0.18s;
}

.jp-account-btn:hover {
  background: #fff;
  color: #EA2F38;
  box-shadow: 0 1px 8px #EA2F3833;
}

.jp-account-dropdown {
  position: absolute;
  right: 0;
  top: 120%;
  min-width: 205px;
  background: #fff;
  box-shadow: 0 3px 18px #0001;
  border-radius: 10px;
  padding: 13px 0 9px 0;
  z-index: 1111;
  animation: fadeIn .21s;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.jp-account-info {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 0 16px 10px 16px;
  border-bottom: 1px solid #eee;
  margin-bottom: 10px;
}

.jp-account-name {
  font-weight: bold;
  color: #EA2F38;
}

.jp-account-email {
  font-size: 0.93rem;
  color: #777;
}

.jp-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
}

.jp-avatar-fallback {
  font-size: 2.1rem;
  color: #EA2F38;
  border-radius: 50%;
  background: #ffeaea;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.jp-dropdown-link {
  display: block;
  padding: 10px 20px;
  color: #222;
  text-decoration: none;
  font-size: 1.04rem;
  background: none;
  border: none;
  text-align: left;
  width: 100%;
  cursor: pointer;
  transition: background .12s, color .12s;
}

.jp-dropdown-link:hover,
.jp-dropdown-link.logout:hover {
  background: #EA2F3833;
  color: #EA2F38;
}

.jp-dropdown-link.logout {
  color: #EA2F38;
  border-top: 1px solid #f1dede;
  margin-top: 5px;
}

/* Side menu user info */
.jp-side-user {
  display: flex;
  align-items: center;
  gap: 10px;
}

.jp-side-logout {
  background: none;
  border: none;
  color: #EA2F38;
  font-weight: bold;
  margin-left: 10px;
  font-size: 1rem;
  cursor: pointer;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity .2s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ============= MENU DƯỚI ============= */
.jp-menu {
  background: #EA2F38;
  min-height: 54px;
  height: 54px;
  box-shadow: 0 1px 8px #f4433615;
  padding: 0 0 0 0;
  margin: 0;
  display: flex;
  align-items: center;
  position: relative;
  transition: top 0.18s, box-shadow 0.13s;
}

.jp-menu.sticky {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1020;
  box-shadow: 0 5px 22px #EA2F38;
  animation: stickyMenu .26s;
}

@keyframes stickyMenu {
  from {
    top: -60px;
    opacity: 0;
  }

  to {
    top: 0;
    opacity: 1;
  }
}

.jp-menu-list {
  margin: 0;
  padding: 0 0 0 24px;
  list-style: none;
  width: 100%;
  height: 54px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.jp-menu-link:hover {
  color: #000000;
  text-decoration: none;

}

.jp-menu-item {
  margin-right: 18px;
  height: 54px;
  display: flex;
  align-items: center;
}

.jp-menu-link {
  display: flex;
  align-items: center;
  color: #fff;
  font-size: 1.19rem;
  font-weight: bold;
  text-transform: uppercase;
  height: 54px;
  padding: 0 20px;
  text-decoration: none;
  border-radius: 0;
  position: relative;
  transition: background 0.13s, color 0.13s;
}



.jp-menu-link .anticon {
  margin-left: 7px;
  font-size: 1em;
}



.jp-menu-item:last-child {
  margin-right: 0;
}

/* ============= MOBILE ============= */
@media (max-width: 1200px) {
  .jp-top {
    padding: 0 7px;
  }

  .jp-search-wrap {
    margin: 0 7px;
    max-width: 350px;
  }

  .jp-logo-img {
    height: 38px;
    width: 38px;
  }

  .jp-logo-title {
    font-size: 1.14rem;
  }

  .jp-hotline {
    min-width: 92px;
    padding: 3px 8px 3px 8px;
  }

  .jp-menu-list {
    padding-left: 0;
  }

  .jp-menu-link {
    font-size: 1.05rem;
    padding: 0 8px;
  }

  .jp-menu,
  .jp-menu-link,
  .jp-menu-list {
    height: 45px;
    min-height: 45px;
  }
}

@media (max-width: 991px) {
  .jp-top {
    min-height: 54px;
  }

  .jp-hamburger {
    display: flex;
  }

  .jp-logo-img {
    height: 34px;
    width: 34px;
  }

  .jp-logo-title {
    font-size: 1.07rem;
  }

  .jp-hotline,
  .jp-hotline-label,
  .jp-hotline-phone {
    display: none !important;
  }

  .jp-icon-round {
    width: 40px;
    height: 40px;
    font-size: 1.08rem;
  }

  .jp-search-wrap {
    display: none !important;
  }

  .jp-menu {
    min-height: 45px;
    height: 45px;
  }

  .jp-menu-list,
  .jp-menu-link {
    height: 45px;
    font-size: 0.92rem;
  }
}

.jp-mobile-search {
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  background: #fff;
  z-index: 110;
  padding: 8px 9px;
  border-bottom: 1.5px solid #eee;
  box-shadow: 0 2px 8px #EA2F3820;
}

.jp-mobile-search-input {
  flex: 1 1 auto;
  height: 36px;
  border: 1.1px solid #EA2F38;
  border-radius: 22px;
  padding: 0 13px;
  font-size: 1.01rem;
  outline: none;
}

.jp-side-menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #0000003a;
  z-index: 1010;
}

.jp-side-menu {
  position: fixed;
  top: 0;
  left: 0;
  width: 270px;
  height: 100vh;
  background: #fff;
  z-index: 1030;
  box-shadow: 2px 0 22px #EA2F383a;
  display: flex;
  flex-direction: column;
  animation: slideLeft .21s;
}

@keyframes slideLeft {
  from {
    left: -265px;
    opacity: 0;
  }

  to {
    left: 0;
    opacity: 1;
  }
}

.jp-side-menu-header {
  padding: 16px 10px 7px 19px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.jp-side-auth {
  background: #fff;
  color: #EA2F38;
  border: 1px solid #EA2F38;
  border-radius: 8px;
  font-weight: bold;
  padding: 6px 15px;
  font-size: 1.04rem;
  transition: background .14s;
}

.jp-side-auth:hover {
  background: #ffeaea;
}

.jp-close-btn {
  font-size: 1.7rem;
  display: flex;
  position: absolute;
  left: 85%;
  top: 10px;
  border: none;
  background: none;
  color: #EA2F38;
}

.jp-side-menu-list {
  list-style: none;
  margin: 0;
  padding: 6px 0;
}

.jp-side-menu-item {
  border-bottom: 1px solid #f3f3f3;
}

.jp-side-menu-link {
  display: flex;
  align-items: center;
  font-size: 1.05rem;
  font-weight: 500;
  color: #222;
  padding: 12px 19px;
  text-decoration: none;
  transition: background .13s, color .13s;
}

.jp-side-menu-link:hover {
  background: #f8e6e7;
  color: #EA2F38;
}

.jp-side-menu-link .anticon {
  margin-left: 6px;
}

@media (max-width: 991.98px) {
  .jp-menu {
    display: none !important;
  }
}

.jp-top-banner {
  width: 100%;
  min-height: 110px;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-bottom: 1px solid #eee;
}

.jp-top-banner-img {
  width: 100%;
  min-height: 110px;
  object-fit: cover;
}

@media (max-width: 991px) {

  .jp-top-banner,
  .jp-top-banner-img {
    min-height: 70px;
    height: 70px;
  }
}
</style>
