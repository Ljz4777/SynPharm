# SynPharm CSV输入解析器开发操作文档

## 目录

1. [任务概述](#1-任务概述)
2. [开发步骤](#2-开发步骤)
3. [代码实现](#3-代码实现)
4. [测试验证](#4-测试验证)

---

## 1. 任务概述

### 1.1 目标

创建 **CsvInputParser**，支持通过CSV文件进行批量输入，解析多行预测数据。

### 1.2 所需文件

| 文件 | 状态 | 说明 |
|:---|:---|:---|
| `pipeline/impl/CsvInputParser.java` | ❌待创建 | CSV输入解析器 |

### 1.3 复用现有组件

| 文件 | 功能 | 复用性 |
|:---|:---|:---|
| `pipeline/InputParser.java` | 输入解析器接口 | ✅通用 |
| `dto/ParsedInput.java` | 统一输入格式DTO | ✅通用 |
| `enums/InputType.java` | 输入类型枚举 | ✅通用（已包含CSV） |

### 1.4 支持的数据流组合

创建后自动支持：

| InputType | AlgoType | OutputType | 状态 |
|:---|:---|:---|:---|
| csv | DTI | json | ✅自动支持 |
| csv | PPI | json | ✅自动支持（需PpiAlgoExecutor） |
| csv | DDI | json | ✅自动支持（需DdiAlgoExecutor） |

---

## 2. 开发步骤

### ⚠️ 前置条件（必须先完成）

| 序号 | 文件 | 状态 | 说明 |
|:---|:---|:---|:---|
| 1 | `enums/InputType.java` | ✅ | 输入类型枚举（已包含CSV） |
| 2 | `dto/ParsedInput.java` | ✅ | 统一输入格式DTO |
| 3 | `pipeline/InputParser.java` | ✅ | 输入解析器接口 |

### 步骤1：创建CsvInputParser文件

**文件夹**：`synpharm-backend/src/main/java/com/synpharm/pipeline/impl/`  
**文件**：`CsvInputParser.java`

### 步骤2：实现InputParser接口

需要实现两个方法：
1. `getInputType()` - 返回输入类型
2. `parse()` - 解析CSV文件，提取批量数据

### 步骤3：完成！

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
│  │ ② dto/ParsedInput.java         → 统一输入格式            │    │
│  │ ③ pipeline/InputParser.java    → 输入解析器接口          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              ↓                                  │
│  阶段2: 实现输入解析器（组员完成）                                │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ ④ pipeline/impl/CsvInputParser.java → CSV解析器         │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              ↓                                  │
│  阶段3: 自动注册（Spring完成）                                   │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ Spring启动时自动扫描并注册到DataPipelineFactory          │    │
│  └─────────────────────────────────────────────────────────┘    │
│                              ↓                                  │
│  阶段4: 验证测试（组员完成）                                      │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │ ⑤ 单元测试 → 编译 → 启动 → 接口测试                      │    │
│  └─────────────────────────────────────────────────────────┘    │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. 代码实现

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
        
        // TODO: 实现从文件读取CSV内容
        // Path path = Paths.get(fileUrl);
        // return Files.readString(path);
        
        // 当前使用模拟数据
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

### 3.2 代码讲解

#### parse方法流程

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

#### CSV格式要求

```csv
drug_smiles,target_seq
CC(=O)OC1=CC=CC=C1C(=O)O,MGLGLG...
C1CCCCC1,MVHLTE...
```

- 第一行为表头（自动跳过）
- 每行包含两个参数，逗号分隔
- 参数列表按顺序存储：`[参数1, 参数2, 参数3, 参数4, ...]`

#### 批量处理说明

`DataPipelineFactory.batchProcess()` 会按顺序每两个参数为一组进行处理：
- `params[0], params[1]` → 第一条预测
- `params[2], params[3]` → 第二条预测
- `params[4], params[5]` → 第三条预测
- ...

---

## 4. 测试验证

### 4.1 单元测试

```java
package com.synpharm.pipeline.impl;

import com.synpharm.dto.ParsedInput;
import com.synpharm.enums.InputType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CsvInputParserTest {

    private final CsvInputParser parser = new CsvInputParser();

    @Test
    void testGetInputType() {
        assertEquals(InputType.CSV, parser.getInputType());
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
        assertEquals("DrugA", result.getParams().get(0));
        assertEquals("TargetA", result.getParams().get(1));
        assertEquals("DrugB", result.getParams().get(2));
        assertEquals("TargetB", result.getParams().get(3));
    }

    @Test
    void testParseWithEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("", null));
    }

    @Test
    void testParseWithFileUrl() {
        ParsedInput result = parser.parse(null, "/uploads/batch.csv");
        
        assertNotNull(result);
        assertEquals("csv", result.getInputType());
    }
}
```

### 4.2 接口测试

使用Postman或curl测试：

```bash
POST /api/predict/general
Content-Type: application/json

{
    "inputType": "csv",
    "algoType": "DTI",
    "outputType": "json",
    "fileUrl": "/uploads/batch.csv"
}
```

### 4.3 验证步骤

1. ✅ 创建 `CsvInputParser.java` 文件
2. ✅ 编写代码，实现 `InputParser` 接口
3. ✅ 编译项目，确保无错误
4. ✅ 启动应用，查看日志：`注册输入解析器: csv`
5. ✅ 调用预测接口，确认返回正确格式
6. ✅ 测试异常输入，确认错误处理

---

## 附录：参考文件

| 文件 | 路径 | 说明 |
|:---|:---|:---|
| 参考示例 | `pipeline/impl/SmilesInputParser.java` | SMILES输入解析器实现（已完成） |
| 接口定义 | `pipeline/InputParser.java` | 输入解析器接口 |
| DTO | `dto/ParsedInput.java` | 统一输入格式 |

---

**版本**: v1.0.0  
**创建日期**: 2026-07-26  
**任务状态**: 待开发  
**负责人**: 组员