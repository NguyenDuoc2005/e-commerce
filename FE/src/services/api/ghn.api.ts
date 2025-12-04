import axios from "axios";

// Định nghĩa các URL API của GHN
const GHN_MASTER_DATA_BASE_URL = "https://online-gateway.ghn.vn/shiip/public-api/master-data/";
const GHN_FEE_URL = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";
const GHN_AVAILABLE_SERVICES_URL =
  "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/available-services";

// Định nghĩa các kiểu dữ liệu (interfaces) cho phản hồi từ GHN
export interface Province {
  ProvinceID: number;
  ProvinceName: string;
}

export interface District {
  DistrictID: number;
  DistrictName: string;
  ProvinceID: number;
}

export interface Ward {
  WardCode: string;
  WardName: string;
  DistrictID: number;
}

// Interface cho yêu cầu tính phí vận chuyển
export interface ShippingFeeRequest {
  myRequest: {
    FromDistrictID: number;
    FromWardCode: string;
    ServiceID: number;
    ToDistrictID: number;
    ToWardCode: string;
    Height: number;
    Length: number;
    Weight: number;
    Width: number;
    InsuranceValue: number;
    Coupon: string | null;
    PickShift: number[] | null;
  };
}

// Interface cho một dịch vụ có sẵn trả về từ GHN
export interface AvailableService {
  service_id: number;
  service_type_id: number;
  short_name: string;
  // ... các trường khác của dịch vụ có sẵn
}

// Interface cho request gửi đi để lấy dịch vụ có sẵn
export interface AvailableServiceRequest {
  myRequest: {
    FromDistrictID: number;
    ToDistrictID: number;
    FromWardCode: string;
    ToWardCode: string;
    ShopId: number;
  };
}

// Interface cho phản hồi tính phí vận chuyển
export interface ShippingFeeResponse {
  data: {
    total: number;
    // ... các trường khác có thể có trong response
  };
}

// Cấu hình Axios instance cho các API master-data (tỉnh, huyện, xã)
const ghnMasterDataApi = axios.create({
  baseURL: GHN_MASTER_DATA_BASE_URL,
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
});

// --- CÁC HÀM LẤY MASTER DATA (TỈNH/HUYỆN/XÃ) ---

// Hàm lấy danh sách Tỉnh/Thành phố
export const getGHNProvinces = async (token: string) => {
  try {
    const response = await ghnMasterDataApi.get("province", {
      headers: { Token: token },
    });
    // GHN thường trả về dữ liệu trong response.data.data
    if (response.data && Array.isArray(response.data.data)) {
      return response.data.data;
    } else {
      console.error("Unexpected response format for provinces:", response.data);
      throw new Error("Invalid provinces data format");
    }
  } catch (error) {
    console.error("Failed to fetch provinces:", error);
    throw error; // Ném lỗi để xử lý ở nơi gọi
  }
};

// Hàm lấy danh sách Quận/Huyện dựa trên ProvinceID
export const getGHNDistricts = async (provinceId: number | null, token: string) => {
  try {
    const response = await ghnMasterDataApi.get("district", {
      headers: { Token: token },
      params: provinceId ? { province_id: provinceId } : {},
    });
    if (response.data && Array.isArray(response.data.data)) {
      return response.data.data;
    } else {
      console.error("Unexpected response format for districts:", response.data);
      throw new Error("Invalid districts data format");
    }
  } catch (error) {
    console.error("Failed to fetch districts:", error);
    throw error;
  }
};

// Hàm lấy danh sách Phường/Xã dựa trên DistrictID
export const getGHNWards = async (districtId: number, token: string) => {
  try {
    const response = await ghnMasterDataApi.get("ward", {
      headers: { Token: token },
      params: { district_id: districtId },
    });
    if (response.data && Array.isArray(response.data.data)) {
      return response.data.data;
    } else {
      console.error("Unexpected response format for wards:", response.data);
      throw new Error("Invalid wards data format");
    }
  } catch (error) {
    console.error("Failed to fetch wards:", error);
    throw error;
  }
};
interface GHNAvailableServiceRequest {
  shop_id: number;
  from_district: number;
  to_district: number;
}

// Hàm tính phí vận chuyển
export const calculateFee = async (myRequest: ShippingFeeRequest, token: string, shopId: number) => {
  try {
    const body = {
      from_district_id: myRequest.myRequest.FromDistrictID,
      to_district_id: myRequest.myRequest.ToDistrictID,
      service_id: myRequest.myRequest.ServiceID,
      insurance_value: myRequest.myRequest.InsuranceValue,
      coupon: myRequest.myRequest.Coupon,
      from_ward_code: myRequest.myRequest.FromWardCode,
      to_ward_code: myRequest.myRequest.ToWardCode,
      height: myRequest.myRequest.Height,
      length: myRequest.myRequest.Length,
      weight: myRequest.myRequest.Weight,
      width: myRequest.myRequest.Width,
    };

    console.log("Request body for calculateFee:", body);
    const response = await axios.post(GHN_FEE_URL, body, {
      headers: {
        Token: token,
        ShopId: shopId.toString(),
        "Content-Type": "application/json",
      },
    });
    if (response.data && response.data.data && typeof response.data.data.total === "number") {
      return response.data;
    } else {
      console.error("Unexpected response format for fee:", response.data);
      throw new Error("Invalid fee response format");
    }
  } catch (error) {
    console.error("Failed to calculate fee:", error);
    if (axios.isAxiosError(error) && error.response) {
      console.error("GHN API error details:", error.response.data);
    }
    throw error;
  }
};

// Hàm lấy danh sách các dịch vụ vận chuyển khả dụng
export const getAvailableServices = async (
  token: string,
  requestBody: GHNAvailableServiceRequest
) => {
  try {
    const response = await axios.post(
      "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/available-services",
      requestBody,
      {
        headers: {
          Token: token,
          "Content-Type": "application/json",
        },
      }
    );
    return response.data;
  } catch (error) {
    // Xử lý lỗi
    throw error;
  }
};
// Interface cho phản hồi thông tin giao hàng từ backend của bạn (nếu có)
export interface ThongTinGiaoHangResponse {
  id: string;
  tenNguoiNhan: string;
  sdtNguoiNhan: string;
  diaChiCuThe: string;
  phuongXa: string;
  quanHuyen: string;
  tinhThanhPho: string;
  phiVanChuyen: number;
  // ... bất kỳ trường nào khác
}
