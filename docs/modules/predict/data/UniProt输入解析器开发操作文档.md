# SynPharm UniProt输入解析器开发操作文档

## 目录

1. [任务概述](#1-任务概述)
2. [开发步骤](#2-开发步骤)
3. [代码实现](#3-代码实现)
4. [测试验证](#4-测试验证)

---

## 1. 任务概述

### 1.1 目标

创建 **UniprotInputParser**，支持通过UniProt蛋白质ID进行输入，自动查询蛋白质序列。

### 1.2 所需文件

| 文件 | 状态 | 说明 |
|:---|:---|:---|
| `pipeline/impl/UniprotInputParser.java` | ❌待创建 | UniProt输入解析器 |

### 1.3 复用现有组件

| 文件 | 功能 | 复用性 |
|:---|:---|:---|
| `pipeline/InputParser.java` | 输入解析器接口 | ✅通用 |
| `dto/ParsedInput.java` | 统一输入格式DTO | ✅通用 |
| `enums/InputType.java` | 输入类型枚举 | ✅通用（已包含UNIPROT） |

### 1.4 支持的数据流组合

创建后自动支持：

| InputType | AlgoType | OutputType | 状态 |
|:---|:---|:---|:---|
| uniprot | DTI | json | ✅自动支持 |
| uniprot | PPI | json | ✅自动支持（需PpiAlgoExecutor） |

---

## 2. 开发步骤

### ⚠️ 前置条件（必须先完成）

| 序号 | 文件 | 状态 | 说明 |
|:---|:---|:---|:---|
| 1 | `enums/InputType.java` | ✅ | 输入类型枚举（已包含UNIPROT） |
| 2 | `dto/ParsedInput.java` | ✅ | 统一输入格式DTO |
| 3 | `pipeline/InputParser.java` | ✅ | 输入解析器接口 |

### 步骤1：创建UniprotInputParser文件

**文件夹**：`synpharm-backend/src/main/java/com/synpharm/pipeline/impl/`  
**文件**：`UniprotInputParser.java`

### 步骤2：实现InputParser接口

需要实现两个方法：
1. `getInputType()` - 返回输入类型
2. `parse()` - 解析UniProt ID，查询蛋白质序列

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
│  │ ④ pipeline/impl/UniprotInputParser.java → UniProt解析器 │    │
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

### 3.1 UniprotInputParser.java

```java
package com.synpharm.pipeline.impl;

import com.synpharm.dto.ParsedInput;
import com.synpharm.enums.InputType;
import com.synpharm.pipeline.InputParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class UniprotInputParser implements InputParser {

    @Override
    public InputType getInputType() {
        return InputType.UNIPROT;
    }

    @Override
    public ParsedInput parse(String inputValue, String fileUrl) {
        log.info("解析UniProt输入: {}", inputValue);
        
        if (inputValue == null || inputValue.trim().isEmpty()) {
            throw new IllegalArgumentException("UniProt输入不能为空");
        }
        
        String[] parts = inputValue.split(",");
        if (parts.length < 2) {
            throw new IllegalArgumentException("UniProt输入格式错误，需要逗号分隔的两个参数");
        }
        
        String uniprotId1 = parts[0].trim();
        String uniprotId2 = parts[1].trim();
        
        log.info("查询UniProt ID: {}, {}", uniprotId1, uniprotId2);
        
        String sequence1 = queryUniProtSequence(uniprotId1);
        String sequence2 = queryUniProtSequence(uniprotId2);
        
        List<String> params = Arrays.asList(sequence1, sequence2);
        
        return ParsedInput.builder()
                .params(params)
                .inputType(InputType.UNIPROT.getCode())
                .build();
    }

    private String queryUniProtSequence(String uniprotId) {
        log.debug("查询UniProt序列: {}", uniprotId);
        
        // TODO: 调用UniProt API查询蛋白质序列
        // 示例: https://rest.uniprot.org/uniprotkb/{uniprotId}.fasta
        
        // 当前使用模拟数据，后续替换为真实API调用
        if (uniprotId.equalsIgnoreCase("P00533")) {
            return "MGLGLG...(EGFR蛋白质序列)";
        }
        if (uniprotId.equalsIgnoreCase("P12345")) {
            return "MVHLTE...(血红蛋白序列)";
        }
        
        // 默认返回UniProt ID作为序列（用于测试）
        return uniprotId;
    }
}
```

### 3.2 代码讲解

#### parse方法流程

1. **参数验证**：检查输入是否为空
   ```java
   if (inputValue == null || inputValue.trim().isEmpty()) {
       throw new IllegalArgumentException("UniProt输入不能为空");
   }
   ```

2. **解析输入**：按逗号分割，提取两个UniProt ID
   ```java
   String[] parts = inputValue.split(",");
   String uniprotId1 = parts[0].trim();
   String uniprotId2 = parts[1].trim();
   ```

3. **查询序列**：调用 `queryUniProtSequence()` 查询蛋白质序列
   ```java
   String sequence1 = queryUniProtSequence(uniprotId1);
   String sequence2 = queryUniProtSequence(uniprotId2);
   ```

4. **构建输出**：返回统一的 `ParsedInput` 对象
   ```java
   return ParsedInput.builder()
           .params(Arrays.asList(sequence1, sequence2))
           .inputType(InputType.UNIPROT.getCode())
           .build();
   ```

#### queryUniProtSequence方法

当前使用模拟数据，后续需要替换为真实API调用：

```java
// 真实实现（后续）
private String queryUniProtSequence(String uniprotId) {
    // 调用UniProt REST API
    // GET https://rest.uniprot.org/uniprotkb/{uniprotId}.fasta
    // 解析FASTA格式，提取序列
}
```

---

## 4. 测试验证

### 4.1 单元测试

```java
package com.synpharm.pipeline.impl;

import com.synpharm.dto.ParsedInput;
import com.synpharm.enums.InputType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UniprotInputParserTest {

    private final UniprotInputParser parser = new UniprotInputParser();

    @Test
    void testGetInputType() {
        assertEquals(InputType.UNIPROT, parser.getInputType());
    }

    @Test
    void testParse() {
        ParsedInput result = parser.parse("P00533,P12345", null);
        
        assertNotNull(result);
        assertEquals("uniprot", result.getInputType());
        assertEquals(2, result.getParams().size());
    }

    @Test
    void testParseWithEmptyInput() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("", null));
    }

    @Test
    void testParseWithInvalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> parser.parse("P00533", null));
    }
}
```

### 4.2 接口测试

使用Postman或curl测试：

```bash
POST /api/predict/general
Content-Type: application/json

{
    "inputType": "uniprot",
    "algoType": "PPI",
    "outputType": "json",
    "inputValue": "P00533,P12345"
}
```

### 4.3 验证步骤

1. ✅ 创建 `UniprotInputParser.java` 文件
2. ✅ 编写代码，实现 `InputParser` 接口
3. ✅ 编译项目，确保无错误
4. ✅ 启动应用，查看日志：`注册输入解析器: uniprot`
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