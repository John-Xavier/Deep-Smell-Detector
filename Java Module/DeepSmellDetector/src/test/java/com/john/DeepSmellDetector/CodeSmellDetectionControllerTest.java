package com.john.DeepSmellDetector;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CodeSmellDetectionControllerTest {

    @InjectMocks
    private CodeSmellDetectionController codeSmellDetectionController;


    @Mock
    private FeatureExtractor featureExtractor;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testDetectCodeSmell() {
        String javaCode = "public class TestClass {}";
        PredictionResponse mockResponse = new PredictionResponse();
        when(featureExtractor.extractFeatures()).thenReturn(mockResponse);
        PredictionResponse response = codeSmellDetectionController.detectCodeSmell(javaCode);
        assertEquals(mockResponse, response);
        verify(featureExtractor).extractFeatures();
    }
}
