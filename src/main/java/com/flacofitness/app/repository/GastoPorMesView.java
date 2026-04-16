package com.flacofitness.app.repository;

import java.math.BigDecimal;

public interface GastoPorMesView {

    int getAnio();

    int getMes();

    BigDecimal getTotal();
}