package entity;

interface User {
    String getId();
    String getFirstName();
    String getLastName();
    String getEmail();
    String getPassword();
    long getPhoneNumber();
    String getCity();
    double getRating();

    void setId(String id);
    void setFirstName(String firstName);
    void setLastName(String lastName);
    void setEmail(String email);
    void setPassword(String password);
    void setPhoneNumber(long phoneNumber);
    void setCity(String city);
    void setRating(double rating);
}
