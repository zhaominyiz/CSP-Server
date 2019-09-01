package com.njust.csa.reg.repository.docker;

import com.njust.csa.reg.repository.entities.TableStructureEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface TableStructureRepo extends CrudRepository<TableStructureEntity, Long> {

    List<TableStructureEntity> findAllByBelongsToOrderByIndexNumber(long belongsTo);

    List<TableStructureEntity> findAllByTableIdAndBelongsToOrderByIndexNumber(long tableId, Long BelongsTo);

    TableStructureEntity findTopByTableIdAndIsUnique(long id, byte isUnique);

    List<TableStructureEntity> findAllByTableId(long id);
}
