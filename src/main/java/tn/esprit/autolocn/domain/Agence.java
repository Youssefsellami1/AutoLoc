package tn.esprit.autolocn.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agence")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Agence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idAgence;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String ville;

    @Column(nullable = false, length = 255)
    private String adresse;

    @Column(nullable = false, length = 20)
    private String telephone;

    // Une agence emploie plusieurs employés
    @OneToMany(mappedBy = "agence", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Employe> employes = new ArrayList<>();

    // Une agence possède plusieurs véhicules
    @OneToMany(mappedBy = "agence", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vehicule> vehicules = new ArrayList<>();
}
