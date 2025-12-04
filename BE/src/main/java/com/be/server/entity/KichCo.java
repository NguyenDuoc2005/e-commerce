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
@Table(name = "kich_co")
@DynamicUpdate
public class KichCo extends PrimaryEntity implements Serializable {

    @Column(name = "ma_kich_co")
    private String ma;

    @Column(name = "ten_kich_co")
    private String ten;

}
