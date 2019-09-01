package com.njust.csa.reg.repository.docker;

import com.njust.csa.reg.repository.entities.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepo extends CrudRepository<UserEntity, Long> {
    boolean existsByNameAndPassword(String name, String password);

    Optional<UserEntity> findByName(String name);
}
