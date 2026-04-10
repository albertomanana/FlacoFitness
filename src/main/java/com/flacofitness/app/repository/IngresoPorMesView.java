package com.flacofitness.app.repository;

import java.math.BigDecimal;

public interface IngresoPorMesView {

    Integer getAnio();

    Integer getMes();

    BigDecimal getTotal();
}
