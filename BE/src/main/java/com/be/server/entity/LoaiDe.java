package com.be.server.entity;


import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.listener.CreateLoaiDeEntityListener;
import com.be.server.infrastructure.listener.CreateSanPhamEntityListener;
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
@Table(name = "loai_de")
@DynamicUpdate
@EntityListeners(CreateLoaiDeEntityListener.class)
public class LoaiDe extends PrimaryEntity implements Serializable {

    @Column(name = "ma_loai_de")
    private String ma;

    @Column(name = "ten_loai_de")
    private String ten;

}
