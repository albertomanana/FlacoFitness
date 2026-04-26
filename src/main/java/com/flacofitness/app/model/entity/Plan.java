package com.flacofitness.app.model.entity;

import com.flacofitness.app.model.enums.TipoMembresia;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "planes")
@Getter
@Setter
@NoArgsConstructor
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_membresia", length = 30)
    private TipoMembresia tipoMembresia;

    @Column(columnDefinition = "TEXT")
    private String beneficios;

    @NotNull
    @Column(name = "precio_mensual", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioMensual;

    @NotNull
    @Column(name = "duracion_dias", nullable = false)
    private Integer duracionDias;

    @NotNull
    @Column(nullable = false)
    private Boolean activo;

    @JsonIgnore
    @OneToMany(mappedBy = "plan")
    private Set<Usuario> usuarios = new HashSet<>();
}
