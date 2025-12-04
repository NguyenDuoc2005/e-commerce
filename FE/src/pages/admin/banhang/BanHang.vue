<template>
  <div class="breadcrumb-section">
    <BreadcrumbDefault :pageTitle="'Bán hàng'" :routes="[
      { path: '/ban-hang', name: 'Bán hàng' }
    ]" />
  </div>
  <div class="main-layout">
    <div class="left-column">
      <div class="top-header">
        <div class="search-and-create-section">
          <!-- <div class="search-box-top">
            <input type="text" placeholder="Tìm kiếm hóa đơn..." class="search-input-top" />
            <button class="search-icon-top">
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-search"
                viewBox="0 0 16 16">
                <path
                  d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001q.044.06.085.12c.047.061.096.119.146.177l3.85 3.85a1 1 0 0 0 1.415-1.415zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0" />
              </svg>
            </button>
          </div> -->
          <button class="btn-create-new-invoice" @click="createInvoice">
            <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-plus-lg"
              viewBox="0 0 16 16">
              <path fill-rule="evenodd"
                d="M8 2a.5.5 0 0 1 .5.5v5h5a.5.5 0 0 1 0 1h-5v5a.5.5 0 0 1-1 0v-5h-5a.5.5 0 0 1 0-1h5v-5A.5.5 0 0 1 8 2" />
            </svg>
            Tạo hóa đơn mới
          </button>
        </div>
      </div>
      <div class="card">
        <div class="card-header">
          <h3>Hóa đơn chờ</h3>
        </div>
        <div class="pending-invoices-container">
          <div v-for="tab in tabs" :key="tab.id" :class="['pending-invoice-card', { active: activeTab === tab.id }]"
            @click="clickkActiveTab(tab.id, tab.idHD, tab.loaiHoaDon)">
            <div class="invoice-id">{{ tab.ma }}</div>
            <div class="invoice-status">Chờ xử lý</div>
            <div class="invoice-product-count">{{ tab.soLuong == null ? 0 : tab.soLuong }} sản phẩm</div>
            <a-popconfirm title="Bạn có chắc chắn muốn hủy hóa đơn này?" ok-text="Đồng ý" cancel-text="Hủy"
              @confirm="() => huy(tab.idHD)" @cancel="() => { }">
              <button class="delete-invoice-btn">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-trash"
                  viewBox="0 0 16 16">
                  <path
                    d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z" />
                  <path
                    d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H9.5a1 1 0 0 1 1 1H14a1 1 0 0 1 1 1zM4.118 4 .5 3.993 1.5 2h13l-1 2H4.118z" />
                </svg>
              </button>
            </a-popconfirm>
          </div>
        </div>
      </div>

      <div class="card mt-4">
        <div class="card-header">
          <h3>Giỏ hàng</h3>
        </div>
        <div class="tab-content-display" v-if="activeTab">
          <div class="actions-add-product">
            <button class="btn-add-product" style="margin-right: 10px; width: 150px;"
              @click="openProductSelectionModal">
              <p class="quet">Chọn sản phẩm</p>
            </button>
            <button class="btn-add-product" @click="openQrModal">
              <p class="quet">
                <QrcodeOutlined /> Quét QR
              </p>
            </button>
          </div>
          <div v-if="state.gioHang.length > 0">
            <a-table :columns="columnsGiohang" :data-source="state.gioHang" :scroll="{ y: 240 }"
              :pagination="{ pageSize: 5, hideOnSinglePage: true }">
              <template #bodyCell="{ column, record }">
                <div v-if="column.key === 'stt'">
                  {{ state.gioHang.indexOf(record) + 1 }}
                </div>
                <template v-if="column.key === 'anh'">
                  <div class="center-cell">
                    <img :src="record.anh" class="anh" style="width: 50px; height: 50px; border-radius: 50%" />
                  </div>
                </template>

                <template v-if="column.key === 'soLuong'">
                  <div class="center-cell">
                    <button class="quantity-btn" @click="decreaseQuantity(record.idHDCT, record.id)">-</button>
                    <input type="text" v-model="record.soLuong" class="quantity-input" readonly />
                    <button class="quantity-btn" @click="increaseQuantity(record.idHDCT, record.id)">+</button>
                  </div>
                </template>

                <div v-if="column.key === 'giaBan'">
                  {{ formatCurrency(record.giaBan) }}
                </div>

                <template v-if="column.key === 'mau'">
                  <div class="center-cell">
                    <div class="color"
                      :style="{ width: '30px', height: '30px', backgroundColor: record.mau, border: '1px solid #000' }">
                    </div>
                  </div>
                </template>
                <template v-if="column.key === 'status'">
                  <div class="center-cell">
                    <b class="highlight">{{ formatCurrency(record.giaBan * record.soLuong) }}</b>
                  </div>
                </template>
                <template v-if="column.key === 'operation'">
                  <div class="d-flex gap-1 justify-content-center align-items-center">
                    <a-tooltip title="Hủy sản phẩm">
                      <a-button type="primary" @click="deleteProduc(record.id, record.idHDCT)"
                        style="color: white; justify-content: center; background-color: #EE0000;"
                        class="p-2 d-flex justify-content-center align-items-center">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor"
                          class="bi bi-trash" viewBox="0 0 16 16">
                          <path
                            d="M5.5 5.5A.5.5 0 0 1 6 6v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m2.5 0a.5.5 0 0 1 .5.5v6a.5.5 0 0 1-1 0V6a.5.5 0 0 1 .5-.5m3 .5a.5.5 0 0 0-1 0v6a.5.5 0 0 0 1 0z" />
                          <path
                            d="M14.5 3a1 1 0 0 1-1 1H13v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V4h-.5a1 1 0 0 1-1-1V2a1 1 0 0 1 1-1H9.5a1 1 0 0 1 1 1H14a1 1 0 0 1 1 1zM4.118 4 .5 3.993 1.5 2h13l-1 2H4.118z" />
                        </svg>
                      </a-button>
                    </a-tooltip>
                  </div>
                </template>
              </template>
            </a-table>
          </div>
          <div class="empty-cart" v-else>

            <p>Không có sản phẩm nào trong giỏ hàng</p>
          </div>
        </div>
      </div>
      <div v-if="isDeliveryEnabled == true" class="delivery-info-section">
        <h5 class="section-title">Thông tin người nhận</h5>
        <div class="delivery-form">
          <div class="form-grid">
            <div class="form-group">
              <label for="receiver-name">Tên người nhận <span class="required">*</span></label>
              <a-input type="text" id="receiver-name" v-model:value="deliveryInfo.tenNguoiNhan" class="input-customer"
                placeholder="Nhập tên người nhận"
                :class="{ 'input-error': !deliveryInfo.tenNguoiNhan && isDeliveryEnabled }" />
            </div>

            <div class="form-group">
              <label for="receiver-phone">Số điện thoại <span class="required">*</span></label>
              <a-input type="text" id="receiver-phone" v-model:value="deliveryInfo.sdtNguoiNhan" class="input-customer"
                placeholder="Nhập số điện thoại"
                :class="{ 'input-error': !deliveryInfo.sdtNguoiNhan && isDeliveryEnabled }" />
            </div>

            <div class="form-group">
              <label for="province">Tỉnh/Thành phố <span class="required">*</span></label>
              <a-select style="width: 100%;" v-model:value="deliveryInfo.tinhThanhPho" placeholder="Chọn tỉnh/thành phố"
                :options="provinces" @change="onProvinceChange" show-search option-filter-prop="label"
                :class="{ 'input-error': !deliveryInfo.tinhThanhPho && isDeliveryEnabled }" />
            </div>

            <div class="form-group">
              <label for="district">Quận/Huyện <span class="required">*</span></label>
              <a-select style="width: 100%;  height: 32px;" v-model:value="deliveryInfo.quanHuyen"
                placeholder="Chọn quận/huyện" :options="districts" @change="onDistrictChange" show-search
                option-filter-prop="label" :class="{ 'input-error': !deliveryInfo.quanHuyen && isDeliveryEnabled }" />
            </div>

            <div class="form-group">
              <label for="ward">Phường/Xã <span class="required">*</span></label>
              <a-select style="width: 100%; height: 32px;" v-model:value="deliveryInfo.phuongXa"
                placeholder="Chọn phường/xã" :options="wards" @change="onWardChange" show-search
                option-filter-prop="label" :class="{ 'input-error': !deliveryInfo.phuongXa && isDeliveryEnabled }" />
            </div>

            <div class="form-group">
              <label for="address-detail">Địa chỉ cụ thể <span class="required">*</span></label>
              <a-input type="text" id="address-detail" v-model:value="deliveryInfo.diaChiCuThe" class="input-customer"
                placeholder="Nhập số nhà, tên đường,..."
                :class="{ 'input-error': !deliveryInfo.diaChiCuThe && isDeliveryEnabled }" />
            </div>

            <div class="form-group" style="margin-left: 100px;">
              <label for="note">Ghi chú</label>
              <a-textarea style=" height: 100px; width: 700px;" id="note" v-model:value="deliveryInfo.ghiChu"
                class="iput-customer large-note" placeholder="Nhập ghi chú (nếu có)" :rows="8" />
            </div>
          </div>
        </div>
      </div>
      <div v-if="showProductModal" class="modal-backdrop" @click.self="showProductModal = false">
        <div class="modal-content">
          <div class="card mt-4">
            <div class="card-header">
              <h3>Danh sách sản phẩm</h3>
            </div>
            <div class="card-body">

              <div class="filter-container">

                <div class="filter-item">
                  <label for="search-query" class="filter-label">Tìm kiếm sản phẩm</label>
                  <a-input id="search-query" v-model:value="localSearchQuery" placeholder="Nhập mã / tên để tìm kiếm..."
                    class="filter-control" />
                </div>

                <div class="filter-item search-input-group">
                  <label for="search-color" class="filter-label">Màu:</label>
                  <a-select id="search-color" v-model:value="localColor" @change="handleColorChange" allow-clear
                    :options="ColorOptions" placeholder="Chọn màu sắc" size="small" />
                </div>

                <div class="filter-item search-input-group">
                  <label for="search-size" class="filter-label">Kích cỡ:</label>
                  <a-select id="search-size" v-model:value="localSize" @change="handleSizeChange" allow-clear
                    :options="SizeOptions" placeholder="Chọn kích cỡ" size="small" />
                </div>

                <div class="filter-item">
                  <label for="category-select" class="filter-label">Danh mục:</label>
                  <a-select id="category-select" class="filter-control" allow-clear :options="danhMucOptions"
                    placeholder="Chọn danh mục" v-model:value="localSelectedCategory" />
                </div>

                <div class="filter-item">
                  <label for="material-select" class="filter-label">Chất liệu:</label>
                  <a-select id="material-select" class="filter-control" allow-clear :options="chatLieuOptions"
                    placeholder="Chọn chất liệu" v-model:value="localSelectedMaterial" />
                </div>

                <div class="filter-item">
                  <label for="brand-select" class="filter-label">Thương hiệu:</label>
                  <a-select id="brand-select" class="filter-control" allow-clear :options="thuongHieuOptions"
                    placeholder="Chọn thương hiệu" v-model:value="localSelectedBrand" />
                </div>

                <div class="filter-item">
                  <label for="sole-type-select" class="filter-label">Loại đế:</label>
                  <a-select id="sole-type-select" class="filter-control" allow-clear :options="loaiDeOptions"
                    placeholder="Chọn loại đế" v-model:value="localSelectedSoleType" />
                </div>

                <div class="filter-item reset-button-group" style="margin-top: 27px;">
                  <a-tooltip title="Làm mới bộ lọc">
                    <a-button style="background-color: dimgrey; color: white" @click="resetFilters"
                      class="reset-button filter-control-button">
                      Đặt lại bộ lọc
                      <ReloadOutlined />
                    </a-button>
                  </a-tooltip>
                </div>
              </div>

              <div class="product-selection-table">

                <a-table :columns="columns" :data-source="stateSP.products" :pagination="{
                  current: stateSP.paginationParams.page,
                  pageSize: stateSP.paginationParams.size,
                  total: stateSP.totalItems,
                  showSizeChanger: true,
                  pageSizeOptions: ['10', '20', '30', '40', '50']
                }" :scroll="{ y: 240 }" @change="handleTableChange">
                  <template #bodyCell="{ column, record }">
                    <template v-if="column.key === 'status'">
                      <a-tag :color="record.status == 'ACTIVE' ? 'green' : 'red'">
                        {{ record.status == 'ACTIVE' ? 'Hoạt động' : 'Không hoạt động' }}
                      </a-tag>
                    </template>
                    <div v-if="column.key === 'stt'">
                      {{ stateSP.products.indexOf(record) + 1 }}
                    </div>
                    <div v-if="column.key === 'giaBan'">
                      {{ formatCurrency(record.giaBan) }}
                    </div>
                    <template v-if="column.key === 'anh'">
                      <div class="center-cell">
                        <img :src="record.anh" class="anh" style="width: 50px; height: 50px; border-radius: 50%" />
                      </div>
                    </template>
                    <template v-if="column.key === 'mau'">
                      <div class="center-cell">
                        <div class="color"
                          :style="{ width: '30px', height: '30px', backgroundColor: record.mau, border: '1px solid #000' }">
                        </div>
                      </div>
                    </template>
                    <template v-if="column.key === 'operation'">
                      <div class="center-cell">
                        <div class="d-flex gap-1 justify-center">
                          <a-tooltip title="chọn sản phẩm">
                            <a-button type="primary" @click="selectProduct(record.id)"
                              style="background-color: #54bddb; color: white;"
                              class="p-2 d-flex justify-content-center align-items-center btn-choose-product">
                              Chọn
                            </a-button>
                          </a-tooltip>
                        </div>
                      </div>
                    </template>
                  </template>
                </a-table>
              </div>
            </div>

          </div>
          <button class="btn-add-product" @click="showProductModal = false" style="margin-left: 1100px;">
            <p class="quet">Hủy</p>
          </button>
        </div>
      </div>
    </div>

    <div class="right-column">


      <div class="card-body" style="margin-top: 80px;">
        <div class="card mt-4">
          <div class="card-header">
            <h3 style="font-size: 18px;">Khách hàng</h3>
          </div>

          <div v-if="state.detailKhachHang">
            <div class="form-group1">
              <label style="color: black; font-size: 15px;" for="customer-name">Tên khách hàng</label>
              <div class="input-wrapper">
                <input style=" font-size: 15px;" :value="state.detailKhachHang.ten" type="text" id="customer-name"
                  class="input-customer" placeholder="Tên khách hàng" readonly />
              </div>
            </div>

            <div class="form-group1">
              <label style="color: black; font-size: 15px;" for="phone">Số điện thoại</label>
              <div class="input-wrapper">
                <input style=" font-size: 15px;" :value="state.detailKhachHang.sdt" type="text" id="phone"
                  placeholder="Số điện thoại" class="input-customer" readonly />
              </div>
            </div>
          </div>
          <div v-else>
            <div class="form-group1">
              <label style="color: black; font-size: 15px;">Tên khách hàng</label>
              <div class="input-wrapper">
                <input style=" font-size: 15px;" v-model="newCustomer.ten" type="text" id="customer-name"
                  class="input-customer" placeholder="Tên khách hàng" />
              </div>
            </div>

            <div class="form-group1">
              <label style="color: black; font-size: 15px;" for="phone">Số điện thoại</label>
              <div class="input-wrapper">
                <input style=" font-size: 15px;" v-model="newCustomer.sdt" type="text" id="phone" class="input-customer"
                  placeholder="Số điện thoại" />
              </div>
            </div>
          </div>
          <div class="button-group-customer">
            <button style=" font-size: 15px;" class="add-button" @click="showKhachHangModal = true">Chọn khách
              hàng</button>
            <button style=" font-size: 15px;" class="add-button" @click="addCustomer">Thêm khách hàng</button>
          </div>
        </div>
      </div>

      <div class="card-body">
        <div class="card mt-4">
          <div class="card-header">
            <h3 style=" font-size: 18px;">Thông tin đơn hàng</h3>
            <div class="delivery-toggle-container">
              <label for="delivery-switch" style=" font-size: 15px;">Giao hàng</label>
              <a-switch v-model:checked="isDeliveryEnabled" @change="giaoHang(isDeliveryEnabled)"
                id="delivery-switch" />
            </div>
          </div>
          <div class="card-body payment-section-content">
            <!-- Các phần khác của payment-section-content giữ nguyên -->
            <div class="discount-code-section">
              <label for="discount-code" style=" font-size: 15px;">Mã giảm giá</label>
              <div class="discount-input-group">
                <input type="text" id="discount-code" class="discount-input" v-model="selectedDiscountCode"
                  placeholder="Chọn mã giảm giá" readonly />
                <button class="select-discount-button" @click="showDiscountModal = true">
                  Chọn mã
                </button>
              </div>
              <!-- <div v-if="isBestDiscountApplied == true" style="color: red;" class="text-green-600 text-sm mt-2">
                Phiếu giảm giá tốt nhất
              </div> -->
              <div class="summary-item" v-if="betterDiscountMessage">
                <span style="color: red; margin-left: 10px;">{{ betterDiscountMessage }}</span>
              </div>
              <div v-if="selectedDiscount" class="discount-info">
                Giảm: {{ formatCurrency(giamGia) }}
              </div>
            </div>
            <div class="payment-summary">
              <div class="summary-item">
                <label>Tổng tiền hàng:</label>
                <span>{{ formatCurrency(tienHang) }}</span>
              </div>
              <div class="summary-item">
                <label>Giảm giá:</label>
                <span>{{ formatCurrency(giamGia) }}</span>
              </div>
              <div class="summary-item" v-if="isDeliveryEnabled">
                <label>Phí vận chuyển:</label>
                <a-input-number v-model:value="shippingFee" :min="0" :formatter="formatCurrency" :parser="parseCurrency"
                  class="input-shipping-fee" :disabled="isFreeShipping" />
                <img src="/images/ghn-logo.webp" alt="GHN Logo" class="ghn-logo" />
              </div>
              <div class="summary-item" v-if="isDeliveryEnabled && isFreeShipping">
                <label></label>
                <span style="color: green; font-weight: bold;">Miễn phí vận chuyển (Đơn hàng trên 5,000,000
                  VND)</span>
              </div>
              <div class="summary-item total-amount">
                <label>Tổng tiền:</label>
                <span style="color: red;">{{ formatCurrency(tongTien) }}</span>
              </div>
            </div>
            <div class="payment-method-options" id="payment-method-selection">
              <button class="btn btn-payment-option" :class="{ 'active': state.currentPaymentMethod == '1' }"
                @click="handlePaymentMethod('1')">
                Chuyển khoản
              </button>
              <button class="btn btn-payment-option" :class="{ 'active': state.currentPaymentMethod == '0' }"
                @click="handlePaymentMethod('0')">
                Tiền mặt
              </button>
              <!-- <button class="btn btn-payment-option" :class="{ 'active': state.currentPaymentMethod == '2' }"
                @click="handlePaymentMethod('2')">
                Cả hai
              </button> -->
            </div>
            <a-popconfirm title="Bạn có chắc chắn muốn xác nhận thanh toán hóa đơn này?" ok-text="Đồng ý"
              cancel-text="Hủy" style="background-color: #54bddb;" @confirm="xacNhan(0)" @cancel="() => { }">
              <button class="btn-confirm-payment">Xác nhận thanh toán</button>
            </a-popconfirm>
          </div>
        </div>
      </div>

    </div>

    <div v-if="showKhachHangModal" class="modal-backdrop" @click.self="showKhachHangModal = false">
      <div class="modal-content">
        <h3 class="modal-title">Khách Hàng</h3>
        <!-- Thêm phần tìm kiếm -->
        <div class="search-customer mb-3">
          <a-input v-model:value="customerSearchQuery" placeholder="Tìm kiếm theo tên hoặc số điện thoại..."
            class="search-input-customer" @input="debouncedFetchCustomers">
            <template #prefix>
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" class="bi bi-search"
                viewBox="0 0 16 16">
                <path
                  d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001q.044.06.085.12c.047.061.096.119.146.177l3.85 3.85a1 1 0 0 0 1.415-1.415zM12 6.5a5.5 5.5 0 1 1-11 0 5.5 5.5 0 0 1 11 0" />
              </svg>
            </template>
          </a-input>
        </div>
        <div class="min-h-[360px]">
          <a-table :columns="columnsKhachHang" :data-source="state.khachHang" :pagination="{
            current: state.paginationParams.page,
            pageSize: state.paginationParams.size,
            total: state.totalItemsKH,
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '30', '40', '50']
          }" :scroll="{ y: 240 }">
            <template #bodyCell="{ column, record }">
              <div v-if="column.key === 'stt'">
                {{ state.khachHang.indexOf(record) + 1 }}
              </div>
              <template v-if="column.key === 'operation'">
                <div class="center-cell">
                  <div class="d-flex gap-1 justify-center">
                    <a-tooltip title="Chọn khách hàng">
                      <a-button type="primary" @click="selectKhachHang(record.id)"
                        class="p-2 d-flex justify-content-center align-items-center"
                        style="background-color: #54bddb; color: white;">
                        Chọn
                      </a-button>
                    </a-tooltip>
                  </div>
                </div>
              </template>
            </template>
          </a-table>
        </div>
      </div>
    </div>

    <a-modal v-model:visible="isBothPaymentModalVisible" title="Thanh toán bằng cả hai (QR và tiền mặt)"
      @ok="confirmBothPayment" @cancel="closeBothPaymentModal" width="500px">
      <div style="text-align: center;">
        <img src="/images/qr.png" alt="QR Code" style="max-width: 100%; height: auto; margin-bottom: 20px;" />
        <a-form ref="paymentForm" name="paymentForm" autocomplete="off">
          <a-form-item label="Số tiền khách đưa (VND)" name="amountPaid" :label-col="{ span: 24 }">
            <a-input v-model:value="amountPaid" placeholder="Nhập số tiền" style="border-radius: 4px;" />
          </a-form-item>
        </a-form>
        <div class="payment-summary">
          <div class="summary-item">
            <label>Tổng tiền khách đã trả (dự kiến):</label>
            <span>{{ formatCurrency(tienKhachThanhToan + (amountPaid || 0)) }}</span>
          </div>
          <div class="summary-item total-amount">
            <label>Tiền còn thiếu (dự kiến):</label>
            <span style="color: red;">{{ formatCurrency(tongTien - (tienKhachThanhToan + (amountPaid || 0) <= 0 ? 0 :
              tienKhachThanhToan + (amountPaid || 0))) }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <a-button key="back" @click="closeBothPaymentModal">Hủy</a-button>
        <a-button key="submit" type="primary" :loading="bothPaymentLoading" @click="confirmBothPayment">Xác
          nhận</a-button>
      </template>
    </a-modal>

    <a-modal v-model:visible="isQrVNpayModalVisible" title="Quét QR để thanh toán" @ok="closeQrModalVnPay"
      @cancel="closeQrModalVnPay" width="400px">
      <div style="text-align: center;">
        <img src="/images/qr.png" alt="QR Code" style="max-width: 100%; height: auto;" />
      </div>
      <template #footer>
        <a-button style="background-color: #54bddb; color: white;" key="submit" type="primary"
          @click="closeQrModalVnPay">Xác nhận</a-button>
      </template>
    </a-modal>

    <a-modal :open="state.isModalOpen" title="Chọn số lượng" width="400px" @cancel="closeModal">
      <template #footer>
        <a-popconfirm title="Bạn có chắc chắn muốn chọn bằng này số lượng sản phẩm ?" @confirm="confirmQuantity"
          ok-text="Đồng ý" cancel-text="Huỷ">
          <a-button type="primary" style="background-color: #54bddb; color: white;">Xác nhận</a-button>
        </a-popconfirm>
        <a-button @click="closeModal">Huỷ</a-button>
      </template>
      <a-form ref="productForm" name="productForm" autocomplete="off">
        <a-form-item label="nhập số lượng" name="soLuong" :label-col="{ span: 24 }">
          <a-input-number v-model:value="selectedProduct.soLuong" :min="1" placeholder="nhập số lượng"
            style="width: 100%; border-radius: 4px;" />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal :open="showDeliveryModal" title="Áp dụng phiếu giảm giá tốt hơn" width="400px" @cancel="closeModal">
      <template #footer>
        <a-popconfirm title="Bạn có chắc chắn muốn chọn phiếu giảm giá này" @confirm="confirmQuantityP" ok-text="Đồng ý"
          cancel-text="Huỷ">
          <a-button type="primary" style="background-color: #54bddb; color: white;">Xác nhận</a-button>
        </a-popconfirm>
        <a-button @click="closeModalP">Huỷ</a-button>
      </template>
      <p>Đang có 1 phiếu giảm giá tốt hơn bạn có muốn áp dụng không</p>
    </a-modal>

    <a-modal v-model:visible="isQrModalVisible" title="Quét mã QR sản phẩm" @cancel="closeQrModal" :footer="null">
      <div id="reader" style="width: 100%; max-width: 500px; margin: auto;"></div>
      <p v-if="!hasCamera" style="color: red; text-align: center;">Không tìm thấy camera hoặc không có quyền truy
        cập
        camera.</p>
    </a-modal>

    <a-modal v-model:visible="showDiscountModal" title="Chọn mã giảm giá" @cancel="showDiscountModal = false"
      width="600px">
      <div class="discount-modal-content">
        <div v-if="state.discountList.length > 0">
          <a-list item-layout="horizontal" :data-source="state.discountList">
            <template #renderItem="{ item }">
              <a-list-item>
                <a-list-item-meta>
                  <template #title>
                    <a @click="selectDiscount(item)">{{ item.ma }} - {{ item.ten }}</a>
                  </template>
                  <template #description>
                    Giá trị giảm: {{ formatCurrency(item.giaTriGiamThucTe) }}
                  </template>
                </a-list-item-meta>
                <template #actions>
                  <a-button type="primary" size="small" @click="selectDiscount(item)"
                    style="background-color: #54bddb; color: white;">Chọn</a-button>
                </template>
              </a-list-item>
            </template>
          </a-list>
        </div>
        <div v-else>
          <a-empty description="Không có mã giảm giá nào phù hợp." />
        </div>
      </div>
      <template #footer>
        <a-button key="back" @click="showDiscountModal = false">Đóng</a-button>
        <a-button key="clear" @click="resetDiscount" v-if="selectedDiscount">Bỏ chọn</a-button>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive, watch, nextTick } from 'vue'
