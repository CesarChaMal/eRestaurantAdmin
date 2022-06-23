package com.erestaurant.admin.service.impl;

import com.erestaurant.admin.domain.AppDiscount;
import com.erestaurant.admin.repository.AppDiscountRepository;
import com.erestaurant.admin.service.AppDiscountService;
import com.erestaurant.admin.service.dto.AppDiscountDTO;
import com.erestaurant.admin.service.mapper.AppDiscountMapper;
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
 * Service Implementation for managing {@link AppDiscount}.
 */
@Service
@Transactional
public class AppDiscountServiceImpl implements AppDiscountService {

    private final Logger log = LoggerFactory.getLogger(AppDiscountServiceImpl.class);

    private final AppDiscountRepository appDiscountRepository;

    private final AppDiscountMapper appDiscountMapper;

    public AppDiscountServiceImpl(AppDiscountRepository appDiscountRepository, AppDiscountMapper appDiscountMapper) {
        this.appDiscountRepository = appDiscountRepository;
        this.appDiscountMapper = appDiscountMapper;
    }

    @Override
    public Mono<AppDiscountDTO> save(AppDiscountDTO appDiscountDTO) {
        log.debug("Request to save AppDiscount : {}", appDiscountDTO);
        return appDiscountRepository.save(appDiscountMapper.toEntity(appDiscountDTO)).map(appDiscountMapper::toDto);
    }

    @Override
    public Mono<AppDiscountDTO> update(AppDiscountDTO appDiscountDTO) {
        log.debug("Request to save AppDiscount : {}", appDiscountDTO);
        return appDiscountRepository.save(appDiscountMapper.toEntity(appDiscountDTO).setIsPersisted()).map(appDiscountMapper::toDto);
    }

    @Override
    public Mono<AppDiscountDTO> partialUpdate(AppDiscountDTO appDiscountDTO) {
        log.debug("Request to partially update AppDiscount : {}", appDiscountDTO);

        return appDiscountRepository
            .findById(appDiscountDTO.getId())
            .map(existingAppDiscount -> {
                appDiscountMapper.partialUpdate(existingAppDiscount, appDiscountDTO);

                return existingAppDiscount;
            })
            .flatMap(appDiscountRepository::save)
            .map(appDiscountMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<AppDiscountDTO> findAll() {
        log.debug("Request to get all AppDiscounts");
        return appDiscountRepository.findAll().map(appDiscountMapper::toDto);
    }

    public Mono<Long> countAll() {
        return appDiscountRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<AppDiscountDTO> findOne(String id) {
        log.debug("Request to get AppDiscount : {}", id);
        return appDiscountRepository.findById(id).map(appDiscountMapper::toDto);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete AppDiscount : {}", id);
        return appDiscountRepository.deleteById(id);
    }
}
