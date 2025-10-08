package service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import dto.ClientDetailsDto;
import repository.ClientRepository;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public Optional<ClientDetailsDto> getClientDetails(String userId) {
        Optional<ClientDetailsDto> maybeClientDetails = clientRepository.getDetails(userId);
        return maybeClientDetails;
    }
}
