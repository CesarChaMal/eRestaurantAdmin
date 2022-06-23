package com.erestaurant.admin.repository;

import com.erestaurant.admin.domain.CompositePermission;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the CompositePermission entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CompositePermissionRepository
    extends ReactiveCrudRepository<CompositePermission, String>, CompositePermissionRepositoryInternal {
    @Override
    <S extends CompositePermission> Mono<S> save(S entity);

    @Override
    Flux<CompositePermission> findAll();

    @Override
    Mono<CompositePermission> findById(String id);

    @Override
    Mono<Void> deleteById(String id);
}

interface CompositePermissionRepositoryInternal {
    <S extends CompositePermission> Mono<S> save(S entity);

    Flux<CompositePermission> findAllBy(Pageable pageable);

    Flux<CompositePermission> findAll();

    Mono<CompositePermission> findById(String id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<CompositePermission> findAllBy(Pageable pageable, Criteria criteria);

}
