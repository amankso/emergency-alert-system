package com.alertsystem.emergencyalert.Repository;

import com.alertsystem.emergencyalert.Entity.ContactEntity;
import com.alertsystem.emergencyalert.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContactRepository extends JpaRepository<ContactEntity, Long> {

    List<ContactEntity> findAllByUserEntity(UserEntity userEntity);

    boolean existsByUserEntityAndMobileNumber(UserEntity userEntity, String mobileNumber);
    // → prevents adding duplicate contacts.

    Optional<ContactEntity> findByUserEntityAndMobileNumber(UserEntity userEntity, String mobileNumber);
    // → for editing or deleting a specific contact.

    void deleteByUserEntityAndMobileNumber(UserEntity userEntity, String mobileNumber);
    // → allows user to remove a saved contact.

    long countByUserEntity(UserEntity userEntity);
    // → can be used to limit max number of emergency contacts.

}
