package controller;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dto.ClientDetailsDto;
import dto.EditClientDetailsDto;
import dto.ValidationResultDto;
import entity.Client;
import service.ClientService;
import service.ClientValidationService;
import service.CustomClientDetails;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientValidationService validationService;

    @GetMapping
    public ResponseEntity<?> getClientDetails(String userId) {
        Optional<ClientDetailsDto> maybeFreelancer = clientService.getClientDetails(userId);
        return ResponseEntity.ok(maybeFreelancer);
    }

    @PutMapping
    public ResponseEntity<?> editClientDetails(Authentication authentication, @RequestBody EditClientDetailsDto clientDetailsDto){
        CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
        String clientId = userDetails.getUser().getId();
        String email = userDetails.getUser().getEmail();
        String password = userDetails.getUser().getPassword();

        Client client = new Client();
        client.setId(clientId);
        client.setFirstName(clientDetailsDto.getFirstName());
        client.setLastName(clientDetailsDto.getLastName());
        client.setCity(clientDetailsDto.getCity());
        client.setPhoneNumber(clientDetailsDto.getPhoneNumber());
        client.setEmail(email);
        client.setPassword(password);

        ValidationResultDto validationResult = validationService.validate(client);
        if(validationResult.isInvalid()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);

        clientService.editClientDetails(clientId, client);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteClient(Authentication authentication){
        CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
        String clientId = userDetails.getUser().getId();

        clientService.deleteClient(clientId);
        return ResponseEntity.ok().build();
    }
}
