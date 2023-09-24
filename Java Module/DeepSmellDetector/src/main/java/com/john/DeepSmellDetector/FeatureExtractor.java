package com.john.DeepSmellDetector;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParseStart;
import com.github.javaparser.Providers;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.*;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FeatureExtractor {

    private String javaCode;
    public FeatureExtractor(String javaCode) {
        this.javaCode = javaCode;
    }

    public PredictionResponse extractFeatures() {
        javaCode = cleanJavaCode(javaCode);//removes utf8 encoding and returns plain text
        JavaParser parser = new JavaParser();
        long wmcnamm = 0;//Weighted Methods per Class
        long locClass = 0;//lines of code in class
        long atfdCount=0;//Access to Foreign Data
        double localAccessesCount = 10;
        int cc = 1;
        String longMethodName = "";
        String feMethodName = "";
        String className = "";
        long locMethod = 0;
        ParseResult<CompilationUnit> result = parser.parse(ParseStart.COMPILATION_UNIT, Providers.provider(javaCode));
        if (result.isSuccessful()) {
            CompilationUnit cu = result.getResult().get();
        for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
             long newwmcnamm = classDecl.findAll(MethodDeclaration.class).stream()//
                    .filter(method -> !isAccessorOrMutator(method))
                    .count();
            if (newwmcnamm > wmcnamm){
                wmcnamm = newwmcnamm;
            }

            String classSource = classDecl.toString();

            // Split the source code by lines and filter out comments and blank lines.
             long newLocClass = classSource.lines()
                    .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("//"))
                    .count();
                if (newLocClass > locClass){
                    locClass = newLocClass;
                    wmcnamm = newwmcnamm;
                    className = classDecl.getNameAsString();
            }
        }
            List<String> classFields = new ArrayList<>();
            for (ClassOrInterfaceDeclaration classDecl : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                classFields.addAll(
                        classDecl.findAll(VariableDeclarator.class).stream()
                                .map(VariableDeclarator::getNameAsString)
                                .collect(Collectors.toList())
                );
            }
            for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {

                 long newatfdCount = method.findAll(FieldAccessExpr.class).stream()
                        .filter(fieldAccess -> !isAccessingOwnField(method, fieldAccess,classFields))
                        .count();

                List<FieldAccessExpr> accesses = method.findAll(FieldAccessExpr.class)
                        .stream()
                        .filter(fieldAccess -> !fieldAccess.getScope().toString().equals("System"))
                        .collect(Collectors.toList());

            if (accesses.size() > 0) {
                long accessCount = method.findAll(FieldAccessExpr.class).stream()
                        .filter(fieldAccess -> isAccessingOwnField(method, fieldAccess, classFields))
                        .count();
                double newlaa = (double) accessCount / accesses.size();
                if (newatfdCount>atfdCount&&newlaa<0.45){
                    atfdCount = newatfdCount;
                    localAccessesCount = newlaa;
                    feMethodName = method.getNameAsString();
                }
            }
            String methodSource = method.toString();
             long newlocMethod = methodSource.lines()
                    .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("//"))
                    .count();

            int newcc = 0;
             newcc += method.findAll(IfStmt.class).size();
            newcc += method.findAll(SwitchEntry.class).size();
            newcc += method.findAll(ForStmt.class).size();
            newcc += method.findAll(WhileStmt.class).size();
            newcc += method.findAll(DoStmt.class).size();
            newcc += method.findAll(CatchClause.class).size();
            newcc += method.findAll(BinaryExpr.class, b -> b.getOperator() == BinaryExpr.Operator.AND ||
                    b.getOperator() == BinaryExpr.Operator.OR).size();

            if (newlocMethod > locMethod){
                locMethod = newlocMethod;
                cc = newcc;
                longMethodName = method.getNameAsString();
            }
        }

            String jsonInputString = String.format(
                    "{\"LOC_method\": %d, \"CC_method\": %d, \"WMCNAMM_type\": %d, \"LOC_type\": %d, \"ATFD_method\": %d, \"LAA_method\": %f}",
                    locMethod, cc, wmcnamm, locClass, atfdCount, localAccessesCount
            );
        System.out.println("Made Request");
        System.out.println(jsonInputString);
         PredictionResponse prediction = makeRequest(jsonInputString);
         prediction.setClassName(className);
         prediction.setFeatureEnvy_method_name(feMethodName);
         prediction.setLong_method_name(longMethodName);
         prediction.setErrorStatus("Success");
        return prediction;
    }
        return new PredictionResponse("Java Class is invalid, Please try again");
    }
    private static String cleanJavaCode(String encodedString){
        String decodedString = "";
        if (encodedString != null) {
        String rawString = encodedString.replaceFirst("javaCode=", "");
            try {
                decodedString = URLDecoder.decode(rawString, "UTF-8");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else{
            System.out.println("encodedString is null");
        }
        return decodedString;
    }

    private static boolean isAccessingOwnField(MethodDeclaration method, FieldAccessExpr fieldAccess, List<String> classFields) {

        return classFields.contains(fieldAccess.getNameAsString());
    }

    private static boolean isAccessorOrMutator(MethodDeclaration method) {
        return method.getNameAsString().startsWith("get") || method.getNameAsString().startsWith("set");
    }
    public PredictionResponse makeRequest(String jsonInputString) {
        try {
            URL url = new URL("http://localhost:5000/predict");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; utf-8");
            conn.setRequestProperty("Accept", "application/json");
            conn.setDoOutput(true);
            System.out.println("");
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
                int responseCode = conn.getResponseCode();
                System.out.println(responseCode);  // Should be 200 if everything's ok
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try (BufferedReader br = new BufferedReader(
                            new InputStreamReader(conn.getInputStream(), "utf-8"))) {

                        StringBuilder response = new StringBuilder();
                        String responseLine;
                        while ((responseLine = br.readLine()) != null) {
                            response.append(responseLine.trim());
                        }
                        System.out.println(response.toString());
                        ObjectMapper objectMapper = new ObjectMapper();
                        try {
                            PredictionResponse prediction = objectMapper.readValue(response.toString(), PredictionResponse.class);
                           return prediction;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    }
                } else {
                    System.out.println("Failed to get a valid response from server.");
                    return null;
                }
            }

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);

        } catch (ProtocolException e) {
            throw new RuntimeException("Error setting up request method.", e);

        } catch (IOException e) {
            throw new RuntimeException("IO error occurred.", e);

        }
    }
}
