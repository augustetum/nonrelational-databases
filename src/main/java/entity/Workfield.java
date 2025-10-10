package entity;

import lombok.Data;

@Data
public class Workfield {
    //Pasirodo, jei workfield yra kaip embedded dokumentas (t.y. dalis freelancerio šiuo atveju), tai jį ir reikia accessint per parent.
    //Embedded dokam also nereikia ID, pasirodo, nes jis nėra kaip atskira kolekcija
    private String id;
    private WorkfieldCategory category;
    private String description;
    private int hourlyRate;
}
