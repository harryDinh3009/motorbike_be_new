package com.motorbikebe.repository.business.admin;

import com.motorbikebe.entity.domain.CarModelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarModelRepository extends JpaRepository<CarModelEntity, String> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, String id);

    List<CarModelEntity> findByActiveTrueOrderByNameAsc();
}

