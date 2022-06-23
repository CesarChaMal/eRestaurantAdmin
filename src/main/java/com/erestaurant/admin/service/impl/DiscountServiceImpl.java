package com.erestaurant.admin.service.impl;

import com.erestaurant.admin.domain.Discount;
import com.erestaurant.admin.repository.DiscountRepository;
import com.erestaurant.admin.service.DiscountService;
import com.erestaurant.admin.service.dto.DiscountDTO;
import com.erestaurant.admin.service.mapper.DiscountMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Service Implementation for managing {@link Discount}.
 */
@Service
@Transactional
public class DiscountServiceImpl implements DiscountService {

    private final Logger log = LoggerFactory.getLogger(DiscountServiceImpl.class);

    private final DiscountRepository discountRepository;

    private final DiscountMapper discountMapper;

    public DiscountServiceImpl(DiscountRepository discountRepository, DiscountMapper discountMapper) {
        this.discountRepository = discountRepository;
        this.discountMapper = discountMapper;
    }

    @Override
    public Mono<DiscountDTO> save(DiscountDTO discountDTO) {
        log.debug("Request to save Discount : {}", discountDTO);
        return discountRepository.save(discountMapper.toEntity(discountDTO)).map(discountMapper::toDto);
    }

    @Override
    public Mono<DiscountDTO> update(DiscountDTO discountDTO) {
        log.debug("Request to save Discount : {}", discountDTO);
        return discountRepository.save(discountMapper.toEntity(discountDTO).setIsPersisted()).map(discountMapper::toDto);
    }

    @Override
    public Mono<DiscountDTO> partialUpdate(DiscountDTO discountDTO) {
        log.debug("Request to partially update Discount : {}", discountDTO);

        return discountRepository
            .findById(discountDTO.getId())
            .map(existingDiscount -> {
                discountMapper.partialUpdate(existingDiscount, discountDTO);

                return existingDiscount;
            })
            .flatMap(discountRepository::save)
            .map(discountMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<DiscountDTO> findAll() {
        log.debug("Request to get all Discounts");
        return discountRepository.findAll().map(discountMapper::toDto);
    }

    public Mono<Long> countAll() {
        return discountRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<DiscountDTO> findOne(String id) {
        log.debug("Request to get Discount : {}", id);
        return discountRepository.findById(id).map(discountMapper::toDto);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Discount : {}", id);
        return discountRepository.deleteById(id);
    }
}