import { toast } from 'vue3-toastify'
import 'vue3-toastify/dist/index.css'
import {
  GetHoaDons,
  getCreateHoaDon,
  themSanPham,
  GetGioHang,
  xoaSP,
  themSL,
  xoaSL,
  type KhachHangResponse,
  GetKhachHang,
  themKhachHang,
  GeOneKhachHang,
  getPhuongThucThanhToan,
  type PhuongThucThanhToanResponse,
  thanhToanThanhCong,
  GetSanPhams,
  type PhieuGiamGiaResponse,
  getMaGiamGia,
  type ThongTinGiaoHangResponse,
  suaGiaoHang,
  huyHoaDon,
  getCustomerAddress,
  getMaGiamGiaKoDu,
  themMoiKhachHang
} from '@/services/api/admin/banhang.api'
const localSearchQuery = ref('');
const localColor = ref<string | null>(null);
const localSize = ref<string | null>(null);
const localSelectedCategory = ref<string | null>(null);
const localSelectedMaterial = ref<string | null>(null);
const localSelectedBrand = ref<string | null>(null);
const localSelectedSoleType = ref<string | null>(null);
import type { TableColumnsType } from 'ant-design-vue'
import {
  GetListColor,
  GetListSize,
  type ParamsGetSanPham,
  type ParamsPhieuGiamGia,
  type SanPhamResponse
} from '@/services/api/admin/sanphamchitiet.api'
import BreadcrumbDefault from '@/components/ui/Breadcrumbs/BreadcrumbDefault.vue'
import { getGHNProvinces } from '@/services/api/ghn.api.ts'
import { getGHNDistricts } from '@/services/api/ghn.api.ts'
import { getGHNWards } from '@/services/api/ghn.api.ts'
import { getAvailableServices } from '@/services/api/ghn.api.ts'
import type { AvailableServiceRequest } from '@/services/api/ghn.api.ts'
import type { ShippingFeeRequest } from '@/services/api/ghn.api.ts'
import axios from 'axios'
import { calculateFee } from '@/services/api/ghn.api.ts'
import { Html5Qrcode } from 'html5-qrcode'
import { GetListChatLieu, GetListDanhMuc, GetListLoaiDe, GetListThuongHieu } from '@/services/api/admin/sanpham.api'
import { debounce } from 'lodash';
import { localStorageAction } from '@/utils/storage';
import { USER_INFO_STORAGE_KEY } from '@/constants/storageKey';
import { QrcodeOutlined } from '@ant-design/icons-vue';
const debouncedFetchProducts = debounce(async () => {
  stateSP.searchQuery = localSearchQuery.value;
  stateSP.selectedCategory = localSelectedCategory.value;
  stateSP.selectedMaterial = localSelectedMaterial.value;
  stateSP.selectedBrand = localSelectedBrand.value;
  stateSP.selectedSoleType = localSelectedSoleType.value;
  await fetchProducts();
}, 300)
const idNV = localStorageAction.get(USER_INFO_STORAGE_KEY)
const ColorOptions = ref<{ label: string; value: string }[]>([]);
const SizeOptions = ref<{ label: string; value: string }[]>([]);
const isBothPaymentModalVisible = ref(false)
const amountPaid = ref(0)
const bothPaymentLoading = ref(false)
const soTien = ref(0)
const tienKhachThanhToan = ref(0)
const tienThieu = ref(0)
const tongTien = ref(0)
const tongTienTruocGiam = ref(0)
const giamGia = ref(0)
const tienHang = ref(0)
const idSP = ref('')
const idHDS = ref('')
const tabs = ref<Array<{ id: number, idHD: string, ma: string, soLuong: number, products: any[], loaiHoaDon: string }>>([])
const activeTab = ref(0)
const idPGG = ref('')
let nextTabId = 1
const loaiHD = ref('')
const showDiscountModal = ref(false)
const discountList = ref<PhieuGiamGiaResponse[]>([])
const selectedDiscount = ref<PhieuGiamGiaResponse | null>(null)
const selectedDiscountCode = ref<string>('')
const thuongHieuOptions = ref<{ label: string; value: string }[]>([])
const loaiDeOptions = ref<{ label: string; value: string }[]>([])
const danhMucOptions = ref<{ label: string; value: string }[]>([])
const chatLieuOptions = ref<{ label: string; value: string }[]>([])
const isDeliveryEnabled = ref(false)
const showDeliveryModal = ref(false)
const deliveryInfo = reactive({
  tenNguoiNhan: '',
  sdtNguoiNhan: '',
  tinhThanhPho: null,
  quanHuyen: null,
  phuongXa: null,
  diaChiCuThe: '',
  ghiChu: '',
});
const currentDeliveryInfo = ref<ThongTinGiaoHangResponse | null>(null)
const customerSearchQuery = ref('');
const betterDiscountMessage = ref('');
const deliveryInfoByInvoice = reactive<{ [key: string]: any }>({});
// GHN specific states
const provinces = ref<Array<{ value: string, label: string, code: string }>>([])
const districts = ref<Array<{ value: string, label: string, code: string }>>([])
const wards = ref<Array<{ value: string, label: string, code: string }>>([])
const shippingFee = ref(0)
const provinceCode = ref<number | null>(null)
const districtCode = ref<number | null>(null)
const wardCode = ref<string | null>(null)
const FROM_DISTRICT_ID = 3440
const FROM_WARD_CODE = '13010';
const isBestDiscountApplied = ref(false);
const phieuNgon = ref('')
const GHN_API_TOKEN = '72f634c6-58a2-11f0-8a1e-1e10d8df3c04';
const GHN_SHOP_ID = 5872469


// Main reactive state object
const state = reactive({
  searchQuery: '',
  idSP: '',
  searchStatus: null as number | null,
  isModalOpen: false,
  isModaThanhToanlOpen: false,
  isModalChangeStatus: false,
  selectedProductId: null as string | null,
  khachHang: [] as KhachHangResponse[],
  thanhToan: [],
  discountList: [] as PhieuGiamGiaResponse[],
  phuongThuThanhToan: [] as PhuongThucThanhToanResponse[],
  tongTien: null as { tongTien: number } | null,
  detailKhachHang: null as KhachHangResponse | null,
  products: [] as SanPhamResponse[],
  gioHang: [] as SanPhamResponse[],
  paginationParams: { page: 1, size: 10 },
  totalItems: 0,
  totalItemsKH: 0,
  selectedPaymentMethod: '' as string,
  currentPaymentMethod: '0'
})

