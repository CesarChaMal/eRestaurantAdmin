package com.erestaurant.admin.repository;

import com.erestaurant.admin.domain.Permission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Permission entity.
 */
@SuppressWarnings("unused")
@Repository
public interface PermissionRepository extends ReactiveCrudRepository<Permission, String>, PermissionRepositoryInternal {
    @Override
    <S extends Permission> Mono<S> save(S entity);

    @Override
    Flux<Permission> findAll();

    @Override
    Mono<Permission> findById(String id);

    @Override
    Mono<Void> deleteById(String id);
}

interface PermissionRepositoryInternal {
    <S extends Permission> Mono<S> save(S entity);

    Flux<Permission> findAllBy(Pageable pageable);

    Flux<Permission> findAll();

    Mono<Permission> findById(String id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Permission> findAllBy(Pageable pageable, Criteria criteria);

}
