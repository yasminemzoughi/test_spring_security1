package tn.esprit.entity.pets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Transient;
import tn.esprit.entity.user.User;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

@Setter
@Getter
@Entity
public class Pets {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Basic Information
    private String name;
    private String species;
    private int age;
    private String color;
    private String sex;

    @Column(nullable = false)
    private boolean forAdoption = false;
    private String location;
    // Descriptions
    @Column(length = 2000)
    private String description;
    // Media
    private String imagePath;
    // Relationships
    private Long ownerId;
}