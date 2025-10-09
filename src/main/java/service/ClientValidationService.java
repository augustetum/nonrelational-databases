package service;

import dto.EditClientDetailsDto;
import dto.ValidationResultDto;
import repository.ClientRepository;

public class ClientValidationService {
    ClientRepository clientRepository;

    public ClientValidationService(ClientRepository clientRepository){
        this.clientRepository = clientRepository;
    }

    public ValidationResultDto canEditClient(String clientId, EditClientDetailsDto clientDetails){
        if(clientDetails.getFirstName()==null || clientDetails.getFirstName().equals("")){
            return ValidationResultDto.invalid("First name cannot be blank");
        }

        if(clientDetails.getLastName()==null || clientDetails.getLastName().equals("")){
            return ValidationResultDto.invalid("Last name cannot be blank");
        }

        if(clientDetails.getCity()==null || clientDetails.getCity().equals("")){
            return ValidationResultDto.invalid("City cannot be blank");
        }
        
        return ValidationResultDto.valid();
    }
}
