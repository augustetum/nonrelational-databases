package controller;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dto.ClientDetailsDto;
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
}
