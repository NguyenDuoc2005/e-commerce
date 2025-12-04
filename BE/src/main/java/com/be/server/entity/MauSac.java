package com.be.server.entity;


import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.listener.CreateMauSacEntityListener;
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
@Table(name = "mau_sac")
@DynamicUpdate
@EntityListeners(CreateMauSacEntityListener.class)
public class MauSac extends PrimaryEntity implements Serializable {

    @Column(name = "ma_mau_sac")
    private String ma;

    @Column(name = "ten_mau_sac")
    private String ten;

    @Column(name = "mau_sac")
    private String mau;

}
