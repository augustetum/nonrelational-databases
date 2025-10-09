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
import dto.PermissionCheckResultDto;
import service.ClientPermissionService;
import service.ClientService;
import service.CustomClientDetails;

@RestController
@RequestMapping("/api/clients")
public class ClientController {
    @Autowired
    private ClientService clientService;

    @Autowired
    private ClientPermissionService permissionService;

    @GetMapping
    public ResponseEntity<?> getClientDetails(String userId) {
        Optional<ClientDetailsDto> maybeFreelancer = clientService.getClientDetails(userId);
        return ResponseEntity.ok(maybeFreelancer);
    }

    @PutMapping
    public ResponseEntity<?> editClientDetails(Authentication authentication, @RequestBody EditClientDetailsDto clientDetailsDto){
        CustomClientDetails userDetails = (CustomClientDetails) authentication.getPrincipal();
        String clientId = userDetails.getUser().getId();

        PermissionCheckResultDto permissionResult = permissionService.canEditClient(clientId, clientDetailsDto);
        if(permissionResult.isDenied()) return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(permissionResult);
        
        clientService.editClientDetails(clientId, clientDetailsDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> deleteClient(String userId){
        clientService.deleteClient(userId);
        return ResponseEntity.ok().build();
    }
}
