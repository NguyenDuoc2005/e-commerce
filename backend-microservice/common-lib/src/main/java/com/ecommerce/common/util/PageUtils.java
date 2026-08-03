package com.ecommerce.common.util;

import com.ecommerce.common.base.PageableRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public final class PageUtils {

    private PageUtils() {
    }

    public static Pageable createPageable(PageableRequest request, String defaultSortBy) {
        int page = Math.max(request.getPage() - 1, 0);
        int size = request.getSize() == 0 ? 10 : request.getSize();
        String sortBy = request.getSortBy() == null || request.getSortBy().isEmpty()
                ? defaultSortBy
                : request.getSortBy();
        if ("created_date".equals(sortBy)) {
            sortBy = "createdDate";
        }
        if ("last_modified_date".equals(sortBy)) {
            sortBy = "lastModifiedDate";
        }
        Sort.Direction direction = request.getOrderBy() == null || request.getOrderBy().isEmpty()
                ? Sort.Direction.DESC
                : Sort.Direction.fromString(request.getOrderBy());
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
