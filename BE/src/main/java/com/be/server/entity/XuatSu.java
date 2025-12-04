package com.be.server.entity;


import com.be.server.entity.base.PrimaryEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "xuat_su")
@DynamicUpdate
public class XuatSu extends PrimaryEntity implements Serializable {

    @Column(name = "ma_xuat_su")
    private String ma;

    @Column(name = "ten_xuat_su")
    private String ten;

}
