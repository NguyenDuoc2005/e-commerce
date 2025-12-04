package com.be.server.core.permitall.giohang.model.request;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class CartDetailRequest {

    private String idKhachHang;

    private String idSPCT;

    private String price;

    private String quantity;


}
