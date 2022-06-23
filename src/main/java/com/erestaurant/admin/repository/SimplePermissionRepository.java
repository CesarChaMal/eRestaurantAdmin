package com.erestaurant.admin.repository;

import com.erestaurant.admin.domain.SimplePermission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the SimplePermission entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SimplePermissionRepository extends ReactiveCrudRepository<SimplePermission, String>, SimplePermissionRepositoryInternal {
    @Override
    <S extends SimplePermission> Mono<S> save(S entity);

    @Override
    Flux<SimplePermission> findAll();

    @Override
    Mono<SimplePermission> findById(String id);

    @Override
    Mono<Void> deleteById(String id);
}

interface SimplePermissionRepositoryInternal {
    <S extends SimplePermission> Mono<S> save(S entity);

    Flux<SimplePermission> findAllBy(Pageable pageable);

    Flux<SimplePermission> findAll();

    Mono<SimplePermission> findById(String id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<SimplePermission> findAllBy(Pageable pageable, Criteria criteria);

}
