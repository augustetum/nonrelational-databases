package service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import dto.ClientDetailsDto;
import entity.Client;
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

    public void addClient(Client client){
        clientRepository.add(client);
    }

    public void editClientDetails(String userId, Client client){
        clientRepository.update(userId, client);
    }

    public void deleteClient(String userId){
        clientRepository.delete(userId);
    }
}
