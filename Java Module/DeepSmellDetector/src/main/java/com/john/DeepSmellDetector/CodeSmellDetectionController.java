package com.john.DeepSmellDetector;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CodeSmellDetectionController {
    @PostMapping("/detect")
    public PredictionResponse detectCodeSmell(@RequestBody String javacode) {
        FeatureExtractor extractor = new FeatureExtractor(javacode);
        return extractor.extractFeatures();
    }
}
