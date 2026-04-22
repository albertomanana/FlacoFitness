package com.flacofitness.app.repository;

import java.math.BigDecimal;

import com.flacofitness.app.model.enums.CategoriaGasto;

public interface GastoPorCategoriaView {

    CategoriaGasto getCategoria();

    BigDecimal getTotal();
}
