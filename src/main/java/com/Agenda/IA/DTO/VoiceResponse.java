package com.Agenda.IA.DTO;

import lombok.Data;

@Data
public class VoiceResponse {

    private String response;

    public VoiceResponse(String response){
        this.response = response;
    }

}