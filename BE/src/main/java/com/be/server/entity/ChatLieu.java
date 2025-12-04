package com.be.server.entity;

import com.be.server.entity.base.PrimaryEntity;
import com.be.server.infrastructure.listener.CreateChatLieuEntityListener;
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
@Table(name = "chat_lieu")
@DynamicUpdate
@EntityListeners(CreateChatLieuEntityListener.class)
public class ChatLieu extends PrimaryEntity implements Serializable {

    @Column(name = "ma_chat_lieu")
    private String ma;

    @Column(name = "ten_chat_lieu")
    private String ten;

}
