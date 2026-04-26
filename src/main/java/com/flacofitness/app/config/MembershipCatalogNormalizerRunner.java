package com.flacofitness.app.config;

import com.flacofitness.app.model.entity.Plan;
import com.flacofitness.app.model.enums.TipoMembresia;
import com.flacofitness.app.repository.PlanRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;

@Component
@Order(4)
public class MembershipCatalogNormalizerRunner implements ApplicationRunner {

    private final PlanRepository planRepository;

    public MembershipCatalogNormalizerRunner(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        Plan basico = findOrCreate("Basico");
        basico.setDescripcion("Plan mensual base para acceso general y operativa estable.");
        basico.setBeneficios("Acceso general, registro de asistencias y rutinas base.");
        basico.setPrecioMensual(new BigDecimal("29.00"));
        basico.setDuracionDias(30);
        basico.setTipoMembresia(TipoMembresia.MENSUAL);
        basico.setActivo(true);
        planRepository.save(basico);

        Plan estudiante = findOrCreate("Estudiante");
        estudiante.setDescripcion("Tarifa reducida y simple para perfiles jovenes.");
        estudiante.setBeneficios("Acceso general con precio reducido y seguimiento basico.");
        estudiante.setPrecioMensual(new BigDecimal("19.00"));
        estudiante.setDuracionDias(30);
        estudiante.setTipoMembresia(TipoMembresia.ESTUDIANTE);
        estudiante.setActivo(true);
        planRepository.save(estudiante);

        for (Plan plan : planRepository.findAll()) {
            String nombre = plan.getNombre() == null ? "" : plan.getNombre().trim().toLowerCase(Locale.ROOT);
            if (!"basico".equals(nombre) && !"estudiante".equals(nombre) && Boolean.TRUE.equals(plan.getActivo())) {
                plan.setActivo(false);
                planRepository.save(plan);
            }
        }
    }

    private Plan findOrCreate(String nombre) {
        return planRepository.findAll().stream()
                .filter(plan -> plan.getNombre() != null && plan.getNombre().equalsIgnoreCase(nombre))
                .findFirst()
                .orElseGet(() -> {
                    Plan plan = new Plan();
                    plan.setNombre(nombre);
                    return plan;
                });
    }
}
