package com.ecommerce.catalog.service;

import com.ecommerce.catalog.entity.Material;
import com.ecommerce.catalog.entity.Category;
import com.ecommerce.catalog.entity.Size;
import com.ecommerce.catalog.entity.SoleType;
import com.ecommerce.catalog.entity.Color;
import com.ecommerce.catalog.entity.Brand;
import com.ecommerce.catalog.entity.Origin;
import com.ecommerce.catalog.model.request.AttributeRequest;
import com.ecommerce.catalog.repository.MaterialRepository;
import com.ecommerce.catalog.repository.CategoryRepository;
import com.ecommerce.catalog.repository.SizeRepository;
import com.ecommerce.catalog.repository.SoleTypeRepository;
import com.ecommerce.catalog.repository.ColorRepository;
import com.ecommerce.catalog.repository.BrandRepository;
import com.ecommerce.catalog.repository.OriginRepository;
import com.ecommerce.common.base.ResponseObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class AttributeDefinitions {

    @Bean
    AttributeService.AttributeDefinition<Color> mauSacDefinition(ColorRepository repository) {
        return new Definition<>("mau-sac", Color.class, repository, Color::new,
                request -> {
                    if (!repository.findByName(request.getName()).isEmpty()) {
                        return new ResponseObject<>(null, HttpStatus.OK, "Mau sac nay da ton tai");
                    }
                    if (!repository.findByMau(request.getColor()).isEmpty()) {
                        return new ResponseObject<>(null, HttpStatus.OK, "Mau sac nay da ton tai");
                    }
                    return null;
                },
                "Lay danh sach mau sac thanh cong", "Mau sac thanh cong", "Khong tim thay Mau sac",
                "Cap nhat mau sac thanh cong", "Tao mau sac thanh cong", "Khong tim mau sac");
    }

    @Bean
    AttributeService.AttributeDefinition<Size> kichCoDefinition(SizeRepository repository) {
        return new Definition<>("size", Size.class, repository, Size::new,
                request -> repository.findByNameContaining(request.getName()).isEmpty()
                        ? null
                        : new ResponseObject<>(null, HttpStatus.OK, "Kich co nay da ton tai"),
                "Lay danh sach size thanh cong", "Lay size thanh cong", "Khong tim thay size",
                "Cap nhat kich co thanh cong", "Tao size thanh cong", "Khong tim size");
    }

    @Bean
    AttributeService.AttributeDefinition<Brand> thuongHieuDefinition(BrandRepository repository) {
        return simple("thuong-hieu", Brand.class, repository, Brand::new, "thuong hieu", "Thuong hieu");
    }

    @Bean
    AttributeService.AttributeDefinition<Origin> xuatSuDefinition(OriginRepository repository) {
        return simple("xuat-xu", Origin.class, repository, Origin::new, "xuat xu", "Xuat xu");
    }

    @Bean
    AttributeService.AttributeDefinition<Material> chatLieuDefinition(MaterialRepository repository) {
        return simple("chat-lieu", Material.class, repository, Material::new, "chat lieu", "Chat lieu");
    }

    @Bean
    AttributeService.AttributeDefinition<Category> danhMucDefinition(CategoryRepository repository) {
        return simple("danh-muc", Category.class, repository, Category::new, "danh muc", "Danh muc");
    }

    @Bean
    AttributeService.AttributeDefinition<SoleType> loaiDeDefinition(SoleTypeRepository repository) {
        return simple("loai-de", SoleType.class, repository, SoleType::new, "loai de", "Loai de");
    }

    private <T extends com.ecommerce.catalog.entity.base.CatalogAttribute> AttributeService.AttributeDefinition<T> simple(
            String key,
            Class<T> entityClass,
            org.springframework.data.jpa.repository.JpaRepository<T, String> repository,
            java.util.function.Supplier<T> supplier,
            String lowerName,
            String titleName
    ) {
        return new Definition<>(key, entityClass, repository, supplier,
                request -> java.util.Collections.emptyList().isEmpty() ? null : null,
                "Lay danh sach " + lowerName + " thanh cong",
                "Lay " + lowerName + " thanh cong",
                "Khong tim thay " + lowerName,
                "Cap nhat " + lowerName + " thanh cong",
                "Tao " + lowerName + " thanh cong",
                "Khong tim " + lowerName);
    }

    private record Definition<T extends com.ecommerce.catalog.entity.base.CatalogAttribute>(
            String key,
            Class<T> entityClass,
            org.springframework.data.jpa.repository.JpaRepository<T, String> repository,
            java.util.function.Supplier<T> supplier,
            java.util.function.Function<AttributeRequest, ResponseObject<?>> duplicateValidator,
            String listSuccessMessage,
            String getSuccessMessage,
            String notFoundMessage,
            String updateSuccessMessage,
            String createSuccessMessage,
            String notFoundForChangeMessage
    ) implements AttributeService.AttributeDefinition<T> {
        @Override
        public T newEntity() {
            return supplier.get();
        }

        @Override
        public ResponseObject<?> validateDuplicate(AttributeRequest request) {
            return duplicateValidator.apply(request);
        }
    }
}
