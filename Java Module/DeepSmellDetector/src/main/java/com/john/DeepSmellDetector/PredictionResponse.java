package com.john.DeepSmellDetector;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PredictionResponse {
    private int lm_output;
    private int gc_output;
    private int fe_output;
    private String featureEnvy_method_name;
    private String long_method_name;
    private String className;
    private String errorStatus;


    public PredictionResponse(String error) {
        errorStatus = error;
    }
}