const debouncedFetchCustomers = debounce(async () => {
  await fetchCustomers();
}, 300);

const fetchCustomers = async () => {
  try {
    const params = {
      page: state.paginationParams.page,
      size: state.paginationParams.size,
      q: customerSearchQuery.value.trim(),
    };
    const response = await GetKhachHang(params);
    console.log("Khách hàng (object):", response.data?.data);
    state.khachHang = response.data?.data;
    state.totalItemsKH = response.totalElements || 0;
  } catch (error) {
    console.error('Failed to fetch customers:', error);
    state.khachHang = [];
    state.totalItemsKH = 0;
  }
};

const stateSP = reactive({
  searchQuery: '',
  searchStatus: null as number | null,
  selectedCategory: null as string | null,
  selectedMaterial: null as string | null,
  selectedBrand: null as string | null,
  selectedSoleType: null as string | null,

  isModalOpen: false,
  isModalChangeStatus: false,
  selectedProductId: null as string | null,
  products: [] as SanPhamResponse[],
  paginationParams: { page: 1, size: 10 },
  totalItems: 0
})


const parseCurrency = (value: string | number) => {
  if (!value) return 0
  let str = String(value).replace(/[^0-9nghìntrieu]/g, '').trim().toLowerCase()
  let number = parseInt(str.replace(/[^0-9]/g, '')) || 0

  if (str.includes('nghìn')) {
    number *= 1000
  } else if (str.includes('triệu')) {
    number *= 1000000
  }

  return number
}

const fetchProvinces = async () => {
  try {
    const response = await getGHNProvinces(GHN_API_TOKEN);
    provinces.value = response.map((item: any) => ({
      value: String(item.ProvinceID),
      label: item.ProvinceName,
      code: String(item.ProvinceID),
    }));
  } catch (error) {
    console.error('Failed to fetch provinces:', error);
    provinces.value = [];
  }
};

const fetchDistricts = async (provinceId: number) => {
  try {
    const response = await getGHNDistricts(provinceId, GHN_API_TOKEN);
    districts.value = response.map((item: any) => ({
      value: String(item.DistrictID),
      label: item.DistrictName,
      code: String(item.DistrictID),
    }));
  } catch (error) {
    console.error('Failed to fetch districts:', error);
    toast.error('Không thể tải danh sách Quận/Huyện.');
    districts.value = [];
  }
};

const fetchWards = async (districtId: number) => {
  try {
    const response = await getGHNWards(districtId, GHN_API_TOKEN);
    wards.value = response.map((item: any) => ({
      value: item.WardCode,
      label: item.WardName,
      code: item.WardCode,
    }));
  } catch (error) {
    console.error('Failed to fetch wards:', error);
    toast.error('Không thể tải danh sách Phường/Xã.');
    wards.value = [];
  }
};

const fetchColor = async () => {
  try {
    const { data } = await GetListColor();
    ColorOptions.value = data.map((c: any) => ({ label: c.ten, value: c.id }));
  } catch (e) {
    console.error('Error fetching colors:', e);
  }
};

watch(localSearchQuery, () => {
  debouncedFetchProducts();
});

watch([localColor, localSize, localSelectedCategory, localSelectedMaterial, localSelectedBrand, localSelectedSoleType], () => {
  debouncedFetchProducts();
});

const resetFilters = () => {
  localSearchQuery.value = '';
  localColor.value = null;
  localSize.value = null;
  localSelectedCategory.value = null;
  localSelectedMaterial.value = null;
  localSelectedBrand.value = null;
  localSelectedSoleType.value = null;
  stateSP.searchQuery = '';
  stateSP.selectedCategory = null;
  stateSP.selectedMaterial = null;
  stateSP.selectedBrand = null;
  stateSP.selectedSoleType = null;
  fetchProducts();
};

const fetchThuongHieu = async () => {
  try {
    const response = await GetListThuongHieu()
    thuongHieuOptions.value = response.data.map((item: any) => ({
      label: item.ten,
      value: item.id
    }))
  } catch (error) {
    console.error('Lỗi khi lấy danh sách thương hiệu:', error)
  }
}

const fetchChatLieu = async () => {
  try {
    const response = await GetListChatLieu()
    chatLieuOptions.value = response.data.map((item: any) => ({
      label: item.ten,
      value: item.id
    }))
  } catch (error) {
    console.error('Lỗi khi lấy danh sách chất liệu:', error)
  }
}

const fetchDanhMuc = async () => {
  try {
    const response = await GetListDanhMuc()
    danhMucOptions.value = response.data.map((item: any) => ({
      label: item.ten,
      value: item.id
    }))
  } catch (error) {
    console.error('Lỗi khi lấy danh sách danh mục:', error)
  }
}

const fetchLoaiDe = async () => {
  try {
    const response = await GetListLoaiDe()
    loaiDeOptions.value = response.data.map((item: any) => ({
      label: item.ten,
      value: item.id
    }))
  } catch (error) {
    console.error('Lỗi khi lấy danh sách loại đế:', error)
  }
}

const fetchSize = async () => {
  try {
    const { data } = await GetListSize();
    SizeOptions.value = data.map((s: any) => ({ label: s.ten, value: s.id }));
  } catch (e) {
    console.error('Error fetching sizes:', e);
  }
};

const checkFromDistrictAndWard = async () => {
  try {
    const response = await getGHNDistricts(null, GHN_API_TOKEN); // Lấy tất cả quận/huyện
    const districtExists = response.data.some((d: District) => d.DistrictID === FROM_DISTRICT_ID);
    if (!districtExists) {
      console.error(`FROM_DISTRICT_ID ${FROM_DISTRICT_ID} không hợp lệ!`);
      toast.error('Mã quận/huyện nguồn không hợp lệ.');
      return false;
    }
    const wardResponse = await getGHNWards(FROM_DISTRICT_ID, GHN_API_TOKEN);
    const wardExists = wardResponse.data.some((w: Ward) => w.WardCode === FROM_WARD_CODE);
    if (!wardExists) {
      console.error(`FROM_WARD_CODE ${FROM_WARD_CODE} không hợp lệ!`);
      toast.error('Mã phường/xã nguồn không hợp lệ.');
      return false;
    }
    return true;
  } catch (error) {
    console.error('Lỗi khi kiểm tra mã quận/huyện hoặc phường/xã:', error);
    return false;
  }
};

// Gọi kiểm tra khi khởi động
onMounted(async () => {
  fetchDanhMuc()
  fetchThuongHieu()
  fetchLoaiDe()
  fetchChatLieu()
  await fetchColor()
  await fetchSize()
  await fetchHoaDon()
  await checkFromDistrictAndWard()
  await fetchProvinces()
  setDefaultPaymentMethod()
})

const onProvinceChange = async (value: string) => {
  deliveryInfo.tinhThanhPho = value;
  const selectedProvince = provinces.value.find(p => p.value === value);
  provinceCode.value = selectedProvince ? parseInt(selectedProvince.code) : null;
  if (provinceCode.value) {
    await fetchDistricts(provinceCode.value);
    deliveryInfo.quanHuyen = undefined;
    deliveryInfo.phuongXa = undefined;
    districtCode.value = null;
    wardCode.value = null;
    await calculateShippingFee();
  } else {
    districts.value = [];
    wards.value = [];
    shippingFee.value = 0;
    calculateTotalAmounts();
  }

  deliveryInfoByInvoice[idHDS.value] = {
    tenNguoiNhan: deliveryInfo.tenNguoiNhan,
    sdtNguoiNhan: deliveryInfo.sdtNguoiNhan,
    diaChiCuThe: deliveryInfo.diaChiCuThe,
    tinhThanhPho: deliveryInfo.tinhThanhPho,
    quanHuyen: deliveryInfo.quanHuyen,
    phuongXa: deliveryInfo.phuongXa,
    provinceCode: provinceCode.value,
    districtCode: districtCode.value,
    wardCode: wardCode.value,
    shippingFee: shippingFee.value,
  };
};

const onDistrictChange = async (value: string) => {
  deliveryInfo.quanHuyen = value;
  const selectedDistrict = districts.value.find(d => d.value === value);
  districtCode.value = selectedDistrict ? parseInt(selectedDistrict.code) : null;
  if (districtCode.value) {
    await fetchWards(districtCode.value);
    deliveryInfo.phuongXa = undefined;
    wardCode.value = null;
    await calculateShippingFee();
  } else {
    wards.value = [];
    shippingFee.value = 0;
    calculateTotalAmounts();
  }

  deliveryInfoByInvoice[idHDS.value] = {
    tenNguoiNhan: deliveryInfo.tenNguoiNhan,
    sdtNguoiNhan: deliveryInfo.sdtNguoiNhan,
    diaChiCuThe: deliveryInfo.diaChiCuThe,
    tinhThanhPho: deliveryInfo.tinhThanhPho,
    quanHuyen: deliveryInfo.quanHuyen,
    phuongXa: deliveryInfo.phuongXa,
    provinceCode: provinceCode.value,
    districtCode: districtCode.value,
    wardCode: wardCode.value,
    shippingFee: shippingFee.value,
  };
};

const onWardChange = async (value: string) => {
  deliveryInfo.phuongXa = value;
  const selectedWard = wards.value.find(w => w.value === value);
  wardCode.value = selectedWard ? selectedWard.code : null;
  await calculateShippingFee();

  deliveryInfoByInvoice[idHDS.value] = {
    tenNguoiNhan: deliveryInfo.tenNguoiNhan,
    sdtNguoiNhan: deliveryInfo.sdtNguoiNhan,
    diaChiCuThe: deliveryInfo.diaChiCuThe,
    tinhThanhPho: deliveryInfo.tinhThanhPho,
    quanHuyen: deliveryInfo.quanHuyen,
    phuongXa: deliveryInfo.phuongXa,
    provinceCode: provinceCode.value,
    districtCode: districtCode.value,
    wardCode: wardCode.value,
    shippingFee: shippingFee.value,
  };
};

const newCustomer = reactive({
  ten: '',
  sdt: ''
});



const isFreeShipping = ref(false);


watch(tienHang, () => {
  calculateTotalAmounts();
  if (isDeliveryEnabled.value && provinceCode.value && districtCode.value && wardCode.value) {
    calculateShippingFee();
  }
});

watch(tienHang, async (newTienHang) => {
  if (idHDS.value) {
    await fetchDiscounts(idHDS.value); // Cập nhật phiếu giảm giá khi tổng tiền hàng thay đổi
  }
});

const calculateShippingFee = async () => {
  if (!isDeliveryEnabled.value || !idHDS.value || !provinceCode.value || !districtCode.value || !wardCode.value || tienHang.value <= 0) {
    shippingFee.value = 0;
    isFreeShipping.value = false;
    calculateTotalAmounts();
    return;
  }

  // Kiểm tra miễn phí vận chuyển trước
  if (tienHang.value > 5000000) {
    isFreeShipping.value = true;
    shippingFee.value = 0;
    toast.success('Đơn hàng trên 5,000,000 VND, miễn phí vận chuyển!');
    calculateTotalAmounts();
    return;
  }

  try {
    const availableServicesRequestBody: AvailableServiceRequest = {
      shop_id: GHN_SHOP_ID,
      from_district: FROM_DISTRICT_ID,
      to_district: districtCode.value,
    };
    const availableServicesResponse = await getAvailableServices(GHN_API_TOKEN, availableServicesRequestBody);

    if (!availableServicesResponse.data || !availableServicesResponse.data.length) {
      shippingFee.value = 0;
      isFreeShipping.value = false;
      toast.warn('Không tìm thấy dịch vụ vận chuyển phù hợp.');
      calculateTotalAmounts();
      return;
    }

    const selectedServiceId = availableServicesResponse.data[0].service_id;
    const requestBody: ShippingFeeRequest = {
      myRequest: {
        FromDistrictID: FROM_DISTRICT_ID,
        FromWardCode: FROM_WARD_CODE,
        ServiceID: selectedServiceId,
        ToDistrictID: districtCode.value,
        ToWardCode: wardCode.value,
        Height: 15,
        Length: 15,
        Weight: 500,
        Width: 15,
        InsuranceValue: tienHang.value,
        Coupon: null,
        PickShift: null,
      },
    };

    const response = await calculateFee(requestBody, GHN_API_TOKEN, GHN_SHOP_ID);
    shippingFee.value = response.data.total || 0;
    isFreeShipping.value = false;
    calculateTotalAmounts();
  } catch (error) {
    console.error('Failed to calculate shipping fee:', error);
    shippingFee.value = 0;
    isFreeShipping.value = false;
    toast.error('Không thể tính phí vận chuyển.');
    calculateTotalAmounts();
  }
};

const confirmBothPayment = async () => {
  bothPaymentLoading.value = true
  try {
    if (!idHDS.value) throw new Error('Không có hóa đơn được chọn!')
    if (amountPaid.value <= 0) throw new Error('Vui lòng nhập số tiền hợp lệ!')
    const remainingAmount = tongTien.value - tienKhachThanhToan.value - amountPaid.value;
    if (remainingAmount > 0) {
      toast.info(`Còn ${formatCurrency(remainingAmount)} cần thanh toán qua QR.`);
    } else {
      toast.success('Thanh toán đủ!');
    }

    // Call your API to record the "Both" payment method
    const formData = new FormData();
    formData.append('idHD', idHDS.value);
    formData.append('tongTien', amountPaid.value.toString()); // Amount paid by cash in this scenario
    formData.append('phuongThuc', 'Cả hai'); // Or specific code for "Both"
    await themPTTT(formData); // Assuming this API exists to record payment methods

    toast.success('Xác nhận thanh toán cả hai phương thức thành công!');
    await clickkActiveTab(activeTab.value, idHDS.value, loaiHD.value); // Refresh current invoice data
    closeBothPaymentModal();

  } catch (error: any) {
    console.error('Error in confirmBothPayment:', error)
    toast.error(error.message || 'Xác nhận thanh toán thất bại!')
  } finally {
    bothPaymentLoading.value = false
  }
}

// Table column definitions (unchanged, assumed correct from script)
const discountColumns: TableColumnsType = [
  { title: 'STT', key: 'stt', dataIndex: 'stt', width: 60, align: 'center' },
  { title: 'Mã phiếu', key: 'ma', dataIndex: 'ma', width: 150, align: 'center' },
  { title: 'Tên phiếu', key: 'ten', dataIndex: 'ten', width: 150, align: 'center' },
  {
    title: 'Giá trị giảm thực tế',
    key: 'giaTriGiamThucTe',
    dataIndex: 'giaTriGiamThucTe',
    width: 150,
    align: 'center'
  },
  {
    title: 'Thao tác',
    key: 'operation',
    width: 90,
    align: 'center'
  }
]

watch([deliveryInfo.tinhThanhPho, deliveryInfo.quanHuyen, deliveryInfo.phuongXa], () => {
  localStorage.setItem('deliveryInfoByInvoice', JSON.stringify(deliveryInfoByInvoice));
});

watch(selectedDiscount, () => {
  localStorage.setItem('selectedDiscount', JSON.stringify(selectedDiscount.value));
  localStorage.setItem('isBestDiscountApplied', JSON.stringify(isBestDiscountApplied.value));
});

