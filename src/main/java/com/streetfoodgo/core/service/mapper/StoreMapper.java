package com.streetfoodgo.core.service.mapper;

import com.streetfoodgo.core.model.Store;
import com.streetfoodgo.core.service.model.StoreView;
import org.springframework.stereotype.Component;

/**
 * Mapper to convert Store entity to StoreView DTO.
 */
@Component
public class StoreMapper {

    private final PersonMapper personMapper;

    public StoreMapper(final PersonMapper personMapper) {
        if (personMapper == null) throw new NullPointerException();
        this.personMapper = personMapper;
    }

    public StoreView toView(final Store store) {
        if (store == null) return null;

        return new StoreView(
                store.getId(),
                personMapper.toView(store.getOwner()),
                store.getName(),
                store.getDescription(),
                store.getCuisineType(),
                store.getStoreType(),
                store.getAddress(),
                store.getLatitude(),
                store.getLongitude(),
                store.getArea(),
                store.getOpeningHours(),
                store.getIsOpen(),
                store.getMinimumOrderAmount(),
                store.getAcceptsDelivery(),
                store.getAcceptsPickup(),
                store.getDeliveryFee(),
                store.getEstimatedDeliveryTimeMinutes(),
                store.getImageUrl(),
                store.getCreatedAt()
        );
    }
}