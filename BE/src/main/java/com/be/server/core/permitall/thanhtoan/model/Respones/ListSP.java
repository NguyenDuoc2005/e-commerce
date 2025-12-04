package com.be.server.core.permitall.thanhtoan.model.Respones;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ListSP {

    @JsonProperty("id")
    private String id;
    @JsonProperty("quantity")
    private Integer quantity;

}
