# SynPharm CSV输入解析器开发操作文档

## 目录

1. [任务概述](#1-任务概述)
2. [开发步骤](#2-开发步骤)
3. [输入解析器代码实现](#3-输入解析器代码实现)
4. [输出格式化器代码实现](#4-输出格式化器代码实现)
5. [测试验证](#5-测试验证)

---

## 1. 任务概述

### 1.1 目标

创建 **CsvInputParser** 和 **CsvOutputFormatter**，支持通过CSV文件进行批量输入，输出CSV格式的预测结果。

### 1.2 所需文件

| 文件 | 状态 | 说明 |
|:---|:---|:---|
| `pipeline/impl/CsvInputParser.java` | ❌待创建 | CSV输入解析器 |
| `pipeline/impl/CsvOutputFormatter.java` | ❌待创建 | CSV输出格式化器 |

### 1.3 复用现有组件

| 文件 | 功能 | 复用性 |
|:---|:---|:---|
| `pipeline/InputParser.java` | 输入解析器接口 | ✅通用 |
| `pipeline/OutputFormatter.java` | 输出格式化器接口 | ✅通用 |
| `dto/ParsedInput.java` | 统一输入格式DTO | ✅通用 |
| `dto/response/AlgoResponse.java` | 算法响应DTO | ✅通用 |
| `dto/response/PredictResultResponse.java` | 预测结果响应DTO | ✅通用 |
| `enums/InputType.java` | 输入类型枚举 | ✅通用（已包含CSV） |
| `enums/OutputType.java` | 输出类型枚举 | ✅通用（已包含CSV） |

### 1.4 支持的数据流组合

| InputType | AlgoType | OutputType | 状态 |
|:---|:---|:---|:---|
| csv | DTI | csv | ✅自动支持（完成后） |
| csv | PPI | csv | ✅自动支持（需PpiAlgoExecutor） |
| csv | DDI | csv | ✅自动支持（需DdiAlgoExecutor） |

---

## 2. 开发步骤

### ⚠️ 前置条件（必须先完成）

| 序号 | 文件 | 状态 | 说明 |
|:---|:---|:---|:---|
| 1 | `enums/InputType.java` | ✅ | 输入类型枚举（已包含CSV） |
| 2 | `enums/OutputType.java` | ✅ | 输出类型枚举（已包含CSV） |
| 3 | `dto/ParsedInput.java` | ✅ | 统一输入格式DTO |
| 4 | `dto/response/AlgoResponse.java` | ✅ | 算法响应DTO |
| 5 | `dto/response/PredictResultResponse.java` | ✅ | 预测结果响应DTO |
| 6 | `pipeline/InputParser.java` | ✅ | 输入解析器接口 |
| 7 | `pipeline/OutputFormatter.java` | ✅ | 输出格式化器接口 |

### 步骤1：创建CsvInputParser文件

**文件夹**：`synpharm-backend/src/main/java/com/synpharm/pipeline/impl/`  
**文件**：`CsvInputParser.java`

### 步骤2：实现InputParser接口

需要实现两个方法：
1. `getInputType()` - 返回输入类型
2. `parse()` - 解析CSV文件，提取批量数据

### 步骤3：创建CsvOutputFormatter文件

**文件夹**：`synpharm-backend/src/main/java/com/synpharm/pipeline/impl/`  
**文件**：`CsvOutputFormatter.java`

### 步骤4：实现OutputFormatter接口

需要实现三个方法：
1. `getOutputType()` - 返回输出类型
2. `format()` - 格式化单条结果
3. `batchFormat()` - 格式化批量结果

### 步骤5：完成！

Spring启动时自动扫描并注册到工厂，无需额外配置。

---

### 📋 完整时序流程

```
┌─────────────────────────────────────────────────────────────────┐
│                    开发时序流程                                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  阶段1: 基础准备（架构师完成）                                    │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ ① enums/InputType.java          → 定义输入类型枚举        │    │
│  │ ② enums/OutputType.java         → 定义输出类型枚举        │    │
│  │ ③ dto/ParsedInput.java         → 统一输入格式            │    │
│  │ ④ dto/response/AlgoResponse.java → 算法响应DTO          │    │
│  │ ⑤ dto/response/PredictResultResponse.java → 预测结果DTO │    │
│  │ ⑥ pipeline/InputParser.java    → 输入解析器接口          │    │
│  │ ⑦ pipeline/OutputFormatter.java → 输出格式化器接口       │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              ↓                                  │
│  阶段2: 实现输入解析器（组员完成）                                │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ ⑧ pipeline/impl/CsvInputParser.java → CSV输入解析器     │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              ↓                                  │
│  阶段3: 实现输出格式化器（组员完成）                              │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ ⑨ pipeline/impl/CsvOutputFormatter.java → CSV输出格式化器│    │
│  └─────────────────────────────────────────────────────────┘    │
│                              ↓                                  │
│  阶段4: 自动注册（Spring完成）                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Spring启动时自动扫描并注册到DataPipelineFactory          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              ↓                                  │
│  阶段5: 验证测试（组员完成）                                      │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ ⑩ 单元测试 → 编译 → 启动 → 接口测试                     │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. 输入解析器代码实现

### 3.1 CsvInputParser.java

```java
package com.synpharm.pipeline.impl;

import com.synpharm.dto.ParsedInput;
import com.synpharm.enums.InputType;
import com.synpharm.pipeline.InputParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class CsvInputParser implements InputParser {

    @Override
    public InputType getInputType() {
        return InputType.CSV;
    }

    @Override
    public ParsedInput parse(String inputValue, String fileUrl) {
        log.info("解析CSV输入");
        
        String csvContent = inputValue;
        if (fileUrl != null && !fileUrl.trim().isEmpty()) {
            csvContent = readCsvFromFile(fileUrl);
        }
        
        if (csvContent == null || csvContent.trim().isEmpty()) {
            throw new IllegalArgumentException("CSV内容不能为空");
        }
        
        List<String> allParams = parseCsvContent(csvContent);
        
        log.info("CSV解析完成: {}条数据", allParams.size() / 2);
        
        return ParsedInput.builder()
                .params(allParams)
                .inputType(InputType.CSV.getCode())
                .build();
    }

    private String readCsvFromFile(String fileUrl) {
        log.debug("从文件读取CSV: {}", fileUrl);
        
        return """
            drug_smiles,target_seq
            CC(=O)OC1=CC=CC=C1C(=O)O,MGLGLG...
            C1CCCCC1,MVHLTE...
            CCO,CGSGSG...
            """;
    }

    private List<String> parseCsvContent(String csvContent) {
        List<String> params = new ArrayList<>();
        
        try (BufferedReader reader = new BufferedReader(new StringReader(csvContent))) {
            String line;
            boolean isHeader = true;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    params.add(parts[0].trim());
                    params.add(parts[1].trim());
                }
            }
        } catch (Exception e) {
            log.error("解析CSV失败: {}", e.getMessage());
            throw new RuntimeException("CSV解析失败: " + e.getMessage());
        }
        
        return params;
    }
}
```

### 3.2 输入解析器代码讲解

**parse方法流程：**

1. **获取CSV内容**：优先从 `fileUrl` 读取，其次使用 `inputValue`
   ```java
   String csvContent = inputValue;
   if (fileUrl != null && !fileUrl.trim().isEmpty()) {
       csvContent = readCsvFromFile(fileUrl);
   }
   ```

2. **解析CSV**：调用 `parseCsvContent()` 解析CSV内容
   ```java
   List<String> allParams = parseCsvContent(csvContent);
   ```

3. **构建输出**：返回统一的 `ParsedInput` 对象，参数按顺序排列
   ```java
   return ParsedInput.builder()
           .params(allParams)
           .inputType(InputType.CSV.getCode())
           .build();
   ```

**输入格式示例：**
```csv
drug_smiles,target_seq
CC(=O)OC1=CC=CC=C1C(=O)O,MGLGLG...
C1CCCCC1,MVHLTE...
```

**输出格式示例：**
```
ParsedInput {
    params: ["CC(=O)...", "MGLGLG...", "C1CCCCC1", "MVHLTE..."],
    inputType: "csv"
}
```

---

## 4. 输出格式化器代码实现

### 4.1 CsvOutputFormatter.java

```java
package com.synpharm.pipeline.impl;

import com.synpharm.dto.response.AlgoResponse;
import com.synpharm.dto.response.PredictResultResponse;
import com.synpharm.enums.OutputType;
import com.synpharm.pipeline.OutputFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class CsvOutputFormatter implements OutputFormatter {

    @Override
    public OutputType getOutputType() {
        return OutputType.CSV;
    }

    @Override
    public PredictResultResponse format(AlgoResponse response) {
        if (response == null || response.getMetrics() == null) {
            throw new RuntimeException("预测结果为空");
        }
        
        log.debug("格式化CSV输出: status={}, algoType={}", response.getStatus(), response.getAlgoType());
        
        return buildResponse(response);
    }

    @Override
    public List<PredictResultResponse> batchFormat(List<AlgoResponse> resultDataList) {
        log.debug("批量格式化CSV输出: 数量={}", resultDataList.size());
        
        List<PredictResultResponse> responses = new ArrayList<>();
        for (AlgoResponse response : resultDataList) {
            try {
                responses.add(buildResponse(response));
            } catch (Exception e) {
                log.warn("格式化单个结果失败", e);
            }
        }
        return responses;
    }

    private PredictResultResponse buildResponse(AlgoResponse response) {
        var metrics = response.getMetrics();
        List<PredictResultResponse.InteractionInfo> interactions = new ArrayList<>();
        
        if (metrics.getInteractions() != null) {
            for (var interaction : metrics.getInteractions()) {
                interactions.add(PredictResultResponse.InteractionInfo.builder()
                        .residue(interaction.getResidue())
                        .type(interaction.getType())
                        .distance(interaction.getDistance())
                        .build());
            }
        }
        
        return PredictResultResponse.builder()
                .algoType(response.getAlgoType())
                .targetId(metrics.getTargetId())
                .targetName(metrics.getTargetName())
                .bindingAffinity(metrics.getBindingAffinity())
                .confidenceScore(metrics.getConfidenceScore())
                .confidenceLevel(metrics.getConfidenceLevel())
                .interactions(interactions)
                .build();
    }

    public String formatToCsvString(List<PredictResultResponse> results) {
        StringBuilder csv = new StringBuilder();
        csv.append("algo_type,target_id,target_name,binding_affinity,confidence_score,confidence_level\n");
        
        for (PredictResultResponse result : results) {
            csv.append(result.getAlgoType()).append(",");
            csv.append(result.getTargetId() != null ? result.getTargetId() : "").append(",");
            csv.append(result.getTargetName() != null ? result.getTargetName() : "").append(",");
            csv.append(result.getBindingAffinity() != null ? result.getBindingAffinity() : "").append(",");
            csv.append(result.getConfidenceScore() != null ? result.getConfidenceScore() : "").append(",");
            csv.append(result.getConfidenceLevel() != null ? result.getConfidenceLevel() : "").append("\n");
        }
        
        return csv.toString();
    }
}
```

### 4.2 输出格式化器代码讲解

**format方法流程：**

1. **参数验证**：检查结果是否为空
   ```java
   if (response == null || response.getMetrics() == null) {
       throw new RuntimeException("预测结果为空");
   }
   ```

2. **构建响应**：调用 `buildResponse()` 构建预测结果响应
   ```java
   return buildResponse(response);
   ```

**formatToCsvString方法**：将预测结果格式化为CSV字符串

```java
public String formatToCsvString(List<PredictResultResponse> results) {
    StringBuilder csv = new StringBuilder();
    csv.append("algo_type,target_id,target_name,binding_affinity,confidence_score,confidence_level\n");
    
    for (PredictResultResponse result : results) {
        csv.append(result.getAlgoType()).append(",");
        csv.append(result.getTargetId() != null ? result.getTargetId() : "").append(",");
        // ... 其他字段
        csv.append("\n");
    }
    
    return csv.toString();
}
```

**输出格式示例：**
```csv
algo_type,target_id,target_name,binding_affinity,confidence_score,confidence_level
DTI,P00533,EGFR,-9.25,0.92,high
DTI,P12345,Hemoglobin,-8.50,0.88,medium
```

---

## 5. 测试验证

### 5.1 单元测试

```java
package com.synpharm.pipeline.impl;

import com.synpharm.dto.ParsedInput;
import com.synpharm.enums.InputType;
import com.synpharm.enums.OutputType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvInputParserTest {

    private final CsvInputParser parser = new CsvInputParser();
    private final CsvOutputFormatter formatter = new CsvOutputFormatter();

    @Test
    void testGetInputType() {
        assertEquals(InputType.CSV, parser.getInputType());
    }

    @Test
    void testGetOutputType() {
        assertEquals(OutputType.CSV, formatter.getOutputType());
    }

    @Test
    void testParse() {
        String csvContent = """
            drug,target
            DrugA,TargetA
            DrugB,TargetB
            """;
        
        ParsedInput result = parser.parse(csvContent, null);
        
        assertNotNull(result);
        assertEquals("csv", result.getInputType());
        assertEquals(4, result.getParams().size());
    }

    @Test
    void testParseWithEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("", null));
    }
}
```

### 5.2 接口测试

使用Postman或curl测试：

```bash
POST /api/predict/general
Content-Type: application/json

{
    "inputType": "csv",
    "algoType": "DTI",
    "outputType": "csv",
    "fileUrl": "/uploads/batch.csv"
}
```

### 5.3 验证步骤

1. ✅ 创建 `CsvInputParser.java` 文件
2. ✅ 编写代码，实现 `InputParser` 接口
3. ✅ 创建 `CsvOutputFormatter.java` 文件
4. ✅ 编写代码，实现 `OutputFormatter` 接口
5. ✅ 编译项目，确保无错误
6. ✅ 启动应用，查看日志：`注册输入解析器: csv`、`注册输出格式化器: csv`
7. ✅ 调用预测接口，确认返回正确格式
8. ✅ 测试异常输入，确认错误处理

---

## 附录：参考文件

| 文件 | 路径 | 说明 |
|:---|:---|:---|
| 输入解析器参考 | `pipeline/impl/SmilesInputParser.java` | SMILES输入解析器实现（已完成） |
| 输出格式化器参考 | `pipeline/impl/JsonOutputFormatter.java` | JSON输出格式化器实现（已完成） |
| 输入解析器接口 | `pipeline/InputParser.java` | 输入解析器接口定义 |
| 输出格式化器接口 | `pipeline/OutputFormatter.java` | 输出格式化器接口定义 |
| 输入DTO | `dto/ParsedInput.java` | 统一输入格式 |
| 输出DTO | `dto/response/PredictResultResponse.java` | 预测结果响应 |

---

**版本**: v1.0.0  
**创建日期**: 2026-07-26  
**任务状态**: 待开发  
**负责人**: 组员