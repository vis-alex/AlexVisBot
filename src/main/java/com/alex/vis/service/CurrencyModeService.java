package com.alex.vis.service;

import com.alex.vis.entity.Currency;
import com.alex.vis.service.impl.HashMapCurrencyModeService;

public interface CurrencyModeService {

    static CurrencyModeService getInstance() {
        return new HashMapCurrencyModeService();
    }

    Currency getOriginalCurrency(long chatId);

    Currency getTargetCurrency(long chatId);

    void setOriginalCurrency(long chatId, Currency currency);

    void setTargetCurrency(long chatId, Currency currency);

}
