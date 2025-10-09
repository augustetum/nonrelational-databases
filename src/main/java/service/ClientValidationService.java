package service;

import java.util.List;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import dto.ValidationResultDto;
import entity.Client;
import repository.ClientRepository;

@Service
public class ClientValidationService {
    ClientRepository clientRepository;

    public ValidationResultDto validate(Client client) {
        if (client == null) {
            return ValidationResultDto.invalid("Client can't be null.");
        }

        List<Function<Client, ValidationResultDto>> validators = List.of(
                c -> validateFirstName(c.getFirstName()),
                c -> validateLastName(c.getLastName()),
                c -> validateEmail(c.getEmail()),
                c -> validateCity(c.getCity()));

        return validators.stream()
                .map(b -> b.apply(client))
                .filter(ValidationResultDto::isInvalid)
                .findFirst()
                .orElse(ValidationResultDto.valid());
    }

    public ValidationResultDto validateFirstName(String firstName) {
        if (firstName == null || firstName.equals("")) {
            return ValidationResultDto.invalid("First name cannot be blank");
        }
        return ValidationResultDto.valid();
    }

    public ValidationResultDto validateLastName(String lastName) {
        if (lastName == null || lastName.equals("")) {
            return ValidationResultDto.invalid("Last name cannot be blank");
        }
        return ValidationResultDto.valid();
    }

    public ValidationResultDto validateEmail(String email){
        if(email==null || email.equals("")){
            return ValidationResultDto.invalid("Email cannot be blank");
        }
        return ValidationResultDto.valid();
    }

    public ValidationResultDto validateCity(String city){
        if(city==null || city.equals("")){
            return ValidationResultDto.invalid("City cannot be blank");
        }
        return ValidationResultDto.valid();
    }
}