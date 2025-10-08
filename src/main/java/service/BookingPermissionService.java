package service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import dto.CreateBookingRequestDto;
import dto.EditBookingRequestDto;
import dto.PermissionCheckResultDto;
import entity.Booking;
import repository.BookingRepository;

@Service
public class BookingPermissionService {

    BookingRepository bookingRepository;

    public BookingPermissionService(BookingRepository bookingRepository){
        this.bookingRepository = bookingRepository;
    }

    public boolean isFree(List<Booking> existingBookings, Date potentialBookingDate){
        
        Calendar potentialCal = Calendar.getInstance();
        potentialCal.setTime(potentialBookingDate);
        
        int potentialYear = potentialCal.get(Calendar.YEAR);
        int potentialMonth = potentialCal.get(Calendar.MONTH);
        int potentialDay = potentialCal.get(Calendar.DAY_OF_MONTH);
        
        for (Booking booking : existingBookings) {
            Calendar bookingCal = Calendar.getInstance();
            bookingCal.setTime(booking.getTime());
            
            int bookingYear = bookingCal.get(Calendar.YEAR);
            int bookingMonth = bookingCal.get(Calendar.MONTH);
            int bookingDay = bookingCal.get(Calendar.DAY_OF_MONTH);
            
            if (potentialYear == bookingYear && 
                potentialMonth == bookingMonth && 
                potentialDay == bookingDay) {
                return false;
            }
        }
        
        return true;
    }

    public boolean tooLate(Date bookingDate){
        Date now = new Date();
        long sixHoursInMillis = 6L * 60 * 60 * 1000;
        Date sixHoursFromNow = new Date(now.getTime() + sixHoursInMillis);

        System.out.println(sixHoursFromNow);

        if(bookingDate.before(sixHoursFromNow)){
            return true;
        }
        return false;
    }

    public PermissionCheckResultDto canCreateBooking(String freelancerId, String clientId, CreateBookingRequestDto potentialBooking){
        if(clientId.equals(freelancerId)){
        return PermissionCheckResultDto.invalid("Freelancers are not allowed to hire themselves.");
        }
/* commentinu out nes dar neturim client padaryto lol
        Client client = clientRepository.getById(clientId);
        if(client == null){
            return PermissionCheckResultDto.invalid("Only clients are allowed to create bookings.");
        }
*/
        if(!isFree(bookingRepository.getByFreelancerId(freelancerId), potentialBooking.getTime())){
            return PermissionCheckResultDto.invalid("The requested freelancer is already booked on that day");
        }

        return PermissionCheckResultDto.valid();
    } 

    public PermissionCheckResultDto canUpdateBooking(String clientId, String bookingId, EditBookingRequestDto updatedBooking){
        Booking booking = bookingRepository.getById(bookingId);
        if(booking == null){
            return PermissionCheckResultDto.invalid("Booking with this ID does not exist.");
        }

        if(!clientId.equals(booking.getClientId())){
        return PermissionCheckResultDto.invalid("Users cannot edit other users' bookings.");
        }

        if(!isFree(bookingRepository.getByFreelancerId(booking.getFreelancerId()), updatedBooking.getTime())){
            return PermissionCheckResultDto.invalid("The requested freelancer is already booked on that day.");
        }

        if(tooLate(booking.getTime())){
            return PermissionCheckResultDto.invalid("Cannot edit booking less than 6 hours before the booking time");
        }

        return PermissionCheckResultDto.valid();
    } 

    public PermissionCheckResultDto canDeleteBooking(String bookingId, String userId){
        Booking booking = bookingRepository.getById(bookingId);

        if(booking == null){
            return PermissionCheckResultDto.invalid("Booking with this ID does not exist.");
        }

        if(!userId.equals(booking.getFreelancerId()) && !userId.equals(booking.getClientId())){
            return PermissionCheckResultDto.invalid("Cannot delete someone else's booking");
        }

        if(tooLate(booking.getTime())){
            return PermissionCheckResultDto.invalid("Cannot delete booking less than 6 hours before the booking time");
        }

        return PermissionCheckResultDto.valid();
    }

}
