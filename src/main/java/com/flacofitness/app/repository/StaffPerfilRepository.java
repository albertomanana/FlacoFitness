package com.flacofitness.app.repository;

import com.flacofitness.app.model.entity.StaffPerfil;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StaffPerfilRepository extends JpaRepository<StaffPerfil, Long> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "usuario.plan"})
    List<StaffPerfil> findAll();

    @Override
    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "usuario.plan"})
    Optional<StaffPerfil> findById(Long id);

    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "usuario.plan"})
    List<StaffPerfil> findByActivoTrue();

    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "usuario.plan"})
    List<StaffPerfil> findByActivoTrueAndPuedeImpartirClasesTrue();

    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "usuario.plan"})
    List<StaffPerfil> findByActivoTrueAndNominaAutomaticaTrue();

    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "usuario.plan"})
    @Query("""
            select perfil from StaffPerfil perfil
            join perfil.usuario usuario
            where lower(concat(
                coalesce(usuario.nombre, ''), ' ',
                coalesce(usuario.apellidos, ''), ' ',
                coalesce(usuario.email, ''), ' ',
                coalesce(usuario.username, ''), ' ',
                coalesce(perfil.especialidad, ''), ' ',
                coalesce(cast(perfil.rolStaff as string), '')
            )) like lower(concat('%', :query, '%'))
            order by perfil.activo desc, usuario.nombre asc, usuario.apellidos asc, perfil.id asc
            """)
    List<StaffPerfil> searchTopForGlobal(@Param("query") String query, org.springframework.data.domain.Pageable pageable);

    @EntityGraph(attributePaths = {"usuario", "usuario.rol", "usuario.plan"})
    Optional<StaffPerfil> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);

    long countByActivoTrue();
}
