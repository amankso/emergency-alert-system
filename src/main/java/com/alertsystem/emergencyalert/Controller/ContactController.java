package com.alertsystem.emergencyalert.Controller;

import com.alertsystem.emergencyalert.DTO.ContactDTO;
import com.alertsystem.emergencyalert.Service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/contacts")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactDTO> addContact(
            @RequestHeader String sessionToken, //used to identify the currently logged-in user
            @Valid @RequestBody ContactDTO contactDTO  //tells Spring to deserialize the incoming JSON payload into a ContactDTO object.
    ) {
        return ResponseEntity.ok(contactService.addContact(sessionToken, contactDTO));
    }

    @GetMapping
    public ResponseEntity<List<ContactDTO>> getAllContacts(
            @RequestHeader String sessionToken
    ) {
        return ResponseEntity.ok(contactService.getAllContacts(sessionToken));
    }

    @DeleteMapping("/{mobileNumber}")
    public ResponseEntity<String> deleteContact(
            @RequestHeader String sessionToken,
            @PathVariable String mobileNumber
    ) {
        contactService.deleteContact(sessionToken, mobileNumber);
        return ResponseEntity.ok("Contact deleted successfully.");
    }
}