const fetchDiscounts = async (idHD: string) => {
  try {
    if (!idHD) {
      console.warn('Missing idHD:', idHD);
      resetDiscount();
      return;
    }
    console.log("giảm giác 0" + tongTien.value.toString())
    const params: ParamsPhieuGiamGia = {
      idHD: tongTienTruocGiam.value.toString(),
      idKH: state.detailKhachHang?.id || '',
    };

    const response = await getMaGiamGia(params);

    console.log("giảm giá" + response.data)

    state.discountList = Array.isArray(response.data) ? response.data : [];
    console.log('Discount list:', state.discountList);

    const paramsFake = {
      idHD: idHD,
      idKH: state.detailKhachHang?.id || '',
      tongTien: tienHang.value,
    };
    const responseFake = await getMaGiamGiaKoDu(paramsFake);

    if (responseFake && responseFake.betterVoucher && responseFake.betterVoucher.amountNeeded !== undefined) {
      betterDiscountMessage.value = `Mua thêm ${formatCurrency(responseFake.betterVoucher.amountNeeded)} để nhận phiếu giảm giá tốt hơn`;
    } else {
      betterDiscountMessage.value = '';
    }

    const storedDiscount = localStorage.getItem('selectedDiscount');
    console.log('Stored discount from localStorage:', storedDiscount);

    if (storedDiscount) {
      let parsedDiscount;
      try {
        parsedDiscount = JSON.parse(storedDiscount);
        console.log('Parsed discount:', parsedDiscount);
      } catch (e) {
        console.error('Failed to parse stored discount:', e);
        parsedDiscount = null;
      }

      if (parsedDiscount && (parsedDiscount.id || parsedDiscount.ma)) {
        const matchedDiscount = state.discountList.find((d) =>
          d.id === parsedDiscount.id || d.ma === parsedDiscount.ma
        );
        if (matchedDiscount) {
          selectedDiscount.value = matchedDiscount;
          selectedDiscountCode.value = matchedDiscount.ma;
          giamGia.value = matchedDiscount.giaTriGiamThucTe || 0;
          isBestDiscountApplied.value = phieuNgon.value === matchedDiscount.ma;
          console.log('Restored selectedDiscount:', selectedDiscount.value);
        } else {
          console.warn('No matching discount, applying best discount');
          applyBestDiscount();
        }
      } else {
        console.warn('Parsed discount missing id or ma, applying best discount');
        applyBestDiscount();
      }
    } else if (state.discountList.length > 0) {
      console.log('No stored discount, applying best discount');
      applyBestDiscount();
    } else {
      resetDiscount();
    }

    console.log('Final selectedDiscount:', selectedDiscount.value);
    localStorage.setItem('selectedDiscount', JSON.stringify(selectedDiscount.value));
    localStorage.setItem('isBestDiscountApplied', JSON.stringify(isBestDiscountApplied.value));
    calculateTotalAmounts();
    console.log("giảm giác 1" + tongTienTruocGiam.value.toString())
  } catch (error) {

    state.discountList = [];
    resetDiscount();
  }
};

const applyBestDiscount = () => {
  if (state.discountList.length > 0) {
    const bestDiscount = state.discountList.reduce((best, current) =>
      (best.giaTriGiamThucTe || 0) > (current.giaTriGiamThucTe || 0) ? best : current
    );
    selectedDiscount.value = bestDiscount;
    selectedDiscountCode.value = bestDiscount.ma;
    phieuNgon.value = bestDiscount.ma;
    giamGia.value = bestDiscount.giaTriGiamThucTe || 0;
    isBestDiscountApplied.value = true;
  } else {
    selectedDiscount.value = null;
    selectedDiscountCode.value = '';
    giamGia.value = 0;
    isBestDiscountApplied.value = false;
  }
  calculateTotalAmounts();
  showDiscountModal.value = false;
};

const giaoHang = async (isDeliveryEnableds: boolean) => {
  isDeliveryEnabled.value = isDeliveryEnableds;

  if (isDeliveryEnabled.value && state.detailKhachHang) {
    deliveryInfo.tenNguoiNhan = state.detailKhachHang.ten || '';
    deliveryInfo.sdtNguoiNhan = state.detailKhachHang.sdt || '';
    deliveryInfo.diaChiCuThe = state.detailKhachHang.diaChi || '';

    if (!provinces.value.length) {
      await fetchProvinces();
    }

    const tinhThanhPhoId = state.detailKhachHang.tinh;
    const quanHuyenId = state.detailKhachHang.huyen;
    const phuongXaId = state.detailKhachHang.xa;

    if (tinhThanhPhoId) {
      const selectedProvince = provinces.value.find(p => p.code === tinhThanhPhoId.toString());
      if (selectedProvince) {
        deliveryInfo.tinhThanhPho = selectedProvince.value;
        provinceCode.value = parseInt(tinhThanhPhoId);
        await fetchDistricts(provinceCode.value);
      }
    }

    if (quanHuyenId && provinceCode.value) {
      const selectedDistrict = districts.value.find(d => d.code === quanHuyenId.toString());
      if (selectedDistrict) {
        deliveryInfo.quanHuyen = selectedDistrict.value;
        districtCode.value = parseInt(quanHuyenId);
        await fetchWards(districtCode.value);
      }
    }

    if (phuongXaId && districtCode.value) {
      const selectedWard = wards.value.find(w => w.code === phuongXaId);
      if (selectedWard) {
        deliveryInfo.phuongXa = selectedWard.value;
        wardCode.value = phuongXaId;
      }
    }

    // Kiểm tra miễn phí vận chuyển và tính phí
    await calculateShippingFee();

    // Lưu thông tin giao hàng cục bộ
    deliveryInfoByInvoice[idHDS.value] = {
      tenNguoiNhan: deliveryInfo.tenNguoiNhan,
      sdtNguoiNhan: deliveryInfo.sdtNguoiNhan,
      diaChiCuThe: deliveryInfo.diaChiCuThe,
      tinhThanhPho: deliveryInfo.tinhThanhPho,
      quanHuyen: deliveryInfo.quanHuyen,
      phuongXa: deliveryInfo.phuongXa,
      provinceCode: provinceCode.value,
      districtCode: districtCode.value,
      wardCode: wardCode.value,
      shippingFee: shippingFee.value,
    };
  } else {
    Object.assign(deliveryInfo, {
      tenNguoiNhan: state.detailKhachHang?.ten || '',
      sdtNguoiNhan: state.detailKhachHang?.sdt || '',
      diaChiCuThe: state.detailKhachHang?.diaChi || '',
      tinhThanhPho: undefined,
      quanHuyen: undefined,
      phuongXa: undefined,
    });
    provinceCode.value = null;
    districtCode.value = null;
    wardCode.value = null;
    shippingFee.value = 0;
    isFreeShipping.value = false;
    calculateTotalAmounts();

    // Lưu trạng thái không giao hàng
    deliveryInfoByInvoice[idHDS.value] = {
      tenNguoiNhan: state.detailKhachHang?.ten || '',
      sdtNguoiNhan: state.detailKhachHang?.sdt || '',
      diaChiCuThe: state.detailKhachHang?.diaChi || '',
      tinhThanhPho: undefined,
      quanHuyen: undefined,
      phuongXa: undefined,
      provinceCode: null,
      districtCode: null,
      wardCode: null,
      shippingFee: 0,
    };
  }

  await suaGiaoHang(idHDS.value);
  await capNhatDanhSach();
};

const selectDiscount = (discount: PhieuGiamGiaResponse) => {
  selectedDiscount.value = discount
  selectedDiscountCode.value = discount.ma
  giamGia.value = discount.giaTriGiamThucTe || 0
  toast.success(`Đã chọn phiếu giảm giá: ${discount.ma}`)
  calculateTotalAmounts()
  if (phieuNgon.value === discount.ma) {
    isBestDiscountApplied.value = true
  } else {
    isBestDiscountApplied.value = false
  }
  showDiscountModal.value = false
}

const handleTableChange = (pagination: any) => {
  stateSP.paginationParams.page = pagination.current;
  stateSP.paginationParams.size = pagination.pageSize;
  state.paginationParams.page = pagination.current;
  state.paginationParams.size = pagination.pageSize;
  fetchProducts(); // Gọi lại API sản phẩm
  fetchCustomers(); // Gọi lại API khách hàng
};

const closeBothPaymentModal = () => {
  isBothPaymentModalVisible.value = false;
  amountPaid.value = 0;
  bothPaymentLoading.value = false;
  // Không đặt lại state.currentPaymentMethod
};
const themPTTT = async (formData: FormData) => {
  // Replace with your actual API call to add payment method
  try {
    await new Promise(resolve => setTimeout(resolve, 500));
  } catch (error) {
    console.error('Failed to add payment method:', error);
    throw error;
  }
}

const handlePaymentMethod = async (method: string) => {
  if (!idHDS.value) {
    toast.error('Vui lòng chọn hoặc tạo hóa đơn trước khi chọn phương thức thanh toán!');
    return;
  }

  state.currentPaymentMethod = method;

  try {
    if (method === '0') {
      toast.success('Đã chọn phương thức thanh toán Tiền mặt.');
    } else if (method === '1') {
      toast.success('Đã chọn phương thức thanh toán chuyển khoản.');
      openQrModalVNPay();
    } else if (method === '2') {
      toast.success('Đã chọn phương thức thanh toán vừa chuyển khoản vừa tiền mặt.');
      openQrModalVNPayCaHai();

    }
  } catch (error: any) {
    console.error('Error in handlePaymentMethod:', error);
    toast.error('Có lỗi khi chọn phương thức thanh toán!');
  }
};

// Hàm mới để cập nhật trạng thái thanh toán
const updatePaymentStatus = async () => {
  if (idHDS.value) {
    const responsePTTT = await getPhuongThucThanhToan(idHDS.value);
    state.phuongThuThanhToan = responsePTTT;
    let totalPaid = 0;
    state.phuongThuThanhToan.forEach((item) => {
      totalPaid += item.tongTien;
    });
    tienKhachThanhToan.value = totalPaid;
    tienThieu.value = (tongTien.value || 0) - tienKhachThanhToan.value;
  }
};


const fetchHoaDon = async () => {
  try {
    await fetchProducts();
    const response = await GetHoaDons();

    if (response && Array.isArray(response)) {
      tabs.value = response.map((invoice, index) => ({
        id: index + 1,
        idHD: invoice.id,
        ma: invoice.ma,
        soLuong: invoice.soLuong,
        loaiHoaDon: invoice.loaiHoaDon,
        products: invoice.data?.products || [],
      }));

      if (tabs.value.length > 0) {
        activeTab.value = tabs.value[0].id;
        idHDS.value = tabs.value[0].idHD;
        loaiHD.value = tabs.value[0].loaiHoaDon;

        // Gọi clickkActiveTab để khôi phục đầy đủ trạng thái
        await clickkActiveTab(tabs.value[0].id, tabs.value[0].idHD, tabs.value[0].loaiHoaDon);
      } else {
        // Reset trạng thái khi không có hóa đơn
        resetDiscount();
        currentDeliveryInfo.value = null;
        isDeliveryEnabled.value = false;
        Object.assign(deliveryInfo, {
          tenNguoiNhan: '',
          sdtNguoiNhan: '',
          diaChiCuThe: '',
          tinhThanhPho: undefined,
          quanHuyen: undefined,
          phuongXa: undefined,
        });
        provinceCode.value = null;
        districtCode.value = null;
        wardCode.value = null;
        shippingFee.value = 0;
        isFreeShipping.value = false;
        state.gioHang = [];
        state.detailKhachHang = null;
        state.currentPaymentMethod = '0';
        tongTien.value = 0;
        tongTienTruocGiam.value = 0
        giamGia.value = 0;
        tienHang.value = 0;
        tienKhachThanhToan.value = 0;
        tienThieu.value = 0;
      }
    }
  } catch (error) {
    console.error('Failed to fetch invoices:', error);
    toast.error('Lấy danh sách hóa đơn thất bại!');
    resetDiscount();
    currentDeliveryInfo.value = null;
    isDeliveryEnabled.value = false;
    Object.assign(deliveryInfo, {
      tenNguoiNhan: '',
      sdtNguoiNhan: '',
      diaChiCuThe: '',
      tinhThanhPho: undefined,
      quanHuyen: undefined,
      phuongXa: undefined,
    });
    provinceCode.value = null;
    districtCode.value = null;
    wardCode.value = null;
    shippingFee.value = 0;
    isFreeShipping.value = false;
  }
};

const resetDiscount = () => {
  state.discountList = []
  selectedDiscount.value = null
  selectedDiscountCode.value = ''
  giamGia.value = 0
  calculateTotalAmounts()
}

const clickkActiveTab = async (id: number, hd: string, loaiHoaDon: string) => {
  idHDS.value = hd;
  activeTab.value = id;
  loaiHD.value = loaiHoaDon;
  isBestDiscountApplied.value = false;

  try {
    // Reset thông tin giao hàng trước khi khôi phục
    Object.assign(deliveryInfo, {
      tenNguoiNhan: '',
      sdtNguoiNhan: '',
      diaChiCuThe: '',
      tinhThanhPho: undefined,
      quanHuyen: undefined,
      phuongXa: undefined,
    });
    provinceCode.value = null;
    districtCode.value = null;
    wardCode.value = null;
    shippingFee.value = 0;
    isFreeShipping.value = false;

    // Lấy thông tin giỏ hàng
    const response = await GetGioHang(hd);
    state.gioHang = response;

    // Lấy thông tin khách hàng
    const responseKH = await GeOneKhachHang(hd);
    state.detailKhachHang = responseKH.id ? responseKH : null;

    // Khôi phục trạng thái giao hàng
    isDeliveryEnabled.value = loaiHoaDon === 'GIAO_HANG';

    if (isDeliveryEnabled.value && state.detailKhachHang) {
      deliveryInfo.tenNguoiNhan = state.detailKhachHang.ten || '';
      deliveryInfo.sdtNguoiNhan = state.detailKhachHang.sdt || '';
      deliveryInfo.diaChiCuThe = state.detailKhachHang.diaChi || '';

      const tinhThanhPhoId = state.detailKhachHang.tinh;
      const quanHuyenId = state.detailKhachHang.huyen;
      const phuongXaId = state.detailKhachHang.xa;

      if (!provinces.value.length) {
        await fetchProvinces();
      }

      if (tinhThanhPhoId) {
        const selectedProvince = provinces.value.find(p => p.code === tinhThanhPhoId.toString());
        if (selectedProvince) {
          deliveryInfo.tinhThanhPho = selectedProvince.value;
          provinceCode.value = parseInt(tinhThanhPhoId);
          await fetchDistricts(provinceCode.value);
        }
      }

      if (quanHuyenId && provinceCode.value) {
        const selectedDistrict = districts.value.find(d => d.code === quanHuyenId.toString());
        if (selectedDistrict) {
          deliveryInfo.quanHuyen = selectedDistrict.value;
          districtCode.value = parseInt(quanHuyenId);
          await fetchWards(districtCode.value);
        }
      }

      if (phuongXaId && districtCode.value) {
        const selectedWard = wards.value.find(w => w.code === phuongXaId);
        if (selectedWard) {
          deliveryInfo.phuongXa = selectedWard.value;
          wardCode.value = phuongXaId;
        }
      }

      // Khôi phục từ deliveryInfoByInvoice nếu có
      if (deliveryInfoByInvoice[hd]) {
        Object.assign(deliveryInfo, {
          tenNguoiNhan: deliveryInfoByInvoice[hd].tenNguoiNhan,
          sdtNguoiNhan: deliveryInfoByInvoice[hd].sdtNguoiNhan,
          diaChiCuThe: deliveryInfoByInvoice[hd].diaChiCuThe,
          tinhThanhPho: deliveryInfoByInvoice[hd].tinhThanhPho,
          quanHuyen: deliveryInfoByInvoice[hd].quanHuyen,
          phuongXa: deliveryInfoByInvoice[hd].phuongXa,
        });
        provinceCode.value = deliveryInfoByInvoice[hd].provinceCode;
        districtCode.value = deliveryInfoByInvoice[hd].districtCode;
        wardCode.value = deliveryInfoByInvoice[hd].wardCode;
        shippingFee.value = deliveryInfoByInvoice[hd].shippingFee;
      }

      // Tính phí vận chuyển
      await calculateShippingFee();
    } else if (state.detailKhachHang) {
      // Nếu không phải giao hàng, chỉ cập nhật thông tin cơ bản
      deliveryInfo.tenNguoiNhan = state.detailKhachHang.ten || '';
      deliveryInfo.sdtNguoiNhan = state.detailKhachHang.sdt || '';
      deliveryInfo.diaChiCuThe = state.detailKhachHang.diaChi || '';
    }

    // Lưu thông tin giao hàng vào deliveryInfoByInvoice
    deliveryInfoByInvoice[hd] = {
      tenNguoiNhan: deliveryInfo.tenNguoiNhan,
      sdtNguoiNhan: deliveryInfo.sdtNguoiNhan,
      diaChiCuThe: deliveryInfo.diaChiCuThe,
      tinhThanhPho: deliveryInfo.tinhThanhPho,
      quanHuyen: deliveryInfo.quanHuyen,
      phuongXa: deliveryInfo.phuongXa,
      provinceCode: provinceCode.value,
      districtCode: districtCode.value,
      wardCode: wardCode.value,
      shippingFee: shippingFee.value,
    };

    // Tính toán tổng tiền
    calculateTotalAmounts();

    await fetchDiscounts(hd);

    // Cập nhật trạng thái thanh toán
    await updatePaymentStatus();
  } catch (error) {
    console.error('Failed to switch invoice:', error);
    toast.error('Chuyển hóa đơn thất bại!');
    resetDiscount();
    isDeliveryEnabled.value = false;
    Object.assign(deliveryInfo, {
      tenNguoiNhan: '',
      sdtNguoiNhan: '',
      diaChiCuThe: '',
      tinhThanhPho: undefined,
      quanHuyen: undefined,
      phuongXa: undefined,
    });
    provinceCode.value = null;
    districtCode.value = null;
    wardCode.value = null;
    shippingFee.value = 0;
    isFreeShipping.value = false;
    calculateTotalAmounts();
  }
};
// Watch for changes in isDeliveryEnabled to clear/set delivery info
watch(isDeliveryEnabled, async (newValue) => {
  if (!newValue) {
    // If delivery is disabled, clear the current delivery info and reset form
    currentDeliveryInfo.value = null
    Object.assign(deliveryInfo, { tenNguoiNhan: '', sdtNguoiNhan: '', diaChiGiaoHang: '', tinhThanhPho: undefined, quanHuyen: undefined, phuongXa: undefined, diaChiCuThe: '' });
    shippingFee.value = 0;
    provinceCode.value = null;
    districtCode.value = null;
    wardCode.value = null;
    calculateTotalAmounts(); // Recalculate total
  } else {
    await fetchProvinces();
  }
}, { immediate: true });

