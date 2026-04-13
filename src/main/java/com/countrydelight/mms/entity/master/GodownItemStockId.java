package com.countrydelight.mms.entity.master;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GodownItemStockId implements Serializable {
    private Long godownId;
    private String itemId;
}
