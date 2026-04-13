package com.countrydelight.mms.repository.master;

import com.countrydelight.mms.entity.master.GodownItemStock;
import com.countrydelight.mms.entity.master.GodownItemStockId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GodownItemStockRepository extends JpaRepository<GodownItemStock, GodownItemStockId> {

    List<GodownItemStock> findByGodownId(Long godownId);
}
