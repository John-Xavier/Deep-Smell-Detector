package com.john.DeepSmellDetector;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FeaturesResponse {
    private int methodLength;
    private int variablesCount;
    private int loopsCount;
    private int commentCount;
    private int conditionCount;
    private int cyclomaticComplexity;

}