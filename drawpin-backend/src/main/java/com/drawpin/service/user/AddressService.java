package com.drawpin.service.user;

import com.drawpin.domain.entity.Address;
import com.drawpin.dto.request.user.AddressRequest;
import com.drawpin.dto.response.AddressResponse;
import com.drawpin.exception.ResourceNotFoundException;
import com.drawpin.mapper.AddressMapper;
import com.drawpin.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    /**
     * Gets all addresses for a user.
     */
    @Transactional(readOnly = true)
    public List<AddressResponse> getAddresses(UUID userId) {
        return addressRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(addressMapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Adds a new address for a user.
     */
    public AddressResponse addAddress(UUID userId, AddressRequest request) {
        Address address = addressMapper.toEntity(request);
        address.setUserId(userId);
        
        if (request.isDefault()) {
            clearExistingDefault(userId);
        } else {
            // If it's their first address, make it default automatically
            if (addressRepository.findAllByUserIdOrderByCreatedAtDesc(userId).isEmpty()) {
                address.setDefault(true);
            }
        }

        address = addressRepository.save(address);
        log.info("AUDIT: Address added for user {}", userId);
        return addressMapper.toResponse(address);
    }

    /**
     * Updates an existing address.
     */
    public AddressResponse updateAddress(UUID userId, UUID addressId, AddressRequest request) {
        Address address = getEntity(addressId, userId);
        
        if (request.isDefault() && !address.isDefault()) {
            clearExistingDefault(userId);
        }

        addressMapper.updateEntityFromRequest(request, address);
        address = addressRepository.save(address);
        
        log.info("AUDIT: Address updated for user {}", userId);
        return addressMapper.toResponse(address);
    }

    /**
     * Deletes an address.
     */
    public void deleteAddress(UUID userId, UUID addressId) {
        Address address = getEntity(addressId, userId);
        addressRepository.delete(address);
        
        if (address.isDefault()) {
            // If the deleted address was the default, try to make the next one default
            addressRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .findFirst()
                .ifPresent(next -> {
                    next.setDefault(true);
                    addressRepository.save(next);
                });
        }
        log.info("AUDIT: Address deleted for user {}", userId);
    }

    /**
     * Sets a specific address as the default.
     */
    public AddressResponse setDefaultAddress(UUID userId, UUID addressId) {
        Address address = getEntity(addressId, userId);
        if (address.isDefault()) {
            return addressMapper.toResponse(address);
        }

        clearExistingDefault(userId);
        address.setDefault(true);
        address = addressRepository.save(address);
        
        log.info("AUDIT: Default address updated for user {}", userId);
        return addressMapper.toResponse(address);
    }

    private void clearExistingDefault(UUID userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
            .ifPresent(existing -> {
                existing.setDefault(false);
                addressRepository.save(existing);
            });
    }

    private Address getEntity(UUID addressId, UUID userId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("ADDRESS_NOT_FOUND", "Address not found."));
    }
}
