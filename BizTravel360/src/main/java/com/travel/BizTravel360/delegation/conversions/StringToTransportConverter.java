package com.travel.BizTravel360.delegation.conversions;

import com.travel.BizTravel360.transport.Transport;
import com.travel.BizTravel360.transport.TransportService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class StringToTransportConverter implements Converter<String, Transport> {
    
    private final TransportService transportService;
    
    public StringToTransportConverter(TransportService transportService) {
        this.transportService = transportService;
    }
    
    @Override
    public Transport convert(String source) {
        Long transportId = Long.parseLong(source);
        try {
            return transportService.findTransportById(transportId);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
