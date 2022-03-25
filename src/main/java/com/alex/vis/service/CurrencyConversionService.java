package com.alex.vis.service;

import com.alex.vis.entity.Currency;
import com.alex.vis.service.impl.NbrbCurrencyConversionService;

public interface CurrencyConversionService {

    static CurrencyConversionService getInstance() {return new NbrbCurrencyConversionService(); }

    double getConversionRatio(Currency original, Currency target);
}