const columnsKhachHang: TableColumnsType = [
  { title: 'STT', key: 'stt', dataIndex: 'stt', width: 80, align: 'center' },
  { title: 'Tên khách hàng', key: 'ten', dataIndex: 'ten', width: 150, align: 'center' },
  { title: 'Số điện thoại', key: 'sdt', dataIndex: 'sdt', width: 150, align: 'center' },
  {
    title: 'Thao tác',
    key: 'operation',
    width: 90,
    align: 'center'
  }
]

const columnsThanhToan: TableColumnsType = [ // Unused
  { title: 'STT', key: 'stt', dataIndex: 'stt', width: 80, align: 'center' },
  { title: 'Số tiền', key: 'tongTien', dataIndex: 'tongTien', width: 150, align: 'center' },
  {
    title: 'Phương thức',
    key: 'phuongThucThanhToan',
    dataIndex: 'phuongThucThanhToan',
    width: 150,
    align: 'center'
  },
  {
    title: 'Hành động',
    key: 'operation',
    width: 90,
    align: 'center'
  }
]

const columnsGiohang: TableColumnsType = [
  { title: 'STT', key: 'stt', dataIndex: 'stt', width: 60, align: 'center' },
  { title: 'Ảnh', key: 'anh', dataIndex: 'anh', width: 90, align: 'center' },
  { title: 'Tên sản phẩm', key: 'ten', dataIndex: 'ten', width: 90, align: 'center' },
  { title: 'Số lượng', key: 'soLuong', dataIndex: 'soLuong', width: 120, align: 'center' },
  { title: 'Giá bán', key: 'giaBan', dataIndex: 'giaBan', width: 110, align: 'center' },
  { title: 'Kích cỡ', key: 'kichThuoc', dataIndex: 'kichThuoc', width: 80, align: 'center' },
  { title: 'Màu sắc', key: 'mau', dataIndex: 'mau', width: 80, align: 'center' },
  { title: 'Tổng tiền', key: 'status', dataIndex: 'status', width: 80, align: 'center' },
  {
    title: 'Thao tác',
    key: 'operation',
    width: 100,
    align: 'center'
  }
]
const columns: TableColumnsType = [ // For product selection modal
  { title: 'STT', key: 'stt', dataIndex: 'stt', width: 60, align: 'center' },
  { title: 'Ảnh', key: 'anh', dataIndex: 'anh', width: 100, align: 'center' },
  { title: 'Tên sản phẩm', key: 'ten', dataIndex: 'ten', width: 100, align: 'center' },
  { title: 'Số lượng', key: 'soLuong', dataIndex: 'soLuong', width: 100, align: 'center' },
  { title: 'Giá bán', key: 'giaBan', dataIndex: 'giaBan', width: 100, align: 'center' },
  { title: 'Kích cỡ', key: 'kichThuoc', dataIndex: 'kichThuoc', width: 100, align: 'center' },
  { title: 'Thương hiệu', key: 'tenThuongHieu', dataIndex: 'tenThuongHieu', width: 100, align: 'center' },
  { title: 'Loại đế', key: 'tenLoaiDe', dataIndex: 'tenLoaiDe', width: 100, align: 'center' },
  { title: 'Chất liệu', key: 'tenChatLieu', dataIndex: 'tenChatLieu', width: 100, align: 'center' },
  { title: 'Danh mục', key: 'tenDanhMuc', dataIndex: 'tenDanhMuc', width: 100, align: 'center' },
  { title: 'Màu sắc', key: 'mau', dataIndex: 'mau', width: 100, align: 'center' },
  {
    title: 'Hành động',
    key: 'operation',
    width: 90,
    align: 'center'
  }
]
const increaseQuantity = async (idHDCT: any, idSPS: any) => {
  try {
    const formData = new FormData();
    formData.append('idSP', idSPS);
    formData.append('idHDCT', idHDCT);
    formData.append('idHD', idHDS.value);
    const res = await themSL(formData);

    if (res.message.includes("thay đổi giá từ")) {
      toast.warning(res.message);
      return;
    }

    if (res.message === 'Số lượng sản phẩm thêm vào nhiều hơn số lượng trong kho') {
      toast.error(res.message);
      return;
    }

    // Cập nhật giỏ hàng từ server
    const response = await GetGioHang(idHDS.value);
    state.gioHang = response;

    // Tính toán lại tổng tiền trước khi fetchDiscounts
    calculateTotalAmounts();
    await fetchDiscounts(idHDS.value); // Gọi fetchDiscounts với idHD mới
    capNhatDanhSach();
  } catch (error) {
    console.error('Failed to increase quantity:', error);
    toast.error('Tăng số lượng thất bại!');
    // Rollback nếu cần
    const updatedProduct = state.gioHang.find((item) => item.id === idSPS);
    if (updatedProduct) {
      updatedProduct.soLuong = Math.max(0, updatedProduct.soLuong - 1);
      calculateTotalAmounts();
    }
  }
};

const huy = async (idHD: string) => {

  try {

    const formData = new FormData()
    formData.append('idNV', idNV.userId)
    formData.append('idHD', idHD)

    const res = await huyHoaDon(formData)

    toast.success(res.message)

    await fetchProducts()
    await capNhatDanhSach()

    const indexToRemove = tabs.value.findIndex((tab) => tab.idHD === idHDS.value)
    if (indexToRemove !== -1) {
      tabs.value.splice(indexToRemove, 1)
    }

    // Reset all states after successful payment
    idHDS.value = ''
    activeTab.value = 0
    state.gioHang = []
    state.detailKhachHang = null
    state.phuongThuThanhToan = []
    state.tongTien = null
    tongTien.value = 0
    tongTienTruocGiam.value = 0
    giamGia.value = 0
    tienHang.value = 0
    soTien.value = 0
    tienKhachThanhToan.value = 0
    tienThieu.value = 0
    state.currentPaymentMethod = '0' // Reset to 'Tiền mặt'
    resetDiscount()
    isDeliveryEnabled.value = false // Reset delivery toggle
    currentDeliveryInfo.value = null // Clear delivery info
    Object.assign(deliveryInfo, { tenNguoiNhan: '', sdtNguoiNhan: '', diaChiGiaoHang: '', tinhThanhPho: undefined, quanHuyen: undefined, phuongXa: undefined, diaChiCuThe: '' });
    shippingFee.value = 0;
    provinceCode.value = null;
    districtCode.value = null;
    wardCode.value = null;

  } catch (error: any) {
    if (error?.response?.data?.message) {
      toast.error(error.response.data.message)
    } else {
      toast.error('Có lỗi xảy ra khi xác nhận thanh toán!')
      console.error('Lỗi khi xác nhận thanh toán:', error)
    }
  }
}



const xacNhan = async (check: number) => {
  if (!idHDS.value) {
    toast.error('Vui lòng chọn một hóa đơn để xác nhận thanh toán!')
    console.error('Lỗi: idHDS.value là null khi xác nhận thanh toán.')
    return
  }



  // Validate delivery info if enabled
  if (isDeliveryEnabled.value) {
    if (!deliveryInfo.tenNguoiNhan || !deliveryInfo.sdtNguoiNhan || !deliveryInfo.diaChiCuThe ||
      !deliveryInfo.tinhThanhPho || !deliveryInfo.quanHuyen || !deliveryInfo.phuongXa) {
      toast.error('Vui lòng nhập đầy đủ thông tin giao hàng!');
      return;
    }
    if (shippingFee.value < 0 && tienHang.value > 0) { // If shipping fee is 0 but there are items in cart
      toast.error('Vui làm kiểm tra lại tiền giao hàng');
      return;
    }
  }

  const form = ref({

    tinh: null as string | null,
    huyen: null as string | null,
    phuong: null as string | null,

  });

  console.log('va', deliveryInfo.tinhThanhPho)

  const selectedProvince = provinces.value.find((p) => p.code === deliveryInfo.tinhThanhPho);
  const selectedDistrict = districts.value.find((d) => d.code === deliveryInfo.quanHuyen);
  const selectedWard = wards.value.find((w) => w.code === deliveryInfo.phuongXa);

  console.log('Selected Province:', selectedProvince);


  try {

    const formData = new FormData()
    formData.append('idNV', idNV.userId)
    formData.append('tienHang', tienHang.value.toString())
    formData.append('idHD', idHDS.value)
    formData.append('tongTien', tongTien.value.toString())
    formData.append('ten', deliveryInfo.tenNguoiNhan);
    formData.append('sdt', deliveryInfo.sdtNguoiNhan);
    formData.append('diaChi', `${deliveryInfo.diaChiCuThe}, ${selectedWard?.label}, ${selectedDistrict?.label}, ${selectedProvince?.label}`);
    formData.append('tienShip', shippingFee.value.toString());
    formData.append('giamGia', giamGia.value.toString());
    formData.append('phuongThucThanhToan', state.currentPaymentMethod);
    formData.append('check', check.toString());
    if (selectedDiscount.value?.id) {
      formData.append('idPGG', selectedDiscount.value.id)
    }

    console.log(state.currentPaymentMethod)

    // Append delivery status and info if enabled
    formData.append('isDeliveryEnabled', isDeliveryEnabled.value.toString())


    const res = await thanhToanThanhCong(formData)

    if (res.message != null) {
      if (res.message.startsWith("Số")) {
        console.log(res.message)
        toast.error(res.message);
        return;
      }

      if (res.message.startsWith("Phiếu")) {
        console.log(res.message)
        toast.error(res.message);
        await fetchDiscounts(idHDS.value)
        return;
      }

      if (res.message.startsWith("Đã")) {
        console.log(res.message)
        // toast.error(res.message);
        showDeliveryModal.value = true;
        await fetchDiscounts(idHDS.value)
        // applyBestDiscount();
        isBestDiscountApplied.value = false; // Reset best discount flag
        return;
      }
    }

    if (isDeliveryEnabled.value == true) {
      toast.success('Giao hàng thành công!')
    } else {
      toast.success('Thanh toán thành công!')
    }

    await fetchProducts()
    await capNhatDanhSach()

    const indexToRemove = tabs.value.findIndex((tab) => tab.idHD === idHDS.value)
    if (indexToRemove !== -1) {
      tabs.value.splice(indexToRemove, 1)
    }

    // Reset all states after successful payment
    idHDS.value = ''
    activeTab.value = 0
    state.gioHang = []
    state.detailKhachHang = null
    state.phuongThuThanhToan = []
    state.tongTien = null
    tongTien.value = 0
    tongTienTruocGiam.value = 0
    giamGia.value = 0
    tienHang.value = 0
    soTien.value = 0
    tienKhachThanhToan.value = 0
    tienThieu.value = 0
    state.currentPaymentMethod = '0' // Reset to 'Tiền mặt'
    resetDiscount()
    isDeliveryEnabled.value = false // Reset delivery toggle
    currentDeliveryInfo.value = null // Clear delivery info
    Object.assign(deliveryInfo, { tenNguoiNhan: '', sdtNguoiNhan: '', diaChiGiaoHang: '', tinhThanhPho: undefined, quanHuyen: undefined, phuongXa: undefined, diaChiCuThe: '' });
    shippingFee.value = 0;
    provinceCode.value = null;
    districtCode.value = null;
    wardCode.value = null;

  } catch (error: any) {
    if (error?.response?.data?.message) {
      toast.error(error.response.data.message)
    } else {
      toast.error('Có lỗi xảy ra khi xác nhận thanh toán!')
      console.error('Lỗi khi xác nhận thanh toán:', error)
    }
  }
}

const decreaseQuantity = async (idHDCT: any, idSPS: any) => {
  try {
    const formData = new FormData();
    formData.append('idSP', idSPS);
    formData.append('idHDCT', idHDCT);
    await xoaSL(formData);

    // Lấy lại giỏ hàng từ server
    const response = await GetGioHang(idHDS.value);
    state.gioHang = response;

    calculateTotalAmounts();
    await fetchDiscounts(idHDS.value);
    capNhatDanhSach();

  } catch (error) {
    console.error('Failed to decrease quantity:', error);
    toast.error('Giảm số lượng thất bại!');
    // Rollback nếu cần
    const updatedProduct = state.gioHang.find((item) => item.id === idSPS);
    if (updatedProduct) {
      updatedProduct.soLuong = Math.min(updatedProduct.soLuong + 1, 1); // Rollback nếu lỗi
      calculateTotalAmounts();
    }
  }
};

watch(
  () => tienHang.value,
  async () => {
    if (idHDS.value) {
      await fetchDiscounts(idHDS.value)
    }
  }
)

watch(
  () => [deliveryInfo.tinhThanhPho, deliveryInfo.quanHuyen, deliveryInfo.phuongXa],
  async () => {
    if (isDeliveryEnabled.value) {
      await calculateShippingFee();
    }
  }
);

const openProductSelectionModal = async () => {

  console.log()
  if (!idHDS.value) {
    toast.error('Vui lòng tạo hoặc chọn hóa đơn trước khi chọn sản phẩm!')
    return
  }
  await fetchProducts()
  showProductModal.value = true
}

// Hàm tạo hóa đơn
async function createInvoice() {
  if (tabs.value.length >= 10) {
    toast.warning('Chỉ được tạo tối đa 10 hóa đơn!', { autoClose: 3000 });
    return;
  }

  try {
    const formData = new FormData();
    formData.append('idNV', idNV.userId);
    const newInvoice = await getCreateHoaDon(formData);
    console.log("hóa đơn mới", newInvoice)
    // Thêm hóa đơn mới vào tabs
    const newTabId = nextTabId++;
    tabs.value.push({
      id: newTabId,
      idHD: newInvoice.data.id,
      ma: newInvoice.data.ma,
      soLuong: 0,
      loaiHoaDon: newInvoice.loaiHoaDon || 'OFFLINE', // Mặc định là tại quầy nếu không có loaiHoaDon
      products: [],
    });
    idHDS.value = newInvoice.data.id;
    activeTab.value = newTabId;
    loaiHD.value = newInvoice.loaiHoaDon || 'OFFLINE';

    console.log(idHDS.value)
    // Tự động knewInvoice.idích hoạt hóa đơn vừa tạo

    // Reset trạng thái cho hóa đơn mới
    state.gioHang = [];
    state.detailKhachHang = null;
    resetDiscount();
    isDeliveryEnabled.value = false;
    currentDeliveryInfo.value = null;
    Object.assign(deliveryInfo, {
      tenNguoiNhan: '',
      sdtNguoiNhan: '',
      diaChiCuThe: '',
      tinhThanhPho: undefined,
      quanHuyen: undefined,
      phuongXa: undefined,
    });
    provinceCode.value = null;
    districtCode.value = null;
    wardCode.value = null;
    shippingFee.value = 0;
    isFreeShipping.value = false;
    state.currentPaymentMethod = '0'; // Reset về Tiền mặt
    tienHang.value = 0;
    tongTien.value = 0;
    tongTienTruocGiam.value = 0;
    tienKhachThanhToan.value = 0;
    tienThieu.value = 0;

    // Cập nhật danh sách và các thông tin liên quan
    await clickkActiveTab(newTabId, newInvoice.data.id, newInvoice.data.loaiHoaDon || 'OFFLINE');
    toast.success('Tạo hóa đơn thành công!');
  } catch (error) {
    console.error('Failed to create invoice:', error);
    toast.error('Tạo hóa đơn thất bại!');
  }
}

