package com.ecommerce.promotion.entity;

import com.ecommerce.promotion.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "voucher_customer")
@DynamicUpdate
public class VoucherCustomer extends PrimaryEntity implements Serializable {

    @Column(name = "code")
    private String code;

    @Column(name = "customer_id")
    private String customerId;

    @ManyToOne
    @JoinColumn(name = "voucher_id", referencedColumnName = "id")
    private Voucher phieuGiamGia;

    @PrePersist
    void generateCode() {
        if (code == null || code.isBlank()) {
            code = "PGGCT-" + UUID.randomUUID();
        }
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public Voucher getVoucher() { return phieuGiamGia; }
    public void setVoucher(Voucher phieuGiamGia) { this.phieuGiamGia = phieuGiamGia; }
}
