package com.valtech.poc.ts.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

import com.valtech.poc.dto.TransactionDTO;
import com.valtech.poc.entities.TransactionMst;

@Mapper(componentModel = "spring")
@Component
public interface DtoMapper {

    @Mapping(source = "transFromAccNo", target = "fromAccountNo")
    @Mapping(source = "transToAccNo", target = "toAccountNo")
    @Mapping(source = "transAmount", target = "transactionAmount")
    @Mapping(source = "destCountryCode", target = "destinationCountryCode")
    @Mapping(source = "transStatus", target = "transactionStatus")

    TransactionMst toTMst(TransactionDTO tdto);



    @Mapping(target = "transFromAccNo", source = "fromAccountNo")
    @Mapping(target = "transToAccNo", source = "toAccountNo")
    @Mapping(target = "transAmount", source = "transactionAmount")
    @Mapping(target = "destCountryCode", source = "destinationCountryCode")
    @Mapping(target = "transStatus", source = "transactionStatus")
    TransactionDTO toTdto(TransactionMst tmst);
}
