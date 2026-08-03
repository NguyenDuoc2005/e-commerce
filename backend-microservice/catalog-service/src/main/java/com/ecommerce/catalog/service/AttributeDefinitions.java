package com.ecommerce.catalog.service;

import com.ecommerce.catalog.entity.ChatLieu;
import com.ecommerce.catalog.entity.DanhMuc;
import com.ecommerce.catalog.entity.KichCo;
import com.ecommerce.catalog.entity.LoaiDe;
import com.ecommerce.catalog.entity.MauSac;
import com.ecommerce.catalog.entity.ThuongHieu;
import com.ecommerce.catalog.entity.XuatSu;
import com.ecommerce.catalog.model.request.AttributeRequest;
import com.ecommerce.catalog.repository.ChatLieuRepository;
import com.ecommerce.catalog.repository.DanhMucRepository;
import com.ecommerce.catalog.repository.KichCoRepository;
import com.ecommerce.catalog.repository.LoaiDeRepository;
import com.ecommerce.catalog.repository.MauSacRepository;
import com.ecommerce.catalog.repository.ThuongHieuRepository;
import com.ecommerce.catalog.repository.XuatSuRepository;
import com.ecommerce.common.base.ResponseObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

@Configuration
public class AttributeDefinitions {

    @Bean
    AttributeService.AttributeDefinition<MauSac> mauSacDefinition(MauSacRepository repository) {
        return new Definition<>("mau-sac", MauSac.class, repository, MauSac::new,
                request -> {
                    if (!repository.findByTen(request.getTen()).isEmpty()) {
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
    AttributeService.AttributeDefinition<KichCo> kichCoDefinition(KichCoRepository repository) {
        return new Definition<>("size", KichCo.class, repository, KichCo::new,
                request -> repository.findByTenContaining(request.getTen()).isEmpty()
                        ? null
                        : new ResponseObject<>(null, HttpStatus.OK, "Kich co nay da ton tai"),
                "Lay danh sach size thanh cong", "Lay size thanh cong", "Khong tim thay size",
                "Cap nhat kich co thanh cong", "Tao size thanh cong", "Khong tim size");
    }

    @Bean
    AttributeService.AttributeDefinition<ThuongHieu> thuongHieuDefinition(ThuongHieuRepository repository) {
        return simple("thuong-hieu", ThuongHieu.class, repository, ThuongHieu::new, "thuong hieu", "Thuong hieu");
    }

    @Bean
    AttributeService.AttributeDefinition<XuatSu> xuatSuDefinition(XuatSuRepository repository) {
        return simple("xuat-xu", XuatSu.class, repository, XuatSu::new, "xuat xu", "Xuat xu");
    }

    @Bean
    AttributeService.AttributeDefinition<ChatLieu> chatLieuDefinition(ChatLieuRepository repository) {
        return simple("chat-lieu", ChatLieu.class, repository, ChatLieu::new, "chat lieu", "Chat lieu");
    }

    @Bean
    AttributeService.AttributeDefinition<DanhMuc> danhMucDefinition(DanhMucRepository repository) {
        return simple("danh-muc", DanhMuc.class, repository, DanhMuc::new, "danh muc", "Danh muc");
    }

    @Bean
    AttributeService.AttributeDefinition<LoaiDe> loaiDeDefinition(LoaiDeRepository repository) {
        return simple("loai-de", LoaiDe.class, repository, LoaiDe::new, "loai de", "Loai de");
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