const closeModal = () => {
  state.isModalOpen = false
}

const closeModalP = () => {
  showDeliveryModal.value = false
  xacNhan(0)
}

const showKhachHangModal = ref(false)
const showProductModal = ref(false) // This is for product selection, not product quantity

// Sản phẩm đã chọn cho modal số lượng
const selectedProduct = ref({ soLuong: 0 })

const setDefaultPaymentMethod = () => {
  state.currentPaymentMethod = '0' // Mặc định là Tiền mặt
}

// Chọn sản phẩm (từ danh sách sản phẩm cho vào giỏ hàng)
const selectKhachHang = async (getIdKH: any) => {
  try {
    const formData = new FormData();
    formData.append('idHD', idHDS.value);
    formData.append('idKH', getIdKH);
    await themKhachHang(formData);
    const responseKH = await GeOneKhachHang(idHDS.value);
    state.detailKhachHang = responseKH;

    // Reset thông tin giao hàng
    Object.assign(deliveryInfo, {
      tenNguoiNhan: '',
      sdtNguoiNhan: '',
      diaChiCuThe: '',
      tinhThanhPho: undefined,
      quanHuyen: undefined,
      phuongXa: undefined,
    });
    provinceCode.value = null;
    districtCode.value = null;
    wardCode.value = null;
    shippingFee.value = 0;

    if (state.detailKhachHang) {
      // Điền thông tin cơ bản
      deliveryInfo.tenNguoiNhan = state.detailKhachHang.ten || '';
      deliveryInfo.sdtNguoiNhan = state.detailKhachHang.sdt || '';
      deliveryInfo.diaChiCuThe = state.detailKhachHang.diaChi || '';

      // Chỉ xử lý địa chỉ nếu hóa đơn là giao hàngTao
      if (loaiHD.value === 'GIAO_HANG') {
        const tinhThanhPhoId = state.detailKhachHang.tinh;
        const quanHuyenId = state.detailKhachHang.huyen;
        const phuongXaId = state.detailKhachHang.xa;

        if (!provinces.value.length) {
          await fetchProvinces();
        }

        if (tinhThanhPhoId) {
          const selectedProvince = provinces.value.find(p => p.code === tinhThanhPhoId.toString());
          if (selectedProvince) {
            deliveryInfo.tinhThanhPho = selectedProvince.value;
            provinceCode.value = parseInt(tinhThanhPhoId);
            await fetchDistricts(provinceCode.value);
          } else {
            console.warn(`Không tìm thấy tỉnh với mã: ${tinhThanhPhoId}`);
          }
        }

        if (quanHuyenId && provinceCode.value) {
          const selectedDistrict = districts.value.find(d => d.code === quanHuyenId.toString());
          if (selectedDistrict) {
            deliveryInfo.quanHuyen = selectedDistrict.value;
            districtCode.value = parseInt(quanHuyenId);
            await fetchWards(districtCode.value);
          } else {
            console.warn(`Không tìm thấy quận/huyện với mã: ${quanHuyenId}`);
            toast.warn('Không tìm thấy quận/huyện trong dữ liệu khách hàng!');
          }
        }

        if (phuongXaId && districtCode.value) {
          const selectedWard = wards.value.find(w => w.code === phuongXaId);
          if (selectedWard) {
            deliveryInfo.phuongXa = selectedWard.value;
            wardCode.value = phuongXaId;
          } else {
            console.warn(`Không tìm thấy phường/xã với mã: ${phuongXaId}`);
            toast.warn('Không tìm thấy phường/xã trong dữ liệu khách hàng!');
          }
        }

        // Bật giao hàng và tính phí nếu có đầy đủ địa chỉ
        if (tinhThanhPhoId && quanHuyenId && phuongXaId) {
          isDeliveryEnabled.value = true;
          await calculateShippingFee();
        } else {
          isDeliveryEnabled.value = false;
          toast.info('Thông tin địa chỉ khách hàng không đầy đủ, vui lòng nhập thủ công.');
        }
      } else {
        isDeliveryEnabled.value = false;
      }

      // Lưu thông tin giao hàng cục bộ
      deliveryInfoByInvoice[idHDS.value] = {
        tenNguoiNhan: deliveryInfo.tenNguoiNhan,
        sdtNguoiNhan: deliveryInfo.sdtNguoiNhan,
        diaChiCuThe: deliveryInfo.diaChiCuThe,
        tinhThanhPho: deliveryInfo.tinhThanhPho,
        quanHuyen: deliveryInfo.quanHuyen,
        phuongXa: deliveryInfo.phuongXa,
        provinceCode: provinceCode.value,
        districtCode: districtCode.value,
        wardCode: wardCode.value,
        shippingFee: shippingFee.value,
      };
    }

    await fetchDiscounts(idHDS.value);
    showKhachHangModal.value = false;
    toast.success('Chọn khách hàng thành công!');
  } catch (error) {
    console.error('Failed to select customer:', error);
    toast.error('Chọn khách hàng thất bại!');
  }
};


const deleteProduc = async (idSPS: any, idHDCT: string) => {
  try {
    const formData = new FormData()
    formData.append('idHD', idHDS.value)
    formData.append('idSP', idSPS)
    formData.append('idHDCT', idHDCT)
    await xoaSP(formData)
    betterDiscountMessage.value = ''
    state.gioHang = state.gioHang.filter((item) => item.id !== idSPS)
    calculateTotalAmounts()

    // Gọi fetchDiscounts bất kể có khách hàng hay không
    await fetchDiscounts(idHDS.value)
    const response = await GetGioHang(idHDS.value)
    state.gioHang = response
    toast.success('Xóa sản phẩm thành công!')
    await capNhatDanhSach()
  } catch (error) {
    console.error('Failed to delete product:', error)
    toast.error('Xóa sản phẩm thất bại!')
  }
}

const fetchProducts = async () => {
  try {
    const params: ParamsGetSanPham = {
      page: stateSP.paginationParams.page,
      size: stateSP.paginationParams.size,
      q: stateSP.searchQuery,
      status: stateSP.searchStatus,
      idMauSac: localColor.value,
      idKichThuoc: localSize.value,
      idDanhMuc: localSelectedCategory.value,
      idChatLieu: localSelectedMaterial.value,
      idThuongHieu: localSelectedBrand.value,
      idLoaiDe: localSelectedSoleType.value,
    };
    // const responseKhachHang = await GetKhachHang({ q: customerSearchQuery.value });
    const response = await GetSanPhams(params);
    console.log(response)
    stateSP.products = response.data?.data || [];
    state.products = response.data?.data || [];
    // state.khachHang = responseKhachHang.data?.data || [];
    stateSP.totalItems = response.data?.totalElements || 0;
    state.totalItems = response.data?.totalElements || 0;
  } catch (error) {
    console.error('Failed to fetch products or customers:', error);
    toast.error('Lấy danh sách sản phẩm hoặc khách hàng thất bại!');
  }
};

watch(customerSearchQuery, () => {
  state.paginationParams.page = 1; // Reset về trang đầu khi tìm kiếm
  debouncedFetchCustomers();
});

const confirmQuantity = async () => {
  try {
    if (selectedProduct.value.soLuong <= 0) {
      toast.error('Số lượng không hợp lệ!')
      return
    }
    const formData = new FormData()
    formData.append('soLuong', selectedProduct.value.soLuong.toString())
    formData.append('idHD', idHDS.value)
    formData.append('idSP', idSP.value)

    const res = await themSanPham(formData)

    if (res.message === 'Số lượng sản phẩm thêm vào nhiều hơn số lượng trong kho') {
      toast.error(res.message)
      return
    }

    state.isModalOpen = false
    console.log(res)
    console.log(res.message)

    if (res.message.includes("thay đổi giá từ")) {
      toast.warning(res.message);
      setTimeout(() => {
        toast.success("Thêm thành công");
      }, 5000);
      toast.success("thêm thành công")
    } else {
      toast.success(res.message)
    }
    console.log("2")

    const response = await GetGioHang(idHDS.value)
    state.gioHang = response

    calculateTotalAmounts()

    // Gọi fetchDiscounts bất kể có khách hàng hay không
    await fetchDiscounts(idHDS.value)

    await fetchProducts()
    await capNhatDanhSach()
  } catch (error: any) {
    if (error?.response?.data?.message) {
      toast.error(error.response.data.message)
    } else {
      toast.error('Thêm sản phẩm vào giỏ hàng thất bại!')
    }
  }
}

const confirmQuantityP = async () => {
  showDeliveryModal.value = false
  applyBestDiscount();
  xacNhan(0)
}

// Hàm định dạng tiền tệ
const formatCurrency = (value: number) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value)
}

// Hàm tính toán lại tổng tiền hàng, giảm giá, tổng tiền cuối cùng
const calculateTotalAmounts = () => {
  tienHang.value = state.gioHang.reduce((sum, item) => sum + item.giaBan * item.soLuong, 0)
  giamGia.value = selectedDiscount.value ? selectedDiscount.value.giaTriGiamThucTe || 0 : 0
  tongTienTruocGiam.value = tienHang.value
  let currentTotal = tienHang.value - giamGia.value;
  if (currentTotal < 0) currentTotal = 0; // Ensure total doesn't go negative due to discount

  if (isDeliveryEnabled.value) {
    currentTotal += shippingFee.value;
  }
  tongTien.value = currentTotal;
  console.log("tỏng" + tongTien.value)
  tienThieu.value = tongTien.value - tienKhachThanhToan.value; // Update tienThieu based on new tongTien
}

// Gọi hàm tính toán khi giỏ hàng thay đổi
watch(() => state.gioHang, calculateTotalAmounts, { deep: true })
watch(giamGia, calculateTotalAmounts); // Watch discount changes
watch(shippingFee, calculateTotalAmounts); // Watch shipping fee changes

const capNhatDanhSach = async () => {
  const response = await GetHoaDons()
  if (response && Array.isArray(response)) {
    tabs.value = response.map((invoice, index) => ({
      id: index + 1,
      idHD: invoice.id,
      ma: invoice.ma,
      soLuong: invoice.soLuong,
      loaiHoaDon: invoice.loaiHoaDon,
      products: invoice.data?.products || []
    }))
  }
}

const isQrVNpayModalVisible = ref(false)
const qrVnPayLoading = ref(false) // Unused

const openQrModalVNPayCaHai = () => {

  isBothPaymentModalVisible.value = true;
  amountPaid.value = 0;

};


const openQrModalVNPay = () => {

  isQrVNpayModalVisible.value = true;

};

const closeQrModalVnPay = () => {
  isQrVNpayModalVisible.value = false
}

const isQrModalVisible = ref(false) // For product QR scan
const qrData = ref('')
const hasCamera = ref(true); // To check camera availability

let html5QrCode: Html5Qrcode

const openQrModal = () => {
  isQrModalVisible.value = true
  nextTick(() => {
    startQrScanning()
  })
}

function selectProduct(idSPS: any) {
  if (!idHDS.value) {
    toast.error('Vui lòng tạo hoặc chọn hóa đơn trước khi thêm sản phẩm!');
    return;
  }
  idSP.value = idSPS
  selectedProduct.value.soLuong = 1 // Đặt số lượng mặc định là 1 khi chọn sản phẩm
  state.isModalOpen = true // Open quantity modal
  showProductModal.value = false // Close product selection modal if open
}

const startQrScanning = () => {
  const qrRegionId = 'reader'
  const qrRegionElement = document.getElementById(qrRegionId)
  if (!qrRegionElement) {
    console.error("Không tìm thấy phần tử với id 'reader'")
    return
  }

  html5QrCode = new Html5Qrcode(qrRegionId)

  Html5Qrcode.getCameras()
    .then((cameras: { id: string; label: string }[]) => {
      if (cameras && cameras.length) {
        hasCamera.value = true;
        const cameraId = cameras[0].id // Use the first camera found
        html5QrCode.start(
          cameraId,
          { fps: 10, qrbox: 250 },
          (qrCodeMessage: string) => {
            qrData.value = qrCodeMessage
            selectProduct(qrCodeMessage) // Select product using scanned ID
            html5QrCode.stop()
            closeQrModal()
          },
          (errorMessage) => {
            console.warn('Lỗi đọc QR: ', errorMessage)
          }
        )
      } else {
        console.warn('Không tìm thấy camera nào!')
        hasCamera.value = false;
        toast.error('Không tìm thấy camera hoặc không có quyền truy cập camera!');
      }
    })
    .catch((error: any) => {
      console.error('Lỗi khi lấy camera: ', error)
      hasCamera.value = false;
      toast.error('Lỗi khi truy cập camera: ' + error.message);
    })
}

const closeQrModal = () => {
  isQrModalVisible.value = false
  stopQrScanning()
}

const stopQrScanning = () => {

  html5QrCode.stop().catch((err) => console.error('Không thể dừng scanner:', err))

}

const addCustomerLoading = ref(false);

// Function to add a new customer (Not implemented in the template, but good to have a placeholder)
const addCustomer = async () => {
  try {
    // Kiểm tra dữ liệu đầu vào
    if (!newCustomer.ten || !newCustomer.sdt) {

      return;
    }

    // Kiểm tra định dạng số điện thoại (10 chữ số)
    const phoneRegex = /^[0-9]{10}$/;
    if (!phoneRegex.test(newCustomer.sdt)) {
      toast.error('Số điện thoại phải là 10 chữ số!');
      return;
    }

    if (!idHDS.value) {
      toast.error('Vui lòng chọn hoặc tạo hóa đơn trước khi thêm khách hàng!');
      return;
    }

    addCustomerLoading.value = true;

    // Gọi API để thêm khách hàng mới
    const formData = new FormData();
    formData.append('ten', newCustomer.ten);
    formData.append('sdt', newCustomer.sdt);

    // Giả định API `themKhachHang` trả về ID của khách hàng vừa thêm
    const response = await themMoiKhachHang(formData);
    const newCustomerId = response.data.id; // Giả định API trả về ID khách hàng

    // Tự động chọn khách hàng vừa thêm cho hóa đơn
    const selectFormData = new FormData();
    selectFormData.append('idHD', idHDS.value);
    selectFormData.append('idKH', newCustomerId);
    await themKhachHang(selectFormData); // Gọi lại API để gán khách hàng vào hóa đơn

    // Cập nhật thông tin khách hàng hiển thị
    const responseKH = await GeOneKhachHang(idHDS.value);
    state.detailKhachHang = responseKH.id ? responseKH : null;

    // Reset input sau khi thêm thành công
    newCustomer.ten = '';
    newCustomer.sdt = '';

    toast.success('Thêm và chọn khách hàng thành công!');
  } catch (error) {
    console.error('Failed to add customer:', error);
    toast.error('Thêm khách hàng thất bại!');
  } finally {
    addCustomerLoading.value = false;
  }
}


onMounted(async () => {
  const storedDiscount = localStorage.getItem('selectedDiscount');
  const storedBestDiscount = localStorage.getItem('isBestDiscountApplied');
  if (storedDiscount) {
    selectedDiscount.value = JSON.parse(storedDiscount);
  }
  if (storedBestDiscount) {
    isBestDiscountApplied.value = JSON.parse(storedBestDiscount);
  }
  const storedDeliveryInfo = localStorage.getItem('deliveryInfoByInvoice');
  if (storedDeliveryInfo) {
    Object.assign(deliveryInfoByInvoice, JSON.parse(storedDeliveryInfo));
  }
  await fetchCustomers();
  await fetchHoaDon()
  if (idHDS.value) {
    await fetchDiscounts(idHDS.value);
  }
  setDefaultPaymentMethod()
  await fetchProvinces()
})
</script>


<style scoped>
.main-layout {
  display: flex;
  width: 100%;
  /* Or whatever maximum width you want for the whole content area */
  box-sizing: border-box;
  /* Important for consistent sizing */
  /* Add a gap between columns if desired */
  gap: 20px;
  /* Adjust as needed */
  /* Remove any fixed height that might restrict content */
  height: auto;
}

/* Card Styling */
.card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h3 {
  margin: 0;
  font-size: 18px;
  color: #333;
}



