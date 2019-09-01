package com.njust.csa.reg.repository.docker;

import com.njust.csa.reg.repository.entities.TableInfoEntity;
import org.springframework.data.repository.CrudRepository;

public interface TableInfoRepo extends CrudRepository<TableInfoEntity, Long> {
    Iterable<TableInfoEntity> findAllByStatus(byte status);
}
