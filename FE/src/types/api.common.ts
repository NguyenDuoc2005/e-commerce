export interface PaginationParams {
    page: number;
    size: number;
    orderBy?: string;
    sortBy?: string;
   q?: string;
  thuongHieuIds?: string[];
  mauSacIds?: string[];
  kichCoIds?: string[];
  chatLieuIds?: string[];
  loaiDeIds?: string[];
  danhMucIds?: string[];
  giaMin?: number;
  giaMax?: number;
}

export interface DefaultResponse<T> {
    data: T;
    message: string;
    status: string;
    success: boolean;
}

export interface PaginationResponse<T> {
    data: T;
    totalPages: number;
    totalElements: number;
    currentPage: number;
}

export type ResponseList = {
    id: string;
    orderNumber: number;
};
