package com.john.DeepSmellDetector;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;

public class FeatureExtractorTests {

        @InjectMocks
        private FeatureExtractor featureExtractor;

        @Mock
        private JavaParser javaParser;

        @Mock
        private ParseResult<CompilationUnit> parseResult;

        @Mock
        private CompilationUnit compilationUnit;

        @Before
        public void setUp() {
            MockitoAnnotations.initMocks(this);
        }

        @Test
        public void testExtractFeatures() {
            PredictionResponse expected = new PredictionResponse();
            expected.setErrorStatus("Java Class is invalid, Please try again");

            FeatureExtractor extractor = new FeatureExtractor(getJavaString());
            PredictionResponse prediction = extractor.extractFeatures();
            assertEquals(expected.getErrorStatus(), prediction.getErrorStatus());
            assertEquals(expected.getFeatureEnvy_method_name(), prediction.getFeatureEnvy_method_name());
            assertEquals(expected.getLong_method_name(), prediction.getLong_method_name());
            assertEquals(expected.getClassName(), prediction.getClassName());
        }
        private String getJavaString(){
            return "public class Person {\n" +
                    "\n" +
                    "    private String firstName;\n" +
                    "    private String lastName;\n" +
                    "    private int age;\n" +
                    "\n" +
                    "    // Constructor\n" +
                    "    public Person(String firstName, String lastName, int age) {\n" +
                    "        this.firstName = firstName;\n" +
                    "        this.lastName = lastName;\n" +
                    "        this.age = age;\n" +
                    "    }\n" +
                    "\n" +
                    "    // Getter for first name\n" +
                    "    public String getFirstName() {\n" +
                    "        return firstName;\n" +
                    "    }\n" +
                    "\n" +
                    "    // Setter for first name\n" +
                    "    public void setFirstName(String firstName) {\n" +
                    "        this.firstName = firstName;\n" +
                    "    }\n" +
                    "\n" +
                    "    // Getter for last name\n" +
                    "    public String getLastName() {\n" +
                    "        return lastName;\n" +
                    "    }\n" +
                    "\n" +
                    "    // Setter for last name\n" +
                    "    public void setLastName(String lastName) {\n" +
                    "        this.lastName = lastName;\n" +
                    "    }\n" +
                    "\n" +
                    "    // Getter for age\n" +
                    "    public int getAge() {\n" +
                    "        return age;\n" +
                    "    }\n" +
                    "\n" +
                    "    // Setter for age\n" +
                    "    public void setAge(int age) {\n" +
                    "        if (age > 0) { // simple validation\n" +
                    "            this.age = age;\n" +
                    "        }\n" +
                    "    }\n" +
                    "\n" +
                    "    // Method to get full name\n" +
                    "    public String getFullName() {\n" +
                    "        return firstName + \" \" + lastName;\n" +
                    "    }\n" +
                    "\n" +
                    "    @Override\n" +
                    "    public String toString() {\n" +
                    "        return \"Person [firstName=\" + firstName + \", lastName=\" + lastName + \", age=\" + age + \"]\";\n" +
                    "    }\n" +
                    "}\n";
        }

}
