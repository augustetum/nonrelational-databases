package service;

import entity.Client;
import entity.Freelancer;
import repository.ClientRepository;
import repository.FreelancerRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Primary
public class UnifiedUserDetailsService implements UserDetailsService {

    private final ClientRepository clientRepository;
    private final FreelancerRepository freelancerRepository;

    public UnifiedUserDetailsService(ClientRepository clientRepository, FreelancerRepository freelancerRepository) {
        this.clientRepository = clientRepository;
        this.freelancerRepository = freelancerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Optional<Client> maybeClient = clientRepository.findByEmail(email);
        if (maybeClient.isPresent()) {
            return new CustomClientDetails(maybeClient.get());
        }


        Optional<Freelancer> maybeFreelancer = freelancerRepository.findByEmail(email);
        if (maybeFreelancer.isPresent()) {
            return new CustomFreelancerDetails(maybeFreelancer.get());
        }


        throw new UsernameNotFoundException("User not found: " + email);
    }
}