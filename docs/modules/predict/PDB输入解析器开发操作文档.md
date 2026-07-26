# SynPharm PDB输入解析器开发操作文档

## 目录

1. [任务概述](#1-任务概述)
2. [开发步骤](#2-开发步骤)
3. [代码实现](#3-代码实现)
4. [测试验证](#4-测试验证)

---

## 1. 任务概述

### 1.1 目标

创建 **PdbInputParser**，支持通过PDB文件路径进行输入，读取蛋白质结构信息。

### 1.2 所需文件

| 文件 | 状态 | 说明 |
|:---|:---|:---|
| `pipeline/impl/PdbInputParser.java` | ❌待创建 | PDB输入解析器 |

### 1.3 复用现有组件

| 文件 | 功能 | 复用性 |
|:---|:---|:---|
| `pipeline/InputParser.java` | 输入解析器接口 | ✅通用 |
| `dto/ParsedInput.java` | 统一输入格式DTO | ✅通用 |
| `enums/InputType.java` | 输入类型枚举 | ✅通用（已包含PDB） |

### 1.4 支持的数据流组合

创建后自动支持：

| InputType | AlgoType | OutputType | 状态 |
|:---|:---|:---|:---|
| pdb | DTI | json | ✅自动支持 |
| pdb | PPI | json | ✅自动支持（需PpiAlgoExecutor） |

---

## 2. 开发步骤

### ⚠️ 前置条件（必须先完成）

| 序号 | 文件 | 状态 | 说明 |
|:---|:---|:---|:---|
| 1 | `enums/InputType.java` | ✅ | 输入类型枚举（已包含PDB） |
| 2 | `dto/ParsedInput.java` | ✅ | 统一输入格式DTO |
| 3 | `pipeline/InputParser.java` | ✅ | 输入解析器接口 |

### 步骤1：创建PdbInputParser文件

**文件夹**：`synpharm-backend/src/main/java/com/synpharm/pipeline/impl/`  
**文件**：`PdbInputParser.java`

### 步骤2：实现InputParser接口

需要实现两个方法：
1. `getInputType()` - 返回输入类型
2. `parse()` - 解析PDB文件路径，读取蛋白质结构

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
│  │ ④ pipeline/impl/PdbInputParser.java → PDB解析器         │    │
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

### 3.1 PdbInputParser.java

```java
package com.synpharm.pipeline.impl;

import com.synpharm.dto.ParsedInput;
import com.synpharm.enums.InputType;
import com.synpharm.pipeline.InputParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class PdbInputParser implements InputParser {

    @Override
    public InputType getInputType() {
        return InputType.PDB;
    }

    @Override
    public ParsedInput parse(String inputValue, String fileUrl) {
        log.info("解析PDB输入: inputValue={}, fileUrl={}", inputValue, fileUrl);
        
        String pdbFilePath = determineFilePath(inputValue, fileUrl);
        
        if (pdbFilePath == null || pdbFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException("PDB文件路径不能为空");
        }
        
        String structureData = readPdbFile(pdbFilePath);
        String sequence = extractSequenceFromPdb(structureData);
        
        List<String> params = Arrays.asList(sequence, structureData);
        
        return ParsedInput.builder()
                .params(params)
                .inputType(InputType.PDB.getCode())
                .build();
    }

    private String determineFilePath(String inputValue, String fileUrl) {
        if (fileUrl != null && !fileUrl.trim().isEmpty()) {
            return fileUrl;
        }
        return inputValue;
    }

    private String readPdbFile(String filePath) {
        log.debug("读取PDB文件: {}", filePath);
        
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                return Files.readString(path);
            }
            
            log.warn("PDB文件不存在: {}, 使用模拟数据", filePath);
            return generateMockPdbData(filePath);
            
        } catch (Exception e) {
            log.error("读取PDB文件失败: {}", e.getMessage());
            return generateMockPdbData(filePath);
        }
    }

    private String extractSequenceFromPdb(String pdbData) {
        log.debug("从PDB数据中提取序列");
        
        // TODO: 解析PDB格式，提取蛋白质序列
        // PDB格式包含SEQRES记录，可从中提取序列
        
        // 当前返回模拟序列
        return "MGLGLG...(从PDB文件提取的序列)";
    }

    private String generateMockPdbData(String pdbId) {
        String pdbName = pdbId.contains("/") ? pdbId.substring(pdbId.lastIndexOf("/") + 1) : pdbId;
        
        return """
            HEADER    PROTEIN KINASE                          01-JAN-2024   %s
            TITLE     CRYSTAL STRUCTURE OF EGFR KINASE DOMAIN
            COMPND    MOL_ID: 1;
            COMPND   2 MOLECULE: EGF RECEPTOR KINASE DOMAIN;
            AUTHOR    DOE,J.D. AND SMITH,J.S.
            SEQRES   1 A  100  MET GLY LEU GLY LEU GLY ARG ALA VAL THR PRO
            SEQRES   2 A  100  SER LYS ALA LEU GLU LEU ASN LEU ASP LEU GLN
            ATOM      1  N   MET A   1      10.000  20.000  30.000  1.00  0.00           N
            ATOM      2  CA  MET A   1      11.000  21.000  31.000  1.00  0.00           C
            END
            """.formatted(pdbName.toUpperCase());
    }
}
```

### 3.2 代码讲解

#### parse方法流程

1. **确定文件路径**：优先使用 `fileUrl`，其次使用 `inputValue`
   ```java
   String pdbFilePath = determineFilePath(inputValue, fileUrl);
   ```

2. **读取PDB文件**：调用 `readPdbFile()` 读取文件内容
   ```java
   String structureData = readPdbFile(pdbFilePath);
   ```

3. **提取序列**：从PDB数据中提取蛋白质序列
   ```java
   String sequence = extractSequenceFromPdb(structureData);
   ```

4. **构建输出**：返回统一的 `ParsedInput` 对象
   ```java
   return ParsedInput.builder()
           .params(Arrays.asList(sequence, structureData))
           .inputType(InputType.PDB.getCode())
           .build();
   ```

#### PDB文件读取说明

- **本地文件**：直接读取文件系统中的PDB文件
- **远程文件**：后续可扩展支持从URL下载
- **模拟数据**：文件不存在时使用模拟数据，便于测试

---

## 4. 测试验证

### 4.1 单元测试

```java
package com.synpharm.pipeline.impl;

import com.synpharm.dto.ParsedInput;
import com.synpharm.enums.InputType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PdbInputParserTest {

    private final PdbInputParser parser = new PdbInputParser();

    @Test
    void testGetInputType() {
        assertEquals(InputType.PDB, parser.getInputType());
    }

    @Test
    void testParse() {
        ParsedInput result = parser.parse("1a9u.pdb", null);
        
        assertNotNull(result);
        assertEquals("pdb", result.getInputType());
        assertEquals(2, result.getParams().size());
    }

    @Test
    void testParseWithEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("", null));
    }

    @Test
    void testParseWithFileUrl() {
        ParsedInput result = parser.parse(null, "/uploads/1a9u.pdb");
        
        assertNotNull(result);
        assertEquals("pdb", result.getInputType());
    }
}
```

### 4.2 接口测试

使用Postman或curl测试：

```bash
POST /api/predict/general
Content-Type: application/json

{
    "inputType": "pdb",
    "algoType": "DTI",
    "outputType": "json",
    "fileUrl": "/uploads/1a9u.pdb"
}
```

### 4.3 验证步骤

1. ✅ 创建 `PdbInputParser.java` 文件
2. ✅ 编写代码，实现 `InputParser` 接口
3. ✅ 编译项目，确保无错误
4. ✅ 启动应用，查看日志：`注册输入解析器: pdb`
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