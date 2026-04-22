package com.flacofitness.app.model.dto;

import com.flacofitness.app.model.enums.CategoriaGasto;

import java.math.BigDecimal;

public interface GastoPorCategoriaView {

    CategoriaGasto getCategoria();

    BigDecimal getTotal();
}
