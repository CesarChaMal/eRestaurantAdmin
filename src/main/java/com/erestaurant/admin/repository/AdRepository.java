package com.erestaurant.admin.repository;

import com.erestaurant.admin.domain.Ad;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Ad entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AdRepository extends ReactiveCrudRepository<Ad, String>, AdRepositoryInternal {
    @Override
    <S extends Ad> Mono<S> save(S entity);

    @Override
    Flux<Ad> findAll();

    @Override
    Mono<Ad> findById(String id);

    @Override
    Mono<Void> deleteById(String id);
}

interface AdRepositoryInternal {
    <S extends Ad> Mono<S> save(S entity);

    Flux<Ad> findAllBy(Pageable pageable);

    Flux<Ad> findAll();

    Mono<Ad> findById(String id);
    // this is not supported at the moment because of https://github.com/jhipster/generator-jhipster/issues/18269
    // Flux<Ad> findAllBy(Pageable pageable, Criteria criteria);

}
