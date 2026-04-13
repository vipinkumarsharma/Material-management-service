package com.countrydelight.mms.entity.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "supplier_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupplierMaster {

    @Id
    @Column(name = "supp_id", length = 20)
    private String suppId;

    @Column(name = "supp_name", nullable = false, length = 100)
    private String suppName;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "mob_no", length = 20)
    private String mobNo;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "gstin", length = 20)
    private String gstin;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