/* Top Header */
.top-header {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-and-create-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-box-top {
  display: flex;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
  width: 60%;
}

.search-input-top {
  border: none;
  padding: 8px 12px;
  flex-grow: 1;
  outline: none;
}

.search-icon-top {
  background-color: #fff;
  border: none;
  padding: 8px 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-create-new-invoice {
  background-color: #54bddb;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 8px 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Pending Invoices */
.pending-invoices-container {
  display: flex;
  gap: 15px;
  padding: 15px 20px;
  overflow-x: auto;
  white-space: nowrap;
}

.pending-invoice-card {
  flex-shrink: 0;
  width: 180px;
  /* Adjust width as needed */
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  padding: 15px;
  display: flex;
  flex-direction: column;
  gap: 5px;
  cursor: pointer;
  transition: all 0.2s;
  position: relative;
  background-color: #fdfdfd;
}

.pending-invoice-card.active {
  border-color: #54bddb;
  box-shadow: 0 0 0 2px rgba(64, 150, 255, 0.2);
}

.pending-invoice-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.invoice-id {
  font-weight: bold;
  color: #333;
}

.invoice-status {
  background-color: #ffe7ba;
  color: #fa8c16;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  width: fit-content;
}

.invoice-product-count {
  font-size: 14px;
  color: #666;
}

.delete-invoice-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  padding: 5px;
  border-radius: 50%;
  transition: background-color 0.2s;
}

.delete-invoice-btn:hover {
  background-color: #f0f0f0;
  color: #e50000;
}

/* Search Box (General) */
.search-box {
  display: flex;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  overflow: hidden;
  width: 250px;
}

.search-input {
  border: none;
  padding: 8px 12px;
  flex-grow: 1;
  outline: none;
}

.search-icon {
  background-color: #fff;
  border: none;
  padding: 8px 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Cart Actions */
.actions-add-product {
  padding-bottom: 15px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 15px;
}

.empty-cart {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: #999;
}

.empty-cart img {
  margin-bottom: 10px;
}

/* Quantity Controls in Cart */
.center-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.quantity-btn {
  background-color: #f0f0f0;
  border: 1px solid #d9d9d9;
  padding: 4px 8px;
  width: 50px;
  cursor: pointer;
  font-weight: bold;
}

.quantity-input {
  width: 40px;
  text-align: center;
  border: 1px solid #d9d9d9;
  border-left: none;
  border-right: none;
  padding: 4px 0;
  -moz-appearance: textfield;
  /* Firefox */
  appearance: textfield;
}

.quantity-input::-webkit-outer-spin-button,
.quantity-input::-webkit-inner-spin-button {
  -webkit-appearance: none;
  margin: 0;
}

/* Customer Search */
.search-customer {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
  align-items: center;
  width: 100%;
}

.search-input-customer {
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 8px 12px;
  flex-grow: 1;
  outline: none;
}

.search-icon-customer {
  background-color: #fff;
  border: 1px solid #d9d9d9;
  border-left: none;
  padding: 8px 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 0 4px 4px 0;
}

.btn-add-customer {
  background-color: #54bddb;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 8px 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  white-space: nowrap;
}

.customer-info-display {
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.customer-info-display h4 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
}

.info-group {
  display: flex;
  flex-direction: column;
  margin-bottom: 10px;
}

.info-group span {
  font-weight: bold;
  color: #555;
  margin-bottom: 5px;
}

.input-full-width,
textarea.input-full-width {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  outline: none;
  resize: vertical;
}

/* Payment Section */
.payment-section-content {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.form-group-payment label {
  font-weight: bold;
  margin-bottom: 5px;
  display: block;
}

.select-discount {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background-color: #fff;
}

.payment-summary {
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  font-size: 16px;
}

.summary-item label {
  font-weight: normal;
  color: #555;
  margin-left: 10px;
}

.summary-item span {
  font-weight: bold;
  color: #333;
  margin-right: 10px;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #000;
  border-top: 1px dashed #e0e0e0;
  padding-top: 10px;
  margin-top: 5px;
}

.btn-confirm-payment {
  background-color: #54bddb;
  color: #fff;
  border: none;
  border-radius: 4px;
  padding: 12px 20px;
  cursor: pointer;
  font-size: 16px;
  margin-top: 15px;
}

/* Modals */
.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 800px;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: #fff;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  overflow-y: auto;
}

.modal-title {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 20px;
  color: #333;
  text-align: center;
}

/* Payment Modal Specific Styles */
.payment-modal-content {
  padding: 10px;
}

.payment-info {
  margin-bottom: 20px;
  font-size: 18px;
  text-align: center;
}

.tabs-payment-method {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.payment-method-summary {
  text-align: center;
  margin-bottom: 20px;
  font-size: 16px;
  padding: 10px;
  background-color: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 4px;
}

.payment-details-table {
  margin-bottom: 20px;
}

.footer-payment-summary {
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
  text-align: right;
  font-size: 16px;
}

.footer-info div {
  margin-bottom: 5px;
}

.top-header {
  border-radius: 5px;
  background-color: #54bddb;
  height: 60PX;
  display: flex;
  justify-content: flex-end;
  /* Căn phải nội dung bên trong top-header */
  margin-bottom: 20px;
}

.search-and-create-section {
  display: flex;
  align-items: center;
  gap: 15px;
  /* Khoảng cách giữa ô tìm kiếm và nút */
  width: 100%;
  /* Đảm bảo nó chiếm đủ không gian để căn chỉnh */
  justify-content: flex-end;
  /* Căn phải các item bên trong */
}

.search-box-top {
  display: flex;
  align-items: center;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  margin-left: 15px;
  /* Bo tròn các góc */
  padding: 5px 10px;
  height: 30px;
  width: 100%;
  background-color: #fff;
  flex-grow: 1;
  /* Cho phép nó mở rộng để chiếm không gian còn lại */
  max-width: 1420px;
  /* Giới hạn độ rộng tối đa */
}

.search-input-top {
  width: 100%;
  /* Đảm bảo ô input chiếm toàn bộ chiều rộng của search-box */
  border: none;
  outline: none;
  height: 30px;
  flex-grow: 1;

  padding: 5px;
  font-size: 14px;

}

.search-icon-top {
  background-color: white;
  background: none;
  border: none;
  cursor: pointer;
  color: #8c8c8c;
  padding-left: 5px;
}

.btn-create-new-invoice {
  color: black;
  background-color: white;
  padding: 10px 15px;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  height: 30px;
  align-items: center;
  gap: 5px;
  margin-right: 30px;
  white-space: nowrap;
}

/* Header trên cùng */
.top-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.search-icon-top {
  background: none;
  border: none;
  cursor: pointer;
  color: #8c8c8c;
  padding-left: 5px;
}

.left-column {
  /*
  flex-grow: 0;   -> Do not grow beyond flex-basis
  flex-shrink: 0; -> Do not shrink below flex-basis
  flex-basis: 80%; -> Initial size is 80% of the parent container
  */
  flex: 0 0 60%;
  box-sizing: border-box;
  /* Important for consistent sizing */
  /* Set a min-width if you want to prevent it from getting too small,
      but be careful not to make it too large and cause overflow. */
  min-width: 600px;
  /* Example: adjust based on your content. Remove if not needed. */
}

.right-column {
  /*
  flex-grow: 1;   -> Grow to fill any available space
  flex-shrink: 1; -> Allow shrinking if space is limited
  flex-basis: 0%; -> Initial size is 0 (it will expand based on flex-grow)
  */
  flex: 1;
  /* This is a shorthand for flex-grow: 1, flex-shrink: 1, flex-basis: 0% */
  box-sizing: border-box;
  /* Important for consistent sizing */
  /* Set a min-width if you want to prevent it from getting too small. */
  min-width: 250px;
  /* Example: adjust based on your content. Remove if not needed. */
}

/* Trong phần <style scoped> của bạn */

/* Khu vực phương thức thanh toán trong cột phải */
.payment-method-options {
  display: flex;
  gap: 12px;
  /* Khoảng cách giữa các nút */
  margin-top: 10px;
  justify-content: space-between;
  /* Đảm bảo các nút phân bố đều */
}

.btn-payment-option {
  flex: 1;
  /* Các nút chiếm đều không gian */
  padding: 12px 16px;
  border: 1px solid #d9d9d9;
  /* Viền nhẹ */
  border-radius: 8px;
  /* Bo góc mượt mà */
  background-color: #f5f5f5;
  /* Nền xám nhạt */
  color: #333;
  /* Màu chữ tối */
  font-size: 12px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  /* Hiệu ứng chuyển đổi mượt */
}

.btn-payment-option:hover {
  background-color: #e6f7ff;
  /* Màu xanh nhạt khi hover */
  border-color: #54bddb;
  /* Viền xanh khi hover */
  color: #54bddb;
  /* Chữ xanh khi hover */
}

.btn-payment-option.active {
  background-color: #54bddb;
  /* Màu xanh chính khi được chọn */
  border-color: #54bddb;
  color: white;
  /* Chữ trắng */
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  /* Bóng nhẹ */
}

/* Nút xác nhận thanh toán */
.btn-confirm-payment {
  background-color: #54bddb;
  /* Màu xanh chính */
  color: white;
  border: none;
  border-radius: 8px;
  padding: 12px 20px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  width: 100%;
  margin-top: 20px;
  transition: background-color 0.3s ease;
}

.btn-confirm-payment:hover {
  background-color: #3a9cc4;
  /* Màu xanh đậm hơn khi hover */
}

/* Modal Thanh toán */
.payment-modal-content {
  padding: 20px;
  /* Tăng padding cho không gian thoáng hơn */
  background-color: #fff;
  border-radius: 8px;
}

.payment-info {
  margin-bottom: 20px;
  font-size: 18px;
  text-align: center;
  color: #333;
  font-weight: 500;
}

.payment-info strong {
  color: #000;
}

/* Tabs phương thức thanh toán trong modal */
.tabs-payment-method {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  justify-content: center;
}

/* Input số tiền thanh toán */
.payment-amount-input {
  margin-bottom: 20px;
}

.payment-amount-input label {
  display: block;
  font-weight: 500;
  margin-bottom: 8px;
  color: #333;
}

.payment-amount-input input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  font-size: 14px;
  color: #333;
  background-color: #fafafa;
  transition: border-color 0.3s ease;
}

.payment-amount-input input:focus {
  border-color: #54bddb;
  outline: none;
  box-shadow: 0 0 0 2px rgba(84, 189, 219, 0.2);
}

/* Tổng kết thanh toán trong modal */
.payment-method-summary {
  text-align: center;
  margin-bottom: 20px;
  font-size: 16px;
  font-weight: 600;
  color: #ff4d4f;
  /* Màu đỏ nổi bật */
  background-color: #fff1f0;
  /* Nền đỏ nhạt */
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #ffa39e;
}

/* Bảng chi tiết phương thức thanh toán */
.payment-details-table {
  margin-bottom: 20px;
}

:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa !important;
  font-weight: 600;
  color: #333;
  text-align: center;
}

:deep(.ant-table-tbody > tr > td) {
  text-align: center;
  color: #333;
}

:deep(.ant-tag) {
  font-size: 14px;
  padding: 4px 8px;
  border-radius: 4px;
}

/* Tùy chỉnh màu cho từng phương thức thanh toán */

/* Footer tổng kết thanh toán */
.footer-payment-summary {
  border-top: 1px solid #f0f0f0;
  padding-top: 15px;
  text-align: right;
}

.footer-info div {
  margin-bottom: 8px;
  font-size: 15px;
  color: #333;
}

.footer-info strong {
  font-weight: 600;
  color: #000;
}

/* Nút trong modal footer */
:deep(.ant-modal-footer .ant-btn-primary) {
  /* background-color: #54bddb; */
  border-color: #54bddb;
  border-radius: 8px;
  padding: 8px 20px;
  font-size: 14px;
}

:deep(.ant-modal-footer .ant-btn-primary:hover) {
  background-color: #3a9cc4;
  border-color: #3a9cc4;
}

:deep(.ant-modal-footer .ant-btn-default) {
  border-radius: 8px;
  padding: 8px 20px;
  font-size: 14px;
  color: #333;
  border-color: #d9d9d9;
}

:deep(.ant-modal-footer .ant-btn-default:hover) {
  border-color: #54bddb;
  color: #54bddb;
}

/* Card chung */
.card {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.card-header {
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.card-header h3 {
  margin: 0;
  font-size: 16px;
}

/* Tab info (chỉ có trong card "Hóa đơn chờ") */
.tab-info {
  display: flex;
  gap: 10px;
}

.tab-item {
  background-color: #e6f7ff;
  /* Light blue */
  color: #54bddb;
  /* Blue text */
  padding: 5px 10px;
  border-radius: 4px;
  font-size: 12px;
}

.select-discount-button {
  background-color: #54bddb;
  border: none;
  padding: 8px 12px;
  color: white;
  cursor: pointer;
  white-space: nowrap;
}

.tab-item.active {
  background-color: #54bddb;
  color: white;
}

/* Tabs cho hóa đơn */
.tabs-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 20px 0;
  /* Chỉ padding trên và ngang */
  border-bottom: 1px solid #f0f0f0;
}

.tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  /* Đảm bảo các tab xuống dòng nếu quá nhiều */
}

.tab-button {
  text-align: center;
  padding: 8px 15px;
  /* Tăng padding để nút to hơn */
  background: #f0f0f0;
  /* Màu nền xám nhạt */
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-weight: 500;
  color: #595959;
  transition: all 0.3s ease;
}

.tab-button.active {
  background: #54bddb;
  color: white;
  box-shadow: 0 2px 0 rgba(45, 140, 240, 0.1);
}

.btn-close-tab {
  background: none;
  border: none;
  cursor: pointer;
  color: #8c8c8c;
  font-size: 18px;
  padding: 5px;
}

/* Nội dung hiển thị tab (giỏ hàng) */
.tab-content-display {
  padding: 20px;
}

.actions-add-product {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 15px;
}

.btn-add-product {
  background-color: #54bddb;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
  height: 40px;
  width: 120px;
}

.empty-cart {
  text-align: center;
  color: #777;
  padding: 50px 0;
}

/* Ant Design Table Overrides */
:deep(.ant-table-wrapper) {
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  overflow: hidden;
  /* Ensure rounded corners */
}

:deep(.ant-table-thead > tr > th) {
  background-color: #fafafa !important;
  font-weight: bold;
  text-align: center !important;
}

:deep(.ant-table-tbody > tr > td) {
  text-align: center;
}

.center-cell {
  display: flex;
  justify-content: center;
  align-items: center;
}

.quantity-btn {
  padding: 4px 10px;
  /* Nhỏ hơn một chút */
  background-color: #54bddb;
  color: white;
  border: none;
  font-size: 16px;
  /* Nhỏ hơn một chút */
  cursor: pointer;
  border-radius: 4px;
  margin: 0 2px;
}

.quantity-input {
  width: 40px;
  /* Nhỏ hơn */
  text-align: center;
  font-size: 16px;
  border: 1px solid #d9d9d9;
  padding: 4px;
  border-radius: 4px;
  margin: 0 5px;
}

.highlight {
  color: red;
  font-weight: bold;
}

.btn-choose-product,
.btn-delete-product {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 14px;
}

/* Phần "Giỏ hàng" (dưới "Hóa đơn chờ") */
.search-box {
  display: flex;
  align-items: center;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 5px 10px;
  background-color: #fff;
  width: 250px;
  /* Điều chỉnh độ rộng */
}

.search-input {
  border: none;
  outline: none;
  flex-grow: 1;
  padding: 3px;
}

.search-icon {
  background: none;
  border: none;
  cursor: pointer;
  color: #8c8c8c;
  padding-left: 5px;
}

/* Phần "Khách hàng" (cột phải) */
.search-customer {
  display: flex;
  align-items: center;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 5px 10px;
  background-color: #fff;
  width: 100%;
  /* Chiếm toàn bộ chiều rộng */
  margin-bottom: 10px;
}

.search-input-customer {
  border: none;
  outline: none;
  flex-grow: 1;
  padding: 3px;
}

.search-icon-customer {
  background: none;
  border: none;
  cursor: pointer;
  color: #8c8c8c;
  padding-left: 5px;
}

.btn-add-customer {
  background-color: #54bddb;
  color: white;
  padding: 8px 15px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 14px;
  margin-bottom: 20px;
}

.customer-info-display h4 {
  margin-top: 0;
  margin-bottom: 15px;
  font-size: 16px;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.info-group {
  margin-bottom: 15px;
}

.info-group span {
  display: block;
  font-weight: bold;
  margin-bottom: 5px;
}

.input-full-width {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  box-sizing: border-box;
  /* Đảm bảo padding không làm tăng chiều rộng */
}

textarea.input-full-width {
  min-height: 60px;
  resize: vertical;
}

/* Phần "Thanh toán" (cột phải) */
.payment-section-content {
  padding-top: 20px;
}

.form-group-payment {
  margin-bottom: 15px;
}

.form-group-payment label {
  font-weight: bold;
  display: block;
  margin-bottom: 5px;
}

.select-discount {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  background-color: #fff;
}

.payment-summary {
  margin-top: 20px;
  border-top: 1px dashed #e0e0e0;
  /* Đường nét đứt */
  padding-top: 15px;
}

.discount-input {
  border: none;
  padding: 8px 12px;
  flex-grow: 1;
  outline: none;
}

.summary-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
  font-size: 15px;
}

.summary-item label {
  font-weight: normal;
  color: #595959;
}

.summary-item span {
  font-weight: bold;
}

.total-amount {
  font-size: 18px;
  font-weight: bold;
  color: #ff4d4f;
  /* Màu đỏ nổi bật */
  border-top: 1px solid #f0f0f0;
  padding-top: 10px;
  margin-top: 15px;
}

.btn-confirm-payment {
  background-color: #54bddb;
  /* Màu xanh Ant Design */
  color: white;
  padding: 12px 20px;
  border: none;
  border-radius: 8px;
  /* Bo tròn nhiều hơn */
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  width: 100%;
  /* Chiếm toàn bộ chiều rộng */
  margin-top: 20px;
  transition: background-color 0.3s;
}

.btn-confirm-payment:hover {
  background-color: #54bddb;
}

.tab-button-payment {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  background-color: #f5f5f5;
  color: #333;
  font-size: 14px;
  font-weight: normal;
  /* Chữ không đậm khi không chọn */
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  /* Sử dụng flex để căn chỉnh văn bản và biểu tượng */
  align-items: center;
  /* Căn giữa theo chiều dọc */
  justify-content: center;
  /* Căn giữa theo chiều ngang */
  gap: 8px;
  /* Khoảng cách giữa văn bản và biểu tượng */
}

.discount-code-section {
  margin-bottom: 20px;
}

.discount-code-section label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.tab-button-payment.active {
  background-color: #54bddb;
  border-color: #54bddb;
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  font-weight: bold;
  /* In đậm khi chọn */
}

.tab-button-payment svg {
  width: 16px;
  height: 16px;
}

.discount-code-section {
  margin-bottom: 20px;
}

.discount-code-section label {
  display: block;
  margin-bottom: 5px;
  font-weight: bold;
}

.payment-method-summary {
  margin-bottom: 15px;
  text-align: center;
}

.payment-details-table {
  margin-bottom: 20px;
}

.footer-payment-summary {
  display: flex;
  justify-content: space-between;
  margin-top: 20px;
  padding-top: 15px;
  border-top: 1px solid #f0f0f0;
}

.footer-info div {
  margin-bottom: 5px;
  font-size: 15px;
}

.footer-info strong {
  font-weight: bold;
}

/* Your existing styles (like .modal-backdrop, .modal-content, etc.) should be kept if they are general and work well. */
/* Just make sure there are no conflicting styles */

.khach-hang-cart {
  color: black;
}

/* Các style ban đầu của bạn, giữ lại và có thể điều chỉnh nếu cần */
.btn-create {
  background-color: #54bddb;
  color: white;
  padding: 8px 14px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 10px;
}

.required {
  color: red;
}

.input-wrapper {
  position: relative;
  height: 30px;
  width: 100%;
}

.input-wrapper .icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #888;
}

.input-wrapper input {
  width: 100%;
  padding: 10px 10px 10px 35px;
  border-radius: 6px;
  border: 1px solid #ccc;
  background-color: #f2f2f2;
}



.add-button {
  width: 100%;
  padding: 12px;
  background-color: #54bddb;
  color: white;
  border: none;
  border-radius: 6px;
  margin-top: 16px;
  cursor: pointer;
  transition: background 0.3s;
}

.add-button:hover {
  background-color: #54bddb;
}

/* Trong phần CSS của bạn */

/* Bổ sung cho giao diện mềm mại hơn (có thể điều chỉnh thêm) */
.card {
  border-radius: 12px;
  /* Tăng bo góc cho card */
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.08);
  /* Bóng đổ mềm hơn */
}

