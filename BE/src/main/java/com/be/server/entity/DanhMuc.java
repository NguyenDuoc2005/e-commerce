package com.be.server.entity;


import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.listener.CreateLoaiGiayEntityListener;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Table(name = "danh_muc")
@DynamicUpdate
@EntityListeners(CreateLoaiGiayEntityListener.class)
public class DanhMuc extends PrimaryEntity implements Serializable {

    @Column(name = "ma_danh_muc")
    private String ma;

    @Column(name = "ten_danh_muc")
    private String ten;


}
