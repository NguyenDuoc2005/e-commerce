package com.be.server.core.permitall.chitietsanpham.dto.response;

import com.be.server.entity.ChatLieu;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatLieuDTO {
    private String id;
    private String tenChatLieu;
    private String maChatLieu;

    public ChatLieuDTO(ChatLieu entity) {
        this.id = entity.getId();
        this.tenChatLieu = entity.getTen();
        this.maChatLieu = entity.getMa();
    }
}
