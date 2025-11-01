package com.alertsystem.emergencyalert.Service;

import com.alertsystem.emergencyalert.DTO.ContactDTO;
import com.alertsystem.emergencyalert.Entity.ContactEntity;
import com.alertsystem.emergencyalert.Entity.UserEntity;
import com.alertsystem.emergencyalert.Repository.ContactRepository;
import com.alertsystem.emergencyalert.exception.BadRequestException;
import com.alertsystem.emergencyalert.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContactService {

    private static final long MAX_CONTACTS = 5;
    private final ContactRepository contactRepository;
    private final UserService userService;

    @Transactional
    public ContactDTO addContact(String sessionToken, ContactDTO dto) {
        UserEntity user = userService.getBySessionToken(sessionToken);
        if(user == null){
            throw new ResourceNotFoundException("User not found");
        }
        if (contactRepository.existsByUserEntityAndMobileNumber(user, dto.getMobileNumber())) {
            throw new BadRequestException("Contact already exists");
        }
        if (contactRepository.countByUserEntity(user) >= MAX_CONTACTS) {
            throw new BadRequestException("Maximum number of contacts reached");
        }

        ContactEntity contact = new ContactEntity();
        contact.setUserEntity(user);
        contact.setName(dto.getName());
        contact.setMobileNumber(dto.getMobileNumber());
        contact.setRelation(dto.getRelation());

        ContactEntity saved = contactRepository.save(contact);

        return ContactDTO.builder()
                .name(saved.getName())
                .mobileNumber(saved.getMobileNumber())
                .build();

    }

    public List<ContactDTO> getAllContacts(String sessionToken) {
        UserEntity user = userService.getBySessionToken(sessionToken);

        return contactRepository.findAllByUserEntity(user)
                .stream()
                .map(c -> ContactDTO.builder()
                        .name(c.getName())
                        .mobileNumber(c.getMobileNumber())
                        .relation(c.getRelation())
                        .build())
                .collect(Collectors.toList());
    }


    @Transactional
    public void deleteContact(String sessionToken, String mobileNumber) {
        UserEntity user = userService.getBySessionToken(sessionToken);
        if(user == null){
            throw new ResourceNotFoundException("User not found");
        }
        ContactEntity contact = contactRepository
                .findByUserEntityAndMobileNumber(user, mobileNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Contact not found"));
        contactRepository.delete(contact);
    }
}
