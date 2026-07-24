package com.synpharm.utils;

import com.synpharm.dto.request.PredictRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CsvUtils {

    private static final String[] DTI_HEADERS = {"drug_smiles", "target_seq"};
    private static final String[] PPI_HEADERS = {"protein_a", "protein_b"};
    private static final String[] DDI_HEADERS = {"drug_a", "drug_b"};

    private static final String[] DTI_RESULT_HEADERS = {"drug_smiles", "target_seq", "binding_affinity", "confidence_score", "confidence_level"};
    private static final String[] PPI_RESULT_HEADERS = {"protein_a", "protein_b", "confidence_score", "confidence_level"};
    private static final String[] DDI_RESULT_HEADERS = {"drug_a", "drug_b", "confidence_score", "confidence_level"};

    public static int countRows(String filePath) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            reader.readLine();
            while (reader.readLine() != null) {
                count++;
            }
        }
        return count;
    }

    public static List<String> readLines(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {
            String line;
            reader.readLine();
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static List<PredictRequest> parseLines(List<String> lines, String algoType) {
        List<PredictRequest> requests = new ArrayList<>();
        for (String line : lines) {
            PredictRequest request = parseLine(line, algoType);
            if (request != null) {
                requests.add(request);
            }
        }
        return requests;
    }

    public static PredictRequest parseLine(String line, String algoType) {
        String[] parts = parseCsvLine(line);
        
        if (parts.length < 2) {
            log.warn("CSV行格式错误，跳过: {}", line);
            return null;
        }

        return switch (algoType) {
            case "DTI" -> PredictRequest.forDTI(parts[0].trim(), parts[1].trim());
            case "PPI" -> PredictRequest.forPPI(parts[0].trim(), parts[1].trim());
            case "DDI" -> PredictRequest.forDDI(parts[0].trim(), parts[1].trim());
            default -> {
                log.warn("未知算法类型: {}", algoType);
                yield null;
            }
        };
    }

    private static String[] parseCsvLine(String line) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                parts.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString());

        return parts.toArray(new String[0]);
    }

    public static void writeResultFile(String resultPath, String algoType, List<Map<String, Object>> results) throws IOException {
        File resultFile = new File(resultPath);
        resultFile.getParentFile().mkdirs();

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(resultFile), StandardCharsets.UTF_8))) {
            String[] headers = getResultHeaders(algoType);
            writer.println(String.join(",", headers));

            for (var result : results) {
                writer.println(formatResultLine(result, algoType));
            }
        }
    }

    private static String[] getResultHeaders(String algoType) {
        return switch (algoType) {
            case "DTI" -> DTI_RESULT_HEADERS;
            case "PPI" -> PPI_RESULT_HEADERS;
            case "DDI" -> DDI_RESULT_HEADERS;
            default -> DTI_RESULT_HEADERS;
        };
    }

    private static String formatResultLine(Map<String, Object> result, String algoType) {
        return switch (algoType) {
            case "DTI" -> String.format("%s,%s,%s,%s,%s",
                    escapeCsv(String.valueOf(result.getOrDefault("drug_smiles", ""))),
                    escapeCsv(String.valueOf(result.getOrDefault("target_seq", ""))),
                    result.getOrDefault("binding_affinity", ""),
                    result.getOrDefault("confidence_score", ""),
                    result.getOrDefault("confidence_level", ""));
            case "PPI" -> String.format("%s,%s,%s,%s",
                    escapeCsv(String.valueOf(result.getOrDefault("protein_a", ""))),
                    escapeCsv(String.valueOf(result.getOrDefault("protein_b", ""))),
                    result.getOrDefault("confidence_score", ""),
                    result.getOrDefault("confidence_level", ""));
            case "DDI" -> String.format("%s,%s,%s,%s",
                    escapeCsv(String.valueOf(result.getOrDefault("drug_a", ""))),
                    escapeCsv(String.valueOf(result.getOrDefault("drug_b", ""))),
                    result.getOrDefault("confidence_score", ""),
                    result.getOrDefault("confidence_level", ""));
            default -> "";
        };
    }

    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}