.input-wrapper input,
.select-discount,
.quantity-input {
  border-radius: 8px;
  /* Bo góc cho input và select */
}

.quantity-btn {
  border-radius: 6px;
  /* Bo góc cho nút tăng/giảm số lượng */
}

/* CSS cho nhóm nút khách hàng */
.button-group-customer {
  display: flex;
  /* Biến container thành flex container */
  gap: 10px;
  /* Khoảng cách giữa các nút */
  margin-top: 16px;
  /* Giữ khoảng cách trên như ban đầu của add-button */
  padding: 0 20px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  /* Subtle shadow */
  /* Thêm padding ngang và dưới để căn chỉnh với body card */
  /* Nếu bạn muốn button-group-customer nằm trong card-body, hãy di chuyển nó vào trong đó */
  /* Hiện tại tôi đang giả định nó nằm ngay sau div đóng của detailKhachHang/v-else */
}

.button-group-customer .add-button {
  flex: 1;
  /* Mỗi nút sẽ chiếm một phần bằng nhau trong không gian khả dụng */
  width: auto;
  /* Ghi đè width: 100% để flex hoạt động */
  padding: 12px;
  /* Giữ padding như cũ */
  font-size: 15px;
  /* Điều chỉnh cỡ chữ cho vừa phải */
  border-radius: 8px;
  /* Bo góc cho nút */
  margin-top: 0;
  /* Bỏ margin-top ở đây vì đã có trên button-group-customer */
}

.form-group1 {
  width: 400px;
  display: block;
  justify-self: center;
  margin-top: 10px;
  margin-bottom: 30px;
}

.form-group {
  width: 300px;
  display: block;
  justify-self: center;
  margin-top: 10px;
}

.card-body {

  /* Đã có */
}



.large-note {
  font-size: 16px;
  padding: 12px 16px;
  line-height: 1.6;
  width: 100%;
  max-width: 600px;
  min-width: 300px;
  border: 1px solid #e8e8e8 !important;
}




.discount-input-group {
  display: flex;

  border-radius: 4px;
  overflow: hidden;
}

.page-container {
  padding: 20px;
  /* Overall padding for the page content */
}

.breadcrumb-section {
  margin-bottom: 25px;
  /* Space below the breadcrumb and above the first section */
  background-color: #fff;
  /* White background for the breadcrumb box */
  padding: 15px 20px;
  /* Padding inside the breadcrumb box */
  border-radius: 8px;
  /* Rounded corners */
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  /* Subtle shadow */
}

.section-title {
  margin-top: 30px;
  font-size: 10px;
  font-weight: bold;
  margin-bottom: 20px;
  margin-left: 0px;
  color: #333;
  display: flex;
  align-items: center;
  gap: 8px;
}

/* .btn-payment-option.active {
  background-color: #54bddb;
  border-color: #54bddb;
  color: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  font-weight: bold;

} */

.modal-backdrop {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  padding: 20px;
  border-radius: 8px;
  width: 1300px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-title {
  font-size: 1.5rem;
  margin-bottom: 15px;
}

.discount-code-section {
  margin-bottom: 20px;
}

.discount-label {
  font-weight: 500;
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  display: block;
}

.discount-input-group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.discount-input {
  border-radius: 8px !important;
  border: 1px solid #d9d9d9;
  transition: border-color 0.3s;
}

.discount-input:hover {
  border-color: #54bddb;
}

.select-discount-button {
  border-radius: 8px;
  padding: 0 20px;
  height: 40px;
  font-weight: 500;
  transition: all 0.3s;
}

.select-discount-button:hover {
  background-color: #3ca8c9 !important;
  border-color: #3ca8c9 !important;
}

.discount-info {
  margin-top: 10px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background-color: #f6ffed;
  border: 1px solid #b7eb8f;
  border-radius: 4px;
  font-size: 14px;
}

.clear-discount-btn {
  color: #ff4d4f;
}

.clear-discount-btn:hover {
  color: #d9363e;
}

.discount-modal .ant-modal-body {
  padding: 24px;
}

.discount-list {
  max-height: 400px;
  overflow-y: auto;
}

.discount-list-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.discount-list-item:hover {
  background-color: #f5f5f5;
}

.discount-title {
  font-size: 16px;
  font-weight: 500;
  color: #1890ff;
  cursor: pointer;
}

.discount-title:hover {
  color: #40a9ff;
}

.discount-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.discount-description {
  font-size: 12px;
  color: #888;
}

.ant-list-item-action>.ant-btn {
  border-radius: 4px;
  padding: 4px 12px;
}

@media (max-width: 768px) {
  .discount-input-group {
    flex-direction: column;
    gap: 8px;
  }

  .select-discount-button {
    width: 100%;
  }

  .discount-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}

/* Cải thiện giao diện phần giao hàng */
.delivery-info-section {
  background: #f1f5f9;
  /* Màu nền nhạt, tông xanh pastel */
  padding: 20px;

  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
  /* Đường viền trái màu xanh lá */
}



.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: #1e3a8a;
  /* Màu xanh đậm cho nhãn */
  margin-bottom: 6px;
  letter-spacing: 0.5px;
}

.form-group .required {
  color: #dc3545;
  /* Màu đỏ cho dấu * */
}

.input-wrapper {
  position: relative;
}



.input-customer::placeholder {
  color: #6c757d;
  height: 32px;

  /* Màu xám nhạt cho placeholder */
}

.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  font-size: 15px;
  background-color: #e9ecef;
  border-radius: 6px;
  margin-top: 10px;
}

.summary-item label {
  font-weight: 600;
  color: #2d3748;
}

.shipping-fee {
  font-weight: 700;

  font-size: 16px;
}

.delivery-toggle-container {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 15px;
}

.delivery-toggle-container :deep(.ant-switch-checked) {
  background-color: #007bff;
  /* Màu xanh lá cây mặc định của Ant Design */
  /* Hoặc một màu xanh lá cây khác mà bạn thích */
  /* background-color: #4CAF50; */
}

.my-button:hover {
  background-color: #007bff;
  /* Ví dụ: #f0f0f0 (màu xám nhạt) hoặc màu xanh bạn muốn */
  /* Hoặc nếu bạn muốn bỏ hiệu ứng nền, có thể dùng */
  /* background-color: transparent; */
}

.delivery-toggle-container label {
  font-size: 14px;
  font-weight: 600;
  color: #1e3a8a;
}

:deep(.ant-switch) {
  background-color: #ced4da;
  /* Màu xám khi tắt */
}



/* Responsive adjustments */
@media (max-width: 768px) {
  .delivery-form {
    grid-template-columns: 1fr;
    /* Chuyển thành 1 cột trên mobile */
  }
}

/* Filter Container */
.filter-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  padding: 20px;
  background: #f8fafc;
  border-radius: 8px;
  margin-bottom: 20px;
}

.filter-item {
  display: flex;
  flex-direction: column;
}

.filter-label {
  font-size: 14px;
  font-weight: 600;
  color: black;
  margin-bottom: 8px;
}

.search-input-wrapper {
  position: relative;
}

.search-box {
  display: flex;
  align-items: center;
  border: 1px solid #d9d9d9;
  border-radius: 8px;
  background: #fff;
  transition: border-color 0.3s ease, box-shadow 0.3s ease;
}

.search-box:hover,
.search-box:focus-within {
  border-color: #54bddb;
  box-shadow: 0 0 5px rgba(84, 189, 219, 0.3);
}

:deep(.ant-input) {

  background: transparent !important;
  padding: 10px 10px 10px 36px !important;
  font-size: 14px;
  color: #333;
  height: 40px;
}

:deep(.ant-input-prefix) {
  margin-right: 8px;
  color: #8c8c8c;
}

:deep(.ant-input::placeholder) {
  color: #bfbfbf;
}

:deep(.ant-select) {
  width: 100%;
}

:deep(.ant-select-selector) {
  border-radius: 8px !important;
  height: 40px !important;
  display: flex;
  align-items: center;
}

:deep(.ant-select-selection-placeholder) {
  color: #bfbfbf;
}

.reset-button-group {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  /* Subtle shadow */
  display: flex;
  align-items: flex-end;
}

.reset-button {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  /* Subtle shadow */
  width: 100%;
  height: 40px;
  border-radius: 8px;
  background-color: #6b7280 !important;
  color: white !important;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.reset-button:hover {
  background-color: #4b5563 !important;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .filter-container {
    grid-template-columns: 1fr;
  }

  .search-box {
    width: 100%;
  }
}

.delivery-info-section {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  margin-top: 20px;
}

.section-title {
  font-size: 1.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 20px;
  border-bottom: 2px solid #e8e8e8;
  padding-bottom: 10px;
}

.delivery-form {
  max-width: 100%;
  margin: 0 auto;
}

/* Grid layout for form fields */
.form-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-group label {
  font-size: 0.9rem;
  font-weight: 500;
  color: #555;
  margin-bottom: 8px;
}

.form-group .required {
  color: #ff4d4f;
}



/* Shipping fee styling */
.summary-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  font-size: 0.9rem;
}

.summary-item label {
  font-weight: 500;
  color: #555;
}

.shipping-fee {
  font-weight: 600;
  color: #1890ff;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .delivery-info-section {
    padding: 15px;
  }

  .section-title {
    font-size: 1.3rem;
  }
}

.input-shipping-fee {
  width: 150px;
}





@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .delivery-info-section {
    padding: 15px;
  }

  .section-title {
    font-size: 1.3rem;
  }
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }

  .delivery-info-section {
    padding: 15px;
  }

  .section-title {
    font-size: 1.3rem;
  }

  .input-shipping-fee {
    width: 100%;
  }
}

.payment-summary {
  margin-top: 20px;
}

.input-customer,
.a-select,
.a-textarea {
  border: 1px solid #e8e8e8 !important;
  padding: 8px 12px;
  font-size: 14px;
  border-radius: 4px;
  transition: border-color 0.3s;
}

/* Beautify shipping fee input */
.input-shipping-fee {
  width: 150px;
  border-radius: 6px;
  padding: 6px 10px;
  font-weight: bold;
  text-align: right;
  margin-right: 10px;
}

/* Style for GHN logo */
.ghn-logo {

  height: 20px;
  vertical-align: middle;
  margin-left: 10px;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .input-shipping-fee {
    width: 100%;
  }

  .ghn-logo {
    width: 30px;
    height: 30px;
  }
}


:deep(.ant-input:hover),
:deep(.ant-input:focus),
:deep(.ant-input-focused) {
  border-color: #58bddb !important;
  /* Màu xanh đậm hơn */
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
  /* Đổ bóng màu xanh đậm */
}

:deep(.ant-select:hover),
:deep(.ant-select:focus),
:deep(.ant-select-focused) {
  border-color: #58bddb !important;
  /* Màu xanh đậm hơn */
  /* box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important; */
  /* Đổ bóng màu xanh đậm */
}

/* Style cho a-input-number khi hover, focus, và focused */
:deep(.ant-input-number:hover),
:deep(.ant-input-number:focus),
:deep(.ant-input-number-focused) {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

/* Style cho a-date-picker khi hover, focus, và focused */
:deep(.ant-picker:hover),
:deep(.ant-picker-focused),
/* Khi DatePicker đã mở và focus vào */
:deep(.ant-picker-focused .ant-picker-input > input),
/* Đảm bảo input bên trong cũng bị ảnh hưởng */
:deep(.ant-picker:focus-within)

/* Dùng cho các component phức tạp có nhiều phần tử con */
  {
  border-color: #58bddb !important;
  box-shadow: 0 0 0 2px rgba(0, 86, 179, 0.2) !important;
}

:deep(.ant-btn) {
  /* background-color: #4aa8c6 !important; */
  /* Màu xanh đậm hơn khi hover */
  /* border-color: #4aa8c6 !important; */
  color: white !important;
  /* box-shadow: 0 2px 8px rgba(0, 0, 0, 1); */
  /* Subtle shadow */
  /* Đổ bóng xanh */
}
</style>