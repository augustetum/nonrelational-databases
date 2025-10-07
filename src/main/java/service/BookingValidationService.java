package service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import dto.PermissionCheckResultDto;
import dto.ValidationResultDto;
import entity.Booking;
import entity.Client;

@Service
public class BookingValidationService {
    public ValidationResultDto validate(Booking booking) {
        if (booking == null) {
            return ValidationResultDto.invalid("Booking can't be null.");
        }

        List<Function<Booking, ValidationResultDto>> validators = List.of(
                b -> validateTime(b.getTime()),
                b -> validateAddress(b.getAddress()),
                b -> validateDetails(b.getDetails()),
                b -> validateClientId(b.getClientId()),
                b -> validateFreelancerId(b.getFreelancerId()));

        return validators.stream()
                .map(b -> b.apply(booking))
                .filter(ValidationResultDto::isInvalid)
                .findFirst()
                .orElse(ValidationResultDto.valid());
    }

    public ValidationResultDto validateTime(Date time){
        Date now = new Date();
        long sixHoursInMillis = 6L * 60 * 60 * 1000;
        Date sixHoursFromNow = new Date(now.getTime() + sixHoursInMillis);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.setTime(now);
        calendar.add(Calendar.MONTH, 1);
        Date oneMonthFromNow = calendar.getTime();

        if(time == null){
            return ValidationResultDto.invalid("Booking time cannot be null.");
        }

        if(time.before(sixHoursFromNow)){
            return ValidationResultDto.invalid("Booking time cannot be less than six hours from now.");
        }

        if(time.after(oneMonthFromNow)){
            return ValidationResultDto.invalid("Booking time cannot be more than a month from now.");
        }
        return ValidationResultDto.valid();
    }

    public ValidationResultDto validateAddress(String address){
        if(address == null){
            return ValidationResultDto.invalid("Address cannot be null");
        }
        return ValidationResultDto.valid();
    }

    public ValidationResultDto validateDetails(String details){
        if(details==null){
            return ValidationResultDto.invalid("Details cannot be null");
        }
        return ValidationResultDto.valid();
    }

    public ValidationResultDto validateClientId(String clientId){
        if(clientId==null){
            return ValidationResultDto.invalid("Client ID cannot be null");
        }
        return ValidationResultDto.valid();
    }

    public ValidationResultDto validateFreelancerId(String freelancerId){
        if(freelancerId==null){
            return ValidationResultDto.invalid("Freelancer ID cannot be null");
        }

        Optional<Client> freelancer = freelancerRepository.getById(freelancerId);
        if(!freelancer.isPresent()){
            return ValidationResultDto.invalid("Freelancers with specified ID does not exist");
        }

        return ValidationResultDto.valid();
    }
}
