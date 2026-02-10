package com.example.crud.service;

import com.example.crud.dto.AddressDTO;
import com.example.crud.entity.Address;
import com.example.crud.entity.AuditLog;
import com.example.crud.exception.NotFoundException;
import com.example.crud.repository.AddressRepository;
import com.example.crud.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public AddressService(AddressRepository addressRepository,
                          UserRepository userRepository,
                          AuditLogService auditLogService) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
    }

    public AddressDTO addAddress(Long userId, AddressDTO dto) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Không tìm thấy user với ID: " + userId);
        }
        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            addressRepository.clearDefaultForUser(userId);
        }
        Address created = addressRepository.createAddress(toEntity(userId, dto));
        auditLogService.record(new AuditLog(null, userId, "CREATE", "ADDRESS", created.getId(), null, null, null, null));
        return toDTO(created);
    }

    public AddressDTO updateAddress(Long userId, Long addressId, AddressDTO dto) {
        Address existing = addressRepository.findById(addressId)
                .filter(address -> address.getUserId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy address với ID: " + addressId));

        if (Boolean.TRUE.equals(dto.getIsDefault())) {
            addressRepository.clearDefaultForUser(userId);
        }

        existing.setType(dto.getType());
        existing.setLine1(dto.getLine1());
        existing.setLine2(dto.getLine2());
        existing.setCity(dto.getCity());
        existing.setState(dto.getState());
        existing.setPostalCode(dto.getPostalCode());
        existing.setCountry(dto.getCountry());
        existing.setIsDefault(dto.getIsDefault());
        Address updated = addressRepository.updateAddress(existing);
        auditLogService.record(new AuditLog(null, userId, "UPDATE", "ADDRESS", updated.getId(), null, null, null, null));
        return toDTO(updated);
    }

    public void deleteAddress(Long userId, Long addressId) {
        Address existing = addressRepository.findById(addressId)
                .filter(address -> address.getUserId().equals(userId))
                .orElseThrow(() -> new NotFoundException("Không tìm thấy address với ID: " + addressId));
        addressRepository.softDelete(existing.getId());
        auditLogService.record(new AuditLog(null, userId, "DELETE", "ADDRESS", addressId, null, null, null, null));
    }

    @Transactional(readOnly = true)
    public List<AddressDTO> getAddresses(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Không tìm thấy user với ID: " + userId);
        }
        return addressRepository.findByUserId(userId).stream().map(this::toDTO).toList();
    }

    private Address toEntity(Long userId, AddressDTO dto) {
        Address address = new Address();
        address.setUserId(userId);
        address.setType(dto.getType());
        address.setLine1(dto.getLine1());
        address.setLine2(dto.getLine2());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());
        address.setIsDefault(dto.getIsDefault());
        return address;
    }

    private AddressDTO toDTO(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setId(address.getId());
        dto.setType(address.getType());
        dto.setLine1(address.getLine1());
        dto.setLine2(address.getLine2());
        dto.setCity(address.getCity());
        dto.setState(address.getState());
        dto.setPostalCode(address.getPostalCode());
        dto.setCountry(address.getCountry());
        dto.setIsDefault(address.getIsDefault());
        return dto;
    }
}
