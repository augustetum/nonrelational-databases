package controller;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dto.ClientDetailsDto;
import dto.EditClientDetailsDto;
import entity.Client;
import service.ClientService;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    @Autowired
    private ClientService clientService;

    @GetMapping
    public ResponseEntity<?> getClientDetails(String userId) {
        Optional<ClientDetailsDto> maybeFreelancer = clientService.getClientDetails(userId);
        return ResponseEntity.ok(maybeFreelancer);
    }
    
    @PostMapping
    public ResponseEntity<?> addClient(@RequestBody Client client){
        clientService.addClient(client);
        return ResponseEntity.ok().build();
    }

    @PutMapping
    public ResponseEntity<?> editClientDetails(String userId, @RequestBody EditClientDetailsDto clientDetailsDto){
        clientService.editClientDetails(userId, clientDetailsDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteClient(String userId){
        clientService.deleteClient(userId);
        return ResponseEntity.ok().build();
    }
}
