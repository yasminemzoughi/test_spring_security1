package tn.esprit.entity;

import jakarta.persistence.*;
import tn.esprit.entity.User;

import java.time.LocalDateTime;

@Entity

public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long  idAppointment;

    private LocalDateTime date;
    private int durationInMinutes;
    private String notes;

   /* @Enumerated(EnumType.STRING)
    private AppointmentStatus status;

    */

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = true)
    private User user;
/*
    @ManyToOne
    @JoinColumn(name = "pet_id",nullable = true)
    private Pet  petId;
 */
   /*
    @ManyToOne
    @JoinColumn(name= "idService",nullable = true)
    @JsonIgnore
    private PetService  generalServiceId; // Reference to General Service
    */
    @ManyToOne
    @JoinColumn(name= "vet_id",nullable = true)
    private User  vetId;

}
