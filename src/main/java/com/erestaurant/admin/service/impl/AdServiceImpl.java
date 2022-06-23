package com.erestaurant.admin.service.impl;

import com.erestaurant.admin.domain.Ad;
import com.erestaurant.admin.repository.AdRepository;
import com.erestaurant.admin.service.AdService;
import com.erestaurant.admin.service.dto.AdDTO;
import com.erestaurant.admin.service.mapper.AdMapper;
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
 * Service Implementation for managing {@link Ad}.
 */
@Service
@Transactional
public class AdServiceImpl implements AdService {

    private final Logger log = LoggerFactory.getLogger(AdServiceImpl.class);

    private final AdRepository adRepository;

    private final AdMapper adMapper;

    public AdServiceImpl(AdRepository adRepository, AdMapper adMapper) {
        this.adRepository = adRepository;
        this.adMapper = adMapper;
    }

    @Override
    public Mono<AdDTO> save(AdDTO adDTO) {
        log.debug("Request to save Ad : {}", adDTO);
        return adRepository.save(adMapper.toEntity(adDTO)).map(adMapper::toDto);
    }

    @Override
    public Mono<AdDTO> update(AdDTO adDTO) {
        log.debug("Request to save Ad : {}", adDTO);
        return adRepository.save(adMapper.toEntity(adDTO).setIsPersisted()).map(adMapper::toDto);
    }

    @Override
    public Mono<AdDTO> partialUpdate(AdDTO adDTO) {
        log.debug("Request to partially update Ad : {}", adDTO);

        return adRepository
            .findById(adDTO.getId())
            .map(existingAd -> {
                adMapper.partialUpdate(existingAd, adDTO);

                return existingAd;
            })
            .flatMap(adRepository::save)
            .map(adMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<AdDTO> findAll() {
        log.debug("Request to get all Ads");
        return adRepository.findAll().map(adMapper::toDto);
    }

    public Mono<Long> countAll() {
        return adRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<AdDTO> findOne(String id) {
        log.debug("Request to get Ad : {}", id);
        return adRepository.findById(id).map(adMapper::toDto);
    }

    @Override
    public Mono<Void> delete(String id) {
        log.debug("Request to delete Ad : {}", id);
        return adRepository.deleteById(id);
    }
}
