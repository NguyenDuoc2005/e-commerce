package com.ecommerce.promotion.service;

import com.ecommerce.promotion.constant.EntityStatus;
import com.ecommerce.promotion.controller.SellerVoucherController;
import com.ecommerce.promotion.controller.VoucherController;
import com.ecommerce.promotion.entity.Voucher;
import com.ecommerce.promotion.repository.VoucherCustomerRepository;
import com.ecommerce.promotion.repository.VoucherRepository;
import com.ecommerce.promotion.service.impl.VoucherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class VoucherServiceImplTest {
    @Mock private VoucherRepository voucherRepository;
    @Mock private VoucherCustomerRepository customerRepository;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        VoucherServiceImpl service = new VoucherServiceImpl(voucherRepository, customerRepository);
        mvc = MockMvcBuilders.standaloneSetup(new VoucherController(service), new SellerVoucherController(service))
                .build();
    }

    @Test
    void adminMissingVoucherReturns404AndFailure() throws Exception {
        when(voucherRepository.findById("missing")).thenReturn(Optional.empty());

        mvc.perform(put("/api/v1/admin/voucher/missing/change-status"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Khong tim voucher"))
                .andExpect(jsonPath("$.data").doesNotExist());
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void sellerMissingVoucherReturns404AndFailure() throws Exception {
        when(voucherRepository.findById("missing")).thenReturn(Optional.empty());

        mvc.perform(put("/api/v1/seller/vouchers/missing/change-status").header("X-Seller-Id", "seller-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Khong tim voucher"));
        verify(voucherRepository, never()).save(any());
    }

    @Test
    void existingPlatformVoucherStillChangesStatusSuccessfully() throws Exception {
        Voucher voucher = new Voucher();
        voucher.setStatus(EntityStatus.ACTIVE);
        when(voucherRepository.findById("voucher-1")).thenReturn(Optional.of(voucher));

        mvc.perform(put("/api/v1/admin/voucher/voucher-1/change-status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertEquals(EntityStatus.INACTIVE, voucher.getStatus());
        verify(voucherRepository).save(voucher);
    }

    @Test
    void sellerStillCannotChangeAnotherSellersVoucher() throws Exception {
        Voucher voucher = new Voucher();
        voucher.setSellerId("seller-2");
        voucher.setStatus(EntityStatus.ACTIVE);
        when(voucherRepository.findById("voucher-1")).thenReturn(Optional.of(voucher));

        mvc.perform(put("/api/v1/seller/vouchers/voucher-1/change-status").header("X-Seller-Id", "seller-1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
        assertEquals(EntityStatus.ACTIVE, voucher.getStatus());
        verify(voucherRepository, never()).save(any());
    }
}
